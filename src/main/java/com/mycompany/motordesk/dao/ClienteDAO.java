// Este archivo pertenece al paquete "dao" — la capa que habla directamente con MySQL
package com.mycompany.motordesk.dao;

// Importamos la clase que nos da la conexión a MySQL (usuario, contraseña, URL de la BD)
import com.mycompany.motordesk.config.Conexion;
// Importamos el modelo Cliente para poder convertir filas de BD en objetos Java
import com.mycompany.motordesk.model.Cliente;
// Clases estándar de Java para trabajar con bases de datos
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Clase que contiene todos los métodos para leer y escribir clientes en MySQL
public class ClienteDAO {

    // -----------------------------------------------------------------------
    // MÉTODO: insertar — Guarda un cliente nuevo en la base de datos
    // Retorna el ID que MySQL le asignó automáticamente (AUTO_INCREMENT)
    // -----------------------------------------------------------------------
    public int insertar(Cliente cliente) {
        // Empezamos con -1; si algo falla, retornaremos este valor como señal de error
        int idGenerado = -1;

        // La consulta SQL con '?' como marcadores de posición — NUNCA concatenamos texto
        // directamente porque eso abre la puerta a ataques de inyección SQL
        String sql = "INSERT INTO cliente (nom_cliente, doc_cliente, direccion_cliente) VALUES (?, ?, ?)";

        // try-with-resources: Java cierra la conexión automáticamente al terminar, sin importar si hubo error
        // RETURN_GENERATED_KEYS le pide a MySQL que nos devuelva el ID que generó
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Reemplazamos cada '?' con el valor real del cliente
            ps.setString(1, cliente.getNombre());   // Primer '?' = nombre
            ps.setString(2, cliente.getDocumento()); // Segundo '?' = documento
            ps.setString(3, cliente.getDireccion()); // Tercer '?' = dirección

            // executeUpdate() ejecuta el INSERT y retorna cuántas filas afectó (esperamos 1)
            if (ps.executeUpdate() > 0) {
                // Leemos el ID que MySQL generó automáticamente para este nuevo cliente
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1); // Guardamos ese ID en nuestra variable
                        cliente.setIdCliente(idGenerado); // También lo guardamos en el objeto
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Si algo falla, imprimimos el error en la consola del servidor
        }

        return idGenerado; // Devolvemos el ID generado (o -1 si falló)
    }

    // -----------------------------------------------------------------------
    // MÉTODO: obtenerPorDocumento — Busca un cliente por su número de cédula/documento
    // Lo usamos al crear una orden para saber si el cliente ya existe en el sistema
    // -----------------------------------------------------------------------
    public Cliente obtenerPorDocumento(String documento) {
        Cliente c = null; // Si no encontramos nada, retornaremos null

        // Buscamos en la tabla cliente el registro que tenga este documento
        String sql = "SELECT * FROM cliente WHERE doc_cliente = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, documento); // Reemplazamos el '?' con el documento buscado

            try (ResultSet rs = ps.executeQuery()) {
                // Si encontramos una fila que coincide, construimos el objeto Cliente con esos datos
                if (rs.next()) {
                    c = new Cliente(
                        rs.getInt("id_cliente"),          // Leemos la columna id_cliente
                        rs.getString("nom_cliente"),       // Leemos la columna nom_cliente
                        rs.getString("doc_cliente"),       // Leemos la columna doc_cliente
                        rs.getString("direccion_cliente")  // Leemos la columna direccion_cliente
                    );
                }
                // Si no hay ninguna fila, 'c' sigue siendo null — el llamador lo maneja
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c; // Retornamos el objeto Cliente encontrado (o null si no existe)
    }

    // -----------------------------------------------------------------------
    // MÉTODO: actualizar — Modifica los datos de un cliente ya existente en la BD
    // Lo usamos cuando un cliente vuelve pero cambió su nombre o dirección
    // -----------------------------------------------------------------------
    public boolean actualizar(Cliente cliente) {
        // Solo actualizamos nombre y dirección — el documento es nuestra clave de búsqueda (no cambia)
        String sql = "UPDATE cliente SET nom_cliente = ?, direccion_cliente = ? WHERE doc_cliente = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());    // Nuevo nombre
            ps.setString(2, cliente.getDireccion()); // Nueva dirección
            ps.setString(3, cliente.getDocumento()); // Documento para el WHERE (identificador)

            // executeUpdate() > 0 significa que sí se modificó al menos una fila — retornamos true
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Si hubo error, retornamos false
        }
    }

    // -----------------------------------------------------------------------
    // MÉTODO: listarTodos — Trae TODOS los clientes de la BD para mostrarlos en la tabla
    // -----------------------------------------------------------------------
    public List<Cliente> listarTodos() {
        // Creamos una lista vacía donde iremos acumulando los clientes que encontremos
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente"; // Sin filtros — traemos todos los registros

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // Ejecutamos la consulta directamente

            // Recorremos cada fila del resultado con un bucle while
            while (rs.next()) {
                // Por cada fila, creamos un objeto Cliente y lo agregamos a la lista
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

        return lista; // Devolvemos la lista completa de clientes
    }

    // -----------------------------------------------------------------------
    // MÉTODO: obtenerPorId — Busca un cliente por su ID numérico interno de la BD
    // Lo usa la factura: Orden → Vehículo → id_cliente_fk → aquí buscamos el cliente
    // -----------------------------------------------------------------------
    public Cliente obtenerPorId(int id) {
        Cliente c = null; // Valor por defecto: no encontrado

        // El '?' será reemplazado por el ID que nos pasen como parámetro
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id); // Ponemos el ID en el lugar del '?'

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Si encontramos al cliente con ese ID
                    c = new Cliente( // Construimos el objeto con los datos de la BD
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

        return c; // Retornamos el cliente (o null si no existe ese ID)
    }
}
