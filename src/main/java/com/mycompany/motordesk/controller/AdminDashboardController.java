// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller;

// A continuación, importamos las dependencias y clases que necesitamos
import com.mycompany.motordesk.dao.AdminDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Utilizamos esta anotación para definir la ruta de acceso URL para este Servlet
/**
 * Como podemos ver, este es el controlador encargado de gestionar nuestro panel de administración.
 * Su principal objetivo es que recolectemos estadísticas y datos recientes para que los mostremos
 * en la vista principal del administrador.
 */
@WebServlet("/AdminDashboard")
public class AdminDashboardController extends HttpServlet {

    /**
     * Aquí tenemos el método doGet, donde manejamos las peticiones HTTP GET.
     * Con esto, nosotros cargamos las estadísticas y registros recientes necesarios para nuestro Dashboard.
     * 
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error específico del Servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Primero, creamos una instancia de nuestro DAO para acceder a las consultas del administrador
        AdminDAO dao = new AdminDAO();
        
        // Ahora vamos a cargar las estadísticas reales desde nuestra base de datos y las pasamos a la vista
        request.setAttribute("totalMecanicos", dao.contarMecanicosActivos());
        request.setAttribute("totalProductos", dao.contarProductos());
        request.setAttribute("totalOrdenes", dao.contarOrdenesTotales());
        request.setAttribute("stockCritico", dao.contarStockCritico());

        // Luego, obtenemos la lista de mecánicos recientes para mostrarla en nuestro Dashboard
        java.util.List<com.mycompany.motordesk.model.Empleado> mecanicos = new com.mycompany.motordesk.dao.EmpleadoDAO().listarMecanicos();
        // Si tenemos más de 3 mecánicos, limitamos la lista a los primeros 3 para no saturar la vista
        if(mecanicos != null && mecanicos.size() > 3) mecanicos = mecanicos.subList(0, 3);
        request.setAttribute("recentMecanicos", mecanicos);

        // De igual forma, obtenemos la lista de nuestras órdenes de trabajo recientes
        java.util.List<com.mycompany.motordesk.model.OrdenTrabajo> ordenes = new com.mycompany.motordesk.dao.OrdenDAO().listarTodas();
        // Si tenemos más de 3 órdenes, simplemente limitamos la lista a las primeras 3
        if(ordenes != null && ordenes.size() > 3) ordenes = ordenes.subList(0, 3);
        request.setAttribute("recentOrdenes", ordenes);

        // Finalmente, redireccionamos a la vista de nuestro panel de administración
        request.getRequestDispatcher("/admin/panelAdmin.jsp").forward(request, response);
    }

    /**
     * En este método doPost manejamos las peticiones HTTP POST.
     * Para este caso, simplemente redirigimos el flujo hacia nuestro método doGet.
     * 
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error específico del Servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
