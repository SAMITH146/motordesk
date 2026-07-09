// Comenzamos con nuestro archivo que pertenece al paquete "dao" — la capa donde nosotros nos comunicamos directamente con MySQL
package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

// Importamos la clase que nos brinda la conexion a nuestra base de datos MySQL
import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
// Importamos nuestro modelo Cliente para poder convertir las filas de nuestra BD en objetos Java
import com.mycompany.motordesk.model.Cliente; // Modelo que representa a un cliente del taller
// A continuacion, importamos las clases estandar de Java para trabajar con nuestras bases de datos
import java.sql.Connection; // Interfaz JDBC para la conexion activa
import java.sql.PreparedStatement; // Sentencia SQL parametrizada para prevenir inyeccion SQL
import java.sql.ResultSet; // Resultado devuelto por la consulta SQL
import java.sql.Statement; // Constante para solicitar el ID generado tras un INSERT
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.List; // Interfaz de lista generica

/**
 * En esta parte, tenemos nuestra Clase de Acceso a Datos (DAO) para los Clientes.
 * Aqui definimos los metodos para que nosotros podamos leer, insertar y actualizar nuestros registros de clientes.
 */
public class ClienteDAO { // DAO que gestiona todas las operaciones sobre la tabla cliente

    /**
     * Con este metodo, nosotros guardamos un cliente nuevo en nuestra base de datos.
     * @param cliente Objeto Cliente con los datos que vamos a insertar.
     * @return El ID numerico que MySQL nos asigna, o -1 si tenemos algun fallo.
     */
    public int insertar(Cliente cliente) { // Registra un nuevo cliente y devuelve el ID generado
        // Inicializamos con -1; si algo nos falla, retornaremos este valor como senal de error
        int idGenerado = -1; // -1 indica que la insercion fallo

        // Escribimos nuestra consulta SQL usando '?' como marcadores de posicion — nosotros NUNCA concatenamos texto
        // directamente porque queremos proteger nuestro sistema de ataques de inyeccion SQL
        String sql = "INSERT INTO cliente (nom_cliente, doc_cliente, direccion_cliente) VALUES (?, ?, ?)"; // Inserta el nuevo cliente en la tabla

        // Con try-with-resources, Java nos cierra la conexion automaticamente al terminar
        // Con RETURN_GENERATED_KEYS, nosotros le pedimos a MySQL que nos devuelva el ID que acaba de generar
        try (Connection con = Conexion.getConexion(); // Abre la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Solicita el ID autogenerado

            // Reemplazamos cada '?' con el valor real de nuestro cliente
            ps.setString(1, cliente.getNombre());   // Nuestro primer '?' es el nombre del cliente
            ps.setString(2, cliente.getDocumento()); // Nuestro segundo '?' es el documento del cliente
            ps.setString(3, cliente.getDireccion()); // Nuestro tercer '?' es la direccion del cliente

            // Ejecutamos nuestro INSERT y verificamos cuantas filas afectamos (esperamos que sea 1)
            if (ps.executeUpdate() > 0) { // Verifica que la insercion fue exitosa
                // Procedemos a leer el ID que MySQL nos genero automaticamente
                try (ResultSet rs = ps.getGeneratedKeys()) { // Obtiene el ID autoincremental generado
                    if (rs.next()) { // Si hay un ID generado
                        idGenerado = rs.getInt(1); // Guardamos ese ID en nuestra variable
                        cliente.setIdCliente(idGenerado); // Y tambien lo guardamos en nuestro objeto para usarlo despues
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Si algo nos falla, imprimimos el error en la consola de nuestro servidor
        }

        return idGenerado; // Finalmente, devolvemos nuestro ID generado (o -1 si algo salio mal)
    }

    /**
     * Aqui nosotros buscamos un cliente usando su numero de cedula o documento.
     * Esto nos resulta esencial al crear una orden para verificar si nuestro cliente ya existe en el sistema.
     * @param documento Documento de identidad de nuestro cliente.
     * @return El objeto Cliente que encontramos, o null si no existe en nuestros registros.
     */
    public Cliente obtenerPorDocumento(String documento) { // Busca un cliente por su numero de documento
        Cliente c = null; // Si no encontramos a nadie, nosotros retornaremos null

        // Buscamos en nuestra tabla cliente el registro que coincida con este documento
        String sql = "SELECT * FROM cliente WHERE doc_cliente = ?"; // Filtra por el documento del cliente

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda

            ps.setString(1, documento); // Reemplazamos nuestro '?' con el documento que estamos buscando

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda del cliente
                // Si encontramos una coincidencia, nosotros construimos nuestro objeto Cliente con esos datos
                if (rs.next()) { // Solo mapea si el cliente fue encontrado
                    c = new Cliente(
                        rs.getInt("id_cliente"),          // Leemos nuestra columna id_cliente
                        rs.getString("nom_cliente"),       // Leemos nuestra columna nom_cliente
                        rs.getString("doc_cliente"),       // Leemos nuestra columna doc_cliente
                        rs.getString("direccion_cliente")  // Leemos nuestra columna direccion_cliente
                    );
                }
                // Si no hay filas, nuestra 'c' sigue siendo null — y asi lo manejamos al retornar
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }

        return c; // Retornamos el objeto Cliente que encontramos (o null si no lo hallamos)
    }

    /**
     * En este paso, nosotros modificamos los datos de un cliente que ya tenemos en nuestra base de datos.
     * Por ejemplo, nos sirve si nuestro cliente regresa pero cambio de direccion.
     * @param cliente Objeto Cliente con la informacion que acabamos de actualizar.
     * @return true si logramos la actualizacion con exito, false en caso contrario.
     */
    public boolean actualizar(Cliente cliente) { // Actualiza los datos de un cliente existente
        // Nosotros solo actualizamos el nombre y la direccion — el documento es nuestra clave de busqueda
        String sql = "UPDATE cliente SET nom_cliente = ?, direccion_cliente = ? WHERE doc_cliente = ?"; // Actualiza nombre y direccion del cliente

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el UPDATE

            ps.setString(1, cliente.getNombre());    // El nuevo nombre que ingresamos
            ps.setString(2, cliente.getDireccion()); // La nueva direccion que ingresamos
            ps.setString(3, cliente.getDocumento()); // El documento para nuestro WHERE (clave de busqueda)

            // Si ps.executeUpdate() > 0, significa que nosotros modificamos al menos una fila, asi que retornamos true
            return ps.executeUpdate() > 0; // true si la actualizacion fue exitosa
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
            return false; // Si tuvimos un error, nosotros retornamos false
        }
    }

    /**
     * Aqui, nosotros obtenemos una lista con TODOS los clientes que tenemos registrados en la base de datos.
     * @return Nuestra lista completa de clientes.
     */
    public List<Cliente> listarTodos() { // Retorna la lista completa de clientes del taller
        // Empezamos creando una lista vacia donde nosotros iremos acumulando los clientes que encontremos
        List<Cliente> lista = new ArrayList<>(); // Lista que contendra todos los clientes
        String sql = "SELECT * FROM cliente"; // Sin filtros — nosotros traemos todos los registros

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql); // Prepara el SELECT sin filtros
             ResultSet rs = ps.executeQuery()) { // Ejecutamos nuestra consulta directamente

            // Recorremos cada fila de nuestro resultado con un bucle while
            while (rs.next()) { // Itera por cada cliente en el resultado
                // Por cada fila, nosotros creamos un objeto Cliente y lo agregamos a nuestra lista
                lista.add(new Cliente(
                    rs.getInt("id_cliente"), // ID del cliente
                    rs.getString("nom_cliente"), // Nombre del cliente
                    rs.getString("doc_cliente"), // Documento del cliente
                    rs.getString("direccion_cliente") // Direccion del cliente
                ));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }

        return lista; // Devolvemos nuestra lista completa de clientes
    }

    /**
     * Mediante este metodo, nosotros buscamos un cliente usando su ID numerico interno.
     * Lo usamos internamente (ej. Factura -> Orden -> Vehiculo -> id_cliente_fk).
     * @param id Identificador numerico de nuestro cliente.
     * @return El objeto Cliente que encontramos, o null si no lo hallamos.
     */
    public Cliente obtenerPorId(int id) { // Busca un cliente por su ID numerico interno
        Cliente c = null; // Por defecto asumimos que no lo encontramos

        // Reemplazaremos nuestro '?' por el ID que nos pasen como parametro
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?"; // Filtra por la llave primaria del cliente

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda

            ps.setInt(1, id); // Colocamos nuestro ID en el lugar del '?'

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                if (rs.next()) { // Si nosotros encontramos al cliente con ese ID
                    c = new Cliente( // Construimos nuestro objeto con los datos de la BD
                        rs.getInt("id_cliente"), // ID del cliente
                        rs.getString("nom_cliente"), // Nombre del cliente
                        rs.getString("doc_cliente"), // Documento del cliente
                        rs.getString("direccion_cliente") // Direccion del cliente
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }

        return c; // Retornamos a nuestro cliente (o null si ese ID no existe)
    }
}
