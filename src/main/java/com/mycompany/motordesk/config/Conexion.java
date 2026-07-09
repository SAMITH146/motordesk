// Definicion del paquete del proyecto
package com.mycompany.motordesk.config; // Paquete de configuracion del sistema

// Importacion de dependencias y clases necesarias
import java.sql.Connection; // Interfaz JDBC que representa una conexion activa a la base de datos
import java.sql.DriverManager; // Clase JDBC que gestiona los drivers y crea conexiones

/**
 * Clase de configuracion encargada de establecer la conexion con la base de datos MySQL.
 * Utiliza JDBC (Java Database Connectivity), que es la API estandar de Java para conectarse a bases de datos relacionales.
 * Esta conexion permite que el sistema (DAOs) pueda enviar consultas (SELECT, INSERT, UPDATE, DELETE) a la base de datos.
 */
public class Conexion { // Clase utilitaria que centraliza la configuracion de la conexion a MySQL

    // URL de conexion JDBC que especifica el motor de base de datos (mysql), el servidor local (localhost),
    // el puerto (3306) y el nombre de la base de datos a la que nos conectaremos ('motordesk').
    private static final String URL =
        "jdbc:mysql://localhost:3306/motordesk?useSSL=false&serverTimezone=UTC"; // Cadena de conexion al servidor MySQL local

    // Credenciales de acceso de la base de datos
    private static final String USER = "root"; // Usuario del servidor MySQL (por defecto 'root' en desarrollo)
    private static final String PASSWORD ="COMPUTER.777"; // Contraseña del usuario MySQL

    // Metodo estatico para obtener la conexion activa a la base de datos MySQL usando JDBC
    public static Connection getConexion() { // Todos los DAOs llaman a este metodo para obtener una conexion

        Connection con = null; // Variable del tipo java.sql.Connection; sera null si la conexion falla

        try {
            // 1. Cargar dinamicamente el Driver del Conector de MySQL en memoria de Java
            Class.forName("com.mysql.cj.jdbc.Driver"); // Registra el driver JDBC de MySQL para que DriverManager pueda usarlo

            // 2. Establecer la conexion con el servidor MySQL mediante la URL y credenciales configuradas
            con = DriverManager.getConnection(URL, USER, PASSWORD); // Crea la conexion fisica con la base de datos motordesk

            System.out.println("Conexion exitosa"); // Diagnostico de conexion en la consola del servidor

        } catch (Exception e) {
            e.printStackTrace(); // Muestra el error en la consola del servidor (Tomcat) en caso de fallo de driver o credenciales
        }

        return con; // Retorna la conexion fisica activa (o null en caso de error)
    }

    // Metodo de soporte autogenerado (sin uso actual en la logica de negocio)
    // Metodo Getter para recuperar el valor de Connection
    public static Connection getConnection() { // Metodo generado automaticamente, no utilizado en el sistema
        throw new UnsupportedOperationException("Not supported yet."); // Lanza excepcion si se invoca por error
    }
}
