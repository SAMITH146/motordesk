// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller; // Paquete que agrupa todos los controladores de MotorDesk

// A continuacion, importamos las dependencias y clases que necesitamos
import com.mycompany.motordesk.dao.AdminDAO; // DAO especializado en consultas del panel de administracion
import java.io.IOException; // Excepcion para errores de entrada/salida en el servlet
import javax.servlet.ServletException; // Excepcion especifica de servlets
import javax.servlet.annotation.WebServlet; // Anotacion que mapea este servlet a una URL
import javax.servlet.http.HttpServlet; // Clase base del servlet HTTP
import javax.servlet.http.HttpServletRequest; // Representa la peticion HTTP entrante
import javax.servlet.http.HttpServletResponse; // Representa la respuesta HTTP

// Utilizamos esta anotacion para definir la ruta de acceso URL para este Servlet
/**
 * Como podemos ver, este es el controlador encargado de gestionar nuestro panel de administracion.
 * Su principal objetivo es que recolectemos estadisticas y datos recientes para que los mostremos
 * en la vista principal del administrador.
 */
@WebServlet("/AdminDashboard") // Mapea el servlet a la ruta /AdminDashboard
public class AdminDashboardController extends HttpServlet { // Controlador del panel de administracion

    /**
     * Aqui tenemos el metodo doGet, donde manejamos las peticiones HTTP GET.
     * Con esto, nosotros cargamos las estadisticas y registros recientes necesarios para nuestro Dashboard.
     *
     * @param request La peticion HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error especifico del Servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Primero, creamos una instancia de nuestro DAO para acceder a las consultas del administrador
        AdminDAO dao = new AdminDAO(); // Instancia el DAO que tiene las consultas de estadisticas del admin

        // Ahora vamos a cargar las estadisticas reales desde nuestra base de datos y las pasamos a la vista
        request.setAttribute("totalMecanicos", dao.contarMecanicosActivos()); // Total de mecanicos en estado Activo
        request.setAttribute("totalProductos", dao.contarProductos()); // Total de productos registrados en inventario
        request.setAttribute("totalOrdenes", dao.contarOrdenesTotales()); // Total de ordenes de trabajo en el sistema
        request.setAttribute("stockCritico", dao.contarStockCritico()); // Productos cuyo stock esta por debajo del minimo

        // Luego, obtenemos la lista de mecanicos recientes para mostrarla en nuestro Dashboard
        java.util.List<com.mycompany.motordesk.model.Empleado> mecanicos = new com.mycompany.motordesk.dao.EmpleadoDAO().listarMecanicos(); // Lista todos los mecanicos del sistema
        // Si tenemos mas de 3 mecanicos, limitamos la lista a los primeros 3 para no saturar la vista
        if(mecanicos != null && mecanicos.size() > 3) mecanicos = mecanicos.subList(0, 3); // Solo mostramos los 3 mas recientes en el dashboard
        request.setAttribute("recentMecanicos", mecanicos); // Pasa la lista limitada a la vista del panel

        // De igual forma, obtenemos la lista de nuestras ordenes de trabajo recientes
        java.util.List<com.mycompany.motordesk.model.OrdenTrabajo> ordenes = new com.mycompany.motordesk.dao.OrdenDAO().listarTodas(); // Lista todas las ordenes de trabajo
        // Si tenemos mas de 3 ordenes, simplemente limitamos la lista a las primeras 3
        if(ordenes != null && ordenes.size() > 3) ordenes = ordenes.subList(0, 3); // Solo mostramos las 3 mas recientes en el dashboard
        request.setAttribute("recentOrdenes", ordenes); // Pasa la lista limitada a la vista del panel

        // Finalmente, redireccionamos a la vista de nuestro panel de administracion
        request.getRequestDispatcher("/admin/panelAdmin.jsp").forward(request, response); // Renderiza la vista del dashboard del admin
    }

    /**
     * En este metodo doPost manejamos las peticiones HTTP POST.
     * Para este caso, simplemente redirigimos el flujo hacia nuestro metodo doGet.
     *
     * @param request La peticion HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error especifico del Servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response); // Redirige los POST al doGet ya que este controlador solo visualiza datos
    }
}
