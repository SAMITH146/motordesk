package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
import com.mycompany.motordesk.model.Factura; // Modelo que representa una factura del sistema
import java.sql.Connection; // Interfaz JDBC para la conexion activa
import java.sql.PreparedStatement; // Sentencia SQL parametrizada para prevenir inyeccion SQL
import java.sql.ResultSet; // Resultado devuelto por la consulta SQL
import java.util.ArrayList; // Implementacion de lista dinamica
import java.util.List; // Interfaz de lista generica

/**
 * Les presentamos nuestra Clase de Acceso a Datos (DAO) para nuestras Facturas.
 * Aqui nosotros gestionamos la generacion y consulta de nuestras facturas de pago en el sistema.
 */
public class FacturaDAO { // DAO que gestiona todas las operaciones de facturacion del taller

    /**
     * Con este metodo, nosotros insertamos una nueva factura en nuestra base de datos.
     * @param f Objeto Factura con la informacion que vamos a guardar.
     * @return true si lo guardamos correctamente, false en caso contrario.
     */
    public boolean insertar(Factura f) { // Guarda una nueva factura en la base de datos
        boolean registrado = false; // Indicador del resultado de la operacion
        // Preparamos nuestra consulta SQL para insertar los datos de nuestra factura
        String sql = "INSERT INTO factura (id_orden_fk, doc_emple_fk, numero_factura, subtotal, iva, total, metodo_pago, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // Inserta la factura vinculada a su orden de trabajo

        try (Connection con = Conexion.getConexion()) { // Abre la conexion a la base de datos

            // Nosotros generamos un numero correlativo si no viene especificado
            // Esto nos asegura que cada factura tenga un numero unico y secuencial
            if (f.getNumeroFactura() == null || f.getNumeroFactura().trim().isEmpty()) { // Si no tiene numero, lo genera automaticamente
                f.setNumeroFactura(generarNumeroFactura(con)); // Genera el numero correlativo (ej. FAC-0001)
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia INSERT
                ps.setInt(1, f.getIdOrdenFk()); // ID de la orden que se esta facturando
                ps.setString(2, f.getDocEmpleFk()); // Documento del empleado (cajero) que emite la factura
                ps.setString(3, f.getNumeroFactura()); // Numero de factura generado o proporcionado
                ps.setDouble(4, f.getSubtotal()); // Subtotal antes del IVA
                ps.setDouble(5, f.getIva()); // Valor del IVA calculado
                ps.setDouble(6, f.getTotal()); // Total final con IVA incluido
                ps.setString(7, f.getMetodoPago()); // Metodo de pago (efectivo, tarjeta, etc.)
                ps.setString(8, f.getEstado() != null ? f.getEstado() : "PAGADA"); // Estado de la factura ('PAGADA' por defecto)

                // Nosotros ejecutamos el insert y verificamos si afectamos alguna fila
                registrado = ps.executeUpdate() > 0; // true si la insercion fue exitosa
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return registrado; // Devuelve true si la factura fue guardada correctamente
    }

    /**
     * Aqui nosotros obtenemos la factura que esta asociada a una orden de trabajo especifica.
     * @param idOrden El ID de nuestra orden de trabajo.
     * @return Objeto Factura si lo encontramos, null en caso contrario.
     */
    public Factura obtenerPorOrden(int idOrden) { // Busca la factura asociada a una orden especifica
        Factura f = null; // Sera null si no se encuentra ninguna factura para la orden
        // Preparamos nuestra consulta SQL para buscar la factura asociada al ID de nuestra orden
        String sql = "SELECT * FROM factura WHERE id_orden_fk = ?"; // Busca por la llave foranea de la orden

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia de busqueda

            ps.setInt(1, idOrden); // Asigna el ID de la orden como filtro

            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la busqueda
                if (rs.next()) { // Si hay una factura asociada a la orden
                    // Mapeo de resultados a objeto Java
                    f = new Factura(
                        rs.getInt("id_factura"), // ID de la factura
                        rs.getInt("id_orden_fk"), // ID de la orden vinculada
                        rs.getString("doc_emple_fk"), // Documento del cajero
                        rs.getString("numero_factura"), // Numero de factura
                        rs.getTimestamp("fecha_emision"), // Fecha de emision
                        rs.getDouble("subtotal"), // Subtotal sin IVA
                        rs.getDouble("iva"), // IVA aplicado
                        rs.getDouble("total"), // Total final
                        rs.getString("metodo_pago"), // Metodo de pago
                        rs.getString("estado") // Estado de la factura
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return f; // Retorna la factura encontrada o null
    }

    /**
     * En esta seccion, nosotros listamos todas las facturas registradas en nuestra base de datos, ordenadas de la mas reciente a la mas antigua.
     * @return Nuestra lista de objetos Factura.
     */
    public List<Factura> listarTodas() { // Retorna todas las facturas del sistema, ordenadas por fecha descendente
        List<Factura> lista = new ArrayList<>(); // Lista que contendra todas las facturas encontradas
        // Armamos nuestra consulta SQL para listar facturas, usando el alias 'f' para acortar el nombre de nuestra tabla
        String sql = "SELECT f.* FROM factura f ORDER BY f.fecha_emision DESC"; // Ordena del mas reciente al mas antiguo

        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia SELECT
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta

            while (rs.next()) { // Recorre todas las filas del resultado
                Factura f = new Factura(
                    rs.getInt("id_factura"), // ID de la factura
                    rs.getInt("id_orden_fk"), // ID de la orden vinculada
                    rs.getString("doc_emple_fk"), // Documento del cajero
                    rs.getString("numero_factura"), // Numero de factura
                    rs.getTimestamp("fecha_emision"), // Fecha de emision
                    rs.getDouble("subtotal"), // Subtotal sin IVA
                    rs.getDouble("iva"), // IVA aplicado
                    rs.getDouble("total"), // Total final
                    rs.getString("metodo_pago"), // Metodo de pago
                    rs.getString("estado") // Estado de la factura
                );
                lista.add(f); // Agrega la factura mapeada a la lista
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return lista; // Devuelve la lista completa de facturas
    }

    /**
     * Finalmente, nosotros generamos un numero de factura correlativo basandonos en la cantidad de facturas que ya existen.
     * Ejemplo: "FAC-0001".
     * @param con Conexion a nuestra base de datos (reutilizamos la de la transaccion actual).
     * @return Nuestro numero de factura generado.
     */
    private String generarNumeroFactura(Connection con) { // Genera un numero correlativo unico para cada factura
        String num = "FAC-0001"; // Valor por defecto que establecemos para nuestra primera factura
        // Preparamos nuestra consulta SQL para contar nuestro total de facturas
        String sql = "SELECT COUNT(*) FROM factura"; // Cuenta las facturas existentes para generar el siguiente numero

        try (PreparedStatement ps = con.prepareStatement(sql); // Prepara el conteo
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta

            if (rs.next()) { // Avanza al primer resultado
                // Nosotros sumamos 1 a nuestro total actual para generar el siguiente numero
                int count = rs.getInt(1) + 1; // El siguiente numero de factura
                // Usando String.format nosotros podemos anadir ceros a la izquierda (ej. 0005)
                num = String.format("FAC-%04d", count); // Formatea con 4 digitos (ej. FAC-0023)
            }
        } catch (Exception ignore) {
            // Si nos topamos con un error, nosotros simplemente retornamos nuestro valor por defecto
        }
        return num; // Retorna el numero de factura generado
    }
}
