// Empezamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.dao;

// A continuación, importamos las dependencias y clases que vamos a necesitar
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// Aquí presentamos nuestra clase pública PanelMecanicoDAO, que nos ayuda a gestionar la lógica correspondiente
/**
 * Como pueden observar, esta es nuestra Clase de Acceso a Datos (DAO) para el Panel del Mecánico.
 * Nosotros la diseñamos para proporcionar información y métricas vitales para el tablero (dashboard) de nuestros mecánicos,
 * permitiéndonos mostrar repuestos con bajo stock o estadísticas de sus órdenes de trabajo.
 */
public class PanelMecanicoDAO {

    /**
     * En este método, obtenemos una lista de productos que tienen un nivel de stock crítico (menor a 5 unidades).
     * @return Retornamos una lista de hasta 5 objetos Producto con stock bajo para nuestra vista.
     */
    public List<Producto> obtenerStockBajo() {
        List<Producto> lista = new ArrayList<>();
        // Preparamos nuestra consulta SQL: Filtramos productos con menos de 5 en stock y limitamos los resultados a 5 (así no sobrecargamos nuestro panel)
        String sql = "SELECT * FROM producto WHERE stock < 5 LIMIT 5";
        
        // Utilizamos try-with-resources porque nos asegura la liberación automática de la conexión a nuestra base de datos
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            // Iniciamos un bucle con el que recorremos las filas que obtuvimos en la BD
            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombreProducto(rs.getString("nombre"));
                p.setStock(rs.getInt("stock")); // Capturamos nuestro nivel de stock actual
                lista.add(p); // Finalmente, lo agregamos a la lista que retornaremos
            }
        } catch (Exception e) {
            e.printStackTrace(); // Si ocurre algo inesperado, imprimimos el error de conexión para depurarlo
        }
        return lista;
    }

    /**
     * Ahora procedemos a contar cuántas órdenes de trabajo en estado 'ABIERTA' tiene asignadas nuestro mecánico.
     * @param docMecanico Documento de identidad del mecánico.
     * @return Devolvemos la cantidad de órdenes abiertas.
     */
    public int contarOrdenesAbiertas(String docMecanico) {
        int total = 0;
        // Configuramos nuestra consulta SQL: Realizamos un conteo (COUNT) filtrando por el documento del mecánico y el estado de la orden
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ? AND estado = 'ABIERTA'";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            // Inyectamos el valor del documento en nuestro marcador '?' de forma segura
            ps.setString(1, docMecanico);
            
            try (ResultSet rs = ps.executeQuery()) {
                // Si nuestra consulta nos arroja el conteo...
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * En esta sección, contamos el número total de órdenes de trabajo que el mecánico tiene asignadas para nuestro día de hoy.
     * @param docMecanico Documento de identidad del mecánico.
     * @return El número de órdenes de nuestro día actual.
     */
    public int contarOrdenesHoy(String docMecanico) {
        int total = 0;
        // Preparamos nuestra consulta SQL: Contamos las órdenes asignadas a nuestro mecánico donde la fecha coincida con la fecha actual del sistema (CURRENT_DATE)
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ? AND fecha = CURRENT_DATE";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            // Asignamos el documento de forma segura a nuestra consulta
            ps.setString(1, docMecanico);
            
            try (ResultSet rs = ps.executeQuery()) {
                // Extraemos el valor devuelto por nuestra función COUNT(*)
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }
}