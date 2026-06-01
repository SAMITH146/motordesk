package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.ClienteDAO;
import com.mycompany.motordesk.dao.OrdenDAO;
import com.mycompany.motordesk.dao.VehiculoDAO;
import com.mycompany.motordesk.model.Cliente;
import com.mycompany.motordesk.model.DetalleOrden;
import com.mycompany.motordesk.model.OrdenTrabajo;
import com.mycompany.motordesk.model.Vehiculo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/OrdenController")
public class OrdenController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        OrdenDAO dao = new OrdenDAO();
        String action = request.getParameter("action");
        
        if ("listAll".equals(action) || action == null || action.trim().isEmpty()) {
            request.setAttribute("listaOrdenes", dao.listarTodas());
            request.getRequestDispatcher("/admin/gestionarOrdenes.jsp").forward(request, response);
        } else {
            // Default could be redirecting to home or dashboard
            response.sendRedirect(request.getContextPath() + "/AdminDashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        OrdenDAO dao = new OrdenDAO();
        
        try {
            if ("insert".equals(action)) {
                // 1. Process Cliente
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
                    // Optionally update name/address if changed
                    cliente.setNombre(nomCliente);
                    cliente.setDireccion(dirCliente);
                    clienteDao.actualizar(cliente);
                }

                // 2. Process Vehiculo
                String placa = request.getParameter("placa");
                String marca = request.getParameter("marca");
                String modelo = request.getParameter("modelo");
                int anio = Integer.parseInt(request.getParameter("anio"));
                
                VehiculoDAO vehiculoDao = new VehiculoDAO();
                Vehiculo vehiculo = vehiculoDao.obtenerPorPlaca(placa);
                int idVehiculo;
                if (vehiculo == null) {
                    Vehiculo nuevoV = new Vehiculo(0, idCliente, placa, marca, modelo, anio);
                    idVehiculo = vehiculoDao.insertar(nuevoV);
                } else {
                    idVehiculo = vehiculo.getIdVehiculo();
                    vehiculo.setIdClienteFk(idCliente);
                    vehiculo.setMarca(marca);
                    vehiculo.setModelo(modelo);
                    vehiculo.setAnio(anio);
                    vehiculoDao.actualizar(vehiculo);
                }

                // 3. Register new order from Mechanic
                OrdenTrabajo o = new OrdenTrabajo();
                o.setIdVehiculoFk(idVehiculo);
                o.setPlacaVehiculo(placa);
                o.setDescripcion(request.getParameter("descripcion"));
                o.setDocEmpleFk(request.getParameter("id_mecanico"));
                o.setEstado("ABIERTA");

                // Get products from parameters
                String[] prodIds = request.getParameterValues("productos[]");
                String[] prodsCant = request.getParameterValues("cantidades[]");
                
                List<DetalleOrden> detalles = new ArrayList<>();
                if (prodIds != null) {
                    for (int i = 0; i < prodIds.length; i++) {
                        if (prodIds[i] != null && !prodIds[i].trim().isEmpty()) {
                            try {
                                DetalleOrden d = new DetalleOrden();
                                d.setIdProductoFk(Integer.parseInt(prodIds[i]));
                                String cantStr = (prodsCant != null && prodsCant.length > i && prodsCant[i] != null && !prodsCant[i].trim().isEmpty()) ? prodsCant[i] : "1";
                                d.setCantidad(Integer.parseInt(cantStr));
                                d.setSubtotal(0.0);
                                detalles.add(d);
                            } catch (NumberFormatException e) {
                                // Ignore malformed rows
                            }
                        }
                    }
                }
                
                try {
                    dao.insertarOrden(o, detalles);
                    request.getSession().setAttribute("mensaje", "Orden registrada exitosamente.");
                    request.getSession().setAttribute("tipoMensaje", "success");
                } catch (Exception ex) {
                    request.getSession().setAttribute("mensaje", ex.getMessage());
                    request.getSession().setAttribute("tipoMensaje", "error");
                }
                response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");

            } else if ("update".equals(action)) {
                // Update existing order from Mechanic
                int idOrden = Integer.parseInt(request.getParameter("id_orden"));
                OrdenTrabajo o = dao.obtenerPorId(idOrden);
                if (o != null) {
                    o.setPlacaVehiculo(request.getParameter("placa"));
                    o.setDescripcion(request.getParameter("descripcion"));
                    
                    // Get products from parameters
                    String[] prodIds = request.getParameterValues("productos[]");
                    String[] prodsCant = request.getParameterValues("cantidades[]");
                    
                    List<DetalleOrden> detalles = new ArrayList<>();
                    if (prodIds != null) {
                        for (int i = 0; i < prodIds.length; i++) {
                            if (prodIds[i] != null && !prodIds[i].trim().isEmpty()) {
                                try {
                                    DetalleOrden d = new DetalleOrden();
                                    d.setIdProductoFk(Integer.parseInt(prodIds[i]));
                                    String cantStr = (prodsCant != null && prodsCant.length > i && prodsCant[i] != null && !prodsCant[i].trim().isEmpty()) ? prodsCant[i] : "1";
                                    d.setCantidad(Integer.parseInt(cantStr));
                                    d.setSubtotal(0.0);
                                    detalles.add(d);
                                } catch (NumberFormatException e) {
                                    // Ignore
                                }
                            }
                        }
                    }
                    
                    try {
                        dao.actualizarOrden(o, detalles);
                        request.getSession().setAttribute("mensaje", "Orden actualizada exitosamente.");
                        request.getSession().setAttribute("tipoMensaje", "success");
                    } catch (Exception ex) {
                        request.getSession().setAttribute("mensaje", ex.getMessage());
                        request.getSession().setAttribute("tipoMensaje", "error");
                    }
                }
                response.sendRedirect(request.getContextPath() + "/PanelMecanicoController");

            } else if ("updateStatus".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id_orden"));
                String nuevoEstado = request.getParameter("nuevo_estado");
                String motivo = request.getParameter("motivo");
                String tiempo = request.getParameter("tiempo");
                
                boolean ok = dao.actualizarEstado(id, nuevoEstado, motivo, tiempo);
                if (ok) {
                    request.getSession().setAttribute("mensaje", "Estado de la orden actualizado.");
                    request.getSession().setAttribute("tipoMensaje", "info");
                }
                response.sendRedirect(request.getContextPath() + "/OrdenController?action=listAll");
            } else if ("updateStatusMecanico".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id_orden"));
                String nuevoEstado = request.getParameter("nuevo_estado");
                String motivo = null;
                String tiempo = null;
                if ("ESPERA".equals(nuevoEstado)) {
                    motivo = "En espera de repuestos";
                    tiempo = "Pendiente";
                }
                
                boolean ok = dao.actualizarEstado(id, nuevoEstado, motivo, tiempo);
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
