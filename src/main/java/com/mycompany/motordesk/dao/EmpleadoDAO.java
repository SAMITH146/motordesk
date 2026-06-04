// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Clase pública EmpleadoDAO que gestiona la lógica correspondiente
public class EmpleadoDAO {

    // Método público 'loginPorPin'
    public Empleado loginPorPin(String pin) {

        Empleado emp = null;
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion()) {

            // Definición de la sentencia SQL para ejecutar en la base de datos
            String sql = "SELECT * FROM empleado WHERE TRIM(pin_acceso) = ? AND estado_empleado = 'ACTIVO'";
            // Declaración de consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, pin);

            // Objeto ResultSet para almacenar los resultados del query de base de datos
            ResultSet rs = ps.executeQuery();

            // Validación condicional
            if (rs.next()) {

                emp = new Empleado();
                emp.setIdEmpleado(rs.getString("doc_emple"));
                emp.setNombre(rs.getString("nom_empleado"));
                emp.setPin(rs.getString("pin_acceso"));
                emp.setIdRol(rs.getInt("id_rol_fk"));
                emp.setIdCargo(rs.getInt("id_cargo_fk"));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Retornar el valor obtenido
        return emp;
    }

    // Método público 'insertar'
    public boolean insertar(Empleado emp) {

        boolean registrado = false;

        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion()) {

            // Definición de la sentencia SQL para ejecutar en la base de datos
            String sql = "INSERT INTO empleado "
                    + "(doc_emple, nom_empleado, id_cargo_fk, id_rol_fk, pin_acceso, estado_empleado, fecha_ingreso) "
                    + "VALUES (?, ?, ?, ?, ?, ?, CURDATE())";

            // Declaración de consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, emp.getIdEmpleado());
            ps.setString(2, emp.getNombre());
            ps.setInt(3, emp.getIdCargo());
            ps.setInt(4, emp.getIdRol());
            ps.setString(5, emp.getPin());
            ps.setString(6, "ACTIVO");

            registrado = ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Retornar el valor obtenido
        return registrado;
    }

    public java.util.List<Empleado> listarMecanicos() {
        java.util.List<Empleado> lista = new java.util.ArrayList<>();
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            // Definición de la sentencia SQL para ejecutar en la base de datos
            String sql = "SELECT * FROM empleado WHERE id_rol_fk = 2";
            // Declaración de consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Empleado emp = new Empleado();
                emp.setIdEmpleado(rs.getString("doc_emple"));
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
        // Retornar el valor obtenido
        return lista;
    }

    // Método público 'toggleEstado'
    public boolean toggleEstado(String idEmpleado) {
        boolean actualizado = false;
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            String sqlSelect = "SELECT estado_empleado FROM empleado WHERE doc_emple = ?";
            // Declaración de consulta preparada para prevenir inyección SQL
            PreparedStatement psSelect = con.prepareStatement(sqlSelect);
            psSelect.setString(1, idEmpleado);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            ResultSet rs = psSelect.executeQuery();

            // Validación condicional
            if (rs.next()) {
                String estadoActual = rs.getString("estado_empleado");
                String nuevoEstado = "ACTIVO".equals(estadoActual) ? "INACTIVO" : "ACTIVO";

                String sqlUpdate = "UPDATE empleado SET estado_empleado = ? WHERE doc_emple = ?";
                // Declaración de consulta preparada para prevenir inyección SQL
                PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                psUpdate.setString(1, nuevoEstado);
                psUpdate.setString(2, idEmpleado);

                actualizado = psUpdate.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return actualizado;
    }

    // Método público 'eliminar'
    public boolean eliminar(String idEmpleado) {
        boolean eliminado = false;
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            // Definición de la sentencia SQL para ejecutar en la base de datos
            String sql = "DELETE FROM empleado WHERE doc_emple = ?";
            // Declaración de consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, idEmpleado);
            eliminado = ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return eliminado;
    }

    // Método público 'obtenerPorId'
    public Empleado obtenerPorId(String id) {
        Empleado emp = null;
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            // Definición de la sentencia SQL para ejecutar en la base de datos
            String sql = "SELECT * FROM empleado WHERE doc_emple = ?";
            // Declaración de consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, id);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            ResultSet rs = ps.executeQuery();
            // Validación condicional
            if (rs.next()) {
                emp = new Empleado();
                emp.setIdEmpleado(rs.getString("doc_emple"));
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
        // Retornar el valor obtenido
        return emp;
    }

    // Método público 'actualizar'
    public boolean actualizar(Empleado emp) {
        boolean actualizado = false;
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            // Definición de la sentencia SQL para ejecutar en la base de datos
            String sql = "UPDATE empleado SET nom_empleado = ?, pin_acceso = ?, id_cargo_fk = ?, id_rol_fk = ? WHERE doc_emple = ?";
            // Declaración de consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getPin());
            ps.setInt(3, emp.getIdCargo());
            ps.setInt(4, emp.getIdRol());
            ps.setString(5, emp.getIdEmpleado());
            actualizado = ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return actualizado;
    }

    // Método público 'existePin'
    public boolean existePin(String pin, String excludeDoc) {
        boolean existe = false;
        // Obtención de la conexión física a la base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            // Definición de la sentencia SQL para ejecutar en la base de datos
            String sql = "SELECT 1 FROM empleado WHERE pin_acceso = ? AND doc_emple != ?";
            // Declaración de consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, pin);
            ps.setString(2, excludeDoc == null ? "" : excludeDoc);
            // Objeto ResultSet para almacenar los resultados del query de base de datos
            ResultSet rs = ps.executeQuery();
            // Validación condicional
            if (rs.next()) {
                existe = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retornar el valor obtenido
        return existe;
    }
}
