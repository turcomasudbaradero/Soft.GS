
package util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import exception.DatosInvalidosException;

// Clase auxiliar para validar los datos del sistema.
// Sus metodos seran utilizados por las entidades y los servicios.
public final class Validaciones {

    // Evitamos instanciar esta clase.
    private Validaciones() {
    }

    // Comprueba que un texto no sea nulo ni vacio.
    public static String textoObligatorio(String valor, String campo) {

        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo " + campo + " es obligatorio.");
        }

        return valor.trim();
    }

    // Comprueba que una cantidad sea mayor a cero.
    public static int cantidadPositiva(int cantidad) {

        if (cantidad <= 0) {
            throw new DatosInvalidosException(
                "La cantidad debe ser mayor a cero."
            );
        }

        return cantidad;
    }

    // Comprueba que una cantidad no sea negativa.
    public static int cantidadNoNegativa(
            int cantidad, String campo) {

        if (cantidad < 0) {
            throw new DatosInvalidosException(campo + " no puede ser negativo.");
        }

        return cantidad;
    }

    // Comprueba que un importe sea valido y no negativo.
    public static BigDecimal importeNoNegativo(
            BigDecimal importe, String campo) {

        if (importe == null || importe.signum() < 0) {
            throw new DatosInvalidosException(campo + " no puede ser nulo ni negativo.");
        }

        return importe.setScale(2, RoundingMode.HALF_UP);
    }
}