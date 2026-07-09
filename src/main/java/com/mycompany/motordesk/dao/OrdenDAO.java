// Este archivo pertenece a nuestro paquete "dao" — la capa mas importante donde nos comunicamos directamente con MySQL
package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

// Aqui tenemos la clase que centraliza nuestra conexion a la base de datos (usuario, contrasena, URL de MySQL)
import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
// Estos son los modelos que necesitamos para convertir nuestras filas de BD en objetos Java
import com.mycompany.motordesk.model.DetalleOrden; // Modelo de un repuesto usado en la orden de trabajo
import com.mycompany.motordesk.model.OrdenTrabajo; // Modelo principal de una orden de trabajo
import com.mycompany.motordesk.model.ServicioOrden; // Modelo de un servicio de mano de obra asociado a la orden
// Clases estandar de Java para que manejemos la BD
import java.sql.*; // Importa Connection, PreparedStatement, ResultSet, Statement, SQLException
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.List; // Interfaz de lista generica

/**
 * Esta es nuestra Clase de Acceso a Datos (DAO) para la gestion de Ordenes de Trabajo.
 * Nosotros gestionamos aqui TODAS las operaciones de la tabla ordentrabajo y detalleorden en MySQL de forma transaccional.
 */
public class OrdenDAO { // DAO que gestiona el ciclo de vida completo de las ordenes de trabajo del taller

    /**
     * En este paso, creamos una orden usando una TRANSACCION (sin servicios de mano de obra).
     * @param o Objeto OrdenTrabajo que vamos a insertar.
     * @param detalles Lista de repuestos (DetalleOrden) que hemos utilizado.
     * @throws Exception Si nos falla la insercion o vemos que no hay stock.
     */
    public void insertarOrden(OrdenTrabajo o, List<DetalleOrden> detalles) throws Exception { // Sobrecarga sin servicios: delega al metodo principal con lista vacia
        insertarOrden(o, detalles, new ArrayList<>()); // Llama al metodo completo con lista de servicios vacia
    }

    /**
     * Aqui procedemos a crear una orden completa usando una TRANSACCION (que incluye repuestos y servicios).
     * Si notamos que algo falla, nosotros cancelamos todo (rollback). Ademas, calculamos y actualizamos el stock automaticamente.
     * @param o Objeto OrdenTrabajo con nuestra informacion base.
     * @param detalles Lista de repuestos que vamos a descontar del inventario.
     * @param servicios Lista de servicios (mano de obra) que facturaremos.
     * @throws Exception Si nos falla la BD o vemos que el stock es insuficiente.
     */
    public void insertarOrden(OrdenTrabajo o, List<DetalleOrden> detalles, List<ServicioOrden> servicios) throws Exception { // Crea una orden completa con repuestos y servicios en una sola transaccion

        String sqlOrden   = "INSERT INTO ordentrabajo (id_vehiculo_fk, doc_emple_fk, estado, descripcion, total, placa_vehiculo) VALUES (?, ?, ?, ?, ?, ?)"; // Inserta la orden principal en la tabla
        String sqlDetalle = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)"; // Inserta cada repuesto utilizado en la orden
        String sqlPrecio  = "SELECT nombre, stock, precio FROM producto WHERE id_producto = ?"; // Consulta el stock y precio del repuesto antes de descontarlo
        String sqlStock   = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?"; // Descuenta el stock del repuesto tras confirmar la orden

        Connection con = null; // Conexion manual para controlar la transaccion

