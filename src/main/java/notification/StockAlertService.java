package notification;

import model.Articulo;

// Observador simple para registrar alertas en consola/log.
public class StockAlertService implements StockObserver {
    
    @Override
    public void actualizarStock(Articulo articulo, int stockAnterior, int stockNuevo) {
        System.out.printf("ALERTA STOCK: %s - %s. Stock: %d (antes %d), minimo: %d%n", articulo.getCodigo(), articulo.getDescripcion(), stockNuevo, stockAnterior, articulo.getStockMinimo());
    }
}