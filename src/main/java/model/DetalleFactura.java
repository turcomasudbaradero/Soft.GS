package model;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;


// Entidad DetalleFactura. Representa cada artículo vendido dentro de una factura.
@Entity
@Table(name = "detalle_factura")
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_factura")
    private Integer id;

    // Factura a la que pertenece esta línea.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factura", nullable = false)
    private Factura factura;

    // Artículo vendido.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_articulo", nullable = false)
    private Articulo articulo;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    // Precio histórico de venta.
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal precioUnitario;

    // Descripción conservada al momento de la venta.
    @Column(name = "descripcion_articulo", nullable = false, length = 200)
    private String descripcionArticulo;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    private java.math.BigDecimal subtotal;

    // Constructor obligatorio para Hibernate.
    protected DetalleFactura() {
    }


    // Constructor principal de una línea de venta.
    public DetalleFactura(Articulo articulo, int cantidad, java.math.BigDecimal precioUnitario) {

        if (articulo == null) {
            throw new IllegalArgumentException("Debe indicar un artículo.");
        }

        if (!articulo.isActivo()) {
            throw new IllegalArgumentException("El artículo está inactivo.");
        }

        this.articulo = articulo;
        this.cantidad = Validaciones.cantidadPositiva(cantidad);
        this.precioUnitario = Validaciones.importeNoNegativo(precioUnitario, "Precio unitario");

        // Copiamos la descripción para preservar la información histórica de la factura.
        this.descripcionArticulo = articulo.getDescripcion();

        calcularSubtotal();
    }


    // Calcula el subtotal de la línea.
    public java.math.BigDecimal calcularSubtotal() {

        subtotal = precioUnitario.multiply(java.math.BigDecimal.valueOf(cantidad)).setScale(2, java.math.RoundingMode.HALF_UP);

        return subtotal;
    }


    // Asociación con la cabecera de factura.
    void asignarFactura(Factura factura) {
        this.factura = factura;
    }

    void desasociarFactura() {
        this.factura = null;
    }

    @PrePersist
    @PreUpdate
    protected void antesDeGuardar() {
        calcularSubtotal();
    }

    
    // GETTERS
    public Integer getId() {
        return id;
    }

    public Factura getFactura() {
        return factura;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public java.math.BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public String getDescripcionArticulo() {
        return descripcionArticulo;
    }

    public java.math.BigDecimal getSubtotal() {
        return calcularSubtotal();
    }
}