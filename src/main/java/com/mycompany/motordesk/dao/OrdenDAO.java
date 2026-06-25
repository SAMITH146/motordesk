// Este archivo pertenece a nuestro paquete "dao" — la capa más importante donde nos comunicamos directamente con MySQL
package com.mycompany.motordesk.dao;

// Aquí tenemos la clase que centraliza nuestra conexión a la base de datos (usuario, contraseña, URL de MySQL)
import com.mycompany.motordesk.config.Conexion;
// Estos son los modelos que necesitamos para convertir nuestras filas de BD en objetos Java
import com.mycompany.motordesk.model.DetalleOrden;
import com.mycompany.motordesk.model.OrdenTrabajo;
import com.mycompany.motordesk.model.ServicioOrden;
// Clases estándar de Java para que manejemos la BD
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta es nuestra Clase de Acceso a Datos (DAO) para la gestión de Órdenes de Trabajo.
 * Nosotros gestionamos aquí TODAS las operaciones de la tabla ordentrabajo y detalleorden en MySQL de forma transaccional.
 */
public class OrdenDAO {

    /**
     * En este paso, creamos una orden usando una TRANSACCIÓN (sin servicios de mano de obra).
     * @param o Objeto OrdenTrabajo que vamos a insertar.
     * @param detalles Lista de repuestos (DetalleOrden) que hemos utilizado.
     * @throws Exception Si nos falla la inserción o vemos que no hay stock.
     */
    public void insertarOrden(OrdenTrabajo o, List<DetalleOrden> detalles) throws Exception {
        insertarOrden(o, detalles, new ArrayList<>());
    }

