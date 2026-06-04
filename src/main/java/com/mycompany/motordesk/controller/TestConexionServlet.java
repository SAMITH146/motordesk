// Definición del paquete del proyecto
package com.mycompany.motordesk.controller;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Anotación que define la ruta de acceso URL para este Servlet
@WebServlet("/test")
// Clase pública TestConexionServlet que gestiona la lógica correspondiente
public class TestConexionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        // Obtención de la conexión física a la base de datos MySQL
        Connection con = Conexion.getConexion();

        // Validación condicional
        if (con != null) {
            response.getWriter().println("<h1>Conexion exitosa a MySQL</h1>");
        } else {
            response.getWriter().println("<h1>Error de conexion</h1>");
        }
    }
}
