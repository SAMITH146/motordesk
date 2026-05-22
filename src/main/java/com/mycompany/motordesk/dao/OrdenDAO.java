package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.DetalleOrden;
import com.mycompany.motordesk.model.OrdenTrabajo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdenDAO {

    public boolean insertarOrden(OrdenTrabajo o, List<DetalleOrden> detalles) {
        String sqlOrden = "INSERT INTO ordentrabajo (id_vehiculo_fk, doc_emple_fk, estado, descripcion, total) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalleorden (id_orden_fk, id_producto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        
        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // Transaction start

            // 1. Insert Main Order
            try (PreparedStatement psO = con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                psO.setNull(1, java.sql.Types.INTEGER); // For now NULL as we don't handle vehicle table yet
                psO.setString(2, o.getDocEmpleFk());
                psO.setString(3, "ABIERTA");
                psO.setString(4, o.getDescripcion());
                psO.setDouble(5, 0.0); // Total will be calculated later or via trigger
                
                int affected = psO.executeUpdate();
                if (affected == 0) throw new SQLException("Error al crear la orden.");

                // Get generated ID
                try (ResultSet rs = psO.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1);
                        
                        // 2. Insert Details
                        try (PreparedStatement psD = con.prepareStatement(sqlDetalle)) {
                            for (DetalleOrden d : detalles) {
                                psD.setInt(1, idGenerado);
                                psD.setInt(2, d.getIdProductoFk());
                                psD.setInt(3, d.getCantidad());
                                psD.setDouble(4, d.getSubtotal());
                                psD.addBatch();
                            }
                            psD.executeBatch();
                        }
                    }
                }
            }

            con.commit(); // Transaction success
            return true;
        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<OrdenTrabajo> listarTodas() {
        List<OrdenTrabajo> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordentrabajo ORDER BY fecha DESC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearOrden(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<OrdenTrabajo> listarPorMecanico(String docMecanico) {
        List<OrdenTrabajo> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordentrabajo WHERE doc_emple_fk = ? ORDER BY fecha DESC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docMecanico);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearOrden(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizarEstado(int id, String nuevoEstado) {
        String sql = "UPDATE ordentrabajo SET estado = ? WHERE id_orden = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private OrdenTrabajo mapearOrden(ResultSet rs) throws SQLException {
        OrdenTrabajo o = new OrdenTrabajo();
        o.setIdOrden(rs.getInt("id_orden"));
        // o.setPlacaVehiculo(rs.getString("placa_vehiculo")); // Not in current schema
        o.setDescripcion(rs.getString("descripcion"));
        o.setDocEmpleFk(rs.getString("doc_emple_fk"));
        o.setEstado(rs.getString("estado"));
        o.setFecha(rs.getDate("fecha"));
        return o;
    }
}
