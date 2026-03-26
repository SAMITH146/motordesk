package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PanelMecanicoDAO {

    public List<producto> obtenerStockBajo() {

        List<producto> lista = new ArrayList<>();

        String sql = "SELECT nombre, categoria, stock " +
                "FROM producto WHERE stock < 10";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                producto p = new producto();

                p.setNombre(rs.getString("nombre"));
                p.setcategoria(rs.getString("categoria"));
                p.setstock(rs.getInt("stock"));

                lista.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ===== METODOS PARA LA GRAFICA Y RESUMEN (ATADOS A BD) =====

    public int alturaDia(int diaSemana) {
        double totalDelDia = 0;
        // La meta monetaria diaria para llenar la barra al 100% (puedes cambiarla)
        double maximoEsperado = 1000000.0;

        // diaSemana desde JSP: 1=Lunes, 2=Martes ... 6=Sabado
        // MySQL DAYOFWEEK: 1=Domingo, 2=Lunes, 3=Martes ... 7=Sabado
        int diaMysql = diaSemana + 1;

        // Asumo que la columna de fecha se llama "fecha" (si es otra cambiala aqui
        // abajo)
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM pago WHERE DAYOFWEEK(fecha) = ? AND YEARWEEK(fecha, 1) = YEARWEEK(CURDATE(), 1)";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, diaMysql);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalDelDia = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al intentar sumar la altura del dia. Verifica el nombre de las columnas.");
            e.printStackTrace();
        }

        // Calcula porcentaje de altura
        double porcentaje = (totalDelDia / maximoEsperado) * 100;
        if (porcentaje > 100)
            porcentaje = 100; // Tope maximo de barra 100%

        return (int) Math.round(porcentaje);
    }

    public int totalServicios() {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM pago"; // Conteo basico por ahora
        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
        }
        return total;
    }

    public int totalOrdenes() {
        return totalServicios(); // Mismo total por ahora
    }

    public String totalDinero() {
        double total = 0;
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM pago";
        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
        }

        // Formatea el numero con puntos de miles (ej: 3.500.000)
        return String.format(java.util.Locale.GERMAN, "%,.0f", total);
    }
}