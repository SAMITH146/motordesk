package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.DetalleOrden;
import com.mycompany.motordesk.model.OrdenTrabajo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdenDAO {

    public void insertarOrden(OrdenTrabajo o, List<DetalleOrden> detalles) throws Exception {
        String sqlOrden = "INSERT INTO ordentrabajo (id_vehiculo_fk, doc_emple_fk, estado, descripcion, total, placa_vehiculo) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        String sqlPrecio = "SELECT nombre, stock, precio FROM producto WHERE id_producto = ?";
        String sqlStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";
        
        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // Transaction start

            // 1. Calculate subtotals and total, validating stock
            double totalOrden = 0.0;
            for (DetalleOrden d : detalles) {
                double precioUnitario = 0.0;
                int stockDisponible = 0;
                String nombreProducto = "";
                
                try (PreparedStatement psP = con.prepareStatement(sqlPrecio)) {
                    psP.setInt(1, d.getIdProductoFk());
                    try (ResultSet rsP = psP.executeQuery()) {
                        if (rsP.next()) {
                            nombreProducto = rsP.getString("nombre");
                            stockDisponible = rsP.getInt("stock");
                            precioUnitario = rsP.getDouble("precio");
                        } else {
                            throw new Exception("Producto no encontrado.");
                        }
                    }
                }

                if (d.getCantidad() > stockDisponible) {
                    throw new Exception("Stock insuficiente para " + nombreProducto + ". Solo hay un stock de " + stockDisponible + " unidades.");
                }
                
                double subtotal = precioUnitario * d.getCantidad();
                d.setSubtotal(subtotal);
                totalOrden += subtotal;
            }
            o.setTotal(totalOrden);

            // 2. Insert Main Order
            try (PreparedStatement psO = con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                if (o.getIdVehiculoFk() > 0) {
                    psO.setInt(1, o.getIdVehiculoFk());
                } else {
                    psO.setNull(1, java.sql.Types.INTEGER);
                }
                psO.setString(2, o.getDocEmpleFk());
                psO.setString(3, "ABIERTA");
                psO.setString(4, o.getDescripcion());
                psO.setDouble(5, o.getTotal());
                psO.setString(6, o.getPlacaVehiculo());
                
                int affected = psO.executeUpdate();
                if (affected == 0) throw new SQLException("Error al crear la orden.");

                // Get generated ID
                try (ResultSet rs = psO.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1);
                        
                        // 3. Insert Details and Update Stock
                        try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                             PreparedStatement psS = con.prepareStatement(sqlStock)) {
                            for (DetalleOrden d : detalles) {
                                // Insert Detail
                                psD.setInt(1, idGenerado);
                                psD.setInt(2, d.getIdProductoFk());
                                psD.setInt(3, d.getCantidad());
                                psD.setDouble(4, d.getSubtotal());
                                psD.addBatch();

                                // Update Stock
                                psS.setInt(1, d.getCantidad());
                                psS.setInt(2, d.getIdProductoFk());
                                psS.addBatch();
                            }
                            psD.executeBatch();
                            psS.executeBatch();
                        }
                    }
                }
            }

            con.commit(); // Transaction success
        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<OrdenTrabajo> listarTodas() {
        List<OrdenTrabajo> lista = new ArrayList<>();
        String sql = "SELECT o.*, e.nom_empleado FROM ordentrabajo o LEFT JOIN empleado e ON o.doc_emple_fk = e.doc_emple ORDER BY o.fecha DESC";
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
        String sql = "SELECT o.*, e.nom_empleado FROM ordentrabajo o LEFT JOIN empleado e ON o.doc_emple_fk = e.doc_emple WHERE o.doc_emple_fk = ? ORDER BY o.fecha DESC";
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

    public boolean actualizarEstado(int id, String nuevoEstado, String motivo, String tiempo) {
        String sql = "UPDATE ordentrabajo SET estado = ?, motivo_espera = ?, tiempo_espera = ? WHERE id_orden = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, "ESPERA".equals(nuevoEstado) ? motivo : null);
            ps.setString(3, "ESPERA".equals(nuevoEstado) ? tiempo : null);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEstado(int id, String nuevoEstado) {
        return actualizarEstado(id, nuevoEstado, null, null);
    }

    public OrdenTrabajo obtenerPorId(int id) {
        String sql = "SELECT o.*, e.nom_empleado FROM ordentrabajo o LEFT JOIN empleado e ON o.doc_emple_fk = e.doc_emple WHERE o.id_orden = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearOrden(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DetalleOrden> obtenerDetallesDeOrden(int idOrden) {
        List<DetalleOrden> lista = new ArrayList<>();
        String sql = "SELECT d.*, p.nombre FROM detalleorden d JOIN producto p ON d.id_repuesto_fk = p.id_producto WHERE d.id_orden_fk = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleOrden d = new DetalleOrden();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setIdOrdenFk(rs.getInt("id_orden_fk"));
                    d.setIdProductoFk(rs.getInt("id_repuesto_fk"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setSubtotal(rs.getDouble("subtotal"));
                    d.setNombreProducto(rs.getString("nombre"));
                    lista.add(d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void actualizarOrden(OrdenTrabajo o, List<DetalleOrden> nuevosDetalles) throws Exception {
        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // Transaction start

            // 1. Get old details to restore stock
            String sqlGetOld = "SELECT id_repuesto_fk, cantidad FROM detalleorden WHERE id_orden_fk = ?";
            List<DetalleOrden> oldDetalles = new ArrayList<>();
            try (PreparedStatement psGetOld = con.prepareStatement(sqlGetOld)) {
                psGetOld.setInt(1, o.getIdOrden());
                try (ResultSet rs = psGetOld.executeQuery()) {
                    while (rs.next()) {
                        DetalleOrden oldD = new DetalleOrden();
                        oldD.setIdProductoFk(rs.getInt("id_repuesto_fk"));
                        oldD.setCantidad(rs.getInt("cantidad"));
                        oldDetalles.add(oldD);
                    }
                }
            }

            // Restore old stock
            String sqlRestoreStock = "UPDATE producto SET stock = stock + ? WHERE id_producto = ?";
            try (PreparedStatement psRestore = con.prepareStatement(sqlRestoreStock)) {
                for (DetalleOrden oldD : oldDetalles) {
                    psRestore.setInt(1, oldD.getCantidad());
                    psRestore.setInt(2, oldD.getIdProductoFk());
                    psRestore.executeUpdate();
                }
            }

            // 2. Validate new stock and calculate total
            double totalOrden = 0.0;
            String sqlPrecio = "SELECT nombre, stock, precio FROM producto WHERE id_producto = ?";
            for (DetalleOrden d : nuevosDetalles) {
                double precioUnitario = 0.0;
                int stockDisponible = 0;
                String nombreProducto = "";
                
                try (PreparedStatement psP = con.prepareStatement(sqlPrecio)) {
                    psP.setInt(1, d.getIdProductoFk());
                    try (ResultSet rsP = psP.executeQuery()) {
                        if (rsP.next()) {
                            nombreProducto = rsP.getString("nombre");
                            stockDisponible = rsP.getInt("stock");
                            precioUnitario = rsP.getDouble("precio");
                        } else {
                            throw new Exception("Producto no encontrado.");
                        }
                    }
                }

                if (d.getCantidad() > stockDisponible) {
                    throw new Exception("Stock insuficiente para " + nombreProducto + ". Solo hay un stock de " + stockDisponible + " unidades.");
                }

                double subtotal = precioUnitario * d.getCantidad();
                d.setSubtotal(subtotal);
                totalOrden += subtotal;
            }
            o.setTotal(totalOrden);

            // 3. Delete old details
            String sqlDeleteDetails = "DELETE FROM detalleorden WHERE id_orden_fk = ?";
            try (PreparedStatement psDel = con.prepareStatement(sqlDeleteDetails)) {
                psDel.setInt(1, o.getIdOrden());
                psDel.executeUpdate();
            }

            // 4. Insert new details and deduct stock
            String sqlDetalle = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)";
            String sqlDeductStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";
            try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                 PreparedStatement psS = con.prepareStatement(sqlDeductStock)) {
                for (DetalleOrden d : nuevosDetalles) {
                    // Insert Detail
                    psD.setInt(1, o.getIdOrden());
                    psD.setInt(2, d.getIdProductoFk());
                    psD.setInt(3, d.getCantidad());
                    psD.setDouble(4, d.getSubtotal());
                    psD.addBatch();

                    // Deduct Stock
                    psS.setInt(1, d.getCantidad());
                    psS.setInt(2, d.getIdProductoFk());
                    psS.addBatch();
                }
                psD.executeBatch();
                psS.executeBatch();
            }

            // 5. Update Main Order
            String sqlUpdateOrder = "UPDATE ordentrabajo SET descripcion = ?, total = ?, placa_vehiculo = ? WHERE id_orden = ?";
            try (PreparedStatement psO = con.prepareStatement(sqlUpdateOrder)) {
                psO.setString(1, o.getDescripcion());
                psO.setDouble(2, o.getTotal());
                psO.setString(3, o.getPlacaVehiculo());
                psO.setInt(4, o.getIdOrden());
                psO.executeUpdate();
            }

            con.commit(); // Commit transaction
        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) {}
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) {}
            }
        }
    }

    private OrdenTrabajo mapearOrden(ResultSet rs) throws SQLException {
        OrdenTrabajo o = new OrdenTrabajo();
        o.setIdOrden(rs.getInt("id_orden"));
        o.setPlacaVehiculo(rs.getString("placa_vehiculo"));
        o.setDescripcion(rs.getString("descripcion"));
        o.setDocEmpleFk(rs.getString("doc_emple_fk"));
        o.setEstado(rs.getString("estado"));
        o.setFecha(rs.getDate("fecha"));
        o.setTotal(rs.getDouble("total"));
        o.setMotivoEspera(rs.getString("motivo_espera"));
        o.setTiempoEspera(rs.getString("tiempo_espera"));
        try {
            o.setNombreMecanico(rs.getString("nom_empleado"));
        } catch (SQLException ignore) {
            // nom_empleado not in result set
        }
        return o;
    }
}
