// Empezamos definiendo el paquete de nuestro proyecto
package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

// A continuacion, importamos las dependencias y clases que vamos a necesitar
import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
import com.mycompany.motordesk.model.Producto; // Modelo de producto para mostrar repuestos con bajo stock
import java.sql.Connection; // Interfaz JDBC para la conexion activa
import java.sql.PreparedStatement; // Sentencia SQL parametrizada para prevenir inyeccion SQL
import java.sql.ResultSet; // Resultado devuelto por la consulta SQL
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.List; // Interfaz de lista generica

// Aqui presentamos nuestra clase publica PanelMecanicoDAO, que nos ayuda a gestionar la logica correspondiente
/**
 * Como pueden observar, esta es nuestra Clase de Acceso a Datos (DAO) para el Panel del Mecanico.
 * Nosotros la disenamos para proporcionar informacion y metricas vitales para el tablero (dashboard) de nuestros mecanicos,
 * permitiendonos mostrar repuestos con bajo stock o estadisticas de sus ordenes de trabajo.
 */
public class PanelMecanicoDAO { // DAO con consultas especificas del dashboard del mecanico

    /**
     * En este metodo, obtenemos una lista de productos que tienen un nivel de stock critico (menor a 5 unidades).
     * @return Retornamos una lista de hasta 5 objetos Producto con stock bajo para nuestra vista.
     */
    public List<Producto> obtenerStockBajo() { // Retorna los productos con stock critico para alertar al mecanico
        List<Producto> lista = new ArrayList<>(); // Lista que contendra los productos con poco inventario
        // Preparamos nuestra consulta SQL: Filtramos productos con menos de 5 en stock y limitamos los resultados a 5 (asi no sobrecargamos nuestro panel)
        String sql = "SELECT * FROM producto WHERE stock < 5 LIMIT 5"; // Solo 5 resultados para no saturar el dashboard

        // Utilizamos try-with-resources porque nos asegura la liberacion automatica de la conexion a nuestra base de datos
        try (Connection con = Conexion.getConexion(); // Obtiene la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia filtrada
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta de stock critico

            // Iniciamos un bucle con el que recorremos las filas que obtuvimos en la BD
            while (rs.next()) { // Itera por cada producto con stock bajo
                Producto p = new Producto(); // Nuevo objeto producto para cada fila
                p.setIdProducto(rs.getInt("id_producto")); // ID del producto
                p.setNombreProducto(rs.getString("nombre")); // Nombre del repuesto con stock bajo
                p.setStock(rs.getInt("stock")); // Capturamos nuestro nivel de stock actual
                lista.add(p); // Finalmente, lo agregamos a la lista que retornaremos
            }
        } catch (Exception e) {
            e.printStackTrace(); // Si ocurre algo inesperado, imprimimos el error de conexion para depurarlo
        }
        return lista; // Devuelve la lista de productos con stock critico
    }

    /**
     * Ahora procedemos a contar cuantas ordenes de trabajo en estado 'ABIERTA' tiene asignadas nuestro mecanico.
     * @param docMecanico Documento de identidad del mecanico.
     * @return Devolvemos la cantidad de ordenes abiertas.
     */
    public int contarOrdenesAbiertas(String docMecanico) { // Cuenta las ordenes ABIERTAS del mecanico para el dashboard
        int total = 0; // Valor inicial por defecto
        // Configuramos nuestra consulta SQL: Realizamos un conteo (COUNT) filtrando por el documento del mecanico y el estado de la orden
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ? AND estado = 'ABIERTA'"; // Filtra por mecanico y estado ABIERTA

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de conteo

            // Inyectamos el valor del documento en nuestro marcador '?' de forma segura
            ps.setString(1, docMecanico); // Asigna el documento del mecanico como parametro

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta
                // Si nuestra consulta nos arroja el conteo...
                if (rs.next()) total = rs.getInt(1); // Lee el numero de ordenes abiertas asignadas al mecanico
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return total; // Devuelve el total de ordenes abiertas del mecanico
    }

    /**
     * En esta seccion, contamos el numero total de ordenes de trabajo que el mecanico tiene asignadas para nuestro dia de hoy.
     * @param docMecanico Documento de identidad del mecanico.
     * @return El numero de ordenes de nuestro dia actual.
     */
    public int contarOrdenesHoy(String docMecanico) { // Cuenta las ordenes asignadas al mecanico en el dia actual
        int total = 0; // Valor inicial por defecto
        // Preparamos nuestra consulta SQL: Contamos las ordenes asignadas a nuestro mecanico donde la fecha coincida con la fecha actual del sistema (CURRENT_DATE)
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ? AND fecha = CURRENT_DATE"; // CURRENT_DATE es la funcion MySQL para la fecha de hoy

        try (Connection con = Conexion.getConexion(); // Obtiene la conexion a la base de datos
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia filtrada por fecha

            // Asignamos el documento de forma segura a nuestra consulta
            ps.setString(1, docMecanico); // Filtra por el documento del mecanico logueado

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta
                // Extraemos el valor devuelto por nuestra funcion COUNT(*)
                if (rs.next()) total = rs.getInt(1); // Lee el numero de ordenes de hoy del mecanico
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return total; // Devuelve el total de ordenes del dia
    }
}