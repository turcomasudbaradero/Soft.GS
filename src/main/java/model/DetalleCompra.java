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

import java.math.BigDecimal;


// Entidad DetalleCompra. Representa cada artículo incluido en una compra.
@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_compra")
    private Integer id;

    // Relación con la cabecera de compra.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra compra;

    // Artículo adquirido.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_articulo", nullable = false)
    private Articulo articulo;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    // Conserva el precio histórico de la compra.
    @Column(name = "precio_unitario_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitarioCompra;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    // Constructor requerido por JPA.
    protected DetalleCompra() {
    }


    // Constructor para crear una línea de compra.
    public DetalleCompra(Articulo articulo, int cantidad, BigDecimal precioUnitarioCompra) {

        if (articulo == null) {
            throw new IllegalArgumentException("Debe indicar un artículo.");
        }

        if (!articulo.isActivo()) {
            throw new IllegalArgumentException("El artículo está inactivo.");
        }

        this.articulo = articulo;
        this.cantidad = Validaciones.cantidadPositiva(cantidad);
        this.precioUnitarioCompra = Validaciones.importeNoNegativo(precioUnitarioCompra, "Precio de compra");

        calcularSubtotal();
    }


    // Calcula el subtotal de la línea.
    public BigDecimal calcularSubtotal() {

        subtotal = precioUnitarioCompra.multiply(BigDecimal.valueOf(cantidad)).setScale(2, java.math.RoundingMode.HALF_UP);

        return subtotal;
    }


    // Asocia este detalle a su compra. Lo utiliza Compra.agregarDetalle().
    void asignarCompra(Compra compra) {
        this.compra = compra;
    }

    void desasociarCompra() {
        this.compra = null;
    }


    // Recalcula el subtotal antes de guardar o actualizar el registro.
    @PrePersist
    @PreUpdate
    protected void antesDeGuardar() {
        calcularSubtotal();
    }

    // GETTERS
    public Integer getId() {
        return id;
    }

    public Compra getCompra() {
        return compra;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitarioCompra() {
        return precioUnitarioCompra;
    }

    public BigDecimal getSubtotal() {
        return calcularSubtotal();
    }
}