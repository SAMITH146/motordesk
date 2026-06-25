// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.CompraRepuesto;
import com.mycompany.motordesk.model.DetalleCompra;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * En esta sección, tenemos nuestra Clase de Acceso a Datos (DAO) para la Compra de Repuestos.
 * Nosotros gestionamos aquí el registro de nuestras compras a proveedores y su detalle, actualizando nuestro inventario mediante transacciones.
 */
public class CompraRepuestoDAO {

    /**
     * Con esta función, nosotros registramos una compra de repuestos en nuestra base de datos de forma transaccional.
     * Insertamos la compra, insertamos los detalles de nuestra compra y actualizamos el stock de nuestros productos.
     * Si algo falla, nosotros revertimos (rollback) toda la operación para mantener la consistencia de nuestra base de datos.
     * @param compra Objeto CompraRepuesto con la información general de nuestra compra.
     * @param detalles Lista de DetalleCompra con los productos que adquirimos.
     * @return Un mensaje de error si nos ocurre algún fallo, o null si logramos el registro con éxito.
     */
    public String registrarCompra(CompraRepuesto compra, List<DetalleCompra> detalles) {
        Connection con = null;
        // Declaramos nuestra consulta preparada para prevenir inyección SQL
        PreparedStatement psCompra = null;
        // Declaramos nuestra consulta preparada para los detalles
        PreparedStatement psDetalle = null;
        // Declaramos nuestra consulta preparada para actualizar nuestro stock
        PreparedStatement psStock = null;
        // Objeto ResultSet donde nosotros almacenaremos los resultados de nuestra base de datos
        ResultSet rs = null;
        String errorMessage = null;

        // Iniciamos nuestro bloque try para el control de excepciones
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // Iniciamos nuestra transacción

            // 1. Nosotros empezamos insertando la compra
            String sqlCompra = "INSERT INTO comprarepuesto (id_proveedor_fk, fecha_compra, total) VALUES (?, ?, ?)";
            psCompra = con.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS);
            psCompra.setInt(1, compra.getIdProveedorFk());
            psCompra.setDate(2, new java.sql.Date(compra.getFechaCompra().getTime()));
            psCompra.setDouble(3, compra.getTotal());
            psCompra.executeUpdate();

            // Obtenemos el ID de la compra que acabamos de generar
            rs = psCompra.getGeneratedKeys();
            int idCompraGenerado = 0;
            // Verificamos si el ResultSet generó exitosamente las claves (en este caso el ID de nuestra compra). Si es así, procedemos a recuperar ese ID generado extrayendo el valor de la primera columna para asociarlo más adelante a los detalles de la compra. Si no se generó ninguna clave, entonces nosotros lanzamos una excepción porque sin el ID de compra no podemos continuar nuestro registro.
            if (rs.next()) {
                idCompraGenerado = rs.getInt(1);
            } else {
                throw new SQLException("Nosotros no pudimos obtener el ID de la compra generada.");
            }

            // 2. Ahora, nosotros insertamos los detalles y actualizamos nuestro stock
            String sqlDetalle = "INSERT INTO detallecompra (id_compra_fk, id_repuesto_fk, cantidad, costo_unitario) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE producto SET stock = stock + ? WHERE id_producto = ?"; 

            psDetalle = con.prepareStatement(sqlDetalle);
            psStock = con.prepareStatement(sqlStock);

            // Creamos nuestro bucle de iteración
            for (DetalleCompra dt : detalles) {
                // Insertamos nuestro detalle
                psDetalle.setInt(1, idCompraGenerado);
                psDetalle.setInt(2, dt.getIdRepuestoFk());
                psDetalle.setInt(3, dt.getCantidad());
                psDetalle.setDouble(4, dt.getCostoUnitario());
                psDetalle.addBatch();

                // Actualizamos nuestro stock
                psStock.setInt(1, dt.getCantidad());
                psStock.setInt(2, dt.getIdRepuestoFk());
                psStock.addBatch();
            }

            // Ejecutamos nuestros batches
            psDetalle.executeBatch();
            psStock.executeBatch();

            // Confirmamos nuestra transacción
            con.commit();

        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            // Revisamos primeramente si la conexión no es nula. Si no es nula, nosotros sabemos que la transacción se inició y debemos proceder a revertir (rollback) todos los cambios no guardados para así mantener la integridad de nuestra base de datos, garantizando que no queden datos a medias tras el error.
            if (con != null) {
                // Iniciamos nuestro bloque try para el control de excepciones al revertir
                try {
                    con.rollback(); // Nosotros revertimos en caso de error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            // Cerramos nuestros recursos
            // Iniciamos nuestro bloque try para el control de excepciones en el cierre
            try {
                // Comprobamos individualmente si cada uno de nuestros recursos (ResultSet, PreparedStatements y Connection) fue instanciado y utilizado durante nuestra ejecución. De no ser nulos, nosotros invocamos su método close() de manera progresiva para liberar la memoria y evitar las fugas de conexiones hacia nuestro servidor de base de datos.
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
        // Finalmente, nosotros retornamos el valor obtenido
        return errorMessage; // null significa que tuvimos éxito
    }

    /**
     * En este método, nosotros obtenemos el historial de todas las compras que realizamos a nuestros proveedores.
     * @return Nuestra lista de compras ordenadas de la más reciente a la más antigua.
     */
    public List<CompraRepuesto> obtenerHistorialCompras() {
        List<CompraRepuesto> lista = new ArrayList<>();
        // Nosotros hacemos JOIN con proveedor para poder tener su nombre
        // Definimos nuestra sentencia SQL para ejecutarla en la base de datos
        String sql = "SELECT c.id_compra, c.id_proveedor_fk, c.fecha_compra, c.total, p.nombre_proveedor " +
                     "FROM comprarepuesto c " +
                     "LEFT JOIN proveedor p ON c.id_proveedor_fk = p.id_proveedor " +
                     "ORDER BY c.fecha_compra DESC, c.id_compra DESC";
        // Obtenemos la conexión física a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaramos nuestra consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Usamos nuestro ResultSet para almacenar los resultados del query
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
        // Retornamos nuestra lista obtenida
        return lista;
    }
}
