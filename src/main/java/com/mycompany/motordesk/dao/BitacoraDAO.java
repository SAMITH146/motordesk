package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Bitacora;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Les presentamos nuestra Clase de Acceso a Datos (DAO) para el registro de la Bitácora.
 * Aquí nosotros nos encargamos de guardar y consultar todo nuestro historial de acciones en el sistema.
 */
public class BitacoraDAO {

    /**
     * Con este método, nosotros registramos una nueva acción en nuestra bitácora del sistema.
     * @param docEmple Documento del empleado que realiza nuestra acción.
     * @param nombreUsuario Nombre de nuestro usuario.
     * @param accion Descripción corta de la acción que realizamos (ej. "LOGIN", "CREAR_ORDEN").
     * @param detalle Descripción detallada de lo que ocurrió en nuestra plataforma.
     * @return true si logramos registrar la acción correctamente, false en caso contrario.
     */
    public boolean registrarAccion(String docEmple, String nombreUsuario, String accion, String detalle) {
        boolean registrado = false;
        // Preparamos nuestra consulta SQL para insertar el registro en la tabla bitacora
        String sql = "INSERT INTO bitacora (doc_emple_fk, nombre_usuario, accion, detalle) VALUES (?, ?, ?, ?)";
        
        // Utilizamos try-with-resources para manejar el cierre automático de nuestros recursos
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            // Hacemos la asignación de parámetros a nuestra consulta preparada para evitar inyección SQL
            ps.setString(1, docEmple);
            ps.setString(2, nombreUsuario);
            ps.setString(3, accion);
            ps.setString(4, detalle);
            
            // Ejecutamos la inserción y verificamos si logramos afectar al menos una fila
            registrado = ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace(); // Imprimimos el error en consola si nuestra base de datos falla
        }
        return registrado;
    }

    /**
     * En este método, nosotros obtenemos una lista con todos nuestros registros de la bitácora, ordenados desde el más reciente al más antiguo.
     * @return Nuestra lista de objetos Bitacora.
     */
    public List<Bitacora> listarTodas() {
        List<Bitacora> lista = new ArrayList<>();
        // Ejecutamos nuestra consulta SQL: Seleccionamos todo y lo ordenamos por fecha de manera descendente
        String sql = "SELECT * FROM bitacora ORDER BY fecha_hora DESC";
        
        // Aplicamos try-with-resources para manejar nuestra conexión y sentencias
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            // Recorremos todas las filas que obtuvimos de nuestra base de datos
            while (rs.next()) {
                // Creamos un objeto Bitacora por cada fila y lo agregamos a nuestra lista
                lista.add(new Bitacora(
                    rs.getInt("id_bitacora"),
                    rs.getString("doc_emple_fk"),
                    rs.getString("nombre_usuario"),
                    rs.getString("accion"),
                    rs.getString("detalle"),
                    rs.getTimestamp("fecha_hora") // Recuperamos nuestro timestamp completo con fecha y hora
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
