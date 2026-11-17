package model;

import util.Validaciones;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

// Entidad Cliente. Representa a los compradores del mayorista.
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "apellido", nullable = false, length = 80)
    private String apellido;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    // El DNI debe ser único.
    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "email")
    private String email;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    // Constructor requerido por Hibernate.
    protected Cliente() {
    }


    // Constructor principal.
    public Cliente(String codigo, String apellido, String nombre, String dni, String direccion, String telefono, String email) {

        this.codigo = Validaciones.textoObligatorio(codigo, "Código");
        this.apellido = Validaciones.textoObligatorio(apellido, "Apellido");
        this.nombre = Validaciones.textoObligatorio(nombre, "Nombre");
        this.dni = Validaciones.textoObligatorio(dni, "DNI");

        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;

        this.activo = true;
    }


    // Modificación controlada de los datos del cliente. No se permite modificar el DNI mediante este método.
    public void modificarDatos(String apellido, String nombre, String direccion, String telefono, String email) {

        this.apellido = Validaciones.textoObligatorio(apellido, "Apellido");
        this.nombre = Validaciones.textoObligatorio(nombre, "Nombre");

        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
    }


    // Devuelve el nombre completo del cliente.
    public String getNombreCompleto() {
        return apellido + ", " + nombre;
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

    public String getApellido() {
        return apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDni() {
        return dni;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}