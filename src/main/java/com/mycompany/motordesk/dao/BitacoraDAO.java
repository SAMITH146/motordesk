package com.mycompany.motordesk.dao; // Declara el paquete del DAO para la bitácora

import com.mycompany.motordesk.config.Conexion; // Clase de utilidades para obtener la conexión a la base de datos
import com.mycompany.motordesk.model.Bitacora; // Modelo que representa cada registro de la tabla bitácora
import java.sql.Connection; // Representa una conexión a la base de datos
import java.sql.PreparedStatement; // Permite precompilar sentencias SQL con parámetros
import java.sql.ResultSet; // Permite leer los resultados devueltos por una consulta
import java.util.ArrayList; // Implementación de lista dinámica utilizada para almacenar resultados
import java.util.List; // Interfaz genérica de lista

/**
 * Les presentamos nuestra Clase de Acceso a Datos (DAO) para el registro de la Bitácora.
 * Aquí nosotros nos encargamos de guardar y consultar todo nuestro historial de acciones en el sistema.
 */
public class BitacoraDAO { // Clase DAO que encapsula operaciones CRUD para la entidad Bitacora

    /**
     * Con este método, nosotros registramos una nueva acción en nuestra bitácora del sistema.
     * @param docEmple Documento del empleado que realiza nuestra acción.
     * @param nombreUsuario Nombre de nuestro usuario.
     * @param accion Descripción corta de la acción que realizamos (ej. "LOGIN", "CREAR_ORDEN").
     * @param detalle Descripción detallada de lo que ocurrió en nuestra plataforma.
     * @return true si logramos registrar la acción correctamente, false en caso contrario.
     */
    public boolean registrarAccion(String docEmple, String nombreUsuario, String accion, String detalle) { // Inserta un registro en la tabla bitácora
        boolean registrado = false; // Indicador de éxito de la inserción
        // Preparamos nuestra consulta SQL para insertar el registro en la tabla bitacora
        String sql = "INSERT INTO bitacora (doc_emple_fk, nombre_usuario, accion, detalle) VALUES (?, ?, ?, ?)"; // Sentencia parametrizada para evitar inyección SQL

        // Utilizamos try-with-resources para manejar el cierre automático de nuestros recursos
        try (Connection con = Conexion.getConexion(); // Obtiene conexión a la base de datos
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia con los parámetros

            // Hacemos la asignación de parámetros a nuestra consulta preparada para evitar inyección SQL
            ps.setString(1, docEmple); // Asigna documento del empleado
            ps.setString(2, nombreUsuario); // Asigna nombre del usuario
            ps.setString(3, accion); // Asigna tipo de acción
            ps.setString(4, detalle); // Asigna descripción detallada

            // Ejecutamos la inserción y verificamos si logramos afectar al menos una fila
            registrado = ps.executeUpdate() > 0; // true si se insertó al menos una fila
        } catch (Exception e) {
            e.printStackTrace(); // Imprimimos el error en consola si nuestra base de datos falla
        }
        return registrado; // Devuelve true si la inserción fue exitosa
    }

    /**
     * En este método, nosotros obtenemos una lista con todos nuestros registros de la bitácora, ordenados desde el más reciente al más antiguo.
     * @return Nuestra lista de objetos Bitacora.
     */
    public List<Bitacora> listarTodas() { // Recupera todos los registros de auditoría
        List<Bitacora> lista = new ArrayList<>(); // Lista donde se almacenarán los objetos Bitacora
        // Ejecutamos nuestra consulta SQL: Seleccionamos todo y lo ordenamos por fecha de manera descendente
        String sql = "SELECT * FROM bitacora ORDER BY fecha_hora DESC"; // Orden descendente por fecha_hora

        // Aplicamos try-with-resources para manejar nuestra conexión y sentencias
        try (Connection con = Conexion.getConexion(); // Obtiene conexión a la base de datos
             PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia SELECT
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta y obtiene resultados

            // Recorremos todas las filas que obtuvimos de nuestra base de datos
            while (rs.next()) {
                // Creamos un objeto Bitacora por cada fila y lo agregamos a nuestra lista
                lista.add(new Bitacora(
                    rs.getInt("id_bitacora"), // ID autogenerado de la bitácora
                    rs.getString("doc_emple_fk"), // Documento del empleado que causó el evento (puede ser null)
                    rs.getString("nombre_usuario"), // Nombre del usuario que realizó la acción
                    rs.getString("accion"), // Tipo de acción (p. ej., LOGIN, CREAR)
                    rs.getString("detalle"), // Detalle descriptivo del evento
                    rs.getTimestamp("fecha_hora") // Timestamp con fecha y hora exactas del registro
                ));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime cualquier error de acceso a la base de datos
        }
        return lista; // Devuelve la lista completa de auditorías
    }
}
