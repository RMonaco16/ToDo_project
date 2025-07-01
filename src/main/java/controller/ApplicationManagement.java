package controller;

import dao.*;
import db.DatabaseConnection;
import model.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.text.ParseException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Controller principale dell'applicazione.
 * <p>
 * Questa classe gestisce le operazioni principali relative alla logica dell'applicazione
 *  e l'interazione con il database attraverso i DAO.
 *  </p>
 */
public class ApplicationManagement {

    private static final Logger logger = Logger.getLogger(ApplicationManagement.class.getName());

    private User currentUser;

    /**
     * Costruttore della classe ApplicationManagement.
     */
    public ApplicationManagement() {
        //controller
    }

    /**
     * Aggiunge un nuovo utente al sistema.
     * <p>
     * Effettua varie validazioni sui campi dell'utente, come nickname, email e password.
     * Controlla che l'email sia valida e non sia già presente nel database.
     * Se tutte le validazioni passano, crea l'utente tramite il DAO.
     * </p>
     *
     * @param u l'oggetto User da aggiungere
     * @return {@code true} se l'utente è stato aggiunto correttamente, {@code false} in caso contrario
     */
    public boolean addUser(User u) {
        if (u.getNickname().isBlank() || u.getEmail().isBlank() || u.getPassword().isBlank()) {
            logger.info("Utente non creato: campi vuoti.");
            return false;
        }

        // --- Validazione email ---
        if (!isEmailValid(u.getEmail())) {
            logger.info("Formato email non valido.");
            return false;
        }

        // --- Validazione password ---
        if (!isPasswordValid(u.getPassword())) {
             logger.info("Password troppo debole: almeno 8 caratteri, lettera e numero.");
            return false;
        }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);

            if (userDAO.emailExists(u.getEmail())) {
                 logger.info("Email già presente!");
                return false;
            }

            boolean added = userDAO.creaUser(u);

            if (added) {
                 logger.info("Utente aggiunto correttamente!");
            } else {
                 logger.info("Errore nell'aggiunta dell'utente.");
            }

            return added;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica se l'indirizzo email fornito ha un formato valido.
     *
     * @param email l'indirizzo email da validare
     * @return {@code true} se l'email è valida secondo il formato specificato, {@code false} altrimenti
     */
    private boolean isEmailValid(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    /**
     * Verifica se una password è considerata valida.
     * <p>
     * Una password è valida se ha almeno 8 caratteri, contiene almeno una lettera
     * e almeno un numero.
     * </p>
     *
     * @param password la password da validare
     * @return {@code true} se la password è valida, {@code false} altrimenti
     */
    public boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasLetter && hasDigit;
    }



