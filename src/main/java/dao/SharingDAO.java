package dao;

import model.Sharing;
import model.ToDo;
import model.User;

import java.sql.*;
import java.util.ArrayList;

public class SharingDAO {

    private final Connection conn;

    public SharingDAO(Connection conn) {
        this.conn = conn;
    }

    // Carica Sharing da un ToDo (identificato dal titolo)
    public Sharing leggiSharingPerToDo(String todoTitle) {
        Sharing sharing = null;
        try {
            // 1. Leggi amministratore e ToDo
            String sqlSharing = "SELECT administrator_email FROM sharing WHERE todo_title = ?";
            PreparedStatement stmt = conn.prepareStatement(sqlSharing);
            stmt.setString(1, todoTitle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String adminEmail = rs.getString("administrator_email");

                // Recupera amministratore e ToDo (dovresti avere DAO User e ToDo)
                User admin = new UserDAO(conn).leggiUserPerEmail(adminEmail);
                ToDo todo = new ToDoDAO(conn).leggiToDoPerTitolo(todoTitle);

                sharing = new Sharing(admin, todo);

                // 2. Leggi membri
                String sqlMembers = "SELECT member_email FROM sharing_members WHERE todo_title = ?";
                PreparedStatement stmtMembers = conn.prepareStatement(sqlMembers);
                stmtMembers.setString(1, todoTitle);
                ResultSet rsMembers = stmtMembers.executeQuery();

                ArrayList<User> members = new ArrayList<>();
                UserDAO userDAO = new UserDAO(conn);
                while (rsMembers.next()) {
                    String memberEmail = rsMembers.getString("member_email");
                    User member = userDAO.leggiUserPerEmail(memberEmail);
                    if (member != null) {
                        members.add(member);
                    }
                }
                sharing.setMembers(members);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sharing;
    }

    // Crea un nuovo sharing
    public void creaSharing(Sharing sharing) {
        String sqlInsertSharing = "INSERT INTO sharing (todo_title, administrator_email) VALUES (?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sqlInsertSharing);
            stmt.setString(1, sharing.getToDo().getTitle());
            stmt.setString(2, sharing.getAdministrator().getEmail());
            stmt.executeUpdate();

            // Inserisci membri
            if (sharing.getMembers() != null && !sharing.getMembers().isEmpty()) {
                String sqlInsertMember = "INSERT INTO sharing_members (todo_title, member_email) VALUES (?, ?)";
                PreparedStatement stmtMember = conn.prepareStatement(sqlInsertMember);
                for (User member : sharing.getMembers()) {
                    stmtMember.setString(1, sharing.getToDo().getTitle());
                    stmtMember.setString(2, member.getEmail());
                    stmtMember.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Aggiorna membri (sostituisce tutti i membri)
    public void aggiornaMembers(Sharing sharing) {
        try {
            // 1. Elimina vecchi membri
            String sqlDeleteMembers = "DELETE FROM sharing_members WHERE todo_title = ?";
            PreparedStatement stmtDelete = conn.prepareStatement(sqlDeleteMembers);
            stmtDelete.setString(1, sharing.getToDo().getTitle());
            stmtDelete.executeUpdate();

            // 2. Inserisci nuovi membri
            if (sharing.getMembers() != null && !sharing.getMembers().isEmpty()) {
                String sqlInsertMember = "INSERT INTO sharing_members (todo_title, member_email) VALUES (?, ?)";
                PreparedStatement stmtInsert = conn.prepareStatement(sqlInsertMember);
                for (User member : sharing.getMembers()) {
                    stmtInsert.setString(1, sharing.getToDo().getTitle());
                    stmtInsert.setString(2, member.getEmail());
                    stmtInsert.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Cambia amministratore
    public void aggiornaAdministrator(Sharing sharing) {
        String sql = "UPDATE sharing SET administrator_email = ? WHERE todo_title = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, sharing.getAdministrator().getEmail());
            stmt.setString(2, sharing.getToDo().getTitle());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Elimina sharing (e membri)
    public void eliminaSharing(String todoTitle) {
        try {
            // Elimina membri
            String sqlDeleteMembers = "DELETE FROM sharing_members WHERE todo_title = ?";
            PreparedStatement stmtDelMembers = conn.prepareStatement(sqlDeleteMembers);
            stmtDelMembers.setString(1, todoTitle);
            stmtDelMembers.executeUpdate();

            // Elimina sharing
            String sqlDeleteSharing = "DELETE FROM sharing WHERE todo_title = ?";
            PreparedStatement stmtDelSharing = conn.prepareStatement(sqlDeleteSharing);
            stmtDelSharing.setString(1, todoTitle);
            stmtDelSharing.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Sharing checkSharingExists(User admin, ToDo todo) {
        String sql = """
        SELECT 1
        FROM sharings s
        JOIN todos t ON s.todo_id = t.id
        WHERE s.administrator_email = ? AND t.title = ?
        LIMIT 1
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, admin.getEmail());
            stmt.setString(2, todo.getTitle());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Sharing(admin, todo);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkUserAlreadySharing(String memberEmail, String todoTitle, String adminEmail) {
        String sql = """
        SELECT 1
        FROM sharing_members sm
        JOIN sharings s ON sm.sharing_id = s.id
        JOIN todos t ON s.todo_id = t.id
        WHERE sm.member_email = ? AND t.title = ? AND s.administrator_email = ?
        LIMIT 1
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberEmail);
            stmt.setString(2, todoTitle);
            stmt.setString(3, adminEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // true se l'utente è già membro
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
