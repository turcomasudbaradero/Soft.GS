package repository;

import model.Cliente;
import jakarta.persistence.EntityManager;
import java.util.Optional;

public class ClienteRepository extends BaseRepository<Cliente> {

    // Constructor del repositorio.
    public ClienteRepository(EntityManager em) {
        super(em, Cliente.class);
    }

    // Busca un cliente por su código.
    public Optional<Cliente> buscarPorCodigo(String codigo) {

        return em.createQuery("SELECT c FROM Cliente c WHERE c.codigo = :codigo", Cliente.class).setParameter("codigo", codigo).getResultStream().findFirst();
    }

    // Busca un cliente por su DNI.
    public Optional<Cliente> buscarPorDni(String dni) {

        return em.createQuery("SELECT c FROM Cliente c WHERE c.dni = :dni", Cliente.class).setParameter("dni", dni).getResultStream().findFirst();
    }
}