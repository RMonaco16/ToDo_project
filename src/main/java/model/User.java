package model;

import java.util.ArrayList;

public class User {
    private String nickname;
    private String email;
    private String password;
    private Board[] boards = new Board[3];//un utente puo avere fino a 3 bacheche(0= UNIVERSITY, 1= WORK, 2= FREETIME)
    private CompletedActivityHistory activityHistory;
    private ArrayList<Sharing> sharing = new ArrayList<>();

    public User(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    //getter and setter
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Board[] getBoards() {
        return boards;
    }

    public void setBoards(Board[] boards) {
        this.boards = boards;
    }

    public CompletedActivityHistory getActivityHistory() {
        return activityHistory;
    }

    public void setActivityHistory(CompletedActivityHistory activityHistory) {
        this.activityHistory = activityHistory;
    }

    public ArrayList<Sharing> getSharing() {
        return sharing;
    }

    public void setSharing(ArrayList<Sharing> sharing) {
        this.sharing = sharing;
    }

    public void addBoard(Board board) {
        if (this.getBoards()[0] == null && board.getType() == TypeBoard.UNIVERSITY) {
            this.getBoards()[0] = board;
            System.out.println("Bacheca Universita creata correttamente!!");
            return;
        } else if (this.getBoards()[1] == null && board.getType() == TypeBoard.WORK) {
            this.getBoards()[1] = board;
            System.out.println("Bacheca Lavoro creata correttamente!!");
            return;
        } else if (this.getBoards()[2] == null && board.getType() == TypeBoard.FREETIME) {
            this.getBoards()[2] = board;
            System.out.println("Bacheca Tempo Libero creata correttamente!!");
            return;
        } else {
            System.out.println("Esiste gia una Bacheca di tipo: " + board.getType());
        }
    }

    public void deleteBoard(String type) {
        if (type.equalsIgnoreCase("universita") && this.getBoards()[0] != null) {
            this.getBoards()[0] = null;
            System.out.println("Bacheca Universita Eliminata!!");
            return;
        } else if (type.equalsIgnoreCase("lavoro") && this.getBoards()[1] != null) {
            this.getBoards()[1] = null;
            System.out.println("Bacheca Lavoro Eliminata!!");
            return;
        } else if (type.equalsIgnoreCase("tempo libero") && this.getBoards()[2] != null) {
            this.getBoards()[2] = null;
            System.out.println("Bacheca Tempo Libero Eliminata!!");
            return;
        }
    }

    public void searchBoardAddToDo(String tipoEnum, ToDo toDo) {
        if (tipoEnum.equalsIgnoreCase("universita") && this.getBoards()[0] != null) {
            this.getBoards()[0].boardAddToDo(toDo);
            return;
        } else if (tipoEnum.equalsIgnoreCase("lavoro") && this.getBoards()[1] != null) {
            this.getBoards()[1].boardAddToDo(toDo);
            return;
        } else if (tipoEnum.equalsIgnoreCase("tempo libero") && this.getBoards()[2] != null) {
            this.getBoards()[2].boardAddToDo(toDo);
            return;
        }else{
            System.out.println("Bacheca non trovata...");
        }
    }







}