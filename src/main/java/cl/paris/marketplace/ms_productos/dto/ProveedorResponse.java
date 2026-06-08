package cl.paris.marketplace.ms_productos.dto;

import java.util.UUID;

public record ProveedorResponse(
        UUID id,
        Boolean activo
) {}