package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnessioneDatabase {
    private static ConnessioneDatabase instance;
    private Connection connection;
    private final String nome = "admin";
    private final String password = "admin";
    private final String url = "jdbc:postgresql://localhost:5432/ToDo";
    private final String driver = "org.postgresql.Driver";

    private ConnessioneDatabase() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, nome, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore nella connessione al database", e);
        }
    }

    public static ConnessioneDatabase getInstance() {
        if (instance == null) {
            synchronized (ConnessioneDatabase.class) {
                if (instance == null) {
                    instance = new ConnessioneDatabase();
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
