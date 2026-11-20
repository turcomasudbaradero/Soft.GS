package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Function;

// Ejecuta una unidad de trabajo JPA con commit o rollback.
public final class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static <T> T ejecutar(Function<EntityManager, T> operacion) {

        EntityManager em = JpaUtil.getEntityManager();

        try {
            EntityTransaction tx = em.getTransaction();

            try {
                // Inicia la transacción.
                tx.begin();

                // Ejecuta la operación solicitada.
                T resultado = operacion.apply(em);

                // Confirma los cambios si todo salió bien.
                tx.commit();

                return resultado;

            } catch (RuntimeException ex) {

                // Si ocurre un error, revierte la transacción.
                if (tx.isActive()) {
                    try {
                        tx.rollback();
                    } catch (RuntimeException rollbackEx) {
                        // Conserva también el error producido durante el rollback.
                        ex.addSuppressed(rollbackEx);
                    }
                }

                // Propaga la excepción original.
                throw ex;
            }

        } finally {

            // Cierra el EntityManager al finalizar.
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}