package model;

import enums.EstadoCompra;
import util.Validaciones;

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


// Entidad Compra. Representa una compra de mercadería realizada a un proveedor.
@Entity
@Table(name = "compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Integer id;

    @Column(name = "numero_comprobante", nullable = false, unique = true, length = 30)
    private String numeroComprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Column(name = "total", nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    // El estado se almacena como texto en MySQL.
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCompra estado;

    @Column(name = "observaciones",
            columnDefinition = "TEXT")
    private String observaciones;

    // Una compra puede tener varios detalles.
    @OneToMany(
        mappedBy = "compra",
        cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
        },
        orphanRemoval = true
    )
    private List<DetalleCompra> detalles = new ArrayList<>();

    // Constructor requerido por Hibernate.
    protected Compra() {
    }


    // Constructor principal.
    public Compra(String numeroComprobante, Proveedor proveedor, String observaciones) {

        this.numeroComprobante = Validaciones.textoObligatorio(numeroComprobante, "Número de comprobante");

        if (proveedor == null || !proveedor.isActivo()) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor activo.");
        }

        this.proveedor = proveedor;
        this.observaciones = observaciones;
        this.fechaCompra = LocalDateTime.now();

        this.total = BigDecimal.ZERO.setScale(2);

        // Toda compra comienza como borrador.
        this.estado = EstadoCompra.BORRADOR;
    }


    // Agrega un detalle a la compra.
    public void agregarDetalle(DetalleCompra detalle) {

        verificarBorrador();

        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede ser nulo.");
        }

        if (!detalle.getArticulo().isActivo()) {
            throw new IllegalArgumentException("El artículo está inactivo.");
        }

        // Evita repetir el mismo artículo en dos líneas.
        for (DetalleCompra existente : detalles) {

            if (mismoArticulo(existente.getArticulo(), detalle.getArticulo())) {

                throw new IllegalArgumentException("El artículo ya está incluido en la compra.");
            }
        }

        detalles.add(detalle);

        // Mantenemos sincronizadas ambas relaciones JPA.
        detalle.asignarCompra(this);

        calcularTotal();
    }


    // Quita un detalle mientras la compra todavía se encuentra en borrador.
    public void quitarDetalle(DetalleCompra detalle) {

        verificarBorrador();

        if (detalle == null || !detalles.contains(detalle)) {
            throw new IllegalArgumentException("El detalle no pertenece a esta compra.");
        }

        detalles.remove(detalle);
        detalle.desasociarCompra();

        calcularTotal();
    }


    // Compara artículos usando su ID cuando existe, o el código único cuando todavía no están guardados.
    private boolean mismoArticulo(Articulo a, Articulo b) {

        if (a.getId() != null && b.getId() != null) {
            return a.getId().equals(b.getId());
        }

        return a.getCodigo().equals(b.getCodigo());
    }

    private void verificarBorrador() {

        if (estado != EstadoCompra.BORRADOR) {
            throw new IllegalStateException("Solo se puede modificar una compra en borrador.");
        }
    }


    // Calcula el total de la compra.
    public BigDecimal calcularTotal() {

        total = detalles.stream().map(DetalleCompra::calcularSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        return total;
    }


    // Comprueba que la compra tenga los datos necesarios antes de confirmar el ingreso de mercadería.
    public void validarParaConfirmar() {

        verificarBorrador();

        if (detalles.isEmpty()) {
            throw new IllegalStateException("La compra debe tener al menos un artículo.");
        }

        if (!proveedor.isActivo()) {
            throw new IllegalStateException("El proveedor está inactivo.");
        }

        for (DetalleCompra detalle : detalles) {
            if (!detalle.getArticulo().isActivo()) {
                throw new IllegalStateException("La compra contiene un artículo inactivo.");
            }
        }
    }


    // Cambia el estado a CONFIRMADA.
    // El incremento del stock se realizará posteriormente desde CompraService.
    public void marcarConfirmada() {

        validarParaConfirmar();

        estado = EstadoCompra.CONFIRMADA;
    }


    // Permite anular una compra. La reversión del stock corresponde al servicio.
    public void marcarAnulada() {

        if (estado == EstadoCompra.ANULADA) {
            throw new IllegalStateException("La compra ya está anulada.");
        }

        estado = EstadoCompra.ANULADA;
    }

    @PrePersist
    protected void antesDeGuardar() {

        if (fechaCompra == null) {
            fechaCompra = LocalDateTime.now();
        }

        calcularTotal();
    }

    
    // GETTERS
    public Integer getId() {
        return id;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public BigDecimal getTotal() {
        return calcularTotal();
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public List<DetalleCompra> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
}