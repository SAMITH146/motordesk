// Este archivo pertenece al paquete "dao" — la capa más importante: habla directamente con MySQL
package com.mycompany.motordesk.dao;

// Clase que centraliza la conexión a la base de datos (usuario, contraseña, URL de MySQL)
import com.mycompany.motordesk.config.Conexion;
// Modelos que necesitamos para convertir filas de BD en objetos Java
import com.mycompany.motordesk.model.DetalleOrden;
import com.mycompany.motordesk.model.OrdenTrabajo;
import com.mycompany.motordesk.model.ServicioOrden;
// Clases estándar de Java para manejar la BD
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Clase que gestiona TODAS las operaciones de la tabla ordentrabajo y detalleorden en MySQL
public class OrdenDAO {

    // ===================================================================
    // MÉTODO PRINCIPAL: insertarOrden
    // Crea una orden completa usando una TRANSACCIÓN — si algo falla, todo se cancela
    // ===================================================================
    // ===================================================================
    // MÉTODO: insertarOrden (sobrecarga simple sin servicios — compatibilidad)
    // ===================================================================
    public void insertarOrden(OrdenTrabajo o, List<DetalleOrden> detalles) throws Exception {
        insertarOrden(o, detalles, new ArrayList<>());
    }

    // ===================================================================
    // MÉTODO PRINCIPAL: insertarOrden (con servicios de mano de obra)
    // Crea una orden completa usando una TRANSACCIÓN — si algo falla, todo se cancela
    // El total = suma de repuestos + suma de servicios
    // ===================================================================
    public void insertarOrden(OrdenTrabajo o, List<DetalleOrden> detalles, List<ServicioOrden> servicios) throws Exception {

        // Las 4 consultas SQL que necesitamos para todo el proceso:
        String sqlOrden   = "INSERT INTO ordentrabajo (id_vehiculo_fk, doc_emple_fk, estado, descripcion, total, placa_vehiculo) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        String sqlPrecio  = "SELECT nombre, stock, precio FROM producto WHERE id_producto = ?"; // Para validar stock y calcular precio
        String sqlStock   = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";       // Para descontar del inventario

        Connection con = null; // Declaramos fuera del try para poder hacer rollback en el catch

        try {
            con = Conexion.getConexion(); // Abrimos la conexión a MySQL

            // === PASO A: Iniciamos la transacción ===
            // setAutoCommit(false) significa: "no guardes NADA en la BD todavía"
            // Todo lo que hagamos quedará en suspenso hasta que llamemos commit()
            con.setAutoCommit(false);

            // === PASO 1: Validar stock y calcular el total de la orden ===
            double totalOrden = 0.0; // Acumulador del precio total de todos los repuestos

            for (DetalleOrden d : detalles) { // Recorremos cada repuesto seleccionado
                double precioUnitario = 0.0;
                int stockDisponible = 0;
                String nombreProducto = "";

                // Consultamos el precio y stock actual de este repuesto en el inventario
                try (PreparedStatement psP = con.prepareStatement(sqlPrecio)) {
                    psP.setInt(1, d.getIdProductoFk()); // ID del repuesto a consultar
                    try (ResultSet rsP = psP.executeQuery()) {
                        if (rsP.next()) {
                            nombreProducto  = rsP.getString("nombre"); // Nombre del repuesto
                            stockDisponible = rsP.getInt("stock");     // Cuántos hay en bodega
                            precioUnitario  = rsP.getDouble("precio"); // Precio unitario
                        } else {
                            // Si el producto no existe en la BD, lanzamos error y se cancela todo
                            throw new Exception("Producto no encontrado.");
                        }
                    }
                }

                // REGLA DE NEGOCIO: Si piden más de lo que hay en stock, se bloquea la orden
                if (d.getCantidad() > stockDisponible) {
                    // Este mensaje de error llega directamente a la pantalla del mecánico
                    throw new Exception("Stock insuficiente para " + nombreProducto +
                                        ". Solo hay un stock de " + stockDisponible + " unidades.");
                }

                // Calculamos el subtotal de este repuesto: precio × cantidad
                double subtotal = precioUnitario * d.getCantidad();
                d.setSubtotal(subtotal);   // Guardamos el subtotal en el objeto
                totalOrden += subtotal;    // Sumamos al total general de la orden
            }
            // Sumamos también el valor de cada servicio de mano de obra al total
            for (ServicioOrden s : servicios) {
                if (s.getNombre() != null && !s.getNombre().trim().isEmpty() && s.getValor() > 0) {
                    totalOrden += s.getValor();
                }
            }
            o.setTotal(totalOrden); // Total final = repuestos + servicios de mano de obra

            // === PASO 2: Insertar la cabecera de la orden en la tabla ordentrabajo ===
            // RETURN_GENERATED_KEYS le pide a MySQL que nos devuelva el ID que generó (id_orden)
            try (PreparedStatement psO = con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {

                // Asignamos el ID del vehículo, o NULL si no se pudo vincular
                if (o.getIdVehiculoFk() > 0) {
                    psO.setInt(1, o.getIdVehiculoFk()); // ID del vehículo asociado
                } else {
                    psO.setNull(1, java.sql.Types.INTEGER); // Si no hay vehículo, insertamos NULL
                }
                psO.setString(2, o.getDocEmpleFk());    // Documento del mecánico que crea la orden
                psO.setString(3, "ABIERTA");             // Estado inicial siempre es ABIERTA
                psO.setString(4, o.getDescripcion());    // Descripción del problema reportado
                psO.setDouble(5, o.getTotal());          // Total ya calculado
                psO.setString(6, o.getPlacaVehiculo());  // Placa del vehículo

                int affected = psO.executeUpdate(); // Ejecutamos el INSERT
                if (affected == 0) throw new SQLException("Error al crear la orden.");

                // Leemos el ID que MySQL generó para esta nueva orden
                try (ResultSet rs = psO.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1); // Este es el id_orden que MySQL asignó

                        // === PASO 3: Insertar los detalles y descontar stock en lote (Batch) ===
                        // El "batch" agrupa múltiples inserts y los ejecuta de una sola vez — más eficiente
                        try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                             PreparedStatement psS = con.prepareStatement(sqlStock)) {

                            for (DetalleOrden d : detalles) { // Para cada repuesto de la orden
                                // Preparamos el insert del detalle
                                psD.setInt(1, idGenerado);          // ID de la orden recién creada
                                psD.setInt(2, d.getIdProductoFk()); // ID del repuesto
                                psD.setInt(3, d.getCantidad());     // Cantidad usada
                                psD.setDouble(4, d.getSubtotal());  // Subtotal calculado
                                psD.addBatch(); // Agregamos este insert al lote, sin ejecutar todavía

                                // Preparamos el descuento de stock en el inventario
                                psS.setInt(1, d.getCantidad());     // Cuánto descontar
                                psS.setInt(2, d.getIdProductoFk()); // A qué producto
                                psS.addBatch(); // Agregamos este update al lote
                            }

                            psD.executeBatch(); // Ejecutamos TODOS los inserts de detalles de una vez
                            psS.executeBatch(); // Ejecutamos TODOS los descuentos de stock de una vez
                        }

                        // === PASO 4: Insertar los servicios de mano de obra ===
                        String sqlServicio = "INSERT INTO servicioorden (id_orden_fk, nombre, valor) VALUES (?, ?, ?)";
                        try (PreparedStatement psSrv = con.prepareStatement(sqlServicio)) {
                            for (ServicioOrden s : servicios) {
                                // Solo insertamos servicios con nombre y valor válidos
                                if (s.getNombre() != null && !s.getNombre().trim().isEmpty() && s.getValor() > 0) {
                                    psSrv.setInt(1, idGenerado);        // ID de la orden
                                    psSrv.setString(2, s.getNombre().trim()); // Nombre del servicio
                                    psSrv.setDouble(3, s.getValor());   // Valor de la mano de obra
                                    psSrv.addBatch();
                                }
                            }
                            psSrv.executeBatch(); // Insertamos todos los servicios de una vez
                        }
                    }
                }
            }

