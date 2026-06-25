// Comenzamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.dao;

// Ahora, importamos las dependencias y clases necesarias para conectarnos a la base de datos
import com.mycompany.motordesk.config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Aquí presentamos nuestra clase pública AdminDAO, la cual gestiona toda nuestra lógica administrativa
/**
 * Como pueden ver, esta es nuestra Clase de Acceso a Datos (DAO) para las operaciones del Administrador.
 * Aquí nosotros gestionamos consultas generales de negocio, como nuestro conteo de mecánicos, productos y órdenes.
 */
public class AdminDAO {

    /**
     * En esta primera función, nosotros contamos la cantidad de mecánicos que tenemos actualmente activos en nuestro sistema.
     * @return El número de nuestros mecánicos activos.
     */
    public int contarMecanicosActivos() {
        int total = 0;
        // Para esto, preparamos nuestra consulta SQL: Filtramos por id_rol_fk = 2 (nuestros Mecánicos) y estado 'ACTIVO'
        String sql = "SELECT COUNT(*) FROM empleado WHERE id_rol_fk = 2 AND estado_empleado = 'ACTIVO'";
        
        // Utilizamos un bloque try-with-resources para asegurarnos de que cerramos nuestra conexión, el PreparedStatement y el ResultSet automáticamente
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            // Si encontramos resultados, extraemos el valor de nuestra primera columna
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            // Si algo sale mal con nuestra base de datos, nosotros imprimimos el error en consola
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Ahora pasamos a este método, donde contamos nuestra cantidad total de productos en el inventario.
     * @return El número total de productos que tenemos.
     */
    public int contarProductos() {
        int total = 0;
        // Lanzamos nuestra consulta SQL: Contamos todos los registros en nuestra tabla producto
        String sql = "SELECT COUNT(*) FROM producto";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            // Extraemos el resultado de nuestro conteo
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * En esta sección, nosotros determinamos cuántos productos se encuentran en nuestro stock crítico (menos de 5 unidades).
     * @return El número de productos con bajo stock en nuestra tienda.
     */
    public int contarStockCritico() {
        int total = 0;
        // Armamos nuestra consulta SQL para filtrar los productos cuya cantidad en stock es menor a 5
        String sql = "SELECT COUNT(*) FROM producto WHERE stock < 5";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            // Obtenemos el resultado de nuestra función agregada COUNT(*)
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Finalmente, con este método contamos nuestro total de órdenes de trabajo registradas en el sistema.
     * @return El número total de nuestras órdenes de trabajo.
     */
    public int contarOrdenesTotales() {
        int total = 0;
        // Ejecutamos nuestra consulta SQL para contar todas las filas en ordentrabajo, sin importar su estado
        String sql = "SELECT COUNT(*) FROM ordentrabajo";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }
}
