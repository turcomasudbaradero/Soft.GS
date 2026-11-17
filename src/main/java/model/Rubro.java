package model;

import util.Validaciones;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

// Entidad Rubro.
// Representa las categorías o familias de artículos del mayorista de productos informáticos.
// Se relaciona con la tabla rubros de MySQL.

@Entity
@Table(name = "rubros")
public class Rubro {

    // Identificador principal generado automáticamente por MySQL.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rubro")
    private Integer id;

    // Código único de identificación del rubro.
    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    // Nombre del rubro.
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    // Descripción opcional del rubro. En MySQL esta columna utiliza el tipo TEXT.
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // Indica si el rubro está habilitado.
    @Column(name = "activo", nullable = false)
    private boolean activo;


    // Constructor vacío requerido por Hibernate/JPA.
    protected Rubro() {
    }

    // Constructor principal. El ID se genera automáticamente en la base de datos.
    public Rubro(String codigo, String nombre, String descripcion) {

        this.codigo = Validaciones.textoObligatorio(codigo, "Código");
        this.nombre = Validaciones.textoObligatorio(nombre, "Nombre");
        this.descripcion = descripcion;

        // Todo rubro nuevo comienza activo.
        this.activo = true;
    }


    // Permite modificar los datos del rubro. El código se conserva para evitar modificar su identificación.
    public void modificarDatos(String nombre, String descripcion) {

        this.nombre = Validaciones.textoObligatorio(nombre, "Nombre");
        this.descripcion = descripcion;
    }


    // Realiza una baja lógica. No elimina físicamente el registro de MySQL.
    public void desactivar() {
        this.activo = false;
    }


    // Permite volver a habilitar un rubro.
    public void activar() {
        this.activo = true;
    }

    // GETTERS
    public Integer getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActivo() {
        return activo;
    }


    // Representación del objeto para mostrarlo fácilmente en JComboBox y otros componentes Swing.
    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}