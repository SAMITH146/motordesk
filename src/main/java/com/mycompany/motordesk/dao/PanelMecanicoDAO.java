package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PanelMecanicoDAO {

    public List<Producto> obtenerStockBajo() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto WHERE stock < 5 LIMIT 5";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombreProducto(rs.getString("nombre"));
                p.setStock(rs.getInt("stock"));
                lista.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public int contarOrdenesAbiertas(String docMecanico) {
        int total = 0;
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ? AND estado = 'ABIERTA'";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docMecanico);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public int contarOrdenesHoy(String docMecanico) {
        int total = 0;
        // Diagram uses 'fecha' DATE
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ? AND fecha = CURRENT_DATE";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docMecanico);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }
}