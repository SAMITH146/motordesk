package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.BitacoraDAO;
import com.mycompany.motordesk.model.Bitacora;
import com.mycompany.motordesk.model.Empleado;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Este es el controlador encargado de gestionar la visualización de la bitácora de nuestro sistema.
 * Con esto, nos aseguramos de que solo los usuarios con rol de administrador puedan acceder al historial.
 */
@WebServlet("/BitacoraController")
public class BitacoraController extends HttpServlet {

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
        HttpSession session = request.getSession();
        Empleado user = (Empleado) session.getAttribute("usuarioLogueado");

        // Por seguridad: validamos si no hay sesión o si nuestro usuario no es administrador (IdRol != 1)
        if (user == null || user.getIdRol() != 1) {
            // Si la validación falla, nosotros lo redirigimos a la página de login
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // Instanciamos nuestro DAO y obtenemos la lista de registros de la bitácora
        BitacoraDAO dao = new BitacoraDAO();
        List<Bitacora> lista = dao.listarTodas();
        // Guardamos esta lista en el request para que nuestra vista pueda iterarla
        request.setAttribute("listaBitacora", lista);

        // Por último, redireccionamos a la vista de la bitácora
        request.getRequestDispatcher("/admin/historialBitacora.jsp").forward(request, response);
    }
}
