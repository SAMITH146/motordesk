// Definicion del paquete del proyecto
package com.mycompany.motordesk.dao; // Paquete de acceso a datos del sistema MotorDesk

// Importacion de dependencias y clases necesarias
import com.mycompany.motordesk.config.Conexion; // Clase que provee la conexion a MySQL
import com.mycompany.motordesk.model.Empleado; // Modelo que representa a un empleado del taller
import java.sql.Connection; // Interfaz JDBC para la conexion activa
import java.sql.PreparedStatement; // Sentencia SQL parametrizada para prevenir inyeccion SQL
import java.sql.ResultSet; // Resultado devuelto por la consulta SQL

/**
 * Bienvenidos a nuestra Clase de Acceso a Datos (DAO) para los Empleados.
 * Aqui nosotros permitimos gestionar el registro de nuestro personal, sus logins, su historial y el control de sus estados.
 */
public class EmpleadoDAO { // DAO que gestiona todo lo relacionado con los empleados del taller

    /**
     * En este metodo, nosotros validamos el acceso de un empleado usando su PIN.
     * @param pin PIN de seguridad de nuestro empleado.
     * @return Objeto Empleado si su PIN es correcto y esta ACTIVO, null si no coincide.
     */
    public Empleado loginPorPin(String pin) { // Autentica al empleado por su PIN para la pantalla de login

        Empleado emp = null; // Sera null si el PIN no coincide o el empleado esta inactivo
        // Obtenemos la conexion fisica a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) { // Abre la conexion a la base de datos

            // Definimos nuestra sentencia SQL para ejecutarla en la base de datos
            String sql = "SELECT * FROM empleado WHERE TRIM(pin_acceso) = ? AND estado_empleado = 'ACTIVO'"; // TRIM elimina espacios y solo trae empleados activos
            // Declaramos nuestra consulta preparada para prevenir inyeccion SQL
            PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia de autenticacion
            ps.setString(1, pin); // Asigna el PIN ingresado como parametro

            // Usamos nuestro ResultSet para almacenar los resultados
            ResultSet rs = ps.executeQuery(); // Ejecuta la busqueda del empleado por PIN

            // Verificamos si nuestro ResultSet encontro algun registro en la base de datos que coincida exactamente con el PIN proporcionado y cuyo estado sea 'ACTIVO'. Si logro encontrar uno, nosotros empezamos a instanciar un objeto Empleado y a mapearle secuencialmente todas sus propiedades (documento, nombre, rol, cargo) directamente desde las columnas de nuestro resultado.
            if (rs.next()) { // Si el PIN coincide con un empleado activo

                emp = new Empleado(); // Crea el objeto empleado para la sesion
                emp.setIdEmpleado(rs.getString("doc_emple")); // Documento de identidad del empleado
                emp.setNombre(rs.getString("nom_empleado")); // Nombre del empleado para mostrar en la sesion
                emp.setPin(rs.getString("pin_acceso")); // PIN almacenado en la base de datos
                emp.setIdRol(rs.getInt("id_rol_fk")); // Rol del empleado (1=Admin, 2=Mecanico)
                emp.setIdCargo(rs.getInt("id_cargo_fk")); // Cargo del empleado en el taller

            }

        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }

