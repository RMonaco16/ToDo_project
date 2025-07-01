package dao;

import model.Activity;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Classe DAO per la gestione delle attività (Activity) associate alle checklist dei ToDo.
 *
 * Permette di caricare, aggiungere, rimuovere e modificare attività legate a checklist associate a ToDo
 * all'interno di board specifiche di utenti, gestendo anche i permessi di condivisione.
 */
public class CheckListDAO {

    private static final Logger logger = Logger.getLogger(CheckListDAO.class.getName());

    private final Connection conn;

    /**
     * Costruttore che riceve una connessione al database.
     *
     * @param conn Connessione al database da usare per tutte le operazioni.
     */
    public CheckListDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Recupera tutte le attività associate a un ToDo specifico di un utente in una certa board.
     *
     * @param email Email dell'utente proprietario della board.
     * @param boardType Tipo della board (es. UNIVERSITY, WORK, FREETIME).
     * @param todoTitle Titolo del ToDo a cui sono associate le attività.
     * @return Lista di oggetti Activity relativi al ToDo richiesto.
     */
    public ArrayList<Activity> getActivities(String email, String boardType, String todoTitle) {
        ArrayList<Activity> activities = new ArrayList<>();

        String sql = """
        SELECT a.name, a.state, a.completion_date
        FROM activities a
        JOIN checklists c ON a.checklist_id = c.id
        JOIN todos t ON t.checklist_id = c.id
        JOIN boards b ON t.board_id = b.id
        WHERE b.user_email = ? AND b.type = ? AND t.title = ?
        ORDER BY a.id
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType.toUpperCase()); // 'UNIVERSITY', 'WORK', 'FREETIME'
            stmt.setString(3, todoTitle);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                boolean state = rs.getBoolean("state");
                Date completion = rs.getDate("completion_date");

                Activity activity = new Activity(name, state);
                if (completion != null) {
                    activity.setCompletionDate(completion.toString()); // o converti in LocalDate se preferisci
                }

                activities.add(activity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }


    /**
     * Aggiunge una nuova attività alla checklist associata a un ToDo specifico.
     * Controlla prima che non esista già un'attività con lo stesso nome.
     *
     * @param email Email dell'utente proprietario.
     * @param titleToDo Titolo del ToDo cui associare l'attività.
     * @param boardType Tipo della board.
     * @param activity Oggetto Activity da inserire.
     */
    public void addActivity(String email, String titleToDo, String boardType, Activity activity) {
        //  Trova ID della checklist collegata al ToDo
        int checklistId = -1;
        String checklistSql = """
        SELECT c.id
        FROM checklists c
        JOIN todos t ON t.checklist_id = c.id
        JOIN boards b ON t.board_id = b.id
        WHERE b.user_email = ? AND b.type = ? AND t.title = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(checklistSql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType.toUpperCase());
            stmt.setString(3, titleToDo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                checklistId = rs.getInt("id");
            } else {
                logger.info("Checklist non trovata.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        //  Controllo: esiste già un'attività con lo stesso nome in questa checklist?
        String checkDuplicateSql = "SELECT 1 FROM activities WHERE checklist_id = ? AND LOWER(name) = LOWER(?)";

        try (PreparedStatement stmt = conn.prepareStatement(checkDuplicateSql)) {
            stmt.setInt(1, checklistId);
            stmt.setString(2, activity.getName());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                logger.info("Attività già esistente nella checklist.");
                return; // blocca inserimento
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Inserimento dell'attività
        String insertSql = "INSERT INTO activities (checklist_id, name, state, completion_date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, checklistId);
            stmt.setString(2, activity.getName());
            stmt.setBoolean(3, activity.getState());

            if (activity.getCompletionDate() != null) {
                stmt.setDate(4, Date.valueOf(activity.getCompletionDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.executeUpdate();
            logger.info("Attività inserita correttamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Rimuove un'attività da una checklist associata a un ToDo di un utente.
     * Dopo la rimozione aggiorna lo stato del ToDo in base al completamento delle attività rimanenti.
     *
     * @param email Email dell'utente proprietario.
     * @param titleToDo Titolo del ToDo associato alla checklist.
     * @param board Tipo della board.
     * @param nameActivity Nome dell'attività da rimuovere.
     * @throws SQLException In caso di errori SQL.
     */
    public void removeActivity(String email, String titleToDo, String board, String nameActivity) throws SQLException{
        try {
            // 1. Recupera l'ID della board per l'utente
            String boardQuery = "SELECT id FROM boards WHERE user_email = ? AND type = ?";
            try (PreparedStatement boardStmt = conn.prepareStatement(boardQuery)) {
                boardStmt.setString(1, email);
                boardStmt.setString(2, board.toUpperCase());
                ResultSet rsBoard = boardStmt.executeQuery();

                if (!rsBoard.next()) {
                    logger.info("Bacheca non trovata.");
                    return;
                }

                int boardId = rsBoard.getInt("id");

                // 2. Recupera il ToDo e il checklist_id
                String todoQuery = "SELECT id, checklist_id FROM todos WHERE LOWER(title) = LOWER(?) AND board_id = ?";
                try (PreparedStatement todoStmt = conn.prepareStatement(todoQuery)) {
                    todoStmt.setString(1, titleToDo);
                    todoStmt.setInt(2, boardId);
                    ResultSet rsTodo = todoStmt.executeQuery();

                    if (!rsTodo.next()) {
                        logger.info("ToDo non trovato.");
                        return;
                    }

                    int todoId = rsTodo.getInt("id");
                    int checklistId = rsTodo.getInt("checklist_id");

                    if (checklistId == 0) {
                        logger.info("ToDo non ha una checklist associata.");
                        return;
                    }

                    // 3. Elimina l'attività specifica dalla checklist
                    String deleteAct = "DELETE FROM activities WHERE checklist_id = ? AND LOWER(name) = LOWER(?)";
                    try (PreparedStatement delActStmt = conn.prepareStatement(deleteAct)) {
                        delActStmt.setInt(1, checklistId);
                        delActStmt.setString(2, nameActivity);
                        int affected = delActStmt.executeUpdate();

                        if (affected == 0) {
                            logger.info("Attività non trovata.");
                            return;
                        }
                    }

                    // 4. Verifica se la checklist è completamente completata
                    String checkQuery = "SELECT COUNT(*) AS incompleti FROM activities WHERE checklist_id = ? AND state = false";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                        checkStmt.setInt(1, checklistId);
                        ResultSet rsCheck = checkStmt.executeQuery();

                        if (rsCheck.next()) {
                            int incompleti = rsCheck.getInt("incompleti");

                            // 5. Aggiorna lo stato del ToDo (completato solo se tutte le attività lo sono)
                            String updateTodo = "UPDATE todos SET state = ? WHERE id = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateTodo)) {
                                updateStmt.setBoolean(1, incompleti == 0);
                                updateStmt.setInt(2, todoId);
                                updateStmt.executeUpdate();
                            }
                        }
                    }

                    logger.info("Attività rimossa con successo.");
                }
            }
        } catch (SQLException e) {
           logger.info("Errore durante la rimozione dell'attività: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Segna un'attività come completata e imposta la data di completamento.
     * Gestisce sia attività di ToDo locali sia condivisi.
     *
     * @param email Email dell'utente (proprietario o membro condiviso).
     * @param board Tipo della board.
     * @param todo Titolo del ToDo.
     * @param activity Nome dell'attività da segnare come completata.
     * @param dataCompletamento Data di completamento in formato "dd-MM-yyyy".
     */
    public void checkActivity(String email, String board, String todo, String activity, String dataCompletamento) {
        String sql = "UPDATE activities SET state = ?, completion_date = ? " +
                "WHERE name = ? AND checklist_id = (" +
                "SELECT c.id FROM checklists c " +
                "JOIN todos t ON c.id = t.checklist_id " +
                "JOIN boards b ON t.board_id = b.id " +
                "LEFT JOIN sharings s ON s.todo_id = t.id " +
                "LEFT JOIN sharing_members sm ON sm.sharing_id = s.id " +
                "WHERE (b.user_email = ? OR sm.member_email = ?) " +
                "AND b.type = ? AND t.title = ? LIMIT 1)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, true);

            if (dataCompletamento != null && !dataCompletamento.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                java.util.Date parsedDate = sdf.parse(dataCompletamento);
                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                stmt.setDate(2, sqlDate);
            } else {
                stmt.setDate(2, null);
            }

            stmt.setString(3, activity);
            stmt.setString(4, email); // owner
            stmt.setString(5, email); // shared member
            stmt.setString(6, board.toUpperCase());
            stmt.setString(7, todo);

            int updatedRows = stmt.executeUpdate();

            if (updatedRows == 0) {
                logger.info("Nessuna attività aggiornata: verifica i parametri o se hai accesso alla ToDo.");
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Segna un'attività come non completata (unchecked) rimuovendo anche la data di completamento.
     *
     * @param email Email dell'utente (proprietario o membro condiviso).
     * @param board Tipo della board.
     * @param todo Titolo del ToDo.
     * @param activity Nome dell'attività da segnare come non completata.
     * @return true se l'operazione è andata a buon fine, false altrimenti.
     */
    public boolean uncheckActivity(String email, String board, String todo, String activity) {
        String sql = "UPDATE activities SET state = ?, completion_date = ? " +
                "WHERE name = ? AND checklist_id = (" +
                "SELECT c.id FROM checklists c " +
                "JOIN todos t ON c.id = t.checklist_id " +
                "JOIN boards b ON t.board_id = b.id " +
                "LEFT JOIN sharings s ON s.todo_id = t.id " +
                "LEFT JOIN sharing_members sm ON sm.sharing_id = s.id " +
                "WHERE (b.user_email = ? OR sm.member_email = ?) " +
                "AND b.type = ? AND t.title = ? LIMIT 1)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, false);
            stmt.setNull(2, java.sql.Types.DATE);
            stmt.setString(3, activity);
            stmt.setString(4, email);
            stmt.setString(5, email);
            stmt.setString(6, board.toUpperCase());
            stmt.setString(7, todo);

            int updatedRows = stmt.executeUpdate();
            return updatedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera l'ID del ToDo corrispondente a un titolo, board e utente (proprietario o membro condiviso).
     *
     * @param email Email dell'utente.
     * @param boardType Tipo della board.
     * @param todoTitle Titolo del ToDo.
     * @return ID del ToDo.
     * @throws SQLException Se il ToDo non viene trovato o si verifica un errore SQL.
     */
    public int getToDoId(String email, String boardType, String todoTitle) throws SQLException {
        String sql = """
        SELECT t.id
        FROM todos t
        JOIN boards b ON t.board_id = b.id
        LEFT JOIN sharings s ON t.id = s.todo_id
        LEFT JOIN sharing_members sm ON sm.sharing_id = s.id
        WHERE b.type = ? AND t.title = ? AND (
            b.user_email = ? OR sm.member_email = ?
        )
        LIMIT 1
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, boardType.toUpperCase());
            ps.setString(2, todoTitle);
            ps.setString(3, email); // proprietario
            ps.setString(4, email); // membro condiviso
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new SQLException("ToDo non trovato");
                }
            }
        }
    }
}
