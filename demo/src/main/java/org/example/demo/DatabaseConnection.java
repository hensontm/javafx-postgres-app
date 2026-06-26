package org.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Variabel ini sudah menggunakan data database Anda
    private static final String URL = "jdbc:postgresql://localhost:5432/Project_BD";
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345678";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Koneksi ke PostgreSQL berhasil!");
        } catch (SQLException e) {
            System.out.println("Gagal terkoneksi ke database!");
            e.printStackTrace();
        }
        return connection;
    }

    // Fungsi main sementara untuk tes koneksi
    public static void main(String[] args) {
        getConnection();
    }
}