            // === PASO B: COMMIT — Todo salió bien, confirmamos todos los cambios en la BD ===
            con.commit();

        } catch (Exception e) {
            // === PASO C: ROLLBACK — Algo falló, deshacemos TODOS los cambios ===
            // Gracias al rollback, la BD queda exactamente como estaba antes — sin datos a medias
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e; // Lanzamos el error hacia arriba para que el Controlador lo muestre al usuario
        } finally {
            // === PASO D: Siempre restauramos autoCommit y cerramos la conexión ===
            // "finally" se ejecuta SIEMPRE, haya o no error — garantiza que nunca quede una conexión abierta
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ===================================================================
    // MÉTODO: listarTodas — Trae todas las órdenes con el nombre del mecánico
    // Usa un JOIN para combinar ordentrabajo + empleado en una sola consulta
    // ===================================================================
    public List<OrdenTrabajo> listarTodas() {
        List<OrdenTrabajo> lista = new ArrayList<>();

        // LEFT JOIN: traemos la orden aunque el mecánico haya sido eliminado del sistema
        // ORDER BY fecha DESC: las órdenes más recientes aparecen primero
        String sql = "SELECT o.*, e.nom_empleado FROM ordentrabajo o LEFT JOIN empleado e ON o.doc_emple_fk = e.doc_emple ORDER BY o.fecha DESC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearOrden(rs)); // Convertimos cada fila en un objeto OrdenTrabajo
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ===================================================================
    // MÉTODO: listarPorMecanico — Filtra las órdenes de UN mecánico específico
    // Lo usa el panel del mecánico para mostrar solo SUS órdenes
    // ===================================================================
    public List<OrdenTrabajo> listarPorMecanico(String docMecanico) {
        List<OrdenTrabajo> lista = new ArrayList<>();

        // WHERE filtra solo las órdenes del mecánico con ese documento
        String sql = "SELECT o.*, e.nom_empleado FROM ordentrabajo o LEFT JOIN empleado e ON o.doc_emple_fk = e.doc_emple WHERE o.doc_emple_fk = ? ORDER BY o.fecha DESC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docMecanico); // Ponemos el documento del mecánico en el '?'
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

    // ===================================================================
    // MÉTODO: actualizarEstado — Cambia el estado de la orden (ABIERTA, PROCESO, etc.)
    // También guarda el motivo si el mecánico pone la orden en ESPERA
    // ===================================================================
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

    // ===================================================================
    // MÉTODO: obtenerPorId — Busca una orden específica por su ID
    // La usa el controlador cuando quiere mostrar la factura de una orden
    // ===================================================================
    public OrdenTrabajo obtenerPorId(int id) {
        // JOIN con empleado para traer también el nombre del mecánico
        String sql = "SELECT o.*, e.nom_empleado FROM ordentrabajo o LEFT JOIN empleado e ON o.doc_emple_fk = e.doc_emple WHERE o.id_orden = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); // El ID de la orden a buscar
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearOrden(rs); // Convertimos la fila en objeto OrdenTrabajo
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Si no encontró esa orden, retornamos null
    }

    // ===================================================================
    // MÉTODO: obtenerDetallesDeOrden — Trae los repuestos usados en una orden
    // Lo usa la factura para mostrar el listado de productos con sus precios
    // ===================================================================
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

    // ===================================================================
    // MÉTODO: tieneOrdenAbiertaPorDocumento
    // Verifica si un cliente ya tiene una orden ABIERTA antes de crear una nueva
    // Regla de negocio: un cliente no puede tener 2 órdenes abiertas al mismo tiempo
    // ===================================================================
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

    // ===================================================================
    // MÉTODO: actualizarOrden — Edita una orden existente (también en transacción)
    // Devuelve el stock de los repuestos viejos, valida el nuevo stock, y reemplaza los detalles
    // ===================================================================
    // ===================================================================
    // MÉTODO: actualizarOrden (sobrecarga simple sin servicios — compatibilidad)
    // ===================================================================
    public void actualizarOrden(OrdenTrabajo o, List<DetalleOrden> nuevosDetalles) throws Exception {
        actualizarOrden(o, nuevosDetalles, new ArrayList<>());
    }

    // ===================================================================
    // MÉTODO: actualizarOrden (con servicios de mano de obra)
    // Devuelve el stock de los repuestos viejos, valida el nuevo stock, y reemplaza los detalles
    // También borra los servicios viejos y los reemplaza por los nuevos
    // ===================================================================
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
                if (s.getNombre() != null && !s.getNombre().trim().isEmpty() && s.getValor() > 0) {
                    totalOrden += s.getValor();
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
            String sqlDetalle    = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)";
            String sqlDeductStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";

            try (PreparedStatement psD = con.prepareStatement(sqlDetalle);
                 PreparedStatement psS = con.prepareStatement(sqlDeductStock)) {
                for (DetalleOrden d : nuevosDetalles) {
                    psD.setInt(1, o.getIdOrden());
                    psD.setInt(2, d.getIdProductoFk());
                    psD.setInt(3, d.getCantidad());
                    psD.setDouble(4, d.getSubtotal());
                    psD.addBatch(); // Agrupamos los inserts

                    psS.setInt(1, d.getCantidad());
                    psS.setInt(2, d.getIdProductoFk());
                    psS.addBatch(); // Agrupamos los descuentos de stock
                }
                psD.executeBatch(); // Ejecutamos todos los inserts
                psS.executeBatch(); // Ejecutamos todos los descuentos
            }

            // PASO 6: Insertamos los nuevos servicios de mano de obra
            String sqlServicioUpd = "INSERT INTO servicioorden (id_orden_fk, nombre, valor) VALUES (?, ?, ?)";
            try (PreparedStatement psSrvUpd = con.prepareStatement(sqlServicioUpd)) {
                for (ServicioOrden s : nuevosServicios) {
                    if (s.getNombre() != null && !s.getNombre().trim().isEmpty() && s.getValor() > 0) {
                        psSrvUpd.setInt(1, o.getIdOrden());
                        psSrvUpd.setString(2, s.getNombre().trim());
                        psSrvUpd.setDouble(3, s.getValor());
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

    // ===================================================================
    // MÉTODO: obtenerServiciosDeOrden — Lista los servicios de mano de obra de una orden
    // Lo usa la factura y el formulario de edición para mostrar los servicios registrados
    // ===================================================================
    public List<ServicioOrden> obtenerServiciosDeOrden(int idOrden) {
        List<ServicioOrden> lista = new ArrayList<>();

        String sql = "SELECT * FROM servicioorden WHERE id_orden_fk = ? ORDER BY id_servicio ASC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOrden); // ID de la orden cuyos servicios queremos

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServicioOrden s = new ServicioOrden();
                    s.setIdServicio(rs.getInt("id_servicio"));    // ID del servicio
                    s.setIdOrdenFk(rs.getInt("id_orden_fk"));     // ID de la orden padre
                    s.setNombre(rs.getString("nombre"));           // Nombre del servicio
                    s.setValor(rs.getDouble("valor"));             // Valor de la mano de obra
                    lista.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ===================================================================
    // MÉTODO PRIVADO: mapearOrden — Convierte una fila del ResultSet en objeto OrdenTrabajo
    // Lo reutilizan varios métodos de esta clase para no repetir el mismo código de mapeo
    // ===================================================================
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
