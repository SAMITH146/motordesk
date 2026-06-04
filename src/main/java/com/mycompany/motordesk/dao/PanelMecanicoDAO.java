// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// Clase pública PanelMecanicoDAO que gestiona la lógica correspondiente
public class PanelMecanicoDAO {

    public List<Producto> obtenerStockBajo() {
        List<Producto> lista = new ArrayList<>();
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM producto WHERE stock < 5 LIMIT 5";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombreProducto(rs.getString("nombre"));
                p.setStock(rs.getInt("stock"));
                lista.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return lista;
    }

    // Método público 'contarOrdenesAbiertas'
    public int contarOrdenesAbiertas(String docMecanico) {
        int total = 0;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ? AND estado = 'ABIERTA'";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docMecanico);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return total;
    }

    // Método público 'contarOrdenesHoy'
    public int contarOrdenesHoy(String docMecanico) {
        int total = 0;
        // Diagram uses 'fecha' DATE
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ? AND fecha = CURRENT_DATE";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docMecanico);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return total;
    }
}