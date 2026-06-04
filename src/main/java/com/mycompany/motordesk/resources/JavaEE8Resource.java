// Definición del paquete del proyecto
package com.mycompany.motordesk.resources;

// Importación de dependencias y clases necesarias
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author 
 */
@Path("javaee8")
// Clase pública JavaEE8Resource que gestiona la lógica correspondiente
public class JavaEE8Resource {
    
    @GET
    // Método público 'ping'
    public Response ping(){
        // Retornar el valor obtenido
        return Response
                .ok("ping")
                .build();
    }
}
