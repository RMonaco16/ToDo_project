package controller;

import dao.*;
import db.ConnessioneDatabase;
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

import java.util.ArrayList;

public class ApplicationManagement {

    ArrayList<User> users = new ArrayList<>();

    private User currentUser;

    public ApplicationManagement() {
    }

    public boolean addUser(User u) {
        if (u.getNickname().isBlank() || u.getEmail().isBlank() || u.getPassword().isBlank()) {
            System.out.println("Utente non creato: campi vuoti.");
            return false;
        }

        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);

            // 1. Controlla se l'email esiste
            if (userDAO.emailExists(u.getEmail())) {
                System.out.println("Email già presente!");
                return false;
            }

            // 2. Inserisce utente se email non esiste
            boolean added = userDAO.creaUser(u);

            if (added) {
                System.out.println("Utente aggiunto correttamente!");
            } else {
                System.out.println("Errore nell'aggiunta dell'utente.");
            }

            return added;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String email, String password) {
        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);

            User user = userDAO.getUserByEmailAndPassword(email, password);
            if (user != null) {
                this.currentUser = user;
                System.out.println("Login effettuato con Successo!!");
                return true;
            } else {
                System.out.println("Email o password errati.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean addBoard(String email, Board b) {
        try {
            // Ottieni connessione dal singleton
            Connection conn = ConnessioneDatabase.getInstance().getConnection();

            // Crea DAO della board e inserisci board nel database
            BoardDAO boardDAO = new BoardDAO(conn);
            boolean created = boardDAO.creaBoard(b, email); // passa l'email al DAO

            if (created) {
                System.out.println("Board creata correttamente.");
            } else {
                System.out.println("Board NON creata.");
            }
            return created;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteBoard(String email, String type) {
        try {
            // Ottieni connessione dal singleton
            Connection conn = ConnessioneDatabase.getInstance().getConnection();

            // Crea DAO della board e inserisci board nel database
            BoardDAO boardDAO = new BoardDAO(conn);
            boardDAO.eliminaBoard(email, type); // passa l'email al DAO

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Board> printBoard(String email) {
        BoardDAO boardDAO = new BoardDAO(ConnessioneDatabase.getInstance().getConnection());
        return boardDAO.getBoardsByEmail(email);
    }

    public boolean addToDoInBoard(String email, String tipoEnum, ToDo toDo) {
        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            ToDoDAO toDoDAO = new ToDoDAO(conn);
            boolean success = toDoDAO.addToDoInBoard(email, tipoEnum, toDo);

            if (success) {
                System.out.println("ToDo aggiunto correttamente.");
            } else {
                System.out.println("Errore nell'aggiunta del ToDo.");
            }

            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteToDo(String email, String board, String title) {
        try {
            ToDoDAO dao = new ToDoDAO(ConnessioneDatabase.getInstance().getConnection());
            return dao.deleteToDo(email, board, title);
        } catch (SQLException e) {
            System.err.println("Errore durante l'eliminazione del ToDo: " + e.getMessage());
            return false;
        }
    }


    public void addActivity(String email, String titleToDo, String board, Activity activity) {
        if (!board.equalsIgnoreCase("UNIVERSITY") && !board.equalsIgnoreCase("WORK") && !board.equalsIgnoreCase("FREETIME")) {
            System.out.println("Tipo di bacheca non valido.");
            return;
        }

        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        CheckListDAO checkListDAO = new CheckListDAO(conn);
        ToDoDAO toDoDAO = new ToDoDAO(conn);

        try {
            checkListDAO.addActivity(email, titleToDo, board, activity);

            // Controllo e aggiornamento dello stato del ToDo dopo aggiunta attività
            int toDoId = checkListDAO.getToDoId(email, board, titleToDo);
            toDoDAO.checkIfComplete(toDoId);

        } catch (SQLException e) {
            System.err.println("Errore SQL durante addActivity: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore generico durante addActivity");
        }
    }


    public void removeActivity(String email, String titleToDo, String board, String nameActivity) {
        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            CheckListDAO dao = new CheckListDAO(conn);
            ToDoDAO toDoDAO = new ToDoDAO(conn);

            dao.removeActivity(email, titleToDo, board, nameActivity);

            // Aggiorna lo stato del ToDo dopo la rimozione
            int toDoId = dao.getToDoId(email, board, titleToDo);
            toDoDAO.checkIfComplete(toDoId);

        } catch (SQLException e) {
            System.err.println("Errore nella connessione al database: " + e.getMessage());
        }
    }


    public boolean editToDo(String email, String board, String toDoTitleOld, String newTitle,
                            String description, LocalDate expiration, String image, Color color) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        ToDoDAO dao = new ToDoDAO(conn);

        return dao.updateToDo(email, board, toDoTitleOld, newTitle, description, expiration, image, color);
    }


    public void checkActivity(String email, String board, String todo, String activity, String dataCompletamento) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
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
                System.out.println("Data di completamento non fornita, cronologia non aggiornata.");
            }

            // Controllo e aggiornamento dello stato del ToDo
            int toDoId = dao.getToDoId(email, board, todo);
            toDoDAO.checkIfComplete(toDoId);

        } catch (ParseException e) {
            System.out.println("Errore nel parsing della data: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Errore SQL: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore generico nel controller durante checkActivity");
        }
    }


    public boolean deCheckActivity(String email, String board, String todo, String activity) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        CheckListDAO dao = new CheckListDAO(conn);
        ToDoDAO toDoDAO = new ToDoDAO(conn);

        try {
            boolean result = dao.uncheckActivity(email, board, todo, activity);

            // Aggiorna lo stato del ToDo dopo la modifica
            int toDoId = dao.getToDoId(email, board, todo);
            toDoDAO.checkIfComplete(toDoId);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore nel controller durante deCheckActivity");
            return false;
        }
    }


    public boolean canUserModifyToDo(String userEmail, ToDo toDo) {
        if (toDo.getOwnerEmail().equalsIgnoreCase(userEmail)) {
            // È proprietario
            return true;
        }

        // Altrimenti controllo se è membro della condivisione
        User user = findUserByEmail(userEmail);
        if (user == null) return false;

        for (Sharing s : user.getSharing()) {
            if (s.getToDo().getTitle().equalsIgnoreCase(toDo.getTitle())
                    && s.getAdministrator().getEmail().equalsIgnoreCase(toDo.getOwnerEmail())) {
                // L'utente è membro della condivisione
                return true;
            }
        }

        return false; // Non è né proprietario né membro
    }

    public ArrayList<Activity> returnCompletedActivity(String email) {
        ArrayList<Activity> completedActivities = new ArrayList<>();

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection()) {
            CompletedActivityHistoryDAO dao = new CompletedActivityHistoryDAO(conn);
            completedActivities = dao.getCompletedActivitiesByUser(email);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore nel recupero delle attività completate da DB");
        }

        return completedActivities;
    }


    public void addHistoryAct(String email, String activityName, Date completionDate) throws SQLException {
        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            CompletedActivityHistoryDAO dao = new CompletedActivityHistoryDAO(conn);
            dao.addActivityToHistory(email, activityName, completionDate);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore nel controller durante l'aggiunta alla cronologia");
        }
    }


    public void rmvHistoryAct(String email, String nmAct) {
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection()) {
            CompletedActivityHistoryDAO dao = new CompletedActivityHistoryDAO(conn);
            boolean removed = dao.removeActivityFromHistory(email, nmAct);

            if (!removed) {
                System.out.println("Attività non trovata nella cronologia per l'utente: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore durante la rimozione dalla cronologia");
        }
    }

    public void dltHistory(String email) {
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection()) {
            CompletedActivityHistoryDAO dao = new CompletedActivityHistoryDAO(conn);
            boolean deleted = dao.deleteAllActivitiesFromHistory(email);

            if (!deleted) {
                System.out.println("Nessuna attività trovata o utente non esistente: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore durante l'eliminazione della cronologia");
        }
    }

    public ArrayList<ToDo> printTodo(String email, String board) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        ToDoDAO toDoDAO = new ToDoDAO(conn);
        return toDoDAO.getTodosByUserAndBoard(email, board);
    }

    public ArrayList<Activity> printActs(String email, String board, String todoTitle) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        CheckListDAO dao = new CheckListDAO(conn);
        return dao.getActivities(email, board, todoTitle);
    }

    public ArrayList<ToDo> getVisibleToDos(User user, String boardName, String filter) {
        ArrayList<ToDo> visibleToDos = new ArrayList<>();

        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);
            if (!userDAO.checkBoard(user.getEmail(), boardName)) { // se la bacheca non esiste crea exception
                throw new Exception();
            } else {
                BoardDAO boardDAO = new BoardDAO(conn);
                if (filter.isBlank()) {       // vuole tutti i to-do locali e condivisi
                    //aggiunge tutti i to-do creati localmente dell'utente
                    visibleToDos.addAll(boardDAO.getAllLocalToDos(user.getEmail(), boardName));
                    //aggiunge tutti i to-do che sono stati condivisi all'utente
                    visibleToDos.addAll(boardDAO.getAllSharedToDos(user.getEmail(), boardName));
                } else if (filter.equals("todayFilter")) {
                    visibleToDos.addAll(boardDAO.getLocalTodosByExpirationDate(user.getEmail(), boardName));
                } else {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        LocalDate date = LocalDate.parse(filter, formatter);

                        visibleToDos.addAll(boardDAO.getLocalTodosExpiringBeforeOrOn(user.getEmail(), boardName, date));
                        visibleToDos.addAll(boardDAO.getSharedTodosExpiringBeforeOrOn(user.getEmail(), boardName, date));

                    } catch (DateTimeParseException e) {
                        visibleToDos.add(boardDAO.findToDoByTitleInBoard(user.getEmail(), boardName, filter));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return visibleToDos;
    }


    public boolean shareToDo(String mailAmministratore, String mailUtenteDestinatario, String boardName, String toDoName) {

        Connection conn = ConnessioneDatabase.getInstance().getConnection();

        if (conn == null) return false;

        if (!isUserAdminOfToDo(mailAmministratore, boardName, toDoName)) {
            System.out.println("Errore: l'utente non è amministratore del ToDo e non può condividerlo.");
            return false;
        }

        if (mailAmministratore.equalsIgnoreCase(mailUtenteDestinatario)) {
            System.out.println("Errore: non puoi condividere un ToDo con te stesso.");
            return false;
        }

        UserDAO userDao = new UserDAO(conn);
        BoardDAO boardDAO = new BoardDAO(conn);
        SharingDAO sharingDAO = new SharingDAO(conn);

        // verifica email esistenti
        if (!userDao.emailExists(mailAmministratore) || !userDao.emailExists(mailUtenteDestinatario)) {
            System.out.println("Email mittente o destinatario non trovata");
            return false;
        }

        // verifica esistenza ToDo
        ToDo todo = boardDAO.checkToDoExists(mailAmministratore, boardName, toDoName);
        if (todo == null) {
            System.out.println("ToDo del mittente non trovata");
            return false;
        }

        // id del todo
        ToDoDAO todoDAO = new ToDoDAO(conn);
        Integer todoId = todoDAO.getTodoIdByTitleUserAndBoard(toDoName, mailAmministratore, boardName);

        // verifica esistenza sharing
        Sharing sharing = sharingDAO.checkSharingExists(findUserByEmail(mailAmministratore), todo);
        if (sharing == null) {
            sharing = new Sharing(findUserByEmail(mailAmministratore), todo);
            sharingDAO.creaSharing(sharing);
            System.out.println("Creazione nuova condivisione");
            todoDAO.setCondivisoTrueById(todoId);
        } else {
            System.out.println("Condivisione già esistente");
        }

        // verifica se l'utente destinatario è già membro della condivisione
        if (!sharingDAO.checkUserAlreadySharing(mailUtenteDestinatario, toDoName, mailAmministratore)) {
            // verifica esistenza board mittente
            if (!userDao.checkBoard(mailUtenteDestinatario, boardName)) {
                Board board;
                switch (boardName){
                    case "UNIVERSITY": board = new Board(TypeBoard.UNIVERSITY,"");break;
                    case "WORK": board = new Board(TypeBoard.WORK,"");break;
                    case "FREETIME": board = new Board(TypeBoard.FREETIME,"");break;
                    default: return false;
                }
                boardDAO.creaBoard(board,mailUtenteDestinatario);
            }
            sharingDAO.aggiungiMembroSharing(mailUtenteDestinatario, mailAmministratore, toDoName);
            System.out.println("Utente aggiunto come partecipante alla condivisione");
            // imposta flag condiviso nel ToDo
        } else {
            System.out.println("Utente già partecipante alla condivisione");
        }


        return true;
    }

    public boolean isUserAdminOfToDo(String emailUtente, String boardName, String toDoTitle) {
        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            ToDoDAO toDoDAO = new ToDoDAO();
            return toDoDAO.isUserAdminOfToDo(emailUtente, boardName, toDoTitle);
        } catch (Exception e) {
            System.out.println("Errore verfica se Utente è anche admin");
            e.printStackTrace();
        }
        return false;
    }

    public User findUserByEmail(String email) {
        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);

            User u = userDAO.leggiUserPerEmail(email);
            return u;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // solo se c’è errore
    }


    public ArrayList<String> getToDoAdminNonCondivisi(String emailUtente, String tipoBacheca) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        SharingDAO sharingDAO = new SharingDAO(conn);
        return sharingDAO.getToDoTitlesAdminNonCondivisi(emailUtente, tipoBacheca);
    }

    //-----------------------------------------------------------------------


    public boolean rimuoviUtenteDaSharing(String mailAmministratore, String mailUtenteDaRimuovere, String boardName, String toDoTitle) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        if (conn == null) return false;

        // 1. Verifica se sei amministratore del ToDo
        if (!isUserAdminOfToDo(mailAmministratore, boardName, toDoTitle)) {
            System.out.println("Errore: l'utente non è amministratore del ToDo.");
            return false;
        }

        if (mailAmministratore.equalsIgnoreCase(mailUtenteDaRimuovere)) {
            System.out.println("Errore: non puoi rimuovere te stesso dalla condivisione.");
            return false;
        }

        UserDAO userDao = new UserDAO(conn);
        BoardDAO boardDAO = new BoardDAO(conn);
        ToDoDAO toDoDAO = new ToDoDAO(conn);
        SharingDAO sharingDAO = new SharingDAO(conn);

        // 2. Verifica esistenza utenti
        if (!userDao.emailExists(mailUtenteDaRimuovere) || !userDao.emailExists(mailAmministratore)) {
            System.out.println("Errore: uno degli utenti non esiste.");
            return false;
        }

        // 3. Recupera ID ToDo
        Integer todoId = toDoDAO.getTodoIdByTitleUserAndBoard(toDoTitle, mailAmministratore, boardName);
        if (todoId == null) {
            System.out.println("Errore: ToDo non trovato.");
            return false;
        }

        // 4. Rimuovi utente da sharing
        boolean removed = sharingDAO.rimuoviMembroSharing(mailUtenteDaRimuovere, todoId);
        if (!removed) {
            System.out.println("Errore: impossibile rimuovere l’utente (forse non è membro).");
            return false;
        }

        // 5. Verifica se ci sono ancora membri
        int membriRimasti = sharingDAO.countMembriSharing(todoId);
        if (membriRimasti == 0) {
            toDoDAO.setCondivisoFalseById(todoId);
            sharingDAO.eliminaSharingSeVuoto(todoId);
            System.out.println("Condivisione terminata: nessun membro rimanente.");
        } else {
            System.out.println("Utente rimosso con successo dalla condivisione.");
        }

        return true;
    }


    public ArrayList<User> getToDoUserShared(String email, String toDo) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        SharingDAO sharingDAO = new SharingDAO(conn);
        return sharingDAO.getSharingUserShared(email, toDo);
    }

    public String getToAdministratorNick(String email, String toDo) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        SharingDAO sharingDAO = new SharingDAO(conn);
        return sharingDAO.getSharingAdministratorNick(email, toDo);
    }

    public String getToAdministratorMail(String email, String toDo) {
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        SharingDAO sharingDAO = new SharingDAO(conn);
        return sharingDAO.getSharingAdministratorEmail(email, toDo);
    }

    public int spostaToDoInBacheca(String email, String nomeToDo, String nomeBachecaInCuiSpostare, String nomeBachecaDiOrigine) {
        ToDoDAO toDoDAO = new ToDoDAO();

        int risultato = toDoDAO.spostaToDoInBacheca(email, nomeToDo, nomeBachecaInCuiSpostare, nomeBachecaDiOrigine);

        switch (risultato) {
            case 0:
                System.out.println("ToDo spostato correttamente");
                break;
            case 1:
                System.out.println("ToDo già presente nella bacheca di destinazione");
                break;
            case 2:
                System.out.println("ToDo condiviso, impossibile spostarlo");
                break;
            case 3:
                System.out.println("Utente, bacheca o ToDo non trovati, oppure errore");
                break;
            default:
                System.out.println("Errore sconosciuto");
        }

        return risultato;
    }



    //ordina i to-do alfabeticamente
    public ArrayList<ToDo> getSortedTodosByName(ArrayList<ToDo> visibleToDos) {
        ArrayList<ToDo> sortedTodos = new ArrayList<>();
        sortedTodos.addAll(visibleToDos);
        // Ordina i To-Do in ordine alfabetico per titolo
        sortedTodos.sort(Comparator.comparing(ToDo::getTitle, String.CASE_INSENSITIVE_ORDER));
        return sortedTodos;
    }

    //ordina i to-do in base alla data
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

    public Color getColorOfToDo(String board, String email, String toDo, boolean shared) {
        if (shared) {
            // Trova l'email dell'amministratore del ToDo condiviso
            email = getToAdministratorMail(email, toDo); // questo metodo esiste già nel tuo controller
        }
        Connection conn = ConnessioneDatabase.getInstance().getConnection();
        ToDoDAO todoDAO = new ToDoDAO(conn);
        return todoDAO.getColorOfToDo(board, email, toDo, shared);
    }


    public ArrayList<ToDo> orderedVisibleToDos(String order,ArrayList<ToDo> visibleToDos){
        switch (order){
            case "":
                return visibleToDos;
            case "sort alphabetically":
                return getSortedTodosByName(visibleToDos);
            case "Sort by deadline":
                return getTodosOrderedByExpiration(visibleToDos);
        }
        return visibleToDos;
    }

}