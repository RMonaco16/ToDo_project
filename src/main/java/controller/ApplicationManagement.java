package controller;

import model.*;

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

    public void addUser(User u) {
        if(u.getNickname().equalsIgnoreCase("") || u.getEmail().equalsIgnoreCase("")
                || u.getEmail().equalsIgnoreCase("")){
            System.out.println("Utente non creato");
        }else{
            users.add(u);
            System.out.println("Utente Aggiunto Correttamente!!");
        }
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
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].searchToDoAddActivity(titleToDo, activity);
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].searchToDoAddActivity(titleToDo, activity);
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
                if (board.equalsIgnoreCase("Universita") && u.getBoards()[0] != null) {
                    u.getBoards()[0].searchToDoRemoveActivity(titleToDo, nameActivity);
                } else if (board.equalsIgnoreCase("lavoro") && u.getBoards()[1] != null) {
                    u.getBoards()[1].searchToDoRemoveActivity(titleToDo, nameActivity);
                } else if (board.equalsIgnoreCase("tempo libero") && u.getBoards()[2] != null) {
                    u.getBoards()[2].searchToDoRemoveActivity(titleToDo, nameActivity);
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
                    return;
                } else if (board.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].srcTodocheck(todo, activity, dataCompletamento);
                    addHistoryAct(email,board,todo,activity);
                    return;
                } else if (board.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].srcTodocheck(todo, activity, dataCompletamento);
                    addHistoryAct(email,board,todo,activity);
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


    public boolean shareToDo(String mailAmministratore, String mainUtenteCondividere, String boardName, String toDoName ){
        int AmministratoreFound = 0;
        int bachecaAmmTrovata = 0;
        int utenteTrovato = 0;
        for (int i = 0; i < users.size(); i++) {
            if (mailAmministratore.equals(users.get(i).getEmail()) ) {
                AmministratoreFound = 1;
                for(int x = 0; x < users.size(); x++) {
                    if (mainUtenteCondividere.equals(users.get(x).getEmail()) ) {
                        utenteTrovato = 1;
                        if(boardName.equalsIgnoreCase("UNIVERSITY") && users.get(i).getBoards()[0] != null){
                            if(users.get(x).getBoards()[0] == null){
                                Board b = new Board(users.get(i).getBoards()[0].getType(), users.get(i).getBoards()[0].getDescription());
                                users.get(x).addBoard(b);
                            }
                            int toDoTrovato = 0;
                            for(int y = 0; y < users.get(i).getBoards()[0].getToDo().size(); y++){
                                if(toDoName.equalsIgnoreCase(users.get(i).getBoards()[0].getToDo().get(y).getTitle())){
                                    ToDo todo = users.get(i).getBoards()[0].getToDo().get(y);
                                    users.get(x).getBoards()[0].getToDo().add(todo);
                                    Sharing s = new Sharing(users.get(i),todo);
                                    users.get(i).getSharing().add(s);
                                    toDoTrovato = 1;
                                    break;
                                }
                            }
                            if(toDoTrovato == 0){
                                System.out.println("To do non trovato dell'amministratore");
                            }
                            bachecaAmmTrovata = 1;
                        }else if(boardName.equalsIgnoreCase("WORK") && users.get(i).getBoards()[1] != null){
                            if(users.get(x).getBoards()[1] == null){
                                Board b = new Board(users.get(i).getBoards()[0].getType(), users.get(i).getBoards()[1].getDescription());
                                users.get(x).addBoard(b);
                            }
                            int toDoTrovato = 0;
                            for(int y = 0; y < users.get(i).getBoards()[1].getToDo().size(); y++){
                                if(toDoName.equalsIgnoreCase(users.get(i).getBoards()[1].getToDo().get(y).getTitle())){
                                    ToDo todo = users.get(i).getBoards()[1].getToDo().get(y);
                                    users.get(x).getBoards()[1].getToDo().add(todo);
                                    Sharing s = new Sharing(users.get(i),todo);
                                    users.get(i).getSharing().add(s);
                                    toDoTrovato = 1;
                                    break;
                                }
                            }
                            if(toDoTrovato == 0){
                                System.out.println("To do non trovato dell'amministratore");
                            }
                            bachecaAmmTrovata = 1;
                        }else if(boardName.equalsIgnoreCase("FREETIME") && users.get(i).getBoards()[2] != null){
                            if(users.get(x).getBoards()[2] == null){
                                Board b = new Board(users.get(i).getBoards()[2].getType(), users.get(i).getBoards()[2].getDescription());
                                users.get(x).addBoard(b);
                            }
                            int toDoTrovato = 0;
                            for(int y = 0; y < users.get(i).getBoards()[2].getToDo().size(); y++){
                                if(toDoName.equalsIgnoreCase(users.get(i).getBoards()[2].getToDo().get(y).getTitle())){
                                    ToDo todo = users.get(i).getBoards()[2].getToDo().get(y);
                                    users.get(x).getBoards()[2].getToDo().add(todo);
                                    Sharing s = new Sharing(users.get(i),todo);
                                    users.get(i).getSharing().add(s);
                                    toDoTrovato = 1;
                                    break;
                                }
                            }
                            if(toDoTrovato == 0){
                                System.out.println("To do non trovato dell'amministratore");
                            }
                            bachecaAmmTrovata = 1;
                        }
                    }
                }
                if(bachecaAmmTrovata == 0){
                    System.out.println("Bacheca amministratore non trovata...:"+ boardName);

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


}