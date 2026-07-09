// Definicion del paquete del proyecto
package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

// Importacion de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
import com.mycompany.motordesk.model.CompraRepuesto; // Modelo que representa la cabecera de una compra a proveedor
import com.mycompany.motordesk.model.DetalleCompra; // Modelo que representa una linea (detalle) de la compra
import java.sql.*; // Importa Connection, PreparedStatement, ResultSet, Statement, SQLException
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.List; // Interfaz de lista generica

/**
 * En esta seccion, tenemos nuestra Clase de Acceso a Datos (DAO) para la Compra de Repuestos.
 * Nosotros gestionamos aqui el registro de nuestras compras a proveedores y su detalle, actualizando nuestro inventario mediante transacciones.
 */
public class CompraRepuestoDAO { // DAO que gestiona el registro de compras de repuestos y la actualizacion del inventario

    /**
     * Con esta funcion, nosotros registramos una compra de repuestos en nuestra base de datos de forma transaccional.
     * Insertamos la compra, insertamos los detalles de nuestra compra y actualizamos el stock de nuestros productos.
     * Si algo falla, nosotros revertimos (rollback) toda la operacion para mantener la consistencia de nuestra base de datos.
     * @param compra Objeto CompraRepuesto con la informacion general de nuestra compra.
     * @param detalles Lista de DetalleCompra con los productos que adquirimos.
     * @return Un mensaje de error si nos ocurre algun fallo, o null si logramos el registro con exito.
     */
    public String registrarCompra(CompraRepuesto compra, List<DetalleCompra> detalles) { // Registra la compra y actualiza stock en una transaccion atomica
        Connection con = null; // Conexion manual para controlar la transaccion
        // Declaramos nuestra consulta preparada para prevenir inyeccion SQL
        PreparedStatement psCompra = null; // PreparedStatement para insertar la cabecera de la compra
        // Declaramos nuestra consulta preparada para los detalles
        PreparedStatement psDetalle = null; // PreparedStatement para insertar los detalles de la compra
        // Declaramos nuestra consulta preparada para actualizar nuestro stock
        PreparedStatement psStock = null; // PreparedStatement para actualizar el stock del producto comprado
        // Objeto ResultSet donde nosotros almacenaremos los resultados de nuestra base de datos
        ResultSet rs = null; // ResultSet para capturar el ID de la compra generado por MySQL
        String errorMessage = null; // null indica exito, cualquier texto indica error

        // Iniciamos nuestro bloque try para el control de excepciones
        try {
            con = Conexion.getConexion(); // Obtiene la conexion manualmente para controlar la transaccion
            con.setAutoCommit(false); // Iniciamos nuestra transaccion (deshabilita el commit automatico)

            // 1. Nosotros empezamos insertando la compra
            String sqlCompra = "INSERT INTO comprarepuesto (id_proveedor_fk, fecha_compra, total) VALUES (?, ?, ?)"; // Inserta la cabecera de la compra
            psCompra = con.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS); // Solicita el ID generado de la compra
            psCompra.setInt(1, compra.getIdProveedorFk()); // ID del proveedor al que se le compro
            psCompra.setDate(2, new java.sql.Date(compra.getFechaCompra().getTime())); // Fecha de la compra convertida a sql.Date
            psCompra.setDouble(3, compra.getTotal()); // Total pagado al proveedor
            psCompra.executeUpdate(); // Ejecuta la insercion de la cabecera

            // Obtenemos el ID de la compra que acabamos de generar
            rs = psCompra.getGeneratedKeys(); // Obtiene el ID autoincremental de la compra recien insertada
            int idCompraGenerado = 0; // ID que se usara para vincular los detalles a esta compra
            // Verificamos si el ResultSet genero exitosamente las claves (en este caso el ID de nuestra compra). Si es asi, procedemos a recuperar ese ID generado extrayendo el valor de la primera columna para asociarlo mas adelante a los detalles de la compra. Si no se genero ninguna clave, entonces nosotros lanzamos una excepcion porque sin el ID de compra no podemos continuar nuestro registro.
            if (rs.next()) { // Si MySQL genero un ID para la compra
                idCompraGenerado = rs.getInt(1); // Captura el ID de la compra para vincularlo con los detalles
            } else {
                throw new SQLException("Nosotros no pudimos obtener el ID de la compra generada."); // Interrumpe la transaccion si no hay ID
            }

            // 2. Ahora, nosotros insertamos los detalles y actualizamos nuestro stock
            String sqlDetalle = "INSERT INTO detallecompra (id_compra_fk, id_repuesto_fk, cantidad, costo_unitario) VALUES (?, ?, ?, ?)"; // Inserta cada linea de detalle de la compra
            String sqlStock = "UPDATE producto SET stock = stock + ? WHERE id_producto = ?"; // Incrementa el stock del producto comprado

            psDetalle = con.prepareStatement(sqlDetalle); // Prepara la sentencia de insercion de detalles
            psStock = con.prepareStatement(sqlStock); // Prepara la sentencia de actualizacion de stock

            // Creamos nuestro bucle de iteracion
            for (DetalleCompra dt : detalles) { // Procesa cada linea de detalle de la compra
                // Insertamos nuestro detalle
                psDetalle.setInt(1, idCompraGenerado); // Vincula el detalle con la cabecera de la compra
                psDetalle.setInt(2, dt.getIdRepuestoFk()); // ID del repuesto comprado
                psDetalle.setInt(3, dt.getCantidad()); // Cantidad adquirida
                psDetalle.setDouble(4, dt.getCostoUnitario()); // Costo unitario pagado al proveedor
                psDetalle.addBatch(); // Agrega la operacion al batch para ejecucion en conjunto

                // Actualizamos nuestro stock
                psStock.setInt(1, dt.getCantidad()); // Cantidad a sumar al stock actual
                psStock.setInt(2, dt.getIdRepuestoFk()); // Producto cuyo stock se va a incrementar
                psStock.addBatch(); // Agrega la actualizacion de stock al batch
            }

            // Ejecutamos nuestros batches
            psDetalle.executeBatch(); // Ejecuta todas las inserciones de detalles de una sola vez
            psStock.executeBatch(); // Ejecuta todas las actualizaciones de stock de una sola vez

            // Confirmamos nuestra transaccion
            con.commit(); // Confirma todos los cambios: compra, detalles y stock actualizados

        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
            errorMessage = e.getMessage(); // Guarda el mensaje de error para retornarlo
            // Revisamos primeramente si la conexion no es nula. Si no es nula, nosotros sabemos que la transaccion se inicio y debemos proceder a revertir (rollback) todos los cambios no guardados para asi mantener la integridad de nuestra base de datos, garantizando que no queden datos a medias tras el error.
            if (con != null) { // Solo hace rollback si la conexion fue abierta
                // Iniciamos nuestro bloque try para el control de excepciones al revertir
                try {
                    con.rollback(); // Nosotros revertimos en caso de error para mantener la integridad de los datos
                } catch (SQLException ex) {
                    ex.printStackTrace(); // Imprime error del rollback para diagnostico
                }
            }
        } finally {
            // Cerramos nuestros recursos
            // Iniciamos nuestro bloque try para el control de excepciones en el cierre
            try {
                // Comprobamos individualmente si cada uno de nuestros recursos (ResultSet, PreparedStatements y Connection) fue instanciado y utilizado durante nuestra ejecucion. De no ser nulos, nosotros invocamos su metodo close() de manera progresiva para liberar la memoria y evitar las fugas de conexiones hacia nuestro servidor de base de datos.
                if (rs != null) rs.close(); // Cierra el ResultSet si fue abierto
                if (psStock != null) psStock.close(); // Cierra el PreparedStatement de stock
                if (psDetalle != null) psDetalle.close(); // Cierra el PreparedStatement de detalles
                if (psCompra != null) psCompra.close(); // Cierra el PreparedStatement de la compra
                if (con != null) { // Cierra la conexion si fue abierta
                    con.setAutoCommit(true); // Restaura el autocommit a true antes de cerrar
                    con.close(); // Devuelve la conexion al pool
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Imprime error al cerrar recursos para diagnostico
            }
        }
        // Finalmente, nosotros retornamos el valor obtenido
        return errorMessage; // null significa que tuvimos exito; cualquier String indica un error
    }

