// Definición del paquete del proyecto
package com.mycompany.motordesk.config;

// Importación de dependencias y clases necesarias
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Clase de configuración encargada de establecer la conexión con la base de datos MySQL.
 * Utiliza JDBC (Java Database Connectivity), que es la API estándar de Java para conectarse a bases de datos relacionales.
 * Esta conexión permite que el sistema (DAOs) pueda enviar consultas (SELECT, INSERT, UPDATE, DELETE) a la base de datos.
 */
public class Conexion {

    // URL de conexión JDBC que especifica el motor de base de datos (mysql), el servidor local (localhost), 
    // el puerto (3306) y el nombre de la base de datos a la que nos conectaremos ('motordesk').
    private static final String URL =
        "jdbc:mysql://localhost:3306/motordesk?useSSL=false&serverTimezone=UTC";

    // Credenciales de acceso de la base de datos
    private static final String USER = "root";
    private static final String PASSWORD ="#Aprendiz2024";

    // Método estático para obtener la conexión activa a la base de datos MySQL usando JDBC
    public static Connection getConexion() {

        Connection con = null; // Variable del tipo java.sql.Connection

        try {
            // 1. Cargar dinámicamente el Driver del Conector de MySQL en memoria de Java
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Establecer la conexión con el servidor MySQL mediante la URL y credenciales configuradas
            con = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Conexion exitosa"); // Diagnóstico de conexión en la consola del servidor

        } catch (Exception e) {
            e.printStackTrace(); // Muestra el error en la consola del servidor (Tomcat) en caso de fallo de driver o credenciales
        }

        return con; // Retorna la conexión física activa (o null en caso de error)
    }

    // Método de soporte autogenerado (sin uso actual en la lógica de negocio)
    // Método Getter para recuperar el valor de Connection
    public static Connection getConnection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
