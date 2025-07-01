package dao;

import model.Sharing;
import model.ToDo;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * DAO (Data Access Object) per gestire le operazioni di persistenza e recupero
 * degli oggetti Sharing relativi ai ToDo condivisi.
 * Permette di leggere, creare, verificare e aggiornare condivisioni tra utenti.
 */
public class SharingDAO {

    private static final Logger logger = Logger.getLogger(SharingDAO.class.getName());

    private final Connection conn;

    /**
     * Costruisce un SharingDAO con la connessione al database fornita.
     *
     * @param conn Connessione al database da utilizzare per tutte le operazioni.
     */
    public SharingDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Recupera lo sharing associato a un ToDo identificato dal titolo,
     * nome della board e email dell'amministratore.
     *
     * Carica anche la lista dei membri condivisi.
     *
     * @param todoTitle titolo del ToDo
     * @param boardName nome della board a cui appartiene il ToDo
     * @param adminEmail email dell'amministratore del ToDo
     * @return oggetto Sharing completo oppure null se non trovato o in caso di errore
     */
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

    /**
     * Crea una nuova condivisione nel database, inserendo la condivisione
     * e i membri associati.
     *
     * @param sharing oggetto Sharing da salvare
     */
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
            int sharingId = -1;
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

    /**
     * Verifica se esiste già una condivisione per un dato amministratore e ToDo.
     *
     * @param admin utente amministratore
     * @param todo ToDo da controllare
     * @return Sharing se esiste, null altrimenti
     */
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

        try (PreparedStatement findTodoStmt = conn.prepareStatement(findTodoIdSql)) {
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

    /**
     * Controlla se un utente è già membro di uno sharing di un ToDo specifico.
     *
     * @param memberEmail email del membro da verificare
     * @param todoTitle titolo del ToDo
     * @param adminEmail email dell'amministratore del ToDo
     * @return true se l'utente è già membro dello sharing, false altrimenti
     */
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

    /**
     * Aggiunge un membro a una condivisione esistente identificata da amministratore e ToDo.
     *
     * @param emailDestinatario email del membro da aggiungere
     * @param adminEmail email dell'amministratore del ToDo
     * @param todoTitle titolo del ToDo
     */
    public void aggiungiMembroSharing(String emailDestinatario, String adminEmail, String todoTitle) {
        String getTodoIdSql = "SELECT id FROM todos WHERE title = ? AND owner_email = ? LIMIT 1";
        String getSharingIdSql = "SELECT id FROM sharings WHERE todo_id = ? AND administrator_email = ? LIMIT 1";
        String insertMemberSql = "INSERT INTO sharing_members (sharing_id, member_email) VALUES (?, ?)";

        try {
            int todoId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(getTodoIdSql)) {
                stmt.setString(1, todoTitle);
                stmt.setString(2, adminEmail);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        todoId = rs.getInt("id");
                    } else {
                        logger.info("ToDo non trovato");
                        return;
                    }
                }
            }

