// Empezamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

// Procedemos con la importacion de las dependencias y clases que vamos a necesitar
import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
import com.mycompany.motordesk.model.Producto; // Modelo que representa un producto del inventario
import java.sql.*; // Importa Connection, PreparedStatement, ResultSet, Statement, SQLException
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.List; // Interfaz de lista generica

/**
 * Presentamos nuestra Clase de Acceso a Datos (DAO) para los Productos y Repuestos.
 * Con ella, gestionamos todo el inventario de la aplicacion, incluyendo listado, insercion, actualizacion y filtrado.
 */
public class ProductoDAO { // DAO que gestiona todas las operaciones del inventario de repuestos

    /**
     * En esta seccion, nosotros listamos todos los productos que estan disponibles en nuestra base de datos.
     * @return Lista completa de objetos Producto.
     */
    public List<Producto> listarTodos() { // Retorna el inventario completo sin filtros
        List<Producto> lista = new ArrayList<>(); // Lista que contendra todos los productos
        // Definimos la sentencia SQL que vamos a ejecutar en nuestra base de datos
        String sql = "SELECT * FROM producto"; // Trae todos los productos sin filtrar
        // Obtenemos la conexion fisica a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             // Declaramos nuestra consulta preparada para prevenir inyeccion SQL
             PreparedStatement ps = con.prepareStatement(sql); // Prepara el SELECT
             // Usamos el objeto ResultSet para almacenar los resultados de nuestra consulta
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta del inventario
            while (rs.next()) { // Itera por cada producto en el resultado
                lista.add(mapearProducto(rs)); // Convierte la fila en un objeto Producto y lo agrega a la lista
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        // Finalmente, retornamos el valor que hemos obtenido
        return lista; // Devuelve la lista completa del inventario
    }

    /**
     * Aqui procedemos a listar los productos aplicando filtros de busqueda (por vehiculo, seccion o nombre).
     * @param tipoVehiculo Tipo de vehiculo que vamos a filtrar (puede ser null).
     * @param seccion Seccion de nuestro inventario (puede ser null).
     * @param busquedaNombre Nombre o parte del nombre que buscaremos (puede ser null).
     * @return Nuestra lista de productos filtrados.
     */
    public List<Producto> listarFiltrados(String tipoVehiculo, String seccion, String busquedaNombre) { // Lista productos aplicando filtros combinados
        List<Producto> lista = new ArrayList<>(); // Lista que contendra los productos filtrados
        StringBuilder sql = new StringBuilder("SELECT * FROM producto WHERE 1=1 "); // Base del WHERE que permite agregar filtros dinamicamente
        List<Object> params = new ArrayList<>(); // Lista de parametros que se asignaran al PreparedStatement

        // Evaluamos primeramente si el parametro 'tipoVehiculo' no es nulo y tampoco esta vacio. Si cumple ambas condiciones, significa que el usuario desea filtrar por este criterio, asi que nosotros agregamos la respectiva clausula AND a nuestra consulta dinamica en el StringBuilder e insertamos el valor a nuestra lista de parametros.
        if (tipoVehiculo != null && !tipoVehiculo.isEmpty()) { // Solo filtra si se proporciono un tipo de vehiculo
            sql.append("AND tipo_vehiculo = ? "); // Agrega filtro por tipo de vehiculo
            params.add(tipoVehiculo); // Guarda el valor para asignarlo al PreparedStatement
        }
        // Revisamos si el filtro de 'seccion' nos llego con algun valor valido (que no sea nulo ni este vacio). En caso de tenerlo, nosotros ampliamos nuestra clausula WHERE concatenando dinamicamente un AND para la columna seccion, y asi mismo anadimos el valor de esta seccion a nuestra lista de parametros ordenados.
        if (seccion != null && !seccion.isEmpty()) { // Solo filtra si se proporciono una seccion
            sql.append("AND seccion = ? "); // Agrega filtro por seccion del taller
            params.add(seccion); // Guarda el valor para asignarlo al PreparedStatement
        }
        // Verificamos por ultimo si el usuario proporciono algun texto para la 'busquedaNombre', descartando los espacios en blanco sobrantes con trim(). Si la cadena tiene contenido, nosotros anexamos un filtro de coincidencia parcial LIKE a nuestro SQL y preparamos el parametro envolviendolo en comodines '%' para que encuentre coincidencias intermedias en la base de datos.
        if (busquedaNombre != null && !busquedaNombre.trim().isEmpty()) { // Solo filtra si hay texto de busqueda
            sql.append("AND nombre LIKE ? "); // LIKE permite busqueda parcial por nombre
            params.add("%" + busquedaNombre.trim() + "%"); // Los % permiten coincidencia en cualquier parte del nombre
        }

        // Obtenemos nuestra conexion fisica a la base de datos MySQL
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             // Declaramos nuestra consulta preparada para prevenir inyeccion SQL
             PreparedStatement ps = con.prepareStatement(sql.toString())) { // Prepara la consulta dinamica construida

            // Entramos en nuestro bucle de iteracion
            for (int i = 0; i < params.size(); i++) { // Asigna cada parametro a su posicion en el PreparedStatement
                // Comprobamos si el tipo de objeto que estamos recuperando de nuestra lista de parametros es especificamente una instancia de la clase String. Al confirmar que si lo es, nosotros realizamos el casteo y usamos setString en nuestro PreparedStatement, asegurando que la asignacion de variables de la consulta dinamica se haga en su respectiva posicion.
                if (params.get(i) instanceof String) { // Verifica que el parametro es un String
                    ps.setString(i + 1, (String) params.get(i)); // Asigna el String al PreparedStatement en la posicion correcta
                }
            }

            // Usamos nuestro ResultSet para almacenar los resultados del query
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta filtrada
                while (rs.next()) { // Itera por cada producto en el resultado
                    lista.add(mapearProducto(rs)); // Convierte la fila en un objeto Producto
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        // Retornamos el valor que obtuvimos
        return lista; // Devuelve la lista de productos filtrados
    }

    /**
     * Mediante este metodo, obtenemos un producto buscando por su identificador numerico interno.
     * @param id Identificador unico de nuestro producto.
     * @return El producto encontrado, o null si no existe.
     */
    public Producto obtenerPorId(int id) { // Busca un producto especifico por su ID
        Producto p = null; // Sera null si no se encuentra el producto
        // Definimos la sentencia SQL que vamos a ejecutar
        String sql = "SELECT * FROM producto WHERE id_producto = ?"; // Filtra por la llave primaria del producto
        // Obtenemos nuestra conexion fisica a la base de datos
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             // Declaramos nuestra consulta preparada
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda
            ps.setInt(1, id); // Asigna el ID como parametro de filtro
            // Creamos nuestro ResultSet para almacenar los resultados
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                // Verificamos si existe el registro
                if (rs.next()) { // Solo mapea si el producto fue encontrado
                    p = mapearProducto(rs); // Convierte la fila en un objeto Producto
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        // Finalmente, retornamos el producto que obtuvimos
        return p; // Devuelve el producto encontrado o null
    }

    /**
     * Aqui buscamos un producto por su nombre exacto.
     * Es sumamente util para que validemos duplicados antes de insertar.
     * @param nombre Nombre exacto del producto que vamos a buscar.
     * @return Nuestro producto encontrado, o null si no existe.
     */
    public Producto obtenerPorNombreExacto(String nombre) { // Busca un producto por su nombre exacto para evitar duplicados
        Producto p = null; // Sera null si no se encuentra el producto
        // Definimos nuestra consulta SQL
        String sql = "SELECT * FROM producto WHERE nombre = ?"; // Filtra por nombre exacto del producto
        // Obtenemos la conexion a MySQL
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             // Declaramos nuestra consulta preparada para proteger nuestro sistema
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la busqueda por nombre
            ps.setString(1, nombre); // Nombre exacto a buscar
            // Usamos el ResultSet para almacenar nuestro resultado
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                // Validamos mediante nuestro ResultSet si logramos localizar exactamente ese nombre de producto en la tabla. Si la base de datos nos retorna un registro, nosotros procedemos enseguida a invocar nuestro metodo auxiliar de mapeo para construir y retornar dicho objeto Producto con toda la informacion.
                if (rs.next()) { // Solo mapea si el producto fue encontrado por nombre exacto
                    p = mapearProducto(rs); // Convierte la fila en un objeto Producto
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        // Retornamos el valor que encontramos
        return p; // Devuelve el producto o null si no existe
    }

    /**
     * Con esta funcion, insertamos un nuevo producto en nuestra base de datos y retornamos su ID generado.
     * @param p Objeto Producto que vamos a guardar.
     * @return ID del producto que hemos generado, o -1 si falla el proceso.
     * @throws SQLException Si ocurre un error en nuestra consulta.
     */
    public int insertarDevolviendoId(Producto p) throws SQLException { // Inserta un nuevo producto y retorna el ID generado
        // Definimos la sentencia SQL
        String sql = "INSERT INTO producto (nombre, precio, stock, categoria, estado, tipo_vehiculo, seccion) VALUES (?, ?, ?, ?, ?, ?, ?)"; // Inserta el producto con todas sus columnas
        // Obtenemos la conexion fisica a la BD
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             // Declaramos nuestra consulta para prevenir inyeccion y solicitamos las claves generadas
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Solicita el ID autogenerado
            ps.setString(1, p.getNombreProducto()); // Nombre del nuevo producto
            ps.setDouble(2, p.getPrecioUnitario()); // Precio de venta del producto
            ps.setInt(3, p.getStock()); // Stock inicial del producto
            ps.setString(4, "General"); // Categoria por defecto para compatibilidad con el esquema de la BD
            ps.setString(5, "Activo"); // Estado por defecto del nuevo producto
            ps.setString(6, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : ""); // Tipo de vehiculo (vacio si no se especifico)
            ps.setString(7, p.getSeccion() != null ? p.getSeccion() : ""); // Seccion del taller (vacio si no se especifico)

            ps.executeUpdate(); // Ejecuta la insercion

            // Usamos ResultSet para atrapar el ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) { // Obtiene el ID autoincremental generado
                // Validamos si obtuvimos el ID
                if (rs.next()) { // Si hay un ID generado
                    // Retornamos el valor obtenido
                    return rs.getInt(1); // Retorna el ID del producto recien insertado
                }
            }
        }
        // Retornamos -1 si no logramos obtener el ID
        return -1; // Indica que la insercion fallo
    }

    /**
     * Aqui insertamos un producto y simplemente retornamos si fuimos exitosos o no.
     * @param p Objeto Producto a insertar.
     * @return true si logramos insertarlo, false en caso contrario.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public boolean insertar(Producto p) throws SQLException { // Inserta un nuevo producto en el inventario
        // Nosotros enviamos 'General' para la categoria original, y llenamos las nuevas columnas
        // Definimos la sentencia SQL
        String sql = "INSERT INTO producto (nombre, precio, stock, categoria, estado, tipo_vehiculo, seccion) VALUES (?, ?, ?, ?, ?, ?, ?)"; // Inserta el producto con todas sus columnas
        // Obtenemos la conexion
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             // Declaramos la consulta preparada
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el INSERT
            ps.setString(1, p.getNombreProducto()); // Nombre del producto
            ps.setDouble(2, p.getPrecioUnitario()); // Precio de venta
            ps.setInt(3, p.getStock()); // Cantidad inicial en inventario
            ps.setString(4, "General"); // Categoria por defecto
            ps.setString(5, "Activo"); // Estado inicial del producto
            ps.setString(6, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : ""); // Tipo de vehiculo
            ps.setString(7, p.getSeccion() != null ? p.getSeccion() : ""); // Seccion del taller
            // Retornamos nuestro resultado de exito
            return ps.executeUpdate() > 0; // true si la insercion fue exitosa
        }
    }

    /**
     * En esta parte, actualizamos la informacion de un producto existente usando su ID.
     * @param p Producto con los datos que hemos actualizado.
     * @return true si lo actualizamos correctamente, false en caso contrario.
     * @throws SQLException Si ocurre un error.
     */
    public boolean actualizar(Producto p) throws SQLException { // Actualiza los datos de un producto existente
        // Preparamos nuestra sentencia SQL
        String sql = "UPDATE producto SET nombre = ?, precio = ?, stock = ?, tipo_vehiculo = ?, seccion = ? WHERE id_producto = ?"; // Actualiza los campos del producto identificado por su ID
        // Obtenemos nuestra conexion
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             // Declaramos la consulta
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el UPDATE
            ps.setString(1, p.getNombreProducto()); // Nuevo nombre del producto
            ps.setDouble(2, p.getPrecioUnitario()); // Nuevo precio de venta
            ps.setInt(3, p.getStock()); // Nuevo stock del producto
            ps.setString(4, p.getTipoVehiculo() != null ? p.getTipoVehiculo() : ""); // Nuevo tipo de vehiculo
            ps.setString(5, p.getSeccion() != null ? p.getSeccion() : ""); // Nueva seccion del taller
            ps.setInt(6, p.getIdProducto()); // ID del producto a actualizar (en el WHERE)
            // Retornamos el exito de nuestra actualizacion
            return ps.executeUpdate() > 0; // true si la actualizacion fue exitosa
        }
    }

    /**
     * Y aqui eliminamos fisicamente un producto de nuestra base de datos (si no tiene relaciones).
     * @param id ID del producto que vamos a eliminar.
     * @return true si lo eliminamos, false si fallamos.
     * @throws SQLException Si el producto ya esta en uso (ej. en una orden o compra).
     */
    public boolean eliminar(int id) throws SQLException { // Elimina un producto del inventario por su ID
        // Definimos la sentencia de borrado
        String sql = "DELETE FROM producto WHERE id_producto = ?"; // Elimina el producto identificado por su ID
        // Obtenemos la conexion
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             // Declaramos nuestra consulta preparada
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el DELETE
            ps.setInt(1, id); // ID del producto a eliminar
            // Retornamos nuestro resultado
            return ps.executeUpdate() > 0; // true si el producto fue eliminado
        }
    }

