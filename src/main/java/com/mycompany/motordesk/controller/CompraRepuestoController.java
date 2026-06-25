// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller;

// Aquí tenemos la importación de dependencias y clases que necesitamos
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

// Utilizamos esta anotación para definir la ruta de acceso URL para este Servlet
/**
 * Este es nuestro controlador encargado de gestionar los ingresos de repuestos
 * y de permitirnos visualizar el historial de las compras que realizamos a nuestros proveedores.
 */
@WebServlet(name = "CompraRepuestoController", urlPatterns = {"/admin/ingreso", "/admin/historialCompras"})
public class CompraRepuestoController extends HttpServlet {

    private ProveedorDAO proveedorDAO = new ProveedorDAO();
    private ProductoDAO productoDAO = new ProductoDAO();
    private CompraRepuestoDAO compraRepuestoDAO = new CompraRepuestoDAO();

    /**
     * En este método doGet manejamos las peticiones HTTP GET.
     * Dependiendo de la ruta que recibamos, cargamos el formulario de ingreso o nuestro historial de compras.
     * 
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        // Validamos la ruta solicitada para que carguemos los datos correspondientes
        if (path.equals("/admin/ingreso")) {
            // Cargamos nuestras listas de proveedores y productos para el formulario de compra
            List<Proveedor> proveedores = proveedorDAO.listarTodos();
            List<Producto> productos = productoDAO.listarTodos();

            request.setAttribute("proveedores", proveedores);
            request.setAttribute("productos", productos);
            request.getRequestDispatcher("/admin/registrarCompra.jsp").forward(request, response);
        } else if (path.equals("/admin/historialCompras")) {
            // Cargamos y mostramos el historial completo de nuestras compras
            List<CompraRepuesto> compras = compraRepuestoDAO.obtenerHistorialCompras();
            request.setAttribute("compras", compras);
            request.getRequestDispatcher("/admin/historialCompras.jsp").forward(request, response);
        }
    }

    /**
     * Por otro lado, en el método doPost manejamos las peticiones HTTP POST.
     * Es aquí donde procesamos el formulario de registro para nuestras compras de repuestos.
     * 
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        // Procedemos a procesar el formulario de ingreso
        if (path.equals("/admin/ingreso")) {
            // Utilizamos un bloque try para que controlemos excepciones durante la conversión y persistencia
            try {
                // Capturamos los parámetros de nuestro formulario
                String nombreProveedor = request.getParameter("nombreProveedor");
                String nombreProducto = request.getParameter("nombreProducto");
                String tipoVehiculo = request.getParameter("tipoVehiculo");
                String seccion = request.getParameter("seccion");
                String precioVentaStr = request.getParameter("precioVenta");
                String cantidadStr = request.getParameter("cantidad");
                String costoUnitarioStr = request.getParameter("costoUnitario");

                // Validamos que no dejemos ningún campo obligatorio vacío o nulo
                if (nombreProveedor == null || nombreProveedor.trim().isEmpty() ||
                    nombreProducto == null || nombreProducto.trim().isEmpty() ||
                    tipoVehiculo == null || tipoVehiculo.trim().isEmpty() ||
                    seccion == null || seccion.trim().isEmpty() ||
                    precioVentaStr == null || precioVentaStr.trim().isEmpty() ||
                    cantidadStr == null || cantidadStr.trim().isEmpty() ||
                    costoUnitarioStr == null || costoUnitarioStr.trim().isEmpty()) {
                    throw new Exception("Todos los campos son obligatorios.");
                }

                // Limpiamos los posibles símbolos no numéricos de los precios para que evitemos errores
                costoUnitarioStr = costoUnitarioStr.replaceAll("[^\\d.,]", "");
                precioVentaStr = precioVentaStr.replaceAll("[^\\d.,]", "");

                // Convertimos los tipos de datos que vamos a usar
                int cantidad = Integer.parseInt(cantidadStr.trim());
                double costoUnitario = Double.parseDouble(costoUnitarioStr.replace(",", ".").trim());
                double precioVenta = Double.parseDouble(precioVentaStr.replace(",", ".").trim());

                // Obtenemos el ID del proveedor, y si no existe, lo insertamos en nuestra base de datos
                int idProveedor = proveedorDAO.obtenerOInsertarProveedor(nombreProveedor.trim());

                // --- Lógica Híbrida de Producto ---
                nombreProducto = nombreProducto.trim();
                // Buscamos si el producto ya existe en nuestro inventario utilizando su nombre
                Producto productoExistente = productoDAO.obtenerPorNombreExacto(nombreProducto);
                int idRepuesto;

                if (productoExistente != null) {
                    // Si el producto ya existe, actualizamos su precio y categoría para mantenerlo al día
                    idRepuesto = productoExistente.getIdProducto();
                    productoExistente.setPrecioUnitario(precioVenta);
                    productoExistente.setTipoVehiculo(tipoVehiculo);
                    productoExistente.setSeccion(seccion);
                    // Actualizamos el producto en la base de datos
                    productoDAO.actualizar(productoExistente);
                } else {
                    // Si no existe, creamos el producto completamente nuevo
                    Producto nuevoProducto = new Producto();
                    nuevoProducto.setNombreProducto(nombreProducto);
                    nuevoProducto.setPrecioUnitario(precioVenta);
                    nuevoProducto.setStock(0); // Iniciamos en 0, luego nuestra compra sumará la cantidad
                    nuevoProducto.setTipoVehiculo(tipoVehiculo);
                    nuevoProducto.setSeccion(seccion);
                    
                    idRepuesto = productoDAO.insertarDevolviendoId(nuevoProducto);
                    // Validamos la inserción para asegurarnos de que todo fue bien
                    if (idRepuesto == -1) {
                        throw new Exception("Fallo en la base de datos al crear el nuevo producto.");
                    }
                }

                // Calculamos el costo total internamente
                double total = cantidad * costoUnitario;

                // Construimos los objetos que necesitamos para la persistencia
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

                // Registramos la transacción de compra en nuestra base de datos
                String dbError = compraRepuestoDAO.registrarCompra(compra, detalles);

                // Validamos la transacción para confirmar el éxito
                if (dbError == null) {
                    request.setAttribute("mensaje", "Ingreso de producto registrado correctamente. Hemos actualizado el stock.");
                } else {
                    request.setAttribute("mensajeError", "Error BD: " + dbError);
                }

                // Recargamos nuestras listas para volver a renderizar la vista con los datos actualizados
                List<Proveedor> proveedores = proveedorDAO.listarTodos();
                List<Producto> productos = productoDAO.listarTodos();
                request.setAttribute("proveedores", proveedores);
                request.setAttribute("productos", productos);
                request.getRequestDispatcher("/admin/registrarCompra.jsp").forward(request, response);

            } catch (Exception e) {
                // Manejamos cualquier excepción que atrapemos y le informamos al usuario
                e.printStackTrace();
                request.setAttribute("mensajeError", "Datos inválidos: " + e.getMessage());
                doGet(request, response);
            }
        }
    }
}
