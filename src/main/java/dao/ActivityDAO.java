package dao;

import db.DatabaseConnection;

import java.sql.*;

/**
 * Classe Data Access Object (DAO) per gestire le operazioni
 * sul database relative all'entità Activity.
 *
 * Si occupa di aprire e mantenere la connessione al database
 * tramite la classe DatabaseConnection.
 */
public class ActivityDAO {

    /**
     * Connessione al database utilizzata per eseguire le query.
     */
    private Connection conn;

    /**
     * Costruttore che inizializza la connessione al database
     * utilizzando il singleton DatabaseConnection.
     */
    public ActivityDAO() {
        conn = DatabaseConnection.getInstance().getConnection();
    }
}
