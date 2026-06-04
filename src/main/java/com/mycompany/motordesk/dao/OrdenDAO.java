// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.DetalleOrden;
import com.mycompany.motordesk.model.OrdenTrabajo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Clase pública OrdenDAO que gestiona la lógica correspondiente
public class OrdenDAO {

    // Método transaccional para registrar una nueva orden de trabajo con sus detalles y actualizar inventario
    // Método público 'insertarOrden'
    public void insertarOrden(OrdenTrabajo o, List<DetalleOrden> detalles) throws Exception {
        // Consultas preparadas requeridas para el flujo transaccional
        String sqlOrden = "INSERT INTO ordentrabajo (id_vehiculo_fk, doc_emple_fk, estado, descripcion, total, placa_vehiculo) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        String sqlPrecio = "SELECT nombre, stock, precio FROM producto WHERE id_producto = ?";
        String sqlStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";
        
        Connection con = null;
        // Inicio del bloque try para control de excepciones
        try {
            con = Conexion.getConexion();
            // 🔐 A. Iniciamos la transacción desactivando el AutoCommit
            con.setAutoCommit(false); 

            // 1. Validar stock disponible y calcular el subtotal y total acumulado
            double totalOrden = 0.0;
            // Bucle de iteración
            for (DetalleOrden d : detalles) {
                double precioUnitario = 0.0;
                int stockDisponible = 0;
                String nombreProducto = "";
                
                // Obtener datos del producto actual desde la base de datos
                // Declaración de consulta preparada para prevenir inyección SQL
                try (PreparedStatement psP = con.prepareStatement(sqlPrecio)) {
                    psP.setInt(1, d.getIdProductoFk());
                    // Objeto ResultSet para almacenar los resultados del query de base de datos
                    try (ResultSet rsP = psP.executeQuery()) {
                        // Validación condicional
                        if (rsP.next()) {
                            nombreProducto = rsP.getString("nombre");
                            stockDisponible = rsP.getInt("stock");
                            precioUnitario = rsP.getDouble("precio");
                        } else {
                            throw new Exception("Producto no encontrado.");
                        }
                    }
                }

                // Si la cantidad solicitada supera el stock disponible, se cancela y se genera excepción
                // Validación condicional
                if (d.getCantidad() > stockDisponible) {
                    throw new Exception("Stock insuficiente para " + nombreProducto + ". Solo hay un stock de " + stockDisponible + " unidades.");
                }
                
                // Calcular subtotal (precio * cantidad) e ir sumando al total general de la orden
                double subtotal = precioUnitario * d.getCantidad();
                d.setSubtotal(subtotal);
                totalOrden += subtotal;
            }
            o.setTotal(totalOrden); // Asignar el total final calculado a la orden de trabajo

            // 2. Insertar el registro principal en la tabla ordentrabajo
            // Declaración de consulta preparada para prevenir inyección SQL
            try (PreparedStatement psO = con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                // Validación condicional
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
                // Validación condicional
                if (affected == 0) throw new SQLException("Error al crear la orden.");

                // Recuperar la clave primaria autoincremental (id_orden) autogenerada por MySQL
                // Objeto ResultSet para almacenar los resultados del query de base de datos
                try (ResultSet rs = psO.getGeneratedKeys()) {
                    // Validación condicional
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1);
                        
                        // 3. Insertar los ítems de repuestos y descontar inventario en lote (Batch)
                        // Declaración de consulta preparada para prevenir inyección SQL
                        try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                             // Declaración de consulta preparada para prevenir inyección SQL
                             PreparedStatement psS = con.prepareStatement(sqlStock)) {
                            // Bucle de iteración
                            for (DetalleOrden d : detalles) {
                                // Mapear e insertar el detalle del ítem
                                psD.setInt(1, idGenerado);
                                psD.setInt(2, d.getIdProductoFk());
                                psD.setInt(3, d.getCantidad());
                                psD.setDouble(4, d.getSubtotal());
                                psD.addBatch(); // Agregar al lote

                                // Mapear y descontar stock del inventario
                                psS.setInt(1, d.getCantidad());
                                psS.setInt(2, d.getIdProductoFk());
                                psS.addBatch(); // Agregar al lote
                            }
                            psD.executeBatch(); // Ejecutar el lote de detalles
                            psS.executeBatch(); // Ejecutar el lote de actualización de stock
                        }
                    }
                }
            }

            // 🔐 B. Confirmar transacción de forma exitosa
            con.commit(); 
        } catch (Exception e) {
            // 🔐 C. Si ocurre algún error en cualquier paso, revertir todos los cambios (Rollback)
            // Validación condicional
            if (con != null) {
                // Inicio del bloque try para control de excepciones
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e; // Lanzar la excepción original para que sea controlada por el Servlet
        } finally {
            // D. Restaurar modo AutoCommit y cerrar la conexión
            // Validación condicional
            if (con != null) {
                // Inicio del bloque try para control de excepciones
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<OrdenTrabajo> listarTodas() {
        List<OrdenTrabajo> lista = new ArrayList<>();
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT o.*, e.nom_empleado FROM ordentrabajo o LEFT JOIN empleado e ON o.doc_emple_fk = e.doc_emple ORDER BY o.fecha DESC";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearOrden(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return lista;
    }

    public List<OrdenTrabajo> listarPorMecanico(String docMecanico) {
        List<OrdenTrabajo> lista = new ArrayList<>();
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT o.*, e.nom_empleado FROM ordentrabajo o LEFT JOIN empleado e ON o.doc_emple_fk = e.doc_emple WHERE o.doc_emple_fk = ? ORDER BY o.fecha DESC";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docMecanico);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearOrden(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return lista;
    }

    // Método público 'actualizarEstado'
    public boolean actualizarEstado(int id, String nuevoEstado, String motivo, String tiempo) {
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "UPDATE ordentrabajo SET estado = ?, motivo_espera = ?, tiempo_espera = ? WHERE id_orden = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, "ESPERA".equals(nuevoEstado) ? motivo : null);
            ps.setString(3, "ESPERA".equals(nuevoEstado) ? tiempo : null);
            ps.setInt(4, id);
            // Retornar el valor obtenido
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Retornar el valor obtenido
            return false;
        }
    }

    // Método público 'actualizarEstado'
    public boolean actualizarEstado(int id, String nuevoEstado) {
        // Retornar el valor obtenido
        return actualizarEstado(id, nuevoEstado, null, null);
    }

    // Método público 'obtenerPorId'
    public OrdenTrabajo obtenerPorId(int id) {
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT o.*, e.nom_empleado FROM ordentrabajo o LEFT JOIN empleado e ON o.doc_emple_fk = e.doc_emple WHERE o.id_orden = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) {
                    // Retornar el valor obtenido
                    return mapearOrden(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return null;
    }

    public List<DetalleOrden> obtenerDetallesDeOrden(int idOrden) {
        List<DetalleOrden> lista = new ArrayList<>();
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT d.*, p.nombre FROM detalleorden d JOIN producto p ON d.id_repuesto_fk = p.id_producto WHERE d.id_orden_fk = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
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
        // Retornar el valor obtenido
        return lista;
    }

    // Método público 'actualizarOrden'
    public void actualizarOrden(OrdenTrabajo o, List<DetalleOrden> nuevosDetalles) throws Exception {
        Connection con = null;
        // Inicio del bloque try para control de excepciones
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // Transaction start

            // 1. Get old details to restore stock
            String sqlGetOld = "SELECT id_repuesto_fk, cantidad FROM detalleorden WHERE id_orden_fk = ?";
            List<DetalleOrden> oldDetalles = new ArrayList<>();
            // Declaración de consulta preparada para prevenir inyección SQL
            try (PreparedStatement psGetOld = con.prepareStatement(sqlGetOld)) {
                psGetOld.setInt(1, o.getIdOrden());
                // Objeto ResultSet para almacenar los resultados del query de base de datos
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
            // Declaración de consulta preparada para prevenir inyección SQL
            try (PreparedStatement psRestore = con.prepareStatement(sqlRestoreStock)) {
                // Bucle de iteración
                for (DetalleOrden oldD : oldDetalles) {
                    psRestore.setInt(1, oldD.getCantidad());
                    psRestore.setInt(2, oldD.getIdProductoFk());
                    psRestore.executeUpdate();
                }
            }

            // 2. Validate new stock and calculate total
            double totalOrden = 0.0;
            String sqlPrecio = "SELECT nombre, stock, precio FROM producto WHERE id_producto = ?";
            // Bucle de iteración
            for (DetalleOrden d : nuevosDetalles) {
                double precioUnitario = 0.0;
                int stockDisponible = 0;
                String nombreProducto = "";
                
                // Declaración de consulta preparada para prevenir inyección SQL
                try (PreparedStatement psP = con.prepareStatement(sqlPrecio)) {
                    psP.setInt(1, d.getIdProductoFk());
                    // Objeto ResultSet para almacenar los resultados del query de base de datos
                    try (ResultSet rsP = psP.executeQuery()) {
                        // Validación condicional
                        if (rsP.next()) {
                            nombreProducto = rsP.getString("nombre");
                            stockDisponible = rsP.getInt("stock");
                            precioUnitario = rsP.getDouble("precio");
                        } else {
                            throw new Exception("Producto no encontrado.");
                        }
                    }
                }

                // Validación condicional
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
            // Declaración de consulta preparada para prevenir inyección SQL
            try (PreparedStatement psDel = con.prepareStatement(sqlDeleteDetails)) {
                psDel.setInt(1, o.getIdOrden());
                psDel.executeUpdate();
            }

            // 4. Insert new details and deduct stock
            String sqlDetalle = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)";
            String sqlDeductStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";
            // Declaración de consulta preparada para prevenir inyección SQL
            try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                 // Declaración de consulta preparada para prevenir inyección SQL
                 PreparedStatement psS = con.prepareStatement(sqlDeductStock)) {
                // Bucle de iteración
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
            // Declaración de consulta preparada para prevenir inyección SQL
            try (PreparedStatement psO = con.prepareStatement(sqlUpdateOrder)) {
                psO.setString(1, o.getDescripcion());
                psO.setDouble(2, o.getTotal());
                psO.setString(3, o.getPlacaVehiculo());
                psO.setInt(4, o.getIdOrden());
                psO.executeUpdate();
            }

            con.commit(); // Commit transaction
        } catch (Exception e) {
            // Validación condicional
            if (con != null) {
                // Inicio del bloque try para control de excepciones
                try { con.rollback(); } catch (SQLException ex) {}
            }
            throw e;
        } finally {
            // Validación condicional
            if (con != null) {
                // Inicio del bloque try para control de excepciones
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) {}
            }
        }
    }

    // Objeto ResultSet para almacenar los resultados del query de base de datos
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
        // Inicio del bloque try para control de excepciones
        try {
            o.setNombreMecanico(rs.getString("nom_empleado"));
        } catch (SQLException ignore) {
            // nom_empleado not in result set
        }
        // Retornar el valor obtenido
        return o;
    }
}
