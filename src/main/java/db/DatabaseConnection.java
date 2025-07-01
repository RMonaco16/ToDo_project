package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private final String nome = "postgres";
    private final String password = "posgre321";
    private final String url = "jdbc:postgresql://localhost:5432/ToDo";
    private final String driver = "org.postgresql.Driver";

    private DatabaseConnection() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, nome, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore nella connessione al database", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // (Re)instanzia la connessione se è nulla o chiusa
                connection = DriverManager.getConnection(url, nome, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
