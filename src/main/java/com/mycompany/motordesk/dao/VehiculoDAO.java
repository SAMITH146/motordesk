// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Clase pública VehiculoDAO que gestiona la lógica correspondiente
public class VehiculoDAO {

    // Método público 'insertar'
    public int insertar(Vehiculo vehiculo) {
        int idGenerado = -1;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "INSERT INTO vehiculo (id_cliente_fk, placa, marca, modelo, anio) VALUES (?, ?, ?, ?, ?)";
        
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, vehiculo.getIdClienteFk());
            ps.setString(2, vehiculo.getPlaca());
            ps.setString(3, vehiculo.getMarca());
            ps.setString(4, vehiculo.getModelo());
            ps.setInt(5, vehiculo.getAnio());
            
            // Validación condicional
            if (ps.executeUpdate() > 0) {
                // Objeto ResultSet para almacenar los resultados del query de base de datos
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    // Validación condicional
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        vehiculo.setIdVehiculo(idGenerado);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return idGenerado;
    }

    // Método público 'obtenerPorPlaca'
    public Vehiculo obtenerPorPlaca(String placa) {
        Vehiculo v = null;
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM vehiculo WHERE placa = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, placa);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) {
                    v = new Vehiculo(
                        rs.getInt("id_vehiculo"),
                        rs.getInt("id_cliente_fk"),
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return v;
    }

    // Método público 'actualizar'
    public boolean actualizar(Vehiculo vehiculo) {
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "UPDATE vehiculo SET marca = ?, modelo = ?, anio = ?, id_cliente_fk = ? WHERE placa = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, vehiculo.getMarca());
            ps.setString(2, vehiculo.getModelo());
            ps.setInt(3, vehiculo.getAnio());
            ps.setInt(4, vehiculo.getIdClienteFk());
            ps.setString(5, vehiculo.getPlaca());
            
            // Retornar el valor obtenido
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Retornar el valor obtenido
            return false;
        }
    }

    public List<Vehiculo> listarPorCliente(int idClienteFk) {
        List<Vehiculo> lista = new ArrayList<>();
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM vehiculo WHERE id_cliente_fk = ?";
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idClienteFk);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Vehiculo(
                        rs.getInt("id_vehiculo"),
                        rs.getInt("id_cliente_fk"),
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return lista;
    }

    // Método para obtener un Vehículo a partir de su ID primario autoincremental
    // Método público 'obtenerPorId'
    public Vehiculo obtenerPorId(int id) {
        Vehiculo v = null; // Inicializamos el objeto de retorno como nulo
        // Definición de la sentencia SQL para ejecutar en la base de datos
        String sql = "SELECT * FROM vehiculo WHERE id_vehiculo = ?"; // Consulta SQL parametrizada
        
        // Abrimos la conexión física a la BD y preparamos el statement para ejecución segura
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion();
             // Declaración de consulta preparada para prevenir inyección SQL
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id); // Asignamos el parámetro ID al primer marcador '?'
            
            // Ejecutamos la consulta y recuperamos los resultados de la BD
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            try (ResultSet rs = ps.executeQuery()) {
                // Validación condicional
                if (rs.next()) { // Si se encuentra un registro coincidente
                    // Mapeamos los datos y construimos el objeto Vehículo usando el constructor de su clase modelo
                    v = new Vehiculo(
                        rs.getInt("id_vehiculo"),
                        rs.getInt("id_cliente_fk"),
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime la traza de errores en la consola en caso de excepciones
        }
        // Retornar el valor obtenido
        return v; // Devuelve el vehículo encontrado o nulo
    }
}
