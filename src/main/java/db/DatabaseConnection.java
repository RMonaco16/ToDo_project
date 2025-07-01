package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton per la gestione della connessione al database PostgreSQL.
 */
public class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private Connection connection;

    private final String nome = "postgres";
    private final String password = "posgre321";
    private final String url = "jdbc:postgresql://localhost:5432/ToDo";
    private final String driver = "org.postgresql.Driver";

    private DatabaseConnection() {
        try {
            Class.forName(driver); // Carica il driver PostgreSQL
            connection = DriverManager.getConnection(url, nome, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore nella connessione al database", e);
        }
    }

    /**
     * Restituisce l'istanza singleton di DatabaseConnection.
     * Implementazione thread-safe con doppio controllo.
     *
     * @return istanza singleton
     */
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

    /**
     * Restituisce la connessione attiva al database.
     * Se la connessione è chiusa o nulla, viene riaperta.
     *
     * @return connection attiva
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, nome, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Puoi anche rilanciare un RuntimeException per evitare ritorni null
            throw new RuntimeException("Errore durante l'apertura della connessione", e);
        }
        return connection;
    }
}
