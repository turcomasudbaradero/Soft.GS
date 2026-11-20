package exception;

// Excepcion para operaciones que intentan utilizar una cantidad de articulos superior al stock disponible.
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}