            int sharingId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(getSharingIdSql)) {
                stmt.setInt(1, todoId);
                stmt.setString(2, adminEmail);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        sharingId = rs.getInt("id");
                    } else {
                        logger.info("Sharing non trovato");
                        return;
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertMemberSql)) {
                stmt.setInt(1, sharingId);
                stmt.setString(2, emailDestinatario);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restituisce il nickname dell'amministratore della condivisione
     * associata al ToDo e all'utente specificati.
     *
     * @param email email dell'utente (admin o membro)
     * @param todoTitle titolo del ToDo
     * @return nickname dell'amministratore o null se non trovato
     */
    public String getSharingAdministratorNick(String email, String todoTitle) {
        String sql = """
        SELECT u.nickname
        FROM users u
        JOIN sharings s ON u.email = s.administrator_email
        JOIN todos t ON s.todo_id = t.id
        LEFT JOIN sharing_members sm ON sm.sharing_id = s.id
        WHERE t.title = ? AND (u.email = ? OR sm.member_email = ?)
        LIMIT 1
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todoTitle);
            stmt.setString(2, email);
            stmt.setString(3, email);
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

    /**
     * Restituisce l'email dell'amministratore della condivisione
     * associata al ToDo e all'utente specificati.
     *
     * @param email email dell'utente (admin o membro)
     * @param todoTitle titolo del ToDo
     * @return email amministratore o null se non trovato
     */
    public String getSharingAdministratorEmail(String email, String todoTitle) {
        String sql = """
        SELECT s.administrator_email
        FROM sharings s
        JOIN todos t ON s.todo_id = t.id
        LEFT JOIN sharing_members sm ON sm.sharing_id = s.id
        WHERE t.title = ? AND (s.administrator_email = ? OR sm.member_email = ?)
        LIMIT 1
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, todoTitle);
            stmt.setString(2, email);
            stmt.setString(3, email);
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

    /**
     * Recupera la lista di utenti (amministratore e membri) che partecipano
     * alla condivisione di un ToDo specifico.
     *
     * @param email email dell'utente che richiede la lista (admin o membro)
     * @param toDoTitle titolo del ToDo
     * @return lista di utenti condivisi, vuota se nessuno trovato o errore
     */
    public ArrayList<User> getSharingUserShared(String email, String toDoTitle) {
        ArrayList<User> utentiCondivisi = new ArrayList<>();
        UserDAO userDAO = new UserDAO(conn);

        String sqlSharingId = """
        SELECT s.id
        FROM sharings s
        JOIN todos t ON s.todo_id = t.id
        LEFT JOIN sharing_members sm ON sm.sharing_id = s.id
        WHERE t.title = ? AND (s.administrator_email = ? OR sm.member_email = ?)
        LIMIT 1
        """;

        try (PreparedStatement stmtSharing = conn.prepareStatement(sqlSharingId)) {
            stmtSharing.setString(1, toDoTitle);
            stmtSharing.setString(2, email);
            stmtSharing.setString(3, email);

            try (ResultSet rsSharing = stmtSharing.executeQuery()) {
                if (rsSharing.next()) {
                    int sharingId = rsSharing.getInt("id");

                    // Prendi amministratore
                    String getAdminSql = """
                    SELECT administrator_email FROM sharings WHERE id = ?
                    """;
                    try (PreparedStatement adminStmt = conn.prepareStatement(getAdminSql)) {
                        adminStmt.setInt(1, sharingId);
                        try (ResultSet adminRs = adminStmt.executeQuery()) {
                            if (adminRs.next()) {
                                String adminEmail = adminRs.getString("administrator_email");
                                User admin = userDAO.leggiUserPerEmail(adminEmail);
                                if (admin != null) {
                                    utentiCondivisi.add(admin);
                                }
                            }
                        }
                    }

                    // Prendi membri
                    String getMembersSql = "SELECT member_email FROM sharing_members WHERE sharing_id = ?";
                    try (PreparedStatement membersStmt = conn.prepareStatement(getMembersSql)) {
                        membersStmt.setInt(1, sharingId);
                        try (ResultSet membersRs = membersStmt.executeQuery()) {
                            while (membersRs.next()) {
                                String memberEmail = membersRs.getString("member_email");
                                User member = userDAO.leggiUserPerEmail(memberEmail);
                                if (member != null) {
                                    utentiCondivisi.add(member);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return utentiCondivisi;
    }

    /**
     * Recupera la lista dei titoli dei ToDo non condivisi e di quelli condivisi
     * amministrati dall'utente, filtrati per tipo di bacheca.
     *
     * @param emailUtente email dell'utente
     * @param tipoBacheca tipo di bacheca (es. "PERSONAL", "TEAM")
     * @return lista di titoli di ToDo ordinati per posizione e titolo
     */
    public ArrayList<String> getToDoTitlesAdminNonCondivisi(String emailUtente, String tipoBacheca) {
        ArrayList<String> todoTitles = new ArrayList<>();

        // Query per titoli ToDo non condivisi
        String nonCondivisiSql = """
        SELECT t.title
        FROM todos t
        JOIN boards b ON t.board_id = b.id
        WHERE t.owner_email = ? AND b.type = ? AND NOT EXISTS (
            SELECT 1 FROM sharings s WHERE s.todo_id = t.id
        )
        """;

        // Query per titoli ToDo condivisi e amministrati dall'utente
        String condivisiSql = """
        SELECT t.title
        FROM todos t
        JOIN boards b ON t.board_id = b.id
        JOIN sharings s ON s.todo_id = t.id
        WHERE t.owner_email = ? AND b.type = ?
        """;

        try {
            try (PreparedStatement stmt = conn.prepareStatement(nonCondivisiSql)) {
                stmt.setString(1, emailUtente);
                stmt.setString(2, tipoBacheca);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        todoTitles.add(rs.getString("title"));
                    }
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(condivisiSql)) {
                stmt.setString(1, emailUtente);
                stmt.setString(2, tipoBacheca);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        todoTitles.add(rs.getString("title"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return todoTitles;
    }

    /**
     * Rimuove un membro dalla condivisione di un ToDo specifico.
     *
     * @param emailUtente email del membro da rimuovere
     * @param todoId identificativo del ToDo
     * @return true se la rimozione è andata a buon fine, false altrimenti
     */
    public boolean rimuoviMembroSharing(String emailUtente, int todoId) {
        String getSharingIdSql = "SELECT id FROM sharings WHERE todo_id = ? LIMIT 1";
        String deleteMemberSql = "DELETE FROM sharing_members WHERE sharing_id = ? AND member_email = ?";

        try {
            int sharingId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(getSharingIdSql)) {
                stmt.setInt(1, todoId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        sharingId = rs.getInt("id");
                    } else {
                        logger.info("Sharing non trovato per todoId " + todoId);
                        return false;
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(deleteMemberSql)) {
                stmt.setInt(1, sharingId);
                stmt.setString(2, emailUtente);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina una condivisione se non ha membri associati.
     *
     * @param todoId identificativo del ToDo
     */
    public void eliminaSharingSeVuoto(int todoId) {
        String countMembersSql = "SELECT COUNT(*) AS count FROM sharings s JOIN sharing_members sm ON s.id = sm.sharing_id WHERE s.todo_id = ?";
        String deleteSharingSql = "DELETE FROM sharings WHERE todo_id = ?";

        try {
            int countMembers = 0;
            try (PreparedStatement stmt = conn.prepareStatement(countMembersSql)) {
                stmt.setInt(1, todoId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        countMembers = rs.getInt("count");
                    }
                }
            }

            if (countMembers == 0) {
                try (PreparedStatement stmt = conn.prepareStatement(deleteSharingSql)) {
                    stmt.setInt(1, todoId);
                    stmt.executeUpdate();
                    logger.info("Sharing eliminata perché senza membri per todoId: " + todoId);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Conta il numero di membri associati a una condivisione di un ToDo.
     *
     * @param todoId identificativo del ToDo
     * @return numero totale dei membri, 0 se nessuno o in caso di errore
     */
    public int countMembriSharing(int todoId) {
        String sql = """
        SELECT COUNT(sm.member_email) AS num_members
        FROM sharings s
        LEFT JOIN sharing_members sm ON s.id = sm.sharing_id
        WHERE s.todo_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, todoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("num_members");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
