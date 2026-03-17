package com.mycompany.motordesk.dao;

import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class EmpleadoDAO {

    public Empleado loginPorPin(String pin) {

        Empleado emp = null;
        try (Connection con = Conexion.getConexion()) {

            String sql = "SELECT * FROM empleado WHERE TRIM(pin_acceso) = ? AND estado_empleado = 'ACTIVO'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, pin);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                emp = new Empleado();
                emp.setIdEmpleado(rs.getInt("doc_emple"));
                emp.setNombre(rs.getString("nom_empleado"));
                emp.setPin(rs.getString("pin_acceso"));
                emp.setIdRol(rs.getInt("id_rol_fk"));
                emp.setIdCargo(rs.getInt("id_cargo_fk"));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return emp;
    }

    public boolean insertar(Empleado emp) {

        boolean registrado = false;

        try (Connection con = Conexion.getConexion()) {

            String sql = "INSERT INTO empleado "
                    + "(doc_emple, nom_empleado, id_cargo_fk, id_rol_fk, pin_acceso, estado_empleado, fecha_ingreso) "
                    + "VALUES (?, ?, ?, ?, ?, ?, CURDATE())";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setLong(1, emp.getIdEmpleado());
            ps.setString(2, emp.getNombre());
            ps.setInt(3, emp.getIdCargo());
            ps.setInt(4, emp.getIdRol());
            ps.setString(5, emp.getPin());
            ps.setString(6, "ACTIVO");

            registrado = ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return registrado;
    }
}
