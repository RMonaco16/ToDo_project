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

    public void addBoard(String email, Board b) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1;
                users.get(i).addBoard(b);
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
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
                if (board.equalsIgnoreCase("Universita") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].searchToDoAddActivity(titleToDo, activity);
                } else if (board.equalsIgnoreCase("lavoro") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].searchToDoAddActivity(titleToDo, activity);
                } else if (board.equalsIgnoreCase("tempo libero") && users.get(i).getBoards()[2] != null) {
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

    public void printHistory(String email) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato
                users.get(i).getActivityHistory().print();
                return;
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void checkActivity(String email, String board, String todo, String activity) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // trovato
                if (board.equalsIgnoreCase("universita") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].srcTodocheck(todo, activity);
                    return;
                } else if (board.equalsIgnoreCase("lavoro") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].srcTodocheck(todo, activity);
                    return;
                } else if (board.equalsIgnoreCase("tempo libero") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].srcTodocheck(todo, activity);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
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

    public void printTodo(String email, String board) {
        int notFound = 0;

        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato

                if (board.equalsIgnoreCase("universita") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].print();
                    return;
                } else if (board.equalsIgnoreCase("lavoro") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].print();
                    return;
                } else if (board.equalsIgnoreCase("tempo libero") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].print();
                    return;
                } else {
                    System.out.println("Bacheca non trovata o vuota.");
                    return;
                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
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
                if(board.equalsIgnoreCase("universita")&& users.get(i).getBoards()[0]!=null){
                    users.get(i).getBoards()[0].printRange(range);
                    return;
                }else if(board.equalsIgnoreCase("lavoro")&& users.get(i).getBoards()[1]!=null){
                    users.get(i).getBoards()[1].printRange(range);
                    return;
                }else if(board.equalsIgnoreCase("tempo libero")&& users.get(i).getBoards()[2]!=null){
                    users.get(i).getBoards()[2].printRange(range);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
    }


    public void printActs(String email, String board, String todo) {
        int notFound = 0;

        for (int i = 0; i < users.size(); i++) {
            if (email.equals(users.get(i).getEmail())) {
                notFound = 1; // utente trovato

                if (board.equalsIgnoreCase("universita") && users.get(i).getBoards()[0] != null) {
                    users.get(i).getBoards()[0].srcTodoPrint(todo);
                    return;
                } else if (board.equalsIgnoreCase("lavoro") && users.get(i).getBoards()[1] != null) {
                    users.get(i).getBoards()[1].srcTodoPrint(todo);
                    return;
                } else if (board.equalsIgnoreCase("tempo libero") && users.get(i).getBoards()[2] != null) {
                    users.get(i).getBoards()[2].srcTodoPrint(todo);
                    return;
                } else {
                    System.out.println("Bacheca non trovata o vuota.");
                    return;
                }
            }
        }

        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
    }


    public void shareToDo(String mailAmministratore, String mainUtenteCondividere, String boardName, String toDoName ){
        int AmministratoreFound = 0;
        int bachecaAmmTrovata = 0;
        int utenteTrovato = 0;
        for (int i = 0; i < users.size(); i++) {
            if (mailAmministratore.equals(users.get(i).getEmail()) ) {
                AmministratoreFound = 1;
                for(int x = 0; x < users.size(); x++) {
                    if (mainUtenteCondividere.equals(users.get(i).getEmail()) ) {
                        utenteTrovato = 1;
                        if(boardName.equalsIgnoreCase("Universita") && users.get(i).getBoards()[0] != null){
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
                        }else if(boardName.equalsIgnoreCase("lavoro") && users.get(i).getBoards()[1] != null){
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
                        }else if(boardName.equalsIgnoreCase("tempo libero") && users.get(i).getBoards()[2] != null){
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
                    System.out.println("Bacheca amministratore non trovata...");

                }
            }
        }
        if (AmministratoreFound == 0) {
            System.out.println("Utente non Loggato...");
        }
        if (utenteTrovato == 0) {
            System.out.println("Utente non Loggato...");
        }

    }


}