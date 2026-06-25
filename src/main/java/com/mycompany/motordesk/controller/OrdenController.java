// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller;

// A continuación, importamos las dependencias y clases necesarias
import com.mycompany.motordesk.dao.ClienteDAO;
import com.mycompany.motordesk.dao.OrdenDAO;
import com.mycompany.motordesk.dao.VehiculoDAO;
import com.mycompany.motordesk.model.Cliente;
import com.mycompany.motordesk.model.DetalleOrden;
import com.mycompany.motordesk.model.Empleado;
import com.mycompany.motordesk.model.OrdenTrabajo;
import com.mycompany.motordesk.model.ServicioOrden;
import com.mycompany.motordesk.model.Vehiculo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

// Utilizamos esta anotación para definir la ruta de acceso URL para nuestro Servlet
/**
 * Este es nuestro Controlador encargado de gestionar todo lo relacionado con las Órdenes de Trabajo.
 * Lo consideramos el núcleo de nuestro taller, procesando inserciones, actualizaciones, cambios de estado y visualización de facturas.
 */
@WebServlet("/OrdenController")
public class OrdenController extends HttpServlet {

    /**
     * En nuestro método doGet manejamos las peticiones HTTP GET.
     * Aquí listamos todas nuestras órdenes o visualizamos la factura detallada de una orden en particular.
     * 
     * @param request La petición HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        OrdenDAO dao = new OrdenDAO();
        String action = request.getParameter("action");
        
        // Si nuestra acción es listar todo (o no hay acción), cargamos todas las órdenes y las enviamos a nuestro gestionarOrdenes.jsp
        // Comprobamos si la petición no especifica una acción o si solicita explícitamente listar todas las órdenes. Si es el caso, obtenemos el listado completo y redirigimos a la vista de gestión de órdenes.
        if ("listAll".equals(action) || action == null || action.trim().isEmpty()) {
            request.setAttribute("listaOrdenes", dao.listarTodas());
            request.getRequestDispatcher("/admin/gestionarOrdenes.jsp").forward(request, response);
        } 
        // En nuestra acción de "Ver Factura", cargamos los datos de la orden, cliente, vehículo, repuestos y servicios
        else if ("verFactura".equals(action)) {
            // Iniciamos nuestro bloque try para el control de excepciones
            try {
                // 1. Obtenemos el ID de la orden desde el parámetro de nuestra petición HTTP
                int idOrden = Integer.parseInt(request.getParameter("id_orden"));
                
                // 2. Buscamos la cabecera de nuestra orden de trabajo en la base de datos
                OrdenTrabajo ord = dao.obtenerPorId(idOrden);
                // Verificamos si pudimos encontrar la orden de trabajo en nuestra base de datos. Si existe, procedemos a cargar toda la información asociada, como el vehículo, el cliente, los repuestos y los servicios utilizados.
                if (ord != null) {
                    VehiculoDAO vDao = new VehiculoDAO();
                    ClienteDAO cDao = new ClienteDAO();
                    
                    // 3. Buscamos nuestro Vehículo asociado usando su ID
                    Vehiculo v = vDao.obtenerPorId(ord.getIdVehiculoFk());
                    
                    // 4. Buscamos al Cliente o Dueño asociado usando el ID del cliente registrado en nuestro Vehículo
                    Cliente c = (v != null) ? cDao.obtenerPorId(v.getIdClienteFk()) : null;
                    
                    // 5. Cargamos los repuestos y detalles de los insumos consumidos en nuestra orden
                    List<DetalleOrden> detalles = dao.obtenerDetallesDeOrden(idOrden);

                    // 6. Cargamos los servicios de mano de obra aplicados en esta orden
                    List<com.mycompany.motordesk.model.ServicioOrden> servicios = dao.obtenerServiciosDeOrden(idOrden);
                    
                    // 7. Inyectamos todos nuestros objetos recuperados como atributos del Request para que nuestra vista JSP pueda leerlos
                    request.setAttribute("orden", ord);
                    request.setAttribute("vehiculo", v);
                    request.setAttribute("cliente", c);
                    request.setAttribute("detalles", detalles);
                    request.setAttribute("servicios", servicios);
                    
                    if ("FACTURADO".equals(ord.getEstado())) {
                        com.mycompany.motordesk.model.Factura f = new com.mycompany.motordesk.dao.FacturaDAO().obtenerPorOrden(idOrden);
                        request.setAttribute("facturaRegistrada", f);
                    }
                    
                    // Finalmente, redirigimos nuestra petición de forma interna hacia el JSP de nuestra factura comercial
                    request.getRequestDispatcher("/admin/factura.jsp").forward(request, response);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace(); // Captura y muestra errores en la consola del servidor
            }
            // En caso de error o si no encontramos la orden, redirigimos de vuelta a nuestro listado general
            response.sendRedirect(request.getContextPath() + "/OrdenController?action=listAll");
        } else {
            // Default could be redirecting to home or dashboard
            response.sendRedirect(request.getContextPath() + "/AdminDashboard");
        }
    }

    /**
     * En nuestro método doPost procesamos las peticiones HTTP POST de creación, edición y cambio de estados de órdenes.
     * Aquí recibimos grandes cantidades de parámetros de nuestro formulario (cliente, vehículo, repuestos, servicios).
     * 
     * @param request La petición HTTP que recibimos.
     * @param response La respuesta HTTP que enviaremos.
     * @throws ServletException Si ocurre un error en nuestro Servlet.
     * @throws IOException Si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8"); // Aseguramos la codificación UTF-8 para recibir tildes y caracteres especiales
        String action = request.getParameter("action"); // Leemos la acción de nuestro formulario
        OrdenDAO dao = new OrdenDAO(); // Instanciamos nuestro DAO de órdenes
        
        // Inicio del bloque try para control de excepciones
        try {
            // ================= ACCIÓN: INSERTAR NUEVA ORDEN =================
            // Verificamos si la acción solicitada desde el formulario es la de insertar una nueva orden. Si es así, procedemos a capturar y validar todos los datos del cliente, vehículo y los detalles del servicio.
            if ("insert".equals(action)) {
                // 1. Obtenemos los parámetros del Dueño (Cliente) de nuestro request
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
                
                // Comprobamos si el cliente ya existe en nuestros registros buscando por su documento. Si no lo encontramos, creamos un nuevo registro; de lo contrario, actualizamos sus datos de contacto con la información más reciente.
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

                // 2. Obtenemos los parámetros del Vehículo de nuestro request
                String placa = request.getParameter("placa");
                String marca = request.getParameter("marca");
                String modelo = request.getParameter("modelo");
                int anio = Integer.parseInt(request.getParameter("anio"));
                String tipoVehiculo = request.getParameter("tipo_vehiculo");
                
                VehiculoDAO vehiculoDao = new VehiculoDAO();
                // Buscar si el vehículo ya está registrado en el sistema por su placa
                Vehiculo vehiculo = vehiculoDao.obtenerPorPlaca(placa);
                int idVehiculo;
                
                // Evaluamos si el vehículo ya está registrado en el sistema usando su placa. Si es un vehículo nuevo, lo insertamos asociándolo a su dueño; si ya existe, actualizamos sus características y verificamos su dueño actual.
                if (vehiculo == null) {
                    // Si no existe, registrar el vehículo y asociarlo al ID del cliente/dueño
                    Vehiculo nuevoV = new Vehiculo(0, idCliente, placa, marca, modelo, anio);
                    nuevoV.setTipoVehiculo(tipoVehiculo);
                    idVehiculo = vehiculoDao.insertar(nuevoV); // Guarda y retorna el ID generado
                } else {
                    // Si ya existe, obtener su ID y actualizar sus datos y la llave foránea del cliente
                    idVehiculo = vehiculo.getIdVehiculo();
                    vehiculo.setIdClienteFk(idCliente);
                    vehiculo.setMarca(marca);
                    vehiculo.setModelo(modelo);
                    vehiculo.setAnio(anio);
                    vehiculo.setTipoVehiculo(tipoVehiculo);
                    vehiculoDao.actualizar(vehiculo);
                }

                // 3. Creamos e instanciamos la cabecera de nuestra Orden de Trabajo
                OrdenTrabajo o = new OrdenTrabajo();
                o.setIdVehiculoFk(idVehiculo); // Asociar al vehículo
                o.setPlacaVehiculo(placa); // Asociar placa
                o.setDescripcion(request.getParameter("descripcion")); // Problema reportado
                o.setDocEmpleFk(request.getParameter("id_mecanico")); // Mecánico que la crea
                o.setEstado("ABIERTA"); // Estado inicial obligatorio

                // 4. Leemos los arrays de repuestos o productos que seleccionamos en nuestro formulario
                String[] prodIds = request.getParameterValues("productos[]");
                String[] prodsCant = request.getParameterValues("cantidades[]");
                
                List<DetalleOrden> detalles = new ArrayList<>();
                // Verificamos si el usuario ha seleccionado repuestos o productos en el formulario. Si la lista no es nula, nos preparamos para recorrer cada elemento y agregarlo a los detalles de nuestra orden.
                if (prodIds != null) {
                    // Bucle de iteración
                    for (int i = 0; i < prodIds.length; i++) {
                        // Evitar procesar filas de repuestos vacías
                        // Validamos que el ID del producto en la iteración actual no esté vacío. Si es un ID válido, creamos un nuevo detalle de orden capturando la cantidad solicitada para asociarlo posteriormente a la orden de trabajo.
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

                // 5. Leemos los servicios de mano de obra de nuestro formulario
                String[] serviciosIds = request.getParameterValues("servicios[]");
                String[] serviciosVal = request.getParameterValues("valoresServicio[]");

                List<ServicioOrden> servicios = new ArrayList<>();
                if (serviciosIds != null) {
                    for (int i = 0; i < serviciosIds.length; i++) {
                        if (serviciosIds[i] != null && !serviciosIds[i].trim().isEmpty()) {
                            try {
                                int idServicioFk = Integer.parseInt(serviciosIds[i].trim());
                                ServicioOrden s = new ServicioOrden();
                                s.setIdServicioFk(idServicioFk);
                                // Leer valor; si está vacío o es 0, se registra como 0
                                String valStr = (serviciosVal != null && serviciosVal.length > i
                                                && serviciosVal[i] != null && !serviciosVal[i].trim().isEmpty())
                                               ? serviciosVal[i].trim() : "0";
                                s.setValorCobrado(Double.parseDouble(valStr));
                                servicios.add(s);
                            } catch (NumberFormatException e) {
                                // Ignorar filas con valor no numérico
                            }
                        }
                    }
                }
                
                // Inicio del bloque try para control de excepciones
                try {
                    // 6. Invocamos a nuestro DAO transaccional para crear la orden, registrar los detalles y servicios, y descontar el stock
                    dao.insertarOrden(o, detalles, servicios);
                    request.getSession().setAttribute("mensaje", "Orden registrada exitosamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                    
                    Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado");
                    if (actor != null) {
                        new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                            actor.getIdEmpleado(),
                            actor.getNombre(),
                            "CREAR ORDEN",
                            "Se creó una nueva orden de trabajo para el vehículo con placa: " + o.getPlacaVehiculo()
                        );
                    }
                } catch (Exception ex) {
                    // Capturamos las excepciones (por ejemplo, si hay stock insuficiente) y configuramos la alerta en pantalla
                    request.getSession().setAttribute("mensaje", ex.getMessage());
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
                // Redirigimos al panel principal de nuestro mecánico
                response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");

            } 
            // ================= ACCIÓN: ACTUALIZAR ORDEN EXISTENTE =================
            else if ("update".equals(action)) {
                int idOrden = Integer.parseInt(request.getParameter("id_orden")); // Cargar ID de orden
                OrdenTrabajo o = dao.obtenerPorId(idOrden); // Traer orden de la BD
                // Comprobamos si la orden que deseamos actualizar realmente existe en la base de datos. Si la encontramos, procedemos a procesar las modificaciones realizadas al cliente, vehículo, repuestos y servicios vinculados.
                if (o != null) {
                    // Actualizamos los datos de nuestro cliente
                    String docCliente = request.getParameter("doc_cliente");
                    String nomCliente = request.getParameter("nom_cliente");
                    String dirCliente = request.getParameter("direccion_cliente");
                    
                    ClienteDAO clienteDao = new ClienteDAO();
                    Cliente cliente = clienteDao.obtenerPorDocumento(docCliente);
                    int idCliente;
                    if (cliente == null) {
                        Cliente nuevo = new Cliente(0, nomCliente, docCliente, dirCliente);
                        idCliente = clienteDao.insertar(nuevo);
                    } else {
                        idCliente = cliente.getIdCliente();
                        cliente.setNombre(nomCliente);
                        cliente.setDireccion(dirCliente);
                        clienteDao.actualizar(cliente);
                    }

                    // Actualizamos los datos de nuestro vehículo
                    String placa = request.getParameter("placa");
                    String marca = request.getParameter("marca");
                    String modelo = request.getParameter("modelo");
                    int anio = Integer.parseInt(request.getParameter("anio"));
                    String tipoVehiculo = request.getParameter("tipo_vehiculo");
                    
                    VehiculoDAO vehiculoDao = new VehiculoDAO();
                    Vehiculo vehiculo = vehiculoDao.obtenerPorPlaca(placa);
                    int idVehiculo;
                    if (vehiculo == null) {
                        Vehiculo nuevoV = new Vehiculo(0, idCliente, placa, marca, modelo, anio);
                        nuevoV.setTipoVehiculo(tipoVehiculo);
                        idVehiculo = vehiculoDao.insertar(nuevoV);
                    } else {
                        idVehiculo = vehiculo.getIdVehiculo();
                        vehiculo.setIdClienteFk(idCliente);
                        vehiculo.setMarca(marca);
                        vehiculo.setModelo(modelo);
                        vehiculo.setAnio(anio);
                        vehiculo.setTipoVehiculo(tipoVehiculo);
                        vehiculoDao.actualizar(vehiculo);
                    }

                    o.setIdVehiculoFk(idVehiculo);
                    o.setPlacaVehiculo(placa);
                    o.setDescripcion(request.getParameter("descripcion"));
                    
                    // Leemos los nuevos repuestos asociados en la edición
                    String[] prodIds = request.getParameterValues("productos[]");
                    String[] prodsCant = request.getParameterValues("cantidades[]");
                    
                    List<DetalleOrden> detalles = new ArrayList<>();
                    // Verificamos si el mecánico incluyó nuevos repuestos o modificó los existentes durante la edición de la orden. Si encontramos datos, procedemos a recorrerlos para actualizar los detalles de consumo.
                    if (prodIds != null) {
                        // Bucle de iteración
                        for (int i = 0; i < prodIds.length; i++) {
                            // Evaluamos que la fila actual del producto contenga un ID válido. Si cumple la condición, construimos el detalle del repuesto con su cantidad respectiva para recalcular el inventario y el costo de la orden.
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

                    // Leemos los servicios de mano de obra en la edición
                    String[] serviciosIds = request.getParameterValues("servicios[]");
                    String[] serviciosVal = request.getParameterValues("valoresServicio[]");

                    List<ServicioOrden> servicios = new ArrayList<>();
                    if (serviciosIds != null) {
                        for (int i = 0; i < serviciosIds.length; i++) {
                            if (serviciosIds[i] != null && !serviciosIds[i].trim().isEmpty()) {
                                try {
                                    int idServicioFk = Integer.parseInt(serviciosIds[i].trim());
                                    ServicioOrden s = new ServicioOrden();
                                    s.setIdServicioFk(idServicioFk);
                                    String valStr = (serviciosVal != null && serviciosVal.length > i
                                                    && serviciosVal[i] != null && !serviciosVal[i].trim().isEmpty())
                                                   ? serviciosVal[i].trim() : "0";
                                    s.setValorCobrado(Double.parseDouble(valStr));
                                    servicios.add(s);
                                } catch (NumberFormatException e) {
                                    // Ignorar
                                }
                            }
                        }
                    }
                    
                    // Inicio del bloque try para control de excepciones
                    try {
                        // Actualizamos los datos de nuestra orden (recalculamos el total, devolvemos el stock viejo y restamos el nuevo, reemplazamos los servicios)
                        dao.actualizarOrden(o, detalles, servicios);
                        request.getSession().setAttribute("mensaje", "Orden actualizada exitosamente.");
                        request.getSession().setAttribute("tipoMensaje", "success");
                        
                        Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado");
                        if (actor != null) {
                            new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                                actor.getIdEmpleado(),
                                actor.getNombre(),
                                "MODIFICAR ORDEN",
                                "Se actualizaron los detalles de la orden #" + o.getIdOrden()
                            );
                        }
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
                
                // Actualizamos el estado general de nuestra orden en la base de datos (por ejemplo, de ABIERTA a FACTURADO)
                boolean ok = dao.actualizarEstado(id, nuevoEstado, motivo, tiempo);
                // Verificamos si la actualización del estado de la orden en la base de datos se completó de manera exitosa. Si fue así, preparamos un mensaje de confirmación y, en caso de pasar a estado 'FACTURADO', procedemos a generar automáticamente la factura correspondiente.
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Estado de la orden actualizado.");
                    request.getSession().setAttribute("tipoMensaje", "info");
                    
                    Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado");
                    
                    if ("FACTURADO".equals(nuevoEstado)) {
                        String metodoPago = request.getParameter("metodo_pago");
                        if (metodoPago == null || metodoPago.trim().isEmpty()) {
                            metodoPago = "EFECTIVO";
                        }
                        
                        OrdenTrabajo orden = dao.obtenerPorId(id);
                        if (orden != null) {
                            com.mycompany.motordesk.model.Factura f = new com.mycompany.motordesk.model.Factura();
                            f.setIdOrdenFk(id);
                            if (actor != null) {
                                f.setDocEmpleFk(actor.getIdEmpleado());
                            }
                            f.setTotal(orden.getTotal());
                            f.setIva(orden.getTotal() * 0.19);
                            f.setSubtotal(orden.getTotal() - f.getIva());
                            f.setMetodoPago(metodoPago.toUpperCase());
                            f.setEstado("PAGADA");
                            
                            new com.mycompany.motordesk.dao.FacturaDAO().insertar(f);
                            
                            if (actor != null) {
                                new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                                    actor.getIdEmpleado(),
                                    actor.getNombre(),
                                    "FACTURACION",
                                    "Se generó la factura para la orden de trabajo #" + id + " por un total de $" + orden.getTotal() + " (Método: " + metodoPago + ")"
                                );
                            }
                        }
                    } else {
                        if (actor != null) {
                            new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                                actor.getIdEmpleado(),
                                actor.getNombre(),
                                "MODIFICAR ORDEN",
                                "Se actualizó el estado de la orden #" + id + " a: " + nuevoEstado
                            );
                        }
                    }
                }
                response.sendRedirect(request.getContextPath() + "/OrdenController?action=listAll");
            } 
            // ================= ACCIÓN: ACTUALIZAR ESTADO (MECÁNICO) =================
            else if ("updateStatusMecanico".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id_orden"));
                String nuevoEstado = request.getParameter("nuevo_estado");
                String motivo = null;
                String tiempo = null;
                // Configuramos los detalles por defecto en caso de cambiar al estado 'ESPERA'
                // Evaluamos si el mecánico está cambiando el estado de la orden a 'ESPERA'. Si esta es la situación, asignamos de forma automática el motivo 'En espera de repuestos' para justificar la pausa en el trabajo.
                if ("ESPERA".equals(nuevoEstado)) {
                    motivo = "En espera de repuestos";
                    tiempo = "Pendiente";
                }
                
                // Aplicamos el cambio de estado que solicitó nuestro mecánico en la base de datos
                boolean ok = dao.actualizarEstado(id, nuevoEstado, motivo, tiempo);
                // Comprobamos si el cambio de estado solicitado por el mecánico se guardó correctamente. Si el resultado es positivo, construimos un mensaje de retroalimentación amigable para la interfaz y registramos el movimiento en la bitácora del sistema.
                if (ok) {
                    String msgEstado = "PROCESO".equals(nuevoEstado) ? "en proceso" 
                                     : "ESPERA".equals(nuevoEstado) ? "en espera de repuestos" 
                                     : "TERMINADO".equals(nuevoEstado) ? "terminada" 
                                     : nuevoEstado.toLowerCase();
                    request.getSession().setAttribute("mensaje", "Orden marcada como " + msgEstado + ".");
                    request.getSession().setAttribute("tipoMensaje", "success");
                    
                    Empleado actor = (Empleado) request.getSession().getAttribute("usuarioLogueado");
                    if (actor != null) {
                        new com.mycompany.motordesk.dao.BitacoraDAO().registrarAccion(
                            actor.getIdEmpleado(),
                            actor.getNombre(),
                            "MODIFICAR ORDEN",
                            "Mecánico cambió el estado de la orden #" + id + " a: " + nuevoEstado
                        );
                    }
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