    /**
     * En este metodo, nosotros obtenemos el historial de todas las compras que realizamos a nuestros proveedores.
     * @return Nuestra lista de compras ordenadas de la mas reciente a la mas antigua.
     */
    public List<CompraRepuesto> obtenerHistorialCompras() { // Retorna el historial completo de compras a proveedores
        List<CompraRepuesto> lista = new ArrayList<>(); // Lista que contendra todas las compras
        // Nosotros hacemos JOIN con proveedor para poder tener su nombre
        // Definimos nuestra sentencia SQL para ejecutarla en la base de datos
        String sql = "SELECT c.id_compra, c.id_proveedor_fk, c.fecha_compra, c.total, p.nombre_proveedor " + // Columnas de la compra y nombre del proveedor
                     "FROM comprarepuesto c " + // Tabla principal de compras
                     "LEFT JOIN proveedor p ON c.id_proveedor_fk = p.id_proveedor " + // LEFT JOIN para incluir compras aunque el proveedor haya sido eliminado
                     "ORDER BY c.fecha_compra DESC, c.id_compra DESC"; // Ordena del mas reciente al mas antiguo
        // Obtenemos la conexion fisica a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             // Declaramos nuestra consulta preparada para prevenir inyeccion SQL
             PreparedStatement ps = con.prepareStatement(sql); // Prepara el SELECT con JOIN
             // Usamos nuestro ResultSet para almacenar los resultados del query
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta del historial
            while (rs.next()) { // Itera por cada compra en el resultado
                CompraRepuesto c = new CompraRepuesto(
                    rs.getInt("id_compra"), // ID de la compra
                    rs.getInt("id_proveedor_fk"), // ID del proveedor
                    rs.getDate("fecha_compra"), // Fecha en que se realizo la compra
                    rs.getDouble("total") // Total pagado al proveedor
                );
                c.setNombreProveedor(rs.getString("nombre_proveedor")); // Nombre del proveedor obtenido por JOIN
                lista.add(c); // Agrega la compra a la lista del historial
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        // Retornamos nuestra lista obtenida
        return lista; // Devuelve el historial completo de compras
    }
}
