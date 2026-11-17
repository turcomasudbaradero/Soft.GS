package com.gestionstock;

import model.Rubro;

public class pruebaRubro {

    public static void main(String[] args) {

        // Creamos un nuevo rubro.
        Rubro rubro = new Rubro("R001", "Periféricos", "Teclados, mouse y otros accesorios");

        System.out.println("===== PRUEBA DE RUBRO =====");

        System.out.println("Código: " + rubro.getCodigo());

        System.out.println("Nombre: " + rubro.getNombre());

        System.out.println("Descripción: " + rubro.getDescripcion());

        System.out.println("Activo: " + rubro.isActivo());

        // Probamos la modificación.
        rubro.modificarDatos("Periféricos de computación", "Accesorios para computadoras");

        System.out.println("\nRubro modificado:");
        System.out.println(rubro);

        // Probamos la baja lógica.
        rubro.desactivar();

        System.out.println("Estado activo: " + rubro.isActivo());
    }
}