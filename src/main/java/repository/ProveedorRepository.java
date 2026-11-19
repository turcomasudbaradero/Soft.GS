package repository;

import model.Proveedor;
import jakarta.persistence.EntityManager;
import java.util.Optional;

public class ProveedorRepository extends BaseRepository<Proveedor> {

    public ProveedorRepository(EntityManager em) {
        super(em, Proveedor.class);
    }

    // Busca un proveedor por su código.
    public Optional<Proveedor> buscarPorCodigo(String codigo) {

        return em.createQuery("SELECT p FROM Proveedor p WHERE p.codigo = :codigo", Proveedor.class).setParameter("codigo", codigo).getResultStream().findFirst();
    }

    // Busca un proveedor por su CUIT o CUIL.
    public Optional<Proveedor> buscarPorCuitCuil(String cuit) {

        return em.createQuery("SELECT p FROM Proveedor p WHERE p.cuitCuil = :cuit",Proveedor.class).setParameter("cuit", cuit).getResultStream().findFirst();
    }
}