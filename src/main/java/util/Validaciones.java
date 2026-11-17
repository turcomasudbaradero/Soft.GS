package util;

import java.math.BigDecimal;

// Clase auxiliar para validar los datos del sistema.
// Sus métodos serán utilizados por las entidades y los servicios.
public final class Validaciones {

    // Evitamos instanciar esta clase.
    private Validaciones() {
    }

    // Comprueba que un texto no sea nulo ni vacío.
    public static String textoObligatorio(String valor, String campo) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }

        return valor.trim();
    }


    // Comprueba que una cantidad sea mayor a cero.
    public static int cantidadPositiva(int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        return cantidad;
    }

    // Comprueba que una cantidad no sea negativa.
    public static int cantidadNoNegativa(
            int cantidad, String campo) {

        if (cantidad < 0) {
            throw new IllegalArgumentException(campo + " no puede ser negativo.");
        }

        return cantidad;
    }

    // Comprueba que un importe sea válido y no negativo.
    public static BigDecimal importeNoNegativo(
            BigDecimal importe, String campo) {

        if (importe == null || importe.signum() < 0) {
            throw new IllegalArgumentException(campo + " no puede ser nulo ni negativo.");
        }

        return importe.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}