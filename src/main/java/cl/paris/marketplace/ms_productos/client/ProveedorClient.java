package cl.paris.marketplace.ms_productos.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "ms-proveedores", 
        configuration = FeignClientConfig.class 
)
public interface ProveedorClient {

    // Llama a la ruta interna que creamos en ms-proveedores
    @GetMapping("/api/proveedores/interno/usuario/{usuarioId}/id")
    UUID obtenerIdProveedorInterno(@PathVariable("usuarioId") UUID usuarioId);
}