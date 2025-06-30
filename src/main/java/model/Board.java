package model;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

public class Board {
    private TypeBoard type;
    private String description;
    private ArrayList<ToDo> toDo = new ArrayList<>();

    public Board(TypeBoard type, String description) {
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

    public boolean boardAddToDo(ToDo todo){
        boolean nuova = true;
        for(int i = 0; i < this.toDo.size(); i++){
            if(this.toDo.get(i).getTitle().equalsIgnoreCase(todo.getTitle())){
                nuova = false;
            }
        }
        if(nuova){
            this.toDo.add(todo);
            System.out.println("ToDo aggiunto correttamente!!");
        }else{
            System.out.println("Un to do con questo titolo all'interno di questa bacheca gia esiste...");
        }
        return nuova; // verso se aggiunta
    }

    // ritorna vero se trova un to-do già esistente con lo stesso nome
    public boolean checkNameToDoAlreadyExisting(String nameToDo){
        for(ToDo t: toDo){
            if (t.getTitle().equalsIgnoreCase(nameToDo))
                return true;
        }
        return false;
    }
}
