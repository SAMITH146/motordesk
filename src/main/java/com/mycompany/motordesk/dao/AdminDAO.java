// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Clase pública AdminDAO que gestiona la lógica correspondiente
public class AdminDAO {

    // Método público 'contarMecanicosActivos'
    public int contarMecanicosActivos() {
        int total = 0;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT COUNT(*) FROM empleado WHERE id_rol_fk = 2 AND estado_empleado = 'ACTIVO'";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
             ResultSet rs = ps.executeQuery()) {
            // Validación condicional
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return total;
    }

    // Método público 'contarProductos'
    public int contarProductos() {
        int total = 0;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT COUNT(*) FROM producto";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
             ResultSet rs = ps.executeQuery()) {
            // Validación condicional
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return total;
    }

    // Método público 'contarStockCritico'
    public int contarStockCritico() {
        int total = 0;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT COUNT(*) FROM producto WHERE stock < 5";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
             ResultSet rs = ps.executeQuery()) {
            // Validación condicional
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return total;
    }

    // Método público 'contarOrdenesTotales'
    public int contarOrdenesTotales() {
        int total = 0;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT COUNT(*) FROM ordentrabajo";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
             ResultSet rs = ps.executeQuery()) {
            // Validación condicional
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return total;
    }
}
