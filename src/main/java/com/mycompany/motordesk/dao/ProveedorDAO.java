// Este archivo pertenece al paquete "dao" — la única capa que habla con MySQL
package com.mycompany.motordesk.dao;

// Clase para obtener la conexión a la base de datos MySQL
import com.mycompany.motordesk.config.Conexion;
// Importamos el modelo Proveedor para mapear filas de la BD a objetos Java
import com.mycompany.motordesk.model.Proveedor;
// Clases estándar de Java para manejo de bases de datos
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Clase que gestiona todas las operaciones de la tabla "proveedor" en MySQL
public class ProveedorDAO {

    // -----------------------------------------------------------------------
    // MÉTODO: listarTodos — Trae todos los proveedores registrados en la BD
    // Lo usamos para mostrar la tabla completa en gestionarProveedores.jsp
    // -----------------------------------------------------------------------
    public List<Proveedor> listarTodos() {
        // Lista vacía donde acumularemos los proveedores encontrados
        List<Proveedor> lista = new ArrayList<>();

        // Traemos todas las columnas de la tabla proveedor
        String sql = "SELECT * FROM proveedor";

        // try-with-resources: la conexión se cierra automáticamente al terminar
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // Ejecutamos la consulta de inmediato

            // Iteramos cada fila del resultado
            while (rs.next()) {
                // Por cada fila creamos un objeto Proveedor con los datos de cada columna
                Proveedor p = new Proveedor(
                    rs.getInt("id_proveedor"),          // ID del proveedor
                    rs.getString("nombre_proveedor"),    // Nombre de la empresa
                    rs.getString("contacto"),            // Persona de contacto
                    rs.getString("telefono"),            // Teléfono (puede ser null)
                    rs.getString("correo")               // Correo (puede ser null)
                );
                lista.add(p); // Agregamos el proveedor a la lista
            }
        } catch (Exception e) {
            e.printStackTrace(); // Mostramos el error en la consola del servidor si falla
        }

        return lista; // Devolvemos la lista de proveedores
    }

    // -----------------------------------------------------------------------
    // MÉTODO: insertar — Registra un proveedor nuevo con todos sus datos
    // Retorna true si el INSERT fue exitoso, false si hubo algún problema
    // -----------------------------------------------------------------------
    public boolean insertar(Proveedor p) {
        // Los '?' son marcadores de posición seguros — evitan inyección SQL
        String sql = "INSERT INTO proveedor (nombre_proveedor, contacto, telefono, correo) VALUES (?, ?, ?, ?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombreProveedor()); // Nombre de la empresa
            ps.setString(2, p.getContacto());         // Nombre del contacto (puede ir vacío)
            ps.setString(3, p.getTelefono());          // Teléfono (puede ir vacío)
            ps.setString(4, p.getCorreo());            // Correo (puede ir vacío)

            // executeUpdate() > 0 confirma que se insertó al menos una fila
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Retornamos false si hubo error
        }
    }

    // -----------------------------------------------------------------------
    // MÉTODO: actualizar — Modifica los datos de un proveedor existente
    // Se llama cuando el administrador edita un proveedor en la tabla
    // -----------------------------------------------------------------------
    public boolean actualizar(Proveedor p) {
        // Actualizamos todos los campos, identificando al proveedor por su ID
        String sql = "UPDATE proveedor SET nombre_proveedor = ?, contacto = ?, telefono = ?, correo = ? WHERE id_proveedor = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombreProveedor()); // Nuevo nombre
            ps.setString(2, p.getContacto());         // Nuevo contacto
            ps.setString(3, p.getTelefono());          // Nuevo teléfono
            ps.setString(4, p.getCorreo());            // Nuevo correo
            ps.setInt(5, p.getIdProveedor());          // ID para el WHERE — identifica cuál actualizar

            return ps.executeUpdate() > 0; // true = se actualizó exitosamente
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // MÉTODO: eliminar — Borra un proveedor de la BD por su ID
    // Si el proveedor tiene compras asociadas, MySQL lanzará un error por llave foránea
    // -----------------------------------------------------------------------
    public boolean eliminar(int id) {
        String sql = "DELETE FROM proveedor WHERE id_proveedor = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id); // Ponemos el ID en el '?' para borrar el correcto

            return ps.executeUpdate() > 0; // true = se eliminó exitosamente
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Si tiene registros relacionados (compras), MySQL no lo deja borrar
        }
    }

    // -----------------------------------------------------------------------
    // MÉTODO: obtenerPorId — Trae UN proveedor específico por su ID
    // Lo usamos cuando el admin hace clic en "Editar" para cargar los datos en el formulario
    // -----------------------------------------------------------------------
    public Proveedor obtenerPorId(int id) {
        String sql = "SELECT * FROM proveedor WHERE id_proveedor = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id); // El ID del proveedor a buscar

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Si lo encontramos, construimos el objeto con sus datos
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

        return null; // Si no se encontró ese ID, retornamos null
    }

    // -----------------------------------------------------------------------
    // MÉTODO: obtenerOInsertarProveedor — Busca un proveedor por nombre;
    // si no existe lo crea. Lo usa el módulo de compras para garantizar
    // que el proveedor siempre exista antes de registrar una compra
    // -----------------------------------------------------------------------
    public int obtenerOInsertarProveedor(String nombre) {
        int id = -1; // Valor de error por defecto

        // Primero buscamos si ya existe un proveedor con ese nombre exacto
        String sqlBusqueda = "SELECT id_proveedor FROM proveedor WHERE nombre_proveedor = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlBusqueda)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // Si existe, retornamos su ID directamente
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Si llegamos aquí, el proveedor NO existía — lo creamos con solo el nombre
        // Los demás campos (teléfono, correo) quedan vacíos y se pueden completar después
        // desde el módulo de Gestión de Proveedores
        String sqlInsert = "INSERT INTO proveedor (nombre_proveedor) VALUES (?)";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            // Leemos el ID que MySQL le asignó al proveedor recién creado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Retornamos el nuevo ID
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return id; // Retornamos -1 si todo falló
    }
}
