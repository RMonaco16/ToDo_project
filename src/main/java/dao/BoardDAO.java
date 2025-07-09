package dao;

import db.DatabaseConnection;
import model.*;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Data Access Object per la gestione delle board e dei relativi ToDo nel database.
 */
public class BoardDAO {
    private static final Logger logger = Logger.getLogger(BoardDAO.class.getName());
    private final Connection conn;

    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_COLOR = "color";
    private static final String COL_IMAGE = "image";
    private static final String COL_EXPIRATION = "expiration";
    private static final String COL_STATE = "state";
    private static final String COL_CONDIVISO = "condiviso";
    private static final String COL_OWNER = "owner_email";

    /**
     * Costruttore della classe BoardDAO.
     *
     * @param conn Connessione al database.
     */
    public BoardDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Crea una nuova board per un determinato utente, se non esiste già una board dello stesso tipo.
     *
     * @param board     Oggetto Board da creare.
     * @param userEmail Email dell'utente proprietario della board.
     * @return true se la board è stata creata con successo, false altrimenti.
     */
    public boolean creaBoard(Board board, String userEmail) {
        String checkSql = "SELECT id FROM boards WHERE user_email = ? AND type = ?";
        String insertSql = "INSERT INTO boards (type, description, user_email) VALUES (?, ?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, userEmail);
            checkStmt.setString(2, board.getType().name());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                logger.info("La board di tipo '" + board.getType().name() + "' esiste già per l'utente: " + userEmail);
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

    /**
     * Elimina una board di un utente in base al tipo.
     *
     * @param email Email dell'utente proprietario della board.
     * @param type  Tipo della board da eliminare.
     */
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

    /**
     * Verifica se un ToDo con titolo specifico esiste nella board di un utente.
     *
     * @param email      Email dell'utente.
     * @param boardType  Tipo della board (es. UNIVERSITY, WORK).
     * @param todoTitle  Titolo del ToDo da cercare.
     * @return Oggetto {@link ToDo} se trovato, null altrimenti.
     */
    public ToDo checkToDoExists(String email, String boardType, String todoTitle) {
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
                    String title = rs.getString(COL_TITLE);
                    boolean state = rs.getBoolean(COL_STATE);
                    boolean condiviso = rs.getBoolean(COL_CONDIVISO);
                    String ownerEmail = rs.getString(COL_OWNER);

                    CheckList checkList = null;

                    ToDo todo = new ToDo(title, state, checkList, condiviso, ownerEmail);
                    todo.setDescription(rs.getString(COL_DESCRIPTION));

                    String hexColor = rs.getString(COL_COLOR);
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString(COL_IMAGE));

                    Date expDate = rs.getDate(COL_EXPIRATION);
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

    /**
     * Ottiene tutte le board associate a un indirizzo email.
     *
     * @param email Email dell'utente.
     * @return Lista di oggetti {@link Board}.
     */
    public ArrayList<Board> getBoardsByEmail(String email) {
        ArrayList<Board> boards = new ArrayList<>();
        String sql = "SELECT type, description FROM boards WHERE user_email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String typeStr = rs.getString("type");
                TypeBoard type = TypeBoard.valueOf(typeStr);
                String description = rs.getString(COL_DESCRIPTION);

                Board board = new Board(type, description);
                boards.add(board);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return boards;
    }

    /**
     * Ottiene tutti i ToDo locali (non condivisi) per una board specifica dell'utente.
     *
     * @param email     Email dell'utente.
     * @param nameBoard Nome (tipo) della board.
     * @return Lista di oggetti {@link ToDo}.
     */
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
                    ToDo todo = new ToDo();
                    todo.setTitle(rs.getString(COL_TITLE));
                    todo.setDescription(rs.getString(COL_DESCRIPTION));

                    String hexColor = rs.getString(COL_COLOR);
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString(COL_IMAGE));

                    Date sqlDate = rs.getDate(COL_EXPIRATION);
                    if (sqlDate != null) {
                        todo.setExpiration(sqlDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean(COL_STATE));
                    todo.setCondiviso(rs.getBoolean(COL_CONDIVISO));
                    todo.setOwnerEmail(rs.getString(COL_OWNER));
                    todo.setCheckList(null);

                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todos;
    }

    /**
     * Ottiene tutti i ToDo condivisi con l'utente per una determinata board.
     *
     * @param email     Email dell'utente.
     * @param boardType Tipo della board.
     * @return Lista di oggetti {@link ToDo} condivisi.
     */
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
                    todo.setTitle(rs.getString(COL_TITLE));
                    todo.setDescription(rs.getString(COL_DESCRIPTION));

