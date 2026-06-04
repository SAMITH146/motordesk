// Definición del paquete del proyecto
package com.mycompany.motordesk.controller;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.dao.EmpleadoDAO;
import com.mycompany.motordesk.model.Empleado;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Anotación que define la ruta de acceso URL para este Servlet
@WebServlet("/MecanicoController")
// Clase pública MecanicoController que gestiona la lógica correspondiente
public class MecanicoController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        EmpleadoDAO dao = new EmpleadoDAO();
        
        String action = request.getParameter("action");
        // Validación condicional
        if ("edit".equals(action)) {
            // Inicio del bloque try para control de excepciones
            try {
                String id = request.getParameter("id");
                Empleado emp = dao.obtenerPorId(id);
                request.setAttribute("empleadoEditar", emp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        request.setAttribute("listaMecanicos", dao.listarMecanicos());
        request.getRequestDispatcher("/Mecanico/gestionarMecanicos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        // Inicio del bloque try para control de excepciones
        try {
            // Validación condicional
            if ("toggleState".equals(action)) {
                String id = request.getParameter("id");
                EmpleadoDAO dao = new EmpleadoDAO();
                boolean ok = dao.toggleEstado(id);
                // Validación condicional
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Estado del mecánico actualizado.");
                    request.getSession().setAttribute("tipoMensaje", "info");
                }
                System.out.println("ESTADO CAMBIADO: " + ok);
            } else if ("edit".equals(action)) {
                // Future edit logic
                System.out.println("EDIT_ACTION_RECEIVED");
            } else if ("delete".equals(action)) {
                String id = request.getParameter("id");
                EmpleadoDAO dao = new EmpleadoDAO();
                boolean ok = dao.eliminar(id);
                // Validación condicional
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Mecánico eliminado correctamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } else {
                    request.getSession().setAttribute("mensaje", "No se pudo eliminar el mecánico. Es posible que tenga órdenes asociadas.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
                System.out.println("ELIMINADO: " + ok);
            } else if ("update".equals(action)) {
                // Actualizar mecánico existente
                String doc = request.getParameter("doc_emple");
                String pin = request.getParameter("pin_acceso");
                EmpleadoDAO dao = new EmpleadoDAO();
                
                // Validación condicional
                if (doc == null || doc.trim().length() < 9 || doc.trim().length() > 10) {
                    request.getSession().setAttribute("mensaje", "No se guardó porque no es un documento válido.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                } else if (dao.existePin(pin, doc.trim())) {
                    request.getSession().setAttribute("mensaje", "Ese PIN no está disponible.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                } else {
                    Empleado emp = new Empleado();
                    emp.setIdEmpleado(doc.trim());
                    emp.setNombre(request.getParameter("nom_empleado"));
                    emp.setPin(pin);
                    emp.setIdRol(Integer.parseInt(request.getParameter("id_rol_fk")));
                    emp.setIdCargo(Integer.parseInt(request.getParameter("id_cargo_fk")));

                    boolean ok = dao.actualizar(emp);

                    // Validación condicional
                    if (ok) {
                        request.getSession().setAttribute("mensaje", "Mecánico actualizado correctamente.");
                        request.getSession().setAttribute("tipoMensaje", "success");
                    } else {
                        request.getSession().setAttribute("mensaje", "Error al actualizar el mecánico.");
                        request.getSession().setAttribute("tipoMensaje", "error");
                    }
                    System.out.println("ACTUALIZADO: " + ok);
                }
            } else {
                // Registrar nuevo mecánico
                String doc = request.getParameter("doc_emple");
                String pin = request.getParameter("pin_acceso");
                EmpleadoDAO dao = new EmpleadoDAO();
                
                // Validación condicional
                if (doc == null || doc.trim().length() < 9 || doc.trim().length() > 10) {
                    request.getSession().setAttribute("mensaje", "No se guardó porque no es un documento válido. (Debe tener 9 o 10 dígitos)");
                    request.getSession().setAttribute("tipoMensaje", "error");
                } else if (dao.existePin(pin, null)) {
                    request.getSession().setAttribute("mensaje", "Ese PIN no está disponible.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                } else {
                    Empleado emp = new Empleado();

                    emp.setIdEmpleado(doc.trim());

                    emp.setNombre(request.getParameter("nom_empleado"));
                    emp.setPin(pin);

                    emp.setIdRol(
                            Integer.parseInt(request.getParameter("id_rol_fk"))
                    );

                    emp.setIdCargo(
                            Integer.parseInt(request.getParameter("id_cargo_fk"))
                    );

                    // guardar en BD
                    boolean ok = dao.insertar(emp);
                    // Validación condicional
                    if (ok) {
                        request.getSession().setAttribute("mensaje", "Mecánico registrado exitosamente.");
                        request.getSession().setAttribute("tipoMensaje", "success");
                    } else {
                        request.getSession().setAttribute("mensaje", "Error al registrar el mecánico.");
                        request.getSession().setAttribute("tipoMensaje", "error");
                    }

                    System.out.println("INSERTADO: " + ok);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // siempre volver al panel de mecanicos con la lista actualizada y la tabla visible
            response.sendRedirect(request.getContextPath() + "/MecanicoController#mecanicos");
        }
    }
}
