package exception;

// Excepcion que se utiliza cuando no se encuentra una entidad solicitada en el sistema.
public class EntidadNoEncontradaException extends RuntimeException {

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}