// Definimos el paquete de nuestro proyecto
package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

// Importamos todas las dependencias y clases que vamos a utilizar
import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
import com.mycompany.motordesk.model.Vehiculo; // Modelo que representa un vehiculo registrado en el sistema
import java.sql.Connection; // Interfaz JDBC para la conexion activa
import java.sql.PreparedStatement; // Sentencia SQL parametrizada para prevenir inyeccion SQL
import java.sql.ResultSet; // Resultado devuelto por la consulta SQL
import java.sql.Statement; // Constante utilizada para solicitar el ID generado tras un INSERT
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.List; // Interfaz de lista generica

// En esta parte presentamos nuestra clase publica VehiculoDAO, la cual gestiona toda la logica correspondiente
/**
 * Les presentamos nuestra Clase de Acceso a Datos (DAO) para la gestion de Vehiculos.
 * Nosotros la disenamos para permitirnos registrar, actualizar y buscar los vehiculos asociados a nuestros clientes.
 */
public class VehiculoDAO { // DAO que gestiona los vehiculos vinculados a los clientes del taller

    /**
     * Mediante este metodo, insertamos un nuevo vehiculo en nuestra base de datos y retornamos el ID que se genere.
     * @param vehiculo Objeto Vehiculo con los datos que vamos a registrar.
     * @return El ID asignado por la base de datos (id_vehiculo) o -1 si falla nuestro proceso.
     */
    public int insertar(Vehiculo vehiculo) { // Registra un nuevo vehiculo y retorna el ID generado por MySQL
        int idGenerado = -1; // Valor de error por defecto si la insercion falla
        // Preparamos nuestra consulta SQL con marcadores '?' para prevenir cualquier inyeccion SQL
        String sql = "INSERT INTO vehiculo (id_cliente_fk, placa, marca, modelo, anio, tipo_vehiculo) VALUES (?, ?, ?, ?, ?, ?)"; // Registra el vehiculo vinculado al cliente

        // Le indicamos a MySQL con Statement.RETURN_GENERATED_KEYS que nos devuelva el ID que se ha creado
        try (Connection con = Conexion.getConexion(); // Obtiene la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Solicita el ID autogenerado

            // Asignamos nuestros valores a los parametros
            ps.setInt(1, vehiculo.getIdClienteFk()); // ID del cliente dueno del vehiculo
            ps.setString(2, vehiculo.getPlaca()); // Placa del vehiculo (unica en el sistema)
            ps.setString(3, vehiculo.getMarca()); // Marca del vehiculo (ej. Honda, Toyota)
            ps.setString(4, vehiculo.getModelo()); // Modelo del vehiculo
            ps.setInt(5, vehiculo.getAnio()); // Anio de fabricacion
            ps.setString(6, vehiculo.getTipoVehiculo()); // Tipo: Moto, Carro, etc.

            // Verificamos si nuestra insercion afecto al menos una fila
            if (ps.executeUpdate() > 0) { // true si se inserto correctamente
                // Recuperamos las claves generadas por MySQL
                try (ResultSet rs = ps.getGeneratedKeys()) { // Obtiene el ID autoincremental generado
                    if (rs.next()) { // Si existe un ID generado
                        idGenerado = rs.getInt(1); // Tomamos el primer valor, que es el ID autoincremental
                        vehiculo.setIdVehiculo(idGenerado); // Actualizamos nuestro objeto en la memoria
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // En caso de error (ej. placa duplicada), lo imprimimos en nuestra consola
        }
        return idGenerado; // Devuelve el ID del vehiculo recien insertado o -1 si fallo
    }

    /**
     * En este metodo, buscamos un vehiculo por su numero de placa (recordemos que es unica).
     * @param placa Placa del vehiculo a buscar.
     * @return Objeto Vehiculo si lo encontramos, o null si no existe.
     */
    public Vehiculo obtenerPorPlaca(String placa) { // Busca un vehiculo por su placa unica
        Vehiculo v = null; // Sera null si no se encuentra el vehiculo
        // Definimos la consulta SQL para buscar por placa
        String sql = "SELECT * FROM vehiculo WHERE placa = ?"; // Filtra por la placa (campo unico)

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda

            ps.setString(1, placa); // Asignamos la placa a nuestra consulta

            // Ejecutamos nuestra consulta y recorremos el resultado
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                if (rs.next()) { // Si encontro el vehiculo
                    v = new Vehiculo(
                        rs.getInt("id_vehiculo"), // ID del vehiculo
                        rs.getInt("id_cliente_fk"), // ID del cliente dueno
                        rs.getString("placa"), // Placa del vehiculo
                        rs.getString("marca"), // Marca del vehiculo
                        rs.getString("modelo"), // Modelo del vehiculo
                        rs.getInt("anio") // Anio de fabricacion
                    );
                    v.setTipoVehiculo(rs.getString("tipo_vehiculo")); // Tipo de vehiculo (Moto, Carro, etc.)
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return v; // Devuelve el vehiculo encontrado o null
    }

    /**
     * Aqui actualizamos la informacion de un vehiculo que ya existe.
     * Usamos la placa como nuestro criterio de busqueda (WHERE placa = ?).
     * @param vehiculo Objeto Vehiculo con los datos que hemos actualizado.
     * @return true si logramos actualizarlo correctamente, false si fallo.
     */
    public boolean actualizar(Vehiculo vehiculo) { // Actualiza los datos de un vehiculo existente
        // Preparamos nuestra consulta SQL de actualizacion
        String sql = "UPDATE vehiculo SET marca = ?, modelo = ?, anio = ?, id_cliente_fk = ?, tipo_vehiculo = ? WHERE placa = ?"; // Actualiza todos los campos usando la placa como clave

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el UPDATE

            ps.setString(1, vehiculo.getMarca()); // Nueva marca del vehiculo
            ps.setString(2, vehiculo.getModelo()); // Nuevo modelo del vehiculo
            ps.setInt(3, vehiculo.getAnio()); // Nuevo anio del vehiculo
            ps.setInt(4, vehiculo.getIdClienteFk()); // ID del cliente (por si cambia de dueno)
            ps.setString(5, vehiculo.getTipoVehiculo()); // Nuevo tipo de vehiculo
            ps.setString(6, vehiculo.getPlaca()); // Colocamos la placa en nuestra clausula WHERE

            // Retornamos true si modificamos al menos una fila
            return ps.executeUpdate() > 0; // true si la actualizacion fue exitosa
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
            return false; // Retorna false si la actualizacion fallo
        }
    }

    /**
     * Con esto, obtenemos una lista de todos los vehiculos que pertenecen a uno de nuestros clientes en particular.
     * @param idClienteFk ID interno del cliente (foranea).
     * @return Lista de vehiculos del cliente que encontramos.
     */
    public List<Vehiculo> listarPorCliente(int idClienteFk) { // Lista todos los vehiculos de un cliente especifico
        List<Vehiculo> lista = new ArrayList<>(); // Lista que contendra los vehiculos del cliente
        // Formulamos la consulta SQL filtrando por id_cliente_fk
        String sql = "SELECT * FROM vehiculo WHERE id_cliente_fk = ?"; // Filtra por la llave foranea del cliente

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda

            ps.setInt(1, idClienteFk); // Asigna el ID del cliente como filtro

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta
                // Iteramos sobre todos los vehiculos que logramos encontrar
                while (rs.next()) { // Itera por cada vehiculo del cliente
                    Vehiculo v = new Vehiculo(
                        rs.getInt("id_vehiculo"), // ID del vehiculo
                        rs.getInt("id_cliente_fk"), // ID del cliente dueno
                        rs.getString("placa"), // Placa del vehiculo
                        rs.getString("marca"), // Marca del vehiculo
                        rs.getString("modelo"), // Modelo del vehiculo
                        rs.getInt("anio") // Anio de fabricacion
                    );
                    v.setTipoVehiculo(rs.getString("tipo_vehiculo")); // Tipo de vehiculo
                    lista.add(v); // Y los vamos agregando a nuestra lista
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return lista; // Devuelve la lista de vehiculos del cliente
    }

    /**
     * En este paso, buscamos y obtenemos un vehiculo usando su ID primario numerico (el autoincremental).
     * @param id Identificador unico del vehiculo en nuestra base de datos.
     * @return Objeto Vehiculo que hemos encontrado, o null si no logramos ubicarlo.
     */
    public Vehiculo obtenerPorId(int id) { // Busca un vehiculo especifico por su ID primario
        Vehiculo v = null; // Sera null si no se encuentra el vehiculo
        // Construimos nuestra consulta SQL para buscar por id_vehiculo
        String sql = "SELECT * FROM vehiculo WHERE id_vehiculo = ?"; // Filtra por llave primaria del vehiculo

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda

            ps.setInt(1, id); // Asignamos el parametro de nuestro ID

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                if (rs.next()) { // Si el registro existe, pasamos a crear nuestro objeto
                    v = new Vehiculo(
                        rs.getInt("id_vehiculo"), // ID del vehiculo
                        rs.getInt("id_cliente_fk"), // ID del cliente dueno
                        rs.getString("placa"), // Placa del vehiculo
                        rs.getString("marca"), // Marca del vehiculo
                        rs.getString("modelo"), // Modelo del vehiculo
                        rs.getInt("anio") // Anio de fabricacion
                    );
                    v.setTipoVehiculo(rs.getString("tipo_vehiculo")); // Tipo de vehiculo
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return v; // Devuelve el vehiculo encontrado o null
    }
}
