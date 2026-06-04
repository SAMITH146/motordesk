// Definición del paquete del proyecto
package com.mycompany.motordesk.controller;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.dao.ProductoDAO;
import com.mycompany.motordesk.model.Producto;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Anotación que define la ruta de acceso URL para este Servlet
@WebServlet("/ProductoController")
// Clase pública ProductoController que gestiona la lógica correspondiente
public class ProductoController extends HttpServlet {

    private final ProductoDAO dao = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        // Validación condicional
        if ("edit".equals(action)) {
            // Inicio del bloque try para control de excepciones
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                request.setAttribute("productoEditar", dao.obtenerPorId(id));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        String buscar = request.getParameter("buscar");
        String fVehiculo = request.getParameter("f_vehiculo");
        String fSeccion = request.getParameter("f_seccion");

        List<Producto> lista;
        // Validación condicional
        if ((buscar != null && !buscar.isEmpty()) || 
            (fVehiculo != null && !fVehiculo.isEmpty()) || 
            (fSeccion != null && !fSeccion.isEmpty())) {
            lista = dao.listarFiltrados(fVehiculo, fSeccion, buscar);
        } else {
            lista = dao.listarTodos();
        }

        request.setAttribute("listaProductos", lista);
        
        // Conservar los valores de filtro para mostrarlos en los inputs
        request.setAttribute("filtroBuscar", buscar);
        request.setAttribute("filtroVehiculo", fVehiculo);
        request.setAttribute("filtroSeccion", fSeccion);
        
        request.getRequestDispatcher("/admin/gestionarRepuestos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        // Inicio del bloque try para control de excepciones
        try {
            Producto p = new Producto();
            // Validación condicional
            if (request.getParameter("id") != null && !request.getParameter("id").isEmpty()) {
                p.setIdProducto(Integer.parseInt(request.getParameter("id")));
            }
            
            p.setNombreProducto(request.getParameter("nombre"));
            p.setTipoVehiculo(request.getParameter("tipoVehiculo"));
            p.setSeccion(request.getParameter("seccion"));
            
            String stockParam = request.getParameter("stock");
            String precioParam = request.getParameter("precio");
            
            p.setStock( (stockParam != null && !stockParam.isEmpty()) ? Integer.parseInt(stockParam) : 0 );
            p.setPrecioUnitario( (precioParam != null && !precioParam.isEmpty()) ? Double.parseDouble(precioParam) : 0.0 );

            boolean exito = false;

            // Validación condicional
            if ("delete".equals(action)) {
                exito = dao.eliminar(p.getIdProducto());
                // Validación condicional
                if (exito) {
                    session.setAttribute("mensaje", "Producto eliminado correctamente.");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "Error al eliminar el producto.");
                    session.setAttribute("tipoMensaje", "error");
                }
            } else if (p.getIdProducto() > 0) {
                exito = dao.actualizar(p);
                // Validación condicional
                if (exito) {
                    session.setAttribute("mensaje", "Producto actualizado con éxito.");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "Error al actualizar el producto.");
                    session.setAttribute("tipoMensaje", "error");
                }
            } else {
                exito = dao.insertar(p);
                // Validación condicional
                if (exito) {
                    session.setAttribute("mensaje", "Producto registrado correctamente.");
                    session.setAttribute("tipoMensaje", "success");
                } else {
                    session.setAttribute("mensaje", "Error al registrar el producto. Revise sus datos.");
                    session.setAttribute("tipoMensaje", "error");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("mensaje", "Error técnico: " + e.getMessage());
            session.setAttribute("tipoMensaje", "error");
        }

        response.sendRedirect(request.getContextPath() + "/ProductoController");
    }
}
