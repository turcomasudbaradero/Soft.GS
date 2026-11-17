package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;


// Clase encargada de administrar la persistencia utilizando Hibernate y JPA.4
public final class JpaUtil {

    // Nombre definido en persistence.xml
    private static final String UNIDAD_PERSISTENCIA = "gestion-stock-pu";

    // Instancia compartida para toda la aplicación.
    private static EntityManagerFactory emf;

    // Evitamos crear objetos de esta clase.
    private JpaUtil() {
    }

    // Devuelve la instancia de EntityManagerFactory.
    // Se inicializa una sola vez durante la ejecución.
    public static synchronized EntityManagerFactory
            getEntityManagerFactory() {

        if (emf == null || !emf.isOpen()) {

            Map<String, Object> propiedades = new HashMap<>();

            // Se pueden configurar las credenciales mediante
            // variables de entorno, sin escribirlas en el código.
            String usuario = System.getenv("MYSQL_USER");
            String clave = System.getenv("MYSQL_PASSWORD");

            if (usuario != null && !usuario.isBlank()) {
                propiedades.put("jakarta.persistence.jdbc.user", usuario);
            }

            if (clave != null) {
                propiedades.put("jakarta.persistence.jdbc.password", clave);
            }

            // Inicialización de Hibernate mediante JPA.
            emf = Persistence.createEntityManagerFactory(UNIDAD_PERSISTENCIA, propiedades);
        }

        return emf;
    }

    // Crea un EntityManager para realizar operaciones sobre la base de datos.
    public static EntityManager getEntityManager() {

        return getEntityManagerFactory()
                .createEntityManager();
    }

    // Cierra la fábrica de conexiones al finalizar la aplicación.
    public static synchronized void cerrar() {

        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}