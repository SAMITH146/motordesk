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
                emp.setIdEmpleado(rs.getLong("doc_emple"));
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

    public java.util.List<Empleado> listarMecanicos() {
        java.util.List<Empleado> lista = new java.util.ArrayList<>();
        try (Connection con = Conexion.getConexion()) {
            String sql = "SELECT * FROM empleado WHERE id_rol_fk = 2";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Empleado emp = new Empleado();
                emp.setIdEmpleado(rs.getLong("doc_emple"));
                emp.setNombre(rs.getString("nom_empleado"));
                emp.setPin(rs.getString("pin_acceso"));
                emp.setIdRol(rs.getInt("id_rol_fk"));
                emp.setIdCargo(rs.getInt("id_cargo_fk"));
                emp.setEstadoEmpleado(rs.getString("estado_empleado"));
                emp.setFechaIngreso(rs.getDate("fecha_ingreso"));
                lista.add(emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean toggleEstado(Long idEmpleado) {
        boolean actualizado = false;
        try (Connection con = Conexion.getConexion()) {
            String sqlSelect = "SELECT estado_empleado FROM empleado WHERE doc_emple = ?";
            PreparedStatement psSelect = con.prepareStatement(sqlSelect);
            psSelect.setLong(1, idEmpleado);
            ResultSet rs = psSelect.executeQuery();

            if (rs.next()) {
                String estadoActual = rs.getString("estado_empleado");
                String nuevoEstado = "ACTIVO".equals(estadoActual) ? "INACTIVO" : "ACTIVO";

                String sqlUpdate = "UPDATE empleado SET estado_empleado = ? WHERE doc_emple = ?";
                PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                psUpdate.setString(1, nuevoEstado);
                psUpdate.setLong(2, idEmpleado);

                actualizado = psUpdate.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return actualizado;
    }

    public boolean eliminar(Long idEmpleado) {
        boolean eliminado = false;
        try (Connection con = Conexion.getConexion()) {
            String sql = "DELETE FROM empleado WHERE doc_emple = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, idEmpleado);
            eliminado = ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eliminado;
    }

    public Empleado obtenerPorId(Long id) {
        Empleado emp = null;
        try (Connection con = Conexion.getConexion()) {
            String sql = "SELECT * FROM empleado WHERE doc_emple = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                emp = new Empleado();
                emp.setIdEmpleado(rs.getLong("doc_emple"));
                emp.setNombre(rs.getString("nom_empleado"));
                emp.setPin(rs.getString("pin_acceso"));
                emp.setIdRol(rs.getInt("id_rol_fk"));
                emp.setIdCargo(rs.getInt("id_cargo_fk"));
                emp.setEstadoEmpleado(rs.getString("estado_empleado"));
                emp.setFechaIngreso(rs.getDate("fecha_ingreso"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emp;
    }

    public boolean actualizar(Empleado emp) {
        boolean actualizado = false;
        try (Connection con = Conexion.getConexion()) {
            String sql = "UPDATE empleado SET nom_empleado = ?, pin_acceso = ?, id_cargo_fk = ?, id_rol_fk = ? WHERE doc_emple = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getPin());
            ps.setInt(3, emp.getIdCargo());
            ps.setInt(4, emp.getIdRol());
            ps.setLong(5, emp.getIdEmpleado());
            actualizado = ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return actualizado;
    }
}
