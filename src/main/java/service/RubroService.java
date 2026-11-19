package service;

import model.Rubro;
import repository.RubroRepository;
import util.TransactionExecutor;
import java.util.List;

public class RubroService {
    
    public Rubro crear(String codigo, String nombre, String descripcion) {
    
        return TransactionExecutor.ejecutar(em -> {
            RubroRepository repo = new RubroRepository(em);
            if (repo.buscarPorCodigo(codigo.trim()).isPresent())
                throw new IllegalArgumentException("Ya existe un rubro con ese codigo.");
        
            return repo.guardar(new Rubro(codigo, nombre, descripcion));
        });
    }

    public void modificar(Integer id, String nombre, String descripcion) {
        
        TransactionExecutor.ejecutar(em -> {
            Rubro rubro = new RubroRepository(em).buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("No existe el rubro " + id));
            rubro.modificarDatos(nombre, descripcion);
            return null;
        });
    }

    public void desactivar(Integer id) {
        
        TransactionExecutor.ejecutar(em -> {
            Rubro rubro = new RubroRepository(em).buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("No existe el rubro " + id));
            rubro.desactivar();
            return null;
        });
    }

    public List<Rubro> listarTodos() {
        
        return TransactionExecutor.ejecutar(em -> new RubroRepository(em).listarTodos());
    }
}