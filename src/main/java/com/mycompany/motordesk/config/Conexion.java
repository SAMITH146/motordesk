// Definición del paquete del proyecto
package com.mycompany.motordesk.config;

// Importación de dependencias y clases necesarias
import java.sql.Connection;
import java.sql.DriverManager;

// Clase de configuración encargada de establecer la conexión con la base de datos MySQL
// Clase pública Conexion que gestiona la lógica correspondiente
public class Conexion {

    // URL de conexión JDBC que especifica el servidor local, el puerto 3306 y la base de datos 'motordesk'
    private static final String URL =
        "jdbc:mysql://localhost:3306/motordesk?useSSL=false&serverTimezone=UTC";

    // Credenciales de acceso de la base de datos
    private static final String USER = "root";
    private static final String PASSWORD ="COMPUTER.777";

    // Método estático para obtener la conexión física de la base de datos
    // Método Getter para recuperar el valor de Conexion
    public static Connection getConexion() {

        Connection con = null; // Inicializamos la variable de tipo Connection

        // Inicio del bloque try para control de excepciones
        try {
            // Cargar de forma dinámica el Driver de MySQL Connector/J en la memoria de la aplicación
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establecer y retornar la conexión usando la URL y credenciales configuradas
            con = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Conexion exitosa"); // Mensaje de diagnóstico en la consola del servidor

        } catch (Exception e) {
            e.printStackTrace(); // Capturar y mostrar errores en caso de fallo de conexión o driver no encontrado
        }

        // Retornar el valor obtenido
        return con; // Retornamos el objeto de conexión (o null si falló)
    }

    // Método de soporte autogenerado (sin uso actual en la lógica de negocio)
    // Método Getter para recuperar el valor de Connection
    public static Connection getConnection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
