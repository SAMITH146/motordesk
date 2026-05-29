package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.CompraRepuesto;
import com.mycompany.motordesk.model.DetalleCompra;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraRepuestoDAO {

    public String registrarCompra(CompraRepuesto compra, List<DetalleCompra> detalles) {
        Connection con = null;
        PreparedStatement psCompra = null;
        PreparedStatement psDetalle = null;
        PreparedStatement psStock = null;
        ResultSet rs = null;
        String errorMessage = null;

        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // Iniciar transaccion

            // 1. Insertar la compra
            String sqlCompra = "INSERT INTO comprarepuesto (id_proveedor_fk, fecha_compra, total) VALUES (?, ?, ?)";
            psCompra = con.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS);
            psCompra.setInt(1, compra.getIdProveedorFk());
            psCompra.setDate(2, new java.sql.Date(compra.getFechaCompra().getTime()));
            psCompra.setDouble(3, compra.getTotal());
            psCompra.executeUpdate();

            // Obtener el ID de la compra generada
            rs = psCompra.getGeneratedKeys();
            int idCompraGenerado = 0;
            if (rs.next()) {
                idCompraGenerado = rs.getInt(1);
            } else {
                throw new SQLException("No se pudo obtener el ID de la compra generada.");
            }

            // 2. Insertar los detalles y actualizar el stock
            String sqlDetalle = "INSERT INTO detallecompra (id_compra_fk, id_repuesto_fk, cantidad, costo_unitario) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE producto SET stock = stock + ? WHERE id_producto = ?"; 

            psDetalle = con.prepareStatement(sqlDetalle);
            psStock = con.prepareStatement(sqlStock);

            for (DetalleCompra dt : detalles) {
                // Insertar detalle
                psDetalle.setInt(1, idCompraGenerado);
                psDetalle.setInt(2, dt.getIdRepuestoFk());
                psDetalle.setInt(3, dt.getCantidad());
                psDetalle.setDouble(4, dt.getCostoUnitario());
                psDetalle.addBatch();

                // Actualizar stock
                psStock.setInt(1, dt.getCantidad());
                psStock.setInt(2, dt.getIdRepuestoFk());
                psStock.addBatch();
            }

            // Ejecutar batches
            psDetalle.executeBatch();
            psStock.executeBatch();

            // Confirmar transaccion
            con.commit();

        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            if (con != null) {
                try {
                    con.rollback(); // Revertir en caso de error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (psStock != null) psStock.close();
                if (psDetalle != null) psDetalle.close();
                if (psCompra != null) psCompra.close();
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return errorMessage; // null significa éxito
    }

    public List<CompraRepuesto> obtenerHistorialCompras() {
        List<CompraRepuesto> lista = new ArrayList<>();
        // Hacemos JOIN con proveedor para tener su nombre
        String sql = "SELECT c.id_compra, c.id_proveedor_fk, c.fecha_compra, c.total, p.nombre_proveedor " +
                     "FROM comprarepuesto c " +
                     "LEFT JOIN proveedor p ON c.id_proveedor_fk = p.id_proveedor " +
                     "ORDER BY c.fecha_compra DESC, c.id_compra DESC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CompraRepuesto c = new CompraRepuesto(
                    rs.getInt("id_compra"),
                    rs.getInt("id_proveedor_fk"),
                    rs.getDate("fecha_compra"),
                    rs.getDouble("total")
                );
                c.setNombreProveedor(rs.getString("nombre_proveedor"));
                lista.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
