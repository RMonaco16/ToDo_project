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


    public boolean creaToDo(ToDo todo) {
        String sql = "INSERT INTO todo (title, description, color, position, image, expiration, state, condiviso, owner_email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todo.getTitle());
            stmt.setString(2, todo.getDescription());
            stmt.setString(3, colorToHex(todo.getColor()));
            stmt.setInt(4, todo.getPosition());
            stmt.setString(5, todo.getImage());
            stmt.setDate(6, todo.getExpiration() != null ? Date.valueOf(todo.getExpiration()) : null);
            stmt.setBoolean(7, todo.isState());
            stmt.setBoolean(8, todo.isCondiviso());
            stmt.setString(9, todo.getOwnerEmail());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Errore durante inserimento ToDo: " + e.getMessage());
            return false;
        }
    }

    public ToDo leggiToDoPerTitolo(String title) {
        String sql = "SELECT * FROM todo WHERE title = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ToDo todo = new ToDo(
                        rs.getString("title"),
                        rs.getBoolean("state"),
                        null, // gestione checklist a parte
                        rs.getBoolean("condiviso"),
                        rs.getString("owner_email")
                );
                todo.setDescription(rs.getString("description"));
                todo.setColor(hexToColor(rs.getString("color")));
                todo.setPosition(rs.getInt("position"));
                todo.setImage(rs.getString("image"));
                todo.setExpiration(rs.getDate("expiration") != null ? rs.getDate("expiration").toLocalDate() : null);

                return todo;
            }
        } catch (SQLException e) {
            System.out.println("Errore durante lettura ToDo: " + e.getMessage());
        }
        return null;
    }

    public boolean aggiornaToDo(ToDo todo) {
        String sql = "UPDATE todo SET description = ?, color = ?, position = ?, image = ?, expiration = ?, state = ?, condiviso = ?, owner_email = ? WHERE title = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todo.getDescription());
            stmt.setString(2, colorToHex(todo.getColor()));
            stmt.setInt(3, todo.getPosition());
            stmt.setString(4, todo.getImage());
            stmt.setDate(5, todo.getExpiration() != null ? Date.valueOf(todo.getExpiration()) : null);
            stmt.setBoolean(6, todo.isState());
            stmt.setBoolean(7, todo.isCondiviso());
            stmt.setString(8, todo.getOwnerEmail());
            stmt.setString(9, todo.getTitle());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Errore durante aggiornamento ToDo: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminaToDo(String title) {
        String sql = "DELETE FROM todo WHERE title = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Errore durante eliminazione ToDo: " + e.getMessage());
            return false;
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

    //serve per caricare automaticamente tutti i To-Do collegati a una specifica Board.
    public ArrayList<ToDo> leggiToDoPerBoard(String boardType) {
        ArrayList<ToDo> listaToDo = new ArrayList<>();
        String sql = "SELECT * FROM todo WHERE type = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, boardType);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                boolean state = rs.getBoolean("state");
                boolean condiviso = rs.getBoolean("condiviso");
                String ownerEmail = rs.getString("owner_email");

                // Carica la checklist tramite il metodo DAO
                CheckList checkList = leggiCheckListPerToDo(title);

                ToDo todo = new ToDo(title, state, checkList, condiviso, ownerEmail);

                // ...altri campi
                todo.setDescription(rs.getString("description"));
                todo.setImage(rs.getString("image"));
                Date date = rs.getDate("expiration");
                if (date != null) {
                    todo.setExpiration(date.toLocalDate());
                }

                listaToDo.add(todo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listaToDo;
    }

    public CheckList leggiCheckListPerToDo(String todoTitle) {
        CheckList checkList = new CheckList();
        String sql = "SELECT * FROM activity WHERE todo_title = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todoTitle);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                boolean state = rs.getBoolean("state");
                String completionDate = rs.getString("completionDate");

                Activity activity = new Activity(name, state);
                activity.setCompletionDate(completionDate);

                checkList.addActivity(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return checkList;
    }


}