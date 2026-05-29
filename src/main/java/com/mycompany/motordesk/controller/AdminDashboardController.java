package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.AdminDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/AdminDashboard")
public class AdminDashboardController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        AdminDAO dao = new AdminDAO();
        
        // Cargar estadísticas reales
        request.setAttribute("totalMecanicos", dao.contarMecanicosActivos());
        request.setAttribute("totalProductos", dao.contarProductos());
        request.setAttribute("totalOrdenes", dao.contarOrdenesTotales());
        request.setAttribute("stockCritico", dao.contarStockCritico());

        // Cargar recientes para el Dashboard
        java.util.List<com.mycompany.motordesk.model.Empleado> mecanicos = new com.mycompany.motordesk.dao.EmpleadoDAO().listarMecanicos();
        if(mecanicos != null && mecanicos.size() > 3) mecanicos = mecanicos.subList(0, 3);
        request.setAttribute("recentMecanicos", mecanicos);

        java.util.List<com.mycompany.motordesk.model.OrdenTrabajo> ordenes = new com.mycompany.motordesk.dao.OrdenDAO().listarTodas();
        if(ordenes != null && ordenes.size() > 3) ordenes = ordenes.subList(0, 3);
        request.setAttribute("recentOrdenes", ordenes);

        // Forward al JSP del panel
        request.getRequestDispatcher("/admin/panelAdmin.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
