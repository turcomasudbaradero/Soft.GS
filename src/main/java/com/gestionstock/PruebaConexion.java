package com.gestionstock;

import util.JpaUtil;
import jakarta.persistence.EntityManager;

public class PruebaConexion {
    
    public static void main(String[] args) {

        EntityManager em = null;

        try {

            System.out.println("============================");
            System.out.println("PRUEBA DE CONEXION CON MYSQL");
            System.out.println("============================");

            // Obtenemos un EntityManager.
            em = JpaUtil.getEntityManager();

            // Ejecutamos una consulta sencilla en MySQL.
            Object resultado = em.createNativeQuery("SELECT 1").getSingleResult();

            System.out.println("Conexion exitosa.");
            System.out.println("Resultado de MySQL: " + resultado);

        } catch (Exception e) {

            System.err.println("ERROR DE CONEXION:");
            System.err.println(e.getMessage());

            e.printStackTrace();

        } finally {

            // Cerramos los recursos utilizados.
            if (em != null && em.isOpen()) {
                em.close();
            }

            JpaUtil.cerrar();
        }
    }

}