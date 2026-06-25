// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller;

// A continuación, importamos las dependencias y clases necesarias
import com.mycompany.motordesk.dao.OrdenDAO;
import com.mycompany.motordesk.dao.PanelMecanicoDAO;
import com.mycompany.motordesk.dao.ProductoDAO;
import com.mycompany.motordesk.model.Empleado;
import com.mycompany.motordesk.model.OrdenTrabajo;
import com.mycompany.motordesk.model.Producto;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Utilizamos esta anotación para definir la ruta de acceso URL para nuestro Servlet
/**
 * Este es nuestro Controlador encargado de preparar el Dashboard (Panel principal) del Mecánico.
 * Aquí cargamos nuestras estadísticas, la lista de órdenes asignadas y los formularios de edición.
 */
@WebServlet("/PanelMecanicoController")
public class PanelMecanicoController extends HttpServlet {

    private final PanelMecanicoDAO dashboardDao = new PanelMecanicoDAO();
    private final OrdenDAO ordenDao = new OrdenDAO();
    private final ProductoDAO productoDao = new ProductoDAO();

    /**
     * En nuestro método doGet manejamos las peticiones HTTP GET.
     * Aquí cargamos todos los datos necesarios para que nuestra vista panelMecanico.jsp se muestre correctamente.
     * 
     * @param request La petición HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Empleado user = (Empleado) session.getAttribute("usuarioLogueado");

        // Verificamos si existe un usuario logueado en la sesión actual. Si detectamos un mecánico activo, procedemos a cargar todas las estadísticas, órdenes asignadas y catálogos necesarios para armar su panel de control.
        if (user != null) {
            String docMecanico = user.getIdEmpleado();
            
            // Cargamos las estadísticas para nuestro dashboard
            request.setAttribute("ordenesAbiertas", dashboardDao.contarOrdenesAbiertas(docMecanico));
            request.setAttribute("ordenesHoy", dashboardDao.contarOrdenesHoy(docMecanico));
            
            // Listamos las tablas correspondientes
            List<OrdenTrabajo> misOrdenes = ordenDao.listarPorMecanico(docMecanico);
            request.setAttribute("listaOrdenes", misOrdenes);
            
            // Preparamos los datos auxiliares para nuestro formulario
            List<Producto> todosProductos = productoDao.listarTodos();
            request.setAttribute("listaProductos", todosProductos);
            
            List<com.mycompany.motordesk.model.Servicio> todosServicios = new com.mycompany.motordesk.dao.ServicioDAO().listarTodos();
            request.setAttribute("listaServicios", todosServicios);
            
            request.setAttribute("stockBajo", dashboardDao.obtenerStockBajo());
            
            // Verificamos si solicitamos realizar una edición
            String action = request.getParameter("action");
            // Comprobamos si la petición entrante solicita la edición de una orden específica. Si se requiere editar, procedemos a consultar la información completa de la orden, incluyendo vehículo, cliente y servicios, para llenar el formulario correspondiente.
            if ("edit".equals(action)) {
                // Iniciamos nuestro bloque try para el control de excepciones
                try {
                    int idOrden = Integer.parseInt(request.getParameter("id_orden"));
                    OrdenTrabajo ord = ordenDao.obtenerPorId(idOrden);
                    // Validamos doblemente que la orden exista y que además pertenezca legítimamente al mecánico que tiene la sesión abierta. Si ambas condiciones se cumplen, autorizamos la carga de sus datos detallados en el formulario de edición.
                    if (ord != null && ord.getDocEmpleFk().equals(docMecanico)) {
                        request.setAttribute("ordenEditar", ord);
                        request.setAttribute("detallesEditar", ordenDao.obtenerDetallesDeOrden(idOrden));
                        // Cargamos los servicios de mano de obra para pre-llenar nuestro formulario de edición
                        request.setAttribute("serviciosEditar", ordenDao.obtenerServiciosDeOrden(idOrden));

                        // Cargamos el vehículo y cliente para pre-llenar nuestro formulario
                        com.mycompany.motordesk.model.Vehiculo veh = new com.mycompany.motordesk.dao.VehiculoDAO().obtenerPorId(ord.getIdVehiculoFk());
                        request.setAttribute("vehiculoEditar", veh);
                        if (veh != null) {
                            com.mycompany.motordesk.model.Cliente cli = new com.mycompany.motordesk.dao.ClienteDAO().obtenerPorId(veh.getIdClienteFk());
                            request.setAttribute("clienteEditar", cli);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        request.getRequestDispatcher("/Mecanico/panelMecanico.jsp").forward(request, response);
    }
}