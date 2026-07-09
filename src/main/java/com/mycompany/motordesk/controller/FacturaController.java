package com.mycompany.motordesk.controller; // Paquete que agrupa todos los controladores de MotorDesk

import com.mycompany.motordesk.dao.FacturaDAO; // DAO para consultar la tabla de facturas
import com.mycompany.motordesk.model.Factura; // Modelo que representa una factura del sistema
import com.mycompany.motordesk.model.Empleado; // Modelo del empleado para verificar el rol en sesion
import java.io.IOException; // Excepcion para errores de entrada/salida del servlet
import java.util.List; // Interfaz de lista para almacenar el resultado de las facturas
import javax.servlet.ServletException; // Excepcion especifica de servlets
import javax.servlet.annotation.WebServlet; // Anotacion que mapea este servlet a una URL
import javax.servlet.http.HttpServlet; // Clase base del servlet HTTP
import javax.servlet.http.HttpServletRequest; // Representa la peticion HTTP entrante
import javax.servlet.http.HttpServletResponse; // Representa la respuesta HTTP al cliente
import javax.servlet.http.HttpSession; // Permite acceder a la sesion del usuario autenticado

/**
 * Este es nuestro controlador encargado de mostrar el historial de facturas.
 * Como pueden ver, es exclusivo para nuestros usuarios con rol de Administrador.
 */
@WebServlet("/FacturaController") // Mapea este servlet a la ruta /FacturaController
public class FacturaController extends HttpServlet { // Controlador del historial de facturas (solo admin)

    /**
     * En el metodo doGet manejamos las peticiones HTTP GET.
     * Lo usamos para listar todas las facturas que registramos en nuestra base de datos, siempre y cuando el usuario tenga permisos.
     *
     * @param request La peticion HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Primero, obtenemos la sesion actual
        HttpSession session = request.getSession(); // Recupera la sesion HTTP del cliente
        Empleado user = (Empleado) session.getAttribute("usuarioLogueado"); // Obtiene el empleado autenticado de la sesion

        // Por seguridad, validamos que tengamos un usuario en sesion y que este sea administrador
        if (user == null || user.getIdRol() != 1) { // Bloquea acceso si no hay sesion activa o el rol no es admin (1)
            // Si vemos que no tiene permisos, lo redirigimos al login
            response.sendRedirect(request.getContextPath() + "/login.jsp"); // Redirige al login para que se autentique
            return; // Detiene la ejecucion para no mostrar datos sensibles
        }

        FacturaDAO dao = new FacturaDAO(); // Instancia el DAO para acceder a la tabla de facturas
        // Obtenemos la lista completa de nuestras facturas
        List<Factura> lista = dao.listarTodas(); // Trae todas las facturas registradas en el sistema
        // Enviamos esta lista a nuestra vista como atributo de la peticion
        request.setAttribute("listaFacturas", lista); // Pone la lista de facturas disponible para el JSP

        // Finalmente, renderizamos la vista de nuestro historial de facturas
        request.getRequestDispatcher("/admin/historialFacturas.jsp").forward(request, response); // Carga la vista de historial de facturas
    }
}
