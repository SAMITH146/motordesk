package com.mycompany.motordesk.controller; // Paquete que agrupa todos los controladores de MotorDesk

import com.mycompany.motordesk.dao.ProveedorDAO; // DAO para operaciones CRUD sobre la tabla proveedor
import com.mycompany.motordesk.model.Proveedor; // Modelo que representa a un proveedor de repuestos
import java.io.IOException; // Excepcion para errores de entrada/salida
import javax.servlet.ServletException; // Excepcion especifica de servlets
import javax.servlet.annotation.WebServlet; // Anotacion que mapea el servlet a una URL
import javax.servlet.http.*; // Importa HttpServlet, HttpServletRequest, HttpServletResponse, HttpSession

/**
 * Este es nuestro Controlador encargado de gestionar nuestros proveedores de repuestos.
 * Aqui permitimos las operaciones CRUD basicas sobre la tabla de proveedores.
 */
@WebServlet("/ProveedorController") // Mapea este servlet a la ruta /ProveedorController
public class ProveedorController extends HttpServlet { // Controlador de gestion de proveedores

    /**
     * En nuestro metodo doGet manejamos las peticiones HTTP GET.
     * Aqui listamos todos nuestros proveedores y, si corresponde, cargamos los datos para editar.
     *
     * @param request La peticion HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ProveedorDAO dao = new ProveedorDAO(); // Instancia el DAO para acceder a la tabla proveedor
        String action = request.getParameter("action"); // Lee la accion solicitada (p. ej., 'edit')

        if ("edit".equals(action)) { // Solo carga datos del proveedor si la accion es 'edit'
            String idStr = request.getParameter("id"); // Obtiene el ID del proveedor a editar
            if (idStr != null && !idStr.trim().isEmpty()) { // Verifica que el ID no sea vacio
                int id = Integer.parseInt(idStr); // Convierte el ID a entero
                Proveedor p = dao.obtenerPorId(id); // Busca el proveedor en la base de datos
                if (p != null) { // Solo expone el proveedor a la vista si realmente fue encontrado
                    request.setAttribute("proveedorEditar", p); // Pre-llena el formulario de edicion con los datos del proveedor
                }
            }
        }

        // Siempre nos aseguramos de cargar la lista
        request.setAttribute("listaProveedores", dao.listarTodos()); // Carga todos los proveedores para mostrarlos en la tabla
        request.getRequestDispatcher("/admin/gestionarProveedores.jsp").forward(request, response); // Renderiza la vista de gestion de proveedores
    }

    /**
     * En nuestro metodo doPost manejamos las peticiones HTTP POST.
     * Aqui procesamos el alta, edicion y eliminacion de un proveedor en nuestro sistema.
     *
     * @param request La peticion HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // Evita problemas con caracteres especiales del espanol
        String action = request.getParameter("action"); // Lee la accion solicitada (insert, update, delete)
        ProveedorDAO dao = new ProveedorDAO(); // Instancia el DAO para persistir cambios en la tabla proveedor

        try {
            if ("insert".equals(action)) { // Solo inserta si la accion es 'insert'
                Proveedor p = new Proveedor(); // Nuevo objeto proveedor a registrar
                p.setNombreProveedor(request.getParameter("nombre_proveedor")); // Nombre del proveedor
                p.setContacto(request.getParameter("contacto")); // Persona de contacto del proveedor
                p.setTelefono(request.getParameter("telefono")); // Telefono de contacto
                p.setCorreo(request.getParameter("correo")); // Correo electronico del proveedor

                if (dao.insertar(p)) { // Intenta registrar el proveedor en la base de datos
                    request.getSession().setAttribute("mensaje", "Proveedor registrado exitosamente."); // Feedback de exito
                    request.getSession().setAttribute("tipoMensaje", "success"); // Estilo visual verde
                } else {
                    request.getSession().setAttribute("mensaje", "Error al registrar el proveedor."); // Feedback de error
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                }
            } else if ("update".equals(action)) { // Solo actualiza si la accion es 'update'
                Proveedor p = new Proveedor(); // Objeto proveedor con datos actualizados del formulario
                p.setIdProveedor(Integer.parseInt(request.getParameter("id_proveedor"))); // ID del proveedor a actualizar
                p.setNombreProveedor(request.getParameter("nombre_proveedor")); // Nombre actualizado
                p.setContacto(request.getParameter("contacto")); // Contacto actualizado
                p.setTelefono(request.getParameter("telefono")); // Telefono actualizado
                p.setCorreo(request.getParameter("correo")); // Correo actualizado

                if (dao.actualizar(p)) { // Intenta actualizar el proveedor en la base de datos
                    request.getSession().setAttribute("mensaje", "Proveedor actualizado exitosamente."); // Feedback de exito
                    request.getSession().setAttribute("tipoMensaje", "success"); // Estilo visual verde
                } else {
                    request.getSession().setAttribute("mensaje", "Error al actualizar el proveedor."); // Feedback de error
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                }
            } else if ("delete".equals(action)) { // Solo elimina si la accion es 'delete'
                int id = Integer.parseInt(request.getParameter("id_proveedor")); // ID del proveedor a eliminar
                if (dao.eliminar(id)) { // Intenta eliminar el proveedor de la base de datos
                    request.getSession().setAttribute("mensaje", "Proveedor eliminado exitosamente."); // Feedback de exito
                    request.getSession().setAttribute("tipoMensaje", "success"); // Estilo visual verde
                } else {
                    request.getSession().setAttribute("mensaje", "Error al eliminar. Es posible que este proveedor ya tenga compras registradas y no se pueda borrar."); // Feedback de error por integridad referencial
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error en consola para diagnostico
            request.getSession().setAttribute("mensaje", "Error inesperado: " + e.getMessage()); // Muestra el error tecnico al usuario
            request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
        }

        response.sendRedirect(request.getContextPath() + "/ProveedorController"); // Recarga la lista de proveedores actualizada
    }
}
