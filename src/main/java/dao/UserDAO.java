package dao;
import model.User;
import java.sql.*;
import java.util.logging.Logger;

public class UserDAO {

    private final Connection conn;
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    /**
     * Costruttore che inizializza il DAO con una connessione al database.
     *
     * @param conn Connessione al database da utilizzare
     */
    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Legge un utente dal database tramite email (chiave primaria).
     *
     * @param email Email dell’utente da cercare
     * @return Oggetto User se trovato, altrimenti null
     */
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

                // TO-DO: Carica boards (se vuoi) qui, es:
                // TO-DO: Carica activityHistory se la gestisci nel DB

                // TO-DO: Carica sharing se serve
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Crea un nuovo utente nel database.
     *
     * @param user Oggetto User da inserire
     * @return true se l’utente è stato creato con successo, false altrimenti
     */
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

    /**
     * Controlla se un’email esiste già nel database.
     *
     * @param email Email da controllare
     * @return true se l’email esiste, false altrimenti
     */
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

    /**
     * Verifica se esistono l’email e la password associati ad un utente nel database.
     *
     * @param email Email dell’utente
     * @param password Password associata
     * @return Oggetto User se credenziali corrette, null altrimenti
     */
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

    /**
     * Controlla se una bacheca esiste per un utente specifico.
     *
     * @param email Email dell’utente proprietario della bacheca
     * @param nameBoard Nome della bacheca (UNIVERSITY, WORK, FREETIME)
     * @return true se la bacheca esiste, false altrimenti
     */
    public boolean checkBoard(String email, String nameBoard){
        // Controllo nomi validi
        if (!nameBoard.equals("UNIVERSITY") && !nameBoard.equals("WORK") && !nameBoard.equals("FREETIME")) {
            logger.info("Nome bacheca non valido.");
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
            logger.info("Bacheca non trovata");
            e.printStackTrace();
        }
        return false;
    }
}
