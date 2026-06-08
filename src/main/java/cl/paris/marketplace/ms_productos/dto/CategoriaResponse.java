package cl.paris.marketplace.ms_productos.dto;

import java.util.UUID;

public record CategoriaResponse(
    UUID id,
    String nombre,
    String descripcion
) {}