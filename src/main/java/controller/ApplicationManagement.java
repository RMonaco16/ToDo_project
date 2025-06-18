package controller;

import model.*;

import java.awt.*;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;

import dao.UserDAO;

import java.sql.Connection;
import java.util.ArrayList;

public class ApplicationManagement {

    private ArrayList<User> users = new ArrayList<>();

    public ApplicationManagement() {
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public boolean addUser(User u) {
        if (u.getNickname().equalsIgnoreCase("") || u.getEmail().equalsIgnoreCase("") || u.getEmail().equalsIgnoreCase("")) {
            System.out.println("Utente non creato");
        } else {
            boolean nuovo = false;
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getEmail().equalsIgnoreCase(u.getEmail())) {
                    System.out.println("Mail gia esistente");
                    return true;
                }
            }
            users.add(u);
            System.out.println("Utente Aggiunto Correttamente!!");
            return false;
        }
        return false;
    }

    /*
      public boolean addUser(User u) {
        if (u.getNickname().isBlank() || u.getEmail().isBlank() || u.getPassword().isBlank()) {
            System.out.println("Utente non creato: campi vuoti.");
            return false;
        }

        boolean added = userDAO.addUser(u);
        if (added) {
            System.out.println("Utente aggiunto correttamente!");
            return true;
        } else {
            System.out.println("Utente non aggiunto (forse email già presente).");
            return false;
        }
    }
}
     */

    public boolean login(String email, String password) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail()) && password.equals(users.get(i).getPassword())) {
                System.out.println("Login effettuato con Successo!!");
                notFound = 1;
                return true;
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
        return false;
    }

    public boolean addBoard(String email, Board b) {
        int notFound = 0;
        boolean creato = false;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1;
                creato = users.get(i).addBoard(b);
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
        return creato;
    }

    public void deleteBoard(String email, String type) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1;
                users.get(i).deleteBoard(type);
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public boolean deleteToDo(String email, String nameBoard, String nomeToDo) {
        int boardIndex = getBoardIndex(nameBoard);

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (!user.getEmail().equalsIgnoreCase(email)) continue;

            Board board = user.getBoards()[boardIndex];
            if (board == null) continue;

            for (int t = 0; t < board.getToDo().size(); t++) {
                ToDo todo = board.getToDo().get(t);
                if (!todo.getTitle().equalsIgnoreCase(nomeToDo)) continue;

                if (todo.isCondiviso()) {
                    // Trova la condivisione di cui è amministratore
                    Sharing sharingToRemove = null;

                    for (int s = 0; s < user.getSharing().size(); s++) {
                        Sharing sharing = user.getSharing().get(s);
                        if (sharing.getToDo().getTitle().equalsIgnoreCase(nomeToDo)
                                && sharing.getAdministrator().getEmail().equalsIgnoreCase(email)) {
                            sharingToRemove = sharing;
                            break;
                        }
                    }

                    if (sharingToRemove == null) {
                        System.out.println("Solo l'amministratore può eliminare un ToDo condiviso.");
                        return false;
                    }

                    // Rimuovi il ToDo condiviso da ogni membro
                    for (int m = 0; m < sharingToRemove.getMembers().size(); m++) {
                        User membro = sharingToRemove.getMembers().get(m);

                        // Rimuovi ToDo dalla board del membro
                        Board[] boards = membro.getBoards();
                        for (int b = 0; b < boards.length; b++) {
                            if (boards[b] != null) {
                                for (int td = 0; td < boards[b].getToDo().size(); td++) {
                                    ToDo tRemove = boards[b].getToDo().get(td);
                                    if (tRemove.getTitle().equalsIgnoreCase(nomeToDo) && tRemove.isCondiviso()) {
                                        boards[b].getToDo().remove(td);
                                        break;
                                    }
                                }
                            }
                        }

                        // Rimuovi sharing dal membro
                        for (int s = 0; s < membro.getSharing().size(); s++) {
                            Sharing sRef = membro.getSharing().get(s);
                            if (sRef.getToDo().getTitle().equalsIgnoreCase(nomeToDo)
                                    && sRef.getAdministrator().getEmail().equalsIgnoreCase(email)) {
                                membro.getSharing().remove(s);
                                break;
                            }
                        }
                    }

                    // Rimuovi sharing da admin
                    user.getSharing().remove(sharingToRemove);
                }

                // Elimina definitivamente il ToDo dalla board dell’amministratore
                board.getToDo().remove(t);
                System.out.println("ToDo eliminato con successo.");
                return true;
            }
        }

        System.out.println("ToDo non trovato o utente non autorizzato.");
        return false;
    }


    public ArrayList<Board> printBoard(String email) {
        ArrayList<Board> popolaLista = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                Board[] boards = users.get(i).getBoards();
                if (boards != null) {
                    for (int x = 0; x < boards.length; x++) {
                        popolaLista.add(boards[x]);
                    }
                }
                return popolaLista;
            }
        }
        System.out.println("Utente non loggato...");
        return new ArrayList<>();
    }


    public boolean addToDoInBoard(String email, String tipoEnum, ToDo toDo) {
        int notFound = 0;
        boolean nuova = true;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1;
                nuova = users.get(i).searchBoardAddToDo(tipoEnum, toDo);
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
        return nuova;
    }


    public void addActivity(String email, String titleToDo, String board, Activity activity) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1;
                if (board.equalsIgnoreCase("UNIVERSITY") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].searchToDoAddActivity(titleToDo, activity);
                    return;
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].searchToDoAddActivity(titleToDo, activity);
                    return;
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].searchToDoAddActivity(titleToDo, activity);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void removeActivity(String email, String titleToDo, String board, String nameActivity) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1;
                User u = users.get(i);
                if (board.equalsIgnoreCase("UNIVERSITY") && u.getBoards()[0] != null) {
                    u.getBoards()[0].searchToDoRemoveActivity(titleToDo, nameActivity);
                    u.getBoards()[0].srcToDoifComplete(titleToDo, getVisibleToDos(findUserByEmail(email),board,""));
                    return;
                } else if (board.equalsIgnoreCase("WORK") && u.getBoards()[1] != null) {
                    u.getBoards()[1].searchToDoRemoveActivity(titleToDo, nameActivity);
                    u.getBoards()[1].srcToDoifComplete(titleToDo, getVisibleToDos(findUserByEmail(email),board,""));
                    return;
                } else if (board.equalsIgnoreCase("FREETIME") && u.getBoards()[2] != null) {
                    u.getBoards()[2].searchToDoRemoveActivity(titleToDo, nameActivity);
                    u.getBoards()[2].srcToDoifComplete(titleToDo, getVisibleToDos(findUserByEmail(email),board,""));
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public boolean editToDo(String email, String board, String ToDoToSrc, String newNameToDo, String description, LocalDate expiration, String image, Color color) {
        int notFound = 0;
        boolean result = false;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato
                if (board.equalsIgnoreCase("UNIVERSITY") && users.get(i).getBoards()[0] != null) {
                    result = users.get(i).getBoards()[0].srcToDoToEdit(ToDoToSrc, newNameToDo, description, expiration, image, color);
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    result = users.get(i).getBoards()[1].srcToDoToEdit(ToDoToSrc, newNameToDo, description, expiration, image, color);
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    result = users.get(i).getBoards()[2].srcToDoToEdit(ToDoToSrc, newNameToDo, description, expiration, image, color);
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
        return result;
    }

    public void checkActivity(String email, String board, String todo, String activity, String dataCompletamento) {
        User user = findUserByEmail(email);
        if (user == null) {
            System.out.println("Utente non Loggato...");
            return;
        }

        int boardIndex = getBoardIndex(board);
        if (boardIndex == -1) {
            System.out.println("Board non valida.");
            return;
        }

        Board bacheca = user.getBoards()[boardIndex];
        if (bacheca == null) {
            System.out.println("Bacheca non trovata.");
            return;
        }

        ToDo target = null;
        for (ToDo t : bacheca.getToDo()) {
            if (t.getTitle().equalsIgnoreCase(todo)) {
                target = t;
                break;
            }
        }

        if (target == null) {
            // Cerca anche tra i ToDo condivisi
            for (Sharing s : user.getSharing()) {
                if (s.getToDo().getTitle().equalsIgnoreCase(todo)) {
                    target = s.getToDo();
                    break;
                }
            }
        }

        if (target == null) {
            System.out.println("ToDo non trovato.");
            return;
        }

        // Controlla i permessi
        if (!canUserModifyToDo(email, target)) {
            System.out.println("Permessi insufficienti per modificare questo ToDo.");
            return;
        }

        // Esegui l’azione
        target.checkActivity(activity, dataCompletamento);
        addHistoryAct(email, board, todo, activity);
        target.checkIfComplete();
        user.getBoards()[boardIndex].srcToDoifComplete(target.getTitle(), getVisibleToDos(findUserByEmail(email),board,""));
    }

    public void deCheckActivity(String email, String board, String todo, String activity) {
        User user = findUserByEmail(email);
        if (user == null) {
            System.out.println("Utente non Loggato...");
            return;
        }

        int boardIndex = getBoardIndex(board);
        if (boardIndex == -1) {
            System.out.println("Board non valida.");
            return;
        }

        Board bacheca = user.getBoards()[boardIndex];
        if (bacheca == null) {
            System.out.println("Bacheca non trovata.");
            return;
        }

        ToDo target = null;
        for (ToDo t : bacheca.getToDo()) {
            if (t.getTitle().equalsIgnoreCase(todo)) {
                target = t;
                t.setState(false);
                break;
            }
        }

        if (target == null) {
            for (Sharing s : user.getSharing()) {
                if (s.getToDo().getTitle().equalsIgnoreCase(todo)) {
                    target = s.getToDo();
                    break;
                }
            }
        }

        if (target == null) {
            System.out.println("ToDo non trovato.");
            return;
        }

        if (!canUserModifyToDo(email, target)) {
            System.out.println("Permessi insufficienti per modificare questo ToDo.");
            return;
        }

        target.deCheckActivity(activity);
        user.getBoards()[boardIndex].srcToDoifComplete(target.getTitle(), getVisibleToDos(findUserByEmail(email),board,""));
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

    public void addHistoryAct(String email, String board, String todo, String activity) {
        int notFound = 0;

        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato

                int boardIndex = -1;
                if (board.equalsIgnoreCase("UNIVERSITY")) {
                    boardIndex = 0;
                } else if (board.equalsIgnoreCase("WORK")) {
                    boardIndex = 1;
                } else if (board.equalsIgnoreCase("FREETIME")) {
                    boardIndex = 2;
                }

                if (boardIndex != -1 && users.get(i).getBoards()[boardIndex] != null) {
                    Board currentBoard = users.get(i).getBoards()[boardIndex];

                    for (int x = 0; x < currentBoard.getToDo().size(); x++) {
                        if (currentBoard.getToDo().get(x).getTitle().equalsIgnoreCase(todo)) {

                            for (int y = 0; y < currentBoard.getToDo().get(x).getCheckList().getActivities().size(); y++) {
                                if (currentBoard.getToDo().get(x).getCheckList().getActivities().get(y).getName().equalsIgnoreCase(activity)) {

                                    users.get(i).getActivityHistory().AddActivityHistory(
                                            currentBoard.getToDo().get(x).getCheckList().getActivities().get(y)
                                    );
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public ArrayList<Activity> returnCompletedActivity(String email) {
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                // Utente trovato: ritorna la lista di attività completate
                return users.get(i).getActivityHistory().print();
            }
        }
        // Utente non trovato
        System.out.println("Utente non loggato...");
        return new ArrayList<>(); // Ritorna lista vuota
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
        int notFound = 0;
        ArrayList<ToDo> listaVuota = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato

                if (board.equalsIgnoreCase("UNIVERSITY") && users.get(i).getBoards()[0] != null) {
                    return users.get(i).getBoards()[0].getToDo();
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    return users.get(i).getBoards()[1].getToDo();
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    return users.get(i).getBoards()[2].getToDo();
                } else {
                    System.out.println("Bacheca non trovata o vuota.");
                    return listaVuota;
                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
        return listaVuota;
    }

    public void swapToDo(String email, String board, String todo, int j) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; //trovato
                if (board.equalsIgnoreCase("universita") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].srcTodoSwap(todo, j);
                    return;
                } else if (board.equalsIgnoreCase("lavoro") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].srcTodoSwap(todo, j);
                    return;
                } else if (board.equalsIgnoreCase("tempo libero") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].srcTodoSwap(todo, j);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void printTodoRange(String email, String board, LocalDate range) { //per parametro range o data di oggi decisa nel main
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato
                if (board.equalsIgnoreCase("UNIVERSITY") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].printRange(range);
                    return;
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].printRange(range);
                    return;
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].printRange(range);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
    }


    public ArrayList<Activity> printActs(String email, String board, String todo) {
        int notFound = 0;
        ArrayList<Activity> listaVuota = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato

                if (board.equalsIgnoreCase("UNIVERSITY") && users.get(i).getBoards()[0] != null) {
                    return users.get(i).getBoards()[0].srcTodoPrint(todo);
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].srcTodoPrint(todo);
                    return users.get(i).getBoards()[1].srcTodoPrint(todo);
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].srcTodoPrint(todo);
                    return users.get(i).getBoards()[2].srcTodoPrint(todo);
                } else {
                    System.out.println("Bacheca non trovata o vuota.");
                    return listaVuota;
                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
        return listaVuota;
    }

    public ArrayList<ToDo> getVisibleToDos(User user, String boardName,String filter) {
        ArrayList<ToDo> visibleToDos = new ArrayList<>();


        if(filter.equals("")){
            int boardIndex = getBoardIndex(boardName);
            // Aggiungi i To-Do della board dell’utente (se esiste)
            if (boardIndex != -1 && user.getBoards()[boardIndex] != null) {
                visibleToDos.addAll(user.getBoards()[boardIndex].getToDo());
            }

            // Aggiungi i To-Do condivisi per quell’utente nella stessa board
            for (Sharing s : user.getSharing()) {
                ToDo sharedToDo = s.getToDo();
                if (sharedToDo != null) {
                    // Verifica che il To-Do condiviso appartenga alla board con quel nome
                    User admin = s.getAdministrator();
                    if (admin != null) {
                        int adminBoardIndex = getBoardIndex(boardName);
                        if (adminBoardIndex != -1 && admin.getBoards()[adminBoardIndex] != null) {
                            Board adminBoard = admin.getBoards()[adminBoardIndex];
                            if (adminBoard.getToDo().contains(sharedToDo)) {
                                // Aggiungi solo se non è già nella lista (evita duplicati)
                                if (!visibleToDos.contains(sharedToDo)) {
                                    visibleToDos.add(sharedToDo);
                                }
                            }
                        }
                    }
                }
            }
            }else if(filter.equals("todayFilter")){
                visibleToDos.addAll(toDoExpiresToday(user.getEmail(),boardName));

            }else{
                try{
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    LocalDate date = LocalDate.parse(filter, formatter);

                    visibleToDos = toDoDueBy(user.getEmail(), boardName,date);
                }catch (DateTimeParseException e){

                    int boardIndex = getBoardIndex(boardName);
                    // Aggiungi i To-Do della board dell’utente (se esiste)
                    if (boardIndex != -1 && user.getBoards()[boardIndex] != null) {
                        for(ToDo t: user.getBoards()[boardIndex].getToDo()){
                            if(t.getTitle().equalsIgnoreCase(filter)){
                                visibleToDos.add(t);
                            }
                        }
                    }

                    // Aggiungi i To-Do condivisi per quell’utente nella stessa board
                    for (Sharing s : user.getSharing()) {
                        ToDo sharedToDo = s.getToDo();
                        if (sharedToDo != null) {
                            // Verifica che il To-Do condiviso appartenga alla board con quel nome
                            User admin = s.getAdministrator();
                            if (admin != null) {
                                int adminBoardIndex = getBoardIndex(boardName);
                                if (adminBoardIndex != -1 && admin.getBoards()[adminBoardIndex] != null) {
                                    Board adminBoard = admin.getBoards()[adminBoardIndex];
                                    if (adminBoard.getToDo().contains(sharedToDo)) {
                                        // Aggiungi solo se non è già nella lista (evita duplicati)
                                        if (!visibleToDos.contains(sharedToDo) && sharedToDo.getTitle().equalsIgnoreCase(filter)) {
                                            visibleToDos.add(sharedToDo);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }



        return visibleToDos;
    }


    public boolean shareToDo(String mailAmministratore, String mailUtenteDestinatario, String boardName, String toDoName) {    // Controllo: solo se mailAmministratore è admin del ToDo si procede
        if (!isUserAdminOfToDo(mailAmministratore, boardName, toDoName)) {
            System.out.println("Errore: l'utente non è amministratore del ToDo e non può condividerlo.");
            return false;
        }
        if (mailAmministratore.equalsIgnoreCase(mailUtenteDestinatario)) {
            System.out.println("Errore: non puoi condividere un ToDo con te stesso.");
            return false;
        }

        User admin = findUserByEmail(mailAmministratore);
        User destinatario = findUserByEmail(mailUtenteDestinatario);

        if (admin == null) {
            System.out.println("Amministratore non trovato.");
            return false;
        }
        if (destinatario == null) {
            System.out.println("Utente destinatario non trovato.");
            return false;
        }

        int boardIndex = getBoardIndex(boardName);
        if (boardIndex == -1 || admin.getBoards()[boardIndex] == null) {
            System.out.println("Bacheca dell'amministratore non trovata.");
            return false;
        }

        Board boardDaCondividere = admin.getBoards()[boardIndex];

        // Cerca il To-Do da condividere
        ToDo toShare = null;
        for (ToDo t : boardDaCondividere.getToDo()) {
            if (t.getTitle().equalsIgnoreCase(toDoName)) {
                toShare = t;
                break;
            }
        }

        if (toShare == null) {
            System.out.println("ToDo non trovato nella bacheca dell'amministratore.");
            return false;
        }

        // Segna il ToDo come condiviso
        toShare.setCondiviso(true);

        // Verifica se lo sharing esiste già
        Sharing existingSharing = null;
        for (Sharing s : admin.getSharing()) {
            if (s.getToDo().getTitle().equalsIgnoreCase(toDoName)) {
                existingSharing = s;
                break;
            }
        }

        if (existingSharing == null) {
            // Crea lo sharing e aggiungilo all'amministratore
            existingSharing = new Sharing(admin, toShare);
            admin.getSharing().add(existingSharing);
        }

        // Aggiungi l’utente destinatario come membro se non già presente
        if (!existingSharing.getMembers().contains(destinatario)) {
            existingSharing.getMembers().add(destinatario);
        }

        // Aggiungi lo sharing anche nella lista dell'utente destinatario (solo referenza)
        if (!destinatario.getSharing().contains(existingSharing)) {
            destinatario.getSharing().add(existingSharing);
        }

        // ── Invece di assegnare la stessa Board di A a B, creiamo una nuova board vuota
        boolean boardPresente = false;
        for (Board b : destinatario.getBoards()) {
            if (b != null && b.getType() == boardDaCondividere.getType()) {
                boardPresente = true;
                break;
            }
        }

        if (!boardPresente) {
            // Creo una nuova Board dello stesso tipo, ma senza ToDo
            Board nuovaBoard = new Board(boardDaCondividere.getType(), "");
            destinatario.getBoards()[boardIndex] = nuovaBoard;
        }
        return true;
    }


    private int getBoardIndex(String boardName) {
        switch (boardName.toUpperCase()) {
            case "UNIVERSITY":
                return 0;
            case "WORK":
                return 1;
            case "FREETIME":
                return 2;
            default:
                return -1;
        }
    }

    public boolean isUserAdminOfToDo(String emailUtente, String boardName, String toDoTitle) {
        User user = findUserByEmail(emailUtente);
        if (user == null) return false;

        int boardIndex = getBoardIndex(boardName);
        if (boardIndex == -1) return false;

        Board board = user.getBoards()[boardIndex];
        if (board == null) return false;

        for (ToDo t : board.getToDo()) {
            if (t.getTitle().equalsIgnoreCase(toDoTitle)) {
                // Utente possiede questo To-Do nella board: è admin
                return true;
            }
        }
        return false;
    }

    public User findUserByEmail(String email) {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
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
    public ArrayList<ToDo> toDoExpiresToday(String email, String boardName) {
        int boardIndex = getBoardIndex(boardName);
        ArrayList<ToDo> toDoExpires = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                Board[] boards = users.get(i).getBoards();
                if (boardIndex >= 0 && boardIndex < boards.length && boards[boardIndex] != null) {
                    ArrayList<ToDo> todos = boards[boardIndex].getToDo();
                    for (ToDo todo : todos) {
                        if (todo != null && todo.getExpiration() != null && todo.getExpiration().equals(today)) {
                            toDoExpires.add(todo);
                        }
                    }
                }
                break; // utente trovato, esco dal ciclo
            }
        }

        return toDoExpires;
    }


    //restituisce tutti i to-Do che scadono entro la data inserita
    public ArrayList<ToDo> toDoDueBy(String email, String boardName, LocalDate expirationDate) {
        int boardIndex = getBoardIndex(boardName);
        int notFound = 0;
        ArrayList<ToDo> toDoExpires = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato

                for (int x = 0; x < users.get(i).getBoards()[boardIndex].getToDo().size(); x++) {
                    ToDo t = users.get(i).getBoards()[boardIndex].getToDo().get(x);
                    if (t != null && t.getExpiration() != null
                            && !t.getExpiration().isAfter(expirationDate)) {
                        toDoExpires.add(t);
                    }
                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }

        return toDoExpires;
    }

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