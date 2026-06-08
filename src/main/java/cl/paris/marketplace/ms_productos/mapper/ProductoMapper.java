package cl.paris.marketplace.ms_productos.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import cl.paris.marketplace.ms_productos.dto.CategoriaResponse;
import cl.paris.marketplace.ms_productos.dto.ProductoRequest;
import cl.paris.marketplace.ms_productos.dto.ProductoResponse;
import cl.paris.marketplace.ms_productos.model.Categoria;
import cl.paris.marketplace.ms_productos.model.Producto;

@Component
public class ProductoMapper {

    public Producto toProductoEntity(ProductoRequest request, Categoria categoria, UUID proveedorId) {
        Producto producto = new Producto();
        producto.setSku(request.sku());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoria(categoria);
        producto.setProveedorId(proveedorId);
        // Los campos activo y estadoModeracion se inicializan por defecto en la Entidad
        return producto;
    }

    public ProductoResponse toProductoResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getActivo(),
                producto.getEstadoModeracion(),
                producto.getCategoria().getId(),
                producto.getProveedorId()
        );
    }

    public CategoriaResponse toCategoriaResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}