package exception;

// Excepcion para operaciones que no estan permitidas por las reglas de funcionamiento del sistema.
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}