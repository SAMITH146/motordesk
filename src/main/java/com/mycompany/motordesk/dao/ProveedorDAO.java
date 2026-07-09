// Este archivo pertenece al paquete "dao" — la unica capa con la que nosotros hablamos con MySQL
package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

// Aqui tenemos la clase para obtener la conexion a nuestra base de datos MySQL
import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
// Importamos el modelo Proveedor para mapear las filas de nuestra BD a objetos Java
import com.mycompany.motordesk.model.Proveedor; // Modelo que representa a un proveedor de repuestos
// Clases estandar de Java para nuestro manejo de bases de datos
import java.sql.*; // Importa Connection, PreparedStatement, ResultSet, Statement
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.List; // Interfaz de lista generica

/**
 * Esta es nuestra Clase de Acceso a Datos (DAO) para la tabla proveedor en MySQL.
 * Nosotros gestionamos con ella el alta, baja, modificacion y consulta de los proveedores del sistema.
 */
public class ProveedorDAO { // DAO que gestiona todas las operaciones sobre la tabla proveedor

    /**
     * Traemos todos los proveedores registrados en nuestra base de datos.
     * @return Lista de todos los proveedores.
     */
    public List<Proveedor> listarTodos() { // Retorna el listado completo de proveedores del sistema
        // Creamos una lista vacia donde acumularemos los proveedores que vayamos encontrando
        List<Proveedor> lista = new ArrayList<>(); // Lista que contendra todos los proveedores

        // Traemos todas las columnas de nuestra tabla proveedor
        String sql = "SELECT * FROM proveedor"; // Sin filtros, trae todos los proveedores registrados

        // Con try-with-resources, nuestra conexion se cerrara automaticamente al terminar
        try (Connection con = Conexion.getConexion(); // Abre la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql); // Prepara el SELECT
             ResultSet rs = ps.executeQuery()) { // Ejecutamos la consulta de inmediato

            // Iteramos cada fila de nuestro resultado
            while (rs.next()) { // Itera por cada proveedor en el resultado
                // Por cada fila, nosotros creamos un objeto Proveedor con los datos de cada columna
                Proveedor p = new Proveedor(
                    rs.getInt("id_proveedor"),          // ID del proveedor
                    rs.getString("nombre_proveedor"),    // Nombre de la empresa proveedora
                    rs.getString("contacto"),            // Persona de contacto en el proveedor
                    rs.getString("telefono"),            // Telefono del proveedor (puede ser null)
                    rs.getString("correo")               // Correo del proveedor (puede ser null)
                );
                lista.add(p); // Agregamos nuestro proveedor a la lista
            }
        } catch (Exception e) {
            e.printStackTrace(); // Mostramos el error en la consola de nuestro servidor si algo llega a fallar
        }

        return lista; // Finalmente devolvemos la lista de proveedores
    }

