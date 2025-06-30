package dao;

import db.ConnessioneDatabase;
import model.Activity;
import model.CheckList;
import model.ToDo;

import java.awt.Color;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ToDoDAO {

    private Connection conn;

    public ToDoDAO() {
        this.conn = ConnessioneDatabase.getInstance().getConnection();
    }

    public ToDoDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean addToDoInBoard(String email, String tipoEnum, ToDo toDo) {
        String boardSql = "SELECT id FROM boards WHERE user_email = ? AND type = ?";
        int boardId = -1;

        try (PreparedStatement boardStmt = conn.prepareStatement(boardSql)) {
            boardStmt.setString(1, email);
            boardStmt.setString(2, tipoEnum);
            ResultSet boardRs = boardStmt.executeQuery();

            if (boardRs.next()) {
                boardId = boardRs.getInt("id");
            } else {
                System.out.println("Board non trovata per l’utente: " + email);
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

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, boardId);               // ToDo locale
            checkStmt.setString(2, toDo.getTitle());

            checkStmt.setString(3, email);              // ToDo condiviso
            checkStmt.setString(4, tipoEnum);           // tipo della board (es. "WORK")
            checkStmt.setString(5, toDo.getTitle());

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Esiste già un ToDo con lo stesso titolo (locale o condiviso).");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        //  Inserisci checklist vuota
        int checklistId = -1;
        String insertChecklistSql = "INSERT INTO checklists DEFAULT VALUES RETURNING id";

        try (PreparedStatement checklistStmt = conn.prepareStatement(insertChecklistSql)) {
            ResultSet rs = checklistStmt.executeQuery();
            if (rs.next()) {
                checklistId = rs.getInt("id");
            } else {
                System.out.println("Errore nella creazione della checklist.");
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

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
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
            System.out.println("ToDo con checklist vuota inserito con successo.");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteToDo(String email, String boardType, String todoTitle) throws SQLException {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();

        // 1. Recupera l'ID della bacheca
        String boardQuery = "SELECT id FROM boards WHERE user_email = ? AND type = ?";
        try (PreparedStatement boardStmt = conn.prepareStatement(boardQuery)) {
            boardStmt.setString(1, email);
            boardStmt.setString(2, boardType.toUpperCase());

            ResultSet rsBoard = boardStmt.executeQuery();
            if (!rsBoard.next()) {
                System.out.println("Bacheca non trovata.");
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
                    System.out.println("ToDo non trovato.");
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
                            System.out.println("Solo l'amministratore può eliminare un ToDo condiviso.");
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

                System.out.println("ToDo eliminato con successo.");
                return true;
            }
        }
    }

    // Utilità per convertire il colore
    private String colorToHex(Color color) {
        if (color == null) return null;
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private Color hexToColor(String hex) {
        if (hex == null || hex.isEmpty()) return null;
        return Color.decode(hex);
    }


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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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


    public boolean setCondivisoTrueById(int todoId) {
        String sql = """
        UPDATE todos
        SET condiviso = TRUE
        WHERE id = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, todoId);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    public void setCondivisoFalseById(int todoId) {
        String sql = "UPDATE todos SET condiviso = FALSE WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, todoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //--------------abominio sposta to-Do

    public int spostaToDoInBacheca(String email, String nomeToDo, String nomeBachecaInCuiSpostare, String nomeBachecaDiOrigine) {
        // codici di ritorno:
        // 0 = successo
        // 1 = to-do già presente nella bacheca di destinazione
        // 2 = to-do condiviso, non puoi spostarlo
        // 3 = utente o bacheca non trovati
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection()) {

            // 1. Verifica esistenza utente
            String sqlCheckUser = "SELECT email FROM users WHERE email = ?";
            try (PreparedStatement psUser = conn.prepareStatement(sqlCheckUser)) {
                psUser.setString(1, email);
                try (ResultSet rsUser = psUser.executeQuery()) {
                    if (!rsUser.next()) {
                        System.out.println("Utente non trovato...");
                        return 3;
                    }
                }
            }

            // 2. Prendi ID board origine
            Integer boardIdOrigine = getBoardId(conn, email, nomeBachecaDiOrigine);
            Integer boardIdDestinazione = getBoardId(conn, email, nomeBachecaInCuiSpostare);

            if (boardIdOrigine == null || boardIdDestinazione == null) {
                System.out.println("Bacheca origine o destinazione non valida");
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
                        System.out.println("ToDo non trovato nella bacheca di origine");
                        return 3;
                    }
                }
            }

            if (condiviso) {
                System.out.println("ToDo condiviso, non puoi spostarlo");
                return 2;
            }

            // 4. Verifica che ToDo non esista già nella board destinazione
            String sqlCheckToDoDest = "SELECT id FROM todos WHERE board_id = ? AND title = ?";
            try (PreparedStatement psCheckDest = conn.prepareStatement(sqlCheckToDoDest)) {
                psCheckDest.setInt(1, boardIdDestinazione);
                psCheckDest.setString(2, nomeToDo);
                try (ResultSet rsCheckDest = psCheckDest.executeQuery()) {
                    if (rsCheckDest.next()) {
                        System.out.println("ToDo già presente nella bacheca di destinazione");
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
                    System.out.println("Errore nello spostamento del ToDo");
                    return 3;
                }
            }

            System.out.println("ToDo spostato con successo");
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore database durante spostamento ToDo");
            return 3;
        }
    }

    // Metodo di supporto per recuperare ID board dato email e tipo (nomeBacheca)
    private Integer getBoardId(Connection conn, String email, String nomeBacheca) throws SQLException {
        String sql = "SELECT id FROM boards WHERE user_email = ? AND type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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

    public void checkIfComplete(int toDoId) throws SQLException {
        String checkSql = """
        SELECT a.state
        FROM activities a
        JOIN todos t ON a.checklist_id = t.checklist_id
        WHERE t.id = ?
         """;

        String updateSql = "UPDATE todos SET state = ? WHERE id = ?";

        Connection conn = this.conn;

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
    public boolean getStateById(int toDoId) throws SQLException {
        String sql = "SELECT state FROM todos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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