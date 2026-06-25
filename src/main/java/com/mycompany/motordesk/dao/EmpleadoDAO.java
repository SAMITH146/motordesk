// Definición del paquete del proyecto
package com.mycompany.motordesk.dao;

// Importación de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion;
import com.mycompany.motordesk.model.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Bienvenidos a nuestra Clase de Acceso a Datos (DAO) para los Empleados.
 * Aquí nosotros permitimos gestionar el registro de nuestro personal, sus logins, su historial y el control de sus estados.
 */
public class EmpleadoDAO {

    /**
     * En este método, nosotros validamos el acceso de un empleado usando su PIN.
     * @param pin PIN de seguridad de nuestro empleado.
     * @return Objeto Empleado si su PIN es correcto y está ACTIVO, null si no coincide.
     */
    public Empleado loginPorPin(String pin) {

        Empleado emp = null;
        // Obtenemos la conexión física a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) {

            // Definimos nuestra sentencia SQL para ejecutarla en la base de datos
            String sql = "SELECT * FROM empleado WHERE TRIM(pin_acceso) = ? AND estado_empleado = 'ACTIVO'";
            // Declaramos nuestra consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, pin);

            // Usamos nuestro ResultSet para almacenar los resultados
            ResultSet rs = ps.executeQuery();

            // Verificamos si nuestro ResultSet encontró algún registro en la base de datos que coincida exactamente con el PIN proporcionado y cuyo estado sea 'ACTIVO'. Si logró encontrar uno, nosotros empezamos a instanciar un objeto Empleado y a mapearle secuencialmente todas sus propiedades (documento, nombre, rol, cargo) directamente desde las columnas de nuestro resultado.
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

        // Retornamos nuestro valor obtenido
        return emp;
    }

