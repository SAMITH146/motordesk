package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.PanelMecanicoDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/panelMecanico")
public class PanelMecanicoController extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        try {

            PanelMecanicoDAO dao = new PanelMecanicoDAO();

            // ===== GRAFICA =====
            request.setAttribute("alturaB1", dao.alturaDia(1));
            request.setAttribute("alturaB2", dao.alturaDia(2));
            request.setAttribute("alturaB3", dao.alturaDia(3));
            request.setAttribute("alturaB4", dao.alturaDia(4));
            request.setAttribute("alturaB5", dao.alturaDia(5));
            request.setAttribute("alturaB6", dao.alturaDia(6));

            // ===== RESUMEN =====
            request.setAttribute("serviciosTotales", dao.totalServicios());
            request.setAttribute("ordenesTotales", dao.totalOrdenes());
            request.setAttribute("dineroTotal", dao.totalDinero());

            // ===== INVENTARIO =====
            request.setAttribute(
                "productosStockBajo",
                dao.obtenerStockBajo()
            );

            // enviar al JSP
            request.getRequestDispatcher(
                "/mecanico/panelMecanico.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}