package com.lostandfound.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// JDBC connection
public final class DatabaseConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/lost_and_found_portal?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "";          // MySQL password here

    private static Connection connection;

    private DatabaseConnection() { }

    // Lazily initialised reusable connection (reconnects if the previous connection was ended)
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. "
                        + "Add mysql-connector-j-*.jar to the classpath.", e);
            }
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }

    /** Closes the shared connection if it is open. */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) { }
        }
    }
}
