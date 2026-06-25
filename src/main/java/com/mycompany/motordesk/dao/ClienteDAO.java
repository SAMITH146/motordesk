// Comenzamos con nuestro archivo que pertenece al paquete "dao" — la capa donde nosotros nos comunicamos directamente con MySQL
package com.mycompany.motordesk.dao;

// Importamos la clase que nos brinda la conexión a nuestra base de datos MySQL
import com.mycompany.motordesk.config.Conexion;
// Importamos nuestro modelo Cliente para poder convertir las filas de nuestra BD en objetos Java
import com.mycompany.motordesk.model.Cliente;
// A continuación, importamos las clases estándar de Java para trabajar con nuestras bases de datos
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * En esta parte, tenemos nuestra Clase de Acceso a Datos (DAO) para los Clientes.
 * Aquí definimos los métodos para que nosotros podamos leer, insertar y actualizar nuestros registros de clientes.
 */
public class ClienteDAO {

    /**
     * Con este método, nosotros guardamos un cliente nuevo en nuestra base de datos.
     * @param cliente Objeto Cliente con los datos que vamos a insertar.
     * @return El ID numérico que MySQL nos asigna, o -1 si tenemos algún fallo.
     */
    public int insertar(Cliente cliente) {
        // Inicializamos con -1; si algo nos falla, retornaremos este valor como señal de error
        int idGenerado = -1;

        // Escribimos nuestra consulta SQL usando '?' como marcadores de posición — nosotros NUNCA concatenamos texto
        // directamente porque queremos proteger nuestro sistema de ataques de inyección SQL
        String sql = "INSERT INTO cliente (nom_cliente, doc_cliente, direccion_cliente) VALUES (?, ?, ?)";

        // Con try-with-resources, Java nos cierra la conexión automáticamente al terminar
        // Con RETURN_GENERATED_KEYS, nosotros le pedimos a MySQL que nos devuelva el ID que acaba de generar
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Reemplazamos cada '?' con el valor real de nuestro cliente
            ps.setString(1, cliente.getNombre());   // Nuestro primer '?' es el nombre
            ps.setString(2, cliente.getDocumento()); // Nuestro segundo '?' es el documento
            ps.setString(3, cliente.getDireccion()); // Nuestro tercer '?' es la dirección

            // Ejecutamos nuestro INSERT y verificamos cuántas filas afectamos (esperamos que sea 1)
            if (ps.executeUpdate() > 0) {
                // Procedemos a leer el ID que MySQL nos generó automáticamente
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1); // Guardamos ese ID en nuestra variable
                        cliente.setIdCliente(idGenerado); // Y también lo guardamos en nuestro objeto
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Si algo nos falla, imprimimos el error en la consola de nuestro servidor
        }

        return idGenerado; // Finalmente, devolvemos nuestro ID generado (o -1 si algo salió mal)
    }

    /**
     * Aquí nosotros buscamos un cliente usando su número de cédula o documento.
     * Esto nos resulta esencial al crear una orden para verificar si nuestro cliente ya existe en el sistema.
     * @param documento Documento de identidad de nuestro cliente.
     * @return El objeto Cliente que encontramos, o null si no existe en nuestros registros.
     */
    public Cliente obtenerPorDocumento(String documento) {
        Cliente c = null; // Si no encontramos a nadie, nosotros retornaremos null

        // Buscamos en nuestra tabla cliente el registro que coincida con este documento
        String sql = "SELECT * FROM cliente WHERE doc_cliente = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, documento); // Reemplazamos nuestro '?' con el documento que estamos buscando

            try (ResultSet rs = ps.executeQuery()) {
                // Si encontramos una coincidencia, nosotros construimos nuestro objeto Cliente con esos datos
                if (rs.next()) {
                    c = new Cliente(
                        rs.getInt("id_cliente"),          // Leemos nuestra columna id_cliente
                        rs.getString("nom_cliente"),       // Leemos nuestra columna nom_cliente
                        rs.getString("doc_cliente"),       // Leemos nuestra columna doc_cliente
                        rs.getString("direccion_cliente")  // Leemos nuestra columna direccion_cliente
                    );
                }
                // Si no hay filas, nuestra 'c' sigue siendo null — y así lo manejamos al retornar
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c; // Retornamos el objeto Cliente que encontramos (o null si no lo hallamos)
    }

    /**
     * En este paso, nosotros modificamos los datos de un cliente que ya tenemos en nuestra base de datos.
     * Por ejemplo, nos sirve si nuestro cliente regresa pero cambió de dirección.
     * @param cliente Objeto Cliente con la información que acabamos de actualizar.
     * @return true si logramos la actualización con éxito, false en caso contrario.
     */
    public boolean actualizar(Cliente cliente) {
        // Nosotros solo actualizamos el nombre y la dirección — el documento es nuestra clave de búsqueda
        String sql = "UPDATE cliente SET nom_cliente = ?, direccion_cliente = ? WHERE doc_cliente = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());    // El nuevo nombre que ingresamos
            ps.setString(2, cliente.getDireccion()); // La nueva dirección que ingresamos
            ps.setString(3, cliente.getDocumento()); // El documento para nuestro WHERE

            // Si ps.executeUpdate() > 0, significa que nosotros modificamos al menos una fila, así que retornamos true
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Si tuvimos un error, nosotros retornamos false
        }
    }

    /**
     * Aquí, nosotros obtenemos una lista con TODOS los clientes que tenemos registrados en la base de datos.
     * @return Nuestra lista completa de clientes.
     */
    public List<Cliente> listarTodos() {
        // Empezamos creando una lista vacía donde nosotros iremos acumulando los clientes que encontremos
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente"; // Sin filtros — nosotros traemos todos los registros

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // Ejecutamos nuestra consulta directamente

            // Recorremos cada fila de nuestro resultado con un bucle while
            while (rs.next()) {
                // Por cada fila, nosotros creamos un objeto Cliente y lo agregamos a nuestra lista
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

        return lista; // Devolvemos nuestra lista completa de clientes
    }

    /**
     * Mediante este método, nosotros buscamos un cliente usando su ID numérico interno.
     * Lo usamos internamente (ej. Factura -> Orden -> Vehículo -> id_cliente_fk).
     * @param id Identificador numérico de nuestro cliente.
     * @return El objeto Cliente que encontramos, o null si no lo hallamos.
     */
    public Cliente obtenerPorId(int id) {
        Cliente c = null; // Por defecto asumimos que no lo encontramos

        // Reemplazaremos nuestro '?' por el ID que nos pasen como parámetro
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id); // Colocamos nuestro ID en el lugar del '?'

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Si nosotros encontramos al cliente con ese ID
                    c = new Cliente( // Construimos nuestro objeto con los datos de la BD
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

        return c; // Retornamos a nuestro cliente (o null si ese ID no existe)
    }
}
