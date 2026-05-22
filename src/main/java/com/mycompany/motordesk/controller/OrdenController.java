package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.OrdenDAO;
import com.mycompany.motordesk.model.DetalleOrden;
import com.mycompany.motordesk.model.OrdenTrabajo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/OrdenController")
public class OrdenController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        OrdenDAO dao = new OrdenDAO();
        String action = request.getParameter("action");
        
        if ("listAll".equals(action)) {
            request.setAttribute("listaOrdenes", dao.listarTodas());
            request.getRequestDispatcher("/admin/gestionarOrdenes.jsp").forward(request, response);
        } else {
            // Default could be redirecting to home or dashboard
            response.sendRedirect(request.getContextPath() + "/AdminDashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        OrdenDAO dao = new OrdenDAO();
        
        try {
            if ("insert".equals(action)) {
                // Register new order from Mechanic
                OrdenTrabajo o = new OrdenTrabajo();
                o.setPlacaVehiculo(request.getParameter("placa"));
                o.setDescripcion(request.getParameter("descripcion"));
                o.setDocEmpleFk(request.getParameter("id_mecanico"));
                o.setEstado("ABIERTA");

                // Get products from parameters (This is a simplified approach)
                String[] prodIds = request.getParameterValues("productos[]");
                String[] prodsCant = request.getParameterValues("cantidades[]");
                String[] prodsPrecio = request.getParameterValues("precios[]");
                
                List<DetalleOrden> detalles = new ArrayList<>();
                if (prodIds != null) {
                    for (int i = 0; i < prodIds.length; i++) {
                        if (prodIds[i] != null && !prodIds[i].trim().isEmpty()) {
                            try {
                                DetalleOrden d = new DetalleOrden();
                                d.setIdProductoFk(Integer.parseInt(prodIds[i]));
                                String cantStr = (prodsCant != null && prodsCant.length > i && prodsCant[i] != null && !prodsCant[i].trim().isEmpty()) ? prodsCant[i] : "1";
                                d.setCantidad(Integer.parseInt(cantStr));
                                String pStr = (prodsPrecio != null && prodsPrecio.length > i && prodsPrecio[i] != null && !prodsPrecio[i].trim().isEmpty()) ? prodsPrecio[i] : "0";
                                d.setSubtotal(Double.parseDouble(pStr));
                                detalles.add(d);
                            } catch (NumberFormatException e) {
                                // Ignore malformed rows
                            }
                        }
                    }
                }
                
                boolean ok = dao.insertarOrden(o, detalles);
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Orden registrada exitosamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } else {
                    request.getSession().setAttribute("mensaje", "Error al registrar la orden.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
                response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");

            } else if ("updateStatus".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id_orden"));
                String nuevoEstado = request.getParameter("nuevo_estado");
                
                boolean ok = dao.actualizarEstado(id, nuevoEstado);
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Estado de la orden actualizado.");
                    request.getSession().setAttribute("tipoMensaje", "info");
                }
                response.sendRedirect(request.getContextPath() + "/OrdenController?action=listAll");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/AdminDashboard");
        }
    }
}
