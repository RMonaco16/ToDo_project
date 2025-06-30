package dao;

import db.ConnessioneDatabase;
import model.*;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class BoardDAO {

    private final Connection conn;

    public BoardDAO(Connection conn) {
        this.conn = conn; // usa quella che ti viene passata
    }

    // Crea una nuova board
    public boolean creaBoard(Board board, String userEmail) {
        String checkSql = "SELECT id FROM boards WHERE user_email = ? AND type = ?";
        String insertSql = "INSERT INTO boards (type, description, user_email) VALUES (?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, userEmail);
            checkStmt.setString(2, board.getType().name());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("La board di tipo '" + board.getType().name() + "' esiste già per l'utente: " + userEmail);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
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

    public ToDo checkToDoExists(String email, String boardType, String todoTitle){
        String sql = """
        SELECT t.title, t.description, t.color, t.image, t.expiration,
               t.state, t.condiviso, t.owner_email
        FROM todos t
        JOIN boards b ON t.board_id = b.id
        WHERE b.type = ? AND t.title = ? AND b.user_email = ?
        LIMIT 1
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, boardType);
            stmt.setString(2, todoTitle);
            stmt.setString(3, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    boolean state = rs.getBoolean("state");
                    boolean condiviso = rs.getBoolean("condiviso");
                    String ownerEmail = rs.getString("owner_email");

                    // Supponiamo di non avere ancora una CheckList associata qui
                    CheckList checkList = null;

                    // Costruttore base
                    ToDo todo = new ToDo(title, state, checkList, condiviso, ownerEmail);

                    // Aggiunta campi opzionali
                    todo.setDescription(rs.getString("description"));

                    String hexColor = rs.getString("color");
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString("image"));

                    Date expDate = rs.getDate("expiration");
                    if (expDate != null) {
                        todo.setExpiration(expDate.toLocalDate());
                    }

                    return todo;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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

    public ArrayList<ToDo> getAllLocalToDos(String email, String nameBoard) {
        String sql = """
            SELECT todos.*
            FROM todos
            JOIN boards ON todos.board_id = boards.id
            JOIN users ON boards.user_email = users.email
            WHERE users.email = ? AND boards.type = ?
            ORDER BY todos.id ASC
        """;

        ArrayList<ToDo> todos = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, nameBoard);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ToDo todo = new ToDo(); // usa costruttore vuoto
                    todo.setTitle(rs.getString("title"));
                    todo.setDescription(rs.getString("description"));

                    // Converte String "#RRGGBB" in java.awt.Color
                    String hexColor = rs.getString("color");
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString("image"));

                    // Conversione da java.sql.Date a java.time.LocalDate
                    Date sqlDate = rs.getDate("expiration");
                    if (sqlDate != null) {
                        todo.setExpiration(sqlDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean("state"));
                    todo.setCondiviso(rs.getBoolean("condiviso"));
                    todo.setOwnerEmail(rs.getString("owner_email"));

                    // Per ora lascia CheckList null finché non implementi join con checklists
                    todo.setCheckList(null);

                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // o gestisci meglio con logger/exception handling
        }

        return todos;
    }

    public ArrayList<ToDo> getAllSharedToDos(String email, String boardType) {
        String sql = """
            SELECT todos.*
            FROM todos
            JOIN sharings ON todos.id = sharings.todo_id
            JOIN sharing_members ON sharings.id = sharing_members.sharing_id
            JOIN boards ON todos.board_id = boards.id
            WHERE sharing_members.member_email = ? AND boards.type = ?
            ORDER BY todos.id ASC
        """;


        ArrayList<ToDo> sharedTodos = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ToDo todo = new ToDo();
                    todo.setTitle(rs.getString("title"));
                    todo.setDescription(rs.getString("description"));

                    String hexColor = rs.getString("color");
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString("image"));

                    Date sqlDate = rs.getDate("expiration");
                    if (sqlDate != null) {
                        todo.setExpiration(sqlDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean("state"));
                    todo.setCondiviso(rs.getBoolean("condiviso"));
                    todo.setOwnerEmail(rs.getString("owner_email"));

                    todo.setCheckList(null); // opzionale, se non gestisci la checklist qui

                    sharedTodos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // oppure gestione con logger
        }

        return sharedTodos;
    }

    public ArrayList<ToDo> getLocalTodosByExpirationDate(String email, String boardType) {
        String sql = """
        SELECT todos.*
        FROM todos
        JOIN boards ON todos.board_id = boards.id
        WHERE boards.user_email = ? 
          AND boards.type = ?
          AND todos.expiration = ?
    """;

        ArrayList<ToDo> todos = new ArrayList<>();
        LocalDate date = LocalDate.now();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType);
            stmt.setDate(3, Date.valueOf(date));  // Conversione da LocalDate a java.sql.Date

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ToDo todo = new ToDo();
                    todo.setTitle(rs.getString("title"));
                    todo.setDescription(rs.getString("description"));

                    String hexColor = rs.getString("color");
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString("image"));

                    Date sqlDate = rs.getDate("expiration");
                    if (sqlDate != null) {
                        todo.setExpiration(sqlDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean("state"));
                    todo.setCondiviso(rs.getBoolean("condiviso"));
                    todo.setOwnerEmail(rs.getString("owner_email"));
                    todo.setCheckList(null);

                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todos;
    }

    public ArrayList<ToDo> getLocalTodosExpiringBeforeOrOn(String email, String boardType, LocalDate date) {
        String sql = """
            SELECT todos.*
            FROM todos
            JOIN boards ON todos.board_id = boards.id
            WHERE boards.user_email = ?
              AND boards.type = ?
              AND todos.expiration IS NOT NULL
              AND todos.expiration <= ?
        """;

        ArrayList<ToDo> todos = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType);
            stmt.setDate(3, Date.valueOf(date)); // conversione da LocalDate a java.sql.Date

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ToDo todo = new ToDo();
                    todo.setTitle(rs.getString("title"));
                    todo.setDescription(rs.getString("description"));

                    String hexColor = rs.getString("color");
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString("image"));

                    Date sqlDate = rs.getDate("expiration");
                    if (sqlDate != null) {
                        todo.setExpiration(sqlDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean("state"));
                    todo.setCondiviso(rs.getBoolean("condiviso"));
                    todo.setOwnerEmail(rs.getString("owner_email"));
                    todo.setCheckList(null);

                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todos;
    }

    public ArrayList<ToDo> getSharedTodosExpiringBeforeOrOn(String email, String boardType, LocalDate date) {
        String sql = """
        SELECT todos.*
        FROM todos
        JOIN sharings ON todos.id = sharings.todo_id
        JOIN sharing_members ON sharings.id = sharing_members.sharing_id
        JOIN boards ON todos.board_id = boards.id
        WHERE sharing_members.member_email = ?
          AND boards.type = ?
          AND todos.expiration IS NOT NULL
          AND todos.expiration <= ?
    """;

        ArrayList<ToDo> todos = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType);
            stmt.setDate(3, Date.valueOf(date));  // LocalDate -> java.sql.Date

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("expiration");
                    if (sqlDate == null) continue; // ulteriore sicurezza

                    ToDo todo = new ToDo();
                    todo.setTitle(rs.getString("title"));
                    todo.setDescription(rs.getString("description"));

                    String hexColor = rs.getString("color");
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString("image"));
                    todo.setExpiration(sqlDate.toLocalDate());
                    todo.setState(rs.getBoolean("state"));
                    todo.setCondiviso(rs.getBoolean("condiviso"));
                    todo.setOwnerEmail(rs.getString("owner_email"));
                    todo.setCheckList(null); // opzionale

                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todos;
    }

    public ToDo findToDoByTitleInBoard(String email, String boardType, String title) {
        String sql = """
    SELECT todos.*, false AS is_shared
    FROM todos
    JOIN boards ON todos.board_id = boards.id
    WHERE boards.user_email = ?
      AND LOWER(boards.type) = LOWER(?)
      AND LOWER(todos.title) = LOWER(?)

    UNION

    SELECT todos.*, true AS is_shared
    FROM todos
    JOIN sharings ON sharings.todo_id = todos.id
    JOIN sharing_members ON sharing_members.sharing_id = sharings.id
    JOIN boards ON todos.board_id = boards.id
    WHERE sharing_members.member_email = ?
      AND LOWER(boards.type) = LOWER(?)
      AND LOWER(todos.title) = LOWER(?)
    """;


        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Per la prima parte (locali)
            stmt.setString(1, email);   // boards.user_email
            stmt.setString(2, boardType);   // boards.type
            stmt.setString(3, title);   // todos.title

            // Per la seconda parte (condivisi)
            stmt.setString(4, email);   // sharing_members.member_email
            stmt.setString(5, boardType);   // boards.type
            stmt.setString(6, title);   // todos.title
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ToDo todo = new ToDo();
                    todo.setTitle(rs.getString("title"));
                    todo.setDescription(rs.getString("description"));

                    String hexColor = rs.getString("color");
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString("image"));

                    Date sqlDate = rs.getDate("expiration");
                    if (sqlDate != null) {
                        todo.setExpiration(sqlDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean("state"));
                    todo.setCondiviso(rs.getBoolean("condiviso"));
                    todo.setOwnerEmail(rs.getString("owner_email"));
                    todo.setCheckList(null); // opzionale

                    return todo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Nessun ToDo trovato
    }



}
