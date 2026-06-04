// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Clase pública ProductoDAO que gestiona la lógica correspondiente
public class ProductoDAO {

    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM producto";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return lista;
    }

    public List<Producto> listarFiltrados(String tipoVehiculo, String seccion, String busquedaNombre) {
        List<Producto> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM producto WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // Validación condicional
        if (tipoVehiculo != null && !tipoVehiculo.isEmpty()) {
            sql.append("AND tipo_vehiculo = ? ");
            params.add(tipoVehiculo);
        }
        // Validación condicional
        if (seccion != null && !seccion.isEmpty()) {
            sql.append("AND seccion = ? ");
            params.add(seccion);
        }
        // Validación condicional
        if (busquedaNombre != null && !busquedaNombre.trim().isEmpty()) {
            sql.append("AND nombre LIKE ? ");
            params.add("%" + busquedaNombre.trim() + "%");
        }

        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            // Bucle de iteración
            for (int i = 0; i < params.size(); i++) {
                // Validación condicional
                if (params.get(i) instanceof String) {
                    ps.setString(i + 1, (String) params.get(i));
                }
            }

            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return lista;
    }

    // Método público 'obtenerPorId'
    public Producto obtenerPorId(int id) {
        Producto p = null;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM producto WHERE id_producto = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) {
                    p = mapearProducto(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return p;
    }

    // Método público 'obtenerPorNombreExacto'
    public Producto obtenerPorNombreExacto(String nombre) {
        Producto p = null;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM producto WHERE nombre = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) {
                    p = mapearProducto(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return p;
    }

    // Método público 'insertarDevolviendoId'
    public int insertarDevolviendoId(Producto p) throws SQLException {
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "INSERT INTO producto (nombre, precio, stock, categoria, estado, tipo_vehiculo, seccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombreProducto());
            ps.setDouble(2, p.getPrecioUnitario());
            ps.setInt(3, p.getStock());
            ps.setString(4, "General");
            ps.setString(5, "Activo");
            ps.setString(6, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : "");
            ps.setString(7, p.getSeccion() != null ? p.getSeccion() : "");
            
            ps.executeUpdate();
            
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.getGeneratedKeys()) {
                // Validación condicional
                if (rs.next()) {
                    // Retornar el valor obtenido
                    return rs.getInt(1);
                }
            }
        }
        // Retornar el valor obtenido
        return -1;
    }

    // Método público 'insertar'
    public boolean insertar(Producto p) throws SQLException {
        // Enviamos 'General' para la categoría original, y llenamos las nuevas columnas
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "INSERT INTO producto (nombre, precio, stock, categoria, estado, tipo_vehiculo, seccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProducto());
            ps.setDouble(2, p.getPrecioUnitario());
            ps.setInt(3, p.getStock());
            ps.setString(4, "General");
            ps.setString(5, "Activo");
            ps.setString(6, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : "");
            ps.setString(7, p.getSeccion() != null ? p.getSeccion() : "");
            // Retornar el valor obtenido
            return ps.executeUpdate() > 0;
        }
    }

    // Método público 'actualizar'
    public boolean actualizar(Producto p) throws SQLException {
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "UPDATE producto SET nombre = ?, precio = ?, stock = ?, tipo_vehiculo = ?, seccion = ? WHERE id_producto = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProducto());
            ps.setDouble(2, p.getPrecioUnitario());
            ps.setInt(3, p.getStock());
            ps.setString(4, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : "");
            ps.setString(5, p.getSeccion() != null ? p.getSeccion() : "");
            ps.setInt(6, p.getIdProducto());
            // Retornar el valor obtenido
            return ps.executeUpdate() > 0;
        }
    }

    // Método público 'eliminar'
    public boolean eliminar(int id) throws SQLException {
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "DELETE FROM producto WHERE id_producto = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            // Retornar el valor obtenido
            return ps.executeUpdate() > 0;
        }
    }

    // Objeto ResultSet para almacenar los resultados del query de base de datos
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setNombreProducto(rs.getString("nombre"));
        p.setPrecioUnitario(rs.getDouble("precio"));
        p.setStock(rs.getInt("stock"));
        p.setEstado(rs.getString("estado"));
        // Inicio del bloque try para control de excepciones
        try {
            p.setTipoVehiculo(rs.getString("tipo_vehiculo"));
            p.setSeccion(rs.getString("seccion"));
        } catch (SQLException e) {
            // Ignorar si las columnas aún no existen en la BD durante la transición
        }
        // Retornar el valor obtenido
        return p;
    }
}
