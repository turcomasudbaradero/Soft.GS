package service;

import exception.DatosInvalidosException;
import exception.EntidadNoEncontradaException;
import exception.OperacionNoPermitidaException;
import exception.StockInsuficienteException;
import model.Articulo;
import model.DetalleFactura;
import model.Factura;
import enums.EstadoFactura;
import notification.StockNotifier;
import repository.ArticuloRepository;
import repository.FacturaRepository;
import repository.ClienteRepository;
import util.TransactionExecutor;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FacturaService {
    
    private record Alerta(Articulo articulo, int anterior, int nuevo) { }

    public Integer crearBorrador(String numero, Integer idCliente, String observaciones) {
    
        return TransactionExecutor.ejecutar(em -> {
            var cliente = new ClienteRepository(em).buscarPorId(idCliente).orElseThrow(() -> new EntidadNoEncontradaException("No existe el cliente."));
            Factura factura = new Factura(numero, cliente, observaciones);
            new FacturaRepository(em).guardar(factura);
            return factura.getId();
        });
    }

    // Agrega una linea tomando el precio vigente directamente del articulo.
    public void agregarDetalle(Integer idFactura, Integer idArticulo, int cantidad) {
        TransactionExecutor.ejecutar(em -> {
            Factura factura = new FacturaRepository(em).buscarPorIdConBloqueo(idFactura).orElseThrow(() -> new EntidadNoEncontradaException("No existe la factura."));
            Articulo articulo = bloquearArticulo(em, idArticulo);
            DetalleFactura detalle = new DetalleFactura(articulo, cantidad, articulo.getPrecioUnitario());
            factura.agregarDetalle(detalle);
            em.persist(detalle); // El padre ya estaba persistido en una transaccion anterior.
            return null;
        });
    }

    public void quitarDetalle(Integer idFactura, Integer idArticulo) {
        
        TransactionExecutor.ejecutar(em -> {
            Factura factura = new FacturaRepository(em).buscarPorIdConBloqueo(idFactura).orElseThrow(() -> new EntidadNoEncontradaException("No existe la factura."));
            DetalleFactura detalle = factura.getDetalles().stream().filter(d -> d.getArticulo().getId().equals(idArticulo)).findFirst().orElseThrow(() -> new IllegalArgumentException("El articulo no esta en la factura."));
            factura.quitarDetalle(detalle);
            return null;
        });
    }

    public Factura registrarFactura(Factura factura) {
        if (factura == null) throw new DatosInvalidosException("La factura es obligatoria.");
        if (factura.getEstado() != EstadoFactura.BORRADOR)
            throw new OperacionNoPermitidaException("Solo se puede registrar una factura en borrador.");
        return TransactionExecutor.ejecutar(em -> new FacturaRepository(em).guardar(factura));
    }

    // Verifica todos los articulos y descuenta stock atomicamente.
    // Las alertas se emiten despues del commit para no notificar un rollback.
    public void emitirFactura(Integer idFactura) {
        
        List<Alerta> alertas = TransactionExecutor.ejecutar(em -> {
            Factura factura = new FacturaRepository(em).buscarPorIdConBloqueo(idFactura).orElseThrow(() -> new EntidadNoEncontradaException("No existe la factura " + idFactura));
            factura.validarParaEmitir();

            // Primera pasada: bloquear y validar TODOS los articulos antes de descontar.
            for (DetalleFactura detalle : factura.getDetalles()) {
                Articulo articulo = bloquearArticulo(em, detalle.getArticulo().getId());
                
                if (!articulo.isActivo())
                    throw new OperacionNoPermitidaException("La factura contiene un articulo inactivo.");
                
                if (detalle.getCantidad() > articulo.getStockActual())
                    throw new StockInsuficienteException("Stock insuficiente para " + articulo.getCodigo());
            }

            // Segunda pasada: descontar. Si algo falla, la transaccion revierte todo.
            List<Alerta> eventos = new ArrayList<>();
            for (DetalleFactura detalle : factura.getDetalles()) {
                Articulo articulo = bloquearArticulo(em, detalle.getArticulo().getId());
                int anterior = articulo.getStockActual();
                articulo.descontarStock(detalle.getCantidad());
                int nuevo = articulo.getStockActual();
                
                if (anterior > articulo.getStockMinimo() && nuevo <= articulo.getStockMinimo()) {
                    eventos.add(new Alerta(articulo, anterior, nuevo));
                }
            }

            factura.marcarEmitida();
            return eventos;
        });

        // La transaccion ya fue confirmada cuando se llega aqui.
        for (Alerta alerta : alertas) {
            StockNotifier.getInstancia().notificar(alerta.articulo(), alerta.anterior(), alerta.nuevo());
        }
    }

    // Anula un borrador o repone stock si la factura ya fue emitida.
    public void anularFactura(Integer idFactura) {
        
        TransactionExecutor.ejecutar(em -> {
            Factura factura = new FacturaRepository(em).buscarPorIdConBloqueo(idFactura).orElseThrow(() -> new EntidadNoEncontradaException("No existe la factura " + idFactura));
            if (factura.getEstado() == EstadoFactura.ANULADA)
                throw new OperacionNoPermitidaException("La factura ya esta anulada.");
            
            if (factura.getEstado() == EstadoFactura.EMITIDA) {
                for (DetalleFactura detalle : factura.getDetalles()) {
                    Articulo articulo = bloquearArticulo(em, detalle.getArticulo().getId());
                    articulo.incrementarStock(detalle.getCantidad());
                }
            }
            factura.marcarAnulada();
            return null;
        });
    }

    public Factura buscarPorId(Integer id) {
        
        return TransactionExecutor.ejecutar(em -> new FacturaRepository(em).buscarPorId(id).orElseThrow(() -> new EntidadNoEncontradaException("No existe la factura " + id)));
    }

    public List<Factura> listarPorCliente(Integer idCliente) {
        
        return TransactionExecutor.ejecutar(em -> new FacturaRepository(em).listarPorCliente(idCliente));
    }

    public List<Factura> listarPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        
        if (desde == null || hasta == null || desde.isAfter(hasta))
            throw new DatosInvalidosException("El rango de fechas no es valido.");
        return TransactionExecutor.ejecutar(em -> new FacturaRepository(em).listarEntreFechas(desde, hasta));
    }

    // Recupera la factura y sus detalles para enviarlos a la impresion desde la GUI.
    public Factura prepararImpresion(Integer idFactura) {
        return TransactionExecutor.ejecutar(em -> new FacturaRepository(em).buscarParaImpresion(idFactura).orElseThrow(() -> new EntidadNoEncontradaException("No existe la factura " + idFactura)));
    }

    private Articulo bloquearArticulo(EntityManager em, Integer id) {
        return new ArticuloRepository(em).buscarPorIdConBloqueo(id).orElseThrow(() -> new EntidadNoEncontradaException("No existe un articulo de la factura."));
    }
}