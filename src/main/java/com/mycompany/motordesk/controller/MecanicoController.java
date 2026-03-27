package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.EmpleadoDAO;
import com.mycompany.motordesk.model.Empleado;

import java.io.IOException;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/MecanicoController")
public class MecanicoController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        EmpleadoDAO dao = new EmpleadoDAO();
        
        String action = request.getParameter("action");
        if ("edit".equals(action)) {
            try {
                Long id = Long.parseLong(request.getParameter("id"));
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

        try {
            if ("toggleState".equals(action)) {
                Long id = Long.parseLong(request.getParameter("id"));
                EmpleadoDAO dao = new EmpleadoDAO();
                boolean ok = dao.toggleEstado(id);
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Estado del mecánico actualizado.");
                    request.getSession().setAttribute("tipoMensaje", "info");
                }
                System.out.println("ESTADO CAMBIADO: " + ok);
            } else if ("edit".equals(action)) {
                // Future edit logic
                System.out.println("EDIT_ACTION_RECEIVED");
            } else if ("delete".equals(action)) {
                Long id = Long.parseLong(request.getParameter("id"));
                EmpleadoDAO dao = new EmpleadoDAO();
                boolean ok = dao.eliminar(id);
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
                Empleado emp = new Empleado();
                emp.setIdEmpleado(Long.parseLong(request.getParameter("doc_emple")));
                emp.setNombre(request.getParameter("nom_empleado"));
                emp.setPin(request.getParameter("pin_acceso"));
                emp.setIdRol(Integer.parseInt(request.getParameter("id_rol_fk")));
                emp.setIdCargo(Integer.parseInt(request.getParameter("id_cargo_fk")));

                EmpleadoDAO dao = new EmpleadoDAO();
                boolean ok = dao.actualizar(emp);

                if (ok) {
                    request.getSession().setAttribute("mensaje", "Mecánico actualizado correctamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } else {
                    request.getSession().setAttribute("mensaje", "Error al actualizar el mecánico.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
                System.out.println("ACTUALIZADO: " + ok);
            } else {
                // Registrar nuevo mecánico
                Empleado emp = new Empleado();

                emp.setIdEmpleado(
                        Long.parseLong(request.getParameter("doc_emple"))
                );

                emp.setNombre(request.getParameter("nom_empleado"));
                emp.setPin(request.getParameter("pin_acceso"));

                emp.setIdRol(
                        Integer.parseInt(request.getParameter("id_rol_fk"))
                );

                emp.setIdCargo(
                        Integer.parseInt(request.getParameter("id_cargo_fk"))
                );

                // guardar en BD
                EmpleadoDAO dao = new EmpleadoDAO();
                boolean ok = dao.insertar(emp);
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Mecánico registrado exitosamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } else {
                    request.getSession().setAttribute("mensaje", "Error al registrar el mecánico.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                }

                System.out.println("INSERTADO: " + ok);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // siempre volver al panel de mecanicos con la lista actualizada y la tabla visible
            response.sendRedirect(request.getContextPath() + "/MecanicoController#mecanicos");
        }
    }
}
