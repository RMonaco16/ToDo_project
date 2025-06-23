package dao;

import model.Activity;
import model.CheckList;

import java.sql.*;

public class CheckListDAO {

    private final Connection conn;

    public CheckListDAO(Connection conn) {
        this.conn = conn;
    }

    // Carica tutte le attività associate a un ToDo (identificato dal suo titolo)
    public CheckList leggiActivitiesPerToDo(String todoTitle) {
        CheckList checkList = new CheckList();
        String sql = "SELECT * FROM activity WHERE todo_title = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todoTitle);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Activity a = new Activity(
                        rs.getString("name"),
                        rs.getBoolean("state")
                );
                a.setCompletionDate(rs.getString("completion_date"));
                checkList.addActivity(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkList;
    }

    // Salva una nuova attività associata a un ToDo
    public boolean addActivity(String email, String titleToDo, String boardType, Activity activity) {
        try {
            // 1. Trova l'id della board
            String boardSql = "SELECT b.id FROM boards b WHERE b.user_email = ? AND b.type = ?";
            int boardId = -1;

            try (PreparedStatement stmt = conn.prepareStatement(boardSql)) {
                stmt.setString(1, email);
                stmt.setString(2, boardType);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    boardId = rs.getInt("id");
                } else {
                    System.out.println("Board non trovata per questo utente.");
                    return false;
                }
            }

            // 2. Trova il ToDo e la sua checklist
            String todoSql = """
            SELECT t.id, t.checklist_id
            FROM todos t
            WHERE t.board_id = ? AND LOWER(t.title) = LOWER(?)
        """;

            int checklistId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(todoSql)) {
                stmt.setInt(1, boardId);
                stmt.setString(2, titleToDo);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    checklistId = rs.getInt("checklist_id");
                } else {
                    System.out.println("ToDo non trovato nella board.");
                    return false;
                }
            }

            // 3. Inserisci la nuova activity nella tabella `activities`
            String insertSql = """
            INSERT INTO activities (name, state, completion_date, checklist_id)
            VALUES (?, ?, ?, ?)
        """;

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, activity.getName());
                stmt.setBoolean(2, activity.getState());
                if (activity.getCompletionDate() != null) {
                    stmt.setDate(3, Date.valueOf(activity.getCompletionDate()));
                } else {
                    stmt.setNull(3, Types.DATE);
                }
                stmt.setInt(4, checklistId);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Attività aggiunta con successo!");
                    return true;
                } else {
                    System.out.println("Errore nell'inserimento attività.");
                    return false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    // Aggiorna lo stato e la data di completamento di una attività
    public void aggiornaActivity(String todoTitle, Activity activity) {
        String sql = "UPDATE activity SET state = ?, completion_date = ? WHERE name = ? AND todo_title = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, activity.getState());
            stmt.setString(2, activity.getCompletionDate());
            stmt.setString(3, activity.getName());
            stmt.setString(4, todoTitle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Rimuove una attività da un ToDo
    public void eliminaActivity(String todoTitle, String activityName) {
        String sql = "DELETE FROM activity WHERE name = ? AND todo_title = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activityName);
            stmt.setString(2, todoTitle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

