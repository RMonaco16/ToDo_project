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
    public void creaActivity(String todoTitle, Activity activity) {
        String sql = "INSERT INTO activity (name, state, completion_date, todo_title) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activity.getName());
            stmt.setBoolean(2, activity.getState());
            stmt.setString(3, activity.getCompletionDate());
            stmt.setString(4, todoTitle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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

