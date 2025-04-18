package model;

public class User {
    private String nickname;
    private String email;
    private String password;
    private Board[] boards = new Board[3];//un utente puo avere fino a 3 bacheche(0= UNIVERSITY, 1= WORK, 2= FREETIME)


    public User (String nickname, String email, String password){
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

}
