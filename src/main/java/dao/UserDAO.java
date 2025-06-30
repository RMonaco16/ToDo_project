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

    //comntrolla se un utente gia esiste all'interno del db restituendo il risultato al controller
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true se l'email esiste
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //verifica se esistono l' email e la password associati ad un utente nel db
    public User getUserByEmailAndPassword(String email, String password) {
        String sql = "SELECT email, nickname, password FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("nickname"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // ritorna falso se non trova la bacheca
    public boolean checkBoard(String email, String nameBoard){
        // Controllo nomi validi
        if (!nameBoard.equals("UNIVERSITY") && !nameBoard.equals("WORK") && !nameBoard.equals("FREETIME")) {
            System.out.println("Nome bacheca non valido.");
            return false;
        }

        String sql = "SELECT 1 FROM boards WHERE user_email = ? AND type = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);       // imposta l'email utente
            stmt.setString(2, nameBoard);   // imposta il nome della bacheca

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // true se esiste almeno una riga
            }
        } catch (Exception e) {
            System.out.println("Bacheca non trovata");
            e.printStackTrace();
        }
        return false;
    }



}
