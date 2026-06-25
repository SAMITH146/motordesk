package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.ProveedorDAO;
import com.mycompany.motordesk.model.Proveedor;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Este es nuestro Controlador encargado de gestionar nuestros proveedores de repuestos.
 * Aquí permitimos las operaciones CRUD básicas sobre la tabla de proveedores.
 */
@WebServlet("/ProveedorController")
public class ProveedorController extends HttpServlet {

    /**
     * En nuestro método doGet manejamos las peticiones HTTP GET.
     * Aquí listamos todos nuestros proveedores y, si corresponde, cargamos los datos para editar.
     * 
     * @param request La petición HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        ProveedorDAO dao = new ProveedorDAO();
        String action = request.getParameter("action");

        if ("edit".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.trim().isEmpty()) {
                int id = Integer.parseInt(idStr);
                Proveedor p = dao.obtenerPorId(id);
                if (p != null) {
                    request.setAttribute("proveedorEditar", p);
                }
            }
        }

        // Siempre nos aseguramos de cargar la lista
        request.setAttribute("listaProveedores", dao.listarTodos());
        request.getRequestDispatcher("/admin/gestionarProveedores.jsp").forward(request, response);
    }

    /**
     * En nuestro método doPost manejamos las peticiones HTTP POST.
     * Aquí procesamos el alta, edición y eliminación de un proveedor en nuestro sistema.
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
        String action = request.getParameter("action");
        ProveedorDAO dao = new ProveedorDAO();

        try {
            if ("insert".equals(action)) {
                Proveedor p = new Proveedor();
                p.setNombreProveedor(request.getParameter("nombre_proveedor"));
                p.setContacto(request.getParameter("contacto"));
                p.setTelefono(request.getParameter("telefono"));
                p.setCorreo(request.getParameter("correo"));

                if (dao.insertar(p)) {
                    request.getSession().setAttribute("mensaje", "Proveedor registrado exitosamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } else {
                    request.getSession().setAttribute("mensaje", "Error al registrar el proveedor.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
            } else if ("update".equals(action)) {
                Proveedor p = new Proveedor();
                p.setIdProveedor(Integer.parseInt(request.getParameter("id_proveedor")));
                p.setNombreProveedor(request.getParameter("nombre_proveedor"));
                p.setContacto(request.getParameter("contacto"));
                p.setTelefono(request.getParameter("telefono"));
                p.setCorreo(request.getParameter("correo"));

                if (dao.actualizar(p)) {
                    request.getSession().setAttribute("mensaje", "Proveedor actualizado exitosamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } else {
                    request.getSession().setAttribute("mensaje", "Error al actualizar el proveedor.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id_proveedor"));
                if (dao.eliminar(id)) {
                    request.getSession().setAttribute("mensaje", "Proveedor eliminado exitosamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } else {
                    request.getSession().setAttribute("mensaje", "Error al eliminar. Es posible que este proveedor ya tenga compras registradas y no se pueda borrar.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("mensaje", "Error inesperado: " + e.getMessage());
            request.getSession().setAttribute("tipoMensaje", "error");
        }

        response.sendRedirect(request.getContextPath() + "/ProveedorController");
    }
}
