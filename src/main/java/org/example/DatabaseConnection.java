
package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";

    private static final String USER =
            "postgres.jqqkwneukimvzalfbcow";

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new IllegalStateException(
                    "DB_PASSWORD environment variable is not set."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Supabase connected successfully!");
        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}
