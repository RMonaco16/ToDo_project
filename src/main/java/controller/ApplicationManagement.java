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

    public boolean addToDoInBoard(String email,String tipoEnum, ToDo toDo) {
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
        checkListDAO.addActivity(email, titleToDo, board, activity);
    }//activity va ma nn escono



    public void removeActivity(String email, String titleToDo, String board, String nameActivity) {
        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            CheckListDAO dao = new CheckListDAO(conn);
            dao.removeActivity(email, titleToDo, board, nameActivity);
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

        try {
            // Chiama il metodo per decheckare l'attività (senza data)
            return dao.uncheckActivity(email, board, todo, activity);
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
        int notFound = 0;

        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato
                users.get(i).getActivityHistory().rmvAct(nmAct);
                return;
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }


    public void dltHistory(String email) {
        int notFound = 0;

        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato
                users.get(i).getActivityHistory().dltActs();
                return;
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
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

    public ArrayList<ToDo> getVisibleToDos(User user, String boardName,String filter) {
        ArrayList<ToDo> visibleToDos = new ArrayList<>();

        try {
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            UserDAO userDAO = new UserDAO(conn);
            if(!userDAO.checkBoard(user.getEmail(),boardName)){ // se la bacheca non esiste crea exception
                throw new Exception();
            }else {
                BoardDAO boardDAO = new BoardDAO(conn);
                if (filter.isBlank()) {       // vuole tutti i to-do locali e condivisi
                    //aggiunge tutti i to-do creati localmente dell'utente
                    visibleToDos.addAll(boardDAO.getAllLocalToDos(user.getEmail(), boardName));
                    //aggiunge tutti i to-do che sono stati condivisi all'utente
                    visibleToDos.addAll(boardDAO.getAllSharedToDos(user.getEmail(), boardName));
                } else if (filter.equals("todayFilter")) {
                    visibleToDos.addAll(boardDAO.getLocalTodosByExpirationDate(user.getEmail(),boardName));
                }else{
                    try{
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        LocalDate date = LocalDate.parse(filter, formatter);

                        visibleToDos.addAll(boardDAO.getLocalTodosExpiringBeforeOrOn(user.getEmail(), boardName,date));
                        visibleToDos.addAll(boardDAO.getSharedTodosExpiringBeforeOrOn(user.getEmail(), boardName,date));

                    }catch (DateTimeParseException e) {
                        visibleToDos.add(boardDAO.findToDoByTitleInBoard(user.getEmail(),boardName,filter));
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return visibleToDos;
    }


    public boolean shareToDo(String mailAmministratore, String mailUtenteDestinatario, String boardName, String toDoName) {    // Controllo: solo se mailAmministratore è admin del ToDo si procede

        Connection conn = ConnessioneDatabase.getInstance().getConnection();

        if(conn != null){
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
            if(userDao.emailExists(mailAmministratore) && userDao.emailExists(mailUtenteDestinatario))
                return false;
            //check baord exists
            if(!userDao.checkBoard(mailAmministratore,boardName))
                return false;
            //check if exists to-do to share-------------------------
            ToDo todo = boardDAO.checkToDoExists(mailAmministratore,boardName,toDoName);
            if(todo==null)
                return false;
            //check if already exists the share of this to-do
            Sharing sharing = sharingDAO.checkSharingExists(findUserByEmail(mailAmministratore),todo);
            if(sharing==null){ // check i the sharing already exists
                sharing = new Sharing(findUserByEmail(mailAmministratore),todo);
                sharingDAO.creaSharing(sharing);
            }
            //check if user is already a participant
            if(!sharingDAO.checkUserAlreadySharing(mailUtenteDestinatario,toDoName,mailAmministratore)){
                findUserByEmail(mailUtenteDestinatario).getSharing().add(sharing);
                todo.setCondiviso(true);
            }
            //check if board of to-do already exist in user participant
            TypeBoard enumType = TypeBoard.valueOf(boardName.toUpperCase());
            if(!userDao.checkBoard(mailUtenteDestinatario,boardName)){
                try {
                    Board board = new Board(enumType, "");
                    boardDAO.creaBoard(board,mailUtenteDestinatario);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
            ToDoDAO toDoDAO = new ToDoDAO(conn);
            toDoDAO.addToDoInBoard(mailUtenteDestinatario,boardName,todo);

        }
        return true;



//        User admin = findUserByEmail(mailAmministratore);
//        User destinatario = findUserByEmail(mailUtenteDestinatario);
//
//        if (admin == null) {
//            System.out.println("Amministratore non trovato.");
//            return false;
//        }
//        if (destinatario == null) {
//            System.out.println("Utente destinatario non trovato.");
//            return false;
//        }
//
//        int boardIndex = getBoardIndex(boardName);
//        if (boardIndex == -1 || admin.getBoards()[boardIndex] == null) {
//            System.out.println("Bacheca dell'amministratore non trovata.");
//            return false;
//        }
//
//        Board boardDaCondividere = admin.getBoards()[boardIndex];
//
//
//
//        // Cerca il To-Do da condividere
//        ToDo toShare = null;
//        for (ToDo t : boardDaCondividere.getToDo()) {
//            if (t.getTitle().equalsIgnoreCase(toDoName)) {
//                toShare = t;
//                break;
//            }
//        }
//
//        if (toShare == null) {
//            System.out.println("ToDo non trovato nella bacheca dell'amministratore.");
//            return false;
//        }
//
//
//        // Segna il ToDo come condiviso
//        toShare.setCondiviso(true);
//
//        // Verifica se lo sharing esiste già
//        Sharing existingSharing = null;
//        for (Sharing s : admin.getSharing()) {
//            if (s.getToDo().getTitle().equalsIgnoreCase(toDoName)) {
//                existingSharing = s;
//                break;
//            }
//        }
//
//        if (existingSharing == null) {
//            // Crea lo sharing e aggiungilo all'amministratore
//            existingSharing = new Sharing(admin, toShare);
//            admin.getSharing().add(existingSharing);
//        }
//
//        // Aggiungi l’utente destinatario come membro se non già presente
//        if (!existingSharing.getMembers().contains(destinatario)) {
//            existingSharing.getMembers().add(destinatario);
//        }
//
//        // Aggiungi lo sharing anche nella lista dell'utente destinatario (solo referenza)
//        if (!destinatario.getSharing().contains(existingSharing)) {
//            destinatario.getSharing().add(existingSharing);
//        }
//
//        // ── Invece di assegnad re la stessa Boardi A a B, creiamo una nuova board vuota
//        boolean boardPresente = false;
//        for (Board b : destinatario.getBoards()) {
//            if (b != null && b.getType() == boardDaCondividere.getType()) {
//                boardPresente = true;
//                break;
//            }
//        }
//
//        if (!boardPresente) {
//            // Creo una nuova Board dello stesso tipo, ma senza ToDo
//            Board nuovaBoard = new Board(boardDaCondividere.getType(), "");
//            destinatario.getBoards()[boardIndex] = nuovaBoard;
//        }
//        return true;
//    }
//
//
//    private int getBoardIndex(String boardName){
//            switch (boardName.toUpperCase()) {
//                case "UNIVERSITY":
//                    return 0;
//                case "WORK":
//                    return 1;
//                case "FREETIME":
//                    return 2;
//                default:
//                    return -1;
//            }
    }


    public boolean isUserAdminOfToDo(String emailUtente, String boardName, String toDoTitle) {
        try{
            Connection conn = ConnessioneDatabase.getInstance().getConnection();
            ToDoDAO toDoDAO = new ToDoDAO();
            return toDoDAO.isUserAdminOfToDo(emailUtente,boardName,toDoTitle);
        }catch(Exception e){
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


    public ArrayList<ToDo> getToDoAdminNonCondivisi(String emailUtente, String tipoBacheca) {
        ArrayList<ToDo> filteredToDos = new ArrayList<>();
        int boardIndex = getBoardIndex(tipoBacheca);

        for (User user : users) {
            if (user.getEmail().equals(emailUtente)) {
                Board board = user.getBoards()[boardIndex];
                if (board == null) return filteredToDos;

                for (ToDo todo : board.getToDo()) {
                    // Solo se l'utente è il creatore
                    if (todo.getOwnerEmail().equals(emailUtente)) {
                        filteredToDos.add(todo);
                    }
                }
                break;
            }
        }

        return filteredToDos;
    }

    //-----------------------------------------------------------------------


    public boolean rimuoviUtenteDaCondivisione(String emailUtente, String toDoName, String emailDaEliminare) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equalsIgnoreCase(emailUtente)) {
                // Cicla su tutti gli sharing
                for (int x = 0; x < users.get(i).getSharing().size(); x++) {
                    Sharing sharing = users.get(i).getSharing().get(x);

                    // Verifica che l'utente sia l'amministratore del sharing
                    if (!sharing.getAdministrator().getEmail().equalsIgnoreCase(emailUtente)) {
                        continue;
                    }

                    if (sharing.getToDo().getTitle().equalsIgnoreCase(toDoName)) {
                        // Cerca il membro da eliminare
                        for (int z = 0; z < sharing.getMembers().size(); z++) {
                            User membro = sharing.getMembers().get(z);
                            if (membro.getEmail().equalsIgnoreCase(emailDaEliminare)) {
                                sharing.getMembers().remove(z); // rimuovi membro

                                // Se non ci sono più membri, disattiva la condivisione del To-Do e quindi la sua icona
                                if (sharing.getMembers().isEmpty()) {
                                    sharing.getToDo().setCondiviso(false);
                                    System.out.println("Condivisione disattivata: nessun utente rimasto.");
                                }

                                // Ora rimuovi anche la sharing da lui
                                for (int u = 0; u < membro.getSharing().size(); u++) {
                                    Sharing s = membro.getSharing().get(u);
                                    if (s.getToDo().getTitle().equalsIgnoreCase(toDoName)
                                            && s.getAdministrator().getEmail().equalsIgnoreCase(emailUtente)) {
                                        membro.getSharing().remove(u);
                                        break;
                                    }
                                }

                                // Rimuovi il To-Do condiviso dalla board
                                Board[] boards = membro.getBoards();
                                for (int b = 0; b < boards.length; b++) {
                                    if (boards[b] != null) {
                                        for (int t = 0; t < boards[b].getToDo().size(); t++) {
                                            ToDo todo = boards[b].getToDo().get(t);
                                            if (todo.getTitle().equalsIgnoreCase(toDoName) && todo.isCondiviso()) {
                                                boards[b].getToDo().remove(t);
                                                break;
                                            }
                                        }
                                    }
                                }

                                System.out.println("Utente rimosso con successo.");
                                return true;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Errore: condivisione o utente non trovati.");
        return false;
    }


    public ArrayList<User> getToDoUserShared(String email, String toDo) {
        int notFound = 0;
        ArrayList<User> listaUtenti = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato
                for (int x = 0; x < users.get(i).getSharing().size(); x++) {
                    if (users.get(i).getSharing().get(x).getToDo().getTitle().equalsIgnoreCase(toDo)) {
                        for (int y = 0; y < users.get(i).getSharing().get(x).getMembers().size(); y++) {
                            listaUtenti.add(users.get(i).getSharing().get(x).getMembers().get(y));
                        }
                    }
                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
        return listaUtenti;
    }

    public String getToAdministratorNick(String email, String toDo) {
        int notFound = 0;
        String nickname = "";
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato
                for (int x = 0; x < users.get(i).getSharing().size(); x++) {
                    if (users.get(i).getSharing().get(x).getToDo().getTitle().equalsIgnoreCase(toDo)) {
                        nickname = users.get(i).getSharing().get(x).getAdministrator().getNickname();
                    }
                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
        return nickname;
    }

    public String getToAdministratorMail(String email, String toDo) {
        int notFound = 0;
        String mail = "";
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato
                for (int x = 0; x < users.get(i).getSharing().size(); x++) {
                    if (users.get(i).getSharing().get(x).getToDo().getTitle().equalsIgnoreCase(toDo)) {
                        mail = users.get(i).getSharing().get(x).getAdministrator().getEmail();
                    }
                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
        return mail;
    }

    public int spostaToDoInBacheca(String email, String nomeToDo, String nomeBachecaInCuiSpostare, String nomeBachecaDiOrigine) {
        // return 0 = tutto ok
        // return 1 = to-do già esistente nella bacheca di destinazione
        // return 2 = to-do condiviso, non puoi spostarlo
        // return 3 = utente o bacheca non trovati

        User utente = null;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equalsIgnoreCase(email)) {
                utente = users.get(i);
                break;
            }
        }
        if (utente == null) {
            System.out.println("Utente non trovato...");
            return 3;
        }

        int boardIndexOrigine = getBoardIndex(nomeBachecaDiOrigine);
        int boardIndexDestinazione = getBoardIndex(nomeBachecaInCuiSpostare);

        if (boardIndexOrigine == -1 || boardIndexDestinazione == -1) {
            System.out.println("Bacheca origine o destinazione non valida");
            return 3;
        }

        Board boardOrigine = utente.getBoards()[boardIndexOrigine];
        Board boardDestinazione = utente.getBoards()[boardIndexDestinazione];

        if (boardOrigine == null || boardDestinazione == null) {
            System.out.println("Una delle due bacheche non è presente per l'utente");
            return 3;
        }

        ToDo toDoDaSpostare = null;
        for (int i = 0; i < boardOrigine.getToDo().size(); i++) {
            if (boardOrigine.getToDo().get(i).getTitle().equalsIgnoreCase(nomeToDo)) {
                toDoDaSpostare = boardOrigine.getToDo().get(i);
                break;
            }
        }

        if (toDoDaSpostare == null) {
            System.out.println("ToDo non trovato nella bacheca di origine");
            return 3;
        }

        if (toDoDaSpostare.isCondiviso()) {
            System.out.println("ToDo condiviso, non puoi spostarlo");
            return 2;
        }

        for (int i = 0; i < boardDestinazione.getToDo().size(); i++) {
            if (boardDestinazione.getToDo().get(i).getTitle().equalsIgnoreCase(nomeToDo)) {
                System.out.println("ToDo già presente nella bacheca di destinazione");
                return 1;
            }
        }

        // Copia checklist
        CheckList nuovaCheckList = new CheckList();
        for (int i = 0; i < toDoDaSpostare.getCheckList().getActivities().size(); i++) {
            Activity a = toDoDaSpostare.getCheckList().getActivities().get(i);
            Activity copiaAttivita = new Activity(a.getName(), a.getState());
            nuovaCheckList.addActivity(copiaAttivita);
        }

        ToDo copiaToDo = new ToDo(
                toDoDaSpostare.getTitle(),
                toDoDaSpostare.isState(),
                nuovaCheckList,
                false,  // dopo lo spostamento non è condiviso
                toDoDaSpostare.getOwnerEmail()
        );

        boardDestinazione.getToDo().add(copiaToDo);
        boardOrigine.getToDo().remove(toDoDaSpostare);

        System.out.println("ToDo spostato con successo");
        return 0;
    }

    //restituisce i to-DO che scadono in giornata
//    public ArrayList<ToDo> toDoExpiresToday(String email, String boardName) {
//        int boardIndex = getBoardIndex(boardName);
//        ArrayList<ToDo> toDoExpires = new ArrayList<>();
//        LocalDate today = LocalDate.now();
//
//        for (int i = 0; i < users.size(); i++) {
//            if (email.equals(users.get(i).getEmail())) {
//                Board[] boards = users.get(i).getBoards();
//                if (boardIndex >= 0 && boardIndex < boards.length && boards[boardIndex] != null) {
//                    ArrayList<ToDo> todos = boards[boardIndex].getToDo();
//                    for (ToDo todo : todos) {
//                        if (todo != null && todo.getExpiration() != null && todo.getExpiration().equals(today)) {
//                            toDoExpires.add(todo);
//                        }
//                    }
//                }
//                break; // utente trovato, esco dal ciclo
//            }
//        }
//
//        return toDoExpires;
//    }


    //restituisce tutti i to-Do che scadono entro la data inserita
//    public ArrayList<ToDo> toDoDueBy(String email, String boardName, LocalDate expirationDate) {
//        int boardIndex = getBoardIndex(boardName);
//        int notFound = 0;
//        ArrayList<ToDo> toDoExpires = new ArrayList<>();
//
//        for (int i = 0; i < users.size(); i++) {
//            if (email.equals(users.get(i).getEmail())) {
//                notFound = 1; // utente trovato
//
//                for (int x = 0; x < users.get(i).getBoards()[boardIndex].getToDo().size(); x++) {
//                    ToDo t = users.get(i).getBoards()[boardIndex].getToDo().get(x);
//                    if (t != null && t.getExpiration() != null
//                            && !t.getExpiration().isAfter(expirationDate)) {
//                        toDoExpires.add(t);
//                    }
//                }
//            }
//        }
//
//        if (notFound == 0) {
//            System.out.println("Utente non trovato...");
//        }
//
//        return toDoExpires;
//    }

    //ordina i to-do alfabeticamente
    public ArrayList<ToDo> getSortedTodosByName(ArrayList<ToDo> visibleToDos) {
        ArrayList<ToDo> sortedTodos = new ArrayList<>();
        sortedTodos.addAll( visibleToDos);
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

    public Color getColorOfToDo(String board, String email,String toDo, boolean shared){
        int index = getBoardIndex(board);
        for(User u: users){
            if(u.getEmail().equals(email)){
                for(ToDo t:getVisibleToDos(findUserByEmail(email),board,"")){
                    if(t.getTitle().equals(toDo)){
                        return t.getColor();
                    }
                }
            }
        }
        return null;
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