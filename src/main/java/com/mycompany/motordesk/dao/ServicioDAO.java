package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Servicio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Aquí tenemos nuestra Clase de Acceso a Datos (DAO) para los Servicios.
 * Nosotros nos encargamos de gestionar la lectura de los servicios disponibles en nuestra base de datos
 * (como afinación, cambio de aceite, etc.).
 */
public class ServicioDAO {

    /**
     * En esta parte, obtenemos todos los servicios registrados en nuestra base de datos, y los ordenamos alfabéticamente por nombre.
     * @return Lista de objetos Servicio que hemos recuperado.
     */
    public List<Servicio> listarTodos() {
        List<Servicio> lista = new ArrayList<>();
        // Formulamos nuestra consulta SQL para obtener todos los servicios ordenados por nombre de la A a la Z
        String sql = "SELECT * FROM servicio ORDER BY nombre ASC";
        
        // Usamos try-with-resources: así nos aseguramos de cerrar nuestros recursos (Connection, PreparedStatement, ResultSet) automáticamente
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            // Recorremos las filas que obtuvimos en nuestro ResultSet
            while (rs.next()) {
                // Mapeamos la fila de nuestra BD a un objeto Servicio de Java
                lista.add(new Servicio(
                    rs.getInt("id_servicio"),
                    rs.getString("nombre"),
                    rs.getDouble("precio_estandar")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprimimos cualquier error si ocurre un problema con nuestro SQL o la conexión
        }
        return lista;
    }

    /**
     * Con este método, buscamos y obtenemos un servicio específico guiándonos por su identificador único (ID).
     * @param id El identificador numérico de nuestro servicio.
     * @return Nuestro objeto Servicio con los datos obtenidos o null si no lo logramos encontrar.
     */
    public Servicio obtenerPorId(int id) {
        Servicio s = null;
        // Preparamos nuestra consulta SQL para buscar un servicio particular usando su llave primaria
        String sql = "SELECT * FROM servicio WHERE id_servicio = ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            // Reemplazamos el '?' con el ID que nos llega por parámetro (lo que nos protege de la inyección SQL)
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                // Si nuestra consulta arroja un resultado (es decir, la fila existe)
                if (rs.next()) {
                    // Instanciamos nuestro objeto con los datos que acabamos de obtener
                    s = new Servicio(
                        rs.getInt("id_servicio"),
                        rs.getString("nombre"),
                        rs.getDouble("precio_estandar")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s; // Finalmente, devolvemos el servicio que encontramos (o null en su defecto)
    }
}
