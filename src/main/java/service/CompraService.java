package service;

import model.Articulo;
import model.Compra;
import model.DetalleCompra;
import enums.EstadoCompra;
import repository.ArticuloRepository;
import repository.CompraRepository;
import repository.ProveedorRepository;
import util.TransactionExecutor;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class CompraService {

    public Integer crearBorrador(String numero, Integer idProveedor, String observaciones) {
        
        return TransactionExecutor.ejecutar(em -> {
            var proveedor = new ProveedorRepository(em).buscarPorId(idProveedor).orElseThrow(() -> new IllegalArgumentException("No existe el proveedor."));
            Compra compra = new Compra(numero, proveedor, observaciones);
            new CompraRepository(em).guardar(compra);
            return compra.getId();
        });
    }

    public void agregarDetalle(Integer idCompra, Integer idArticulo, int cantidad, java.math.BigDecimal precioCompra) {
        
        TransactionExecutor.ejecutar(em -> {
            Compra compra = new CompraRepository(em).buscarPorIdConBloqueo(idCompra).orElseThrow(() -> new IllegalArgumentException("No existe la compra."));
            Articulo articulo = bloquearArticulo(em, idArticulo);
            DetalleCompra detalle = new DetalleCompra(articulo, cantidad, precioCompra);
            compra.agregarDetalle(detalle);
            em.persist(detalle); // El padre ya estaba persistido en una transaccion anterior.
            return null;
        });
    }

    public void quitarDetalle(Integer idCompra, Integer idArticulo) {
        
        TransactionExecutor.ejecutar(em -> {
            Compra compra = new CompraRepository(em).buscarPorIdConBloqueo(idCompra).orElseThrow(() -> new IllegalArgumentException("No existe la compra."));
            DetalleCompra detalle = compra.getDetalles().stream().filter(d -> d.getArticulo().getId().equals(idArticulo)).findFirst().orElseThrow(() -> new IllegalArgumentException("El articulo no esta en la compra."));
            compra.quitarDetalle(detalle);
            return null;
        });
    }

    public Compra registrarCompra(Compra compra) {
        
        if (compra == null) throw new IllegalArgumentException("La compra es obligatoria.");
        if (compra.getEstado() != EstadoCompra.BORRADOR)
            throw new IllegalArgumentException("Solo se puede registrar una compra en borrador.");
        return TransactionExecutor.ejecutar(em -> new CompraRepository(em).guardar(compra));
    }

    // Confirma la compra y aumenta todo el stock en una sola transaccion.
    public void confirmarCompra(Integer idCompra) {
        
        TransactionExecutor.ejecutar(em -> {
            Compra compra = new CompraRepository(em).buscarPorIdConBloqueo(idCompra).orElseThrow(() -> new IllegalArgumentException("No existe la compra " + idCompra));
            compra.validarParaConfirmar();
            for (DetalleCompra detalle : compra.getDetalles()) {
                Articulo articulo = bloquearArticulo(em, detalle.getArticulo().getId());
                if (!articulo.isActivo())
                    throw new IllegalStateException("La compra contiene un articulo inactivo.");
                articulo.incrementarStock(detalle.getCantidad());
            }
            compra.marcarConfirmada();
            return null;
        });
    }

    // Anula borradores; si estaba confirmada, revierte el ingreso de stock.
    public void anularCompra(Integer idCompra) {
        
        TransactionExecutor.ejecutar(em -> {
            Compra compra = new CompraRepository(em).buscarPorIdConBloqueo(idCompra).orElseThrow(() -> new IllegalArgumentException("No existe la compra " + idCompra));
            if (compra.getEstado() == EstadoCompra.ANULADA)
                throw new IllegalStateException("La compra ya esta anulada.");
            if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
                for (DetalleCompra detalle : compra.getDetalles()) {
                    Articulo articulo = bloquearArticulo(em, detalle.getArticulo().getId());
                    // Si la mercaderia ya se vendio, la anulacion se rechaza sin cambios parciales.
                    articulo.descontarStock(detalle.getCantidad());
                }
            }
            compra.marcarAnulada();
            return null;
        });
    }

    public List<Compra> listarComprasPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        
        validarRango(desde, hasta);
        return TransactionExecutor.ejecutar(em -> new CompraRepository(em).listarEntreFechas(desde, hasta));
    }

    public Compra buscarPorId(Integer id) {
        return TransactionExecutor.ejecutar(em -> new CompraRepository(em).buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la compra " + id)));
    }

    private Articulo bloquearArticulo(EntityManager em, Integer id) {
        
        return new ArticuloRepository(em).buscarPorIdConBloqueo(id).orElseThrow(() -> new IllegalArgumentException("No existe un articulo de la compra."));
    }

    private void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        
        if (desde == null || hasta == null || desde.isAfter(hasta))
            throw new IllegalArgumentException("El rango de fechas no es valido.");
    }
}