                    String hexColor = rs.getString(COL_COLOR);
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString(COL_IMAGE));

                    Date sqlDate = rs.getDate(COL_EXPIRATION);
                    if (sqlDate != null) {
                        todo.setExpiration(sqlDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean(COL_STATE));
                    todo.setCondiviso(rs.getBoolean(COL_CONDIVISO));
                    todo.setOwnerEmail(rs.getString(COL_OWNER));
                    todo.setCheckList(null);

                    sharedTodos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sharedTodos;
    }

    /**
     * Recupera tutti i ToDo locali (non condivisi) appartenenti a una board specifica
     * dell'utente che scadono nella data corrente (oggi).
     *
     * @param email L'indirizzo email dell'utente proprietario della board.
     * @param boardType Il tipo di board (es. "UNIVERSITY", "WORK", "FREETIME").
     * @return Una lista di oggetti {@link ToDo} con scadenza uguale alla data odierna.
     */
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
                    todo.setTitle(rs.getString(COL_TITLE));
                    todo.setDescription(rs.getString(COL_DESCRIPTION));

                    String hexColor = rs.getString(COL_COLOR);
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString(COL_IMAGE));

                    Date sqlDate = rs.getDate(COL_EXPIRATION);
                    if (sqlDate != null) {
                        todo.setExpiration(sqlDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean(COL_STATE));
                    todo.setCondiviso(rs.getBoolean(COL_CONDIVISO));
                    todo.setOwnerEmail(rs.getString(COL_OWNER));
                    todo.setCheckList(null);

                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todos;
    }

    /**
     * Recupera tutti i ToDo locali (non condivisi) appartenenti a una board specifica
     * dell'utente che hanno una data di scadenza precedente o uguale a quella specificata.
     *
     * @param email L'indirizzo email dell'utente proprietario della board.
     * @param boardType Il tipo di board (es. "UNIVERSITY", "WORK", "FREETIME").
     * @param date La data limite per il filtro (inclusiva).
     * @return Una lista di {@link ToDo} con scadenza minore o uguale alla data specificata.
     */

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
                    todo.setTitle(rs.getString(COL_TITLE));
                    todo.setDescription(rs.getString(COL_DESCRIPTION));

                    String hexColor = rs.getString(COL_COLOR);
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString(COL_IMAGE));

                    Date sqlDate = rs.getDate(COL_EXPIRATION);
                    if (sqlDate != null) {
                        todo.setExpiration(sqlDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean(COL_STATE));
                    todo.setCondiviso(rs.getBoolean(COL_CONDIVISO));
                    todo.setOwnerEmail(rs.getString(COL_OWNER));
                    todo.setCheckList(null);

                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todos;
    }

    /**
     * Recupera tutti i ToDo condivisi con l'utente, appartenenti a board di un certo tipo,
     * che hanno una data di scadenza precedente o uguale a quella specificata.
     *
     * @param email L'indirizzo email dell'utente destinatario della condivisione.
     * @param boardType Il tipo di board (es. "UNIVERSITY", "WORK", "FREETIME").
     * @param date La data limite per il filtro (inclusiva).
     * @return Una lista di {@link ToDo} condivisi in scadenza prima o entro la data specificata.
     */
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
                    Date sqlDate = rs.getDate(COL_EXPIRATION);
                    if (sqlDate == null) continue; // ulteriore sicurezza

                    ToDo todo = new ToDo();
                    todo.setTitle(rs.getString(COL_TITLE));
                    todo.setDescription(rs.getString(COL_DESCRIPTION));

                    String hexColor = rs.getString(COL_COLOR);
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString(COL_IMAGE));
                    todo.setExpiration(sqlDate.toLocalDate());
                    todo.setState(rs.getBoolean(COL_STATE));
                    todo.setCondiviso(rs.getBoolean(COL_CONDIVISO));
                    todo.setOwnerEmail(rs.getString(COL_OWNER));
                    todo.setCheckList(null); // opzionale

                    todos.add(todo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todos;
    }

    /**
     * Cerca un singolo ToDo con un titolo specifico in una board di un certo tipo,
     * considerando sia i ToDo locali (dell'utente) che quelli condivisi con l'utente.
     * <p>
     * Se esiste sia un ToDo locale che uno condiviso con lo stesso titolo e boardType,
     * viene restituito solo uno dei due (priorità non definita).
     * </p>
     *
     * @param email L'indirizzo email dell'utente (come proprietario o membro condiviso).
     * @param boardType Il tipo della board (es. "UNIVERSITY", "WORK", "FREETIME").
     * @param title Il titolo del ToDo da cercare.
     * @return Il {@link ToDo} trovato, oppure {@code null} se non esiste.
     */
    public ToDo findToDoByTitleInBoard(String email, String boardType, String title) {
        String sql = """
    SELECT todos.*, false AS is_shared
    FROM todos
    JOIN boards ON todos.board_id = boards.id
    WHERE boards.user_email = ?
      AND boards.type = ?
      AND todos.title = ?

    UNION

    SELECT todos.*, true AS is_shared
    FROM todos
    JOIN sharings ON todos.id = sharings.todo_id
    JOIN sharing_members ON sharings.id = sharing_members.sharing_id
    JOIN boards ON todos.board_id = boards.id
    WHERE sharing_members.member_email = ?
      AND boards.type = ?
      AND todos.title = ?
    LIMIT 1
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType);
            stmt.setString(3, title);
            stmt.setString(4, email);
            stmt.setString(5, boardType);
            stmt.setString(6, title);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ToDo todo = new ToDo();
                    todo.setTitle(rs.getString(COL_TITLE));
                    todo.setDescription(rs.getString(COL_DESCRIPTION));

                    String hexColor = rs.getString(COL_COLOR);
                    if (hexColor != null) {
                        todo.setColor(Color.decode(hexColor));
                    }

                    todo.setImage(rs.getString(COL_IMAGE));

                    Date expDate = rs.getDate(COL_EXPIRATION);
                    if (expDate != null) {
                        todo.setExpiration(expDate.toLocalDate());
                    }

                    todo.setState(rs.getBoolean(COL_STATE));
                    todo.setCondiviso(rs.getBoolean(COL_CONDIVISO));
                    todo.setOwnerEmail(rs.getString(COL_OWNER));
                    todo.setCheckList(null);
                    // is_shared si può leggere se vuoi, ma non è usato qui

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
}
