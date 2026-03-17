package com.mycompany.motordesk.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {

    private static final String URL =
        "jdbc:mysql://localhost:3306/motordesk?useSSL=false&serverTimezone=UTC";

    private static final String USER = "root";
    private static final String PASSWORD ="COMPUTER.777";

    public static Connection getConexion() {

        Connection con = null; 

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Conexion exitosa");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return con;
    }

    public static Connection getConnection() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
