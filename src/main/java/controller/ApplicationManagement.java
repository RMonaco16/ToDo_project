package controller;

import model.*;

import java.sql.SQLOutput;
import java.time.LocalDate;
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
        if(u.getNickname().equalsIgnoreCase("") || u.getEmail().equalsIgnoreCase("") || u.getEmail().equalsIgnoreCase("")){
            System.out.println("Utente non creato");
        }else{
            boolean nuovo = false;
            for(int i = 0; i < users.size(); i ++){
                if(users.get(i).getEmail().equalsIgnoreCase(u.getEmail())){
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


    public void addToDoInBoard(String email, String tipoEnum, ToDo toDo) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1;
                users.get(i).searchBoardAddToDo(tipoEnum, toDo);
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
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
                    u.getBoards()[0].srcToDoifComplete(titleToDo);
                    return;
                } else if (board.equalsIgnoreCase("WORK") && u.getBoards()[1] != null) {
                    u.getBoards()[1].searchToDoRemoveActivity(titleToDo, nameActivity);
                    u.getBoards()[0].srcToDoifComplete(titleToDo);
                    return;
                } else if (board.equalsIgnoreCase("FREETIME") && u.getBoards()[2] != null) {
                    u.getBoards()[2].searchToDoRemoveActivity(titleToDo, nameActivity);
                    u.getBoards()[0].srcToDoifComplete(titleToDo);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void editToDo(String email,String board,String ToDoToSrc,String newNameToDo,String description,LocalDate expiration,String image, String color){
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato
                if (board.equalsIgnoreCase("UNIVERSITY") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].srcToDoToEdit(ToDoToSrc, newNameToDo, description, expiration, image,  color);
                    return;
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].srcToDoToEdit(ToDoToSrc, newNameToDo, description, expiration, image,  color);
                    return;
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].srcToDoToEdit(ToDoToSrc, newNameToDo, description, expiration, image,  color);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void checkActivity(String email, String board, String todo, String activity, String dataCompletamento) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato
                if (board.equalsIgnoreCase("UNIVERSITY") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].srcTodocheck(todo, activity, dataCompletamento);
                    addHistoryAct(email,board,todo,activity);
                    users.get(i).getBoards()[0].srcToDoifComplete(todo);
                    return;
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].srcTodocheck(todo, activity, dataCompletamento);
                    addHistoryAct(email,board,todo,activity);
                    users.get(i).getBoards()[1].srcToDoifComplete(todo);
                    return;
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].srcTodocheck(todo, activity, dataCompletamento);
                    addHistoryAct(email,board,todo,activity);
                    users.get(i).getBoards()[2].srcToDoifComplete(todo);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void deCheckActivity(String email, String board, String todo, String activity) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato
                if (board.equalsIgnoreCase("UNIVERSITY") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].srcTodoDeCheck(todo, activity);
                    return;
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].srcTodoDeCheck(todo, activity);
                    return;
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].srcTodoDeCheck(todo, activity);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
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


    public void printArchive(String email, String board) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato
                if (board.equalsIgnoreCase("universita") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].getToDoArchiveCompleted().print();
                    return;
                } else if (board.equalsIgnoreCase("lavoro") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].getToDoArchiveCompleted().print();
                    return;
                } else if (board.equalsIgnoreCase("tempo libero") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].getToDoArchiveCompleted().print();
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
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

    public void swapToDo(String email, String board, String todo, int j){
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; //trovato
                if(board.equalsIgnoreCase("universita")&& users.get(i).getBoards()[0]!=null){
                    users.get(i).getBoards()[0].srcTodoSwap(todo,j);
                    return;
                }else if(board.equalsIgnoreCase("lavoro")&& users.get(i).getBoards()[1]!=null){
                    users.get(i).getBoards()[1].srcTodoSwap(todo,j);
                    return;
                }else if(board.equalsIgnoreCase("tempo libero")&& users.get(i).getBoards()[2]!=null){
                    users.get(i).getBoards()[2].srcTodoSwap(todo,j);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void printTodoRange(String email, String board, LocalDate range){ //per parametro range o data di oggi decisa nel main
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato
                if(board.equalsIgnoreCase("UNIVERSITY")&& users.get(i).getBoards()[0]!=null){
                    users.get(i).getBoards()[0].printRange(range);
                    return;
                }else if(board.equalsIgnoreCase("WORK")&& users.get(i).getBoards()[1]!=null){
                    users.get(i).getBoards()[1].printRange(range);
                    return;
                }else if(board.equalsIgnoreCase("FREETIME")&& users.get(i).getBoards()[2]!=null){
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


    public boolean shareToDo(String mailAmministratore, String mainUtenteCondividere, String boardName, String toDoName) {
        // Blocco per evitare autocondivisione
        if (mailAmministratore.equalsIgnoreCase(mainUtenteCondividere)) {
            System.out.println("Errore: non puoi condividere un ToDo con te stesso.");
            return false;
        }

        int AmministratoreFound = 0;
        int bachecaAmmTrovata = 0;
        int utenteTrovato = 0;

        for (int i = 0; i < users.size(); i++) {
            if (mailAmministratore.equals(users.get(i).getEmail())) {
                AmministratoreFound = 1;

                for (int x = 0; x < users.size(); x++) {
                    if (mainUtenteCondividere.equals(users.get(x).getEmail())) {
                        utenteTrovato = 1;

                        int boardIndex = -1;
                        if (boardName.equalsIgnoreCase("UNIVERSITY")) boardIndex = 0;
                        else if (boardName.equalsIgnoreCase("WORK")) boardIndex = 1;
                        else if (boardName.equalsIgnoreCase("FREETIME")) boardIndex = 2;

                        if (boardIndex != -1 && users.get(i).getBoards()[boardIndex] != null) {
                            if (users.get(x).getBoards()[boardIndex] == null) {
                                Board b = new Board(
                                        users.get(i).getBoards()[boardIndex].getType(),
                                        users.get(i).getBoards()[boardIndex].getDescription()
                                );
                                users.get(x).addBoard(b);
                            }

                            int toDoTrovato = 0;
                            for (int y = 0; y < users.get(i).getBoards()[boardIndex].getToDo().size(); y++) {
                                if (toDoName.equalsIgnoreCase(users.get(i).getBoards()[boardIndex].getToDo().get(y).getTitle())) {
                                    ToDo todo = users.get(i).getBoards()[boardIndex].getToDo().get(y);

                                    // 1. Crea una nuova CheckList vuota
                                    CheckList nuovaCheckList = new CheckList();

                                    // 2. Copia le attività dalla checklist originale
                                    for (Activity a : todo.getCheckList().getActivities()) {
                                        // Si assume che Activity abbia un costruttore che accetta nome e stato
                                        Activity copiaAttivita = new Activity(a.getName(), a.getState());
                                        nuovaCheckList.addActivity(copiaAttivita); 
                                    }

                                    // 3. Crea il nuovo ToDo copiato, con checklist e condiviso=true
                                    ToDo copiaToDo = new ToDo(todo.getTitle(), todo.getState(), nuovaCheckList, true);

                                    // 4. Aggiungi il ToDo copiato all’utente destinatario
                                    users.get(x).getBoards()[boardIndex].getToDo().add(copiaToDo);



                                    // Crea sharing per l’amministratore
                                    Sharing sharingAdmin = new Sharing(users.get(i), todo);
                                    sharingAdmin.getMembers().add(users.get(x)); // aggiungi utente condiviso
                                    users.get(i).getSharing().add(sharingAdmin);

                                    // Crea sharing per l’utente condiviso
                                    Sharing sharingUser = new Sharing(users.get(i), todo);
                                    sharingUser.getMembers().add(users.get(x)); // aggiungi utente condiviso
                                    users.get(x).getSharing().add(sharingUser);

                                    toDoTrovato = 1;
                                    break;
                                }
                            }

                            if (toDoTrovato == 0) {
                                System.out.println("To do non trovato dell'amministratore");
                            }

                            bachecaAmmTrovata = 1;
                        }
                    }
                }

                if (bachecaAmmTrovata == 0) {
                    System.out.println("Bacheca amministratore non trovata...: " + boardName);
                }
            }
        }

        if (AmministratoreFound == 0) {
            System.out.println("Amministratore non Loggato...");
        }

        if (utenteTrovato == 0) {
            System.out.println("Utente non esistente...");
            return false;
        }

        return true;
    }

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

                                // Ora rimuovi anche la sharing da lui
                                for (int u = 0; u < membro.getSharing().size(); u++) {
                                    Sharing s = membro.getSharing().get(u);
                                    if (s.getToDo().getTitle().equalsIgnoreCase(toDoName)
                                            && s.getAdministrator().getEmail().equalsIgnoreCase(emailUtente)) {
                                        membro.getSharing().remove(u);
                                        break;
                                    }
                                }

                                // Rimuovi il ToDo condiviso dalla board
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


    public ArrayList<User> getToDoUserShared(String email, String toDo){
        int notFound = 0;
        ArrayList<User> listaUtenti = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato
                for (int x = 0; x < users.get(i).getSharing().size(); x++){
                    if(users.get(i).getSharing().get(x).getToDo().getTitle().equalsIgnoreCase(toDo)){
                        for (int y = 0; y < users.get(i).getSharing().get(x).getMembers().size(); y++){
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

    public String getToAdministratorNick(String email, String toDo){
        int notFound = 0;
        String nickname = "";
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato
                for (int x = 0; x < users.get(i).getSharing().size(); x++){
                    if(users.get(i).getSharing().get(x).getToDo().getTitle().equalsIgnoreCase(toDo)){
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

    public String getToAdministratorMail(String email, String toDo){
        int notFound = 0;
        String mail = "";
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato
                for (int x = 0; x < users.get(i).getSharing().size(); x++){
                    if(users.get(i).getSharing().get(x).getToDo().getTitle().equalsIgnoreCase(toDo)){
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

    public int spostaToDoInBacheca(String email, String nomeToDo, String nomeBachecaInCuiSpostare, String nomeBachecaDiOrigine){
        //return 0 = tutto ok
        //return 1 = todo gia esistente nella bacheca dove vuoi spostare
        //return 2 = toDo condiviso, non puoi spostarlo
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato
                for (int x = 0; x < users.size(); x++) {
                    //trovo bacheca in cui spostare
                    int boardIndex = -1;
                    if (nomeBachecaInCuiSpostare.equalsIgnoreCase("UNIVERSITY")) boardIndex = 0;
                    else if (nomeBachecaInCuiSpostare.equalsIgnoreCase("WORK")) boardIndex = 1;
                    else if (nomeBachecaInCuiSpostare.equalsIgnoreCase("FREETIME")) boardIndex = 2;

                    //trovo bacheca di origine dove presente il toDo
                    int boardIndexOrigine = -1;
                    if (nomeBachecaDiOrigine.equalsIgnoreCase("UNIVERSITY")) boardIndexOrigine = 0;
                    else if (nomeBachecaDiOrigine.equalsIgnoreCase("WORK")) boardIndexOrigine = 1;
                    else if (nomeBachecaDiOrigine.equalsIgnoreCase("FREETIME")) boardIndexOrigine = 2;

                    for(int y = 0; y < users.get(i).getBoards()[boardIndexOrigine].getToDo().size(); y++){
                        if(users.get(i).getBoards()[boardIndexOrigine].getToDo().get(y).getTitle().equalsIgnoreCase(nomeToDo) && users.get(i).getBoards()[boardIndexOrigine].getToDo().get(y).isCondiviso() == false){
                            ToDo todo = users.get(i).getBoards()[boardIndexOrigine].getToDo().get(y);
                            for(int z = 0; z < users.get(i).getBoards()[boardIndex].getToDo().size(); z++){
                                if(users.get(i).getBoards()[boardIndex].getToDo().get(z).getTitle().equalsIgnoreCase(nomeToDo)){
                                    System.out.println("To do con questo nome gia presente nell'altra bacheca");
                                    return 1;
                                }
                            }
                            // 1 Crea una nuova CheckList vuota
                            CheckList nuovaCheckList = new CheckList();

                            // 2 Copia le attività dalla checklist originale
                            for (Activity a : todo.getCheckList().getActivities()) {
                                Activity copiaAttivita = new Activity(a.getName(), a.getState());
                                nuovaCheckList.addActivity(copiaAttivita);
                            }

                            // 3 Crea il nuovo ToDo copiato, con checklist e condiviso=true
                            ToDo copiaToDo = new ToDo(todo.getTitle(), todo.getState(), nuovaCheckList, todo.isCondiviso());

                            // 4 Aggiungi il ToDo copiato all’utente destinatario
                            users.get(i).getBoards()[boardIndex].getToDo().add(copiaToDo);


                        }else{
                            System.out.println("to do condiviso non spostabile");
                            return 2;
                        }
                    }

                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
        return 0;
    }


}