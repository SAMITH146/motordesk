// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Proveedor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Clase pública ProveedorDAO que gestiona la lógica correspondiente
public class ProveedorDAO {

    public List<Proveedor> listarTodos() {
        List<Proveedor> lista = new ArrayList<>();
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM proveedor";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Proveedor p = new Proveedor(
                    rs.getInt("id_proveedor"),
                    rs.getString("nombre_proveedor"),
                    rs.getString("contacto"),
                    rs.getString("telefono"),
                    rs.getString("correo")
                );
                lista.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return lista;
    }

    // Método público 'obtenerOInsertarProveedor'
    public int obtenerOInsertarProveedor(String nombre) {
        int id = -1;
        String sqlBusqueda = "SELECT id_proveedor FROM proveedor WHERE nombre_proveedor = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sqlBusqueda)) {
            ps.setString(1, nombre);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) {
                    // Retornar el valor obtenido
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Si no existe, lo creamos dinámicamente
        String sqlInsert = "INSERT INTO proveedor (nombre_proveedor) VALUES (?)";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.getGeneratedKeys()) {
                // Validación condicional
                if (rs.next()) {
                    // Retornar el valor obtenido
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return id;
    }
}
