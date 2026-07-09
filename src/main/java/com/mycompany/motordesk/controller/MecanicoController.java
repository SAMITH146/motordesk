// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller; // Paquete que agrupa todos los controladores de MotorDesk

// A continuacion, importamos las dependencias y clases necesarias para nuestro controlador
import com.mycompany.motordesk.dao.EmpleadoDAO; // DAO para operaciones CRUD sobre la tabla empleado
import com.mycompany.motordesk.model.Empleado; // Modelo que representa a un empleado del taller

import java.io.IOException; // Excepcion para errores de entrada/salida del servlet
import javax.servlet.*; // Importa clases genericas de servlet
import javax.servlet.annotation.WebServlet; // Anotacion para mapear el servlet a una URL
import javax.servlet.http.*; // Importa clases HTTP: request, response, session

// Usamos esta anotacion para definir la ruta de acceso URL para nuestro Servlet
/**
 * Este es nuestro Controlador encargado de gestionar las operaciones relacionadas con nuestros mecanicos.
 * Aqui permitimos listar, crear, editar, eliminar y cambiar el estado de nuestros empleados con rol de mecanico.
 */
@WebServlet("/MecanicoController") // Mapea el servlet a la ruta /MecanicoController
public class MecanicoController extends HttpServlet { // Controlador CRUD de mecanicos del taller