    /**
     * Esegue il login di un utente con le credenziali fornite.
     * <p>
     * Il metodo verifica le credenziali contro i dati memorizzati nel database.
     * Se l'autenticazione ha successo, l'utente viene impostato come corrente.
     * </p>
     *
     * @param email    l'indirizzo email dell'utente
     * @param password la password dell'utente
     * @return {@code true} se il login è avvenuto con successo, {@code false} altrimenti
     */
    public boolean login(String email, String password) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);

            User user = userDAO.getUserByEmailAndPassword(email, password);
            if (user != null) {
                this.currentUser = user;
                 logger.info("Login effettuato con Successo!!");
                return true;
            } else {
                 logger.info("Email o password errati.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Esegue il logout dell'utente corrente.
     * <p>
     * Rimuove l'utente autenticato dalla sessione impostando {@code currentUser} a {@code null}.
     * </p>
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Aggiunge una nuova board per l'utente specificato.
     *
     * @param email l'email dell'utente proprietario della board
     * @param b l'oggetto Board da creare
     * @return true se la board è stata creata con successo, false altrimenti
     */
    public boolean addBoard(String email, Board b) {
        try {
            // Ottieni connessione dal singleton
            Connection conn = DatabaseConnection.getInstance().getConnection();

            // Crea DAO della board e inserisci board nel database
            BoardDAO boardDAO = new BoardDAO(conn);
            boolean created = boardDAO.creaBoard(b, email); // passa l'email al DAO

            if (created) {
                logger.info("Board creata correttamente.");
            } else {
                logger.info("Board NON creata.");
            }
            return created;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina una board associata a un utente in base all'email e al tipo di board.
     * <p>
     * Utilizza il {@link BoardDAO} per eseguire l'eliminazione dal database.
     * </p>
     *
     * @param email l'email dell'utente proprietario della board
     * @param type  il tipo di board da eliminare
     */
    public void deleteBoard(String email, String type) {
        try {
            // Ottieni connessione dal singleton
            Connection conn = DatabaseConnection.getInstance().getConnection();

            // Crea DAO della board e inserisci board nel database
            BoardDAO boardDAO = new BoardDAO(conn);
            boardDAO.eliminaBoard(email, type); // passa l'email al DAO

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupera tutte le board associate a un utente specifico tramite email.
     *
     * @param email l'indirizzo email dell'utente di cui si vogliono ottenere le board
     * @return una lista di oggetti {@link Board} associati all'utente
     */
    public ArrayList<Board> printBoard(String email) {
        BoardDAO boardDAO = new BoardDAO(DatabaseConnection.getInstance().getConnection());
        return boardDAO.getBoardsByEmail(email);
    }

    /**
     * Aggiunge un oggetto {@link ToDo} a una board specifica identificata dall'email dell'utente e dal tipo.
     * <p>
     * Utilizza il {@link ToDoDAO} per inserire il ToDo nel database. Registra nel logger
     * il successo o il fallimento dell'operazione.
     * </p>
     *
     * @param email    l'email dell'utente proprietario della board
     * @param tipoEnum il tipo della board in cui aggiungere il ToDo
     * @param toDo     l'oggetto ToDo da aggiungere
     * @return {@code true} se l'inserimento è andato a buon fine, {@code false} in caso di errore
     */
    public boolean addToDoInBoard(String email, String tipoEnum, ToDo toDo) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ToDoDAO toDoDAO = new ToDoDAO(conn);

            boolean success = toDoDAO.addToDoInBoard(email, tipoEnum, toDo);

            if (success) {
                 logger.info("ToDo aggiunto correttamente.");
            } else {
                 logger.info("Errore nell'aggiunta del ToDo.");
            }

            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un ToDo da una board specifica di un utente, identificato tramite titolo.
     * <p>
     * Utilizza il {@link ToDoDAO} per rimuovere l'elemento dal database.
     * </p>
     *
     * @param email l'indirizzo email dell'utente proprietario della board
     * @param board il nome della board da cui eliminare il ToDo
     * @param title il titolo del ToDo da eliminare
     * @return {@code true} se l'eliminazione è avvenuta con successo, {@code false} altrimenti
     */
    public boolean deleteToDo(String email, String board, String title) {
        try {
            ToDoDAO dao = new ToDoDAO(DatabaseConnection.getInstance().getConnection());
            return dao.deleteToDo(email, board, title);
        } catch (SQLException e) {
            logger.info("Errore durante l'eliminazione del ToDo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Aggiunge un'attività ({@link Activity}) a un To-Do specifico in una board data.
     * <p>
     * Il metodo verifica che il tipo di board sia uno tra "UNIVERSITY", "WORK" o "FREETIME".
     * Dopo l'aggiunta, controlla e aggiorna lo stato del To-Do associato.
     * </p>
     *
     * @param email    l'indirizzo email dell'utente proprietario del To-Do
     * @param titleToDo il titolo del To-Do a cui aggiungere l'attività
     * @param board    il tipo di board ("UNIVERSITY", "WORK" o "FREETIME")
     * @param activity l'oggetto {@link Activity} da aggiungere
     */
    public void addActivity(String email, String titleToDo, String board, Activity activity) {
        if (!board.equalsIgnoreCase("UNIVERSITY") && !board.equalsIgnoreCase("WORK") && !board.equalsIgnoreCase("FREETIME")) {
             logger.info("Tipo di bacheca non valido.");
            return;
        }

        Connection conn = DatabaseConnection.getInstance().getConnection();
        CheckListDAO checkListDAO = new CheckListDAO(conn);
        ToDoDAO toDoDAO = new ToDoDAO(conn);

        try {
            checkListDAO.addActivity(email, titleToDo, board, activity);

            // Controllo e aggiornamento dello stato del To-Do dopo aggiunta attività
            int toDoId = checkListDAO.getToDoId(email, board, titleToDo);
            toDoDAO.checkIfComplete(toDoId);

        } catch (SQLException e) {
           logger.info("Errore SQL durante addActivity: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
             logger.info("Errore generico durante addActivity");
        }
    }

    /**
     * Rimuove un'attività specifica da un To-Do all'interno di una board.
     * <p>
     * Dopo la rimozione, aggiorna lo stato del To-Do per verificare se è completo.
     * </p>
     *
     * @param email        l'indirizzo email dell'utente proprietario del To-Do
     * @param titleToDo    il titolo del To-Do da cui rimuovere l'attività
     * @param board        il nome della board in cui si trova il To-Do
     * @param nameActivity il nome dell'attività da rimuovere
     */
    public void removeActivity(String email, String titleToDo, String board, String nameActivity) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            CheckListDAO dao = new CheckListDAO(conn);
            ToDoDAO toDoDAO = new ToDoDAO(conn);

            dao.removeActivity(email, titleToDo, board, nameActivity);

            // Aggiorna lo stato del To-Do dopo la rimozione
            int toDoId = dao.getToDoId(email, board, titleToDo);
            toDoDAO.checkIfComplete(toDoId);

        } catch (SQLException e) {
            logger.info("Errore nella connessione al database: " + e.getMessage());
        }
    }

    /**
     * Modifica i dettagli di un To-Do esistente in una board specifica.
     *
     * @param email        l'indirizzo email dell'utente proprietario del To-Do
     * @param board        il nome della board contenente il To-Do
     * @param toDoTitleOld il titolo attuale del To-Do da modificare
     * @param newTitle     il nuovo titolo da assegnare al To-Do
     * @param description  la nuova descrizione del To-Do
     * @param expiration   la nuova data di scadenza del To-Do
     * @param image        il nuovo percorso o riferimento all'immagine associata al To-Do
     * @param color        il nuovo colore associato al To-Do
     * @return {@code true} se l'aggiornamento è andato a buon fine, {@code false} altrimenti
     */
    public boolean editToDo(String email, String board, String toDoTitleOld, String newTitle,
                            String description, LocalDate expiration, String image, Color color) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        ToDoDAO dao = new ToDoDAO(conn);

        return dao.updateToDo(email, board, toDoTitleOld, newTitle, description, expiration, image, color);
    }

    /**
     * Segna un'attività come completata e aggiorna la cronologia e lo stato del To-Do corrispondente.
     * <p>
     * Se viene fornita una data di completamento valida, questa viene convertita e aggiunta alla cronologia
     * tramite il metodo {@code addHistoryAct}. Successivamente viene verificato e aggiornato lo stato
     * del To-Do associato.
     * </p>
     *
     * @param email           l'indirizzo email dell'utente proprietario dell'attività
     * @param board           il nome della board che contiene il To-Do
     * @param todo            il titolo del To-Do associato all'attività
     * @param activity        il nome dell'attività da segnare come completata
     * @param dataCompletamento la data di completamento dell'attività in formato "dd-MM-yyyy"; può essere null o vuota
     */
    public void checkActivity(String email, String board, String todo, String activity, String dataCompletamento) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        CheckListDAO dao = new CheckListDAO(conn);
        ToDoDAO toDoDAO = new ToDoDAO(conn);

        try {
            dao.checkActivity(email, board, todo, activity, dataCompletamento);

            if (dataCompletamento != null && !dataCompletamento.isEmpty()) {
                // Converti stringa in java.sql.Date
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                java.util.Date utilDate = sdf.parse(dataCompletamento);
                Date sqlDate = new Date(utilDate.getTime()); // java.sql.Date
                addHistoryAct(email, activity, sqlDate);
            } else {
                 logger.info("Data di completamento non fornita, cronologia non aggiornata.");
            }

            // Controllo e aggiornamento dello stato del To-Do
            int toDoId = dao.getToDoId(email, board, todo);
            toDoDAO.checkIfComplete(toDoId);

        } catch (ParseException e) {
             logger.info("Errore nel parsing della data: " + e.getMessage());
        } catch (SQLException e) {
             logger.info("Errore SQL: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
             logger.info("Errore generico nel controller durante checkActivity");
        }
    }

    /**
     * Segna un'attività come non completata rimuovendo il suo stato di completamento.
     * <p>
     * Dopo la modifica, aggiorna lo stato del To-Do associato per riflettere la nuova condizione.
     * </p>
     *
     * @param email    l'indirizzo email dell'utente proprietario dell'attività
     * @param board    il nome della board contenente il To-Do
     * @param todo     il titolo del To-Do associato all'attività
     * @param activity il nome dell'attività da segnare come non completata
     * @return {@code true} se l'operazione è stata completata con successo, {@code false} in caso di errore
     */
    public boolean deCheckActivity(String email, String board, String todo, String activity) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        CheckListDAO dao = new CheckListDAO(conn);
        ToDoDAO toDoDAO = new ToDoDAO(conn);

        try {
            boolean result = dao.uncheckActivity(email, board, todo, activity);

            // Aggiorna lo stato del To-Do dopo la modifica
            int toDoId = dao.getToDoId(email, board, todo);
            toDoDAO.checkIfComplete(toDoId);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
             logger.info("Errore nel controller durante deCheckActivity");
            return false;
        }
    }

    /**
     * Restituisce la lista delle attività completate da un utente specifico.
     *
     * @param email l'indirizzo email dell'utente di cui si vogliono ottenere le attività completate
     * @return una {@link ArrayList} di oggetti {@link Activity} rappresentanti le attività completate;
     *         la lista è vuota se non ci sono attività completate o in caso di errore nel recupero
     */
    public ArrayList<Activity> returnCompletedActivity(String email) {
        ArrayList<Activity> completedActivities = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            CompletedActivityHistoryDAO dao = new CompletedActivityHistoryDAO(conn);
            completedActivities = dao.getCompletedActivitiesByUser(email);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.info("Errore nel recupero delle attività completate da DB");
        }

        return completedActivities;
    }

    /**
     * Aggiunge un'attività completata alla cronologia delle attività di un utente.
     *
     * @param email          l'indirizzo email dell'utente
     * @param activityName   il nome dell'attività completata
     * @param completionDate la data di completamento dell'attività
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public void addHistoryAct(String email, String activityName, Date completionDate) throws SQLException {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            CompletedActivityHistoryDAO dao = new CompletedActivityHistoryDAO(conn);
            dao.addActivityToHistory(email, activityName, completionDate);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Errore nel controller durante l'aggiunta alla cronologia");
        }
    }

    /**
     * Rimuove un'attività completata dalla cronologia di un utente.
     *
     * @param email l'indirizzo email dell'utente
     * @param nmAct il nome dell'attività da rimuovere dalla cronologia
     */
    public void rmvHistoryAct(String email, String nmAct) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            CompletedActivityHistoryDAO dao = new CompletedActivityHistoryDAO(conn);
            boolean removed = dao.removeActivityFromHistory(email, nmAct);
            if (!removed) {
                logger.info("Attività non trovata nella cronologia per l'utente: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.info("Errore durante la rimozione dalla cronologia");
        }
    }

    /**
     * Elimina tutte le attività completate dalla cronologia di un utente specifico.
     *
     * @param email l'indirizzo email dell'utente di cui si vuole cancellare la cronologia delle attività completate
     */
    public void dltHistory(String email) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            CompletedActivityHistoryDAO dao = new CompletedActivityHistoryDAO(conn);
            boolean deleted = dao.deleteAllActivitiesFromHistory(email);

            if (!deleted) {
                logger.info("Nessuna attività trovata o utente non esistente: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.info("Errore durante l'eliminazione della cronologia");
        }
    }

    /**
     * Verifica se un To-Do esiste all'interno di una specifica board di un utente.
     *
     * @param email     l'indirizzo email dell'utente proprietario della board
     * @param boardName il nome della board in cui cercare il To-Do
     * @param toDoName  il titolo del To-Do da cercare
     * @return {@code true} se il To-Do esiste nella board specificata, {@code false} altrimenti
     */
    public boolean ifExistsTodoInUserBoard(String email,String boardName, String toDoName){
        Connection conn = DatabaseConnection.getInstance().getConnection();
        BoardDAO boardDAO = new BoardDAO(conn);
        if(boardDAO.findToDoByTitleInBoard(email, boardName, toDoName)==null)
            return false;
        return true;
    }

    /**
     * Restituisce la lista di tutti i To-Do associati a una specifica board di un utente.
     *
     * @param email l'indirizzo email dell'utente proprietario della board
     * @param board il nome della board da cui recuperare i To-Do
     * @return una {@link ArrayList} di oggetti {@link ToDo} contenenti tutti i To-Do della board specificata
     */
    public ArrayList<ToDo> printTodo(String email, String board) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        BoardDAO boardDAO = new BoardDAO(conn);
        return boardDAO.getAllLocalToDos(email, board);
    }

    /**
     * Restituisce la lista delle attività associate a un determinato To-Do all'interno di una board di un utente.
     *
     * @param email     l'indirizzo email dell'utente proprietario della board e del To-Do
     * @param board     il nome della board contenente il To-Do
     * @param todoTitle il titolo del To-Do di cui si vogliono ottenere le attività
     * @return una {@link ArrayList} di oggetti {@link Activity} associati al To-Do specificato
     */
    public ArrayList<Activity> printActs(String email, String board, String todoTitle) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        CheckListDAO dao = new CheckListDAO(conn);
        return dao.getActivities(email, board, todoTitle);
    }

    /**
     * Restituisce la lista dei To-Do visibili per un utente su una board specifica,
     * applicando un filtro opzionale che può essere una data o un titolo.
     *
     * <p>
     * Se il filtro è vuoto, restituisce tutti i To-Do locali e condivisi non nulli.
     * Se il filtro è "todayFilter", restituisce i To-Do locali con scadenza odierna.
     * Se il filtro è una data nel formato "dd-MM-yyyy", restituisce i To-Do locali e condivisi
     * con scadenza fino a quella data.
     * Se il filtro non è nessuno dei precedenti, cerca un To-Do con quel titolo e lo aggiunge se presente.
     * </p>
     *
     * @param user      l'utente proprietario della board
     * @param boardName il nome della board da cui recuperare i To-Do
     * @param filter    filtro opzionale che può essere una data ("dd-MM-yyyy"), "todayFilter" oppure un titolo
     * @return una {@link ArrayList} di {@link ToDo} visibili in base al filtro specificato
     */
    public ArrayList<ToDo> getVisibleToDos(User user, String boardName, String filter) {
        ArrayList<ToDo> visibleToDos = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);

            if (!userDAO.checkBoard(user.getEmail(), boardName)) {
                throw new Exception();
            }

            BoardDAO boardDAO = new BoardDAO(conn);

            if (filter.isBlank()) {
                // aggiunge solo i To-Do non null
                visibleToDos.addAll(
                        boardDAO.getAllLocalToDos(user.getEmail(), boardName).stream()
                                .filter(Objects::nonNull)
                                .toList()
                );
                visibleToDos.addAll(
                        boardDAO.getAllSharedToDos(user.getEmail(), boardName).stream()
                                .filter(Objects::nonNull)
                                .toList()
                );

            } else if (filter.equals("todayFilter")) {
                visibleToDos.addAll(
                        boardDAO.getLocalTodosByExpirationDate(user.getEmail(), boardName).stream()
                                .filter(Objects::nonNull)
                                .toList()
                );

            } else {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    LocalDate date = LocalDate.parse(filter, formatter);

                    visibleToDos.addAll(
                            boardDAO.getLocalTodosExpiringBeforeOrOn(user.getEmail(), boardName, date).stream()
                                    .filter(Objects::nonNull)
                                    .toList()
                    );
                    visibleToDos.addAll(
                            boardDAO.getSharedTodosExpiringBeforeOrOn(user.getEmail(), boardName, date).stream()
                                    .filter(Objects::nonNull)
                                    .toList()
                    );

                } catch (DateTimeParseException e) {
                    ToDo single = boardDAO.findToDoByTitleInBoard(user.getEmail(), boardName, filter);
                    if (single != null) {
                        visibleToDos.add(single);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return visibleToDos;
    }

    /**
     * Condivide un To-Do da un amministratore a un utente destinatario su una specifica board.
     *
     * <p>
     * Il metodo verifica che la condivisione sia permessa, che gli indirizzi email e il To-Do siano validi,
     * crea o recupera l'oggetto Sharing associato, e aggiunge l'utente destinatario alla condivisione se necessario.
     * </p>
     *
     * @param mailAmministratore      email dell'amministratore che condivide il To-Do
     * @param mailUtenteDestinatario email dell'utente destinatario della condivisione
     * @param boardName              nome della board contenente il To-Do da condividere
     * @param toDoName               nome del To-Do da condividere
     * @return {@code true} se la condivisione è avvenuta con successo, {@code false} in caso contrario
     */
    public boolean shareToDo(String mailAmministratore, String mailUtenteDestinatario, String boardName, String toDoName) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;

        if (!canShareToDo(mailAmministratore, mailUtenteDestinatario, boardName, toDoName)) return false;

        if (!validateEmailsAndToDo(conn, mailAmministratore, mailUtenteDestinatario, boardName, toDoName)) return false;

        BoardDAO boardDAO = new BoardDAO(conn);
        ToDo todo = boardDAO.checkToDoExists(mailAmministratore, boardName, toDoName);

        ensureSharingExists(conn, mailAmministratore, todo, toDoName, boardName);

        addUserToSharingIfNeeded(conn, mailUtenteDestinatario, mailAmministratore, toDoName, boardName);

        return true;
    }

    /**
     * Verifica se è possibile condividere un To-Do da un amministratore a un utente destinatario.
     *
     * <p>
     * Controlla che l'amministratore e il destinatario siano utenti diversi,
     * che l'amministratore sia effettivamente tale per il To-Do indicato,
     * e che il destinatario non abbia già un To-Do con lo stesso nome nella board specificata.
     * </p>
     *
     * @param mailAmministratore      email dell'amministratore che vuole condividere il To-Do
     * @param mailUtenteDestinatario email dell'utente destinatario della condivisione
     * @param boardName              nome della board contenente il To-Do
     * @param toDoName               nome del To-Do da condividere
     * @return {@code true} se la condivisione è consentita, {@code false} altrimenti
     */
    private boolean canShareToDo(String mailAmministratore, String mailUtenteDestinatario, String boardName, String toDoName) {
        if (mailAmministratore.equalsIgnoreCase(mailUtenteDestinatario)) {
            logger.info("Errore: non puoi condividere un ToDo con te stesso.");
            return false;
        }
        if (!isUserAdminOfToDo(mailAmministratore, boardName, toDoName)) {
            logger.info("Errore: l'utente non è amministratore del ToDo e non può condividerlo.");
            return false;
        }
        if (ifExistsTodoInUserBoard(mailUtenteDestinatario, boardName, toDoName)) {
            logger.info("Errore: il destinatario ha già un todo locale o condiviso con lo stesso nome");
            return false;
        }
        return true;
    }

    /**
     * Verifica la validità delle email degli utenti e l'esistenza del To-Do specificato.
     *
     * <p>
     * Controlla se le email dell'amministratore e del destinatario esistono nel database
     * e se il To-Do indicato esiste nella board dell'amministratore.
     * </p>
     *
     * @param conn               la connessione al database
     * @param mailAmministratore  email dell'amministratore che condivide il To-Do
     * @param mailUtenteDestinatario email dell'utente destinatario della condivisione
     * @param boardName          nome della board contenente il To-Do
     * @param toDoName           nome del To-Do da verificare
     * @return {@code true} se entrambe le email esistono e il To-Do è presente, {@code false} altrimenti
     */
    private boolean validateEmailsAndToDo(Connection conn, String mailAmministratore, String mailUtenteDestinatario,
                                          String boardName, String toDoName) {
        UserDAO userDao = new UserDAO(conn);
        BoardDAO boardDAO = new BoardDAO(conn);
        if (!userDao.emailExists(mailAmministratore) || !userDao.emailExists(mailUtenteDestinatario)) {
            logger.info("Email mittente o destinatario non trovata");
            return false;
        }
        ToDo todo = boardDAO.checkToDoExists(mailAmministratore, boardName, toDoName);
        if (todo == null) {
             logger.info("ToDo del mittente non trovata");
            return false;
        }
        return true;
    }

    /**
     * Verifica se esiste una condivisione (Sharing) per un determinato ToDo e amministratore.
     * <p>
     * Se la condivisione non esiste, la crea, aggiorna lo stato del ToDo a "condiviso" e la restituisce.
     * Se invece esiste già, restituisce quella esistente.
     * </p>
     *
     * @param conn             la connessione al database
     * @param mailAmministratore l'email dell'amministratore che condivide il ToDo
     * @param todo             l'oggetto ToDo da condividere
     * @param toDoName         il nome del ToDo
     * @param boardName        il nome della board a cui appartiene il ToDo
     * @return la condivisione esistente o appena creata (Sharing)
     */
    private Sharing ensureSharingExists(Connection conn, String mailAmministratore, ToDo todo, String toDoName, String boardName) {
        SharingDAO sharingDAO = new SharingDAO(conn);
        Sharing sharing = sharingDAO.checkSharingExists(findUserByEmail(mailAmministratore), todo);
        ToDoDAO todoDAO = new ToDoDAO(conn);
        Integer todoId = todoDAO.getTodoIdByTitleUserAndBoard(toDoName, mailAmministratore, boardName);

        if (sharing == null) {
            sharing = new Sharing(findUserByEmail(mailAmministratore), todo);
            sharingDAO.creaSharing(sharing);
            logger.info("Creazione nuova condivisione");
            todoDAO.setCondivisoTrueById(todoId);
        } else {
            logger.info("Condivisione già esistente");
        }
        return sharing;
    }

    /**
     * Aggiunge un utente come membro alla condivisione di un ToDo, se non è già partecipante.
     * <p>
     * Verifica se l'utente destinatario è già membro della condivisione; in caso contrario,
     * controlla se l'utente ha la board specificata, e la crea se assente.
     * Infine, aggiunge l'utente come membro della condivisione.
     * </p>
     *
     * @param conn                 la connessione al database
     * @param mailUtenteDestinatario l'email dell'utente destinatario da aggiungere
     * @param mailAmministratore   l'email dell'amministratore che condivide il ToDo
     * @param toDoName             il nome del ToDo condiviso
     * @param boardName            il nome della board a cui appartiene il ToDo
     */
    private void addUserToSharingIfNeeded(Connection conn, String mailUtenteDestinatario,
                                          String mailAmministratore, String toDoName, String boardName) {
        SharingDAO sharingDAO = new SharingDAO(conn);
        UserDAO userDao = new UserDAO(conn);
        BoardDAO boardDAO = new BoardDAO(conn);

        if (!sharingDAO.checkUserAlreadySharing(mailUtenteDestinatario, toDoName, mailAmministratore)) {
            if (!userDao.checkBoard(mailUtenteDestinatario, boardName)) {
                Board board;
                switch (boardName) {
                    case "UNIVERSITY":
                        board = new Board(TypeBoard.UNIVERSITY, "");
                        break;
                    case "WORK":
                        board = new Board(TypeBoard.WORK, "");
                        break;
                    case "FREETIME":
                        board = new Board(TypeBoard.FREETIME, "");
                        break;
                    default:
                        logger.info("Board name non riconosciuto");
                        return;
                }
                boardDAO.creaBoard(board, mailUtenteDestinatario);
            }
            sharingDAO.aggiungiMembroSharing(mailUtenteDestinatario, mailAmministratore, toDoName);
             logger.info("Utente aggiunto come partecipante alla condivisione");
            // imposta flag condiviso nel ToDo
        } else {
             logger.info("Utente già partecipante alla condivisione");
        }
    }


    /**
     * Verifica se un utente è amministratore di un determinato ToDo in una board specifica.
     *
     * @param emailUtente l'email dell'utente da verificare
     * @param boardName   il nome della board in cui si trova il ToDo
     * @param toDoTitle   il titolo del ToDo da verificare
     * @return true se l'utente è amministratore del ToDo, false altrimenti o in caso di errore
     */
    public boolean isUserAdminOfToDo(String emailUtente, String boardName, String toDoTitle) {
        try {
            ToDoDAO toDoDAO = new ToDoDAO();
            return toDoDAO.isUserAdminOfToDo(emailUtente, boardName, toDoTitle);
        } catch (Exception e) {
             logger.info("Errore verfica se Utente è anche admin");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cerca e restituisce un utente tramite la sua email.
     *
     * @param email l'indirizzo email dell'utente da cercare
     * @return l'oggetto User corrispondente all'email, oppure null se non trovato o in caso di errore
     */
    public User findUserByEmail(String email) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);

            User u = userDAO.leggiUserPerEmail(email);
            return u;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // solo se c’è errore
    }

    /**
     * Restituisce la lista dei titoli dei ToDo per cui l'utente è amministratore
     * e che non sono ancora condivisi nella specifica bacheca.
     *
     * @param emailUtente l'email dell'utente amministratore
     * @param tipoBacheca il tipo di bacheca (es. "WORK", "UNIVERSITY", "FREETIME")
     * @return una lista di titoli dei ToDo non condivisi per cui l'utente è amministratore
     */
    public ArrayList<String> getToDoAdminNonCondivisi(String emailUtente, String tipoBacheca) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        SharingDAO sharingDAO = new SharingDAO(conn);
        return sharingDAO.getToDoTitlesAdminNonCondivisi(emailUtente, tipoBacheca);
    }

    /**
     * Rimuove un utente dalla condivisione di un ToDo specifico.
     *
     * Il metodo verifica che il richiedente sia amministratore del ToDo,
     * che l'utente da rimuovere esista e non sia lo stesso amministratore,
     * quindi procede a rimuoverlo dalla condivisione. Se non rimangono più membri,
     * la condivisione viene eliminata e il ToDo viene marcato come non condiviso.
     *
     * @param mailAmministratore l'email dell'amministratore che richiede la rimozione
     * @param mailUtenteDaRimuovere l'email dell'utente da rimuovere dalla condivisione
     * @param boardName il nome della bacheca a cui appartiene il ToDo
     * @param toDoTitle il titolo del ToDo condiviso
     * @return true se l'utente è stato rimosso con successo, false in caso di errore o condizioni non soddisfatte
     */
    public boolean rimuoviUtenteDaSharing(String mailAmministratore, String mailUtenteDaRimuovere, String boardName, String toDoTitle) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) return false;

        // 1. Verifica se sei amministratore del To-Do
        if (!isUserAdminOfToDo(mailAmministratore, boardName, toDoTitle)) {
             logger.info("Errore: l'utente non è amministratore del ToDo.");
            return false;
        }

        if (mailAmministratore.equalsIgnoreCase(mailUtenteDaRimuovere)) {
             logger.info("Errore: non puoi rimuovere te stesso dalla condivisione.");
            return false;
        }

        UserDAO userDao = new UserDAO(conn);
        ToDoDAO toDoDAO = new ToDoDAO(conn);
        SharingDAO sharingDAO = new SharingDAO(conn);

        // 2. Verifica esistenza utenti
        if (!userDao.emailExists(mailUtenteDaRimuovere) || !userDao.emailExists(mailAmministratore)) {
             logger.info("Errore: uno degli utenti non esiste.");
            return false;
        }

        // 3. Recupera ID To-Do
        Integer todoId = toDoDAO.getTodoIdByTitleUserAndBoard(toDoTitle, mailAmministratore, boardName);
        if (todoId == null) {
             logger.info("Errore: ToDo non trovato.");
            return false;
        }

        // 4. Rimuovi utente da sharing
        boolean removed = sharingDAO.rimuoviMembroSharing(mailUtenteDaRimuovere, todoId);
        if (!removed) {
             logger.info("Errore: impossibile rimuovere l’utente (forse non è membro).");
            return false;
        }

        // 5. Verifica se ci sono ancora membri
        int membriRimasti = sharingDAO.countMembriSharing(todoId);
        if (membriRimasti == 0) {
            toDoDAO.setCondivisoFalseById(todoId);
            sharingDAO.eliminaSharingSeVuoto(todoId);
             logger.info("Condivisione terminata: nessun membro rimanente.");
        } else {
             logger.info("Utente rimosso con successo dalla condivisione.");
        }
        return true;
    }

    /**
     * Restituisce la lista degli utenti con cui un ToDo è stato condiviso.
     *
     * @param email l'email del proprietario del ToDo
     * @param toDo il titolo del ToDo di cui si vogliono ottenere gli utenti condivisi
     * @return una lista di utenti con cui il ToDo è condiviso
     */
    public ArrayList<User> getToDoUserShared(String email, String toDo) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        SharingDAO sharingDAO = new SharingDAO(conn);
        return sharingDAO.getSharingUserShared(email, toDo);
    }

    /**
     * Recupera il nickname dell'amministratore di condivisione di un ToDo.
     *
     * @param email l'email dell'utente proprietario o coinvolto nel ToDo
     * @param toDo il titolo del ToDo di cui si vuole conoscere l'amministratore
     * @return il nickname dell'amministratore della condivisione del ToDo, oppure null se non trovato
     */
    public String getToAdministratorNick(String email, String toDo) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        SharingDAO sharingDAO = new SharingDAO(conn);
        return sharingDAO.getSharingAdministratorNick(email, toDo);
    }

    /**
     * Recupera l'email dell'amministratore della condivisione di un ToDo specifico.
     *
     * @param email l'email dell'utente proprietario o coinvolto nel ToDo
     * @param toDo il titolo del ToDo di cui si vuole conoscere l'amministratore
     * @return l'email dell'amministratore della condivisione del ToDo, oppure null se non trovato
     */
    public String getToAdministratorMail(String email, String toDo) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        SharingDAO sharingDAO = new SharingDAO(conn);
        return sharingDAO.getSharingAdministratorEmail(email, toDo);
    }

    /**
     * Sposta un ToDo da una bacheca di origine a una bacheca di destinazione per un dato utente.
     *
     * @param email l'email dell'utente proprietario del ToDo
     * @param nomeToDo il titolo del ToDo da spostare
     * @param nomeBachecaInCuiSpostare il nome della bacheca di destinazione
     * @param nomeBachecaDiOrigine il nome della bacheca di origine
     * @return un codice intero che indica il risultato dell'operazione:
     *         <ul>
     *           <li>0 - ToDo spostato correttamente</li>
     *           <li>1 - ToDo già presente nella bacheca di destinazione</li>
     *           <li>2 - ToDo condiviso, impossibile spostarlo</li>
     *           <li>3 - Utente, bacheca o ToDo non trovati, oppure errore</li>
     *           <li>altro - Errore sconosciuto</li>
     *         </ul>
     */
    public int spostaToDoInBacheca(String email, String nomeToDo, String nomeBachecaInCuiSpostare, String nomeBachecaDiOrigine) {
        ToDoDAO toDoDAO = new ToDoDAO();

        int risultato = toDoDAO.spostaToDoInBacheca(email, nomeToDo, nomeBachecaInCuiSpostare, nomeBachecaDiOrigine);

        switch (risultato) {
            case 0:
                 logger.info("ToDo spostato correttamente");
                break;
            case 1:
                 logger.info("ToDo già presente nella bacheca di destinazione");
                break;
            case 2:
                 logger.info("ToDo condiviso, impossibile spostarlo");
                break;
            case 3:
                 logger.info("Utente, bacheca o ToDo non trovati, oppure errore");
                break;
            default:
                 logger.info("Errore sconosciuto");
        }

        return risultato;
    }

    /**
     * Restituisce una nuova lista di ToDo ordinata alfabeticamente in base al titolo.
     *
     * @param visibleToDos la lista di ToDo da ordinare
     * @return una nuova ArrayList di ToDo ordinata per titolo in modo case-insensitive
     */
    public ArrayList<ToDo> getSortedTodosByName(ArrayList<ToDo> visibleToDos) {
        ArrayList<ToDo> sortedTodos = new ArrayList<>();
        sortedTodos.addAll(visibleToDos);
        // Ordina i To-Do in ordine alfabetico per titolo
        sortedTodos.sort(Comparator.comparing(ToDo::getTitle, String.CASE_INSENSITIVE_ORDER));
        return sortedTodos;
    }

    /**
     * Restituisce una lista di ToDo con data di scadenza definita,
     * ordinata in ordine crescente di scadenza (dal più vicino al più lontano).
     *
     * @param visibleToDos la lista di ToDo da filtrare e ordinare
     * @return una nuova lista di ToDo con data di scadenza non nulla, ordinata per data di scadenza
     */
    public ArrayList<ToDo> getTodosOrderedByExpiration(ArrayList<ToDo> visibleToDos) {
        ArrayList<ToDo> todosWithDate = new ArrayList<>();

        // Aggiunge solo i To-Do con expiration non null
        for (ToDo t : visibleToDos) {
            if (t.getExpiration() != null) {
                todosWithDate.add(t);
            }
        }

        // Ordina per data di scadenza (dal più vicino al più lontano)
        todosWithDate.sort(Comparator.comparing(ToDo::getExpiration));
        return todosWithDate;
    }

    /**
     * Restituisce il colore associato a un ToDo specifico, locale o condiviso.
     *
     * @param board il nome della board a cui appartiene il ToDo
     * @param email l'email dell'utente proprietario o del destinatario (se condiviso)
     * @param toDo il titolo del ToDo
     * @param shared true se il ToDo è condiviso, false se è locale
     * @return il colore associato al ToDo
     */
    public Color getColorOfToDo(String board, String email, String toDo, boolean shared) {
        if (shared) {
            // Trova l'email dell'amministratore del ToDo condiviso
            email = getToAdministratorMail(email, toDo); // questo metodo esiste già nel tuo controller
        }
        Connection conn = DatabaseConnection.getInstance().getConnection();
        ToDoDAO todoDAO = new ToDoDAO(conn);
        return todoDAO.getColorOfToDo(board, email, toDo, shared);
    }

    /**
     * Restituisce una lista di ToDo visibili ordinata in base al criterio specificato.
     *
     * @param order il criterio di ordinamento; può essere:
     *              "" (nessun ordinamento),
     *              "sort alphabetically" (ordina alfabeticamente per nome),
     *              "Sort by deadline" (ordina per data di scadenza).
     * @param visibleToDos la lista di ToDo da ordinare
     * @return la lista di ToDo ordinata secondo il criterio specificato,
     *         oppure la lista originale se il criterio non è riconosciuto
     */
    public ArrayList<ToDo> orderedVisibleToDos(String order, ArrayList<ToDo> visibleToDos) {
        switch (order) {
            case "":
                return visibleToDos;
            case "sort alphabetically":
                return getSortedTodosByName(visibleToDos);
            case "Sort by deadline":
                return getTodosOrderedByExpiration(visibleToDos);
            default:
                return visibleToDos;  // default fallback
        }
    }

}