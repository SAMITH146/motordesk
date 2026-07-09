// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller; // Paquete que agrupa todos los controladores

// A continuacion, importamos las dependencias y clases necesarias
import com.mycompany.motordesk.dao.ProductoDAO; // DAO que gestiona el inventario de repuestos
import com.mycompany.motordesk.model.Producto; // Modelo que representa un repuesto o producto del inventario
import java.io.IOException; // Excepcion para errores de entrada/salida
import java.util.List; // Interfaz de lista para almacenar los productos
import javax.servlet.ServletException; // Excepcion especifica de servlets
import javax.servlet.annotation.WebServlet; // Anotacion que mapea el servlet a una URL
import javax.servlet.http.HttpServlet; // Clase base del servlet HTTP
import javax.servlet.http.HttpServletRequest; // Representa la peticion HTTP entrante
import javax.servlet.http.HttpServletResponse; // Representa la respuesta HTTP
import javax.servlet.http.HttpSession; // Permite almacenar mensajes en la sesion del usuario

// Utilizamos esta anotacion para definir la ruta de acceso URL para nuestro Servlet
/**
 * Este es nuestro Controlador encargado de la gestion de nuestro inventario de repuestos o productos.
 * Aqui permitimos listar, filtrar, registrar, actualizar y eliminar productos.
 */
@WebServlet("/ProductoController") // Mapea el servlet a la ruta /ProductoController
public class ProductoController extends HttpServlet { // Controlador del inventario de repuestos

    private final ProductoDAO dao = new ProductoDAO(); // DAO del inventario, reutilizado en todos los metodos del controlador

