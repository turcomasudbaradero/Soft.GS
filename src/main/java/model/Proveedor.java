package model;

import util.Validaciones;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;


//Entidad Proveedor. Representa a las empresas o personas que suministran productos al mayorista.
@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer id;

    // Código interno único del proveedor.
    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    // Identificación tributaria única.
    @Column(name = "cuit_cuil", nullable = false, unique = true, length = 20)
    private String cuitCuil;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "email")
    private String email;

    // Permite realizar una baja lógica.
    @Column(name = "activo", nullable = false)
    private boolean activo;

    // Constructor obligatorio para Hibernate/JPA.
    protected Proveedor() {
    }


    // Constructor principal.
    public Proveedor(String codigo,String cuitCuil, String razonSocial, String direccion, String telefono, String email) {

        this.codigo = Validaciones.textoObligatorio(codigo, "Código");
        this.cuitCuil = Validaciones.textoObligatorio(cuitCuil, "CUIT/CUIL");
        this.razonSocial = Validaciones.textoObligatorio(razonSocial, "Razón social");

        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;

        // Por defecto, el proveedor queda activo.
        this.activo = true;
    }


    // Modifica los datos de contacto y razón social. El código y CUIT/CUIL se mantienen como identificadores.
    public void modificarDatos(
            String razonSocial, String direccion, String telefono, String email) {

        this.razonSocial = Validaciones.textoObligatorio(razonSocial, "Razón social");

        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
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

    public String getCuitCuil() {
        return cuitCuil;
    }

    public String getRazonSocial() {
        return razonSocial;
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
        return codigo + " - " + razonSocial;
    }
}