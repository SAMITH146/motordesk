package com.mycompany.motordesk.controller;

import com.mycompany.motordesk.dao.CompraRepuestoDAO;
import com.mycompany.motordesk.dao.ProductoDAO;
import com.mycompany.motordesk.dao.ProveedorDAO;
import com.mycompany.motordesk.model.CompraRepuesto;
import com.mycompany.motordesk.model.DetalleCompra;
import com.mycompany.motordesk.model.Producto;
import com.mycompany.motordesk.model.Proveedor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "CompraRepuestoController", urlPatterns = {"/admin/ingreso", "/admin/historialCompras"})
public class CompraRepuestoController extends HttpServlet {

    private ProveedorDAO proveedorDAO = new ProveedorDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private CompraRepuestoDAO compraRepuestoDAO = new CompraRepuestoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.equals("/admin/ingreso")) {
            // Cargar listas para el formulario de ingreso
            List<Proveedor> proveedores = proveedorDAO.listarTodos();
            List<Producto> productos = productoDAO.listarTodos();

            request.setAttribute("proveedores", proveedores);
            request.setAttribute("productos", productos);
            request.getRequestDispatcher("/admin/registrarCompra.jsp").forward(request, response);
        } else if (path.equals("/admin/historialCompras")) {
            // Cargar historial de compras
            List<CompraRepuesto> compras = compraRepuestoDAO.obtenerHistorialCompras();
            request.setAttribute("compras", compras);
            request.getRequestDispatcher("/admin/historialCompras.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.equals("/admin/ingreso")) {
            try {
                String nombreProveedor = request.getParameter("nombreProveedor");
                String nombreProducto = request.getParameter("nombreProducto");
                String tipoVehiculo = request.getParameter("tipoVehiculo");
                String seccion = request.getParameter("seccion");
                String precioVentaStr = request.getParameter("precioVenta");
                String cantidadStr = request.getParameter("cantidad");
                String costoUnitarioStr = request.getParameter("costoUnitario");

                if (nombreProveedor == null || nombreProveedor.trim().isEmpty() ||
                    nombreProducto == null || nombreProducto.trim().isEmpty() ||
                    tipoVehiculo == null || tipoVehiculo.trim().isEmpty() ||
                    seccion == null || seccion.trim().isEmpty() ||
                    precioVentaStr == null || precioVentaStr.trim().isEmpty() ||
                    cantidadStr == null || cantidadStr.trim().isEmpty() ||
                    costoUnitarioStr == null || costoUnitarioStr.trim().isEmpty()) {
                    throw new Exception("Todos los campos son obligatorios.");
                }

                // Limpiar posibles símbolos
                costoUnitarioStr = costoUnitarioStr.replaceAll("[^\\d.,]", "");
                precioVentaStr = precioVentaStr.replaceAll("[^\\d.,]", "");

                int cantidad = Integer.parseInt(cantidadStr.trim());
                double costoUnitario = Double.parseDouble(costoUnitarioStr.replace(",", ".").trim());
                double precioVenta = Double.parseDouble(precioVentaStr.replace(",", ".").trim());

                int idProveedor = proveedorDAO.obtenerOInsertarProveedor(nombreProveedor.trim());

                // Lógica Híbrida de Producto
                nombreProducto = nombreProducto.trim();
                Producto productoExistente = productoDAO.obtenerPorNombreExacto(nombreProducto);
                int idRepuesto;

                if (productoExistente != null) {
                    // El producto ya existe, actualizamos su precio y categoría para mantenerlo al día
                    idRepuesto = productoExistente.getIdProducto();
                    productoExistente.setPrecioUnitario(precioVenta);
                    productoExistente.setTipoVehiculo(tipoVehiculo);
                    productoExistente.setSeccion(seccion);
                    // Actualizamos (el stock no hace falta porque lo sube CompraRepuestoDAO después)
                    productoDAO.actualizar(productoExistente);
                } else {
                    // Creamos el producto completamente nuevo
                    Producto nuevoProducto = new Producto();
                    nuevoProducto.setNombreProducto(nombreProducto);
                    nuevoProducto.setPrecioUnitario(precioVenta);
                    nuevoProducto.setStock(0); // Inicia en 0, la compra sumará la cantidad
                    nuevoProducto.setTipoVehiculo(tipoVehiculo);
                    nuevoProducto.setSeccion(seccion);
                    
                    idRepuesto = productoDAO.insertarDevolviendoId(nuevoProducto);
                    if (idRepuesto == -1) {
                        throw new Exception("Fallo en la base de datos al crear el nuevo producto.");
                    }
                }

                // Calculamos el total internamente
                double total = cantidad * costoUnitario;

                CompraRepuesto compra = new CompraRepuesto();
                compra.setIdProveedorFk(idProveedor);
                compra.setFechaCompra(new Date());// Fecha actual
                compra.setTotal(total);

                DetalleCompra dt = new DetalleCompra();
                dt.setIdRepuestoFk(idRepuesto);
                dt.setCantidad(cantidad);
                dt.setCostoUnitario(costoUnitario);

                List<DetalleCompra> detalles = new ArrayList<>();
                detalles.add(dt);

                String dbError = compraRepuestoDAO.registrarCompra(compra, detalles);

                if (dbError == null) {
                    request.setAttribute("mensaje", "Ingreso de producto registrado correctamente. El stock ha sido actualizado.");
                } else {
                    request.setAttribute("mensajeError", "Error BD: " + dbError);
                }

                // Recargar página con datos
                List<Proveedor> proveedores = proveedorDAO.listarTodos();
                List<Producto> productos = productoDAO.listarTodos();
                request.setAttribute("proveedores", proveedores);
                request.setAttribute("productos", productos);
                request.getRequestDispatcher("/admin/registrarCompra.jsp").forward(request, response);

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("mensajeError", "Datos inválidos: " + e.getMessage());
                doGet(request, response);
            }
        }
    }
}
