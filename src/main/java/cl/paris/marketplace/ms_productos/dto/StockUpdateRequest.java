package cl.paris.marketplace.ms_productos.dto;

public record StockUpdateRequest(
    Integer cantidad // Positivo para sumar, negativo para restar stock
) {}