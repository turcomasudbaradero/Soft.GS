package service;

import model.Articulo;
import notification.StockNotifier;
import repository.ArticuloRepository;
import util.TransactionExecutor;
import java.util.List;

public class StockService {
    
    public List<Articulo> listarStockBajo() {
    
        return TransactionExecutor.ejecutar(em -> new ArticuloRepository(em).listarStockBajo());
    }

    // Ajuste manual de inventario; cantidad siempre positiva.
    public void ajustar(Integer idArticulo, int cantidad, boolean incrementar) {
    
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        int[] stock = TransactionExecutor.ejecutar(em -> {
            Articulo a = new ArticuloRepository(em).buscarPorIdConBloqueo(idArticulo).orElseThrow(() -> new IllegalArgumentException("No existe el articulo."));
            int anterior = a.getStockActual();
            
            if (incrementar) a.incrementarStock(cantidad); else a.descontarStock(cantidad);
            return new int[]{anterior, a.getStockActual(), a.getStockMinimo()};
        });
        
        // La notificacion se realiza solo despues del commit.
        if (stock[0] > stock[2] && stock[1] <= stock[2]) {
            notificarPorId(idArticulo, stock[0], stock[1]);
        }
    }

    private void notificarPorId(Integer id, int anterior, int nuevo) {
        Articulo articulo = TransactionExecutor.ejecutar(em ->
                new ArticuloRepository(em).buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("No existe el articulo.")));
        StockNotifier.getInstancia().notificar(articulo, anterior, nuevo);
    }
}