    /**
     * En nuestro metodo doGet manejamos las peticiones HTTP GET.
     * Aqui listamos los productos y aplicamos filtros de busqueda si proporcionamos alguno.
     *
     * @param request La peticion HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action"); // Lee la accion solicitada (p. ej., 'edit')
        // Evaluamos si la accion enviada corresponde a la edicion de un producto. Si detectamos esta intencion, intentamos capturar el ID del producto y cargar sus datos desde la base de datos para mostrarlos en la vista.
        if ("edit".equals(action)) { // Solo carga datos de edicion si la accion es 'edit'
            // Iniciamos nuestro bloque try para el control de excepciones
            try {
                int id = Integer.parseInt(request.getParameter("id")); // Convierte el ID del producto de String a entero
                request.setAttribute("productoEditar", dao.obtenerPorId(id)); // Busca el producto en BD y lo expone a la vista
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Imprime el error si el ID no es un numero valido
            }
        }

        String buscar = request.getParameter("buscar"); // Texto libre ingresado por el usuario para buscar productos
        String fVehiculo = request.getParameter("f_vehiculo"); // Filtro por tipo de vehiculo
        String fSeccion = request.getParameter("f_seccion"); // Filtro por seccion del taller

        List<Producto> lista; // Lista que contendra los productos a mostrar en la tabla
        // Verificamos si el usuario ha introducido algun criterio de busqueda, como texto libre, tipo de vehiculo o seccion. Si detectamos filtros activos, procedemos a consultar los productos que coincidan; de lo contrario, cargamos el inventario completo.
        if ((buscar != null && !buscar.isEmpty()) ||
            (fVehiculo != null && !fVehiculo.isEmpty()) ||
            (fSeccion != null && !fSeccion.isEmpty())) {
            lista = dao.listarFiltrados(fVehiculo, fSeccion, buscar); // Aplica los filtros seleccionados
        } else {
            lista = dao.listarTodos(); // Sin filtros, carga el inventario completo
        }

        request.setAttribute("listaProductos", lista); // Pasa la lista de productos al JSP

        // Conservamos los valores de filtro para mostrarlos en nuestros inputs
        request.setAttribute("filtroBuscar", buscar); // Mantiene el texto de busqueda visible en el input
        request.setAttribute("filtroVehiculo", fVehiculo); // Mantiene el filtro de tipo vehiculo seleccionado
        request.setAttribute("filtroSeccion", fSeccion); // Mantiene el filtro de seccion seleccionado

        request.getRequestDispatcher("/admin/gestionarRepuestos.jsp").forward(request, response); // Renderiza la vista del inventario
    }

    /**
     * En nuestro metodo doPost manejamos las peticiones HTTP POST.
     * Aqui procesamos la insercion, actualizacion o eliminacion de nuestros productos.
     *
     * @param request La peticion HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // Evita problemas de codificacion con caracteres especiales del espanol
        HttpSession session = request.getSession(); // Recupera la sesion para almacenar mensajes de feedback
        String action = request.getParameter("action"); // Lee la accion solicitada (delete, update, insert)

        // Inicio del bloque try para control de excepciones
        try {
            Producto p = new Producto(); // Objeto producto que se llenara con los datos del formulario
            // Comprobamos si el formulario nos envia el ID de un producto existente. Si encontramos un ID valido, se lo asignamos a nuestro objeto para que el sistema sepa que se trata de una actualizacion y no de una creacion nueva.
            if (request.getParameter("id") != null && !request.getParameter("id").isEmpty()) {
                p.setIdProducto(Integer.parseInt(request.getParameter("id"))); // Asigna el ID del producto existente
            }

            p.setNombreProducto(request.getParameter("nombre")); // Nombre del repuesto o producto
            p.setTipoVehiculo(request.getParameter("tipoVehiculo")); // Tipo de vehiculo compatible con el producto
            p.setSeccion(request.getParameter("seccion")); // Seccion del taller donde se usa el producto

            String stockParam = request.getParameter("stock"); // Cantidad en inventario
            String precioParam = request.getParameter("precio"); // Precio unitario del producto

            p.setStock( (stockParam != null && !stockParam.isEmpty()) ? Integer.parseInt(stockParam) : 0 ); // Asigna stock o 0 si viene vacio
            p.setPrecioUnitario( (precioParam != null && !precioParam.isEmpty()) ? Double.parseDouble(precioParam) : 0.0 ); // Asigna precio o 0.0 si viene vacio

            boolean exito = false; // Indicador del resultado de la operacion en base de datos

            // Evaluamos si el usuario solicito la eliminacion del producto. Si es el caso, procedemos a borrar el registro de nuestra base de datos y preparamos el mensaje de resultado correspondiente para la interfaz.
            if ("delete".equals(action)) { // Elimina el producto si la accion es 'delete'
                exito = dao.eliminar(p.getIdProducto()); // Intenta eliminar el producto de la base de datos
                // Verificamos si la operacion de eliminacion en la base de datos se ejecuto de forma correcta. Si logramos borrar el producto, configuramos un mensaje de exito para notificar al usuario.
                if (exito) {
                    session.setAttribute("mensaje", "Producto eliminado correctamente."); // Feedback de exito
                    session.setAttribute("tipoMensaje", "success"); // Estilo visual verde
                } else {
                    session.setAttribute("mensaje", "Error al eliminar el producto."); // Feedback de error (puede tener ordenes asociadas)
                    session.setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                }
            } else if (p.getIdProducto() > 0) { // Si el producto tiene ID asignado, es una actualizacion
                exito = dao.actualizar(p); // Actualiza los datos del producto en la base de datos
                // Comprobamos si la actualizacion de los datos del producto en la base de datos fue exitosa. Si el proceso termino correctamente, preparamos un mensaje de confirmacion positiva para mostrar en el sistema.
                if (exito) {
                    session.setAttribute("mensaje", "Producto actualizado con exito."); // Feedback de exito
                    session.setAttribute("tipoMensaje", "success"); // Estilo visual verde
                } else {
                    session.setAttribute("mensaje", "Error al actualizar el producto."); // Feedback de error
                    session.setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                }
            } else { // Si no tiene ID, es un producto nuevo que debe insertarse
                exito = dao.insertar(p); // Inserta el nuevo producto en el inventario
                // Validamos si la insercion del nuevo producto en nuestra base de datos se realizo sin inconvenientes. Si el registro se creo exitosamente, configuramos una alerta de confirmacion para que el usuario sepa que el inventario ha sido actualizado.
                if (exito) {
                    session.setAttribute("mensaje", "Producto registrado correctamente."); // Feedback de exito
                    session.setAttribute("tipoMensaje", "success"); // Estilo visual verde
                } else {
                    session.setAttribute("mensaje", "Error al registrar el producto. Revise sus datos."); // Feedback de error
                    session.setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error en consola para diagnóstico
            session.setAttribute("mensaje", "Error tecnico: " + e.getMessage()); // Muestra el error tecnico al usuario
            session.setAttribute("tipoMensaje", "error"); // Estilo visual rojo
        }

        response.sendRedirect(request.getContextPath() + "/ProductoController"); // Recarga la vista del inventario con datos actualizados
    }
}
