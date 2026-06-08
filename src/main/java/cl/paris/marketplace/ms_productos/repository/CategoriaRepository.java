package cl.paris.marketplace.ms_productos.repository;

import cl.paris.marketplace.ms_productos.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    
    
    Optional<Categoria> findByNombre(String nombre);
}