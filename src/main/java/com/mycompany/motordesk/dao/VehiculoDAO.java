package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    public int insertar(Vehiculo vehiculo) {
        int idGenerado = -1;
        String sql = "INSERT INTO vehiculo (id_cliente_fk, placa, marca, modelo, anio) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, vehiculo.getIdClienteFk());
            ps.setString(2, vehiculo.getPlaca());
            ps.setString(3, vehiculo.getMarca());
            ps.setString(4, vehiculo.getModelo());
            ps.setInt(5, vehiculo.getAnio());
            
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        vehiculo.setIdVehiculo(idGenerado);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idGenerado;
    }

    public Vehiculo obtenerPorPlaca(String placa) {
        Vehiculo v = null;
        String sql = "SELECT * FROM vehiculo WHERE placa = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = new Vehiculo(
                        rs.getInt("id_vehiculo"),
                        rs.getInt("id_cliente_fk"),
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    public boolean actualizar(Vehiculo vehiculo) {
        String sql = "UPDATE vehiculo SET marca = ?, modelo = ?, anio = ?, id_cliente_fk = ? WHERE placa = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, vehiculo.getMarca());
            ps.setString(2, vehiculo.getModelo());
            ps.setInt(3, vehiculo.getAnio());
            ps.setInt(4, vehiculo.getIdClienteFk());
            ps.setString(5, vehiculo.getPlaca());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Vehiculo> listarPorCliente(int idClienteFk) {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM vehiculo WHERE id_cliente_fk = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idClienteFk);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Vehiculo(
                        rs.getInt("id_vehiculo"),
                        rs.getInt("id_cliente_fk"),
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
