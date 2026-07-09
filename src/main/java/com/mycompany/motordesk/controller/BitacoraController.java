package com.mycompany.motordesk.controller; // Declara el paquete del controlador

import com.mycompany.motordesk.dao.BitacoraDAO; // DAO que permite acceder a la tabla de auditoría
import com.mycompany.motordesk.model.Bitacora; // Modelo que representa cada registro de la bitácora
import com.mycompany.motordesk.model.Empleado; // Modelo del empleado (usuario del sistema)
import java.io.IOException; // Excepción que puede lanzar el servlet
import java.util.List; // Lista para almacenar los registros de la bitácora
import javax.servlet.ServletException; // Excepción específica de Servlets
import javax.servlet.annotation.WebServlet; // Anotación para mapear la URL del servlet
import javax.servlet.http.HttpServlet; // Clase base para crear un servlet HTTP
import javax.servlet.http.HttpServletRequest; // Representa la petición HTTP entrante
import javax.servlet.http.HttpServletResponse; // Representa la respuesta HTTP que enviamos
import javax.servlet.http.HttpSession; // Maneja la sesión del usuario conectado

/**
 * Este es el controlador encargado de gestionar la visualización de la bitácora de nuestro sistema.
 * Con esto, nos aseguramos de que solo los usuarios con rol de administrador puedan acceder al historial.
 */
@WebServlet("/BitacoraController") // Mapea el servlet a la URL /BitacoraController
public class BitacoraController extends HttpServlet { // Define el servlet que manejará las peticiones de la bitácora

    /**
     * Aquí tenemos el método doGet para manejar las peticiones HTTP GET.
     * Primero validamos la sesión del usuario y, si es administrador, cargamos el historial de la bitácora.
     *
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error específico del Servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Empezamos obteniendo la sesión actual para que verifiquemos quién es el usuario logueado
        HttpSession session = request.getSession(); // Recupera (o crea) la sesión HTTP del cliente
        Empleado user = (Empleado) session.getAttribute("usuarioLogueado"); // Obtiene del atributo de sesión el objeto Empleado autenticado

        // Por seguridad: validamos si no hay sesión o si nuestro usuario no es administrador (IdRol != 1)
        if (user == null || user.getIdRol() != 1) { // Si no está logueado o no es admin, bloqueamos el acceso
            // Si la validación falla, nosotros lo redirigimos a la página de login
            response.sendRedirect(request.getContextPath() + "/login.jsp"); // Redirige al login para que se autentique nuevamente
            return; // Finaliza el método para impedir seguir ejecutando
        }

        // Instanciamos nuestro DAO y obtenemos la lista de registros de la bitácora
        BitacoraDAO dao = new BitacoraDAO(); // Crea una instancia del DAO para operar sobre la tabla bitácora
        List<Bitacora> lista = dao.listarTodas(); // Obtiene todas las entradas de auditoría, ordenadas por fecha (más reciente primero)

        // Guardamos esta lista en el request para que nuestra vista pueda iterarla
        request.setAttribute("listaBitacora", lista); // Coloca la lista en el scope de la petición, disponible para el JSP

        // Por último, redireccionamos a la vista de la bitácora
        request.getRequestDispatcher("/admin/historialBitacora.jsp")
               .forward(request, response); // Reenvía la petición al JSP que muestra la tabla de auditoría
    }
}
