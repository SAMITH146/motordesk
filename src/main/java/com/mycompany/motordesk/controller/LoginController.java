// Este archivo pertenece al paquete "controller" del proyecto MotorDesk
package com.mycompany.motordesk.controller;

// Importamos el DAO de empleados para poder consultar la base de datos
import com.mycompany.motordesk.dao.EmpleadoDAO;
// Importamos el modelo Empleado para trabajar con sus datos (nombre, rol, etc.)
import com.mycompany.motordesk.model.Empleado;
// Librería para manejar errores de entrada/salida (redirecciones, etc.)
import java.io.IOException;
// Clases base de los Servlets de Java
import javax.servlet.*;
// Anotación que convierte esta clase en un Servlet accesible por URL
import javax.servlet.annotation.WebServlet;
// Clases para manejar la sesión y la petición/respuesta HTTP
import javax.servlet.http.*;

// Esta anotación le dice al servidor Tomcat que este Servlet responde
// cuando alguien acceda a la URL: /LoginController
@WebServlet("/LoginController")
// Clase principal del inicio de sesión — extiende HttpServlet para ser un Servlet
public class LoginController extends HttpServlet {

    // doPost se ejecuta cuando el usuario envía el formulario del login (botón "Ingresar")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Leemos el PIN que el usuario escribió en el formulario y eliminamos espacios
        String pin = request.getParameter("pin").trim();

        // Creamos una instancia del DAO para poder consultar la tabla empleado en MySQL
        EmpleadoDAO dao = new EmpleadoDAO();

        // Le preguntamos a la base de datos: ¿existe algún empleado activo con este PIN?
        // Retorna un objeto Empleado si lo encuentra, o null si el PIN no existe
        Empleado emp = dao.loginPorPin(pin);

        // Evaluamos si la autenticación fue exitosa (el PIN existía en la BD)
        if (emp != null) {

            // Creamos (o reutilizamos) la sesión HTTP del usuario
            // La sesión es como una "caja" que el servidor mantiene abierta mientras el usuario navega
            HttpSession session = request.getSession();

            // Guardamos el objeto Empleado dentro de la sesión con el nombre "usuarioLogueado"
            // Cualquier JSP puede leer este dato con: ${sessionScope.usuarioLogueado.nombre}
            session.setAttribute("usuarioLogueado", emp);

            // Verificamos el rol del empleado para enviarlo a la pantalla correcta
            // Rol 1 = Administrador
            if (emp.getIdRol() == 1) {
                // Redirigimos al servlet que prepara y muestra el Dashboard del Administrador
                response.sendRedirect(request.getContextPath() + "/AdminDashboard");
            }
            // Rol 2 = Mecánico
            else if (emp.getIdRol() == 2) {
                // Redirigimos al servlet que prepara el panel de trabajo del Mecánico
                response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");
            }
            // Si el rol no es ni 1 ni 2, es un caso inesperado — mostramos error
            else {
                request.setAttribute("mensajeError", "Error: Rol no válido asignado a este usuario (" + emp.getIdRol() + ").");
                // Volvemos al formulario de login con el mensaje de error visible
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }

        } else {
            // Si emp == null significa que el PIN no existe o el empleado está inactivo
            // Colocamos el mensaje de error como atributo para que el JSP lo muestre
            request.setAttribute("mensajeError", "PIN NO ASOCIADO A NINGÚN ADMIN O MECÁNICO");
            // Reenviamos la petición de vuelta al login.jsp sin cambiar la URL del navegador
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
