package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.FacturaDAO;
import com.mycompany.motordesk.model.Factura;
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
 * Este es nuestro controlador encargado de mostrar el historial de facturas.
 * Como pueden ver, es exclusivo para nuestros usuarios con rol de Administrador.
 */
@WebServlet("/FacturaController")
public class FacturaController extends HttpServlet {

    /**
     * En el método doGet manejamos las peticiones HTTP GET.
     * Lo usamos para listar todas las facturas que registramos en nuestra base de datos, siempre y cuando el usuario tenga permisos.
     * 
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Primero, obtenemos la sesión actual
        HttpSession session = request.getSession();
        Empleado user = (Empleado) session.getAttribute("usuarioLogueado");

        // Por seguridad, validamos que tengamos un usuario en sesión y que este sea administrador
        if (user == null || user.getIdRol() != 1) {
            // Si vemos que no tiene permisos, lo redirigimos al login
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        FacturaDAO dao = new FacturaDAO();
        // Obtenemos la lista completa de nuestras facturas
        List<Factura> lista = dao.listarTodas();
        // Enviamos esta lista a nuestra vista como atributo de la petición
        request.setAttribute("listaFacturas", lista);

        // Finalmente, renderizamos la vista de nuestro historial de facturas
        request.getRequestDispatcher("/admin/historialFacturas.jsp").forward(request, response);
    }
}
