package cl.paris.marketplace.ms_productos.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoRequest(
        String sku,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        UUID categoriaId
) {}