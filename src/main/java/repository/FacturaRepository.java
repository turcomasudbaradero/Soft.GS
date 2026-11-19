package repository;

import model.Factura;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class FacturaRepository extends BaseRepository<Factura> {

    // Constructor del repositorio.
    public FacturaRepository(EntityManager em) {
        super(em, Factura.class);
    }

    // Busca una factura por su ID y bloquea el registro durante la operación.
    public Optional<Factura> buscarPorIdConBloqueo(Integer id) {

        return Optional.ofNullable(em.find(Factura.class, id, LockModeType.PESSIMISTIC_WRITE));
    }

    // Busca una factura con sus datos asociados para imprimirla.
    public Optional<Factura> buscarParaImpresion(Integer id) {

        return em.createQuery("SELECT DISTINCT f FROM Factura f " + "LEFT JOIN FETCH f.cliente " + "LEFT JOIN FETCH f.detalles d " + "LEFT JOIN FETCH d.articulo " + "WHERE f.id = :id", Factura.class).setParameter("id", id).getResultStream().findFirst();
    }

    // Busca una factura por su número.
    public Optional<Factura> buscarPorNumero(String numero) {

        return em.createQuery("SELECT f FROM Factura f WHERE f.numeroFactura = :numero", Factura.class).setParameter("numero", numero).getResultStream().findFirst();
    }

    // Lista las facturas correspondientes a un cliente.
    public List<Factura> listarPorCliente(Integer idCliente) {

        return em.createQuery("SELECT DISTINCT f FROM Factura f " + "LEFT JOIN FETCH f.cliente " + "LEFT JOIN FETCH f.detalles d " + "LEFT JOIN FETCH d.articulo " + "WHERE f.cliente.id = :id " + "ORDER BY f.fechaEmision DESC", Factura.class).setParameter("id", idCliente).getResultList();
    }

    // Lista las facturas emitidas dentro de un período.
    public List<Factura> listarEntreFechas(LocalDateTime desde, LocalDateTime hasta) {

        return em.createQuery("SELECT DISTINCT f FROM Factura f " + "LEFT JOIN FETCH f.cliente " + "LEFT JOIN FETCH f.detalles d " + "LEFT JOIN FETCH d.articulo " + "WHERE f.fechaEmision >= :desde " + "AND f.fechaEmision <= :hasta " + "ORDER BY f.fechaEmision DESC", Factura.class).setParameter("desde", desde).setParameter("hasta", hasta).getResultList();
    }
}