package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.config.Conexion;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/test")
public class TestConexionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        Connection con = Conexion.getConexion();

        if (con != null) {
            response.getWriter().println("<h1>Conexion exitosa a MySQL</h1>");
        } else {
            response.getWriter().println("<h1>Error de conexion</h1>");
        }
    }
}