    /**
     * Ahora, nosotros registramos a un nuevo empleado en nuestra base de datos.
     * Primero lo registramos en nuestra tabla histórica y luego en nuestra tabla principal.
     * @param emp Objeto Empleado con los datos que vamos a insertar.
     * @return true si nosotros lo registramos correctamente.
     * @throws Exception Si nos ocurre un problema con las claves o integridad.
     */
    public boolean insertar(Empleado emp) throws Exception {

        boolean registrado = false;

        try (Connection con = Conexion.getConexion()) {

            // PASO 1: Insertar en empleado_historico PRIMERO (lo exige la FK de la BD)
            String sqlHist = "INSERT IGNORE INTO empleado_historico (doc_emple, nom_empleado) VALUES (?, ?)";
            try (PreparedStatement psHist = con.prepareStatement(sqlHist)) {
                psHist.setString(1, emp.getIdEmpleado());
                psHist.setString(2, emp.getNombre());
                psHist.executeUpdate();
                System.out.println("[EmpleadoDAO] empleado_historico -> OK para doc: " + emp.getIdEmpleado());
            }

            // PASO 2: Insertar en empleado (ahora la FK ya está satisfecha)
            String sql = "INSERT INTO empleado "
                    + "(doc_emple, nom_empleado, id_cargo_fk, id_rol_fk, pin_acceso, estado_empleado, fecha_ingreso) "
                    + "VALUES (?, ?, ?, ?, ?, ?, CURDATE())";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, emp.getIdEmpleado());
                ps.setString(2, emp.getNombre());
                ps.setInt(3, emp.getIdCargo());
                ps.setInt(4, emp.getIdRol());
                ps.setString(5, emp.getPin());
                ps.setString(6, "ACTIVO");

                registrado = ps.executeUpdate() > 0;
                System.out.println("[EmpleadoDAO] insertar() -> filas afectadas: " + registrado
                        + " | doc: " + emp.getIdEmpleado() + " | nombre: " + emp.getNombre());
            }
        }
        // Si hay SQLException se propaga al controller para mostrar el error real
        return registrado;
    }

    /**
     * Con este método, nosotros listamos todos nuestros mecánicos (empleados con id_rol_fk = 2).
     * @return Nuestra lista de objetos Empleado que cumplen el rol de mecánico.
     */
    public java.util.List<Empleado> listarMecanicos() {
        java.util.List<Empleado> lista = new java.util.ArrayList<>();
        // Obtenemos la conexión física a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            // Definimos nuestra sentencia SQL para ejecutarla en nuestra base de datos
            String sql = "SELECT * FROM empleado WHERE id_rol_fk = 2";
            // Declaramos nuestra consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            // Usamos nuestro ResultSet para almacenar los resultados del query
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
        // Retornamos nuestra lista obtenida
        return lista;
    }

    /**
     * En esta sección, nosotros cambiamos el estado de un empleado (de ACTIVO a INACTIVO o viceversa).
     * Esto nos resulta útil para suspender el acceso de un empleado sin que tengamos que eliminar sus registros pasados.
     * @param idEmpleado Documento de identidad de nuestro empleado.
     * @return true si logramos actualizar su estado.
     */
    public boolean toggleEstado(String idEmpleado) {
        boolean actualizado = false;
        // Obtenemos la conexión física a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            String sqlSelect = "SELECT estado_empleado FROM empleado WHERE doc_emple = ?";
            // Declaramos nuestra consulta preparada para prevenir inyección SQL
            PreparedStatement psSelect = con.prepareStatement(sqlSelect);
            psSelect.setString(1, idEmpleado);
            // Usamos nuestro ResultSet para almacenar los resultados
            ResultSet rs = psSelect.executeQuery();

            // Validamos a través del ResultSet si el documento de identidad buscado efectivamente pertenece a uno de nuestros empleados registrados. En el caso positivo de hallarlo, nosotros procedemos a leer cuál es su estado actual, determinamos su nuevo valor (si está activo lo pasaremos a inactivo y viceversa), y luego ejecutamos el respectivo Update para cambiar y guardar dicho estado.
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
        // Retornamos el estado de nuestra actualización
        return actualizado;
    }

    /**
     * Aquí, nosotros verificamos si nuestro empleado o mecánico tiene órdenes de trabajo registradas a su nombre.
     * Esto nos permite validar antes de intentar borrar, para evitar errores de integridad referencial.
     * @param docEmple Documento de identidad de nuestro empleado.
     * @return true si tiene órdenes, false si no.
     */
    public boolean tieneOrdenesAsociadas(String docEmple) {
        boolean tiene = false;
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docEmple);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tiene = rs.getInt(1) > 0; // Si el conteo es mayor a 0, tiene órdenes
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tiene;
    }

    /**
     * Ahora, nosotros eliminamos físicamente a un empleado de nuestra base de datos.
     * Nuestra validación de que no tenga datos asociados ya debería haberse hecho,
     * o bien nuestra base de datos la rechazaría por restricciones de foráneas.
     * @param idEmpleado Documento de identidad del empleado que vamos a borrar.
     * @return true si nosotros logramos borrarlo exitosamente.
     */
    public boolean eliminar(String idEmpleado) {
        boolean eliminado = false;
        // Obtenemos la conexión física a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            // Escribimos nuestra sentencia SQL Delete directa
            String sql = "DELETE FROM empleado WHERE doc_emple = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, idEmpleado);
            eliminado = ps.executeUpdate() > 0; // Nosotros retornamos true si afectamos al menos 1 fila
        } catch (Exception e) {
            e.printStackTrace(); // Imprimimos nuestro error si rompemos alguna otra restricción no controlada
        }
        return eliminado;
    }

    /**
     * Con este método, nosotros obtenemos los datos de un empleado específico mediante su documento.
     * @param id Documento de nuestro empleado.
     * @return Objeto Empleado que encontramos, null si no existe.
     */
    public Empleado obtenerPorId(String id) {
        Empleado emp = null;
        // Obtenemos la conexión física a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            // Definimos nuestra sentencia SQL para ejecutar en la base de datos
            String sql = "SELECT * FROM empleado WHERE doc_emple = ?";
            // Declaramos nuestra consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, id);
            // Usamos nuestro ResultSet para almacenar los resultados
            ResultSet rs = ps.executeQuery();
            // Chequeamos si el ResultSet arrojó algún resultado tras buscar el documento del empleado solicitado. Si efectivamente existe un registro coincidente, nosotros comenzamos a instanciar un nuevo objeto Empleado en memoria para mapearle cada una de las columnas (nombre, pin, rol, cargo, estado y fecha de ingreso) extraídas directamente desde nuestra base de datos.
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
        // Retornamos a nuestro empleado encontrado
        return emp;
    }

    /**
     * En este paso, nosotros modificamos la información de un empleado existente y guardamos un histórico de su nombre.
     * @param emp Objeto Empleado con nuestros datos nuevos.
     * @return true si lo modificamos correctamente.
     */
    public boolean actualizar(Empleado emp) {
        boolean actualizado = false;
        // Obtenemos la conexión física a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) {
            // Definimos nuestra sentencia SQL para ejecutarla
            String sql = "UPDATE empleado SET nom_empleado = ?, pin_acceso = ?, id_cargo_fk = ?, id_rol_fk = ? WHERE doc_emple = ?";
            // Declaramos nuestra consulta preparada para prevenir inyección SQL
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getPin());
            ps.setInt(3, emp.getIdCargo());
            ps.setInt(4, emp.getIdRol());
            ps.setString(5, emp.getIdEmpleado());
            actualizado = ps.executeUpdate() > 0;
            System.out.println("[EmpleadoDAO] actualizar() -> filas afectadas: " + actualizado
                    + " | doc: " + emp.getIdEmpleado());
        } catch (Exception e) {
            System.err.println("[EmpleadoDAO] ERROR al actualizar empleado: " + e.getMessage());
            e.printStackTrace();
        }
        // Actualizamos nuestro histórico de forma INDEPENDIENTE
        if (actualizado) {
            try (Connection con2 = Conexion.getConexion()) {
                String sqlHist = "INSERT INTO empleado_historico (doc_emple, nom_empleado) VALUES (?, ?) ON DUPLICATE KEY UPDATE nom_empleado = ?";
                try (PreparedStatement psHist = con2.prepareStatement(sqlHist)) {
                    psHist.setString(1, emp.getIdEmpleado());
                    psHist.setString(2, emp.getNombre());
                    psHist.setString(3, emp.getNombre());
                    psHist.executeUpdate();
                }
            } catch (Exception e) {
                System.err.println("[EmpleadoDAO] AVISO: No se pudo actualizar empleado_historico: " + e.getMessage());
            }
        }
        // Retornamos el resultado de nuestra actualización
        return actualizado;
    }

    /**
     * Aquí, nosotros verificamos si un PIN de acceso ya está siendo utilizado por otro de nuestros empleados.
     * @param pin El PIN que vamos a comprobar.
     * @param excludeDoc El documento del empleado que nosotros estamos editando (para no validarlo contra sí mismo), o null si es nuevo.
     * @return true si el PIN ya existe, false si está libre.
     */
    public boolean existePin(String pin, String excludeDoc) {
        boolean existe = false;
        try (Connection con = Conexion.getConexion()) {
            PreparedStatement ps;
            if (excludeDoc == null) {
                // CREAR: solo verificar si el PIN ya existe en cualquier empleado
                String sql = "SELECT 1 FROM empleado WHERE TRIM(pin_acceso) = TRIM(?)";
                ps = con.prepareStatement(sql);
                ps.setString(1, pin);
            } else {
                // EDITAR: verificar si el PIN existe en otro empleado diferente al actual
                String sql = "SELECT 1 FROM empleado WHERE TRIM(pin_acceso) = TRIM(?) AND doc_emple != ?";
                ps = con.prepareStatement(sql);
                ps.setString(1, pin);
                ps.setString(2, excludeDoc);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                existe = true;
            }
            System.out.println("[EmpleadoDAO] existePin('" + pin + "', excludeDoc='" + excludeDoc + "') -> " + existe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return existe;
    }

    /**
     * Finalmente, nosotros verificamos si un número de documento ya está registrado para otro de nuestros empleados.
     * @param docEmple Número de documento que vamos a buscar.
     * @return true si ya existe en nuestro sistema, false si está disponible.
     */
    public boolean existeDocumento(String docEmple) {
        boolean existe = false;
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM empleado WHERE doc_emple = ?")) {
            ps.setString(1, docEmple);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                existe = true;
            }
            System.out.println("[EmpleadoDAO] existeDocumento('" + docEmple + "') -> " + existe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return existe;
    }
}
