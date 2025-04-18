package model;

import java.util.ArrayList;

public class Board {
    private TypeBoard type;
    private String description;
    private ArrayList<ToDo> toDo = new ArrayList<>();

    public Board (TypeBoard type,String description){
        this.type = type;
        this.description = description;
    }

    //getter and setter
    public TypeBoard getType() {
        return type;
    }

    public void setType(TypeBoard type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<ToDo> getToDo() {
        return toDo;
    }

    public void setToDo(ArrayList<ToDo> toDo) {
        this.toDo = toDo;
    }
}
