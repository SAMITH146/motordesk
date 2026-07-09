// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller; // Paquete que agrupa todos los controladores

// A continuacion, importamos las dependencias y clases necesarias
import com.mycompany.motordesk.dao.OrdenDAO; // DAO para acceder a las ordenes de trabajo
import com.mycompany.motordesk.dao.PanelMecanicoDAO; // DAO con consultas especificas del dashboard del mecanico
import com.mycompany.motordesk.dao.ProductoDAO; // DAO para consultar el catalogo de productos/repuestos
import com.mycompany.motordesk.model.Empleado; // Modelo del empleado para identificar al mecanico en sesion
import com.mycompany.motordesk.model.OrdenTrabajo; // Modelo que representa una orden de trabajo
import com.mycompany.motordesk.model.Producto; // Modelo que representa un producto del inventario
import java.io.IOException; // Excepcion para errores de entrada/salida
import java.util.List; // Interfaz de lista para colecciones de objetos
import javax.servlet.ServletException; // Excepcion especifica de servlets
import javax.servlet.annotation.WebServlet; // Anotacion que mapea el servlet a una URL
import javax.servlet.http.*; // Importa clases HTTP: HttpServlet, HttpServletRequest, HttpServletResponse, HttpSession

// Utilizamos esta anotacion para definir la ruta de acceso URL para nuestro Servlet
/**
 * Este es nuestro Controlador encargado de preparar el Dashboard (Panel principal) del Mecanico.
 * Aqui cargamos nuestras estadisticas, la lista de ordenes asignadas y los formularios de edicion.
 */
@WebServlet("/PanelMecanicoController") // Mapea el servlet a la ruta /PanelMecanicoController
public class PanelMecanicoController extends HttpServlet { // Controlador del panel del mecanico

    private final PanelMecanicoDAO dashboardDao = new PanelMecanicoDAO(); // DAO con estadisticas del dashboard del mecanico
    private final OrdenDAO ordenDao = new OrdenDAO(); // DAO para consultar y modificar ordenes de trabajo
    private final ProductoDAO productoDao = new ProductoDAO(); // DAO para consultar el catalogo de repuestos

    /**
     * En nuestro metodo doGet manejamos las peticiones HTTP GET.
     * Aqui cargamos todos los datos necesarios para que nuestra vista panelMecanico.jsp se muestre correctamente.
     *
     * @param request La peticion HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(); // Recupera la sesion activa del cliente
        Empleado user = (Empleado) session.getAttribute("usuarioLogueado"); // Obtiene el objeto mecanico autenticado

        // Verificamos si existe un usuario logueado en la sesion actual. Si detectamos un mecanico activo, procedemos a cargar todas las estadisticas, ordenes asignadas y catalogos necesarios para armar su panel de control.
        if (user != null) { // Solo carga el panel si hay un mecanico autenticado en sesion
            String docMecanico = user.getIdEmpleado(); // Obtiene el documento del mecanico para filtrar sus ordenes

            // Cargamos las estadisticas para nuestro dashboard
            request.setAttribute("ordenesAbiertas", dashboardDao.contarOrdenesAbiertas(docMecanico)); // Total de ordenes que el mecanico tiene actualmente abiertas
            request.setAttribute("ordenesHoy", dashboardDao.contarOrdenesHoy(docMecanico)); // Ordenes asignadas al mecanico en el dia de hoy

            // Listamos las tablas correspondientes
            List<OrdenTrabajo> misOrdenes = ordenDao.listarPorMecanico(docMecanico); // Obtiene solo las ordenes asignadas a este mecanico
            request.setAttribute("listaOrdenes", misOrdenes); // Pasa la lista de ordenes a la vista del panel

            // Preparamos los datos auxiliares para nuestro formulario
            List<Producto> todosProductos = productoDao.listarTodos(); // Carga todo el catalogo de repuestos disponibles
            request.setAttribute("listaProductos", todosProductos); // Pasa el catalogo de repuestos al JSP para el formulario de ordenes

            List<com.mycompany.motordesk.model.Servicio> todosServicios = new com.mycompany.motordesk.dao.ServicioDAO().listarTodos(); // Lista todos los servicios disponibles (mano de obra)
            request.setAttribute("listaServicios", todosServicios); // Pasa el catalogo de servicios al JSP

            request.setAttribute("stockBajo", dashboardDao.obtenerStockBajo()); // Productos con stock critico, para alertar al mecanico

            // Verificamos si solicitamos realizar una edicion
            String action = request.getParameter("action"); // Lee si el mecanico solicito editar una orden
            // Comprobamos si la peticion entrante solicita la edicion de una orden especifica. Si se requiere editar, procedemos a consultar la informacion completa de la orden, incluyendo vehiculo, cliente y servicios, para llenar el formulario correspondiente.
            if ("edit".equals(action)) { // Solo carga datos de edicion si la accion es 'edit'
                // Iniciamos nuestro bloque try para el control de excepciones
                try {
                    int idOrden = Integer.parseInt(request.getParameter("id_orden")); // Convierte el ID de la orden de String a entero
                    OrdenTrabajo ord = ordenDao.obtenerPorId(idOrden); // Busca la orden en la base de datos por su ID
                    // Validamos doblemente que la orden exista y que ademas pertenezca legitimamente al mecanico que tiene la sesion abierta. Si ambas condiciones se cumplen, autorizamos la carga de sus datos detallados en el formulario de edicion.
                    if (ord != null && ord.getDocEmpleFk().equals(docMecanico)) { // Verifica que la orden existe y pertenece al mecanico logueado
                        request.setAttribute("ordenEditar", ord); // Pone la orden en el request para pre-llenar el formulario
                        request.setAttribute("detallesEditar", ordenDao.obtenerDetallesDeOrden(idOrden)); // Carga los repuestos usados en la orden
                        // Cargamos los servicios de mano de obra para pre-llenar nuestro formulario de edicion
                        request.setAttribute("serviciosEditar", ordenDao.obtenerServiciosDeOrden(idOrden)); // Carga los servicios (mano de obra) de la orden

                        // Cargamos el vehiculo y cliente para pre-llenar nuestro formulario
                        com.mycompany.motordesk.model.Vehiculo veh = new com.mycompany.motordesk.dao.VehiculoDAO().obtenerPorId(ord.getIdVehiculoFk()); // Busca el vehiculo asociado a la orden
                        request.setAttribute("vehiculoEditar", veh); // Pone el vehiculo en el request para el formulario
                        if (veh != null) { // Solo busca el cliente si el vehiculo existe
                            com.mycompany.motordesk.model.Cliente cli = new com.mycompany.motordesk.dao.ClienteDAO().obtenerPorId(veh.getIdClienteFk()); // Busca el cliente dueno del vehiculo
                            request.setAttribute("clienteEditar", cli); // Pone el cliente en el request para el formulario
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // Imprime en consola cualquier error al cargar la orden de edicion
                }
            }
        }

        request.getRequestDispatcher("/Mecanico/panelMecanico.jsp").forward(request, response); // Renderiza la vista del panel del mecanico
    }
}