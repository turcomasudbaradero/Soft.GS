package repository;

import model.Rubro;
import jakarta.persistence.EntityManager;
import java.util.Optional;

public class RubroRepository extends BaseRepository<Rubro> {

    // Recibe el EntityManager y lo entrega al repositorio genérico.
    public RubroRepository(EntityManager em) {
        super(em, Rubro.class);
    }

    // Busca un rubro por su código.
    public Optional<Rubro> buscarPorCodigo(String codigo) {

        return em.createQuery("SELECT r FROM Rubro r WHERE r.codigo = :codigo", Rubro.class).setParameter("codigo", codigo).getResultStream().findFirst();
    }
}