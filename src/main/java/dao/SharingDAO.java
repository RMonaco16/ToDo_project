package dao;

import controller.ApplicationManagement;
import model.Sharing;
import model.ToDo;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class SharingDAO {

    private static final Logger logger = Logger.getLogger(SharingDAO.class.getName());


    private final Connection conn;

    public SharingDAO(Connection conn) {
        this.conn = conn;
    }

    // Carica Sharing da un ToDo (identificato dal titolo)
    public Sharing leggiSharingPerToDo(String todoTitle, String boardName, String adminEmail) {
        Sharing sharing = null;
        String sqlSharing = """
        SELECT s.id AS sharing_id, s.administrator_email, t.id AS todo_id
        FROM sharings s
        JOIN todos t ON s.todo_id = t.id
        JOIN boards b ON t.board_id = b.id
        WHERE t.title = ? AND b.name = ? AND t.administrator_email = ?
        LIMIT 1
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sqlSharing)) {
            stmt.setString(1, todoTitle);
            stmt.setString(2, boardName);
            stmt.setString(3, adminEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String admin = rs.getString("administrator_email");
                    int todoId = rs.getInt("todo_id");
                    int sharingId = rs.getInt("sharing_id");

                    UserDAO userDAO = new UserDAO(conn);
                    BoardDAO boardDAO = new BoardDAO(conn);

                    User adminUser = userDAO.leggiUserPerEmail(admin);
                    ToDo todo = boardDAO.findToDoByTitleInBoard(adminEmail, boardName, todoTitle);
                    sharing = new Sharing(adminUser, todo);

                    String sqlMembers = "SELECT member_email FROM sharing_members WHERE sharing_id = ?";
                    try (PreparedStatement stmtMembers = conn.prepareStatement(sqlMembers)) {
                        stmtMembers.setInt(1, sharingId);

                        try (ResultSet rsMembers = stmtMembers.executeQuery()) {
                            ArrayList<User> members = new ArrayList<>();
                            while (rsMembers.next()) {
                                String memberEmail = rsMembers.getString("member_email");
                                User member = userDAO.leggiUserPerEmail(memberEmail);
                                if (member != null) {
                                    members.add(member);
                                }
                            }
                            sharing.setMembers(members);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sharing;
    }

    // Crea un nuovo sharing
    public void creaSharing(Sharing sharing) {
        String getTodoIdSql = "SELECT id FROM todos WHERE title = ? AND owner_email = ? LIMIT 1";
        String insertSharingSql = "INSERT INTO sharings (todo_id, administrator_email) VALUES (?, ?)";
        String insertMemberSql = "INSERT INTO sharing_members (sharing_id, member_email) VALUES (?, ?)";

        try {
            // 1. Recupera l'id del To-Do dal titolo e amministratore
            int todoId = -1;
            try (PreparedStatement getTodoStmt = conn.prepareStatement(getTodoIdSql)) {
                getTodoStmt.setString(1, sharing.getToDo().getTitle());
                getTodoStmt.setString(2, sharing.getAdministrator().getEmail());
                try (ResultSet rs = getTodoStmt.executeQuery()) {
                    if (rs.next()) {
                        todoId = rs.getInt("id");
                    } else {
                         logger.info("ToDo non trovato con titolo e amministratore forniti.");
                        return;
                    }
                }
            }

            // 2. Inserisci nuova condivisione nella tabella sharings
            int sharingId=-1;
            try (PreparedStatement insertSharingStmt = conn.prepareStatement(insertSharingSql, Statement.RETURN_GENERATED_KEYS)) {
                insertSharingStmt.setInt(1, todoId);
                insertSharingStmt.setString(2, sharing.getAdministrator().getEmail());
                insertSharingStmt.executeUpdate();

                // Recupera l'id generato della condivisione (sharing)
                try (ResultSet generatedKeys = insertSharingStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        sharingId = generatedKeys.getInt(1);
                    } else {
                         logger.info("Errore: impossibile ottenere l'ID della condivisione appena creata.");
                        return;
                    }
                }
            }

            // 3. Inserisci i membri nella tabella sharing_members collegati alla condivisione appena creata
            if (sharing.getMembers() != null && !sharing.getMembers().isEmpty()) {
                try (PreparedStatement insertMemberStmt = conn.prepareStatement(insertMemberSql)) {
                    for (User member : sharing.getMembers()) {
                        insertMemberStmt.setInt(1, sharingId);
                        insertMemberStmt.setString(2, member.getEmail());
                        insertMemberStmt.executeUpdate();
                    }
                }
            }

             logger.info("Condivisione creata con successo!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Sharing checkSharingExists(User admin, ToDo todo) {
        String findTodoIdSql = """
        SELECT id FROM todos 
        WHERE title = ? AND owner_email = ?
        LIMIT 1
    """;

        String checkSharingSql = """
        SELECT 1
        FROM sharings
        WHERE administrator_email = ? AND todo_id = ?
        LIMIT 1
    """;

        try (
                PreparedStatement findTodoStmt = conn.prepareStatement(findTodoIdSql);
        ) {
            findTodoStmt.setString(1, todo.getTitle());
            findTodoStmt.setString(2, admin.getEmail());

            try (ResultSet todoRs = findTodoStmt.executeQuery()) {
                if (!todoRs.next()) {
                    return null; // ToDo non trovato
                }

                int todoId = todoRs.getInt("id");

                try (PreparedStatement checkStmt = conn.prepareStatement(checkSharingSql)) {
                    checkStmt.setString(1, admin.getEmail());
                    checkStmt.setInt(2, todoId);

                    try (ResultSet sharingRs = checkStmt.executeQuery()) {
                        if (sharingRs.next()) {
                            // Ricostruisci gli oggetti se servono
                            return new Sharing(admin, todo);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public boolean checkUserAlreadySharing(String memberEmail, String todoTitle, String adminEmail) {
        String getTodoIdSql = "SELECT id FROM todos WHERE title = ? AND owner_email = ? LIMIT 1";
        String checkSharingSql = """
        SELECT 1
        FROM sharing_members sm
        JOIN sharings s ON sm.sharing_id = s.id
        WHERE sm.member_email = ? AND s.todo_id = ? AND s.administrator_email = ?
        LIMIT 1
    """;

        try {
            // 1. Prendi l'id del todo dal titolo e amministratore
            int todoId = -1;
            try (PreparedStatement getTodoStmt = conn.prepareStatement(getTodoIdSql)) {
                getTodoStmt.setString(1, todoTitle);
                getTodoStmt.setString(2, adminEmail);
                try (ResultSet rs = getTodoStmt.executeQuery()) {
                    if (rs.next()) {
                        todoId = rs.getInt("id");
                    } else {
                         logger.info("ToDo non trovato con titolo e amministratore forniti.");
                        return false;
                    }
                }
            }

            // 2. Controlla se l'utente è già membro dello sharing
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSharingSql)) {
                checkStmt.setString(1, memberEmail);
                checkStmt.setInt(2, todoId);
                checkStmt.setString(3, adminEmail);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public void aggiungiMembroSharing(String emailDestinatario, String adminEmail, String todoTitle) {
        String getSharingIdSql = """
        SELECT s.id
        FROM sharings s
        JOIN todos t ON s.todo_id = t.id
        WHERE s.administrator_email = ? AND t.title = ? AND t.owner_email = ?
        LIMIT 1
    """;

        String insertMemberSql = """
        INSERT INTO sharing_members (member_email, sharing_id)
        VALUES (?, ?)
    """;

        try (
                PreparedStatement getIdStmt = conn.prepareStatement(getSharingIdSql)
        ) {
            getIdStmt.setString(1, adminEmail);
            getIdStmt.setString(2, todoTitle);
            getIdStmt.setString(3, adminEmail);

            try (ResultSet rs = getIdStmt.executeQuery()) {
                if (rs.next()) {
                    int sharingId = rs.getInt("id");

                    try (PreparedStatement insertStmt = conn.prepareStatement(insertMemberSql)) {
                        insertStmt.setString(1, emailDestinatario);
                        insertStmt.setInt(2, sharingId);
                        insertStmt.executeUpdate();
                         logger.info("Membro aggiunto correttamente alla condivisione.");
                    }
                } else {
                     logger.info("Nessuna condivisione trovata per il titolo e l’amministratore forniti.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiunta del membro alla condivisione:");
            e.printStackTrace();
        }
    }

    public String getSharingAdministratorNick(String email, String todoTitle) {
        String sql = """
        SELECT u.nickname
        FROM sharings s
        JOIN users u ON s.administrator_email = u.email
        JOIN todos t ON s.todo_id = t.id
        WHERE LOWER(t.title) = LOWER(?) 
          AND (LOWER(s.administrator_email) = LOWER(?) 
               OR EXISTS (
                   SELECT 1 
                   FROM sharing_members sm 
                   WHERE sm.sharing_id = s.id 
                     AND LOWER(sm.member_email) = LOWER(?)
               ))
        LIMIT 1
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todoTitle);
            stmt.setString(2, email); // se sei admin
            stmt.setString(3, email); // se sei membro

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nickname");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public String getSharingAdministratorEmail(String email, String todoTitle) {
        String sql = """
        SELECT s.administrator_email
        FROM sharings s
        JOIN todos t ON s.todo_id = t.id
        WHERE LOWER(t.title) = LOWER(?) 
          AND (LOWER(s.administrator_email) = LOWER(?) 
               OR EXISTS (
                   SELECT 1 
                   FROM sharing_members sm 
                   WHERE sm.sharing_id = s.id 
                     AND LOWER(sm.member_email) = LOWER(?)
               ))
        LIMIT 1
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todoTitle);
            stmt.setString(2, email); // se sei admin
            stmt.setString(3, email); // se sei membro

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("administrator_email");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<User> getSharingUserShared(String email, String toDoTitle) {
        ArrayList<User> listaUtenti = new ArrayList<>();
        String sql = """
        SELECT u.nickname, u.email, u.password
        FROM users u
        WHERE u.email IN (
            -- tutti i membri
            SELECT sm.member_email
            FROM sharing_members sm
            JOIN sharings s ON sm.sharing_id = s.id
            JOIN todos t ON s.todo_id = t.id
            WHERE LOWER(t.title) = LOWER(?)
              AND (LOWER(s.administrator_email) = LOWER(?) 
                   OR EXISTS (
                       SELECT 1
                       FROM sharing_members sm2
                       WHERE sm2.sharing_id = s.id 
                         AND LOWER(sm2.member_email) = LOWER(?)
                   ))
            UNION
            -- e amministratore
            SELECT s.administrator_email
            FROM sharings s
            JOIN todos t ON s.todo_id = t.id
            WHERE LOWER(t.title) = LOWER(?)
              AND (LOWER(s.administrator_email) = LOWER(?)
                   OR EXISTS (
                       SELECT 1 FROM sharing_members sm3
                       WHERE sm3.sharing_id = s.id
                         AND LOWER(sm3.member_email) = LOWER(?)
                   ))
        )
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, toDoTitle);
            stmt.setString(2, email);
            stmt.setString(3, email);
            stmt.setString(4, toDoTitle);
            stmt.setString(5, email);
            stmt.setString(6, email);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String nickname = rs.getString("nickname");
                    String userEmail = rs.getString("email");
                    String password = rs.getString("password");
                    listaUtenti.add(new User(nickname, userEmail, password));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listaUtenti;
    }


    public ArrayList<String> getToDoTitlesAdminNonCondivisi(String emailUtente, String tipoBacheca) {
        ArrayList<String> titles = new ArrayList<>();

        String sql = """
        SELECT t.title
        FROM todos t
        JOIN boards b ON t.board_id = b.id
        LEFT JOIN sharings s ON s.todo_id = t.id
        WHERE b.user_email = ?
          AND b.type = ?
          AND (
            (t.owner_email = ? AND t.condiviso = FALSE)  -- ToDo non condivisi dell'utente
            OR (s.administrator_email = ?)                -- oppure ToDo condivisi amministrati da lui
          )
        ORDER BY t.position, t.title
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailUtente);
            stmt.setString(2, tipoBacheca.toUpperCase());
            stmt.setString(3, emailUtente);
            stmt.setString(4, emailUtente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    titles.add(rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return titles;
    }

    public boolean rimuoviMembroSharing(String emailUtente, int todoId) {
        String sql = """
        DELETE FROM sharing_members
        WHERE LOWER(member_email) = LOWER(?) AND sharing_id = (
            SELECT s.id FROM sharings s WHERE s.todo_id = ?
        )
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailUtente);
            stmt.setInt(2, todoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void eliminaSharingSeVuoto(int todoId) {
        String sql = "DELETE FROM sharings WHERE todo_id = ? AND id NOT IN (SELECT sharing_id FROM sharing_members)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, todoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int countMembriSharing(int todoId) {
        String sql = """
        SELECT COUNT(*) AS total
        FROM sharing_members
        WHERE sharing_id = (
            SELECT id FROM sharings WHERE todo_id = ?
        )
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, todoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
