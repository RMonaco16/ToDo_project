package dao;

import model.Activity;
import model.CompletedActivityHistory;

import java.sql.*;
import java.util.ArrayList;

/**
 * Classe DAO per la gestione della cronologia delle attività completate dagli utenti.
 * Permette di aggiungere, rimuovere e recuperare le attività completate memorizzate nel database.
 */
public class CompletedActivityHistoryDAO {

    private final Connection conn;

    /**
     * Costruttore che riceve la connessione al database.
     *
     * @param conn Connessione al database da utilizzare per le operazioni.
     */
    public CompletedActivityHistoryDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Rimuove una specifica attività dalla cronologia completata di un utente.
     *
     * @param email Email dell'utente.
     * @param activityName Nome dell'attività da rimuovere.
     * @return true se l'attività è stata rimossa con successo, false altrimenti.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public boolean removeActivityFromHistory(String email, String activityName) throws SQLException {
        String sql = "DELETE FROM completed_activities WHERE user_email = ? AND activity_name = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, activityName);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Cancella tutta la cronologia delle attività completate per un utente specifico.
     *
     * @param email Email dell'utente.
     * @return true se almeno una attività è stata eliminata, false altrimenti.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public boolean deleteAllActivitiesFromHistory(String email) throws SQLException {
        String sql = "DELETE FROM completed_activities WHERE user_email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Aggiunge una nuova attività completata alla cronologia di un utente.
     * Prima controlla che l'attività con la stessa data di completamento non sia già presente.
     *
     * @param email Email dell'utente.
     * @param activity Nome dell'attività completata.
     * @param completionDate Data di completamento dell'attività.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void addActivityToHistory(String email, String activity, Date completionDate) throws SQLException {
        String checkQuery = "SELECT 1 FROM completed_activities WHERE user_email = ? AND activity_name = ? AND completion_date = ?";
        String insertQuery = "INSERT INTO completed_activities(user_email, activity_name, completion_date) VALUES (?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, email);
            checkStmt.setString(2, activity);
            checkStmt.setDate(3, completionDate);

            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                // Solo se non esiste già, procedi con l'inserimento
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, email);
                    insertStmt.setString(2, activity);
                    insertStmt.setDate(3, completionDate);
                    insertStmt.executeUpdate();
                }
            } else {
                System.out.println("Attività già presente nella cronologia, non verrà reinserita.");
            }
        }
    }

    /**
     * Recupera tutte le attività completate da un utente.
     *
     * @param email Email dell'utente.
     * @return Lista di oggetti Activity completate dall'utente.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public ArrayList<Activity> getCompletedActivitiesByUser(String email) throws SQLException {
        ArrayList<Activity> activities = new ArrayList<>();

        String sql = "SELECT activity_name, completion_date FROM completed_activities WHERE user_email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("activity_name");
                    Date completionDate = rs.getDate("completion_date");

                    Activity activity = new Activity();
                    activity.setName(name);
                    activity.setCompletionDate(String.valueOf(completionDate));
                    activity.setState(true); // l'attività è già completata

                    activities.add(activity);
                }
            }
        }
        return activities;
    }

}
