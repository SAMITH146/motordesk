// Este archivo pertenece al paquete "controller" de nuestro proyecto MotorDesk
package com.mycompany.motordesk.controller;

// Aquí importamos el DAO de empleados para que podamos consultar la base de datos
import com.mycompany.motordesk.dao.EmpleadoDAO;
// Importamos el modelo Empleado para que trabajemos con sus datos
import com.mycompany.motordesk.model.Empleado;
// Librería que nos permite manejar errores de entrada/salida
import java.io.IOException;
// Clases base de los Servlets de Java que utilizamos
import javax.servlet.*;
// Anotación con la que convertimos esta clase en un Servlet accesible por URL
import javax.servlet.annotation.WebServlet;
// Clases para que manejemos la sesión y la petición/respuesta HTTP
import javax.servlet.http.*;

// Con esta anotación le decimos a nuestro servidor Tomcat que este Servlet responde
// cuando alguien accede a la URL: /LoginController
/**
 * Aquí les presentamos el controlador encargado de gestionar el inicio de sesión de nuestros empleados.
 * Nos permite autenticar mediante un PIN y luego redirigimos según el rol del usuario.
 */
@WebServlet("/LoginController")
// Clase principal de nuestro inicio de sesión
public class LoginController extends HttpServlet {

    /**
     * Este es nuestro método doPost, que usamos para manejar las peticiones HTTP POST de inicio de sesión.
     * 
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Primero leemos el PIN que el usuario escribió en el formulario y le eliminamos los espacios
        String pin = request.getParameter("pin").trim();

        // Creamos una instancia de nuestro DAO para consultar la tabla empleado en nuestra base de datos
        EmpleadoDAO dao = new EmpleadoDAO();

        // Le preguntamos a nuestra base de datos si existe algún empleado activo con este PIN
        Empleado emp = dao.loginPorPin(pin);

        // Evaluamos si la autenticación que hicimos fue exitosa
        if (emp != null) {

            // Creamos o reutilizamos la sesión HTTP de nuestro usuario
            HttpSession session = request.getSession();

            // Guardamos el objeto Empleado dentro de nuestra sesión
            session.setAttribute("usuarioLogueado", emp);
            
            // Registramos este evento en nuestra bitácora para llevar control
            new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                emp.getIdEmpleado(),
                emp.getNombre(),
                "LOGIN",
                "Inicio de sesión exitoso en el sistema. Rol: " + (emp.getIdRol() == 1 ? "ADMIN" : "MECANICO")
            );

            // Verificamos el rol de nuestro empleado para enviarlo a la pantalla correcta
            if (emp.getIdRol() == 1) {
                // Si es administrador, lo redirigimos al Dashboard del Administrador
                response.sendRedirect(request.getContextPath() + "/AdminDashboard");
            }
            else if (emp.getIdRol() == 2) {
                // Si es mecánico, lo redirigimos al panel de trabajo del Mecánico
                response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");
            }
            else {
                // Si el rol no es válido, mostramos un error porque es un caso inesperado
                request.setAttribute("mensajeError", "Error: Rol no válido asignado a este usuario (" + emp.getIdRol() + ").");
                // Volvemos a nuestro formulario de login mostrando el mensaje de error
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }

        } else {
            // Si emp es null, significa que el PIN no existe o el empleado está inactivo
            // Por lo tanto, colocamos el mensaje de error para que nuestro JSP lo muestre
            request.setAttribute("mensajeError", "PIN NO ASOCIADO A NINGÚN ADMIN O MECÁNICO");
            // Reenviamos la petición de vuelta a nuestro login
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
