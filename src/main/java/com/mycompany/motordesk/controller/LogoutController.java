// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller;
// A continuación, importamos las dependencias y clases necesarias para nuestro controlador
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Usamos esta anotación para definir la ruta de acceso URL para nuestro Servlet
/**
 * Este es nuestro Controlador encargado de gestionar el cierre de sesión.
 * Aquí invalidamos la sesión activa del usuario y lo redirigimos a nuestro formulario de login.
 */
@WebServlet("/LogoutController")
public class LogoutController extends HttpServlet {

    /**
     * En nuestro método doGet manejamos las peticiones HTTP GET.
     * Aquí cerramos la sesión de nuestro usuario de forma segura.
     * 
     * @param request La petición HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        // Primero, obtenemos la sesión actual sin crear una nueva si no existe
        HttpSession session = request.getSession(false);
        
        // Si encontramos una sesión activa, procedemos a invalidarla para borrar los datos de nuestro usuario logueado
        if (session != null) {
            session.invalidate();
        }

        // Finalmente, redirigimos a nuestra página de login
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}