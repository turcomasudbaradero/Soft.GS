package service;

import model.Cliente;
import repository.ClienteRepository;
import util.TransactionExecutor;
import java.util.List;

public class ClienteService {
    
    public Cliente crear(String codigo, String apellido, String nombre, String dni, String direccion, String telefono, String email) {
        
        return TransactionExecutor.ejecutar(em -> {
            ClienteRepository repo = new ClienteRepository(em);
            if (repo.buscarPorCodigo(codigo.trim()).isPresent())
                throw new IllegalArgumentException("El codigo de cliente ya existe.");
            if (repo.buscarPorDni(dni.trim()).isPresent())
                throw new IllegalArgumentException("El DNI ya esta registrado.");
            return repo.guardar(new Cliente(codigo, apellido, nombre, dni, direccion, telefono, email));
        });
    }

    
    public void modificar(Integer id, String apellido, String nombre, String direccion, String telefono, String email) {
        TransactionExecutor.ejecutar(em -> {
            Cliente c = new ClienteRepository(em).buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("No existe el cliente " + id));
            c.modificarDatos(apellido, nombre, direccion, telefono, email);
            return null;
        });
    }

    
    public void desactivar(Integer id) {
        
        TransactionExecutor.ejecutar(em -> {
            Cliente c = new ClienteRepository(em).buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("No existe el cliente " + id));
            c.desactivar();
            return null;
        });
    }

    
    public List<Cliente> listarTodos() {
        
        return TransactionExecutor.ejecutar(em -> new ClienteRepository(em).listarTodos());
    }
}