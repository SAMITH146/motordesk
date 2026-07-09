// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.controller; // Paquete que agrupa todos los controladores

// Aqui tenemos la importacion de dependencias y clases que necesitamos
import com.mycompany.motordesk.dao.CompraRepuestoDAO; // DAO para registrar y consultar compras de repuestos
import com.mycompany.motordesk.dao.ProductoDAO; // DAO del catalogo de productos/inventario
import com.mycompany.motordesk.dao.ProveedorDAO; // DAO de proveedores de repuestos
import com.mycompany.motordesk.model.CompraRepuesto; // Modelo que representa la cabecera de una compra
import com.mycompany.motordesk.model.DetalleCompra; // Modelo que representa el detalle (linea) de una compra
import com.mycompany.motordesk.model.Producto; // Modelo de producto del inventario
import com.mycompany.motordesk.model.Proveedor; // Modelo de proveedor
import java.io.IOException; // Excepcion para errores de entrada/salida
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.Date; // Para asignar la fecha actual de la compra
import java.util.List; // Interfaz de lista
import javax.servlet.ServletException; // Excepcion especifica de servlets
import javax.servlet.annotation.WebServlet; // Anotacion para mapear el servlet a URLs
import javax.servlet.http.HttpServlet; // Clase base del servlet HTTP
import javax.servlet.http.HttpServletRequest; // Representa la peticion HTTP entrante
import javax.servlet.http.HttpServletResponse; // Representa la respuesta HTTP

// Utilizamos esta anotacion para definir la ruta de acceso URL para este Servlet
/**
 * Este es nuestro controlador encargado de gestionar los ingresos de repuestos
 * y de permitirnos visualizar el historial de las compras que realizamos a nuestros proveedores.
 */
@WebServlet(name = "CompraRepuestoController", urlPatterns = {"/admin/ingreso", "/admin/historialCompras"}) // Mapea el servlet a dos rutas: registro de compra e historial
public class CompraRepuestoController extends HttpServlet { // Controlador de compras e ingresos de inventario

    private ProveedorDAO proveedorDAO = new ProveedorDAO(); // DAO de proveedores para cargar el desplegable del formulario
    private ProductoDAO productoDAO = new ProductoDAO(); // DAO de productos para cargar el catalogo y actualizar stock
    private CompraRepuestoDAO compraRepuestoDAO = new CompraRepuestoDAO(); // DAO principal para registrar y consultar compras

