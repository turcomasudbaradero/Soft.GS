package service;

import model.Proveedor;
import repository.ProveedorRepository;
import util.TransactionExecutor;
import java.util.List;

public class ProveedorService {
    
    public Proveedor crear(String codigo, String cuit, String razonSocial, String direccion, String telefono, String email) {
        
        return TransactionExecutor.ejecutar(em -> {
            ProveedorRepository repo = new ProveedorRepository(em);
            if (repo.buscarPorCodigo(codigo.trim()).isPresent())
                throw new IllegalArgumentException("El codigo de proveedor ya existe.");
        
            if (repo.buscarPorCuitCuil(cuit.trim()).isPresent())
                throw new IllegalArgumentException("El CUIT/CUIL ya esta registrado.");
            
            return repo.guardar(new Proveedor(codigo, cuit, razonSocial,
                    direccion, telefono, email));
        });
    }

    public void modificar(Integer id, String razonSocial, String direccion, String telefono, String email) {
        
        TransactionExecutor.ejecutar(em -> {
            Proveedor p = new ProveedorRepository(em).buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("No existe el proveedor " + id));
            p.modificarDatos(razonSocial, direccion, telefono, email);
            return null;
        });
    }

    public void desactivar(Integer id) {
        
        TransactionExecutor.ejecutar(em -> {
            Proveedor p = new ProveedorRepository(em).buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("No existe el proveedor " + id));
            if (p.isActivo()) {
                p.desactivar();
            } else {
                    p.activar();
            }
            return null;
        });
    }

    public List<Proveedor> listarTodos() {
        
        return TransactionExecutor.ejecutar(em -> new ProveedorRepository(em).listarTodos());
    }
}