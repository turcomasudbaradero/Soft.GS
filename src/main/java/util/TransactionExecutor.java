package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Function;

// Ejecuta una unidad de trabajo JPA con commit o rollback atomico.
public final class TransactionExecutor {
    
    private TransactionExecutor() { 
    }

    public static <T> T ejecutar(Function<EntityManager, T> operacion) {
        
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            T resultado = operacion.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}