package notification;

import model.Articulo;

// Contrato del patron Observer para alertas de stock.
@FunctionalInterface
public interface StockObserver {
    
    void actualizarStock(Articulo articulo, int stockAnterior, int stockNuevo);
}