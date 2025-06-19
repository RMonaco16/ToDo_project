package dao;

import db.ConnessioneDatabase;
import model.Board;
import model.ToDo;
import model.TypeBoard;
import model.User;

import java.sql.*;
import java.util.ArrayList;

public class BoardDAO {

    private final Connection conn;

    public BoardDAO(Connection conn) {
        this.conn = conn; // usa quella che ti viene passata
    }

    // Crea una nuova board
    public boolean creaBoard(Board board, String userEmail) {
        String sql = "INSERT INTO boards (type, description, user_email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, board.getType().name());
            stmt.setString(2, board.getDescription());
            stmt.setString(3, userEmail);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


/*
    // Leggi una board per tipo
    public Board leggiBoard(String type) {
        String sql = "SELECT * FROM boards WHERE type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Board board = new Board(
                        TypeBoard.valueOf(rs.getString("type")),
                        rs.getString("description")
                );

                // Caricamento ToDo associati
                ToDoDAO toDoDAO = new ToDoDAO();
                board.setToDo(toDoDAO.leggiToDoPerBoard(type));
                return board;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
*/
    // Elimina board per tipo
    public void eliminaBoard(String email, String type) {
        String sql = "DELETE FROM boards WHERE user_email = ? AND type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Aggiorna descrizione (o altri dati) di una board identificata dal tipo
    public void aggiornaBoard(Board board) {
        String sql = "UPDATE boards SET description = ? WHERE type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, board.getDescription());
            stmt.setString(2, board.getType().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Leggi tutte le board
    public ArrayList<Board> leggiTutteLeBoard() {
        ArrayList<Board> boards = new ArrayList<>();
        String sql = "SELECT * FROM board";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Board board = new Board(
                        TypeBoard.valueOf(rs.getString("type")),
                        rs.getString("description")
                );
                boards.add(board);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return boards;
    }

    public ArrayList<Board> getBoardsByEmail(String email) {
        ArrayList<Board> boards = new ArrayList<>();
        String sql = "SELECT type, description FROM boards WHERE user_email = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String typeStr = rs.getString("type");
                TypeBoard type = TypeBoard.valueOf(typeStr); // enum: UNIVERSITY, WORK, FREETIME
                String description = rs.getString("description");

                Board board = new Board(type, description);
                boards.add(board);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return boards;
    }


}
