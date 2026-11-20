package exception;

// Excepcion para datos ingresados incorrectamente.
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}