package repository;

import model.Compra;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CompraRepository extends BaseRepository<Compra> {

    // Constructor del repositorio.
    public CompraRepository(EntityManager em) {
        super(em, Compra.class);
    }

    // Busca una compra por su ID y bloquea el registro durante la operación.
    public Optional<Compra> buscarPorIdConBloqueo(Integer id) {

        return Optional.ofNullable(em.find(Compra.class, id, LockModeType.PESSIMISTIC_WRITE));
    }

    // Lista las compras realizadas dentro de un período. El intervalo incluye las fechas inicial y final.
    public List<Compra> listarEntreFechas(LocalDateTime desde, LocalDateTime hasta) {

        return em.createQuery("SELECT DISTINCT c FROM Compra c " + "LEFT JOIN FETCH c.proveedor " + "LEFT JOIN FETCH c.detalles d " + "LEFT JOIN FETCH d.articulo " + "WHERE c.fechaCompra >= :desde " + "AND c.fechaCompra <= :hasta " + "ORDER BY c.fechaCompra DESC", Compra.class).setParameter("desde", desde).setParameter("hasta", hasta).getResultList();
    }
}