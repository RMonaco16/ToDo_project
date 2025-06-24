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

        //  CONTROLLO ESISTENZA ToDo CON STESSO TITOLO
        String checkSql = "SELECT id FROM todos WHERE board_id = ? AND LOWER(title) = LOWER(?)";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, boardId);
            checkStmt.setString(2, toDo.getTitle());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("ToDo con lo stesso titolo già presente nella board.");
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
                    String checkAdminQuery = "SELECT id FROM sharings WHERE todo_id = ? AND admin_email = ?";
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

    /*
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

    */

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

    public boolean isUserAdminOfToDo(String email, String boardType, String todoTitle) {
        String sql = """
        SELECT 1
        FROM todos
        JOIN boards ON todos.board_id = boards.id
        WHERE todos.title = ?
          AND boards.type = ?
          AND boards.user_email = ?
          AND todos.owner_email = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todoTitle);
            stmt.setString(2, boardType);
            stmt.setString(3, email); // proprietario della bacheca
            stmt.setString(4, email); // utente da verificare come admin

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // ritorna true se l'utente è admin
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // se errore o non trovato
    }

    public ArrayList<ToDo> getTodosByUserAndBoard(String email, String boardType) {
        ArrayList<ToDo> todos = new ArrayList<>();

        String sql = """
            SELECT t.id, t.title, t.description, t.color, t.image, t.expiration, 
                   t.state, t.condiviso, t.owner_email, t.checklist_id
            FROM todos t
            JOIN boards b ON t.board_id = b.id
            WHERE b.user_email = ? AND b.type = ?
            ORDER BY t.id ASC
        """;



        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType.toUpperCase());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ToDo todo = new ToDo();
                todo.setTitle(rs.getString("title"));
                todo.setDescription(rs.getString("description"));

                String colorHex = rs.getString("color");
                if (colorHex != null) {
                    todo.setColor(Color.decode(colorHex)); // es. "#FF0000"
                }

                todo.setImage(rs.getString("image"));

                Date expDate = rs.getDate("expiration");
                if (expDate != null) {
                    todo.setExpiration(expDate.toLocalDate());
                }

                todo.setState(rs.getBoolean("state"));
                todo.setCondiviso(rs.getBoolean("condiviso"));
                todo.setOwnerEmail(rs.getString("owner_email"));

                // Checklist vuota per ora (non carichiamo le attività qui)
                CheckList checkList = new CheckList();
                todo.setCheckList(checkList);

                todos.add(todo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todos;
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




}