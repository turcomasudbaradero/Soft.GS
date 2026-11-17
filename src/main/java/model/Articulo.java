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

import java.math.BigDecimal;


// Entidad Articulo. Representa cada producto comercializado por el mayorista.
// Administra los datos y las operaciones básicas relacionadas con el stock.
@Entity
@Table(name = "articulos")
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_articulo")
    private Integer id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    // Un artículo pertenece a un único rubro.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rubro", nullable = false)
    private Rubro rubro;

    // Un artículo tiene un proveedor habitual.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedorHabitual;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    // Constructor vacío requerido por JPA.
    protected Articulo() {
    }


    // Constructor principal.
    public Articulo(String codigo, String descripcion, Rubro rubro, Proveedor proveedorHabitual, int stockActual, int stockMinimo, BigDecimal precioUnitario) {

        this.codigo = Validaciones.textoObligatorio(codigo, "Código");
        this.descripcion = Validaciones.textoObligatorio(descripcion, "Descripción");
        this.rubro = validarRubro(rubro);
        this.proveedorHabitual = validarProveedor(proveedorHabitual);
        this.stockActual = Validaciones.cantidadNoNegativa(stockActual, "Stock actual");
        this.stockMinimo = Validaciones.cantidadNoNegativa(stockMinimo, "Stock mínimo");
        this.precioUnitario = Validaciones.importeNoNegativo(precioUnitario, "Precio unitario");

        this.activo = true;
    }

    
    private Rubro validarRubro(Rubro rubro) {

        if (rubro == null || !rubro.isActivo()) {
            throw new IllegalArgumentException("Debe seleccionar un rubro activo.");
        }

        return rubro;
    }

    
    private Proveedor validarProveedor(Proveedor proveedor) {

        if (proveedor == null || !proveedor.isActivo()) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor activo.");
        }

        return proveedor;
    }

    
    // Modifica los datos generales del artículo.
    public void modificarDatos(String descripcion, Rubro rubro, Proveedor proveedorHabitual) {

        this.descripcion = Validaciones.textoObligatorio(descripcion, "Descripción");
        this.rubro = validarRubro(rubro);
        this.proveedorHabitual = validarProveedor(proveedorHabitual);
    }

    
    // Actualiza el precio de venta.
    public void actualizarPrecio(BigDecimal nuevoPrecio) {

        this.precioUnitario = Validaciones.importeNoNegativo(nuevoPrecio, "Precio unitario");
    }

    
    // Permite actualizar el stock mínimo.
    public void actualizarStockMinimo(int nuevoMinimo) {

        this.stockMinimo = Validaciones.cantidadNoNegativa(nuevoMinimo, "Stock mínimo");
    }

    
    // Incrementa el stock. Será utilizado por CompraService.
    public void incrementarStock(int cantidad) {

        Validaciones.cantidadPositiva(cantidad);

        // Controlamos también posibles desbordamientos numéricos.
        this.stockActual = Math.addExact(this.stockActual, cantidad);
    }

    
    // Descuenta el stock. Será utilizado por FacturaService.
    public void descontarStock(int cantidad) {

        Validaciones.cantidadPositiva(cantidad);

        // Nunca permitimos que el stock quede negativo.
        if (cantidad > stockActual) {
            throw new IllegalStateException(
                    "Stock insuficiente para el artículo: "
                    + codigo);
        }

        this.stockActual -= cantidad;
    }


    // Comprueba si el artículo está en stock bajo.
    public boolean estaEnStockBajo() {

        return stockActual <= stockMinimo;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    // GETTERS
    public Integer getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Rubro getRubro() {
        return rubro;
    }

    public Proveedor getProveedorHabitual() {
        return proveedorHabitual;
    }

    public int getStockActual() {
        return stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public String toString() {
        return codigo + " - " + descripcion;
    }
}