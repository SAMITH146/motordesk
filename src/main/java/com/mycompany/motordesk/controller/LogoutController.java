// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller; // Paquete que agrupa todos los controladores de MotorDesk

import java.io.IOException; // Excepcion necesaria para manejar errores de entrada/salida del servlet
import javax.servlet.annotation.WebServlet; // Anotacion que mapea este servlet a una URL
import javax.servlet.http.HttpServlet; // Clase base del servlet HTTP
import javax.servlet.http.HttpServletRequest; // Representa la peticion HTTP del cliente
import javax.servlet.http.HttpServletResponse; // Representa la respuesta HTTP al cliente
import javax.servlet.http.HttpSession; // Permite acceder y destruir la sesion del usuario

// Usamos esta anotacion para definir la ruta de acceso URL para nuestro Servlet
/**
 * Este es nuestro Controlador encargado de gestionar el cierre de sesion.
 * Aqui invalidamos la sesion activa del usuario y lo redirigimos a nuestro formulario de login.
 */
@WebServlet("/LogoutController") // Mapea este servlet a la ruta /LogoutController
public class LogoutController extends HttpServlet { // Servlet que cierra la sesion del usuario

    /**
     * En nuestro metodo doGet manejamos las peticiones HTTP GET.
     * Aqui cerramos la sesion de nuestro usuario de forma segura.
     *
     * @param request La peticion HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        // Primero, obtenemos la sesion actual sin crear una nueva si no existe
        HttpSession session = request.getSession(false); // false = no crear nueva sesion si no existe; evita crear sesiones innecesarias

        // Si encontramos una sesion activa, procedemos a invalidarla para borrar los datos de nuestro usuario logueado
        if (session != null) { // Solo invalida si realmente hay una sesion activa
            session.invalidate(); // Elimina la sesion y todos sus atributos (usuario, rol, etc.) cerrando la autenticacion
        }

        // Finalmente, redirigimos a nuestra pagina de login
        response.sendRedirect(request.getContextPath() + "/login.jsp"); // Lleva al usuario de vuelta al formulario de ingreso
    }
}