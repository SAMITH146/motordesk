// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller;

// A continuación, importamos las dependencias y clases necesarias para nuestro controlador
import com.mycompany.motordesk.dao.EmpleadoDAO;
import com.mycompany.motordesk.model.Empleado;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Usamos esta anotación para definir la ruta de acceso URL para nuestro Servlet
/**
 * Este es nuestro Controlador encargado de gestionar las operaciones relacionadas con nuestros mecánicos.
 * Aquí permitimos listar, crear, editar, eliminar y cambiar el estado de nuestros empleados con rol de mecánico.
 */
@WebServlet("/MecanicoController")
public class MecanicoController extends HttpServlet {

    /**
     * En nuestro método doGet manejamos las peticiones HTTP GET.
     * Aquí cargamos la lista de mecánicos y, si solicitamos edición, cargamos los datos del mecánico a editar.
     * 
     * @param request La petición HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EmpleadoDAO dao = new EmpleadoDAO();

        // Capturamos la acción que vamos a realizar
        String action = request.getParameter("action");
        
        // Si nuestra acción es 'edit', buscamos el mecánico por su documento para cargar sus datos en nuestro formulario
        if ("edit".equals(action)) {
            try {
                String id = request.getParameter("id");
                Empleado emp = dao.obtenerPorId(id);
                request.setAttribute("empleadoEditar", emp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Obtenemos y enviamos a nuestra vista la lista de todos nuestros mecánicos
        request.setAttribute("listaMecanicos", dao.listarMecanicos());
        request.getRequestDispatcher("/Mecanico/gestionarMecanicos.jsp").forward(request, response);
    }

    /**
     * En nuestro método doPost manejamos las peticiones HTTP POST.
     * Aquí procesamos las operaciones de cambio de estado, edición, eliminación, 
     * actualización y creación de un nuevo mecánico.
     * 
     * @param request La petición HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        // Iniciamos nuestro bloque try para el control de excepciones
        try {
            // Evaluamos la acción solicitada por el usuario. Si la acción es 'toggleState', procedemos a cambiar el estado de actividad del mecánico en la base de datos y registramos el evento en la bitácora.
            if ("toggleState".equals(action)) {
                String id = request.getParameter("id");
                EmpleadoDAO dao = new EmpleadoDAO();
                boolean ok = dao.toggleEstado(id);
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Estado del mecánico actualizado.");
                    request.getSession().setAttribute("tipoMensaje", "info");
                    Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado");
                    if (actor != null) {
                        new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                            actor.getIdEmpleado(),
                            actor.getNombre(),
                            "MODIFICAR EMPLEADO",
                            "Se cambió el estado del empleado con documento: " + id
                        );
                    }
                }
                System.out.println("ESTADO CAMBIADO: " + ok);

            } else if ("edit".equals(action)) {
                // Future edit logic
                System.out.println("EDIT_ACTION_RECEIVED");

            } else if ("delete".equals(action)) {
                String id = request.getParameter("id");
                EmpleadoDAO dao = new EmpleadoDAO();
                boolean ok = dao.eliminar(id);
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Mecánico eliminado correctamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                    Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado");
                    if (actor != null) {
                        new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                            actor.getIdEmpleado(),
                            actor.getNombre(),
                            "ELIMINAR EMPLEADO",
                            "Se eliminó al empleado con documento: " + id
                        );
                    }
                } else {
                    request.getSession().setAttribute("mensaje", "No se pudo eliminar el mecánico. Es posible que tenga órdenes asociadas.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
                System.out.println("ELIMINADO: " + ok);

            } else if ("update".equals(action)) {
                // Procedemos a actualizar nuestro mecánico existente
                String doc = request.getParameter("doc_emple");
                String pin = request.getParameter("pin_acceso");
                EmpleadoDAO dao = new EmpleadoDAO();

                // Verificamos que el documento ingresado sea válido (entre 6 y 10 caracteres) y comprobamos que el PIN no esté en uso por otro usuario. Si los datos son correctos, procedemos a actualizar la información del mecánico en el sistema.
                if (doc == null || doc.trim().length() < 6 || doc.trim().length() > 10) {
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

                    if (ok) {
                        request.getSession().setAttribute("mensaje", "Mecánico actualizado correctamente.");
                        request.getSession().setAttribute("tipoMensaje", "success");
                        Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado");
                        if (actor != null) {
                            new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                                actor.getIdEmpleado(),
                                actor.getNombre(),
                                "MODIFICAR EMPLEADO",
                                "Se actualizaron los datos del empleado: " + emp.getNombre() + " (Doc: " + emp.getIdEmpleado() + ")"
                            );
                        }
                    } else {
                        request.getSession().setAttribute("mensaje", "Error al actualizar el mecánico.");
                        request.getSession().setAttribute("tipoMensaje", "error");
                    }
                    System.out.println("ACTUALIZADO: " + ok);
                }

            } else {
                // Registramos nuestro nuevo mecánico (cuando la acción es "insert" o viene sin acción)
                String doc = request.getParameter("doc_emple");
                String pin = request.getParameter("pin_acceso");
                EmpleadoDAO dao = new EmpleadoDAO();

                System.out.println("[MecanicoController] CREAR mecánico | doc='" + doc + "' | pin='" + pin + "'");

                // Realizamos la validación: aseguramos que los documentos colombianos válidos tengan entre 6 y 10 dígitos
                if (doc == null || doc.trim().isEmpty()) {
                    request.getSession().setAttribute("mensaje", "El documento es obligatorio.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                } else if (doc.trim().length() < 6 || doc.trim().length() > 10) {
                    request.getSession().setAttribute("mensaje",
                        "Documento inválido. Debe tener entre 6 y 10 dígitos. Se ingresaron " + doc.trim().length() + ".");
                    request.getSession().setAttribute("tipoMensaje", "error");
                } else if (pin == null || pin.trim().length() < 4) {
                    request.getSession().setAttribute("mensaje", "El PIN debe tener al menos 4 dígitos.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                } else if (dao.existeDocumento(doc.trim())) {
                    request.getSession().setAttribute("mensaje",
                        "El documento '" + doc.trim() + "' ya está registrado en el sistema.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                } else if (dao.existePin(pin.trim(), null)) {
                    request.getSession().setAttribute("mensaje", "Ese PIN ya está en uso por otro mecánico. Usa uno diferente.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                } else {
                    Empleado emp = new Empleado();
                    emp.setIdEmpleado(doc.trim());
                    emp.setNombre(request.getParameter("nom_empleado"));
                    emp.setPin(pin.trim());
                    emp.setIdRol(Integer.parseInt(request.getParameter("id_rol_fk")));
                    emp.setIdCargo(Integer.parseInt(request.getParameter("id_cargo_fk")));

                    // Guardamos en nuestra base de datos y capturamos la excepción para mostrar el error real de MySQL
                    try {
                        boolean ok = dao.insertar(emp);
                        if (ok) {
                            request.getSession().setAttribute("mensaje", "Mecánico registrado exitosamente.");
                            request.getSession().setAttribute("tipoMensaje", "success");
                            Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado");
                            if (actor != null) {
                                new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                                    actor.getIdEmpleado(),
                                    actor.getNombre(),
                                    "CREAR EMPLEADO",
                                    "Se registró un nuevo empleado: " + emp.getNombre() + " (Doc: " + emp.getIdEmpleado() + ")"
                                );
                            }
                        } else {
                            request.getSession().setAttribute("mensaje",
                                "No se pudo insertar el mecánico (BD retornó 0 filas afectadas). Verifica la consola del servidor.");
                            request.getSession().setAttribute("tipoMensaje", "error");
                        }
                        System.out.println("[MecanicoController] INSERTADO: " + ok);
                    } catch (Exception dbEx) {
                        // Mostramos el mensaje real de MySQL directamente a nuestro usuario para facilitar el diagnóstico
                        String msgBD = dbEx.getMessage() != null ? dbEx.getMessage() : dbEx.getClass().getSimpleName();
                        System.err.println("[MecanicoController] SQL ERROR: " + msgBD);
                        dbEx.printStackTrace();
                        request.getSession().setAttribute("mensaje",
                            "Error de base de datos: " + msgBD);
                        request.getSession().setAttribute("tipoMensaje", "error");
                    }
                }
            } // fin else (insertar)

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Siempre nos aseguramos de volver a nuestro panel de mecánicos
            response.sendRedirect(request.getContextPath() + "/MecanicoController#mecanicos");
        }
    }
}
