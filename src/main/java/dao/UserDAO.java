package dao;

import model.User;
import model.Board;
import model.CompletedActivityHistory;

import java.sql.*;
import java.util.ArrayList;

public class UserDAO {

    private final Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    // Leggi utente da email (chiave primaria)
    public User leggiUserPerEmail(String email) {
        User user = null;
        String sql = "SELECT nickname, email, password FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String nickname = rs.getString("nickname");
                String password = rs.getString("password");

                user = new User(nickname, email, password);

                // TODO: Carica boards (se vuoi) qui, es:
                // Board[] boards = leggiBoardsPerUtente(email);
                // user.setBoards(boards);

                // TODO: Carica activityHistory se la gestisci nel DB
                // user.setActivityHistory(...);

                // TODO: Carica sharing se serve
                // user.setSharing(...);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Crea un nuovo utente
    public boolean creaUser(User user) {
        String sql = "INSERT INTO users (nickname, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getNickname());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Aggiorna dati utente (nickname e password)
    public boolean aggiornaUser(User user) {
        String sql = "UPDATE users SET nickname = ?, password = ? WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getNickname());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Elimina utente
    public boolean eliminaUser(String email) {
        String sql = "DELETE FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Metodo di esempio per leggere boards (da implementare)
    // Nota: devi avere DAO Board con metodo leggiBoardsPerUtente(email)
    /*
    public Board[] leggiBoardsPerUtente(String email) {
        BoardDAO boardDAO = new BoardDAO(conn);
        return boardDAO.leggiBoardsPerUtente(email);
    }
    */

}
