package model;

import java.util.ArrayList;

/**
 * Rappresenta un utente del sistema, con credenziali, bacheche personali, attività completate e condivisioni.
 */
public class User {
    private String nickname;
    private String email;
    private String password;

    /**
     * Un array di bacheche associate all'utente.
     * Le posizioni rappresentano:
     * 0 = UNIVERSITY, 1 = WORK, 2 = FREETIME.
     */
    private Board[] boards = new Board[3];

    /**
     * Cronologia delle attività completate dall'utente.
     */
    private CompletedActivityHistory activityHistory;

    /**
     * Elenco delle condivisioni (ToDo condivisi con l'utente o da lui amministrati).
     */
    private ArrayList<Sharing> sharing = new ArrayList<>();

    /**
     * Costruttore della classe User.
     *
     * @param nickname il nickname dell'utente
     * @param email l'email dell'utente
     * @param password la password dell'utente
     */
    public User(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.activityHistory = new CompletedActivityHistory();
    }

    /**
     * Restituisce il nickname dell'utente.
     *
     * @return il nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Imposta il nickname dell'utente.
     *
     * @param nickname il nuovo nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Restituisce l'email dell'utente.
     *
     * @return l'email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Imposta l'email dell'utente.
     *
     * @param email la nuova email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Restituisce la password dell'utente.
     *
     * @return la password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Imposta la password dell'utente.
     *
     * @param password la nuova password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Restituisce l'array di bacheche dell'utente.
     *
     * @return un array di oggetti Board
     */
    public Board[] getBoards() {
        return boards;
    }

    /**
     * Imposta l'array di bacheche dell'utente.
     *
     * @param boards l'array da assegnare
     */
    public void setBoards(Board[] boards) {
        this.boards = boards;
    }

    /**
     * Restituisce la cronologia delle attività completate dell'utente.
     *
     * @return l'oggetto CompletedActivityHistory associato all'utente
     */
    public CompletedActivityHistory getActivityHistory() {
        return activityHistory;
    }

    /**
     * Imposta una nuova cronologia delle attività completate.
     *
     * @param activityHistory l'oggetto da assegnare
     */
    public void setActivityHistory(CompletedActivityHistory activityHistory) {
        this.activityHistory = activityHistory;
    }

    /**
     * Restituisce la lista delle condivisioni associate all'utente.
     *
     * @return una lista di oggetti Sharing
     */
    public ArrayList<Sharing> getSharing() {
        return sharing;
    }

    /**
     * Imposta la lista delle condivisioni associate all'utente.
     *
     * @param sharing la lista da assegnare
     */
    public void setSharing(ArrayList<Sharing> sharing) {
        this.sharing = sharing;
    }
}