    /**
     * Este es nuestro metodo auxiliar privado que convierte una fila del ResultSet en un objeto Producto.
     * @param rs El ResultSet posicionado en la fila que estamos leyendo.
     * @return Un objeto Producto completamente mapeado para que lo usemos.
     * @throws SQLException Si alguna columna no existe o hay error de tipo.
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException { // Metodo auxiliar reutilizable para mapear filas a objetos Producto
        Producto p = new Producto(); // Crea un nuevo objeto producto
        p.setIdProducto(rs.getInt("id_producto")); // ID del producto desde la BD
        p.setNombreProducto(rs.getString("nombre")); // Nombre del producto
        p.setPrecioUnitario(rs.getDouble("precio")); // Precio de venta
        p.setStock(rs.getInt("stock")); // Cantidad disponible en inventario
        p.setEstado(rs.getString("estado")); // Estado del producto (Activo/Inactivo)
        // Iniciamos el bloque try para controlar nuestras excepciones
        try {
            p.setTipoVehiculo(rs.getString("tipo_vehiculo")); // Tipo de vehiculo compatible (puede no existir en esquemas antiguos)
            p.setSeccion(rs.getString("seccion")); // Seccion del taller a la que pertenece el producto
        } catch (SQLException e) {
            // Ignoramos el error si las columnas aun no existen en nuestra BD durante la transicion
        }
        // Retornamos nuestro producto ya ensamblado
        return p; // Devuelve el objeto Producto completamente mapeado
    }
}
