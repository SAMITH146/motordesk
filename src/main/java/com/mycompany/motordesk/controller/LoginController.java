// Definición del paquete del proyecto
package com.mycompany.motordesk.controller;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.dao.EmpleadoDAO;
import com.mycompany.motordesk.model.Empleado;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Controlador Servlet que maneja la autenticación de usuarios (Administradores y Mecánicos) por PIN
// Anotación que define la ruta de acceso URL para este Servlet
@WebServlet("/LoginController")
// Clase pública LoginController que gestiona la lógica correspondiente
public class LoginController extends HttpServlet {

    // Método doPost que responde a las peticiones POST enviadas por el formulario de login.jsp
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Obtener y recortar espacios en blanco del PIN ingresado en el formulario
        String pin = request.getParameter("pin").trim();

        // 2. Instanciar el objeto DAO de acceso a datos para empleados
        EmpleadoDAO dao = new EmpleadoDAO();
        
        // 3. Consultar en la base de datos si existe un empleado activo con ese PIN
        Empleado emp = dao.loginPorPin(pin);

        // 4. Evaluar si la autenticación fue exitosa
        // Validación condicional
        if (emp != null) {

            // 🔐 5. Obtener la sesión HTTP actual (o crear una nueva) y almacenar al empleado autenticado
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogueado", emp);

            // ================= RUTA PARA ADMINISTRADOR =================
            // Validación condicional
            if (emp.getIdRol() == 1) {
                // Redirigir al servlet que alimenta la vista del dashboard administrativo
                response.sendRedirect(request.getContextPath() + "/AdminDashboard");
            }
            // ================= RUTA PARA MECÁNICO =================
            else if (emp.getIdRol() == 2) {
                // Redirigir al servlet que maneja el panel de control del mecánico
                response.sendRedirect(
                        request.getContextPath() + "/PanelMecanicoController"
                );
            }
            // ================= CASOS ESPECIALES DE ROL NO DEFINIDO =================
            else {
                request.setAttribute("mensajeError", "Error: Rol no válido asignado a este usuario (" + emp.getIdRol() + ").");
                request.getRequestDispatcher("login.jsp")
                        .forward(request, response);
            }

        } else {
            // ❌ 6. Si el PIN es incorrecto o el empleado está inactivo, mostrar error en login.jsp
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
