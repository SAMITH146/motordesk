// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.CompraRepuesto;
import com.mycompany.motordesk.model.DetalleCompra;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Clase pública CompraRepuestoDAO que gestiona la lógica correspondiente
public class CompraRepuestoDAO {

    // Método público 'registrarCompra'
    public String registrarCompra(CompraRepuesto compra, List<DetalleCompra> detalles) {
        Connection con = null;
        // Declaración de consulta preparada para prevenir inyección SQL
        PreparedStatement psCompra = null;
        // Declaración de consulta preparada para prevenir inyección SQL
        PreparedStatement psDetalle = null;
        // Declaración de consulta preparada para prevenir inyección SQL
        PreparedStatement psStock = null;
        // Objeto ResultSet para almacenar los resultados del query de base de datos
        ResultSet rs = null;
        String errorMessage = null;

        // Inicio del bloque try para control de excepciones
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
            // Validación condicional
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

            // Bucle de iteración
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
            // Validación condicional
            if (con != null) {
                // Inicio del bloque try para control de excepciones
                try {
                    con.rollback(); // Revertir en caso de error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // Cerrar recursos
            // Inicio del bloque try para control de excepciones
            try {
                // Validación condicional
                if (rs != null) rs.close();
                // Validación condicional
                if (psStock != null) psStock.close();
                // Validación condicional
                if (psDetalle != null) psDetalle.close();
                // Validación condicional
                if (psCompra != null) psCompra.close();
                // Validación condicional
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // Retornar el valor obtenido
        return errorMessage; // null significa éxito
    }

    public List<CompraRepuesto> obtenerHistorialCompras() {
        List<CompraRepuesto> lista = new ArrayList<>();
        // Hacemos JOIN con proveedor para tener su nombre
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT c.id_compra, c.id_proveedor_fk, c.fecha_compra, c.total, p.nombre_proveedor " +
                     "FROM comprarepuesto c " +
                     "LEFT JOIN proveedor p ON c.id_proveedor_fk = p.id_proveedor " +
                     "ORDER BY c.fecha_compra DESC, c.id_compra DESC";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
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
        // Retornar el valor obtenido
        return lista;
    }
}
