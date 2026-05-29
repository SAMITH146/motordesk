package com.mycompany.motordesk.controller;

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

@WebServlet("/PanelMecanicoController")
public class PanelMecanicoController extends HttpServlet {

    private final PanelMecanicoDAO dashboardDao = new PanelMecanicoDAO();
    private final OrdenDAO ordenDao = new OrdenDAO();
    private final ProductoDAO productoDao = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Empleado user = (Empleado) session.getAttribute("usuarioLogueado");

        if (user != null) {
            String docMecanico = user.getIdEmpleado();
            
            // Stats for dashboard
            request.setAttribute("ordenesAbiertas", dashboardDao.contarOrdenesAbiertas(docMecanico));
            request.setAttribute("ordenesHoy", dashboardDao.contarOrdenesHoy(docMecanico));
            
            // Table lists
            List<OrdenTrabajo> misOrdenes = ordenDao.listarPorMecanico(docMecanico);
            request.setAttribute("listaOrdenes", misOrdenes);
            
            // Auxiliary data for form
            List<Producto> todosProductos = productoDao.listarTodos();
            request.setAttribute("listaProductos", todosProductos);
            
            request.setAttribute("stockBajo", dashboardDao.obtenerStockBajo());
            
            // Check if edit is requested
            String action = request.getParameter("action");
            if ("edit".equals(action)) {
                try {
                    int idOrden = Integer.parseInt(request.getParameter("id_orden"));
                    OrdenTrabajo ord = ordenDao.obtenerPorId(idOrden);
                    if (ord != null && ord.getDocEmpleFk().equals(docMecanico)) {
                        request.setAttribute("ordenEditar", ord);
                        request.setAttribute("detallesEditar", ordenDao.obtenerDetallesDeOrden(idOrden));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        request.getRequestDispatcher("/Mecanico/panelMecanico.jsp").forward(request, response);
    }
}