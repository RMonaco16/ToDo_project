package model;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Sharing {

    private User administrator ;
    private ToDo toDo;
    private ArrayList<User> members = new ArrayList<>();

    public Sharing(User administrator, ToDo todo){
        this.administrator = administrator;
        this.toDo = todo;
    }

    public User getAdministrator() {
        return administrator;
    }

    public void setAdministrator(User administrator) {
        this.administrator = administrator;
    }

    public ToDo getToDo() {
        return toDo;
    }

    public void setToDo(ToDo toDo) {
        this.toDo = toDo;
    }

    public ArrayList<User> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<User> members) {
        this.members = members;
    }
}
