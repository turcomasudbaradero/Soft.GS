package service;

import exception.DatosInvalidosException;
import exception.EntidadNoEncontradaException;
import model.Articulo;
import model.Proveedor;
import model.Rubro;
import repository.ArticuloRepository;
import repository.ProveedorRepository;
import repository.RubroRepository;
import util.TransactionExecutor;
import java.math.BigDecimal;
import java.util.List;

public class ArticuloService {
    
    public Articulo crear(String codigo, String descripcion, Integer idRubro, Integer idProveedor, int stockInicial, int stockMinimo, BigDecimal precio) {
        
        return TransactionExecutor.ejecutar(em -> {
        
            ArticuloRepository articulos = new ArticuloRepository(em);
            
            if (articulos.buscarPorCodigo(codigo.trim()).isPresent())
                throw new DatosInvalidosException("El codigo de articulo ya existe.");
            Rubro rubro = new RubroRepository(em).buscarPorId(idRubro).orElseThrow(() -> new EntidadNoEncontradaException("No existe el rubro seleccionado."));
            Proveedor proveedor = new ProveedorRepository(em).buscarPorId(idProveedor).orElseThrow(() -> new EntidadNoEncontradaException("No existe el proveedor seleccionado."));
            return articulos.guardar(new Articulo(codigo, descripcion, rubro, proveedor, stockInicial, stockMinimo, precio));
        });
    }

    
    public void modificar(Integer id, String descripcion, Integer idRubro, Integer idProveedor, int stockMinimo, BigDecimal precio) {

        TransactionExecutor.ejecutar(em -> {
            ArticuloRepository repo = new ArticuloRepository(em);
            Articulo a = repo.buscarPorIdConBloqueo(id).orElseThrow(() -> new EntidadNoEncontradaException("No existe el articulo " + id));
            Rubro r = new RubroRepository(em).buscarPorId(idRubro).orElseThrow(() -> new EntidadNoEncontradaException("No existe el rubro."));
            Proveedor p = new ProveedorRepository(em).buscarPorId(idProveedor).orElseThrow(() -> new EntidadNoEncontradaException("No existe el proveedor."));
            a.modificarDatos(descripcion, r, p);
            a.actualizarStockMinimo(stockMinimo);
            a.actualizarPrecio(precio);
            return null;
        });
    }

    
    public void desactivar(Integer id) {
        TransactionExecutor.ejecutar(em -> {
            Articulo a = new ArticuloRepository(em).buscarPorId(id).orElseThrow(() -> new EntidadNoEncontradaException("No existe el articulo " + id));
            a.desactivar();
            return null;
        });
    }

    
    public List<Articulo> listarTodos() {
        return TransactionExecutor.ejecutar(em -> new ArticuloRepository(em).listarTodos());
    }

    
    public List<Articulo> listarStockBajo() {
        return TransactionExecutor.ejecutar(em -> new ArticuloRepository(em).listarStockBajo());
    }
}