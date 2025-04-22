package model;

import java.util.ArrayList;

public class ToDoArchiveCompleted {
    private ArrayList<ToDo> toDoArchive = new ArrayList<>();

    public ToDoArchiveCompleted(){

    }

    public ArrayList<ToDo> getToDoArchive() {
        return toDoArchive;
    }

    public void setToDoArchive(ArrayList<ToDo> toDoArchive) {
        this.toDoArchive = toDoArchive;
    }

    public void print(){
        for(ToDo a :toDoArchive){
            System.out.println(a.getTitle());
        }
    }
}
