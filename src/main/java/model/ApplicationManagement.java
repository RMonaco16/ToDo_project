package model;

import java.util.ArrayList;

public class ApplicationManagement {

    private ArrayList<User> users = new ArrayList<>();

    public ApplicationManagement(){

    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public void addUser(User u){
        users.add(u);
        System.out.println("Utente Aggiunto Correttamente!!");
    }

    public void login(User u){
        int notFound = 0;
        for(int i = 0; i < users.size(); i++){
            if(u.getEmail().equalsIgnoreCase(users.get(i).getEmail()) && u.getPassword().equalsIgnoreCase(users.get(i).getPassword()) && u.getNickname().equalsIgnoreCase(users.get(i).getNickname())){
                System.out.println("Login effettuato con Successo!!");
                notFound = 1;
            }
        }
        if(notFound == 0){
            System.out.println("Utente non trovato...");
        }
    }

    public void addBoard(){

    }




}
