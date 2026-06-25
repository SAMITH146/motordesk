package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Factura;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Les presentamos nuestra Clase de Acceso a Datos (DAO) para nuestras Facturas.
 * Aquí nosotros gestionamos la generación y consulta de nuestras facturas de pago en el sistema.
 */
public class FacturaDAO {

    /**
     * Con este método, nosotros insertamos una nueva factura en nuestra base de datos.
     * @param f Objeto Factura con la información que vamos a guardar.
     * @return true si lo guardamos correctamente, false en caso contrario.
     */
    public boolean insertar(Factura f) {
        boolean registrado = false;
        // Preparamos nuestra consulta SQL para insertar los datos de nuestra factura
        String sql = "INSERT INTO factura (id_orden_fk, doc_emple_fk, numero_factura, subtotal, iva, total, metodo_pago, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = Conexion.getConexion()) {
            
            // Nosotros generamos un número correlativo si no viene especificado
            // Esto nos asegura que cada factura tenga un número único y secuencial
            if (f.getNumeroFactura() == null || f.getNumeroFactura().trim().isEmpty()) {
                f.setNumeroFactura(generarNumeroFactura(con));
            }
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, f.getIdOrdenFk());
                ps.setString(2, f.getDocEmpleFk());
                ps.setString(3, f.getNumeroFactura());
                ps.setDouble(4, f.getSubtotal());
                ps.setDouble(5, f.getIva());
                ps.setDouble(6, f.getTotal());
                ps.setString(7, f.getMetodoPago());
                ps.setString(8, f.getEstado() != null ? f.getEstado() : "PAGADA");
                
                // Nosotros ejecutamos el insert y verificamos si afectamos alguna fila
                registrado = ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return registrado;
    }

    /**
     * Aquí nosotros obtenemos la factura que está asociada a una orden de trabajo específica.
     * @param idOrden El ID de nuestra orden de trabajo.
     * @return Objeto Factura si lo encontramos, null en caso contrario.
     */
    public Factura obtenerPorOrden(int idOrden) {
        Factura f = null;
        // Preparamos nuestra consulta SQL para buscar la factura asociada al ID de nuestra orden
        String sql = "SELECT * FROM factura WHERE id_orden_fk = ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idOrden);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Mapeo de resultados a objeto Java
                    f = new Factura(
                        rs.getInt("id_factura"),
                        rs.getInt("id_orden_fk"),
                        rs.getString("doc_emple_fk"),
                        rs.getString("numero_factura"),
                        rs.getTimestamp("fecha_emision"),
                        rs.getDouble("subtotal"),
                        rs.getDouble("iva"),
                        rs.getDouble("total"),
                        rs.getString("metodo_pago"),
                        rs.getString("estado")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    /**
     * En esta sección, nosotros listamos todas las facturas registradas en nuestra base de datos, ordenadas de la más reciente a la más antigua.
     * @return Nuestra lista de objetos Factura.
     */
    public List<Factura> listarTodas() {
        List<Factura> lista = new ArrayList<>();
        // Armamos nuestra consulta SQL para listar facturas, usando el alias 'f' para acortar el nombre de nuestra tabla
        String sql = "SELECT f.* FROM factura f ORDER BY f.fecha_emision DESC";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Factura f = new Factura(
                    rs.getInt("id_factura"),
                    rs.getInt("id_orden_fk"),
                    rs.getString("doc_emple_fk"),
                    rs.getString("numero_factura"),
                    rs.getTimestamp("fecha_emision"),
                    rs.getDouble("subtotal"),
                    rs.getDouble("iva"),
                    rs.getDouble("total"),
                    rs.getString("metodo_pago"),
                    rs.getString("estado")
                );
                lista.add(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Finalmente, nosotros generamos un número de factura correlativo basándonos en la cantidad de facturas que ya existen.
     * Ejemplo: "FAC-0001".
     * @param con Conexión a nuestra base de datos (reutilizamos la de la transacción actual).
     * @return Nuestro número de factura generado.
     */
    private String generarNumeroFactura(Connection con) {
        String num = "FAC-0001"; // Valor por defecto que establecemos para nuestra primera factura
        // Preparamos nuestra consulta SQL para contar nuestro total de facturas
        String sql = "SELECT COUNT(*) FROM factura";
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            if (rs.next()) {
                // Nosotros sumamos 1 a nuestro total actual para generar el siguiente número
                int count = rs.getInt(1) + 1;
                // Usando String.format nosotros podemos añadir ceros a la izquierda (ej. 0005)
                num = String.format("FAC-%04d", count);
            }
        } catch (Exception ignore) {
            // Si nos topamos con un error, nosotros simplemente retornamos nuestro valor por defecto
        }
        return num;
    }
}
