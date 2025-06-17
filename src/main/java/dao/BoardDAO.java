package dao;

import db.ConnessioneDatabase;
import model.Board;
import model.ToDo;
import model.TypeBoard;

import java.sql.*;
import java.util.ArrayList;

public class BoardDAO {

    private final Connection conn;

    public BoardDAO() {
        this.conn = ConnessioneDatabase.getInstance().getConnection();
    }

    // Crea una nuova board
    public void creaBoard(Board board) {
        String sql = "INSERT INTO board (type, description) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, board.getType().name());
            stmt.setString(2, board.getDescription());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Leggi una board per tipo
    public Board leggiBoard(String type) {
        String sql = "SELECT * FROM board WHERE type = ?";
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

    // Elimina board per tipo
    public void eliminaBoard(String type) {
        String sql = "DELETE FROM board WHERE type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Aggiorna descrizione (o altri dati) di una board identificata dal tipo
    public void aggiornaBoard(Board board) {
        String sql = "UPDATE board SET description = ? WHERE type = ?";
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
}
