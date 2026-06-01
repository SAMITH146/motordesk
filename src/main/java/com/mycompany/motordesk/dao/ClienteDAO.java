package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public int insertar(Cliente cliente) {
        int idGenerado = -1;
        String sql = "INSERT INTO cliente (nom_cliente, doc_cliente, direccion_cliente) VALUES (?, ?, ?)";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getDocumento());
            ps.setString(3, cliente.getDireccion());
            
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        cliente.setIdCliente(idGenerado);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idGenerado;
    }

    public Cliente obtenerPorDocumento(String documento) {
        Cliente c = null;
        String sql = "SELECT * FROM cliente WHERE doc_cliente = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c = new Cliente(
                        rs.getInt("id_cliente"),
                        rs.getString("nom_cliente"),
                        rs.getString("doc_cliente"),
                        rs.getString("direccion_cliente")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE cliente SET nom_cliente = ?, direccion_cliente = ? WHERE doc_cliente = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getDireccion());
            ps.setString(3, cliente.getDocumento());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nom_cliente"),
                    rs.getString("doc_cliente"),
                    rs.getString("direccion_cliente")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
