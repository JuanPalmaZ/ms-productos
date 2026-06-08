package cl.paris.marketplace.ms_productos.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.paris.marketplace.ms_productos.client.ProveedorClient; 
import cl.paris.marketplace.ms_productos.dto.CategoriaRequest;
import cl.paris.marketplace.ms_productos.dto.CategoriaResponse;
import cl.paris.marketplace.ms_productos.dto.ProductoRequest;
import cl.paris.marketplace.ms_productos.dto.ProductoResponse;
import cl.paris.marketplace.ms_productos.mapper.ProductoMapper;
import cl.paris.marketplace.ms_productos.model.Categoria;
import cl.paris.marketplace.ms_productos.model.Producto;
import cl.paris.marketplace.ms_productos.repository.CategoriaRepository;
import cl.paris.marketplace.ms_productos.repository.ProductoRepository;
import feign.FeignException;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;
    private final ProveedorClient proveedorClient;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoMapper productoMapper,
                           ProveedorClient proveedorClient) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoMapper = productoMapper;
        this.proveedorClient = proveedorClient;
    }

    // ==========================================
    // LÓGICA DE NEGOCIO: PRODUCTOS
    // ==========================================
    
    @Transactional
    public ProductoResponse registrarProducto(ProductoRequest request, UUID usuarioId) {
        if (productoRepository.findBySku(request.sku()).isPresent()) {
            throw new RuntimeException("El SKU '" + request.sku() + "' ya se encuentra registrado en el Marketplace.");
        }

        UUID proveedorId;
        try {
            proveedorId = proveedorClient.obtenerIdProveedorInterno(usuarioId);
        } catch (FeignException e) {
            throw new RuntimeException("Error: No se encontró un perfil de proveedor asociado a este usuario.");
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RuntimeException("La Categoría especificada no existe."));

        Producto producto = productoMapper.toProductoEntity(request, categoria, proveedorId);
        Producto productoGuardado = productoRepository.save(producto);
        return productoMapper.toProductoResponse(productoGuardado);
    }

    @Transactional
    public ProductoResponse modificarProducto(UUID id, ProductoRequest request, UUID usuarioId) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado para modificar."));

        UUID proveedorDueñoDelProducto = producto.getProveedorId();
        
        UUID miProveedorId;
        try {
            miProveedorId = proveedorClient.obtenerIdProveedorInterno(usuarioId);
        } catch (FeignException e) {
            throw new RuntimeException("Error: No se encontró un perfil de proveedor asociado a este usuario.");
        }

        if (!proveedorDueñoDelProducto.equals(miProveedorId)) {
            throw new RuntimeException("Acceso Denegado: No tienes permiso para modificar un producto que pertenece a otra empresa.");
        }

        if (!producto.getSku().equals(request.sku()) && productoRepository.findBySku(request.sku()).isPresent()) {
            throw new RuntimeException("El SKU '" + request.sku() + "' ya se encuentra registrado por otro artículo.");
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RuntimeException("La Categoría especificada no existe."));

        producto.setSku(request.sku());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setCategoria(categoria);

        Producto productoActualizado = productoRepository.save(producto);
        return productoMapper.toProductoResponse(productoActualizado);
    }

    // ==========================================
    // PUERTA TRASERA: ADMINISTRACIÓN
    // ==========================================
    @Transactional
    public void actualizarEstadoModeracion(UUID id, String estado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));
        
        producto.setEstadoModeracion(estado);
        
        // Si el admin lo rechaza, lo bajamos del catálogo activo inmediatamente
        if (estado.equals("RECHAZADO")) {
            producto.setActivo(false);
        } else if (estado.equals("APROBADO")) {
            producto.setActivo(true);
        }
        
        productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProductoPorId(UUID id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));
        return productoMapper.toProductoResponse(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosActivos() {
        return productoRepository.findByActivoTrue().stream()
                .map(productoMapper::toProductoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosPorProveedor(UUID usuarioId) {
        UUID proveedorId;
        try {
            proveedorId = proveedorClient.obtenerIdProveedorInterno(usuarioId);
        } catch (FeignException e) {
            throw new RuntimeException("Error: No se encontró un perfil de proveedor asociado.");
        }
        
        return productoRepository.findByProveedorId(proveedorId).stream()
                .map(productoMapper::toProductoResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoResponse actualizarStock(UUID id, Integer cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado para actualizar stock."));

        int nuevoStock = producto.getStock() + Math.toIntExact(cantidad);
        if (nuevoStock < 0) {
            throw new RuntimeException("Operación inválida: El stock no puede quedar en negativo. Stock actual: " + producto.getStock());
        }

        producto.setStock(nuevoStock);
        Producto productoActualizado = productoRepository.save(producto);
        return productoMapper.toProductoResponse(productoActualizado);
    }

    @Transactional
    public void eliminarProductoLogico(UUID id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));
        
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    // ==========================================
    // LÓGICA DE NEGOCIO: CATEGORÍAS
    // ==========================================
    
    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        if (categoriaRepository.findByNombre(request.nombre()).isPresent()) {
            throw new RuntimeException("La categoría ya existe en el sistema.");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());

        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return productoMapper.toCategoriaResponse(categoriaGuardada);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(productoMapper::toCategoriaResponse)
                .collect(Collectors.toList());
    }
}