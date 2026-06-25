// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller;

// A continuación, importamos las dependencias y clases necesarias
import com.mycompany.motordesk.dao.ProductoDAO;
import com.mycompany.motordesk.model.Producto;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Utilizamos esta anotación para definir la ruta de acceso URL para nuestro Servlet
/**
 * Este es nuestro Controlador encargado de la gestión de nuestro inventario de repuestos o productos.
 * Aquí permitimos listar, filtrar, registrar, actualizar y eliminar productos.
 */
@WebServlet("/ProductoController")
public class ProductoController extends HttpServlet {

    private final ProductoDAO dao = new ProductoDAO();

    /**
     * En nuestro método doGet manejamos las peticiones HTTP GET.
     * Aquí listamos los productos y aplicamos filtros de búsqueda si proporcionamos alguno.
     * 
     * @param request La petición HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        // Evaluamos si la acción enviada corresponde a la edición de un producto. Si detectamos esta intención, intentamos capturar el ID del producto y cargar sus datos desde la base de datos para mostrarlos en la vista.
        if ("edit".equals(action)) {
            // Iniciamos nuestro bloque try para el control de excepciones
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                request.setAttribute("productoEditar", dao.obtenerPorId(id));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        String buscar = request.getParameter("buscar");
        String fVehiculo = request.getParameter("f_vehiculo");
        String fSeccion = request.getParameter("f_seccion");

        List<Producto> lista;
        // Verificamos si el usuario ha introducido algún criterio de búsqueda, como texto libre, tipo de vehículo o sección. Si detectamos filtros activos, procedemos a consultar los productos que coincidan; de lo contrario, cargamos el inventario completo.
        if ((buscar != null && !buscar.isEmpty()) || 
            (fVehiculo != null && !fVehiculo.isEmpty()) || 
            (fSeccion != null && !fSeccion.isEmpty())) {
            lista = dao.listarFiltrados(fVehiculo, fSeccion, buscar);
        } else {
            lista = dao.listarTodos();
        }

        request.setAttribute("listaProductos", lista);
        
        // Conservamos los valores de filtro para mostrarlos en nuestros inputs
        request.setAttribute("filtroBuscar", buscar);
        request.setAttribute("filtroVehiculo", fVehiculo);
        request.setAttribute("filtroSeccion", fSeccion);
        
        request.getRequestDispatcher("/admin/gestionarRepuestos.jsp").forward(request, response);
    }

    /**
     * En nuestro método doPost manejamos las peticiones HTTP POST.
     * Aquí procesamos la inserción, actualización o eliminación de nuestros productos.
     * 
     * @param request La petición HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        // Inicio del bloque try para control de excepciones
        try {
            Producto p = new Producto();
            // Comprobamos si el formulario nos envía el ID de un producto existente. Si encontramos un ID válido, se lo asignamos a nuestro objeto para que el sistema sepa que se trata de una actualización y no de una creación nueva.
            if (request.getParameter("id") != null && !request.getParameter("id").isEmpty()) {
                p.setIdProducto(Integer.parseInt(request.getParameter("id")));
            }
            
            p.setNombreProducto(request.getParameter("nombre"));
            p.setTipoVehiculo(request.getParameter("tipoVehiculo"));
            p.setSeccion(request.getParameter("seccion"));
            
            String stockParam = request.getParameter("stock");
            String precioParam = request.getParameter("precio");
            
            p.setStock( (stockParam != null && !stockParam.isEmpty()) ? Integer.parseInt(stockParam) : 0 );
            p.setPrecioUnitario( (precioParam != null && !precioParam.isEmpty()) ? Double.parseDouble(precioParam) : 0.0 );

            boolean exito = false;

            // Evaluamos si el usuario solicitó la eliminación del producto. Si es el caso, procedemos a borrar el registro de nuestra base de datos y preparamos el mensaje de resultado correspondiente para la interfaz.
            if ("delete".equals(action)) {
                exito = dao.eliminar(p.getIdProducto());
                // Verificamos si la operación de eliminación en la base de datos se ejecutó de forma correcta. Si logramos borrar el producto, configuramos un mensaje de éxito para notificar al usuario.
                if (exito) {
                    session.setAttribute("mensaje", "Producto eliminado correctamente.");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "Error al eliminar el producto.");
                    session.setAttribute("tipoMensaje", "error");
                }
            } else if (p.getIdProducto() > 0) {
                exito = dao.actualizar(p);
                // Comprobamos si la actualización de los datos del producto en la base de datos fue exitosa. Si el proceso terminó correctamente, preparamos un mensaje de confirmación positiva para mostrar en el sistema.
                if (exito) {
                    session.setAttribute("mensaje", "Producto actualizado con éxito.");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "Error al actualizar el producto.");
                    session.setAttribute("tipoMensaje", "error");
                }
            } else {
                exito = dao.insertar(p);
                // Validamos si la inserción del nuevo producto en nuestra base de datos se realizó sin inconvenientes. Si el registro se creó exitosamente, configuramos una alerta de confirmación para que el usuario sepa que el inventario ha sido actualizado.
                if (exito) {
                    session.setAttribute("mensaje", "Producto registrado correctamente.");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "Error al registrar el producto. Revise sus datos.");
                    session.setAttribute("tipoMensaje", "error");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("mensaje", "Error técnico: " + e.getMessage());
            session.setAttribute("tipoMensaje", "error");
        }

        response.sendRedirect(request.getContextPath() + "/ProductoController");
    }
}