    /**
     * En esta seccion, nosotros registramos un proveedor nuevo con todos sus datos.
     * @param p Objeto Proveedor que vamos a insertar.
     * @return true si nuestro INSERT fue exitoso, false si detectamos algun problema.
     */
    public boolean insertar(Proveedor p) { // Registra un nuevo proveedor en la base de datos
        // Usamos los '?' como marcadores de posicion seguros — asi evitamos inyeccion SQL
        String sql = "INSERT INTO proveedor (nombre_proveedor, contacto, telefono, correo) VALUES (?, ?, ?, ?)"; // Inserta el nuevo proveedor con sus datos de contacto

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el INSERT

            ps.setString(1, p.getNombreProveedor()); // Nombre de la empresa proveedora
            ps.setString(2, p.getContacto());         // Nombre del contacto (puede ir vacio)
            ps.setString(3, p.getTelefono());          // Telefono de contacto (puede ir vacio)
            ps.setString(4, p.getCorreo());            // Correo electronico (puede ir vacio)

            // Con executeUpdate() > 0 confirmamos que se inserto al menos una fila en nuestra base
            return ps.executeUpdate() > 0; // true si la insercion fue exitosa
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
            return false; // Retornamos false si tuvimos algun error
        }
    }

    /**
     * Aqui modificamos los datos de un proveedor que ya existe.
     * @param p Objeto Proveedor con los datos que hemos actualizado.
     * @return true si logramos actualizarlo exitosamente, false en caso contrario.
     */
    public boolean actualizar(Proveedor p) { // Actualiza los datos de un proveedor existente
        // Actualizamos todos los campos, identificando a nuestro proveedor por su ID
        String sql = "UPDATE proveedor SET nombre_proveedor = ?, contacto = ?, telefono = ?, correo = ? WHERE id_proveedor = ?"; // Actualiza todos los campos del proveedor

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el UPDATE

            ps.setString(1, p.getNombreProveedor()); // Nuevo nombre del proveedor
            ps.setString(2, p.getContacto());         // Nuevo contacto
            ps.setString(3, p.getTelefono());          // Nuevo telefono
            ps.setString(4, p.getCorreo());            // Nuevo correo
            ps.setInt(5, p.getIdProveedor());          // ID para el WHERE — aqui identificamos cual actualizar

            return ps.executeUpdate() > 0; // Si es true, entonces se actualizo exitosamente
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
            return false; // Retorna false si la actualizacion fallo
        }
    }

    /**
     * Por aca borramos un proveedor de nuestra base de datos usando su ID.
     * @param id ID del proveedor que vamos a eliminar.
     * @return true si lo eliminamos exitosamente, false si no (ej. si ya tiene compras asociadas).
     */
    public boolean eliminar(int id) { // Elimina un proveedor de la base de datos por su ID
        String sql = "DELETE FROM proveedor WHERE id_proveedor = ?"; // Elimina el proveedor identificado por su ID

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el DELETE

            ps.setInt(1, id); // Ponemos nuestro ID en el '?' para borrar el correcto

            return ps.executeUpdate() > 0; // true = lo hemos eliminado exitosamente
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
            return false; // Si vemos que tiene registros relacionados (como compras), MySQL no nos dejara borrarlo
        }
    }

    /**
     * Mediante esto traemos un proveedor especifico por su ID.
     * @param id ID del proveedor a buscar.
     * @return Objeto Proveedor que encontramos, o null si no existe.
     */
    public Proveedor obtenerPorId(int id) { // Busca un proveedor especifico por su ID
        String sql = "SELECT * FROM proveedor WHERE id_proveedor = ?"; // Filtra por la llave primaria del proveedor

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda

            ps.setInt(1, id); // El ID del proveedor que vamos a buscar

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                if (rs.next()) { // Si lo encontramos, construimos nuestro objeto con sus datos
                    return new Proveedor(
                        rs.getInt("id_proveedor"), // ID del proveedor
                        rs.getString("nombre_proveedor"), // Nombre del proveedor
                        rs.getString("contacto"), // Persona de contacto
                        rs.getString("telefono"), // Telefono de contacto
                        rs.getString("correo") // Correo electronico
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }

        return null; // Si no llegamos a encontrar ese ID, retornaremos null
    }

    /**
     * En este bloque buscamos un proveedor por su nombre; si vemos que no existe, nosotros lo creamos.
     * @param nombre Nombre del proveedor.
     * @return ID del proveedor que hemos encontrado o del nuevo proveedor que hayamos insertado.
     */
    public int obtenerOInsertarProveedor(String nombre) { // Busca el proveedor o lo crea si no existe (upsert)
        int id = -1; // Fijamos un valor de error por defecto

        // Primero nos aseguramos de buscar si ya existe un proveedor con ese nombre exacto
        String sqlBusqueda = "SELECT id_proveedor FROM proveedor WHERE nombre_proveedor = ?"; // Busca el proveedor por nombre exacto

        try (Connection con = Conexion.getConexion(); // Abre la conexion para la busqueda
             PreparedStatement ps = con.prepareStatement(sqlBusqueda)) { // Prepara la consulta de busqueda
            ps.setString(1, nombre); // Nombre del proveedor a buscar
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                if (rs.next()) { // Si el proveedor ya existe
                    return rs.getInt(1); // Si existe, nosotros retornamos su ID directamente
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }

        // Si logramos llegar aqui, significa que el proveedor NO existia — nosotros procedemos a crearlo con solo el nombre
        // Los demas campos (telefono, correo) los dejamos vacios para que puedan completarse despues
        // desde nuestro modulo de Gestion de Proveedores
        String sqlInsert = "INSERT INTO proveedor (nombre_proveedor) VALUES (?)"; // Crea el proveedor con solo el nombre

        try (Connection con = Conexion.getConexion(); // Abre una nueva conexion para la insercion
             PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) { // Solicita el ID generado
            ps.setString(1, nombre); // Nombre del nuevo proveedor
            ps.executeUpdate(); // Inserta el nuevo proveedor
            // Leemos el ID que MySQL le ha asignado al proveedor que acabamos de crear
            try (ResultSet rs = ps.getGeneratedKeys()) { // Obtiene el ID autoincremental generado
                if (rs.next()) { // Si se genero un ID
                    return rs.getInt(1); // Finalmente, retornamos el nuevo ID del proveedor creado
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }

        return id; // Retornamos -1 si vemos que todo fallo
    }
}
