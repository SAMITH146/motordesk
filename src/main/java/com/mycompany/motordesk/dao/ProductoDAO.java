// Empezamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.dao;

// Procedemos con la importación de las dependencias y clases que vamos a necesitar
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Presentamos nuestra Clase de Acceso a Datos (DAO) para los Productos y Repuestos.
 * Con ella, gestionamos todo el inventario de la aplicación, incluyendo listado, inserción, actualización y filtrado.
 */
public class ProductoDAO {

    /**
     * En esta sección, nosotros listamos todos los productos que están disponibles en nuestra base de datos.
     * @return Lista completa de objetos Producto.
     */
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        // Definimos la sentencia SQL que vamos a ejecutar en nuestra base de datos
        String sql = "SELECT * FROM producto";
        // Obtenemos la conexión física a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaramos nuestra consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Usamos el objeto ResultSet para almacenar los resultados de nuestra consulta
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Finalmente, retornamos el valor que hemos obtenido
        return lista;
    }

    /**
     * Aquí procedemos a listar los productos aplicando filtros de búsqueda (por vehículo, sección o nombre).
     * @param tipoVehiculo Tipo de vehículo que vamos a filtrar (puede ser null).
     * @param seccion Sección de nuestro inventario (puede ser null).
     * @param busquedaNombre Nombre o parte del nombre que buscaremos (puede ser null).
     * @return Nuestra lista de productos filtrados.
     */
    public List<Producto> listarFiltrados(String tipoVehiculo, String seccion, String busquedaNombre) {
        List<Producto> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM producto WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // Evaluamos primeramente si el parámetro 'tipoVehiculo' no es nulo y tampoco está vacío. Si cumple ambas condiciones, significa que el usuario desea filtrar por este criterio, así que nosotros agregamos la respectiva cláusula AND a nuestra consulta dinámica en el StringBuilder e insertamos el valor a nuestra lista de parámetros.
        if (tipoVehiculo != null && !tipoVehiculo.isEmpty()) {
            sql.append("AND tipo_vehiculo = ? ");
            params.add(tipoVehiculo);
        }
        // Revisamos si el filtro de 'seccion' nos llegó con algún valor válido (que no sea nulo ni esté vacío). En caso de tenerlo, nosotros ampliamos nuestra cláusula WHERE concatenando dinámicamente un AND para la columna sección, y así mismo añadimos el valor de esta sección a nuestra lista de parámetros ordenados.
        if (seccion != null && !seccion.isEmpty()) {
            sql.append("AND seccion = ? ");
            params.add(seccion);
        }
        // Verificamos por último si el usuario proporcionó algún texto para la 'busquedaNombre', descartando los espacios en blanco sobrantes con trim(). Si la cadena tiene contenido, nosotros anexamos un filtro de coincidencia parcial LIKE a nuestro SQL y preparamos el parámetro envolviéndolo en comodines '%' para que encuentre coincidencias intermedias en la base de datos.
        if (busquedaNombre != null && !busquedaNombre.trim().isEmpty()) {
            sql.append("AND nombre LIKE ? ");
            params.add("%" + busquedaNombre.trim() + "%");
        }

        // Obtenemos nuestra conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaramos nuestra consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            // Entramos en nuestro bucle de iteración
            for (int i = 0; i < params.size(); i++) {
                // Comprobamos si el tipo de objeto que estamos recuperando de nuestra lista de parámetros es específicamente una instancia de la clase String. Al confirmar que sí lo es, nosotros realizamos el casteo y usamos setString en nuestro PreparedStatement, asegurando que la asignación de variables de la consulta dinámica se haga en su respectiva posición.
                if (params.get(i) instanceof String) {
                    ps.setString(i + 1, (String) params.get(i));
                }
            }

            // Usamos nuestro ResultSet para almacenar los resultados del query
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornamos el valor que obtuvimos
        return lista;
    }

    /**
     * Mediante este método, obtenemos un producto buscando por su identificador numérico interno.
     * @param id Identificador único de nuestro producto.
     * @return El producto encontrado, o null si no existe.
     */
    public Producto obtenerPorId(int id) {
        Producto p = null;
        // Definimos la sentencia SQL que vamos a ejecutar
        String sql = "SELECT * FROM producto WHERE id_producto = ?";
        // Obtenemos nuestra conexión física a la base de datos
        try (Connection con = Conexion.getConexion();
             // Declaramos nuestra consulta preparada
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            // Creamos nuestro ResultSet para almacenar los resultados
            try (ResultSet rs = ps.executeQuery()) {
                // Verificamos si existe el registro
                if (rs.next()) {
                    p = mapearProducto(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Finalmente, retornamos el producto que obtuvimos
        return p;
    }

    /**
     * Aquí buscamos un producto por su nombre exacto.
     * Es sumamente útil para que validemos duplicados antes de insertar.
     * @param nombre Nombre exacto del producto que vamos a buscar.
     * @return Nuestro producto encontrado, o null si no existe.
     */
    public Producto obtenerPorNombreExacto(String nombre) {
        Producto p = null;
        // Definimos nuestra consulta SQL
        String sql = "SELECT * FROM producto WHERE nombre = ?";
        // Obtenemos la conexión a MySQL
        try (Connection con = Conexion.getConexion();
             // Declaramos nuestra consulta preparada para proteger nuestro sistema
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            // Usamos el ResultSet para almacenar nuestro resultado
            try (ResultSet rs = ps.executeQuery()) {
                // Validamos mediante nuestro ResultSet si logramos localizar exactamente ese nombre de producto en la tabla. Si la base de datos nos retorna un registro, nosotros procedemos enseguida a invocar nuestro método auxiliar de mapeo para construir y retornar dicho objeto Producto con toda la información.
                if (rs.next()) {
                    p = mapearProducto(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornamos el valor que encontramos
        return p;
    }

    /**
     * Con esta función, insertamos un nuevo producto en nuestra base de datos y retornamos su ID generado.
     * @param p Objeto Producto que vamos a guardar.
     * @return ID del producto que hemos generado, o -1 si falla el proceso.
     * @throws SQLException Si ocurre un error en nuestra consulta.
     */
    public int insertarDevolviendoId(Producto p) throws SQLException {
        // Definimos la sentencia SQL
        String sql = "INSERT INTO producto (nombre, precio, stock, categoria, estado, tipo_vehiculo, seccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Obtenemos la conexión física a la BD
        try (Connection con = Conexion.getConexion();
             // Declaramos nuestra consulta para prevenir inyección y solicitamos las claves generadas
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombreProducto());
            ps.setDouble(2, p.getPrecioUnitario());
            ps.setInt(3, p.getStock());
            ps.setString(4, "General");
            ps.setString(5, "Activo");
            ps.setString(6, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : "");
            ps.setString(7, p.getSeccion() != null ? p.getSeccion() : "");
            
            ps.executeUpdate();
            
            // Usamos ResultSet para atrapar el ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                // Validamos si obtuvimos el ID
                if (rs.next()) {
                    // Retornamos el valor obtenido
                    return rs.getInt(1);
                }
            }
        }
        // Retornamos -1 si no logramos obtener el ID
        return -1;
    }

    /**
     * Aquí insertamos un producto y simplemente retornamos si fuimos exitosos o no.
     * @param p Objeto Producto a insertar.
     * @return true si logramos insertarlo, false en caso contrario.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public boolean insertar(Producto p) throws SQLException {
        // Nosotros enviamos 'General' para la categoría original, y llenamos las nuevas columnas
        // Definimos la sentencia SQL
        String sql = "INSERT INTO producto (nombre, precio, stock, categoria, estado, tipo_vehiculo, seccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Obtenemos la conexión
        try (Connection con = Conexion.getConexion();
             // Declaramos la consulta preparada
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProducto());
            ps.setDouble(2, p.getPrecioUnitario());
            ps.setInt(3, p.getStock());
            ps.setString(4, "General");
            ps.setString(5, "Activo");
            ps.setString(6, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : "");
            ps.setString(7, p.getSeccion() != null ? p.getSeccion() : "");
            // Retornamos nuestro resultado de éxito
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * En esta parte, actualizamos la información de un producto existente usando su ID.
     * @param p Producto con los datos que hemos actualizado.
     * @return true si lo actualizamos correctamente, false en caso contrario.
     * @throws SQLException Si ocurre un error.
     */
    public boolean actualizar(Producto p) throws SQLException {
        // Preparamos nuestra sentencia SQL
        String sql = "UPDATE producto SET nombre = ?, precio = ?, stock = ?, tipo_vehiculo = ?, seccion = ? WHERE id_producto = ?";
        // Obtenemos nuestra conexión
        try (Connection con = Conexion.getConexion();
             // Declaramos la consulta
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProducto());
            ps.setDouble(2, p.getPrecioUnitario());
            ps.setInt(3, p.getStock());
            ps.setString(4, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : "");
            ps.setString(5, p.getSeccion() != null ? p.getSeccion() : "");
            ps.setInt(6, p.getIdProducto());
            // Retornamos el éxito de nuestra actualización
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Y aquí eliminamos físicamente un producto de nuestra base de datos (si no tiene relaciones).
     * @param id ID del producto que vamos a eliminar.
     * @return true si lo eliminamos, false si fallamos.
     * @throws SQLException Si el producto ya está en uso (ej. en una orden o compra).
     */
    public boolean eliminar(int id) throws SQLException {
        // Definimos la sentencia de borrado
        String sql = "DELETE FROM producto WHERE id_producto = ?";
        // Obtenemos la conexión
        try (Connection con = Conexion.getConexion();
             // Declaramos nuestra consulta preparada
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            // Retornamos nuestro resultado
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Este es nuestro método auxiliar privado que convierte una fila del ResultSet en un objeto Producto.
     * @param rs El ResultSet posicionado en la fila que estamos leyendo.
     * @return Un objeto Producto completamente mapeado para que lo usemos.
     * @throws SQLException Si alguna columna no existe o hay error de tipo.
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setNombreProducto(rs.getString("nombre"));
        p.setPrecioUnitario(rs.getDouble("precio"));
        p.setStock(rs.getInt("stock"));
        p.setEstado(rs.getString("estado"));
        // Iniciamos el bloque try para controlar nuestras excepciones
        try {
            p.setTipoVehiculo(rs.getString("tipo_vehiculo"));
            p.setSeccion(rs.getString("seccion"));
        } catch (SQLException e) {
            // Ignoramos el error si las columnas aún no existen en nuestra BD durante la transición
        }
        // Retornamos nuestro producto ya ensamblado
        return p;
    }
}