    /**
     * En este metodo doGet manejamos las peticiones HTTP GET.
     * Dependiendo de la ruta que recibamos, cargamos el formulario de ingreso o nuestro historial de compras.
     *
     * @param request La peticion HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath(); // Obtiene la ruta solicitada para decidir que vista cargar

        // Validamos la ruta solicitada para que carguemos los datos correspondientes
        if (path.equals("/admin/ingreso")) { // Si la ruta es /admin/ingreso, muestra el formulario de registro de compra
            // Cargamos nuestras listas de proveedores y productos para el formulario de compra
            List<Proveedor> proveedores = proveedorDAO.listarTodos(); // Lista todos los proveedores registrados
            List<Producto> productos = productoDAO.listarTodos(); // Lista todos los productos del inventario

            request.setAttribute("proveedores", proveedores); // Pasa la lista de proveedores al JSP para el desplegable
            request.setAttribute("productos", productos); // Pasa el catalogo de productos al JSP
            request.getRequestDispatcher("/admin/registrarCompra.jsp").forward(request, response); // Muestra el formulario de ingreso de repuesto
        } else if (path.equals("/admin/historialCompras")) { // Si la ruta es /admin/historialCompras, muestra el historial
            // Cargamos y mostramos el historial completo de nuestras compras
            List<CompraRepuesto> compras = compraRepuestoDAO.obtenerHistorialCompras(); // Obtiene todas las compras realizadas
            request.setAttribute("compras", compras); // Pasa el historial de compras al JSP
            request.getRequestDispatcher("/admin/historialCompras.jsp").forward(request, response); // Renderiza el historial de compras
        }
    }

    /**
     * Por otro lado, en el metodo doPost manejamos las peticiones HTTP POST.
     * Es aqui donde procesamos el formulario de registro para nuestras compras de repuestos.
     *
     * @param request La peticion HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error del Servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath(); // Obtiene la ruta para identificar que formulario fue enviado

        // Procedemos a procesar el formulario de ingreso
        if (path.equals("/admin/ingreso")) { // Solo procesa si el POST viene del formulario de ingreso
            // Utilizamos un bloque try para que controlemos excepciones durante la conversion y persistencia
            try {
                // Capturamos los parametros de nuestro formulario
                String nombreProveedor = request.getParameter("nombreProveedor"); // Nombre del proveedor seleccionado
                String nombreProducto = request.getParameter("nombreProducto"); // Nombre del producto a ingresar
                String tipoVehiculo = request.getParameter("tipoVehiculo"); // Tipo de vehiculo compatible
                String seccion = request.getParameter("seccion"); // Seccion del taller
                String precioVentaStr = request.getParameter("precioVenta"); // Precio de venta del producto
                String cantidadStr = request.getParameter("cantidad"); // Cantidad a ingresar al inventario
                String costoUnitarioStr = request.getParameter("costoUnitario"); // Costo de compra al proveedor

                // Validamos que no dejemos ningun campo obligatorio vacio o nulo
                if (nombreProveedor == null || nombreProveedor.trim().isEmpty() ||
                    nombreProducto == null || nombreProducto.trim().isEmpty() ||
                    tipoVehiculo == null || tipoVehiculo.trim().isEmpty() ||
                    seccion == null || seccion.trim().isEmpty() ||
                    precioVentaStr == null || precioVentaStr.trim().isEmpty() ||
                    cantidadStr == null || cantidadStr.trim().isEmpty() ||
                    costoUnitarioStr == null || costoUnitarioStr.trim().isEmpty()) {
                    throw new Exception("Todos los campos son obligatorios."); // Lanza excepcion para interrumpir el flujo
                }

                // Limpiamos los posibles simbolos no numericos de los precios para que evitemos errores
                costoUnitarioStr = costoUnitarioStr.replaceAll("[^\\d.,]", ""); // Elimina simbolos como $ o espacios del costo
                precioVentaStr = precioVentaStr.replaceAll("[^\\d.,]", ""); // Elimina simbolos como $ o espacios del precio

                // Convertimos los tipos de datos que vamos a usar
                int cantidad = Integer.parseInt(cantidadStr.trim()); // Convierte la cantidad a entero
                double costoUnitario = Double.parseDouble(costoUnitarioStr.replace(",", ".").trim()); // Convierte el costo a double
                double precioVenta = Double.parseDouble(precioVentaStr.replace(",", ".").trim()); // Convierte el precio a double

                // Obtenemos el ID del proveedor, y si no existe, lo insertamos en nuestra base de datos
                int idProveedor = proveedorDAO.obtenerOInsertarProveedor(nombreProveedor.trim()); // Reutiliza o crea el proveedor

                // --- Logica Hibrida de Producto ---
                nombreProducto = nombreProducto.trim(); // Elimina espacios del nombre del producto
                // Buscamos si el producto ya existe en nuestro inventario utilizando su nombre
                Producto productoExistente = productoDAO.obtenerPorNombreExacto(nombreProducto); // Busca el producto por nombre exacto en BD
                int idRepuesto; // ID del producto (existente o nuevo)

                if (productoExistente != null) { // Si el producto ya esta en el inventario, lo actualiza
                    // Si el producto ya existe, actualizamos su precio y categoria para mantenerlo al dia
                    idRepuesto = productoExistente.getIdProducto(); // Usa el ID del producto ya registrado
                    productoExistente.setPrecioUnitario(precioVenta); // Actualiza el precio de venta
                    productoExistente.setTipoVehiculo(tipoVehiculo); // Actualiza el tipo de vehiculo compatible
                    productoExistente.setSeccion(seccion); // Actualiza la seccion del taller
                    // Actualizamos el producto en la base de datos
                    productoDAO.actualizar(productoExistente); // Persiste los cambios en el producto existente
                } else { // Si el producto no existe, lo crea como nuevo
                    // Si no existe, creamos el producto completamente nuevo
                    Producto nuevoProducto = new Producto(); // Nuevo objeto producto
                    nuevoProducto.setNombreProducto(nombreProducto); // Nombre del nuevo producto
                    nuevoProducto.setPrecioUnitario(precioVenta); // Precio de venta del nuevo producto
                    nuevoProducto.setStock(0); // Iniciamos en 0, luego nuestra compra sumara la cantidad
                    nuevoProducto.setTipoVehiculo(tipoVehiculo); // Tipo de vehiculo del nuevo producto
                    nuevoProducto.setSeccion(seccion); // Seccion del taller del nuevo producto

                    idRepuesto = productoDAO.insertarDevolviendoId(nuevoProducto); // Inserta el producto y retorna su ID generado
                    // Validamos la insercion para asegurarnos de que todo fue bien
                    if (idRepuesto == -1) { // Si el ID es -1, la insercion fallo
                        throw new Exception("Fallo en la base de datos al crear el nuevo producto."); // Interrumpe el flujo con error
                    }
                }

                // Calculamos el costo total internamente
                double total = cantidad * costoUnitario; // Total = cantidad comprada x costo unitario al proveedor

                // Construimos los objetos que necesitamos para la persistencia
                CompraRepuesto compra = new CompraRepuesto(); // Cabecera de la compra
                compra.setIdProveedorFk(idProveedor); // Asigna el proveedor a la compra
                compra.setFechaCompra(new Date()); // Fecha actual de la compra
                compra.setTotal(total); // Costo total de esta compra

                DetalleCompra dt = new DetalleCompra(); // Detalle (linea) de la compra
                dt.setIdRepuestoFk(idRepuesto); // Producto comprado
                dt.setCantidad(cantidad); // Cantidad adquirida
                dt.setCostoUnitario(costoUnitario); // Costo unitario pagado al proveedor

                List<DetalleCompra> detalles = new ArrayList<>(); // Lista que contendra los detalles de la compra
                detalles.add(dt); // Agrega el detalle a la lista

                // Registramos la transaccion de compra en nuestra base de datos
                String dbError = compraRepuestoDAO.registrarCompra(compra, detalles); // Persiste la compra y actualiza el stock del producto

                // Validamos la transaccion para confirmar el exito
                if (dbError == null) { // Si no hay error, la compra fue exitosa
                    request.setAttribute("mensaje", "Ingreso de producto registrado correctamente. Hemos actualizado el stock."); // Confirmacion al usuario
                } else {
                    request.setAttribute("mensajeError", "Error BD: " + dbError); // Muestra el error de base de datos
                }

                // Recargamos nuestras listas para volver a renderizar la vista con los datos actualizados
                List<Proveedor> proveedores = proveedorDAO.listarTodos(); // Lista actualizada de proveedores
                List<Producto> productos = productoDAO.listarTodos(); // Lista actualizada del inventario
                request.setAttribute("proveedores", proveedores); // Pasa los proveedores al JSP
                request.setAttribute("productos", productos); // Pasa el inventario actualizado al JSP
                request.getRequestDispatcher("/admin/registrarCompra.jsp").forward(request, response); // Renderiza el formulario con el mensaje de resultado

            } catch (Exception e) {
                // Manejamos cualquier excepcion que atrapemos y le informamos al usuario
                e.printStackTrace(); // Imprime el error en consola para diagnostico
                request.setAttribute("mensajeError", "Datos invalidos: " + e.getMessage()); // Muestra el error al usuario
                doGet(request, response); // Recarga el formulario con los datos actuales
            }
        }
    }
}
