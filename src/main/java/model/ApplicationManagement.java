package model;

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
        users.add(u);
        System.out.println("Utente Aggiunto Correttamente!!");
    }

    public void login(User u) {
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                System.out.println("Login effettuato con Successo!!");
                notFound = 1;
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non trovato...");
        }
    }

    public void addBoard(User u, Board b) {//Aggiunta di una Bacheca, passaggio per: (Managment --> User)
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1;
                u.addBoard(b);
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void deleteBoard(User u, String type) {//Eliminazione di una Bacheca, passaggio per: (Managment --> User)
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1;
               u.deleteBoard(type);
            }

        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void addToDoInBoard(User u, String tipoEnum, ToDo toDo) {//Aggiunta di un to_do all'interno di una specifica Bahceca con i ralativi controlli ( )
        int notFound = 0;
        int alreadyExist = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1;
                u.searchBoardAddToDo(tipoEnum,toDo);
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void addActivity(User u, String titleToDo, String board, Activity activity){//funzione per aggiungere attivita ad un to Do, fatta separando le attivita tra (Managment --> Board --> checklist)
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1;
                if(board.equalsIgnoreCase("Universita") && u.getBoards()[0] != null){
                    u.getBoards()[0].searchToDoAddActivity(titleToDo,activity);
                }else if(board.equalsIgnoreCase("lavoro") && u.getBoards()[1] != null){
                    u.getBoards()[1].searchToDoAddActivity(titleToDo,activity);
                }else if(board.equalsIgnoreCase("tempo libero") && u.getBoards()[2] != null){
                    u.getBoards()[2].searchToDoAddActivity(titleToDo,activity);
                }
            }

        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void removeActivity(User u, String titleToDo, String board, String nameActivity){//funzione per rimuovere attivita da un to Do, creata separando le attivita tra (Managment --> Board --> checklist)
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1;
                if(board.equalsIgnoreCase("Universita") && u.getBoards()[0] != null){
                    u.getBoards()[0].searchToDoRemoveActivity(titleToDo,nameActivity);
                }else if(board.equalsIgnoreCase("lavoro") && u.getBoards()[1] != null){
                    u.getBoards()[1].searchToDoRemoveActivity(titleToDo,nameActivity);
                }else if(board.equalsIgnoreCase("tempo libero") && u.getBoards()[2] != null){
                    u.getBoards()[2].searchToDoRemoveActivity(titleToDo,nameActivity);
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void printHistory(User u){
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1; //trovato
                u.getActivityHistory().print();
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void checkActivity(User u,String board, String todo, String activity){
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1; //trovato
                if(board.equalsIgnoreCase("universita") && u.getBoards()[0]!=null){
                    u.getBoards()[0].srcTodocheck(todo,activity);
                    return;
                }else if(board.equalsIgnoreCase("lavoro") && u.getBoards()[1]!=null){
                    u.getBoards()[1].srcTodocheck(todo,activity);
                    return;
                }else if(board.equalsIgnoreCase("tempo libero") && u.getBoards()[2]!=null){
                    u.getBoards()[2].srcTodocheck(todo,activity);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void printArchive(User u,String board){
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1; //trovato
                if(board.equalsIgnoreCase("universita")&& u.getBoards()[0]!=null){
                    u.getBoards()[0].getToDoArchiveCompleted().print();
                    return;
                }else if(board.equalsIgnoreCase("lavoro")&& u.getBoards()[1]!=null){
                    u.getBoards()[1].getToDoArchiveCompleted().print();
                    return;
                }else if(board.equalsIgnoreCase("tempo libero")&& u.getBoards()[2]!=null){
                    u.getBoards()[2].getToDoArchiveCompleted().print();
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void rmvHistoryAct(User u,String nmAct){
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1; //trovato
                u.getActivityHistory().rmvAct(nmAct);
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void dltHistory(User u,String nmAct){
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1; //trovato
                u.getActivityHistory().dltActs();
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void printTodo(User u,String board){
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1; //trovato
                if(board.equalsIgnoreCase("universita")&& u.getBoards()[0]!=null){
                    u.getBoards()[0].print();
                    return;
                }else if(board.equalsIgnoreCase("lavoro")&& u.getBoards()[1]!=null){
                    u.getBoards()[1].print();
                    return;
                }else if(board.equalsIgnoreCase("tempo libero")&& u.getBoards()[2]!=null){
                    u.getBoards()[2].print();
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

    public void printActs(User u, String board, String todo){
        int notFound = 0;
        for (int i = 0; i < users.size(); i++) {
            if (u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())) {
                notFound = 1; //trovato
                if(board.equalsIgnoreCase("universita")&& u.getBoards()[0]!=null){
                    u.getBoards()[0].srcTodoPrint(todo);
                    return;
                }else if(board.equalsIgnoreCase("lavoro")&& u.getBoards()[1]!=null){
                    u.getBoards()[1].srcTodoPrint(todo);
                    return;
                }else if(board.equalsIgnoreCase("tempo libero")&& u.getBoards()[2]!=null){
                    u.getBoards()[2].srcTodoPrint(todo);
                    return;
                }
            }
        }
        if (notFound == 0) {
            System.out.println("Utente non Loggato...");
        }
    }

}