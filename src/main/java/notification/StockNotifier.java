package notification;

import model.Articulo;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

// Mantiene los observadores interesados en las transiciones de stock minimo.
public final class StockNotifier {
    
    private static final StockNotifier INSTANCIA = new StockNotifier();
    private final List<StockObserver> observadores = new CopyOnWriteArrayList<>();

    private StockNotifier() { }

    
    public static StockNotifier getInstancia() { return INSTANCIA; }

    
    public void registrar(StockObserver observer) {
        
        if (observer != null) observadores.add(observer);
    }

    
    public void quitar(StockObserver observer) { 
    
        observadores.remove(observer);
    }

    
    public void notificar(Articulo articulo, int anterior, int nuevo) {
        
        for (StockObserver observer : observadores) {
            try {
                observer.actualizarStock(articulo, anterior, nuevo);
            } catch (RuntimeException ex) {
                // Un fallo visual no debe revertir una venta ya confirmada.
                System.getLogger(StockNotifier.class.getName()).log(System.Logger.Level.ERROR, "No se pudo notificar stock bajo", ex);
            }
        }
    }
}