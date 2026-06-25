// Este archivo pertenece al paquete "dao" — la única capa con la que nosotros hablamos con MySQL
package com.mycompany.motordesk.dao;

// Aquí tenemos la clase para obtener la conexión a nuestra base de datos MySQL
import com.mycompany.motordesk.config.Conexion;
// Importamos el modelo Proveedor para mapear las filas de nuestra BD a objetos Java
import com.mycompany.motordesk.model.Proveedor;
// Clases estándar de Java para nuestro manejo de bases de datos
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta es nuestra Clase de Acceso a Datos (DAO) para la tabla proveedor en MySQL.
 * Nosotros gestionamos con ella el alta, baja, modificación y consulta de los proveedores del sistema.
 */
public class ProveedorDAO {

    /**
     * Traemos todos los proveedores registrados en nuestra base de datos.
     * @return Lista de todos los proveedores.
     */
    public List<Proveedor> listarTodos() {
        // Creamos una lista vacía donde acumularemos los proveedores que vayamos encontrando
        List<Proveedor> lista = new ArrayList<>();

        // Traemos todas las columnas de nuestra tabla proveedor
        String sql = "SELECT * FROM proveedor";

        // Con try-with-resources, nuestra conexión se cerrará automáticamente al terminar
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // Ejecutamos la consulta de inmediato

            // Iteramos cada fila de nuestro resultado
            while (rs.next()) {
                // Por cada fila, nosotros creamos un objeto Proveedor con los datos de cada columna
                Proveedor p = new Proveedor(
                    rs.getInt("id_proveedor"),          // ID del proveedor
                    rs.getString("nombre_proveedor"),    // Nombre de la empresa
                    rs.getString("contacto"),            // Persona de contacto
                    rs.getString("telefono"),            // Teléfono (puede ser null)
                    rs.getString("correo")               // Correo (puede ser null)
                );
                lista.add(p); // Agregamos nuestro proveedor a la lista
            }
        } catch (Exception e) {
            e.printStackTrace(); // Mostramos el error en la consola de nuestro servidor si algo llega a fallar
        }

        return lista; // Finalmente devolvemos la lista de proveedores
    }

    /**
     * En esta sección, nosotros registramos un proveedor nuevo con todos sus datos.
     * @param p Objeto Proveedor que vamos a insertar.
     * @return true si nuestro INSERT fue exitoso, false si detectamos algún problema.
     */
    public boolean insertar(Proveedor p) {
        // Usamos los '?' como marcadores de posición seguros — así evitamos inyección SQL
        String sql = "INSERT INTO proveedor (nombre_proveedor, contacto, telefono, correo) VALUES (?, ?, ?, ?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombreProveedor()); // Nombre de la empresa
            ps.setString(2, p.getContacto());         // Nombre del contacto (puede ir vacío)
            ps.setString(3, p.getTelefono());          // Teléfono (puede ir vacío)
            ps.setString(4, p.getCorreo());            // Correo (puede ir vacío)

            // Con executeUpdate() > 0 confirmamos que se insertó al menos una fila en nuestra base
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Retornamos false si tuvimos algún error
        }
    }

    /**
     * Aquí modificamos los datos de un proveedor que ya existe.
     * @param p Objeto Proveedor con los datos que hemos actualizado.
     * @return true si logramos actualizarlo exitosamente, false en caso contrario.
     */
    public boolean actualizar(Proveedor p) {
        // Actualizamos todos los campos, identificando a nuestro proveedor por su ID
        String sql = "UPDATE proveedor SET nombre_proveedor = ?, contacto = ?, telefono = ?, correo = ? WHERE id_proveedor = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombreProveedor()); // Nuevo nombre
            ps.setString(2, p.getContacto());         // Nuevo contacto
            ps.setString(3, p.getTelefono());          // Nuevo teléfono
            ps.setString(4, p.getCorreo());            // Nuevo correo
            ps.setInt(5, p.getIdProveedor());          // ID para el WHERE — aquí identificamos cuál actualizar

            return ps.executeUpdate() > 0; // Si es true, entonces se actualizó exitosamente
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Por acá borramos un proveedor de nuestra base de datos usando su ID.
     * @param id ID del proveedor que vamos a eliminar.
     * @return true si lo eliminamos exitosamente, false si no (ej. si ya tiene compras asociadas).
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM proveedor WHERE id_proveedor = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id); // Ponemos nuestro ID en el '?' para borrar el correcto

            return ps.executeUpdate() > 0; // true = lo hemos eliminado exitosamente
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Si vemos que tiene registros relacionados (como compras), MySQL no nos dejará borrarlo
        }
    }

    /**
     * Mediante esto traemos un proveedor específico por su ID.
     * @param id ID del proveedor a buscar.
     * @return Objeto Proveedor que encontramos, o null si no existe.
     */
    public Proveedor obtenerPorId(int id) {
        String sql = "SELECT * FROM proveedor WHERE id_proveedor = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id); // El ID del proveedor que vamos a buscar

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Si lo encontramos, construimos nuestro objeto con sus datos
                    return new Proveedor(
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre_proveedor"),
                        rs.getString("contacto"),
                        rs.getString("telefono"),
                        rs.getString("correo")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // Si no llegamos a encontrar ese ID, retornaremos null
    }

    /**
     * En este bloque buscamos un proveedor por su nombre; si vemos que no existe, nosotros lo creamos.
     * @param nombre Nombre del proveedor.
     * @return ID del proveedor que hemos encontrado o del nuevo proveedor que hayamos insertado.
     */
    public int obtenerOInsertarProveedor(String nombre) {
        int id = -1; // Fijamos un valor de error por defecto

        // Primero nos aseguramos de buscar si ya existe un proveedor con ese nombre exacto
        String sqlBusqueda = "SELECT id_proveedor FROM proveedor WHERE nombre_proveedor = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlBusqueda)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // Si existe, nosotros retornamos su ID directamente
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Si logramos llegar aquí, significa que el proveedor NO existía — nosotros procedemos a crearlo con solo el nombre
        // Los demás campos (teléfono, correo) los dejamos vacíos para que puedan completarse después
        // desde nuestro módulo de Gestión de Proveedores
        String sqlInsert = "INSERT INTO proveedor (nombre_proveedor) VALUES (?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            // Leemos el ID que MySQL le ha asignado al proveedor que acabamos de crear
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Finalmente, retornamos el nuevo ID
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return id; // Retornamos -1 si vemos que todo falló
    }
}
