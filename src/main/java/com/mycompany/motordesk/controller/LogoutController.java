// Definición del paquete del proyecto
package com.mycompany.motordesk.controller;
// Importación de dependencias y clases necesarias
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Anotación que define la ruta de acceso URL para este Servlet
@WebServlet("/LogoutController")
// Clase pública LogoutController que gestiona la lógica correspondiente
public class LogoutController extends HttpServlet {

  
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        // Validación condicional
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}