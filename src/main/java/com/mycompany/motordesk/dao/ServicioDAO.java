package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
import com.mycompany.motordesk.model.Servicio; // Modelo que representa un servicio de mano de obra
import java.sql.Connection; // Interfaz JDBC para la conexion activa
import java.sql.PreparedStatement; // Sentencia SQL parametrizada para prevenir inyeccion SQL
import java.sql.ResultSet; // Resultado devuelto por la consulta SQL
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.List; // Interfaz de lista generica

/**
 * Aqui tenemos nuestra Clase de Acceso a Datos (DAO) para los Servicios.
 * Nosotros nos encargamos de gestionar la lectura de los servicios disponibles en nuestra base de datos
 * (como afinacion, cambio de aceite, etc.).
 */
public class ServicioDAO { // DAO del catalogo de servicios de mano de obra del taller

    /**
     * En esta parte, obtenemos todos los servicios registrados en nuestra base de datos, y los ordenamos alfabeticamente por nombre.
     * @return Lista de objetos Servicio que hemos recuperado.
     */
    public List<Servicio> listarTodos() { // Retorna el catalogo completo de servicios del taller
        List<Servicio> lista = new ArrayList<>(); // Lista que almacenara todos los servicios encontrados
        // Formulamos nuestra consulta SQL para obtener todos los servicios ordenados por nombre de la A a la Z
        String sql = "SELECT * FROM servicio ORDER BY nombre ASC"; // Ordena alfabeticamente para facilitar la seleccion en formularios

        // Usamos try-with-resources: asi nos aseguramos de cerrar nuestros recursos (Connection, PreparedStatement, ResultSet) automaticamente
        try (Connection con = Conexion.getConexion(); // Obtiene la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia SELECT
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta

            // Recorremos las filas que obtuvimos en nuestro ResultSet
            while (rs.next()) { // Itera por cada servicio en el resultado
                // Mapeamos la fila de nuestra BD a un objeto Servicio de Java
                lista.add(new Servicio(
                    rs.getInt("id_servicio"), // ID del servicio
                    rs.getString("nombre"), // Nombre del servicio (ej. "Cambio de aceite")
                    rs.getDouble("precio_estandar") // Precio base del servicio
                ));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprimimos cualquier error si ocurre un problema con nuestro SQL o la conexion
        }
        return lista; // Devuelve el catalogo completo de servicios
    }

    /**
     * Con este metodo, buscamos y obtenemos un servicio especifico guiandonos por su identificador unico (ID).
     * @param id El identificador numerico de nuestro servicio.
     * @return Nuestro objeto Servicio con los datos obtenidos o null si no lo logramos encontrar.
     */
    public Servicio obtenerPorId(int id) { // Busca un servicio especifico por su ID
        Servicio s = null; // Sera null si no se encuentra ningun servicio con ese ID
        // Preparamos nuestra consulta SQL para buscar un servicio particular usando su llave primaria
        String sql = "SELECT * FROM servicio WHERE id_servicio = ?"; // Filtra por llave primaria

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda

            // Reemplazamos el '?' con el ID que nos llega por parametro (lo que nos protege de la inyeccion SQL)
            ps.setInt(1, id); // Asigna el ID como parametro de filtro

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta de busqueda
                // Si nuestra consulta arroja un resultado (es decir, la fila existe)
                if (rs.next()) { // Solo mapea si encontro el servicio
                    // Instanciamos nuestro objeto con los datos que acabamos de obtener
                    s = new Servicio(
                        rs.getInt("id_servicio"), // ID del servicio
                        rs.getString("nombre"), // Nombre del servicio
                        rs.getDouble("precio_estandar") // Precio estandar del servicio
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return s; // Finalmente, devolvemos el servicio que encontramos (o null en su defecto)
    }
}
