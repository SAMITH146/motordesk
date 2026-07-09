// Como siempre, iniciamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller; // Paquete que agrupa todos los controladores

// Aqui importamos las dependencias y clases que vamos a necesitar
import com.mycompany.motordesk.dao.ClienteDAO; // DAO para operaciones CRUD sobre la tabla cliente
import com.mycompany.motordesk.model.Cliente; // Modelo que representa a un cliente del taller
import java.io.IOException; // Excepcion para errores de entrada/salida
import javax.servlet.ServletException; // Excepcion especifica de servlets
import javax.servlet.annotation.WebServlet; // Anotacion para mapear el servlet a una URL
import javax.servlet.http.*; // Importa HttpServlet, HttpServletRequest, HttpServletResponse, HttpSession

// Con esta anotacion definimos la ruta de acceso URL para nuestro Servlet
/**
 * Les presentamos el controlador encargado de gestionar las operaciones CRUD de nuestros clientes.
 * A traves de el, permitimos listar a los clientes y procesamos la actualizacion de sus datos.
 */
@WebServlet("/ClienteController") // Mapea este servlet a la ruta /ClienteController
public class ClienteController extends HttpServlet { // Controlador de la gestion de clientes

    /**
     * Este es nuestro metodo doGet, utilizado para manejar peticiones HTTP GET.
     * Nos encargamos de listar todos los clientes y, si se requiere, cargamos los datos
     * de un cliente especifico para que podamos editarlo.
     *
     * @param request La peticion HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ClienteDAO dao = new ClienteDAO(); // Instancia el DAO para acceder a la tabla cliente
        // Capturamos el parametro 'action' para que sepamos que operacion vamos a realizar
        String action = request.getParameter("action"); // Determina si se listan clientes o se carga uno para editar

        // Si vemos que la accion es 'edit', buscamos el cliente por su documento para cargar sus datos
        if ("edit".equals(action)) { // Solo entra aqui si el usuario solicitó editar un cliente
            String doc = request.getParameter("doc"); // Obtiene el documento del cliente a editar
            Cliente c = dao.obtenerPorDocumento(doc); // Busca el cliente en la base de datos por su documento

            // Si nuestro cliente existe, lo enviamos a la vista para rellenar el formulario
            if (c != null) { // Verifica que el cliente exista antes de cargarlo en la vista
                request.setAttribute("clienteEditar", c); // Expone el objeto cliente al JSP para pre-llenar el formulario
            }
        }

        // Obtenemos la lista completa de nuestros clientes y la enviamos al request
        request.setAttribute("listaClientes", dao.listarTodos()); // Lista todos los clientes para mostrarlos en la tabla
        // Redirigimos a nuestra vista de gestion de clientes
        request.getRequestDispatcher("/admin/gestionarClientes.jsp").forward(request, response); // Renderiza la vista de gestion de clientes
    }

    /**
     * Ahora pasamos al metodo doPost, para manejar las peticiones HTTP POST.
     * Lo utilizamos para procesar la actualizacion de los datos de nuestro cliente (cuando action='update').
     *
     * @param request La peticion HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Primero, configuramos la codificacion para que soportemos caracteres especiales (como tildes o la n con virgulilla)
        request.setCharacterEncoding("UTF-8"); // Evita problemas de codificacion con caracteres especiales del español
        // Capturamos la accion que queremos realizar
        String action = request.getParameter("action"); // Determina que operacion POST se esta ejecutando
        ClienteDAO dao = new ClienteDAO(); // Instancia el DAO para operar sobre la tabla cliente

        // Si confirmamos que la accion es 'update', procedemos a actualizar el cliente
        if ("update".equals(action)) { // Solo actualiza si la accion enviada por el formulario es 'update'
            // Creamos nuestro objeto Cliente con los datos que recibimos del formulario
            Cliente c = new Cliente(); // Nuevo objeto que se llenara con los datos del formulario
            c.setDocumento(request.getParameter("doc_cliente")); // Documento de identificacion del cliente
            c.setNombre(request.getParameter("nom_cliente")); // Nombre completo del cliente
            c.setDireccion(request.getParameter("direccion_cliente")); // Direccion fisica del cliente

            // Intentamos actualizar este cliente en nuestra base de datos
            if (dao.actualizar(c)) { // Ejecuta la actualizacion y verifica si fue exitosa
                // Si la actualizacion es exitosa, guardamos un mensaje de exito en nuestra sesion
                request.getSession().setAttribute("mensaje", "Cliente actualizado correctamente."); // Mensaje de confirmacion para el usuario
                request.getSession().setAttribute("tipoMensaje", "success"); // Estilo visual del mensaje
            } else {
                // Si llegara a fallar, guardamos un mensaje de error en la sesion
                request.getSession().setAttribute("mensaje", "Error al actualizar el cliente."); // Mensaje de error para el usuario
                request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual del mensaje de error
            }
        }

        // Redireccionamos al doGet de nuestro mismo Servlet usando sendRedirect
        response.sendRedirect(request.getContextPath() + "/ClienteController"); // Recarga la lista de clientes actualizada
    }
}
