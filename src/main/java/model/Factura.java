package model;

import enums.EstadoFactura;
import util.Validaciones;
import exception.DatosInvalidosException;
import exception.OperacionNoPermitidaException;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// Entidad Factura. Representa una venta realizada a un cliente.
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Integer id;

    @Column(name = "numero_factura",nullable = false, unique = true, length = 30)
    private String numeroFactura;

    // Cada factura pertenece a un único cliente.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "total", nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoFactura estado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Una factura contiene múltiples detalles.
    @OneToMany(
        mappedBy = "factura",
        cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
        },
        orphanRemoval = true
    )
    private List<DetalleFactura> detalles = new ArrayList<>();

    // Constructor requerido por Hibernate.
    protected Factura() {
    }


    // Constructor principal.
    public Factura(String numeroFactura, Cliente cliente, String observaciones) {

        this.numeroFactura = Validaciones.textoObligatorio(numeroFactura, "Número de factura");

        if (cliente == null || !cliente.isActivo()) {
            throw new DatosInvalidosException("Debe seleccionar un cliente activo.");
        }

        this.cliente = cliente;
        this.observaciones = observaciones;
        this.fechaEmision = LocalDateTime.now();
        this.total = BigDecimal.ZERO.setScale(2);

        // Toda factura comienza como borrador.
        this.estado = EstadoFactura.BORRADOR;
    }


    // Agrega un artículo a la factura.
    public void agregarDetalle(DetalleFactura detalle) {

        verificarBorrador();

        if (detalle == null) {
            throw new DatosInvalidosException("El detalle no puede ser nulo.");
        }

        if (!detalle.getArticulo().isActivo()) {
            throw new DatosInvalidosException("El artículo está inactivo.");
        }

        // Evita incorporar dos veces el mismo artículo.
        for (DetalleFactura existente : detalles) {

            if (mismoArticulo(
                    existente.getArticulo(),
                    detalle.getArticulo())) {

                throw new DatosInvalidosException("El artículo ya está incluido en la factura.");
            }
        }

        detalles.add(detalle);

        // Sincronizamos el lado propietario de la relación.
        detalle.asignarFactura(this);

        calcularTotal();
    }


    // Quita un artículo de la factura. Solo se permite durante el estado BORRADOR.
    public void quitarDetalle(DetalleFactura detalle) {

        verificarBorrador();

        if (detalle == null || !detalles.contains(detalle)) {
            throw new DatosInvalidosException("El detalle no pertenece a esta factura.");
        }

        detalles.remove(detalle);
        detalle.desasociarFactura();

        calcularTotal();
    }


    // Evita duplicados comparando por identificador o por código único del artículo.
    private boolean mismoArticulo(Articulo a, Articulo b) {

        if (a.getId() != null && b.getId() != null) {
            return a.getId().equals(b.getId());
        }

        return a.getCodigo().equals(b.getCodigo());
    }

    private void verificarBorrador() {

        if (estado != EstadoFactura.BORRADOR) {
            throw new OperacionNoPermitidaException("Solo se puede modificar una factura en borrador.");
        }
    }


    // Calcula el total sumando todos los subtotales.
    public BigDecimal calcularTotal() {

        total = detalles.stream().map(DetalleFactura::calcularSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        return total;
    }


    // Valida todos los datos antes de emitir la factura.
    public void validarParaEmitir() {

        verificarBorrador();

        if (detalles.isEmpty()) {
            throw new DatosInvalidosException("La factura debe contener al menos un artículo.");
        }

        if (!cliente.isActivo()) {
            throw new DatosInvalidosException("El cliente está inactivo.");
        }

        for (DetalleFactura detalle : detalles) {

            if (!detalle.getArticulo().isActivo()) {
                throw new DatosInvalidosException("La factura contiene un artículo inactivo.");
            }
        }
    }


    // Cambia el estado de la factura a EMITIDA.
    // El descuento de stock se realizará posteriormente desde FacturaService dentro de una transacción.
    public void marcarEmitida() {

        validarParaEmitir();

        estado = EstadoFactura.EMITIDA;
    }


    // Anula la factura. La reposición de stock se coordina desde el servicio.
    public void marcarAnulada() {

        if (estado == EstadoFactura.ANULADA) {
            throw new OperacionNoPermitidaException("La factura ya está anulada.");
        }

        estado = EstadoFactura.ANULADA;
    }

    @PrePersist
    protected void antesDeGuardar() {

        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }

        calcularTotal();
    }

    
    // GETTERS
    public Integer getId() {
        return id;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public BigDecimal getTotal() {
        return calcularTotal();
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public List<DetalleFactura> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
}