        // Retornamos nuestro valor obtenido
        return emp; // Devuelve el empleado autenticado o null si el PIN fue incorrecto
    }

    /**
     * Ahora, nosotros registramos a un nuevo empleado en nuestra base de datos.
     * Primero lo registramos en nuestra tabla historica y luego en nuestra tabla principal.
     * @param emp Objeto Empleado con los datos que vamos a insertar.
     * @return true si nosotros lo registramos correctamente.
     * @throws Exception Si nos ocurre un problema con las claves o integridad.
     */
    public boolean insertar(Empleado emp) throws Exception { // Registra un nuevo empleado en el sistema

        boolean registrado = false; // Indicador del resultado de la insercion

        try (Connection con = Conexion.getConexion()) { // Abre la conexion a la base de datos

            // PASO 1: Insertar en empleado_historico PRIMERO (lo exige la FK de la BD)
            String sqlHist = "INSERT IGNORE INTO empleado_historico (doc_emple, nom_empleado) VALUES (?, ?)"; // INSERT IGNORE evita error si ya existe el registro historico
            try (PreparedStatement psHist = con.prepareStatement(sqlHist)) { // Prepara la insercion en el historico
                psHist.setString(1, emp.getIdEmpleado()); // Documento del empleado para el historico
                psHist.setString(2, emp.getNombre()); // Nombre del empleado para el historico
                psHist.executeUpdate(); // Inserta en el historico (necesario para satisfacer la FK)
                System.out.println("[EmpleadoDAO] empleado_historico -> OK para doc: " + emp.getIdEmpleado()); // Log de confirmacion
            }

            // PASO 2: Insertar en empleado (ahora la FK ya esta satisfecha)
            String sql = "INSERT INTO empleado " // Inserta el empleado en la tabla principal
                    + "(doc_emple, nom_empleado, id_cargo_fk, id_rol_fk, pin_acceso, estado_empleado, fecha_ingreso) "
                    + "VALUES (?, ?, ?, ?, ?, ?, CURDATE())"; // CURDATE() asigna automaticamente la fecha de ingreso de hoy

            try (PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la insercion del empleado
                ps.setString(1, emp.getIdEmpleado()); // Documento del empleado (llave primaria)
                ps.setString(2, emp.getNombre()); // Nombre del empleado
                ps.setInt(3, emp.getIdCargo()); // Cargo en el taller
                ps.setInt(4, emp.getIdRol()); // Rol del empleado (1=Admin, 2=Mecanico)
                ps.setString(5, emp.getPin()); // PIN de acceso al sistema
                ps.setString(6, "ACTIVO"); // Todo empleado nuevo comienza en estado ACTIVO

                registrado = ps.executeUpdate() > 0; // true si la insercion fue exitosa
                System.out.println("[EmpleadoDAO] insertar() -> filas afectadas: " + registrado
                        + " | doc: " + emp.getIdEmpleado() + " | nombre: " + emp.getNombre()); // Log de confirmacion
            }
        }
        // Si hay SQLException se propaga al controller para mostrar el error real
        return registrado; // Devuelve true si el empleado fue registrado correctamente
    }

    /**
     * Con este metodo, nosotros listamos todos nuestros mecanicos (empleados con id_rol_fk = 2).
     * @return Nuestra lista de objetos Empleado que cumplen el rol de mecanico.
     */
    public java.util.List<Empleado> listarMecanicos() { // Retorna la lista de todos los mecanicos del taller
        java.util.List<Empleado> lista = new java.util.ArrayList<>(); // Lista que contendra los mecanicos
        // Obtenemos la conexion fisica a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) { // Abre la conexion
            // Definimos nuestra sentencia SQL para ejecutarla en nuestra base de datos
            String sql = "SELECT * FROM empleado WHERE id_rol_fk = 2"; // Filtra solo empleados con rol Mecanico (id_rol_fk=2)
            // Declaramos nuestra consulta preparada para prevenir inyeccion SQL
            PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia SELECT de mecanicos
            // Usamos nuestro ResultSet para almacenar los resultados del query
            ResultSet rs = ps.executeQuery(); // Ejecuta la consulta de listado
            while (rs.next()) { // Itera por cada mecanico en el resultado
                Empleado emp = new Empleado(); // Nuevo objeto mecanico para cada fila
                emp.setIdEmpleado(rs.getString("doc_emple")); // Documento del mecanico
                emp.setNombre(rs.getString("nom_empleado")); // Nombre del mecanico
                emp.setPin(rs.getString("pin_acceso")); // PIN de acceso del mecanico
                emp.setIdRol(rs.getInt("id_rol_fk")); // Rol (siempre 2=Mecanico en este metodo)
                emp.setIdCargo(rs.getInt("id_cargo_fk")); // Cargo especifico del mecanico
                emp.setEstadoEmpleado(rs.getString("estado_empleado")); // Estado Activo/Inactivo
                emp.setFechaIngreso(rs.getDate("fecha_ingreso")); // Fecha en que ingreso al taller
                lista.add(emp); // Agrega el mecanico a la lista
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        // Retornamos nuestra lista obtenida
        return lista; // Devuelve la lista de mecanicos
    }

    /**
     * En esta seccion, nosotros cambiamos el estado de un empleado (de ACTIVO a INACTIVO o viceversa).
     * Esto nos resulta util para suspender el acceso de un empleado sin que tengamos que eliminar sus registros pasados.
     * @param idEmpleado Documento de identidad de nuestro empleado.
     * @return true si logramos actualizar su estado.
     */
    public boolean toggleEstado(String idEmpleado) { // Alterna el estado ACTIVO/INACTIVO del empleado
        boolean actualizado = false; // Indicador del resultado de la actualizacion
        // Obtenemos la conexion fisica a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) { // Abre la conexion
            String sqlSelect = "SELECT estado_empleado FROM empleado WHERE doc_emple = ?"; // Consulta el estado actual del empleado
            // Declaramos nuestra consulta preparada para prevenir inyeccion SQL
            PreparedStatement psSelect = con.prepareStatement(sqlSelect); // Prepara la sentencia de consulta
            psSelect.setString(1, idEmpleado); // Filtra por el documento del empleado
            // Usamos nuestro ResultSet para almacenar los resultados
            ResultSet rs = psSelect.executeQuery(); // Ejecuta la consulta del estado actual

            // Validamos a traves del ResultSet si el documento de identidad buscado efectivamente pertenece a uno de nuestros empleados registrados. En el caso positivo de hallarlo, nosotros procedemos a leer cual es su estado actual, determinamos su nuevo valor (si esta activo lo pasaremos a inactivo y viceversa), y luego ejecutamos el respectivo Update para cambiar y guardar dicho estado.
            if (rs.next()) { // Solo actua si el empleado existe
                String estadoActual = rs.getString("estado_empleado"); // Lee el estado actual del empleado
                String nuevoEstado = "ACTIVO".equals(estadoActual) ? "INACTIVO" : "ACTIVO"; // Invierte el estado actual

                String sqlUpdate = "UPDATE empleado SET estado_empleado = ? WHERE doc_emple = ?"; // Actualiza el estado del empleado
                // Declaracion de consulta preparada para prevenir inyeccion SQL
                PreparedStatement psUpdate = con.prepareStatement(sqlUpdate); // Prepara el UPDATE de estado
                psUpdate.setString(1, nuevoEstado); // Nuevo estado a aplicar
                psUpdate.setString(2, idEmpleado); // Empleado cuyo estado se va a cambiar

                actualizado = psUpdate.executeUpdate() > 0; // true si la actualizacion fue exitosa
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        // Retornamos el estado de nuestra actualizacion
        return actualizado; // Devuelve true si el estado fue cambiado exitosamente
    }

    /**
     * Aqui, nosotros verificamos si nuestro empleado o mecanico tiene ordenes de trabajo registradas a su nombre.
     * Esto nos permite validar antes de intentar borrar, para evitar errores de integridad referencial.
     * @param docEmple Documento de identidad de nuestro empleado.
     * @return true si tiene ordenes, false si no.
     */
    public boolean tieneOrdenesAsociadas(String docEmple) { // Verifica si el empleado tiene ordenes antes de eliminarlo
        boolean tiene = false; // Por defecto asume que no tiene ordenes
        String sql = "SELECT COUNT(*) FROM ordentrabajo WHERE doc_emple_fk = ?"; // Cuenta las ordenes del empleado
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el conteo
            ps.setString(1, docEmple); // Filtra por el documento del empleado
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta
                if (rs.next()) { // Si hay resultado
                    tiene = rs.getInt(1) > 0; // Si el conteo es mayor a 0, tiene ordenes asociadas
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return tiene; // true si el empleado tiene ordenes asociadas
    }

    /**
     * Ahora, nosotros eliminamos fisicamente a un empleado de nuestra base de datos.
     * Nuestra validacion de que no tenga datos asociados ya deberia haberse hecho,
     * o bien nuestra base de datos la rechazaria por restricciones de foraneas.
     * @param idEmpleado Documento de identidad del empleado que vamos a borrar.
     * @return true si nosotros logramos borrarlo exitosamente.
     */
    public boolean eliminar(String idEmpleado) { // Elimina fisicamente al empleado de la base de datos
        boolean eliminado = false; // Indicador del resultado de la eliminacion
        // Obtenemos la conexion fisica a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) { // Abre la conexion
            // Escribimos nuestra sentencia SQL Delete directa
            String sql = "DELETE FROM empleado WHERE doc_emple = ?"; // Elimina el empleado por su documento
            PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia DELETE
            ps.setString(1, idEmpleado); // Documento del empleado a eliminar
            eliminado = ps.executeUpdate() > 0; // Nosotros retornamos true si afectamos al menos 1 fila
        } catch (Exception e) {
            e.printStackTrace(); // Imprimimos nuestro error si rompemos alguna otra restriccion no controlada
        }
        return eliminado; // true si el empleado fue eliminado correctamente
    }

    /**
     * Con este metodo, nosotros obtenemos los datos de un empleado especifico mediante su documento.
     * @param id Documento de nuestro empleado.
     * @return Objeto Empleado que encontramos, null si no existe.
     */
    public Empleado obtenerPorId(String id) { // Busca y retorna un empleado por su documento de identidad
        Empleado emp = null; // Sera null si el empleado no existe en la base de datos
        // Obtenemos la conexion fisica a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) { // Abre la conexion
            // Definimos nuestra sentencia SQL para ejecutar en la base de datos
            String sql = "SELECT * FROM empleado WHERE doc_emple = ?"; // Filtra por el documento del empleado
            // Declaramos nuestra consulta preparada para prevenir inyeccion SQL
            PreparedStatement ps = con.prepareStatement(sql); // Prepara la sentencia de busqueda
            ps.setString(1, id); // Asigna el documento como parametro de filtro
            // Usamos nuestro ResultSet para almacenar los resultados
            ResultSet rs = ps.executeQuery(); // Ejecuta la busqueda del empleado
            // Chequeamos si el ResultSet arrojo algun resultado tras buscar el documento del empleado solicitado. Si efectivamente existe un registro coincidente, nosotros comenzamos a instanciar un nuevo objeto Empleado en memoria para mapearle cada una de las columnas (nombre, pin, rol, cargo, estado y fecha de ingreso) extraidas directamente desde nuestra base de datos.
            if (rs.next()) { // Solo mapea si el empleado fue encontrado
                emp = new Empleado(); // Crea el objeto empleado
                emp.setIdEmpleado(rs.getString("doc_emple")); // Documento de identidad
                emp.setNombre(rs.getString("nom_empleado")); // Nombre del empleado
                emp.setPin(rs.getString("pin_acceso")); // PIN de acceso al sistema
                emp.setIdRol(rs.getInt("id_rol_fk")); // Rol del empleado
                emp.setIdCargo(rs.getInt("id_cargo_fk")); // Cargo del empleado
                emp.setEstadoEmpleado(rs.getString("estado_empleado")); // Estado Activo/Inactivo
                emp.setFechaIngreso(rs.getDate("fecha_ingreso")); // Fecha de ingreso al taller
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        // Retornamos a nuestro empleado encontrado
        return emp; // Devuelve el empleado encontrado o null
    }

    /**
     * En este paso, nosotros modificamos la informacion de un empleado existente y guardamos un historico de su nombre.
     * @param emp Objeto Empleado con nuestros datos nuevos.
     * @return true si lo modificamos correctamente.
     */
    public boolean actualizar(Empleado emp) { // Actualiza los datos de un empleado existente
        boolean actualizado = false; // Indicador del resultado de la actualizacion
        // Obtenemos la conexion fisica a nuestra base de datos MySQL
        try (Connection con = Conexion.getConexion()) { // Abre la conexion
            // Definimos nuestra sentencia SQL para ejecutarla
            String sql = "UPDATE empleado SET nom_empleado = ?, pin_acceso = ?, id_cargo_fk = ?, id_rol_fk = ? WHERE doc_emple = ?"; // Actualiza los datos del empleado por su documento
            // Declaramos nuestra consulta preparada para prevenir inyeccion SQL
            PreparedStatement ps = con.prepareStatement(sql); // Prepara el UPDATE
            ps.setString(1, emp.getNombre()); // Nuevo nombre del empleado
            ps.setString(2, emp.getPin()); // Nuevo PIN de acceso
            ps.setInt(3, emp.getIdCargo()); // Nuevo cargo
            ps.setInt(4, emp.getIdRol()); // Nuevo rol
            ps.setString(5, emp.getIdEmpleado()); // Documento para identificar el empleado a actualizar
            actualizado = ps.executeUpdate() > 0; // true si la actualizacion fue exitosa
            System.out.println("[EmpleadoDAO] actualizar() -> filas afectadas: " + actualizado
                    + " | doc: " + emp.getIdEmpleado()); // Log de confirmacion de la actualizacion
        } catch (Exception e) {
            System.err.println("[EmpleadoDAO] ERROR al actualizar empleado: " + e.getMessage()); // Imprime el error en stderr
            e.printStackTrace(); // Muestra el stack trace completo para diagnostico
        }
        // Actualizamos nuestro historico de forma INDEPENDIENTE
        if (actualizado) { // Solo actualiza el historico si la actualizacion principal fue exitosa
            try (Connection con2 = Conexion.getConexion()) { // Abre una segunda conexion para el historico
                String sqlHist = "INSERT INTO empleado_historico (doc_emple, nom_empleado) VALUES (?, ?) ON DUPLICATE KEY UPDATE nom_empleado = ?"; // ON DUPLICATE KEY actualiza si ya existe el registro historico
                try (PreparedStatement psHist = con2.prepareStatement(sqlHist)) { // Prepara la insercion/actualizacion del historico
                    psHist.setString(1, emp.getIdEmpleado()); // Documento para el historico
                    psHist.setString(2, emp.getNombre()); // Nuevo nombre para el historico
                    psHist.setString(3, emp.getNombre()); // Nombre a actualizar si ya existe el registro
                    psHist.executeUpdate(); // Persiste el historico del empleado
                }
            } catch (Exception e) {
                System.err.println("[EmpleadoDAO] AVISO: No se pudo actualizar empleado_historico: " + e.getMessage()); // Aviso no critico del historico
            }
        }
        // Retornamos el resultado de nuestra actualizacion
        return actualizado; // Devuelve true si los datos del empleado fueron actualizados
    }

    /**
     * Aqui, nosotros verificamos si un PIN de acceso ya esta siendo utilizado por otro de nuestros empleados.
     * @param pin El PIN que vamos a comprobar.
     * @param excludeDoc El documento del empleado que nosotros estamos editando (para no validarlo contra si mismo), o null si es nuevo.
     * @return true si el PIN ya existe, false si esta libre.
     */
    public boolean existePin(String pin, String excludeDoc) { // Verifica si el PIN ya esta en uso en el sistema
        boolean existe = false; // Por defecto asume que el PIN esta disponible
        try (Connection con = Conexion.getConexion()) { // Abre la conexion
            PreparedStatement ps; // Prepared statement dinamico segun el contexto
            if (excludeDoc == null) { // CREAR: solo verificar si el PIN ya existe en cualquier empleado
                String sql = "SELECT 1 FROM empleado WHERE TRIM(pin_acceso) = TRIM(?)"; // Busca el PIN sin importar quién lo tiene
                ps = con.prepareStatement(sql); // Prepara la consulta de verificacion de PIN
                ps.setString(1, pin); // PIN a verificar
            } else { // EDITAR: verificar si el PIN existe en otro empleado diferente al actual
                String sql = "SELECT 1 FROM empleado WHERE TRIM(pin_acceso) = TRIM(?) AND doc_emple != ?"; // Excluye al empleado que se esta editando
                ps = con.prepareStatement(sql); // Prepara la consulta de verificacion excluyendo al empleado actual
                ps.setString(1, pin); // PIN a verificar
                ps.setString(2, excludeDoc); // Documento del empleado actual (excluido de la verificacion)
            }
            ResultSet rs = ps.executeQuery(); // Ejecuta la consulta de verificacion
            if (rs.next()) { // Si hay resultado, el PIN ya esta en uso
                existe = true; // El PIN ya pertenece a otro empleado
            }
            System.out.println("[EmpleadoDAO] existePin('" + pin + "', excludeDoc='" + excludeDoc + "') -> " + existe); // Log para diagnostico
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return existe; // true si el PIN ya esta en uso, false si esta disponible
    }

    /**
     * Finalmente, nosotros verificamos si un numero de documento ya esta registrado para otro de nuestros empleados.
     * @param docEmple Numero de documento que vamos a buscar.
     * @return true si ya existe en nuestro sistema, false si esta disponible.
     */
    public boolean existeDocumento(String docEmple) { // Verifica si el documento ya esta registrado en el sistema
        boolean existe = false; // Por defecto asume que el documento esta disponible
        try (Connection con = Conexion.getConexion(); // Abre la conexion
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM empleado WHERE doc_emple = ?")) { // Busca si el documento ya existe en la tabla
            ps.setString(1, docEmple); // Documento a verificar
            ResultSet rs = ps.executeQuery(); // Ejecuta la busqueda
            if (rs.next()) { // Si hay resultado, el documento ya esta registrado
                existe = true; // El documento ya pertenece a un empleado existente
            }
            System.out.println("[EmpleadoDAO] existeDocumento('" + docEmple + "') -> " + existe); // Log para diagnostico
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error SQL para diagnostico
        }
        return existe; // true si el documento ya esta registrado, false si esta disponible
    }
}
