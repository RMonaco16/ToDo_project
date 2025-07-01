package dao;

import db.DatabaseConnection;
import model.ToDo;

import java.awt.Color;
import java.sql.*;
import java.time.LocalDate;
import java.util.logging.Logger;

/**
 * Classe DAO per la gestione delle operazioni CRUD sui ToDo nel database.
 * Si occupa di aggiungere, modificare, eliminare e recuperare ToDo associati alle board degli utenti.
 */
public class ToDoDAO {

    private static final Logger logger = Logger.getLogger(ToDoDAO.class.getName());

    private Connection conn;

    /**
     * Costruttore di default che utilizza la connessione singleton dal DatabaseConnection.
     */
    public ToDoDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Costruttore che accetta una connessione esterna.
     * @param conn Connessione al database da utilizzare
     */
    public ToDoDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Aggiunge un nuovo ToDo ad una board specifica dell'utente.
     * Verifica che non esista già un ToDo con lo stesso titolo (locale o condiviso).
     * Crea una checklist vuota associata al ToDo.
     *
     * @param email Email dell'utente proprietario della board
     * @param tipoEnum Tipo della board (es. "WORK")
     * @param toDo Oggetto ToDo da inserire
     * @return true se l'inserimento ha successo, false altrimenti
     */
    public boolean addToDoInBoard(String email, String tipoEnum, ToDo toDo) {
        try {
            // Controllo se esiste ToDo locale con lo stesso titolo
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT COUNT(*) FROM todo WHERE titolo = ? AND email_utente = ? AND condiviso = false");
            ps1.setString(1, toDo.getTitolo());
            ps1.setString(2, email);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            int countLocal = rs1.getInt(1);

            // Controllo se esiste ToDo condiviso con lo stesso titolo
            PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT COUNT(*) FROM todo WHERE titolo = ? AND condiviso = true");
            ps2.setString(1, toDo.getTitolo());
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            int countShared = rs2.getInt(1);

            if (countLocal > 0 || countShared > 0) {
                return false; // ToDo con titolo già esistente
            }

            // Recupero ID della board
            Integer idBacheca = getBoardId(conn, email, tipoEnum);
            if (idBacheca == null) return false;

            // Inserisco ToDo
            PreparedStatement psInsert = conn.prepareStatement(
                    "INSERT INTO todo (titolo, descrizione, data_scadenza, immagine, colore, condiviso, email_utente, id_bacheca) " +
                            "VALUES (?, ?, ?, ?, ?, false, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            psInsert.setString(1, toDo.getTitolo());
            psInsert.setString(2, toDo.getDescrizione());
            if (toDo.getDataScadenza() != null)
                psInsert.setDate(3, Date.valueOf(toDo.getDataScadenza()));
            else
                psInsert.setNull(3, Types.DATE);
            psInsert.setString(4, toDo.getImmagine());
            psInsert.setString(5, toDo.getColore());
            psInsert.setString(6, email);
            psInsert.setInt(7, idBacheca);

            int affectedRows = psInsert.executeUpdate();
            if (affectedRows == 0) return false;

            ResultSet generatedKeys = psInsert.getGeneratedKeys();
            if (generatedKeys.next()) {
                int todoId = generatedKeys.getInt(1);

                // Creo checklist vuota
                PreparedStatement psChecklist = conn.prepareStatement(
                        "INSERT INTO checklist (id_todo) VALUES (?)");
                psChecklist.setInt(1, todoId);
                psChecklist.executeUpdate();
            } else {
                return false;
            }

            return true;

        } catch (SQLException e) {
            logger.warning("Errore in addToDoInBoard: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un ToDo dalla board dell'utente.
     * Se il ToDo è condiviso, verifica che l'utente sia amministratore per poterlo eliminare.
     *
     * @param email Email dell'utente
     * @param boardType Tipo della board (es. "WORK")
     * @param todoTitle Titolo del ToDo da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     * @throws SQLException se si verifica un errore SQL
     */
    public boolean deleteToDo(String email, String boardType, String todoTitle) throws SQLException {
        // Controllo se il ToDo è condiviso
        PreparedStatement psCheck = conn.prepareStatement(
                "SELECT condiviso FROM todo WHERE titolo = ? AND email_utente = ?");
        psCheck.setString(1, todoTitle);
        psCheck.setString(2, email);
        ResultSet rsCheck = psCheck.executeQuery();

        if (rsCheck.next()) {
            boolean condiviso = rsCheck.getBoolean("condiviso");
            if (condiviso) {
                // Verifico se l'utente è admin
                if (!isUserAdminOfToDo(email, boardType, todoTitle)) {
                    return false;
                }
            }
        } else {
            return false; // ToDo non trovato
        }

        // Elimino ToDo
        PreparedStatement psDelete = conn.prepareStatement(
                "DELETE FROM todo WHERE titolo = ? AND email_utente = ?");
        psDelete.setString(1, todoTitle);
        psDelete.setString(2, email);

        int result = psDelete.executeUpdate();

        return result > 0;
    }

    /**
     * Verifica se l'utente è l'amministratore (proprietario) di un ToDo specifico.
     *
     * @param email Email dell'utente
     * @param boardName Nome/tipo della board
     * @param toDoTitle Titolo del ToDo
     * @return true se l'utente è amministratore del ToDo, false altrimenti
     */
    public boolean isUserAdminOfToDo(String email, String boardName, String toDoTitle) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM todo t " +
                            "JOIN bacheca b ON t.id_bacheca = b.id_bacheca " +
                            "WHERE t.titolo = ? AND t.email_utente = ? AND b.tipo = ?");
            ps.setString(1, toDoTitle);
            ps.setString(2, email);
            ps.setString(3, boardName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.warning("Errore in isUserAdminOfToDo: " + e.getMessage());
        }
        return false;
    }

    /**
     * Aggiorna i dati di un ToDo esistente in una board specifica.
     *
     * @param email Email dell'utente proprietario
     * @param boardType Tipo della board
     * @param oldTitle Titolo corrente del ToDo
     * @param newTitle Nuovo titolo del ToDo
     * @param description Nuova descrizione (può essere null)
     * @param expiration Nuova data di scadenza (può essere null)
     * @param image Nuovo link o riferimento all'immagine (può essere null)
     * @param color Nuovo colore associato al ToDo (può essere null)
     * @return true se l'aggiornamento ha successo, false altrimenti
     */
    public boolean updateToDo(String email, String boardType, String oldTitle,
                              String newTitle, String description, LocalDate expiration,
                              String image, Color color) {
        try {
            Integer idBacheca = getBoardId(conn, email, boardType);
            if (idBacheca == null) return false;

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE todo SET titolo = ?, descrizione = ?, data_scadenza = ?, immagine = ?, colore = ? " +
                            "WHERE titolo = ? AND email_utente = ? AND id_bacheca = ?");
            ps.setString(1, newTitle);
            ps.setString(2, description);

            if (expiration != null) {
                ps.setDate(3, Date.valueOf(expiration));
            } else {
                ps.setNull(3, Types.DATE);
            }

            ps.setString(4, image);
            ps.setString(5, color != null ? "#" + Integer.toHexString(color.getRGB()).substring(2) : null);

            ps.setString(6, oldTitle);
            ps.setString(7, email);
            ps.setInt(8, idBacheca);

            int updatedRows = ps.executeUpdate();
            return updatedRows > 0;

        } catch (SQLException e) {
            logger.warning("Errore in updateToDo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera l'ID di un ToDo dato il titolo, l'email dell'amministratore e il nome della board.
     *
     * @param title Titolo del ToDo
     * @param adminEmail Email dell'amministratore/utente proprietario
     * @param boardName Nome/tipo della board
     * @return ID del ToDo se trovato, null altrimenti
     */
    public Integer getTodoIdByTitleUserAndBoard(String title, String adminEmail, String boardName) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT t.id_todo FROM todo t JOIN bacheca b ON t.id_bacheca = b.id_bacheca " +
                            "WHERE t.titolo = ? AND t.email_utente = ? AND b.tipo = ?");
            ps.setString(1, title);
            ps.setString(2, adminEmail);
            ps.setString(3, boardName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_todo");
            }
        } catch (SQLException e) {
            logger.warning("Errore in getTodoIdByTitleUserAndBoard: " + e.getMessage());
        }
        return null;
    }

    /**
     * Imposta il campo 'condiviso' di un ToDo a true dato il suo ID.
     *
     * @param todoId ID del ToDo
     * @return true se l'aggiornamento ha successo, false altrimenti
     */
    public boolean setCondivisoTrueById(int todoId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE todo SET condiviso = true WHERE id_todo = ?");
            ps.setInt(1, todoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("Errore in setCondivisoTrueById: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera il colore associato a un ToDo specifico.
     *
     * @param boardType Tipo della board
     * @param email Email dell'utente proprietario
     * @param toDoTitle Titolo del ToDo
     * @param shared Indica se il ToDo è condiviso o meno
     * @return Colore del ToDo, oppure null se non trovato o non valido
     */
    public Color getColorOfToDo(String boardType, String email, String toDoTitle, boolean shared) {
        try {
            PreparedStatement ps;
            if (shared) {
                ps = conn.prepareStatement(
                        "SELECT colore FROM todo WHERE titolo = ? AND condiviso = true");
                ps.setString(1, toDoTitle);
            } else {
                Integer idBacheca = getBoardId(conn, email, boardType);
                if (idBacheca == null) return null;
                ps = conn.prepareStatement(
                        "SELECT colore FROM todo WHERE titolo = ? AND email_utente = ? AND id_bacheca = ?");
                ps.setString(1, toDoTitle);
                ps.setString(2, email);
                ps.setInt(3, idBacheca);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String colorStr = rs.getString("colore");
                if (colorStr == null) return null;
                return Color.decode(colorStr);
            }
        } catch (SQLException e) {
            logger.warning("Errore in getColorOfToDo: " + e.getMessage());
        }
        return null;
    }

    /**
     * Imposta il campo 'condiviso' di un ToDo a false dato il suo ID.
     *
     * @param todoId ID del ToDo
     */
    public void setCondivisoFalseById(int todoId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE todo SET condiviso = false WHERE id_todo = ?");
            ps.setInt(1, todoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Errore in setCondivisoFalseById: " + e.getMessage());
        }
    }

    /**
     * Sposta un ToDo da una board di origine ad una board di destinazione appartenenti allo stesso utente.
     * Non è possibile spostare ToDo condivisi.
     *
     * @param email Email dell'utente
     * @param nomeToDo Titolo del ToDo da spostare
     * @param nomeBachecaInCuiSpostare Nome della board di destinazione
     * @param nomeBachecaDiOrigine Nome della board di origine
     * @return Codice di ritorno:
     *         0 = successo,
     *         1 = ToDo già presente nella board di destinazione,
     *         2 = ToDo condiviso, non può essere spostato,
     *         3 = utente o board non trovati o errore
     */
    public int spostaToDoInBacheca(String email, String nomeToDo, String nomeBachecaInCuiSpostare, String nomeBachecaDiOrigine) {
        try {
            Integer idBoardDest = getBoardId(conn, email, nomeBachecaInCuiSpostare);
            Integer idBoardOrig = getBoardId(conn, email, nomeBachecaDiOrigine);
            if (idBoardDest == null || idBoardOrig == null) return 3;

            // Controllo se ToDo è condiviso
            PreparedStatement psCheck = conn.prepareStatement(
                    "SELECT condiviso FROM todo WHERE titolo = ? AND id_bacheca = ?");
            psCheck.setString(1, nomeToDo);
            psCheck.setInt(2, idBoardOrig);
            ResultSet rsCheck = psCheck.executeQuery();
            if (!rsCheck.next()) return 3;
            if (rsCheck.getBoolean("condiviso")) return 2;

            // Controllo se ToDo già presente nella board di destinazione
            PreparedStatement psCheckDest = conn.prepareStatement(
                    "SELECT COUNT(*) FROM todo WHERE titolo = ? AND id_bacheca = ?");
            psCheckDest.setString(1, nomeToDo);
            psCheckDest.setInt(2, idBoardDest);
            ResultSet rsCheckDest = psCheckDest.executeQuery();
            rsCheckDest.next();
            if (rsCheckDest.getInt(1) > 0) return 1;

            // Eseguo lo spostamento
            PreparedStatement psUpdate = conn.prepareStatement(
                    "UPDATE todo SET id_bacheca = ? WHERE titolo = ? AND id_bacheca = ?");
            psUpdate.setInt(1, idBoardDest);
            psUpdate.setString(2, nomeToDo);
            psUpdate.setInt(3, idBoardOrig);
            int updated = psUpdate.executeUpdate();
            return updated > 0 ? 0 : 3;

        } catch (SQLException e) {
            logger.warning("Errore in spostaToDoInBacheca: " + e.getMessage());
            return 3;
        }
    }

    /**
     * Metodo di supporto per recuperare l'ID di una board dato email e nome (tipo) della board.
     *
     * @param conn Connessione al database
     * @param email Email dell'utente proprietario
     * @param nomeBacheca Nome/tipo della board
     * @return ID della board, oppure null se non trovata
     * @throws SQLException se si verifica un errore SQL
     */
    private Integer getBoardId(Connection conn, String email, String nomeBacheca) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT id_bacheca FROM bacheca WHERE email_utente = ? AND tipo = ?");
        ps.setString(1, email);
        ps.setString(2, nomeBacheca);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id_bacheca");
        }
        return null;
    }

    /**
     * Controlla se tutte le attività associate al ToDo sono completate e aggiorna lo stato del ToDo.
     * Se non ci sono attività, lo stato sarà impostato a incompleto.
     *
     * @param toDoId ID del ToDo da verificare
     * @throws SQLException se si verifica un errore SQL
     */
    public void checkIfComplete(int toDoId) throws SQLException {
        PreparedStatement psCheck = conn.prepareStatement(
                "SELECT COUNT(*) AS incompleti FROM attivita WHERE id_todo = ? AND completato = false");
        psCheck.setInt(1, toDoId);
        ResultSet rs = psCheck.executeQuery();
        boolean complete = false;
        if (rs.next()) {
            complete = rs.getInt("incompleti") == 0;
        }

        PreparedStatement psUpdate = conn.prepareStatement(
                "UPDATE todo SET completato = ? WHERE id_todo = ?");
        psUpdate.setBoolean(1, complete);
        psUpdate.setInt(2, toDoId);
        psUpdate.executeUpdate();
    }

    /**
     * Recupera lo stato di completamento di un ToDo dato il suo ID.
     *
     * @param toDoId ID del ToDo
     * @return true se completato, false altrimenti
     */
    public boolean getCompletionStatus(int toDoId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT completato FROM todo WHERE id_todo = ?");
            ps.setInt(1, toDoId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("completato");
            }
        } catch (SQLException e) {
            logger.warning("Errore in getCompletionStatus: " + e.getMessage());
        }
        return false;
    }
}
