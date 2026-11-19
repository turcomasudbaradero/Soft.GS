package repository;

import model.Articulo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public class ArticuloRepository extends BaseRepository<Articulo> {

    // Constructor del repositorio.
    public ArticuloRepository(EntityManager em) {
        super(em, Articulo.class);
    }

    // Busca un artículo por su código.
    public Optional<Articulo> buscarPorCodigo(String codigo) {

        return em.createQuery("SELECT a FROM Articulo a WHERE a.codigo = :codigo", Articulo.class).setParameter("codigo", codigo).getResultStream().findFirst();
    }

    // Lista los artículos cuyo stock llegó al mínimo o está por debajo.
    public List<Articulo> listarStockBajo() {

        return em.createQuery("SELECT a FROM Articulo a " + "WHERE a.activo = true " + "AND a.stockActual <= a.stockMinimo " + "ORDER BY a.descripcion", Articulo.class).getResultList();
    }

    // Busca un artículo por ID aplicando bloqueo pesimista.
    public Optional<Articulo> buscarPorIdConBloqueo(Integer id) {

        return Optional.ofNullable(em.find(Articulo.class, id, LockModeType.PESSIMISTIC_WRITE));
    }
}