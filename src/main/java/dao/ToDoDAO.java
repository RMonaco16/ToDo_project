package dao;

import db.DatabaseConnection;
import model.ToDo;

import java.awt.Color;
import java.sql.*;
import java.time.LocalDate;
import java.util.logging.Logger;

public class ToDoDAO {

    private static final Logger logger = Logger.getLogger(ToDoDAO.class.getName());

    private Connection connection;
    private static final String SELECT_BOARD_ID =
            "SELECT id FROM boards WHERE user_email = ? AND type = ?";

    public ToDoDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public ToDoDAO(Connection conn) {
        this.connection = conn;
    }

    /**
     * Aggiunge un nuovo ToDo nella board specificata per l'utente.
     * Controlla che non esista già un ToDo con lo stesso titolo (locale o condiviso).
     * Crea una checklist vuota associata al ToDo.
     *
     * @param email email dell'utente proprietario della board
     * @param tipoEnum tipo della board (es. "WORK")
     * @param toDo oggetto ToDo da aggiungere
     * @return true se il ToDo è stato aggiunto correttamente, false altrimenti
     */
    public boolean addToDoInBoard(String email, String tipoEnum, ToDo toDo) {
        int boardId = -1;

        try (PreparedStatement boardStmt = connection.prepareStatement(SELECT_BOARD_ID)) {
            boardStmt.setString(1, email);
            boardStmt.setString(2, tipoEnum);
            ResultSet boardRs = boardStmt.executeQuery();

            if (boardRs.next()) {
                boardId = boardRs.getInt("id");
            } else {
                logger.info("Board non trovata per l’utente: " + email);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // CONTROLLO ESISTENZA ToDo CON LO STESSO TITOLO (sia locale che condiviso)
        String checkSql = """
    SELECT 1
    FROM todos
    WHERE board_id = ? AND LOWER(title) = LOWER(?)

    UNION

    SELECT 1
    FROM todos
    JOIN sharings ON todos.id = sharings.todo_id
    JOIN sharing_members ON sharings.id = sharing_members.sharing_id
    JOIN boards ON todos.board_id = boards.id
    WHERE sharing_members.member_email = ?
      AND boards.type = ?
      AND LOWER(todos.title) = LOWER(?)
    LIMIT 1
    """;

        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, boardId);               // To-Do locale
            checkStmt.setString(2, toDo.getTitle());

            checkStmt.setString(3, email);              // To-Do condiviso
            checkStmt.setString(4, tipoEnum);           // tipo della board (es. "WORK")
            checkStmt.setString(5, toDo.getTitle());

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                logger.info("Esiste già un ToDo con lo stesso titolo (locale o condiviso).");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        //  Inserisci checklist vuota
        int checklistId = -1;
        String insertChecklistSql = "INSERT INTO checklists DEFAULT VALUES RETURNING id";

        try (PreparedStatement checklistStmt = connection.prepareStatement(insertChecklistSql)) {
            ResultSet rs = checklistStmt.executeQuery();
            if (rs.next()) {
                checklistId = rs.getInt("id");
            } else {
                logger.info("Errore nella creazione della checklist.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        //Inserisci ToDo con la checklist appena creata
        String insertSql = """
        INSERT INTO todos (board_id, title, description, color, position, image, expiration, state, condiviso, owner_email, checklist_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setInt(1, boardId);
            insertStmt.setString(2, toDo.getTitle());
            insertStmt.setString(3, toDo.getDescription() != null ? toDo.getDescription() : null);
            insertStmt.setString(4, toDo.getColor() != null ? toDo.getColor().toString() : null);
            insertStmt.setObject(5, null); // position (non gestito ora)
            insertStmt.setString(6, toDo.getImage() != null ? toDo.getImage() : null);
            if (toDo.getExpiration() != null) {
                insertStmt.setDate(7, Date.valueOf(toDo.getExpiration()));
            } else {
                insertStmt.setNull(7, Types.DATE);
            }
            insertStmt.setBoolean(8, toDo.isState());
            insertStmt.setBoolean(9, toDo.isCondiviso());
            insertStmt.setString(10, toDo.getOwnerEmail());
            insertStmt.setInt(11, checklistId);

            insertStmt.executeUpdate();
            logger.info("ToDo con checklist vuota inserito con successo.");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Elimina un ToDo dalla board specificata di un utente.
     * Se il ToDo è condiviso, verifica che l'utente sia amministratore prima di eliminarlo.
     *
     * @param email email dell'utente che richiede l'eliminazione
     * @param boardType tipo della board (es. "WORK")
     * @param todoTitle titolo del ToDo da eliminare
     * @return true se il ToDo è stato eliminato, false altrimenti
     * @throws SQLException in caso di errore nell'accesso al database
     */
    public boolean deleteToDo(String email, String boardType, String todoTitle) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // 1. Recupera l'ID della bacheca
        try (PreparedStatement boardStmt = conn.prepareStatement(SELECT_BOARD_ID)) {
            boardStmt.setString(1, email);
            boardStmt.setString(2, boardType.toUpperCase());

            ResultSet rsBoard = boardStmt.executeQuery();
            if (!rsBoard.next()) {
                logger.info("Bacheca non trovata.");
                return false;
            }

            int boardId = rsBoard.getInt("id");

            // 2. Cerca il ToDo
            String todoQuery = "SELECT id, condiviso FROM todos WHERE LOWER(title) = LOWER(?) AND board_id = ?";
            try (PreparedStatement todoStmt = conn.prepareStatement(todoQuery)) {
                todoStmt.setString(1, todoTitle);
                todoStmt.setInt(2, boardId);

                ResultSet rsTodo = todoStmt.executeQuery();
                if (!rsTodo.next()) {
                    logger.info("ToDo non trovato.");
                    return false;
                }

                int todoId = rsTodo.getInt("id");
                boolean isCondiviso = rsTodo.getBoolean("condiviso");

                // 3. Se condiviso, verifica i permessi e rimuovi sharing
                if (isCondiviso) {
                    String checkAdminQuery = "SELECT id FROM sharings WHERE todo_id = ? AND administrator_email = ?";
                    try (PreparedStatement adminStmt = conn.prepareStatement(checkAdminQuery)) {
                        adminStmt.setInt(1, todoId);
                        adminStmt.setString(2, email);

                        ResultSet rsAdmin = adminStmt.executeQuery();
                        if (!rsAdmin.next()) {
                            logger.info("Solo l'amministratore può eliminare un ToDo condiviso.");
                            return false;
                        }

                        int sharingId = rsAdmin.getInt("id");

                        try (PreparedStatement delMembers = conn.prepareStatement(
                                "DELETE FROM sharing_members WHERE sharing_id = ?")) {
                            delMembers.setInt(1, sharingId);
                            delMembers.executeUpdate();
                        }

                        try (PreparedStatement delSharing = conn.prepareStatement(
                                "DELETE FROM sharings WHERE id = ?")) {
                            delSharing.setInt(1, sharingId);
                            delSharing.executeUpdate();
                        }
                    }
                }

                // 4. Elimina il ToDo
                try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM todos WHERE id = ?")) {
                    deleteStmt.setInt(1, todoId);
                    deleteStmt.executeUpdate();
                }

                logger.info("ToDo eliminato con successo.");
                return true;
            }
        }
    }

    /**
     * Verifica se un utente è amministratore di un ToDo specifico nella board indicata.
     *
     * @param email email dell'utente
     * @param boardName tipo della board (es. "WORK")
     * @param toDoTitle titolo del ToDo
     * @return true se l'utente è amministratore del ToDo, false altrimenti
     */
    public boolean isUserAdminOfToDo(String email, String boardName, String toDoTitle) {
        String sql = """
        SELECT COUNT(*) as total
        FROM todos t
        JOIN boards b ON t.board_id = b.id
        WHERE LOWER(t.owner_email) = LOWER(?)
          AND LOWER(t.title) = LOWER(?)
          AND LOWER(b.type) = LOWER(?)
          AND LOWER(b.user_email) = LOWER(?)
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, toDoTitle);
            stmt.setString(3, boardName.toUpperCase());
            stmt.setString(4, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Aggiorna i dettagli di un ToDo esistente nella board di un utente.
     *
     * @param email email dell'utente proprietario della board
     * @param boardType tipo della board (es. "WORK")
     * @param oldTitle titolo corrente del ToDo da aggiornare
     * @param newTitle nuovo titolo da assegnare
     * @param description nuova descrizione (può essere null)
     * @param expiration nuova data di scadenza (può essere null)
     * @param image nuova immagine associata (può essere null)
     * @param color nuovo colore associato (può essere null)
     * @return true se l'aggiornamento è andato a buon fine, false altrimenti
     */
    public boolean updateToDo(String email, String boardType, String oldTitle,
                              String newTitle, String description, LocalDate expiration,
                              String image, Color color) {

        String sql = """
        UPDATE todos
        SET title = ?, description = ?, expiration = ?, image = ?, color = ?
        WHERE id = (
            SELECT t.id
            FROM todos t
            JOIN boards b ON t.board_id = b.id
            WHERE b.user_email = ? AND b.type = ? AND LOWER(t.title) = LOWER(?)
        )
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newTitle);
            stmt.setString(2, description);
            stmt.setDate(3, expiration != null ? Date.valueOf(expiration) : null);
            stmt.setString(4, image);
            stmt.setString(5, color != null ? String.format("#%06X", (0xFFFFFF & color.getRGB())) : null);
            stmt.setString(6, email);
            stmt.setString(7, boardType.toUpperCase());
            stmt.setString(8, oldTitle);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Recupera l'ID di un ToDo dato il titolo, l'email dell'amministratore e il tipo di board.
     *
     * @param title titolo del ToDo
     * @param adminEmail email dell'amministratore/proprietario
     * @param boardName tipo della board (es. "WORK")
     * @return ID del ToDo se trovato, null altrimenti
     */
    public Integer getTodoIdByTitleUserAndBoard(String title, String adminEmail, String boardName) {
        String sql = """
        SELECT t.id
        FROM todos t
        JOIN boards b ON t.board_id = b.id
        WHERE LOWER(t.title) = LOWER(?) 
          AND LOWER(b.type) = LOWER(?) 
          AND b.user_email = ?
          AND t.owner_email = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, boardName);
            stmt.setString(3, adminEmail); // verifica che la board appartenga all'utente
            stmt.setString(4, adminEmail); // verifica che il todo sia suo

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Imposta il campo 'condiviso' di un ToDo a true dato il suo ID.
     *
     * @param todoId ID del ToDo
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean setCondivisoTrueById(int todoId) {
        String sql = """
        UPDATE todos
        SET condiviso = TRUE
        WHERE id = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, todoId);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Recupera il colore associato a un ToDo specifico.
     *
     * @param boardType tipo della board (es. "WORK")
     * @param email email dell'utente proprietario della board
     * @param toDoTitle titolo del ToDo
     * @param shared true se il ToDo è condiviso, false altrimenti
     * @return oggetto Color se presente, null altrimenti
     */
    public Color getColorOfToDo(String boardType, String email, String toDoTitle, boolean shared) {
        String sql = """
        SELECT t.color
        FROM todos t
        JOIN boards b ON t.board_id = b.id
        WHERE b.user_email = ?
        AND b.type = ?
        AND LOWER(t.title) = LOWER(?)
        AND t.condiviso = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType.toUpperCase());
            stmt.setString(3, toDoTitle);
            stmt.setBoolean(4, shared);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hexColor = rs.getString("color");
                    if (hexColor != null && hexColor.matches("#[0-9A-Fa-f]{6}")) {
                        return Color.decode(hexColor);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Imposta il campo 'condiviso' di un ToDo a false dato il suo ID.
     *
     * @param todoId ID del ToDo
     */
    public void setCondivisoFalseById(int todoId) {
        String sql = "UPDATE todos SET condiviso = FALSE WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, todoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sposta un ToDo da una board all'altra di proprietà dello stesso utente.
     * Non è possibile spostare ToDo condivisi.
     *
     * Codici di ritorno:
     * 0 = successo
     * 1 = ToDo già presente nella board di destinazione
     * 2 = ToDo condiviso, non può essere spostato
     * 3 = utente o board non trovati o errore
     *
     * @param email email dell'utente
     * @param nomeToDo titolo del ToDo da spostare
     * @param nomeBachecaInCuiSpostare nome della board di destinazione
     * @param nomeBachecaDiOrigine nome della board di origine
     * @return codice intero che indica il risultato dell'operazione
     */
    public int spostaToDoInBacheca(String email, String nomeToDo, String nomeBachecaInCuiSpostare, String nomeBachecaDiOrigine) {
        // codici di ritorno:
        // 0 = successo
        // 1 = to-do già presente nella bacheca di destinazione
        // 2 = to-do condiviso, non puoi spostarlo
        // 3 = utente o bacheca non trovati
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            // 1. Verifica esistenza utente
            String sqlCheckUser = "SELECT email FROM users WHERE email = ?";
            try (PreparedStatement psUser = conn.prepareStatement(sqlCheckUser)) {
                psUser.setString(1, email);
                try (ResultSet rsUser = psUser.executeQuery()) {
                    if (!rsUser.next()) {
                        logger.info("Utente non trovato...");
                        return 3;
                    }
                }
            }

            // 2. Prendi ID board origine
            Integer boardIdOrigine = getBoardId(conn, email, nomeBachecaDiOrigine);
            Integer boardIdDestinazione = getBoardId(conn, email, nomeBachecaInCuiSpostare);

            if (boardIdOrigine == null || boardIdDestinazione == null) {
                logger.info("Bacheca origine o destinazione non valida");
                return 3;
            }

            // 3. Verifica che ToDo esista nella board di origine e ne prendi i dati (compreso 'condiviso')
            String sqlCheckToDoOrigine = "SELECT id, condiviso FROM todos WHERE board_id = ? AND title = ?";
            Integer todoId = null;
            boolean condiviso = false;
            try (PreparedStatement psToDo = conn.prepareStatement(sqlCheckToDoOrigine)) {
                psToDo.setInt(1, boardIdOrigine);
                psToDo.setString(2, nomeToDo);
                try (ResultSet rsToDo = psToDo.executeQuery()) {
                    if (rsToDo.next()) {
                        todoId = rsToDo.getInt("id");
                        condiviso = rsToDo.getBoolean("condiviso");
                    } else {
                        logger.info("ToDo non trovato nella bacheca di origine");
                        return 3;
                    }
                }
            }

            if (condiviso) {
                logger.info("ToDo condiviso, non puoi spostarlo");
                return 2;
            }

            // 4. Verifica che ToDo non esista già nella board destinazione
            String sqlCheckToDoDest = "SELECT id FROM todos WHERE board_id = ? AND title = ?";
            try (PreparedStatement psCheckDest = conn.prepareStatement(sqlCheckToDoDest)) {
                psCheckDest.setInt(1, boardIdDestinazione);
                psCheckDest.setString(2, nomeToDo);
                try (ResultSet rsCheckDest = psCheckDest.executeQuery()) {
                    if (rsCheckDest.next()) {
                        logger.info("ToDo già presente nella bacheca di destinazione");
                        return 1;
                    }
                }
            }

            // 5. Aggiorna il board_id del todo per spostarlo
            String sqlUpdateToDo = "UPDATE todos SET board_id = ?, condiviso = false WHERE id = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateToDo)) {
                psUpdate.setInt(1, boardIdDestinazione);
                psUpdate.setInt(2, todoId);
                int rows = psUpdate.executeUpdate();
                if (rows == 0) {
                    logger.info("Errore nello spostamento del ToDo");
                    return 3;
                }
            }

            logger.info("ToDo spostato con successo");
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            logger.info("Errore database durante spostamento ToDo");
            return 3;
        }
    }

    // Metodo di supporto per recuperare ID board dato email e tipo (nomeBacheca)
    private Integer getBoardId(Connection conn, String email, String nomeBacheca) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BOARD_ID)) {
            ps.setString(1, email);
            ps.setString(2, nomeBacheca.toUpperCase()); // assumendo valori in maiuscolo
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    return null;
                }
            }
        }
    }
    /**
     * Verifica se tutte le attività associate a un ToDo sono completate.
     * Aggiorna di conseguenza lo stato del ToDo.
     *
     * @param toDoId ID del ToDo da verificare
     * @throws SQLException in caso di errore nell'accesso al database
     */
    public void checkIfComplete(int toDoId) throws SQLException {
        String checkSql = """
        SELECT a.state
        FROM activities a
        JOIN todos t ON a.checklist_id = t.checklist_id
        WHERE t.id = ?
         """;

        String updateSql = "UPDATE todos SET state = ? WHERE id = ?";

        Connection conn = this.connection;

        boolean allComplete = true;
        boolean hasActivities = false;

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, toDoId);
            ResultSet rs = checkStmt.executeQuery();

            while (rs.next()) {
                hasActivities = true; // almeno un'attività esiste
                boolean stato = rs.getBoolean("state");
                if (!stato) {
                    allComplete = false;
                    break;
                }
            }
        }

        // Se non ci sono attività, considera il ToDo incompleto
        boolean newState = hasActivities && allComplete;

        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setBoolean(1, newState);
            updateStmt.setInt(2, toDoId);
            updateStmt.executeUpdate();
        }
    }

    /**
     * Recupera lo stato (completato o meno) di un ToDo dato il suo ID.
     *
     * @param toDoId ID del ToDo
     * @return true se completato, false altrimenti
     * @throws SQLException se il ToDo non è trovato o errore DB
     */
    public boolean getStateById(int toDoId) throws SQLException {
        String sql = "SELECT state FROM todos WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, toDoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("state");
                } else {
                    throw new SQLException("ToDo non trovato");
                }
            }
        }
    }
}