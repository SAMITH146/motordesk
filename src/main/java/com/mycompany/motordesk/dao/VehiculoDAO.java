// Definimos el paquete de nuestro proyecto
package com.mycompany.motordesk.dao;

// Importamos todas las dependencias y clases que vamos a utilizar
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// En esta parte presentamos nuestra clase pública VehiculoDAO, la cual gestiona toda la lógica correspondiente
/**
 * Les presentamos nuestra Clase de Acceso a Datos (DAO) para la gestión de Vehículos.
 * Nosotros la diseñamos para permitirnos registrar, actualizar y buscar los vehículos asociados a nuestros clientes.
 */
public class VehiculoDAO {

    /**
     * Mediante este método, insertamos un nuevo vehículo en nuestra base de datos y retornamos el ID que se genere.
     * @param vehiculo Objeto Vehiculo con los datos que vamos a registrar.
     * @return El ID asignado por la base de datos (id_vehiculo) o -1 si falla nuestro proceso.
     */
    public int insertar(Vehiculo vehiculo) {
        int idGenerado = -1;
        // Preparamos nuestra consulta SQL con marcadores '?' para prevenir cualquier inyección SQL
        String sql = "INSERT INTO vehiculo (id_cliente_fk, placa, marca, modelo, anio, tipo_vehiculo) VALUES (?, ?, ?, ?, ?, ?)";
        
        // Le indicamos a MySQL con Statement.RETURN_GENERATED_KEYS que nos devuelva el ID que se ha creado
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Asignamos nuestros valores a los parámetros
            ps.setInt(1, vehiculo.getIdClienteFk());
            ps.setString(2, vehiculo.getPlaca());
            ps.setString(3, vehiculo.getMarca());
            ps.setString(4, vehiculo.getModelo());
            ps.setInt(5, vehiculo.getAnio());
            ps.setString(6, vehiculo.getTipoVehiculo());
            
            // Verificamos si nuestra inserción afectó al menos una fila
            if (ps.executeUpdate() > 0) {
                // Recuperamos las claves generadas por MySQL
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1); // Tomamos el primer valor, que es el ID autoincremental
                        vehiculo.setIdVehiculo(idGenerado); // Actualizamos nuestro objeto en la memoria
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // En caso de error (ej. placa duplicada), lo imprimimos en nuestra consola
        }
        return idGenerado;
    }

    /**
     * En este método, buscamos un vehículo por su número de placa (recordemos que es única).
     * @param placa Placa del vehículo a buscar.
     * @return Objeto Vehiculo si lo encontramos, o null si no existe.
     */
    public Vehiculo obtenerPorPlaca(String placa) {
        Vehiculo v = null;
        // Definimos la consulta SQL para buscar por placa
        String sql = "SELECT * FROM vehiculo WHERE placa = ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, placa); // Asignamos la placa a nuestra consulta
            
            // Ejecutamos nuestra consulta y recorremos el resultado
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = new Vehiculo(
                        rs.getInt("id_vehiculo"),
                        rs.getInt("id_cliente_fk"),
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    );
                    v.setTipoVehiculo(rs.getString("tipo_vehiculo"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    /**
     * Aquí actualizamos la información de un vehículo que ya existe.
     * Usamos la placa como nuestro criterio de búsqueda (WHERE placa = ?).
     * @param vehiculo Objeto Vehiculo con los datos que hemos actualizado.
     * @return true si logramos actualizarlo correctamente, false si falló.
     */
    public boolean actualizar(Vehiculo vehiculo) {
        // Preparamos nuestra consulta SQL de actualización
        String sql = "UPDATE vehiculo SET marca = ?, modelo = ?, anio = ?, id_cliente_fk = ?, tipo_vehiculo = ? WHERE placa = ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, vehiculo.getMarca());
            ps.setString(2, vehiculo.getModelo());
            ps.setInt(3, vehiculo.getAnio());
            ps.setInt(4, vehiculo.getIdClienteFk());
            ps.setString(5, vehiculo.getTipoVehiculo());
            ps.setString(6, vehiculo.getPlaca()); // Colocamos la placa en nuestra cláusula WHERE
            
            // Retornamos true si modificamos al menos una fila
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Con esto, obtenemos una lista de todos los vehículos que pertenecen a uno de nuestros clientes en particular.
     * @param idClienteFk ID interno del cliente (foránea).
     * @return Lista de vehículos del cliente que encontramos.
     */
    public List<Vehiculo> listarPorCliente(int idClienteFk) {
        List<Vehiculo> lista = new ArrayList<>();
        // Formulamos la consulta SQL filtrando por id_cliente_fk
        String sql = "SELECT * FROM vehiculo WHERE id_cliente_fk = ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idClienteFk);
            
            try (ResultSet rs = ps.executeQuery()) {
                // Iteramos sobre todos los vehículos que logramos encontrar
                while (rs.next()) {
                    Vehiculo v = new Vehiculo(
                        rs.getInt("id_vehiculo"),
                        rs.getInt("id_cliente_fk"),
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    );
                    v.setTipoVehiculo(rs.getString("tipo_vehiculo"));
                    lista.add(v); // Y los vamos agregando a nuestra lista
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * En este paso, buscamos y obtenemos un vehículo usando su ID primario numérico (el autoincremental).
     * @param id Identificador único del vehículo en nuestra base de datos.
     * @return Objeto Vehiculo que hemos encontrado, o null si no logramos ubicarlo.
     */
    public Vehiculo obtenerPorId(int id) {
        Vehiculo v = null;
        // Construimos nuestra consulta SQL para buscar por id_vehiculo
        String sql = "SELECT * FROM vehiculo WHERE id_vehiculo = ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id); // Asignamos el parámetro de nuestro ID
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Si el registro existe, pasamos a crear nuestro objeto
                    v = new Vehiculo(
                        rs.getInt("id_vehiculo"),
                        rs.getInt("id_cliente_fk"),
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    );
                    v.setTipoVehiculo(rs.getString("tipo_vehiculo"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }
}