        try {
            // PASO 1: Iniciamos nuestra transaccion de base de datos desactivando el auto-commit
            con = Conexion.getConexion(); // Obtiene la conexion manualmente
            con.setAutoCommit(false); // Deshabilita autocommit para controlar la transaccion manualmente

            double totalOrden = 0.0; // Acumulador del total de la orden (repuestos + servicios)

            // PASO 2: Recorremos los repuestos solicitados para validar si hay stock suficiente en inventario
            for (DetalleOrden d : detalles) { // Itera por cada repuesto solicitado en la orden
                double precioUnitario = 0.0; // Precio de venta del repuesto
                int stockDisponible = 0; // Stock actual del repuesto en inventario
                String nombreProducto = ""; // Nombre del repuesto (para mensajes de error)

                try (PreparedStatement psP = con.prepareStatement(sqlPrecio)) { // Prepara la consulta de precio y stock
                    psP.setInt(1, d.getIdProductoFk()); // ID del repuesto a consultar
                    try (ResultSet rsP = psP.executeQuery()) { // Ejecuta la consulta del repuesto
                        // Verificamos si encontramos el repuesto en la base de datos para extraer su stock actual
                        if (rsP.next()) { // Solo procesa si el repuesto existe en inventario
                            nombreProducto  = rsP.getString("nombre"); // Nombre para mensajes de error
                            stockDisponible = rsP.getInt("stock"); // Stock actual disponible
                            precioUnitario  = rsP.getDouble("precio"); // Precio de venta unitario
                        } else {
                            throw new Exception("Producto no encontrado."); // Interrumpe si el repuesto no existe
                        }
                    }
                }

                if (d.getCantidad() > stockDisponible) { // Valida que haya suficiente stock antes de registrar
                    throw new Exception("Stock insuficiente para " + nombreProducto +
                                        ". Solo hay un stock de " + stockDisponible + " unidades."); // Error de stock insuficiente
                }

                double subtotal = precioUnitario * d.getCantidad(); // Calcula el subtotal del repuesto (precio * cantidad)
                d.setSubtotal(subtotal); // Guarda el subtotal en el detalle para persistirlo
                totalOrden += subtotal; // Acumula al total de la orden
            }

            // PASO 3: Recorremos los servicios de mano de obra y los sumamos al total de nuestra orden
            for (ServicioOrden s : servicios) { // Itera por cada servicio de mano de obra
                if (s.getIdServicioFk() > 0 && s.getValorCobrado() >= 0) { // Valida que el servicio sea valido
                    totalOrden += s.getValorCobrado(); // Suma el valor del servicio al total de la orden
                }
            }
            o.setTotal(totalOrden); // Asigna el total calculado a la orden antes de persistirla

            // PASO 4: Procedemos a guardar la Orden principal en la base de datos
            try (PreparedStatement psO = con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) { // Solicita el ID generado de la orden

                if (o.getIdVehiculoFk() > 0) { // Si la orden tiene un vehiculo asociado
                    psO.setInt(1, o.getIdVehiculoFk()); // ID del vehiculo del cliente
                } else {
                    psO.setNull(1, java.sql.Types.INTEGER); // Permite ordenes sin vehiculo registrado
                }
                psO.setString(2, o.getDocEmpleFk()); // Documento del mecanico asignado a la orden
                psO.setString(3, "ABIERTA"); // Estado inicial de toda orden nueva
                psO.setString(4, o.getDescripcion()); // Descripcion del trabajo a realizar
                psO.setDouble(5, o.getTotal()); // Total calculado de repuestos y servicios
                psO.setString(6, o.getPlacaVehiculo()); // Placa del vehiculo (redundante para consultas rapidas)

                int affected = psO.executeUpdate(); // Ejecuta la insercion de la orden
                if (affected == 0) throw new SQLException("Error al crear la orden."); // Interrumpe si no se inserto

                // PASO 5: Obtenemos el ID unico que MySQL le acaba de asignar a la orden que guardamos
                try (ResultSet rs = psO.getGeneratedKeys()) { // Obtiene el ID autogenerado de la orden
                    if (rs.next()) { // Si se genero un ID
                        int idGenerado = rs.getInt(1); // ID de la nueva orden para vincular detalles y servicios

                        // PASO 6: Si usamos repuestos, los guardamos en detalleorden y descontamos el stock en producto
                        if (!detalles.isEmpty()) { // Solo ejecuta si hay repuestos en la orden
                            try (PreparedStatement psD = con.prepareStatement(sqlDetalle); // Prepara la insercion de detalles
                                 PreparedStatement psS = con.prepareStatement(sqlStock)) { // Prepara el descuento de stock

                                for (DetalleOrden d : detalles) { // Itera por cada repuesto para persistirlo
                                    psD.setInt(1, idGenerado); // Vincula el detalle con la orden recien creada
                                    psD.setInt(2, d.getIdProductoFk()); // ID del repuesto usado
                                    psD.setInt(3, d.getCantidad()); // Cantidad usada del repuesto
                                    psD.setDouble(4, d.getSubtotal()); // Subtotal del repuesto
                                    psD.addBatch(); // Agrega al batch para ejecucion conjunta

                                    psS.setInt(1, d.getCantidad()); // Cantidad a descontar del stock
                                    psS.setInt(2, d.getIdProductoFk()); // Producto cuyo stock se reduce
                                    psS.addBatch(); // Agrega al batch de stock
                                }

                                psD.executeBatch(); // Inserta todos los detalles de una vez
                                psS.executeBatch(); // Descuenta el stock de todos los repuestos de una vez
                            }
                        }

                        // PASO 7: Finalmente guardamos los servicios de mano de obra asociados a esta orden
                        String sqlServicio = "INSERT INTO servicioorden (id_orden_fk, id_servicio_fk, valor_cobrado) VALUES (?, ?, ?)"; // Inserta cada servicio de mano de obra cobrado
                        try (PreparedStatement psSrv = con.prepareStatement(sqlServicio)) { // Prepara la insercion de servicios
                            for (ServicioOrden s : servicios) { // Itera por cada servicio de mano de obra
                                if (s.getIdServicioFk() > 0 && s.getValorCobrado() >= 0) { // Valida que el servicio sea valido
                                    psSrv.setInt(1, idGenerado); // ID de la orden a la que pertenece el servicio
                                    psSrv.setInt(2, s.getIdServicioFk()); // ID del servicio del catalogo
                                    psSrv.setDouble(3, s.getValorCobrado()); // Valor cobrado por el servicio
                                    psSrv.addBatch(); // Agrega al batch de servicios
                                }
                            }
                            psSrv.executeBatch(); // Inserta todos los servicios de una vez
                        }
                    }
                }
            }

            // PASO 8: Si logramos llegar hasta aqui sin errores, confirmamos todos los cambios (Commit)
            con.commit(); // Confirma la transaccion: orden, detalles, servicios y stock actualizados

        } catch (Exception e) {
            if (con != null) { // Solo hace rollback si la conexion fue abierta
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Revierte todos los cambios si algo fallo
            }
            throw e; // Propaga el error al controlador para mostrar el mensaje al usuario
        } finally {
            if (con != null) { // Cierra la conexion en el bloque finally para garantizar el cierre
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); } // Restaura autocommit y cierra la conexion
            }
        }
    }

    /**
     * Con esto traemos todas las ordenes junto con el nombre de nuestro mecanico asignado.
     * Usamos un JOIN para combinar ordentrabajo con empleado_historico.
     * @return Nuestra lista de todas las ordenes de trabajo.
     */
    public List<OrdenTrabajo> listarTodas() { // Retorna todas las ordenes de trabajo con el nombre del mecanico
        List<OrdenTrabajo> lista = new ArrayList<>(); // Lista que contendra todas las ordenes

        String sql = "SELECT o.*, h.nom_empleado AS nom_empleado FROM ordentrabajo o LEFT JOIN empleado_historico h ON o.doc_emple_fk = h.doc_emple ORDER BY o.fecha DESC"; // JOIN con historico para obtener el nombre del mecanico aunque haya sido eliminado

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql); // Prepara el SELECT con JOIN
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta
            while (rs.next()) { // Itera por cada orden en el resultado
                lista.add(mapearOrden(rs)); // Convierte la fila en un objeto OrdenTrabajo
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return lista; // Devuelve la lista completa de ordenes
    }

    /**
     * Ahora filtramos las ordenes asignadas a un mecanico en especifico.
     * Esto lo usamos en el panel del mecanico para que el pueda mostrar solo SUS ordenes.
     * @param docMecanico Documento del mecanico.
     * @return Nuestra lista de ordenes correspondientes al mecanico.
     */
    public List<OrdenTrabajo> listarPorMecanico(String docMecanico) { // Lista solo las ordenes asignadas al mecanico logueado
        List<OrdenTrabajo> lista = new ArrayList<>(); // Lista de ordenes del mecanico

        String sql = "SELECT o.*, h.nom_empleado AS nom_empleado FROM ordentrabajo o LEFT JOIN empleado_historico h ON o.doc_emple_fk = h.doc_emple WHERE o.doc_emple_fk = ? ORDER BY o.fecha DESC"; // Filtra por el documento del mecanico y ordena por fecha

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el SELECT filtrado
            ps.setString(1, docMecanico); // Documento del mecanico cuyas ordenes se buscan
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                while (rs.next()) { // Itera por cada orden del mecanico
                    lista.add(mapearOrden(rs)); // Convierte la fila en un objeto OrdenTrabajo
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return lista; // Devuelve las ordenes del mecanico
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
    public boolean actualizarEstado(int id, String nuevoEstado, String motivo, String tiempo) { // Cambia el estado de la orden e incluye motivo/tiempo si es ESPERA
        String sql = "UPDATE ordentrabajo SET estado = ?, motivo_espera = ?, tiempo_espera = ? WHERE id_orden = ?"; // Actualiza el estado y datos de espera de la orden

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el UPDATE
            ps.setString(1, nuevoEstado); // El nuevo estado (ej. "PROCESO", "TERMINADO")
            // Si el estado es ESPERA, guardamos motivo y tiempo — si no, ponemos null
            ps.setString(2, "ESPERA".equals(nuevoEstado) ? motivo : null); // Solo guarda motivo si es estado ESPERA
            ps.setString(3, "ESPERA".equals(nuevoEstado) ? tiempo : null); // Solo guarda tiempo si es estado ESPERA
            ps.setInt(4, id); // ID de la orden a actualizar (en el WHERE)

            return ps.executeUpdate() > 0; // true = se actualizo correctamente
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
            return false; // Retorna false si la actualizacion fallo
        }
    }

    // Version simplificada — para cambios de estado que no necesitan motivo ni tiempo
    public boolean actualizarEstado(int id, String nuevoEstado) { // Sobrecarga sin motivo ni tiempo para estados simples
        return actualizarEstado(id, nuevoEstado, null, null); // Delega al metodo completo con nulls
    }

    /**
     * Buscamos una orden especifica usando su ID, y tambien incluimos el nombre de nuestro mecanico asignado.
     * @param id ID de nuestra orden.
     * @return El objeto OrdenTrabajo que encontramos, o null si vemos que no existe.
     */
    public OrdenTrabajo obtenerPorId(int id) { // Busca una orden especifica por su ID con el nombre del mecanico
        String sql = "SELECT o.*, h.nom_empleado AS nom_empleado FROM ordentrabajo o LEFT JOIN empleado_historico h ON o.doc_emple_fk = h.doc_emple WHERE o.id_orden = ?"; // JOIN para traer tambien el nombre del mecanico

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda
            ps.setInt(1, id); // ID de la orden a buscar
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                if (rs.next()) { // Si encontro la orden
                    return mapearOrden(rs); // Convierte la fila en un objeto OrdenTrabajo
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return null; // Retorna null si la orden no existe
    }

    /**
     * Traemos los repuestos (DetalleOrden) que hemos usado en una orden especifica.
     * Tambien incluimos el nombre de nuestro producto mediante un JOIN con la tabla producto.
     * @param idOrden ID de nuestra orden.
     * @return Lista de los repuestos que usamos en esa orden.
     */
    public List<DetalleOrden> obtenerDetallesDeOrden(int idOrden) { // Obtiene los repuestos utilizados en una orden especifica
        List<DetalleOrden> lista = new ArrayList<>(); // Lista que contendra los detalles de la orden

        // JOIN con producto para traer tambien el nombre del repuesto (no solo el ID)
        String sql = "SELECT d.*, p.nombre FROM detalleorden d JOIN producto p ON d.id_repuesto_fk = p.id_producto WHERE d.id_orden_fk = ?"; // JOIN para obtener el nombre del repuesto junto con sus detalles

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia con JOIN
            ps.setInt(1, idOrden); // ID de la orden cuyos detalles queremos

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda de detalles
                while (rs.next()) { // Itera por cada repuesto en el resultado
                    DetalleOrden d = new DetalleOrden(); // Nuevo objeto detalle para cada fila
                    d.setIdDetalle(rs.getInt("id_detalle"));       // ID del detalle
                    d.setIdOrdenFk(rs.getInt("id_orden_fk"));      // ID de la orden padre
                    d.setIdProductoFk(rs.getInt("id_repuesto_fk")); // ID del repuesto utilizado
                    d.setCantidad(rs.getInt("cantidad"));           // Cantidad usada del repuesto
                    d.setSubtotal(rs.getDouble("subtotal"));        // Subtotal (precio * cantidad)
                    d.setNombreProducto(rs.getString("nombre"));    // Nombre del repuesto para mostrar en la vista
                    lista.add(d); // Agrega el detalle a la lista
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return lista; // Devuelve la lista de repuestos de la orden
    }

    /**
     * Nosotros verificamos si un cliente ya tiene una orden en estado 'ABIERTA' antes de que le creemos una nueva.
     * Nuestra regla de negocio nos indica: un cliente no puede tener 2 ordenes abiertas al mismo tiempo.
     * @param docCliente Documento de nuestro cliente a validar.
     * @return true si vemos que tiene una orden abierta, false si consideramos que se le puede crear una nueva.
     */
    public boolean tieneOrdenAbiertaPorDocumento(String docCliente) { // Valida la regla de negocio: un cliente no puede tener dos ordenes abiertas
        boolean tiene = false; // Por defecto asume que no tiene ordenes abiertas

        // Unimos 3 tablas: ordentrabajo -> vehiculo -> cliente para buscar por documento
        String sql = "SELECT 1 FROM ordentrabajo o " + // SELECT 1 es mas eficiente que SELECT * cuando solo interesa saber si existe
                     "JOIN vehiculo v ON o.id_vehiculo_fk = v.id_vehiculo " + // Vincula la orden con el vehiculo del cliente
                     "JOIN cliente c ON v.id_cliente_fk = c.id_cliente " + // Vincula el vehiculo con el cliente
                     "WHERE c.doc_cliente = ? AND o.estado = 'ABIERTA' LIMIT 1"; // Filtra por documento del cliente y estado ABIERTA
        // "SELECT 1" es mas eficiente que "SELECT *" — solo nos importa SI existe, no que datos tiene

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la consulta de verificacion
            ps.setString(1, docCliente); // El documento del cliente a verificar

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la verificacion
                if (rs.next()) { // Si hay al menos una fila, el cliente SI tiene una orden abierta
                    tiene = true; // Marca que el cliente ya tiene una orden abierta
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return tiene; // true = tiene orden abierta, false = puede crear una nueva
    }

    /**
     * Editamos una orden que ya existe usando transaccion (nuestra version sin servicios).
     * Devolvemos el stock de nuestros repuestos viejos, y luego validamos y descontamos el nuevo stock.
     * @param o Objeto OrdenTrabajo con nuestra informacion base.
     * @param nuevosDetalles Lista actualizada de nuestros repuestos.
     * @throws Exception Si nos ocurre un problema de base de datos o de stock.
     */
    public void actualizarOrden(OrdenTrabajo o, List<DetalleOrden> nuevosDetalles) throws Exception { // Sobrecarga sin servicios: delega al metodo completo con lista vacia
        actualizarOrden(o, nuevosDetalles, new ArrayList<>()); // Llama al metodo completo con lista de servicios vacia
    }

    /**
     * Editamos una orden completa (incluyendo repuestos y servicios) usando nuestra TRANSACCION.
     * Nosotros reintegramos el stock de nuestros repuestos viejos, y luego cobramos el de los nuevos.
     * Borramos los servicios anteriores e insertamos los que hemos actualizado.
     * @param o OrdenTrabajo que hemos actualizado.
     * @param nuevosDetalles Nuestra nueva lista de repuestos.
     * @param nuevosServicios Nuestra nueva lista de servicios.
     * @throws Exception En caso de que notemos error de conexion o inventario insuficiente.
     */
    public void actualizarOrden(OrdenTrabajo o, List<DetalleOrden> nuevosDetalles, List<ServicioOrden> nuevosServicios) throws Exception { // Edita una orden devolviendo stock viejo y descontando el nuevo en transaccion
        Connection con = null; // Conexion manual para controlar la transaccion
        try {
            con = Conexion.getConexion(); // Obtiene la conexion manualmente
            con.setAutoCommit(false); // Inicia la transaccion

            // PASO 1: Leemos los detalles VIEJOS de la orden para devolver su stock al inventario
            String sqlGetOld = "SELECT id_repuesto_fk, cantidad FROM detalleorden WHERE id_orden_fk = ?"; // Consulta los repuestos actuales de la orden
            List<DetalleOrden> oldDetalles = new ArrayList<>(); // Lista de repuestos viejos para devolver al inventario

            try (PreparedStatement psGetOld = con.prepareStatement(sqlGetOld)) { // Prepara la consulta de detalles viejos
                psGetOld.setInt(1, o.getIdOrden()); // ID de la orden cuyos detalles viejos se consultaran
                try (ResultSet rs = psGetOld.executeQuery()) { // Ejecuta la consulta
                    while (rs.next()) { // Itera por cada detalle viejo
                        DetalleOrden oldD = new DetalleOrden(); // Objeto temporal para guardar el detalle viejo
                        oldD.setIdProductoFk(rs.getInt("id_repuesto_fk")); // ID del repuesto viejo
                        oldD.setCantidad(rs.getInt("cantidad")); // Cantidad que se va a devolver al inventario
                        oldDetalles.add(oldD); // Guarda los detalles viejos para devolver el stock
                    }
                }
            }

            // PASO 2: Devolvemos al inventario el stock de los repuestos que tenia antes
            // stock = stock + cantidad_anterior (revertimos el descuento original)
            String sqlRestoreStock = "UPDATE producto SET stock = stock + ? WHERE id_producto = ?"; // Devuelve el stock de los repuestos viejos al inventario
            try (PreparedStatement psRestore = con.prepareStatement(sqlRestoreStock)) { // Prepara el UPDATE de restauracion
                for (DetalleOrden oldD : oldDetalles) { // Itera por cada repuesto viejo a devolver
                    psRestore.setInt(1, oldD.getCantidad());     // Cuanto devolver al stock
                    psRestore.setInt(2, oldD.getIdProductoFk()); // A que producto devolverlo
                    psRestore.executeUpdate(); // Ejecuta la devolucion de stock
                }
            }

            // PASO 3: Validamos stock de los nuevos repuestos y calculamos el nuevo total
            double totalOrden = 0.0; // Acumulador del nuevo total de la orden
            String sqlPrecio = "SELECT nombre, stock, precio FROM producto WHERE id_producto = ?"; // Consulta el stock y precio del nuevo repuesto

            // Sumamos los servicios de mano de obra al total primero
            for (ServicioOrden s : nuevosServicios) { // Itera por cada servicio nuevo
                if (s.getIdServicioFk() > 0 && s.getValorCobrado() >= 0) { // Valida que el servicio sea valido
                    totalOrden += s.getValorCobrado(); // Suma el valor del servicio al total
                }
            }

            for (DetalleOrden d : nuevosDetalles) { // Itera por cada nuevo repuesto
                try (PreparedStatement psP = con.prepareStatement(sqlPrecio)) { // Prepara la consulta de precio y stock
                    psP.setInt(1, d.getIdProductoFk()); // ID del nuevo repuesto
                    try (ResultSet rsP = psP.executeQuery()) { // Ejecuta la consulta
                        if (rsP.next()) { // Si el repuesto existe
                            String nombreProducto  = rsP.getString("nombre"); // Nombre para mensajes de error
                            int stockDisponible    = rsP.getInt("stock"); // Stock disponible actual (tras la devolucion)
                            double precioUnitario  = rsP.getDouble("precio"); // Precio de venta

                            // Si el nuevo stock no alcanza, cancelamos la edicion
                            if (d.getCantidad() > stockDisponible) { // Valida que haya suficiente stock nuevo
                                throw new Exception("Stock insuficiente para " + nombreProducto +
                                                    ". Solo hay " + stockDisponible + " unidades."); // Error de stock
                            }
                            double subtotal = precioUnitario * d.getCantidad(); // Calcula el subtotal del nuevo repuesto
                            d.setSubtotal(subtotal); // Guarda el subtotal en el detalle
                            totalOrden += subtotal; // Acumula al total
                        } else {
                            throw new Exception("Producto no encontrado."); // Error si el repuesto no existe
                        }
                    }
                }
            }
            o.setTotal(totalOrden); // Asigna el nuevo total calculado a la orden

            // PASO 4: Borramos los detalles viejos de la tabla detalleorden
            // Y tambien los servicios viejos de la tabla servicioorden
            String sqlDeleteDetails = "DELETE FROM detalleorden WHERE id_orden_fk = ?"; // Elimina los repuestos viejos de la orden
            String sqlDeleteServicios = "DELETE FROM servicioorden WHERE id_orden_fk = ?"; // Elimina los servicios viejos de la orden
            try (PreparedStatement psDel = con.prepareStatement(sqlDeleteDetails); // Prepara el DELETE de detalles
                 PreparedStatement psDelSrv = con.prepareStatement(sqlDeleteServicios)) { // Prepara el DELETE de servicios
                psDelSrv.setInt(1, o.getIdOrden()); // ID de la orden cuyos servicios se eliminan
                psDelSrv.executeUpdate(); // Eliminamos servicios viejos
                psDel.setInt(1, o.getIdOrden()); // ID de la orden cuyos detalles se eliminan
                psDel.executeUpdate(); // Eliminamos todos los detalles anteriores
            }

            // PASO 5: Insertamos los nuevos detalles y descontamos el nuevo stock
            if (!nuevosDetalles.isEmpty()) { // Solo ejecuta si hay nuevos repuestos
                String sqlDetalle    = "INSERT INTO detalleorden (id_orden_fk, id_repuesto_fk, cantidad, subtotal) VALUES (?, ?, ?, ?)"; // Inserta los nuevos detalles de la orden
                String sqlDeductStock = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?"; // Descuenta el stock de los nuevos repuestos

                try (PreparedStatement psD = con.prepareStatement(sqlDetalle); // Prepara la insercion de nuevos detalles
                     PreparedStatement psS = con.prepareStatement(sqlDeductStock)) { // Prepara el descuento de stock
                    for (DetalleOrden d : nuevosDetalles) { // Itera por cada nuevo repuesto
                        psD.setInt(1, o.getIdOrden()); // ID de la orden
                        psD.setInt(2, d.getIdProductoFk()); // ID del nuevo repuesto
                        psD.setInt(3, d.getCantidad()); // Cantidad del nuevo repuesto
                        psD.setDouble(4, d.getSubtotal()); // Subtotal del nuevo repuesto
                        psD.addBatch(); // Agrega al batch de detalles

                        psS.setInt(1, d.getCantidad()); // Cantidad a descontar del inventario
                        psS.setInt(2, d.getIdProductoFk()); // Producto cuyo stock se reduce
                        psS.addBatch(); // Agrega al batch de stock
                    }
                    psD.executeBatch(); // Inserta todos los nuevos detalles de una vez
                    psS.executeBatch(); // Descuenta el stock de todos los nuevos repuestos de una vez
                }
            }

            // PASO 6: Insertamos los nuevos servicios de mano de obra
            String sqlServicioUpd = "INSERT INTO servicioorden (id_orden_fk, id_servicio_fk, valor_cobrado) VALUES (?, ?, ?)"; // Inserta los nuevos servicios de mano de obra
            try (PreparedStatement psSrvUpd = con.prepareStatement(sqlServicioUpd)) { // Prepara la insercion de servicios
                for (ServicioOrden s : nuevosServicios) { // Itera por cada nuevo servicio
                    if (s.getIdServicioFk() > 0 && s.getValorCobrado() >= 0) { // Valida que el servicio sea valido
                        psSrvUpd.setInt(1, o.getIdOrden()); // ID de la orden
                        psSrvUpd.setInt(2, s.getIdServicioFk()); // ID del servicio del catalogo
                        psSrvUpd.setDouble(3, s.getValorCobrado()); // Valor cobrado por el servicio
                        psSrvUpd.addBatch(); // Agrega al batch de servicios
                    }
                }
                psSrvUpd.executeBatch(); // Inserta todos los nuevos servicios de una vez
            }

            // PASO 7: Actualizamos el registro principal de la orden (descripcion y total nuevo)
            String sqlUpdateOrder = "UPDATE ordentrabajo SET descripcion = ?, total = ?, placa_vehiculo = ? WHERE id_orden = ?"; // Actualiza los campos principales de la orden
            try (PreparedStatement psO = con.prepareStatement(sqlUpdateOrder)) { // Prepara el UPDATE principal
                psO.setString(1, o.getDescripcion()); // Nueva descripcion del trabajo
                psO.setDouble(2, o.getTotal()); // Nuevo total calculado
                psO.setString(3, o.getPlacaVehiculo()); // Placa del vehiculo
                psO.setInt(4, o.getIdOrden()); // ID de la orden a actualizar
                psO.executeUpdate(); // Ejecuta la actualizacion principal
            }

            con.commit(); // Todo salio bien — confirmamos todos los cambios de la edicion

        } catch (Exception e) {
            if (con != null) { // Solo hace rollback si la conexion fue abierta
                try { con.rollback(); } catch (SQLException ex) {} // Revertimos si algo fallo
            }
            throw e; // Propagamos el error al controlador
        } finally {
            if (con != null) { // Cierra la conexion en el bloque finally
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) {} // Restaura autocommit y cierra la conexion
            }
        }
    }

    /**
     * Listamos los servicios (mano de obra) que tenemos registrados para una orden en particular.
     * Lo usamos en nuestra factura y en nuestro formulario de edicion para poder mostrar lo que hemos cobrado.
     * @param idOrden ID de nuestra orden.
     * @return Lista de los servicios que hemos asociado a la orden.
     */
    public List<ServicioOrden> obtenerServiciosDeOrden(int idOrden) { // Obtiene los servicios de mano de obra de una orden
        List<ServicioOrden> lista = new ArrayList<>(); // Lista de servicios de la orden

        String sql = "SELECT so.*, s.nombre FROM servicioorden so JOIN servicio s ON so.id_servicio_fk = s.id_servicio WHERE so.id_orden_fk = ? ORDER BY so.id_servicio ASC"; // JOIN para traer tambien el nombre del servicio del catalogo

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia con JOIN
            ps.setInt(1, idOrden); // ID de la orden cuyos servicios queremos

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda de servicios
                while (rs.next()) { // Itera por cada servicio en el resultado
                    ServicioOrden s = new ServicioOrden(); // Nuevo objeto servicio para cada fila
                    s.setIdServicio(rs.getInt("id_servicio"));    // ID del registro en servicioorden
                    s.setIdOrdenFk(rs.getInt("id_orden_fk"));     // ID de la orden padre
                    s.setIdServicioFk(rs.getInt("id_servicio_fk")); // ID del servicio en el catalogo
                    s.setNombre(rs.getString("nombre"));           // Nombre del servicio del catalogo
                    s.setValorCobrado(rs.getDouble("valor_cobrado")); // Valor cobrado por el servicio
                    lista.add(s); // Agrega el servicio a la lista
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return lista; // Devuelve la lista de servicios de la orden
    }

    /**
     * Nosotros convertimos una fila del ResultSet en nuestro objeto OrdenTrabajo.
     * Esto lo reutilizamos en varios de nuestros metodos de esta clase para no tener que repetir el mismo codigo de mapeo.
     * @param rs ResultSet con los datos que hemos obtenido.
     * @return Objeto OrdenTrabajo que hemos mapeado.
     * @throws SQLException Si nos ocurre algun problema de lectura.
     */
    private OrdenTrabajo mapearOrden(ResultSet rs) throws SQLException { // Metodo auxiliar reutilizable para mapear filas a objetos OrdenTrabajo
        OrdenTrabajo o = new OrdenTrabajo(); // Crea el objeto orden
        o.setIdOrden(rs.getInt("id_orden"));             // ID unico de la orden
        o.setPlacaVehiculo(rs.getString("placa_vehiculo")); // Placa del vehiculo (para consultas rapidas sin JOIN)
        o.setDescripcion(rs.getString("descripcion"));    // Descripcion del problema o trabajo realizado
        o.setDocEmpleFk(rs.getString("doc_emple_fk"));   // Documento del mecanico asignado
        o.setEstado(rs.getString("estado"));              // Estado actual de la orden
        o.setFecha(rs.getDate("fecha"));                  // Fecha de creacion de la orden
        o.setTotal(rs.getDouble("total"));                // Total en dinero de la orden
        o.setMotivoEspera(rs.getString("motivo_espera")); // Motivo si la orden esta en espera
        o.setTiempoEspera(rs.getString("tiempo_espera")); // Tiempo estimado de espera
        try {
            o.setIdVehiculoFk(rs.getInt("id_vehiculo_fk")); // ID del vehiculo (llave foranea, puede ser null)
        } catch (SQLException ignore) {} // Ignora si la columna no esta disponible en la consulta
        try {
            o.setNombreMecanico(rs.getString("nom_empleado")); // Nombre del mecanico obtenido por JOIN con empleado_historico
        } catch (SQLException ignore) {} // Si no viene en el resultado, ignoramos sin error
        return o; // Retorna el objeto completamente lleno para mostrarlo en la vista
    }
}
