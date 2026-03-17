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
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        try {

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

            // ✅ guardar en BD
            EmpleadoDAO dao = new EmpleadoDAO();
            boolean ok = dao.insertar(emp);

            System.out.println("INSERTADO: " + ok);

            // ✅ volver al panel
            response.sendRedirect(
                    request.getContextPath() + "/admin/panelAdmin.jsp"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
