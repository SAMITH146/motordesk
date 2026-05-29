package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Producto> listarFiltrados(String tipoVehiculo, String seccion, String busquedaNombre) {
        List<Producto> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM producto WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (tipoVehiculo != null && !tipoVehiculo.isEmpty()) {
            sql.append("AND tipo_vehiculo = ? ");
            params.add(tipoVehiculo);
        }
        if (seccion != null && !seccion.isEmpty()) {
            sql.append("AND seccion = ? ");
            params.add(seccion);
        }
        if (busquedaNombre != null && !busquedaNombre.trim().isEmpty()) {
            sql.append("AND nombre LIKE ? ");
            params.add("%" + busquedaNombre.trim() + "%");
        }

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof String) {
                    ps.setString(i + 1, (String) params.get(i));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProducto(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Producto obtenerPorId(int id) {
        Producto p = null;
        String sql = "SELECT * FROM producto WHERE id_producto = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = mapearProducto(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    public Producto obtenerPorNombreExacto(String nombre) {
        Producto p = null;
        String sql = "SELECT * FROM producto WHERE nombre = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = mapearProducto(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    public int insertarDevolviendoId(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (nombre, precio, stock, categoria, estado, tipo_vehiculo, seccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombreProducto());
            ps.setDouble(2, p.getPrecioUnitario());
            ps.setInt(3, p.getStock());
            ps.setString(4, "General");
            ps.setString(5, "Activo");
            ps.setString(6, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : "");
            ps.setString(7, p.getSeccion() != null ? p.getSeccion() : "");
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean insertar(Producto p) throws SQLException {
        // Enviamos 'General' para la categoría original, y llenamos las nuevas columnas
        String sql = "INSERT INTO producto (nombre, precio, stock, categoria, estado, tipo_vehiculo, seccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProducto());
            ps.setDouble(2, p.getPrecioUnitario());
            ps.setInt(3, p.getStock());
            ps.setString(4, "General");
            ps.setString(5, "Activo");
            ps.setString(6, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : "");
            ps.setString(7, p.getSeccion() != null ? p.getSeccion() : "");
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Producto p) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, stock = ?, tipo_vehiculo = ?, seccion = ? WHERE id_producto = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProducto());
            ps.setDouble(2, p.getPrecioUnitario());
            ps.setInt(3, p.getStock());
            ps.setString(4, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : "");
            ps.setString(5, p.getSeccion() != null ? p.getSeccion() : "");
            ps.setInt(6, p.getIdProducto());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id_producto = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setNombreProducto(rs.getString("nombre"));
        p.setPrecioUnitario(rs.getDouble("precio"));
        p.setStock(rs.getInt("stock"));
        p.setEstado(rs.getString("estado"));
        try {
            p.setTipoVehiculo(rs.getString("tipo_vehiculo"));
            p.setSeccion(rs.getString("seccion"));
        } catch (SQLException e) {
            // Ignorar si las columnas aún no existen en la BD durante la transición
        }
        return p;
    }
}