    /**
     * En nuestro metodo doGet manejamos las peticiones HTTP GET.
     * Aqui cargamos la lista de mecanicos y, si solicitamos edicion, cargamos los datos del mecanico a editar.
     *
     * @param request La peticion HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EmpleadoDAO dao = new EmpleadoDAO(); // Instancia el DAO para consultar la tabla de empleados

        // Capturamos la accion que vamos a realizar
        String action = request.getParameter("action"); // Lee la accion solicitada (p. ej., 'edit')

        // Si nuestra accion es 'edit', buscamos el mecanico por su documento para cargar sus datos en nuestro formulario
        if ("edit".equals(action)) { // Solo busca el mecanico si la accion es 'edit'
            try {
                String id = request.getParameter("id"); // Obtiene el documento del mecanico a editar
                Empleado emp = dao.obtenerPorId(id); // Busca el mecanico en la base de datos por su documento
                request.setAttribute("empleadoEditar", emp); // Expone el mecanico encontrado al JSP para pre-llenar el formulario
            } catch (Exception e) {
                e.printStackTrace(); // Imprime el error si el mecanico no se encuentra
            }
        }

        // Obtenemos y enviamos a nuestra vista la lista de todos nuestros mecanicos
        request.setAttribute("listaMecanicos", dao.listarMecanicos()); // Lista solo empleados con rol de mecanico
        request.getRequestDispatcher("/Mecanico/gestionarMecanicos.jsp").forward(request, response); // Renderiza la vista de gestion de mecanicos
    }

    /**
     * En nuestro metodo doPost manejamos las peticiones HTTP POST.
     * Aqui procesamos las operaciones de cambio de estado, edicion, eliminacion,
     * actualizacion y creacion de un nuevo mecanico.
     *
     * @param request La peticion HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // Evita problemas con caracteres especiales del espanol
        String action = request.getParameter("action"); // Lee la accion enviada por el formulario

        // Iniciamos nuestro bloque try para el control de excepciones
        try {
            // Evaluamos la accion solicitada por el usuario. Si la accion es 'toggleState', procedemos a cambiar el estado de actividad del mecanico en la base de datos y registramos el evento en la bitacora.
            if ("toggleState".equals(action)) { // Cambia el estado Activo/Inactivo del mecanico
                String id = request.getParameter("id"); // Documento del mecanico cuyo estado se va a cambiar
                EmpleadoDAO dao = new EmpleadoDAO(); // DAO para ejecutar el cambio de estado
                boolean ok = dao.toggleEstado(id); // Cambia el estado del mecanico en la base de datos
                if (ok) { // Solo registra el mensaje y la bitacora si el cambio fue exitoso
                    request.getSession().setAttribute("mensaje", "Estado del mecanico actualizado."); // Feedback de exito al usuario
                    request.getSession().setAttribute("tipoMensaje", "info"); // Estilo visual informativo
                    Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado"); // Obtiene el admin que realizo la accion
                    if (actor != null) { // Solo registra en bitacora si hay un actor autenticado
                        new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                            actor.getIdEmpleado(), // Documento del admin que cambio el estado
                            actor.getNombre(), // Nombre del admin que realizo la accion
                            "MODIFICAR EMPLEADO", // Tipo de accion para la bitacora
                            "Se cambio el estado del empleado con documento: " + id // Detalle de la accion auditada
                        );
                    }
                }
                System.out.println("ESTADO CAMBIADO: " + ok); // Log de consola para diagnostico del servidor

            } else if ("edit".equals(action)) {
                // Future edit logic
                System.out.println("EDIT_ACTION_RECEIVED"); // Log temporal, la logica de edicion esta en doGet

            } else if ("delete".equals(action)) { // Elimina el mecanico del sistema
                String id = request.getParameter("id"); // Documento del mecanico a eliminar
                EmpleadoDAO dao = new EmpleadoDAO(); // DAO para ejecutar la eliminacion
                boolean ok = dao.eliminar(id); // Intenta eliminar el mecanico de la base de datos
                if (ok) { // Si la eliminacion fue exitosa
                    request.getSession().setAttribute("mensaje", "Mecanico eliminado correctamente."); // Feedback de exito
                    request.getSession().setAttribute("tipoMensaje", "success"); // Estilo visual verde
                    Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado"); // Obtiene el admin que elimino al mecanico
                    if (actor != null) { // Solo registra si hay sesion activa
                        new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                            actor.getIdEmpleado(), // Documento del admin que elimino
                            actor.getNombre(), // Nombre del admin
                            "ELIMINAR EMPLEADO", // Tipo de accion para la bitacora
                            "Se elimino al empleado con documento: " + id // Detalle del empleado eliminado
                        );
                    }
                } else { // Si no se pudo eliminar (probablemente tiene ordenes asociadas)
                    request.getSession().setAttribute("mensaje", "No se pudo eliminar el mecanico. Es posible que tenga ordenes asociadas."); // Error de integridad referencial
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                }
                System.out.println("ELIMINADO: " + ok); // Log de consola para diagnostico

            } else if ("update".equals(action)) {
                // Procedemos a actualizar nuestro mecanico existente
                String doc = request.getParameter("doc_emple"); // Documento del mecanico a actualizar
                String pin = request.getParameter("pin_acceso"); // Nuevo PIN de acceso
                EmpleadoDAO dao = new EmpleadoDAO(); // DAO para ejecutar la actualizacion

                // Verificamos que el documento ingresado sea valido (entre 6 y 10 caracteres) y comprobamos que el PIN no este en uso por otro usuario. Si los datos son correctos, procedemos a actualizar la informacion del mecanico en el sistema.
                if (doc == null || doc.trim().length() < 6 || doc.trim().length() > 10) { // Valida longitud del documento
                    request.getSession().setAttribute("mensaje", "No se guardo porque no es un documento valido."); // Error de validacion
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                } else if (dao.existePin(pin, doc.trim())) { // Verifica que el PIN no este en uso por otro empleado
                    request.getSession().setAttribute("mensaje", "Ese PIN no esta disponible."); // Error de PIN duplicado
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                } else { // Datos validos: procede a actualizar
                    Empleado emp = new Empleado(); // Nuevo objeto con los datos actualizados del formulario
                    emp.setIdEmpleado(doc.trim()); // Documento del mecanico
                    emp.setNombre(request.getParameter("nom_empleado")); // Nombre actualizado del mecanico
                    emp.setPin(pin); // Nuevo PIN de acceso
                    emp.setIdRol(Integer.parseInt(request.getParameter("id_rol_fk"))); // Rol actualizado
                    emp.setIdCargo(Integer.parseInt(request.getParameter("id_cargo_fk"))); // Cargo actualizado

                    boolean ok = dao.actualizar(emp); // Persiste los cambios en la base de datos

                    if (ok) { // Actualizacion exitosa
                        request.getSession().setAttribute("mensaje", "Mecanico actualizado correctamente."); // Feedback de exito
                        request.getSession().setAttribute("tipoMensaje", "success"); // Estilo visual verde
                        Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado"); // Admin que realizo la actualizacion
                        if (actor != null) { // Solo registra si hay sesion activa
                            new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                                actor.getIdEmpleado(), // Documento del admin que actualizo
                                actor.getNombre(), // Nombre del admin
                                "MODIFICAR EMPLEADO", // Tipo de accion para la bitacora
                                "Se actualizaron los datos del empleado: " + emp.getNombre() + " (Doc: " + emp.getIdEmpleado() + ")" // Detalle del cambio
                            );
                        }
                    } else { // Error al actualizar
                        request.getSession().setAttribute("mensaje", "Error al actualizar el mecanico."); // Feedback de error
                        request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                    }
                    System.out.println("ACTUALIZADO: " + ok); // Log de consola para diagnostico
                }

            } else { // Si la accion no es ninguna de las anteriores, se trata de un INSERT de nuevo mecanico
                // Registramos nuestro nuevo mecanico (cuando la accion es "insert" o viene sin accion)
                String doc = request.getParameter("doc_emple"); // Documento del nuevo mecanico
                String pin = request.getParameter("pin_acceso"); // PIN de acceso del nuevo mecanico
                EmpleadoDAO dao = new EmpleadoDAO(); // DAO para insertar el nuevo mecanico

                System.out.println("[MecanicoController] CREAR mecanico | doc='" + doc + "' | pin='" + pin + "'"); // Log de debug en consola

                // Realizamos la validacion: aseguramos que los documentos colombianos validos tengan entre 6 y 10 digitos
                if (doc == null || doc.trim().isEmpty()) { // Documento no puede estar vacio
                    request.getSession().setAttribute("mensaje", "El documento es obligatorio."); // Error de campo vacio
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                } else if (doc.trim().length() < 6 || doc.trim().length() > 10) { // Longitud fuera del rango colombiano
                    request.getSession().setAttribute("mensaje",
                        "Documento invalido. Debe tener entre 6 y 10 digitos. Se ingresaron " + doc.trim().length() + "."); // Error de longitud
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                } else if (pin == null || pin.trim().length() < 4) { // PIN debe tener al menos 4 digitos por seguridad
                    request.getSession().setAttribute("mensaje", "El PIN debe tener al menos 4 digitos."); // Error de PIN corto
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                } else if (dao.existeDocumento(doc.trim())) { // Verifica que el documento no este ya registrado
                    request.getSession().setAttribute("mensaje",
                        "El documento '" + doc.trim() + "' ya esta registrado en el sistema."); // Error de documento duplicado
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                } else if (dao.existePin(pin.trim(), null)) { // Verifica que el PIN no este ya en uso
                    request.getSession().setAttribute("mensaje", "Ese PIN ya esta en uso por otro mecanico. Usa uno diferente."); // Error de PIN duplicado
                    request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                } else { // Todos los datos son validos: procede a insertar el nuevo mecanico
                    Empleado emp = new Empleado(); // Objeto nuevo mecanico llenado con datos del formulario
                    emp.setIdEmpleado(doc.trim()); // Documento del nuevo mecanico
                    emp.setNombre(request.getParameter("nom_empleado")); // Nombre completo del mecanico
                    emp.setPin(pin.trim()); // PIN de acceso al sistema
                    emp.setIdRol(Integer.parseInt(request.getParameter("id_rol_fk"))); // Rol (generalmente mecanico = 2)
                    emp.setIdCargo(Integer.parseInt(request.getParameter("id_cargo_fk"))); // Cargo en el taller

                    // Guardamos en nuestra base de datos y capturamos la excepcion para mostrar el error real de MySQL
                    try {
                        boolean ok = dao.insertar(emp); // Persiste el nuevo mecanico en la base de datos
                        if (ok) { // Insercion exitosa
                            request.getSession().setAttribute("mensaje", "Mecanico registrado exitosamente."); // Feedback de exito
                            request.getSession().setAttribute("tipoMensaje", "success"); // Estilo visual verde
                            Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado"); // Admin que creo el mecanico
                            if (actor != null) { // Solo registra si hay sesion activa
                                new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                                    actor.getIdEmpleado(), // Documento del admin que creo el mecanico
                                    actor.getNombre(), // Nombre del admin
                                    "CREAR EMPLEADO", // Tipo de accion para la bitacora
                                    "Se registro un nuevo empleado: " + emp.getNombre() + " (Doc: " + emp.getIdEmpleado() + ")" // Detalle del nuevo empleado
                                );
                            }
                        } else { // La insercion no afecto ninguna fila
                            request.getSession().setAttribute("mensaje",
                                "No se pudo insertar el mecanico (BD retorno 0 filas afectadas). Verifica la consola del servidor."); // Error de insercion
                            request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                        }
                        System.out.println("[MecanicoController] INSERTADO: " + ok); // Log de consola para diagnostico
                    } catch (Exception dbEx) {
                        // Mostramos el mensaje real de MySQL directamente a nuestro usuario para facilitar el diagnostico
                        String msgBD = dbEx.getMessage() != null ? dbEx.getMessage() : dbEx.getClass().getSimpleName(); // Extrae el mensaje del error
                        System.err.println("[MecanicoController] SQL ERROR: " + msgBD); // Imprime el error SQL en consola de errores
                        dbEx.printStackTrace(); // Muestra el stack trace completo para diagnostico
                        request.getSession().setAttribute("mensaje",
                            "Error de base de datos: " + msgBD); // Muestra el error al usuario
                        request.getSession().setAttribute("tipoMensaje", "error"); // Estilo visual rojo
                    }
                }
            } // fin else (insertar)

        } catch (Exception e) {
            e.printStackTrace(); // Captura cualquier excepcion no controlada y la imprime en consola
        } finally {
            // Siempre nos aseguramos de volver a nuestro panel de mecanicos
            response.sendRedirect(request.getContextPath() + "/MecanicoController#mecanicos"); // Redirige al panel de mecanicos independientemente del resultado
        }
    }
}
