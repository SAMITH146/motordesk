package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.EmpleadoDAO;
import com.mycompany.motordesk.model.Empleado;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/LoginController")
public class LoginController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pin = request.getParameter("pin").trim();

        EmpleadoDAO dao = new EmpleadoDAO();
        Empleado emp = dao.loginPorPin(pin);

        if (emp != null) {

            // 🔐 guardar usuario en sesión
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogueado", emp);

            // ================= ADMIN =================
            if (emp.getIdRol() == 1) {
                response.sendRedirect(request.getContextPath() + "/AdminDashboard");
            }
            // ================= MECANICO =================
            else if (emp.getIdRol() == 2) {

                response.sendRedirect(
                        request.getContextPath() + "/PanelMecanicoController"
                );
            }
            // ================= OTROS ROLES O ROL NULO =================
            else {
                request.setAttribute("mensajeError", "Error: Rol no válido asignado a este usuario (" + emp.getIdRol() + ").");
                request.getRequestDispatcher("login.jsp")
                        .forward(request, response);
            }

        } else {

            request.setAttribute("mensajeError", "PIN NO ASOCIADO A NINGÚN ADMIN O MECÁNICO");
            request.getRequestDispatcher("login.jsp")
                    .forward(request, response);
        }
    }
}

//        if (emp.getIdRol() == 0) {
//            request.getRequestDispatcher("/admin/panelAdmin.jsp").forward(request, response);
//            return;
//
//        } else if (emp.getIdRol() == 2) {
//            request.getRequestDispatcher("/mecanico/panelMecanico.jsp").forward(request, response);
//
//        }
//            return;
//
//    }
//
//    
//        else {
//
//            request.setAttribute("mensajeError", "PIN incorrecto");
//        request.getRequestDispatcher("login.jsp").forward(request, response);
//    }
//}
//}
