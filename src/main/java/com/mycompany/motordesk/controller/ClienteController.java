package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.ClienteDAO;
import com.mycompany.motordesk.model.Cliente;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/ClienteController")
public class ClienteController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        ClienteDAO dao = new ClienteDAO();
        String action = request.getParameter("action");
        
        if ("edit".equals(action)) {
            String doc = request.getParameter("doc");
            Cliente c = dao.obtenerPorDocumento(doc);
            if (c != null) {
                request.setAttribute("clienteEditar", c);
            }
        }
        
        request.setAttribute("listaClientes", dao.listarTodos());
        request.getRequestDispatcher("/admin/gestionarClientes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        ClienteDAO dao = new ClienteDAO();
        
        if ("update".equals(action)) {
            Cliente c = new Cliente();
            c.setDocumento(request.getParameter("doc_cliente"));
            c.setNombre(request.getParameter("nom_cliente"));
            c.setDireccion(request.getParameter("direccion_cliente"));
            
            if (dao.actualizar(c)) {
                request.getSession().setAttribute("mensaje", "Cliente actualizado correctamente.");
                request.getSession().setAttribute("tipoMensaje", "success");
            } else {
                request.getSession().setAttribute("mensaje", "Error al actualizar el cliente.");
                request.getSession().setAttribute("tipoMensaje", "error");
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/ClienteController");
    }
}
