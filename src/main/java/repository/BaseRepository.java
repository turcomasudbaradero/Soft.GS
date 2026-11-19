package repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;
import java.util.Optional;


// Repositorio genérico. Contiene las operaciones comunes de persistencia para todas las entidades del sistema.
public abstract class BaseRepository<T> {

    protected final EntityManager em;

    private final Class<T> tipoEntidad;

    // Cada repositorio informa qué entidad administra.
    public BaseRepository(EntityManager em, Class<T> tipoEntidad) {
        this.em = em;
        this.tipoEntidad = tipoEntidad;
    }

    // Guarda una nueva entidad.
    public T guardar(T entidad) {
        em.persist(entidad);
        return entidad;
    }

    // Actualiza una entidad existente.
    public T actualizar(T entidad) {
        return em.merge(entidad);
    }

    // * Busca una entidad mediante su identificador.
    public Optional<T> buscarPorId(Integer id) {
        return Optional.ofNullable(em.find(tipoEntidad, id));
    }

    // Recupera todos los registros de la entidad. Este método será heredado por todos los Repository.
    public List<T> listarTodos() {

        CriteriaQuery<T> consulta = em.getCriteriaBuilder().createQuery(tipoEntidad);

        consulta.select(consulta.from(tipoEntidad));

        return em.createQuery(consulta).getResultList();
    }

    // Cuenta la cantidad total de registros.
    public long contar() {

        CriteriaQuery<Long> consulta = em.getCriteriaBuilder().createQuery(Long.class);

        consulta.select(em.getCriteriaBuilder().count(consulta.from(tipoEntidad)));

        return em.createQuery(consulta).getSingleResult();
    }
}