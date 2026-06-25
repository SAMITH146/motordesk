// Como siempre, iniciamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller;

// Aquí importamos las dependencias y clases que vamos a necesitar
import com.mycompany.motordesk.dao.ClienteDAO;
import com.mycompany.motordesk.model.Cliente;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Con esta anotación definimos la ruta de acceso URL para nuestro Servlet
/**
 * Les presentamos el controlador encargado de gestionar las operaciones CRUD de nuestros clientes.
 * A través de él, permitimos listar a los clientes y procesamos la actualización de sus datos.
 */
@WebServlet("/ClienteController")
public class ClienteController extends HttpServlet {

    /**
     * Este es nuestro método doGet, utilizado para manejar peticiones HTTP GET.
     * Nos encargamos de listar todos los clientes y, si se requiere, cargamos los datos
     * de un cliente específico para que podamos editarlo.
     * 
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        ClienteDAO dao = new ClienteDAO();
        // Capturamos el parámetro 'action' para que sepamos qué operación vamos a realizar
        String action = request.getParameter("action");
        
        // Si vemos que la acción es 'edit', buscamos el cliente por su documento para cargar sus datos
        if ("edit".equals(action)) {
            String doc = request.getParameter("doc");
            Cliente c = dao.obtenerPorDocumento(doc);
            
            // Si nuestro cliente existe, lo enviamos a la vista para rellenar el formulario
            if (c != null) {
                request.setAttribute("clienteEditar", c);
            }
        }
        
        // Obtenemos la lista completa de nuestros clientes y la enviamos al request
        request.setAttribute("listaClientes", dao.listarTodos());
        // Redirigimos a nuestra vista de gestión de clientes
        request.getRequestDispatcher("/admin/gestionarClientes.jsp").forward(request, response);
    }

    /**
     * Ahora pasamos al método doPost, para manejar las peticiones HTTP POST.
     * Lo utilizamos para procesar la actualización de los datos de nuestro cliente (cuando action='update').
     * 
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Primero, configuramos la codificación para que soportemos caracteres especiales (como tildes o la 'ñ')
        request.setCharacterEncoding("UTF-8");
        // Capturamos la acción que queremos realizar
        String action = request.getParameter("action");
        ClienteDAO dao = new ClienteDAO();
        
        // Si confirmamos que la acción es 'update', procedemos a actualizar el cliente
        if ("update".equals(action)) {
            // Creamos nuestro objeto Cliente con los datos que recibimos del formulario
            Cliente c = new Cliente();
            c.setDocumento(request.getParameter("doc_cliente"));
            c.setNombre(request.getParameter("nom_cliente"));
            c.setDireccion(request.getParameter("direccion_cliente"));
            
            // Intentamos actualizar este cliente en nuestra base de datos
            if (dao.actualizar(c)) {
                // Si la actualización es exitosa, guardamos un mensaje de éxito en nuestra sesión
                request.getSession().setAttribute("mensaje", "Cliente actualizado correctamente.");
                request.getSession().setAttribute("tipoMensaje", "success");
            } else {
                // Si llegara a fallar, guardamos un mensaje de error en la sesión
                request.getSession().setAttribute("mensaje", "Error al actualizar el cliente.");
                request.getSession().setAttribute("tipoMensaje", "error");
            }
        }
        
        // Redireccionamos al doGet de nuestro mismo Servlet usando sendRedirect
        response.sendRedirect(request.getContextPath() + "/ClienteController");
    }
}
