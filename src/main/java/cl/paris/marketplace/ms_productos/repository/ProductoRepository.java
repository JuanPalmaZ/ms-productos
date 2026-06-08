package cl.paris.marketplace.ms_productos.repository;

import cl.paris.marketplace.ms_productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    // 1. Buscar por SKU (Crucial para buscar un producto específico por su código de barra/inventario)
    Optional<Producto> findBySku(String sku);

    // 2. Listar solo productos activos (Para el catálogo público de Paris.cl, ocultando los eliminados lógicamente)
    List<Producto> findByActivoTrue();

    // 3. Listar productos por Proveedor (Para que cuando el compañero proveedor entre a su panel, vea SOLO sus productos)
    List<Producto> findByProveedorId(UUID proveedorId);

    // 4. Listar por categoría y que estén activos (Para cuando el cliente navega en Paris.cl por ejemplo en "Tecnología")
    List<Producto> findByCategoriaIdAndActivoTrue(UUID categoriaId);
}