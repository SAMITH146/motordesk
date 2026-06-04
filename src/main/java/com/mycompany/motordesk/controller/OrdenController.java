// Definición del paquete del proyecto
package com.mycompany.motordesk.controller;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.dao.ClienteDAO;
import com.mycompany.motordesk.dao.OrdenDAO;
import com.mycompany.motordesk.dao.VehiculoDAO;
import com.mycompany.motordesk.model.Cliente;
import com.mycompany.motordesk.model.DetalleOrden;
import com.mycompany.motordesk.model.OrdenTrabajo;
import com.mycompany.motordesk.model.ServicioOrden;
import com.mycompany.motordesk.model.Vehiculo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Anotación que define la ruta de acceso URL para este Servlet
@WebServlet("/OrdenController")
// Clase pública OrdenController que gestiona la lógica correspondiente
public class OrdenController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        OrdenDAO dao = new OrdenDAO();
        String action = request.getParameter("action");
        
        // Si la acción es listar todo (o no hay acción), se cargan todas las órdenes y se envían a gestionarOrdenes.jsp
        // Validación condicional
        if ("listAll".equals(action) || action == null || action.trim().isEmpty()) {
            request.setAttribute("listaOrdenes", dao.listarTodas());
            request.getRequestDispatcher("/admin/gestionarOrdenes.jsp").forward(request, response);
        } 
        // Acción de "Ver Factura": carga los datos de la orden, cliente, vehículo, repuestos y servicios
        else if ("verFactura".equals(action)) {
            // Inicio del bloque try para control de excepciones
            try {
                // 1. Obtener el ID de la orden desde el parámetro de la petición HTTP
                int idOrden = Integer.parseInt(request.getParameter("id_orden"));
                
                // 2. Buscar la cabecera de la orden de trabajo en la BD
                OrdenTrabajo ord = dao.obtenerPorId(idOrden);
                // Validación condicional
                if (ord != null) {
                    VehiculoDAO vDao = new VehiculoDAO();
                    ClienteDAO cDao = new ClienteDAO();
                    
                    // 3. Buscar el Vehículo asociado usando su ID
                    Vehiculo v = vDao.obtenerPorId(ord.getIdVehiculoFk());
                    
                    // 4. Buscar el Cliente/Dueño asociado usando el ID del cliente registrado en el Vehículo
                    Cliente c = (v != null) ? cDao.obtenerPorId(v.getIdClienteFk()) : null;
                    
                    // 5. Cargar los repuestos/detalles de los insumos consumidos en esta orden
                    List<DetalleOrden> detalles = dao.obtenerDetallesDeOrden(idOrden);

                    // 6. Cargar los servicios de mano de obra de esta orden
                    List<com.mycompany.motordesk.model.ServicioOrden> servicios = dao.obtenerServiciosDeOrden(idOrden);
                    
                    // 7. Inyectar todos los objetos recuperados como atributos del Request para que la vista JSP pueda leerlos
                    request.setAttribute("orden", ord);
                    request.setAttribute("vehiculo", v);
                    request.setAttribute("cliente", c);
                    request.setAttribute("detalles", detalles);
                    request.setAttribute("servicios", servicios);
                    
                    // 7. Redirigir la petición de forma interna (Forward) hacia el JSP de la factura comercial
                    request.getRequestDispatcher("/admin/factura.jsp").forward(request, response);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace(); // Captura y muestra errores en la consola del servidor
            }
            // En caso de error o de no encontrar la orden, se redirige de vuelta al listado general
            response.sendRedirect(request.getContextPath() + "/OrdenController?action=listAll");
        } else {
            // Default could be redirecting to home or dashboard
            response.sendRedirect(request.getContextPath() + "/AdminDashboard");
        }
    }

    // Método doPost que procesa las peticiones POST de creación, edición y cambio de estados de órdenes
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8"); // Asegurar codificación UTF-8 para recibir tildes y caracteres especiales
        String action = request.getParameter("action"); // Leer la acción del formulario
        OrdenDAO dao = new OrdenDAO(); // Instanciar el DAO de órdenes
        
        // Inicio del bloque try para control de excepciones
        try {
            // ================= ACCIÓN: INSERTAR NUEVA ORDEN =================
            // Validación condicional
            if ("insert".equals(action)) {
                // 1. Obtener los parámetros del Dueño (Cliente) del request
                String docCliente = request.getParameter("doc_cliente");
                
                // Validar si el cliente ya tiene una orden abierta
                if (dao.tieneOrdenAbiertaPorDocumento(docCliente)) {
                    request.getSession().setAttribute("mensaje", "Error: Este cliente ya tiene una orden ABIERTA. No se puede crear otra orden a su nombre.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                    response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");
                    return;
                }

                String nomCliente = request.getParameter("nom_cliente");
                String dirCliente = request.getParameter("direccion_cliente");
                
                ClienteDAO clienteDao = new ClienteDAO();
                // Buscar si el cliente ya existe en el sistema por su documento
                Cliente cliente = clienteDao.obtenerPorDocumento(docCliente);
                int idCliente;
                
                // Validación condicional
                if (cliente == null) {
                    // Si no existe, crear un nuevo objeto Cliente e insertarlo en la base de datos
                    Cliente nuevo = new Cliente(0, nomCliente, docCliente, dirCliente);
                    idCliente = clienteDao.insertar(nuevo); // Guarda y retorna el ID autogenerado
                } else {
                    // Si existe, obtener su ID y actualizar su nombre o dirección por si cambiaron
                    idCliente = cliente.getIdCliente();
                    cliente.setNombre(nomCliente);
                    cliente.setDireccion(dirCliente);
                    clienteDao.actualizar(cliente);
                }

                // 2. Obtener los parámetros del Vehículo del request
                String placa = request.getParameter("placa");
                String marca = request.getParameter("marca");
                String modelo = request.getParameter("modelo");
                int anio = Integer.parseInt(request.getParameter("anio"));
                
                VehiculoDAO vehiculoDao = new VehiculoDAO();
                // Buscar si el vehículo ya está registrado en el sistema por su placa
                Vehiculo vehiculo = vehiculoDao.obtenerPorPlaca(placa);
                int idVehiculo;
                
                // Validación condicional
                if (vehiculo == null) {
                    // Si no existe, registrar el vehículo y asociarlo al ID del cliente/dueño
                    Vehiculo nuevoV = new Vehiculo(0, idCliente, placa, marca, modelo, anio);
                    idVehiculo = vehiculoDao.insertar(nuevoV); // Guarda y retorna el ID generado
                } else {
                    // Si ya existe, obtener su ID y actualizar sus datos y la llave foránea del cliente
                    idVehiculo = vehiculo.getIdVehiculo();
                    vehiculo.setIdClienteFk(idCliente);
                    vehiculo.setMarca(marca);
                    vehiculo.setModelo(modelo);
                    vehiculo.setAnio(anio);
                    vehiculoDao.actualizar(vehiculo);
                }

                // 3. Crear e instanciar la cabecera de la Orden de Trabajo
                OrdenTrabajo o = new OrdenTrabajo();
                o.setIdVehiculoFk(idVehiculo); // Asociar al vehículo
                o.setPlacaVehiculo(placa); // Asociar placa
                o.setDescripcion(request.getParameter("descripcion")); // Problema reportado
                o.setDocEmpleFk(request.getParameter("id_mecanico")); // Mecánico que la crea
                o.setEstado("ABIERTA"); // Estado inicial obligatorio

                // 4. Leer los arrays de repuestos/productos seleccionados en el formulario
                String[] prodIds = request.getParameterValues("productos[]");
                String[] prodsCant = request.getParameterValues("cantidades[]");
                
                List<DetalleOrden> detalles = new ArrayList<>();
                // Validación condicional
                if (prodIds != null) {
                    // Bucle de iteración
                    for (int i = 0; i < prodIds.length; i++) {
                        // Evitar procesar filas de repuestos vacías
                        // Validación condicional
                        if (prodIds[i] != null && !prodIds[i].trim().isEmpty()) {
                            // Inicio del bloque try para control de excepciones
                            try {
                                DetalleOrden d = new DetalleOrden();
                                d.setIdProductoFk(Integer.parseInt(prodIds[i])); // Asignar id repuesto
                                String cantStr = (prodsCant != null && prodsCant.length > i && prodsCant[i] != null && !prodsCant[i].trim().isEmpty()) ? prodsCant[i] : "1";
                                d.setCantidad(Integer.parseInt(cantStr)); // Asignar cantidad solicitada
                                d.setSubtotal(0.0); // Se calculará de forma segura en la base de datos
                                detalles.add(d); // Agregar a la lista de detalles
                            } catch (NumberFormatException e) {
                                // Ignorar filas mal estructuradas o con datos no numéricos
                            }
                        }
                    }
                }

                // 5. Leer los servicios de mano de obra del formulario
                String[] serviciosNom = request.getParameterValues("servicios[]");
                String[] serviciosVal = request.getParameterValues("valoresServicio[]");

                List<ServicioOrden> servicios = new ArrayList<>();
                if (serviciosNom != null) {
                    for (int i = 0; i < serviciosNom.length; i++) {
                        if (serviciosNom[i] != null && !serviciosNom[i].trim().isEmpty()) {
                            try {
                                ServicioOrden s = new ServicioOrden();
                                s.setNombre(serviciosNom[i].trim());
                                // Leer valor; si está vacío o es 0, se registra como 0
                                String valStr = (serviciosVal != null && serviciosVal.length > i
                                                && serviciosVal[i] != null && !serviciosVal[i].trim().isEmpty())
                                               ? serviciosVal[i].trim() : "0";
                                s.setValor(Double.parseDouble(valStr));
                                servicios.add(s);
                            } catch (NumberFormatException e) {
                                // Ignorar filas con valor no numérico
                            }
                        }
                    }
                }
                
                // Inicio del bloque try para control de excepciones
                try {
                    // 6. Invocar al DAO transaccional para crear la orden, registrar los detalles/servicios y descontar stock
                    dao.insertarOrden(o, detalles, servicios);
                    request.getSession().setAttribute("mensaje", "Orden registrada exitosamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } catch (Exception ex) {
                    // Capturar excepciones (ej: stock insuficiente) y configurar alerta en pantalla
                    request.getSession().setAttribute("mensaje", ex.getMessage());
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
                // Redirigir al panel principal del mecánico
                response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");

            } 
            // ================= ACCIÓN: ACTUALIZAR ORDEN EXISTENTE =================
            else if ("update".equals(action)) {
                int idOrden = Integer.parseInt(request.getParameter("id_orden")); // Cargar ID de orden
                OrdenTrabajo o = dao.obtenerPorId(idOrden); // Traer orden de la BD
                // Validación condicional
                if (o != null) {
                    o.setPlacaVehiculo(request.getParameter("placa"));
                    o.setDescripcion(request.getParameter("descripcion"));
                    
                    // Leer nuevos repuestos asociados en la edición
                    String[] prodIds = request.getParameterValues("productos[]");
                    String[] prodsCant = request.getParameterValues("cantidades[]");
                    
                    List<DetalleOrden> detalles = new ArrayList<>();
                    // Validación condicional
                    if (prodIds != null) {
                        // Bucle de iteración
                        for (int i = 0; i < prodIds.length; i++) {
                            // Validación condicional
                            if (prodIds[i] != null && !prodIds[i].trim().isEmpty()) {
                                // Inicio del bloque try para control de excepciones
                                try {
                                    DetalleOrden d = new DetalleOrden();
                                    d.setIdProductoFk(Integer.parseInt(prodIds[i]));
                                    String cantStr = (prodsCant != null && prodsCant.length > i && prodsCant[i] != null && !prodsCant[i].trim().isEmpty()) ? prodsCant[i] : "1";
                                    d.setCantidad(Integer.parseInt(cantStr));
                                    d.setSubtotal(0.0);
                                    detalles.add(d);
                                } catch (NumberFormatException e) {
                                    // Ignorar
                                }
                            }
                        }
                    }

                    // Leer los servicios de mano de obra en la edición
                    String[] serviciosNom = request.getParameterValues("servicios[]");
                    String[] serviciosVal = request.getParameterValues("valoresServicio[]");

                    List<ServicioOrden> servicios = new ArrayList<>();
                    if (serviciosNom != null) {
                        for (int i = 0; i < serviciosNom.length; i++) {
                            if (serviciosNom[i] != null && !serviciosNom[i].trim().isEmpty()) {
                                try {
                                    ServicioOrden s = new ServicioOrden();
                                    s.setNombre(serviciosNom[i].trim());
                                    String valStr = (serviciosVal != null && serviciosVal.length > i
                                                    && serviciosVal[i] != null && !serviciosVal[i].trim().isEmpty())
                                                   ? serviciosVal[i].trim() : "0";
                                    s.setValor(Double.parseDouble(valStr));
                                    servicios.add(s);
                                } catch (NumberFormatException e) {
                                    // Ignorar
                                }
                            }
                        }
                    }
                    
                    // Inicio del bloque try para control de excepciones
                    try {
                        // Actualizar datos de la orden (recalcular total, devolver stock viejo y restar nuevo, reemplazar servicios)
                        dao.actualizarOrden(o, detalles, servicios);
                        request.getSession().setAttribute("mensaje", "Orden actualizada exitosamente.");
                        request.getSession().setAttribute("tipoMensaje", "success");
                    } catch (Exception ex) {
                        request.getSession().setAttribute("mensaje", ex.getMessage());
                        request.getSession().setAttribute("tipoMensaje", "error");
                    }
                }
                response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");

            } 
            // ================= ACCIÓN: ACTUALIZAR ESTADO (ADMINISTRADOR) =================
            else if ("updateStatus".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id_orden"));
                String nuevoEstado = request.getParameter("nuevo_estado");
                String motivo = request.getParameter("motivo");
                String tiempo = request.getParameter("tiempo");
                
                // Actualizar estado general de la orden en la BD (ej. ABIERTA -> FACTURADO)
                boolean ok = dao.actualizarEstado(id, nuevoEstado, motivo, tiempo);
                // Validación condicional
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Estado de la orden actualizado.");
                    request.getSession().setAttribute("tipoMensaje", "info");
                }
                response.sendRedirect(request.getContextPath() + "/OrdenController?action=listAll");
            } 
            // ================= ACCIÓN: ACTUALIZAR ESTADO (MECÁNICO) =================
            else if ("updateStatusMecanico".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id_orden"));
                String nuevoEstado = request.getParameter("nuevo_estado");
                String motivo = null;
                String tiempo = null;
                // Configurar detalles por defecto en caso de cambiar a estado 'ESPERA'
                // Validación condicional
                if ("ESPERA".equals(nuevoEstado)) {
                    motivo = "En espera de repuestos";
                    tiempo = "Pendiente";
                }
                
                // Aplicar el cambio de estado solicitado por el mecánico en la BD
                boolean ok = dao.actualizarEstado(id, nuevoEstado, motivo, tiempo);
                // Validación condicional
                if (ok) {
                    String msgEstado = "PROCESO".equals(nuevoEstado) ? "en proceso" 
                                     : "ESPERA".equals(nuevoEstado) ? "en espera de repuestos" 
                                     : "TERMINADO".equals(nuevoEstado) ? "terminada" 
                                     : nuevoEstado.toLowerCase();
                    request.getSession().setAttribute("mensaje", "Orden marcada como " + msgEstado + ".");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } else {
                    request.getSession().setAttribute("mensaje", "Error al actualizar el estado de la orden.");
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
                response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/AdminDashboard");
        }
    }
}
