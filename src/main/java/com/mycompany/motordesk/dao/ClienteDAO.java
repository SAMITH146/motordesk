// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Clase pública ClienteDAO que gestiona la lógica correspondiente
public class ClienteDAO {

    // Método público 'insertar'
    public int insertar(Cliente cliente) {
        int idGenerado = -1;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "INSERT INTO cliente (nom_cliente, doc_cliente, direccion_cliente) VALUES (?, ?, ?)";
        
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getDocumento());
            ps.setString(3, cliente.getDireccion());
            
            // Validación condicional
            if (ps.executeUpdate() > 0) {
                // Objeto ResultSet para almacenar los resultados del query de base de datos
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    // Validación condicional
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        cliente.setIdCliente(idGenerado);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return idGenerado;
    }

    // Método público 'obtenerPorDocumento'
    public Cliente obtenerPorDocumento(String documento) {
        Cliente c = null;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM cliente WHERE doc_cliente = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, documento);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) {
                    c = new Cliente(
                        rs.getInt("id_cliente"),
                        rs.getString("nom_cliente"),
                        rs.getString("doc_cliente"),
                        rs.getString("direccion_cliente")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return c;
    }

    // Método público 'actualizar'
    public boolean actualizar(Cliente cliente) {
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "UPDATE cliente SET nom_cliente = ?, direccion_cliente = ? WHERE doc_cliente = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getDireccion());
            ps.setString(3, cliente.getDocumento());
            
            // Retornar el valor obtenido
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Retornar el valor obtenido
            return false;
        }
    }

    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM cliente";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql);
             // Objeto ResultSet para almacenar los resultados del query de base de datos
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nom_cliente"),
                    rs.getString("doc_cliente"),
                    rs.getString("direccion_cliente")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return lista;
    }

    // Método para obtener un Cliente por su ID primario autoincremental
    // Método público 'obtenerPorId'
    public Cliente obtenerPorId(int id) {
        Cliente c = null; // Inicializamos el objeto de retorno como nulo
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?"; // Sentencia SQL con parámetro (?) para evitar inyección SQL
        
        // Abrimos la conexión y preparamos la consulta dentro de un try-with-resources para el cierre automático
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id); // Asignamos el ID del parámetro al primer '?' de la consulta SQL
            
            // Ejecutamos la consulta y recorremos el resultado (ResultSet)
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) { // Si encuentra una fila que coincida
                    // Instanciamos el objeto Cliente mapeando las columnas de la tabla de la BD
                    c = new Cliente(
                        rs.getInt("id_cliente"),
                        rs.getString("nom_cliente"),
                        rs.getString("doc_cliente"),
                        rs.getString("direccion_cliente")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime errores en la consola en caso de fallo de conexión o sintaxis SQL
        }
        // Retornar el valor obtenido
        return c; // Retorna el objeto Cliente (o nulo si no se encontró)
    }
}
