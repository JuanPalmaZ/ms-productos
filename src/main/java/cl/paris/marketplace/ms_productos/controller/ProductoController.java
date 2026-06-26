package cl.paris.marketplace.ms_productos.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; 
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping; 
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.paris.marketplace.ms_productos.dto.CategoriaRequest;
import cl.paris.marketplace.ms_productos.dto.CategoriaResponse;
import cl.paris.marketplace.ms_productos.dto.ProductoRequest;
import cl.paris.marketplace.ms_productos.dto.ProductoResponse;
import cl.paris.marketplace.ms_productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Endpoints para la gestión del catálogo de productos y categorías")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // ==========================================
    // CRUD: PRODUCTOS
    // ==========================================

    @Operation(summary = "Registra un nuevo producto en el catálogo")
    @ApiResponse(responseCode = "201", description = "Producto registrado exitosamente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Payload con los detalles del producto",
        content = @Content(
            examples = @ExampleObject(
                name = "EjemploCrearProducto",
                value = "{\n  \"sku\": \"TEC-001\",\n  \"nombre\": \"Notebook Gamer\",\n  \"descripcion\": \"Notebook 15 pulgadas, 16GB RAM\",\n  \"precio\": 599990.00,\n  \"stock\": 10,\n  \"categoriaId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"\n}"
            )
        )
    )
    @PreAuthorize("hasRole('PROVEEDOR') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductoResponse> registrarProducto(
            @Valid @RequestBody ProductoRequest request,
            Authentication authentication
    ) {
        UUID usuarioId = UUID.fromString(authentication.getCredentials().toString());
        ProductoResponse response = productoService.registrarProducto(request, usuarioId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Modifica los detalles de un producto existente")
    @ApiResponse(responseCode = "200", description = "Producto modificado exitosamente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Payload con los datos actualizados del producto",
        content = @Content(
            examples = @ExampleObject(
                name = "EjemploModificarProducto",
                value = "{\n  \"sku\": \"TEC-001\",\n  \"nombre\": \"Notebook Gamer Actualizado\",\n  \"descripcion\": \"Nueva descripción del producto\",\n  \"precio\": 550000.00,\n  \"stock\": 15,\n  \"categoriaId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"\n}"
            )
        )
    )
    @PreAuthorize("hasRole('PROVEEDOR') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> modificarProducto(
            @PathVariable UUID id,
            @Valid @RequestBody ProductoRequest request,
            Authentication authentication
    ) {
        UUID usuarioId = UUID.fromString(authentication.getCredentials().toString());
        ProductoResponse response = productoService.modificarProducto(id, request, usuarioId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Realiza un borrado lógico de un producto")
    @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente")
    @PreAuthorize("hasRole('PROVEEDOR') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProductoLogico(@PathVariable UUID id) {
        productoService.eliminarProductoLogico(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualiza el stock disponible de un producto sumando o restando unidades")
    @ApiResponse(responseCode = "200", description = "Stock actualizado exitosamente")
    @PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN', 'CLIENTE')")
    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductoResponse> actualizarStock(
            @PathVariable UUID id,
            @RequestParam Integer cantidad) {
        ProductoResponse response = productoService.actualizarStock(id, cantidad);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ENDPOINT ADMINISTRATIVO (Llamado vía Feign)
    // ==========================================
    
    @Operation(summary = "Actualiza el estado de moderación de un producto (Administrativo)")
    @ApiResponse(responseCode = "200", description = "Estado de moderación actualizado exitosamente")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/estado-moderacion")
    public ResponseEntity<Void> actualizarEstadoModeracion(
            @PathVariable UUID id, 
            @RequestParam String estado) {
        
        productoService.actualizarEstadoModeracion(id, estado);
        return ResponseEntity.ok().build();
    }

    // ==========================================
    // ENDPOINTS: CONSULTAS
    // ==========================================

    @Operation(summary = "Obtiene los detalles completos de un producto según su ID")
    @ApiResponse(responseCode = "200", description = "Producto obtenido exitosamente")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(@PathVariable UUID id) {
        ProductoResponse response = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista todos los productos activos en el marketplace")
    @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarProductosActivos() {
        List<ProductoResponse> response = productoService.listarProductosActivos();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista todos los productos asociados a un usuario proveedor específico")
    @ApiResponse(responseCode = "200", description = "Productos del proveedor listados exitosamente")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROVEEDOR') and #usuarioId.toString() == authentication.credentials)")
    @GetMapping("/proveedor/usuario/{usuarioId}")
    public ResponseEntity<List<ProductoResponse>> listarProductosPorProveedor(@PathVariable UUID usuarioId) {
        List<ProductoResponse> response = productoService.listarProductosPorProveedor(usuarioId);
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ENDPOINTS: CATEGORÍAS
    // ==========================================

    @Operation(summary = "Crea una nueva categoría para clasificar productos")
    @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Payload con los datos de la nueva categoría",
        content = @Content(
            examples = @ExampleObject(
                name = "EjemploCategoria",
                value = "{\n  \"nombre\": \"Electrónica\",\n  \"descripcion\": \"Dispositivos tecnológicos, computación y audio\"\n}"
            )
        )
    )
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMIN')")
    @PostMapping("/categorias")
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = productoService.crearCategoria(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtiene la lista de todas las categorías disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMIN')")
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaResponse>> listarCategorias() {
        List<CategoriaResponse> response = productoService.listarCategorias();
        return ResponseEntity.ok(response);
    }
}