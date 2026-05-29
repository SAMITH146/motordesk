package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDAO {

    public int contarMecanicosActivos() {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM empleado WHERE id_rol_fk = 2 AND estado_empleado = 'ACTIVO'";
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

    public int contarProductos() {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM producto";
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

    public int contarStockCritico() {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM producto WHERE stock < 5";
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

    public int contarOrdenesTotales() {
        int total = 0;
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
