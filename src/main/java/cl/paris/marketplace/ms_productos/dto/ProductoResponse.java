package cl.paris.marketplace.ms_productos.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoResponse(
    UUID id,
    String sku,
    String nombre,
    String descripcion,
    BigDecimal precio,
    Integer stock,
    Boolean activo,
    String estadoModeracion,
    UUID categoriaId,
    UUID proveedorId
) {}