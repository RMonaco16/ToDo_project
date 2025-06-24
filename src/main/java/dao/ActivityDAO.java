package dao;

import db.ConnessioneDatabase;
import model.Activity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO {

    private Connection conn;

    public ActivityDAO() {
        conn = ConnessioneDatabase.getInstance().getConnection();
    }

    //  Crea una nuova attività
    public void creaActivity(Activity activity) {
        String sql = "INSERT INTO activity (name, state, completion_date) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activity.getName());
            stmt.setBoolean(2, activity.getState());
            stmt.setString(3, activity.getCompletionDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  Leggi un'attività per nome
    public Activity leggiActivity(String name) {
        String sql = "SELECT * FROM activity WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Activity activity = new Activity(
                        rs.getString("name"),
                        rs.getBoolean("state")
                );
                activity.setCompletionDate(rs.getString("completion_date"));
                return activity;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //  Leggi tutte le attività
    public List<Activity> leggiTutte() {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT * FROM activity";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Activity a = new Activity(
                        rs.getString("name"),
                        rs.getBoolean("state")
                );
                a.setCompletionDate(rs.getString("completion_date"));
                activities.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activities;
    }

    //  Aggiorna un'attività
    public void aggiornaActivity(Activity activity) {
        String sql = "UPDATE activity SET state = ?, completion_date = ? WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, activity.getState());
            stmt.setString(2, activity.getCompletionDate());
            stmt.setString(3, activity.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  Elimina un'attività per nome
    public void eliminaActivity(String name) {
        String sql = "DELETE FROM activity WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