    /**
     * Aquí procedemos a crear una orden completa usando una TRANSACCIÓN (que incluye repuestos y servicios).
     * Si notamos que algo falla, nosotros cancelamos todo (rollback). Además, calculamos y actualizamos el stock automáticamente.
     * @param o Objeto OrdenTrabajo con nuestra información base.
     * @param detalles Lista de repuestos que vamos a descontar del inventario.
     * @param servicios Lista de servicios (mano de obra) que facturaremos.
     * @throws Exception Si nos falla la BD o vemos que el stock es insuficiente.
     */
    public void insertarOrden(OrdenTrabajo o, List<DetalleOrden> detalles, List<ServicioOrden> servicios) throws Exception {

        String sqlOrden   = "INSERT INTO ordentrabajo (id_vehiculo_fk, doc_emple_fk, estado, descripcion, total, placa_vehiculo) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        String sqlPrecio  = "SELECT nombre, stock, precio FROM producto WHERE id_producto = ?";
        String sqlStock   = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";

        Connection con = null;

        try {
            // PASO 1: Iniciamos nuestra transacción de base de datos desactivando el auto-commit
            con = Conexion.getConexion();
            con.setAutoCommit(false);

            double totalOrden = 0.0;

            // PASO 2: Recorremos los repuestos solicitados para validar si hay stock suficiente en inventario
            for (DetalleOrden d : detalles) {
                double precioUnitario = 0.0;
                int stockDisponible = 0;
                String nombreProducto = "";

                try (PreparedStatement psP = con.prepareStatement(sqlPrecio)) {
                    psP.setInt(1, d.getIdProductoFk());
                    try (ResultSet rsP = psP.executeQuery()) {
                        // Verificamos si encontramos el repuesto en la base de datos para extraer su stock actual
                        if (rsP.next()) {
                            nombreProducto  = rsP.getString("nombre");
                            stockDisponible = rsP.getInt("stock");
                            precioUnitario  = rsP.getDouble("precio");
                        } else {
                            throw new Exception("Producto no encontrado.");
                        }
                    }
                }

                if (d.getCantidad() > stockDisponible) {
                    throw new Exception("Stock insuficiente para " + nombreProducto +
                                        ". Solo hay un stock de " + stockDisponible + " unidades.");
                }

                double subtotal = precioUnitario * d.getCantidad();
                d.setSubtotal(subtotal);
                totalOrden += subtotal;
            }

            // PASO 3: Recorremos los servicios de mano de obra y los sumamos al total de nuestra orden
            for (ServicioOrden s : servicios) {
                if (s.getIdServicioFk() > 0 && s.getValorCobrado() >= 0) {
                    totalOrden += s.getValorCobrado();
                }
            }
            o.setTotal(totalOrden);

            // PASO 4: Procedemos a guardar la Orden principal en la base de datos
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

                // PASO 5: Obtenemos el ID único que MySQL le acaba de asignar a la orden que guardamos
                try (ResultSet rs = psO.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1);

                        // PASO 6: Si usamos repuestos, los guardamos en detalleorden y descontamos el stock en producto
                        if (!detalles.isEmpty()) {
                            try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                                 PreparedStatement psS = con.prepareStatement(sqlStock)) {

                                for (DetalleOrden d : detalles) {
                                    psD.setInt(1, idGenerado);
                                    psD.setInt(2, d.getIdProductoFk());
                                    psD.setInt(3, d.getCantidad());
                                    psD.setDouble(4, d.getSubtotal());
                                    psD.addBatch();

                                    psS.setInt(1, d.getCantidad());
                                    psS.setInt(2, d.getIdProductoFk());
                                    psS.addBatch();
                                }

                                psD.executeBatch();
                                psS.executeBatch();
                            }
                        }

                        // PASO 7: Finalmente guardamos los servicios de mano de obra asociados a esta orden
                        String sqlServicio = "INSERT INTO servicioorden (id_orden_fk, id_servicio_fk, valor_cobrado) VALUES (?, ?, ?)";
                        try (PreparedStatement psSrv = con.prepareStatement(sqlServicio)) {
                            for (ServicioOrden s : servicios) {
                                if (s.getIdServicioFk() > 0 && s.getValorCobrado() >= 0) {
                                    psSrv.setInt(1, idGenerado);
                                    psSrv.setInt(2, s.getIdServicioFk());
                                    psSrv.setDouble(3, s.getValorCobrado());
                                    psSrv.addBatch();
                                }
                            }
                            psSrv.executeBatch();
                        }
                    }
                }
            }

            // PASO 8: Si logramos llegar hasta aquí sin errores, confirmamos todos los cambios (Commit)
            con.commit();

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

    /**
     * Con esto traemos todas las órdenes junto con el nombre de nuestro mecánico asignado.
     * Usamos un JOIN para combinar ordentrabajo con empleado_historico.
     * @return Nuestra lista de todas las órdenes de trabajo.
     */
    public List<OrdenTrabajo> listarTodas() {
        List<OrdenTrabajo> lista = new ArrayList<>();

        String sql = "SELECT o.*, h.nom_empleado AS nom_empleado FROM ordentrabajo o LEFT JOIN empleado_historico h ON o.doc_emple_fk = h.doc_emple ORDER BY o.fecha DESC";

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

    /**
     * Ahora filtramos las órdenes asignadas a un mecánico en específico.
     * Esto lo usamos en el panel del mecánico para que él pueda mostrar solo SUS órdenes.
     * @param docMecanico Documento del mecánico.
     * @return Nuestra lista de órdenes correspondientes al mecánico.
     */
    public List<OrdenTrabajo> listarPorMecanico(String docMecanico) {
        List<OrdenTrabajo> lista = new ArrayList<>();

        String sql = "SELECT o.*, h.nom_empleado AS nom_empleado FROM ordentrabajo o LEFT JOIN empleado_historico h ON o.doc_emple_fk = h.doc_emple WHERE o.doc_emple_fk = ? ORDER BY o.fecha DESC";

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

    /**
     * Cambiamos el estado de nuestra orden (ABIERTA, PROCESO, ESPERA, TERMINADA, etc.).
     * Si decidimos que el estado es ESPERA, guardamos el motivo y el tiempo estimado.
     * @param id ID de nuestra orden.
     * @param nuevoEstado Nuevo estado que vamos a asignar.
     * @param motivo Motivo de nuestra espera (puede ser null).
     * @param tiempo Tiempo de espera que calculamos (puede ser null).
     * @return true si logramos actualizar correctamente.
     */
    public boolean actualizarEstado(int id, String nuevoEstado, String motivo, String tiempo) {
        String sql = "UPDATE ordentrabajo SET estado = ?, motivo_espera = ?, tiempo_espera = ? WHERE id_orden = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado); // El nuevo estado (ej. "PROCESO", "TERMINADO")
            // Si el estado es ESPERA, guardamos motivo y tiempo — si no, ponemos null
            ps.setString(2, "ESPERA".equals(nuevoEstado) ? motivo : null);
            ps.setString(3, "ESPERA".equals(nuevoEstado) ? tiempo : null);
            ps.setInt(4, id); // ID de la orden a actualizar

            return ps.executeUpdate() > 0; // true = se actualizó correctamente
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Versión simplificada — para cambios de estado que no necesitan motivo ni tiempo
    public boolean actualizarEstado(int id, String nuevoEstado) {
        return actualizarEstado(id, nuevoEstado, null, null);
    }

    /**
     * Buscamos una orden específica usando su ID, y también incluimos el nombre de nuestro mecánico asignado.
     * @param id ID de nuestra orden.
     * @return El objeto OrdenTrabajo que encontramos, o null si vemos que no existe.
     */
    public OrdenTrabajo obtenerPorId(int id) {
        String sql = "SELECT o.*, h.nom_empleado AS nom_empleado FROM ordentrabajo o LEFT JOIN empleado_historico h ON o.doc_emple_fk = h.doc_emple WHERE o.id_orden = ?";

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

    /**
     * Traemos los repuestos (DetalleOrden) que hemos usado en una orden específica.
     * También incluimos el nombre de nuestro producto mediante un JOIN con la tabla producto.
     * @param idOrden ID de nuestra orden.
     * @return Lista de los repuestos que usamos en esa orden.
     */
    public List<DetalleOrden> obtenerDetallesDeOrden(int idOrden) {
        List<DetalleOrden> lista = new ArrayList<>();

        // JOIN con producto para traer también el nombre del repuesto (no solo el ID)
        String sql = "SELECT d.*, p.nombre FROM detalleorden d JOIN producto p ON d.id_repuesto_fk = p.id_producto WHERE d.id_orden_fk = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden); // ID de la orden cuyos detalles queremos

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleOrden d = new DetalleOrden();
                    d.setIdDetalle(rs.getInt("id_detalle"));       // ID del detalle
                    d.setIdOrdenFk(rs.getInt("id_orden_fk"));      // ID de la orden padre
                    d.setIdProductoFk(rs.getInt("id_repuesto_fk")); // ID del repuesto
                    d.setCantidad(rs.getInt("cantidad"));           // Cantidad usada
                    d.setSubtotal(rs.getDouble("subtotal"));        // Subtotal (precio × cantidad)
                    d.setNombreProducto(rs.getString("nombre"));    // Nombre del repuesto para mostrar
                    lista.add(d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Nosotros verificamos si un cliente ya tiene una orden en estado 'ABIERTA' antes de que le creemos una nueva.
     * Nuestra regla de negocio nos indica: un cliente no puede tener 2 órdenes abiertas al mismo tiempo.
     * @param docCliente Documento de nuestro cliente a validar.
     * @return true si vemos que tiene una orden abierta, false si consideramos que se le puede crear una nueva.
     */
    public boolean tieneOrdenAbiertaPorDocumento(String docCliente) {
        boolean tiene = false;

        // Unimos 3 tablas: ordentrabajo → vehiculo → cliente para buscar por documento
        String sql = "SELECT 1 FROM ordentrabajo o " +
                     "JOIN vehiculo v ON o.id_vehiculo_fk = v.id_vehiculo " +
                     "JOIN cliente c ON v.id_cliente_fk = c.id_cliente " +
                     "WHERE c.doc_cliente = ? AND o.estado = 'ABIERTA' LIMIT 1";
        // "SELECT 1" es más eficiente que "SELECT *" — solo nos importa SI existe, no qué datos tiene

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docCliente); // El documento del cliente a verificar

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tiene = true; // Si hay al menos una fila, el cliente SÍ tiene una orden abierta
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tiene; // true = tiene orden abierta, false = puede crear una nueva
    }

    /**
     * Editamos una orden que ya existe usando transacción (nuestra versión sin servicios).
     * Devolvemos el stock de nuestros repuestos viejos, y luego validamos y descontamos el nuevo stock.
     * @param o Objeto OrdenTrabajo con nuestra información base.
     * @param nuevosDetalles Lista actualizada de nuestros repuestos.
     * @throws Exception Si nos ocurre un problema de base de datos o de stock.
     */
    public void actualizarOrden(OrdenTrabajo o, List<DetalleOrden> nuevosDetalles) throws Exception {
        actualizarOrden(o, nuevosDetalles, new ArrayList<>());
    }

    /**
     * Editamos una orden completa (incluyendo repuestos y servicios) usando nuestra TRANSACCIÓN.
     * Nosotros reintegramos el stock de nuestros repuestos viejos, y luego cobramos el de los nuevos.
     * Borramos los servicios anteriores e insertamos los que hemos actualizado.
     * @param o OrdenTrabajo que hemos actualizado.
     * @param nuevosDetalles Nuestra nueva lista de repuestos.
     * @param nuevosServicios Nuestra nueva lista de servicios.
     * @throws Exception En caso de que notemos error de conexión o inventario insuficiente.
     */
    public void actualizarOrden(OrdenTrabajo o, List<DetalleOrden> nuevosDetalles, List<ServicioOrden> nuevosServicios) throws Exception {
        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false); // Iniciamos la transacción

            // PASO 1: Leemos los detalles VIEJOS de la orden para devolver su stock al inventario
            String sqlGetOld = "SELECT id_repuesto_fk, cantidad FROM detalleorden WHERE id_orden_fk = ?";
            List<DetalleOrden> oldDetalles = new ArrayList<>();

            try (PreparedStatement psGetOld = con.prepareStatement(sqlGetOld)) {
                psGetOld.setInt(1, o.getIdOrden());
                try (ResultSet rs = psGetOld.executeQuery()) {
                    while (rs.next()) {
                        DetalleOrden oldD = new DetalleOrden();
                        oldD.setIdProductoFk(rs.getInt("id_repuesto_fk"));
                        oldD.setCantidad(rs.getInt("cantidad"));
                        oldDetalles.add(oldD); // Guardamos los detalles viejos para devolver el stock
                    }
                }
            }

            // PASO 2: Devolvemos al inventario el stock de los repuestos que tenía antes
            // stock = stock + cantidad_anterior (revertimos el descuento original)
            String sqlRestoreStock = "UPDATE producto SET stock = stock + ? WHERE id_producto = ?";
            try (PreparedStatement psRestore = con.prepareStatement(sqlRestoreStock)) {
                for (DetalleOrden oldD : oldDetalles) {
                    psRestore.setInt(1, oldD.getCantidad());     // Cuánto devolver
                    psRestore.setInt(2, oldD.getIdProductoFk()); // A qué producto devolverlo
                    psRestore.executeUpdate();
                }
            }

            // PASO 3: Validamos stock de los nuevos repuestos y calculamos el nuevo total
            double totalOrden = 0.0;
            String sqlPrecio = "SELECT nombre, stock, precio FROM producto WHERE id_producto = ?";

            // Sumamos los servicios de mano de obra al total primero
            for (ServicioOrden s : nuevosServicios) {
                if (s.getIdServicioFk() > 0 && s.getValorCobrado() >= 0) {
                    totalOrden += s.getValorCobrado();
                }
            }

            for (DetalleOrden d : nuevosDetalles) {
                try (PreparedStatement psP = con.prepareStatement(sqlPrecio)) {
                    psP.setInt(1, d.getIdProductoFk());
                    try (ResultSet rsP = psP.executeQuery()) {
                        if (rsP.next()) {
                            String nombreProducto  = rsP.getString("nombre");
                            int stockDisponible    = rsP.getInt("stock");
                            double precioUnitario  = rsP.getDouble("precio");

                            // Si el nuevo stock no alcanza, cancelamos la edición
                            if (d.getCantidad() > stockDisponible) {
                                throw new Exception("Stock insuficiente para " + nombreProducto +
                                                    ". Solo hay " + stockDisponible + " unidades.");
                            }
                            double subtotal = precioUnitario * d.getCantidad();
                            d.setSubtotal(subtotal);
                            totalOrden += subtotal;
                        } else {
                            throw new Exception("Producto no encontrado.");
                        }
                    }
                }
            }
            o.setTotal(totalOrden); // Nuevo total calculado

            // PASO 4: Borramos los detalles viejos de la tabla detalleorden
            // Y también los servicios viejos de la tabla servicioorden
            String sqlDeleteDetails = "DELETE FROM detalleorden WHERE id_orden_fk = ?";
            String sqlDeleteServicios = "DELETE FROM servicioorden WHERE id_orden_fk = ?";
            try (PreparedStatement psDel = con.prepareStatement(sqlDeleteDetails);
                 PreparedStatement psDelSrv = con.prepareStatement(sqlDeleteServicios)) {
                psDelSrv.setInt(1, o.getIdOrden());
                psDelSrv.executeUpdate(); // Eliminamos servicios viejos
                psDel.setInt(1, o.getIdOrden());
                psDel.executeUpdate(); // Eliminamos todos los detalles anteriores
            }

            // PASO 5: Insertamos los nuevos detalles y descontamos el nuevo stock
            if (!nuevosDetalles.isEmpty()) {
                String sqlDetalle    = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)";
                String sqlDeductStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";

                try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                     PreparedStatement psS = con.prepareStatement(sqlDeductStock)) {
                    for (DetalleOrden d : nuevosDetalles) {
                        psD.setInt(1, o.getIdOrden());
                        psD.setInt(2, d.getIdProductoFk());
                        psD.setInt(3, d.getCantidad());
                        psD.setDouble(4, d.getSubtotal());
                        psD.addBatch();

                        psS.setInt(1, d.getCantidad());
                        psS.setInt(2, d.getIdProductoFk());
                        psS.addBatch();
                    }
                    psD.executeBatch();
                    psS.executeBatch();
                }
            }

            // PASO 6: Insertamos los nuevos servicios de mano de obra
            String sqlServicioUpd = "INSERT INTO servicioorden (id_orden_fk, id_servicio_fk, valor_cobrado) VALUES (?, ?, ?)";
            try (PreparedStatement psSrvUpd = con.prepareStatement(sqlServicioUpd)) {
                for (ServicioOrden s : nuevosServicios) {
                    if (s.getIdServicioFk() > 0 && s.getValorCobrado() >= 0) {
                        psSrvUpd.setInt(1, o.getIdOrden());
                        psSrvUpd.setInt(2, s.getIdServicioFk());
                        psSrvUpd.setDouble(3, s.getValorCobrado());
                        psSrvUpd.addBatch();
                    }
                }
                psSrvUpd.executeBatch();
            }

            // PASO 7: Actualizamos el registro principal de la orden (descripción y total nuevo)
            String sqlUpdateOrder = "UPDATE ordentrabajo SET descripcion = ?, total = ?, placa_vehiculo = ? WHERE id_orden = ?";
            try (PreparedStatement psO = con.prepareStatement(sqlUpdateOrder)) {
                psO.setString(1, o.getDescripcion());
                psO.setDouble(2, o.getTotal());
                psO.setString(3, o.getPlacaVehiculo());
                psO.setInt(4, o.getIdOrden());
                psO.executeUpdate();
            }

            con.commit(); // Todo salió bien — confirmamos todos los cambios

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) {} // Revertimos si algo falló
            }
            throw e; // Propagamos el error al controlador
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) {}
            }
        }
    }

    /**
     * Listamos los servicios (mano de obra) que tenemos registrados para una orden en particular.
     * Lo usamos en nuestra factura y en nuestro formulario de edición para poder mostrar lo que hemos cobrado.
     * @param idOrden ID de nuestra orden.
     * @return Lista de los servicios que hemos asociado a la orden.
     */
    public List<ServicioOrden> obtenerServiciosDeOrden(int idOrden) {
        List<ServicioOrden> lista = new ArrayList<>();

        String sql = "SELECT so.*, s.nombre FROM servicioorden so JOIN servicio s ON so.id_servicio_fk = s.id_servicio WHERE so.id_orden_fk = ? ORDER BY so.id_servicio ASC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden); // ID de la orden cuyos servicios queremos

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServicioOrden s = new ServicioOrden();
                    s.setIdServicio(rs.getInt("id_servicio"));    // ID del servicio
                    s.setIdOrdenFk(rs.getInt("id_orden_fk"));     // ID de la orden padre
                    s.setIdServicioFk(rs.getInt("id_servicio_fk")); // ID del catálogo
                    s.setNombre(rs.getString("nombre"));           // Nombre del servicio
                    s.setValorCobrado(rs.getDouble("valor_cobrado"));             // Valor cobrado
                    lista.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Nosotros convertimos una fila del ResultSet en nuestro objeto OrdenTrabajo.
     * Esto lo reutilizamos en varios de nuestros métodos de esta clase para no tener que repetir el mismo código de mapeo.
     * @param rs ResultSet con los datos que hemos obtenido.
     * @return Objeto OrdenTrabajo que hemos mapeado.
     * @throws SQLException Si nos ocurre algún problema de lectura.
     */
    private OrdenTrabajo mapearOrden(ResultSet rs) throws SQLException {
        OrdenTrabajo o = new OrdenTrabajo();
        o.setIdOrden(rs.getInt("id_orden"));             // ID único de la orden
        o.setPlacaVehiculo(rs.getString("placa_vehiculo")); // Placa del vehículo
        o.setDescripcion(rs.getString("descripcion"));    // Problema reportado
        o.setDocEmpleFk(rs.getString("doc_emple_fk"));   // Documento del mecánico
        o.setEstado(rs.getString("estado"));              // Estado actual de la orden
        o.setFecha(rs.getDate("fecha"));                  // Fecha de creación
        o.setTotal(rs.getDouble("total"));                // Total en dinero
        o.setMotivoEspera(rs.getString("motivo_espera")); // Motivo si está en espera
        o.setTiempoEspera(rs.getString("tiempo_espera")); // Tiempo estimado de espera
        try {
            o.setIdVehiculoFk(rs.getInt("id_vehiculo_fk")); // ID del vehículo (llave foránea)
        } catch (SQLException ignore) {}
        try {
            o.setNombreMecanico(rs.getString("nom_empleado")); // Nombre del mecánico (del JOIN)
        } catch (SQLException ignore) {} // Si no viene en el resultado, ignoramos sin error
        return o; // Retornamos el objeto completamente lleno
    }
}
