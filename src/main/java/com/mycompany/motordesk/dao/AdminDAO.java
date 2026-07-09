// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.dao; // Paquete de acceso a datos (DAOs)

// Ahora, importamos las dependencias y clases necesarias para conectarnos a la base de datos
import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
import java.sql.Connection; // Interfaz JDBC para la conexion activa
import java.sql.PreparedStatement; // Sentencia SQL parametrizada para prevenir inyeccion SQL
import java.sql.ResultSet; // Resultado devuelto por la consulta SQL

// Aqui presentamos nuestra clase publica AdminDAO, la cual gestiona toda nuestra logica administrativa
/**
 * Como pueden ver, esta es nuestra Clase de Acceso a Datos (DAO) para las operaciones del Administrador.
 * Aqui nosotros gestionamos consultas generales de negocio, como nuestro conteo de mecanicos, productos y ordenes.
 */
public class AdminDAO { // DAO exclusivo para las consultas de estadisticas del panel de administracion

    /**
     * En esta primera funcion, nosotros contamos la cantidad de mecanicos que tenemos actualmente activos en nuestro sistema.
     * @return El numero de nuestros mecanicos activos.
     */
    public int contarMecanicosActivos() { // Retorna el total de mecanicos en estado ACTIVO para el dashboard
        int total = 0; // Valor inicial por defecto
        // Para esto, preparamos nuestra consulta SQL: Filtramos por id_rol_fk = 2 (nuestros Mecanicos) y estado 'ACTIVO'
        String sql = "SELECT COUNT(*) FROM empleado WHERE id_rol_fk = 2 AND estado_empleado = 'ACTIVO'"; // Solo cuenta empleados con rol mecanico y activos

        // Utilizamos un bloque try-with-resources para asegurarnos de que cerramos nuestra conexion, el PreparedStatement y el ResultSet automaticamente
        try (Connection con = Conexion.getConexion(); // Obtiene la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia COUNT
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta y obtiene el resultado

            // Si encontramos resultados, extraemos el valor de nuestra primera columna
            if (rs.next()) { // Avanza al primer (y unico) resultado del COUNT
                total = rs.getInt(1); // Lee el valor numerico del conteo
            }
        } catch (Exception e) {
            // Si algo sale mal con nuestra base de datos, nosotros imprimimos el error en consola
            e.printStackTrace(); // Imprime el error SQL para diagnostico del servidor
        }
        return total; // Devuelve el numero de mecanicos activos
    }

    /**
     * Ahora pasamos a este metodo, donde contamos nuestra cantidad total de productos en el inventario.
     * @return El numero total de productos que tenemos.
     */
    public int contarProductos() { // Retorna el total de productos registrados en el inventario
        int total = 0; // Valor inicial por defecto
        // Lanzamos nuestra consulta SQL: Contamos todos los registros en nuestra tabla producto
        String sql = "SELECT COUNT(*) FROM producto"; // Cuenta todos los productos sin filtros

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia COUNT
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta

            // Extraemos el resultado de nuestro conteo
            if (rs.next()) { // Avanza al primer resultado
                total = rs.getInt(1); // Lee el valor numerico del conteo total de productos
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return total; // Devuelve el total de productos en inventario
    }

    /**
     * En esta seccion, nosotros determinamos cuantos productos se encuentran en nuestro stock critico (menos de 5 unidades).
     * @return El numero de productos con bajo stock en nuestra tienda.
     */
    public int contarStockCritico() { // Cuenta los productos cuyo stock esta por debajo del minimo aceptable
        int total = 0; // Valor inicial por defecto
        // Armamos nuestra consulta SQL para filtrar los productos cuya cantidad en stock es menor a 5
        String sql = "SELECT COUNT(*) FROM producto WHERE stock < 5"; // Stock critico = menos de 5 unidades

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia filtrada por stock
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta

            // Obtenemos el resultado de nuestra funcion agregada COUNT(*)
            if (rs.next()) { // Avanza al primer resultado
                total = rs.getInt(1); // Lee el numero de productos con stock critico
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return total; // Devuelve el total de productos con stock critico
    }

    /**
     * Finalmente, con este metodo contamos nuestro total de ordenes de trabajo registradas en el sistema.
     * @return El numero total de nuestras ordenes de trabajo.
     */
    public int contarOrdenesTotales() { // Retorna el total de ordenes de trabajo en el sistema
        int total = 0; // Valor inicial por defecto
        // Ejecutamos nuestra consulta SQL para contar todas las filas en ordentrabajo, sin importar su estado
        String sql = "SELECT COUNT(*) FROM ordentrabajo"; // Cuenta todas las ordenes sin filtro de estado

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia COUNT
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta

            if (rs.next()) { // Avanza al primer resultado del COUNT
                total = rs.getInt(1); // Lee el numero total de ordenes de trabajo
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return total; // Devuelve el total de ordenes de trabajo en el sistema
    }
}
