package dao;

import model.Activity;
import model.CompletedActivityHistory;

import java.sql.*;
import java.util.ArrayList;

public class CompletedActivityHistoryDAO {

    private final Connection conn;

    public CompletedActivityHistoryDAO(Connection conn) {
        this.conn = conn;
    }

    // Carica la cronologia completata per un To-Do
    public CompletedActivityHistory leggiCompletedHistoryPerToDo(String todoTitle) {
        CompletedActivityHistory history = new CompletedActivityHistory();
        String sql = "SELECT * FROM completed_activity_history WHERE todo_title = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todoTitle);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Activity a = new Activity(
                        rs.getString("name"),
                        true // assumiamo che tutte qui siano completate
                );
                a.setCompletionDate(rs.getString("completion_date"));
                history.getActivityHistory().add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // Salva una nuova attività completata in cronologia per un To-Do
    public void creaActivityHistory(String todoTitle, Activity activity) {
        String sql = "INSERT INTO completed_activity_history (name, completion_date, todo_title) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activity.getName());
            stmt.setString(2, activity.getCompletionDate());
            stmt.setString(3, todoTitle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Rimuove una attività dalla cronologia completata di un ToDo
    public void eliminaActivityHistory(String todoTitle, String activityName) {
        String sql = "DELETE FROM completed_activity_history WHERE name = ? AND todo_title = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activityName);
            stmt.setString(2, todoTitle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Cancella tutta la cronologia completata di un ToDo
    public void eliminaTuttaHistoryPerToDo(String todoTitle) {
        String sql = "DELETE FROM completed_activity_history WHERE todo_title = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todoTitle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
