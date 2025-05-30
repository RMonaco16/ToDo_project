package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

public class Board {
    private TypeBoard type;
    private String description;
    private ArrayList<ToDo> toDo = new ArrayList<>();
    private ToDoArchiveCompleted toDoArchiveCompleted;

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

    public ToDoArchiveCompleted getToDoArchiveCompleted() {
        return toDoArchiveCompleted;
    }

    public void setToDoArchiveCompleted(ToDoArchiveCompleted toDoArchiveCompleted) {
        this.toDoArchiveCompleted = toDoArchiveCompleted;
    }

    public void searchToDoAddActivity(String titleToDo, Activity a){
        int trovato = 0;
        for(int i = 0; i < this.toDo.size(); i++){
            if(titleToDo.equalsIgnoreCase(this.toDo.get(i).getTitle())){
                trovato = 1;
                this.toDo.get(i).getCheckList().addActivity(a);
            }
        }
        if(trovato == 0){
            System.out.println("ToDo non trovato..");
        }
    }

    public void searchToDoRemoveActivity(String titleToDo, String nameActivity){
        int trovato = 0;
        for(int i = 0; i < this.toDo.size(); i++){
            if(titleToDo.equalsIgnoreCase(this.toDo.get(i).getTitle())){
                trovato = 1;
                this.toDo.get(i).getCheckList().rmvActivity(nameActivity);
            }
        }
        if(trovato == 0){
            System.out.println("ToDo non trovato..");
        }
    }

    public void boardAddToDo(ToDo todo){
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
    }

    public void srcTodocheck(String nmTodo,String nmAct, String dataCompletamento){
        for(ToDo t : toDo){
            if(nmTodo.equalsIgnoreCase(t.getTitle())){
                t.getCheckList().checkAct(nmAct, dataCompletamento);
                return;
            }
        }
    }

    public void srcTodoDeCheck(String nmTodo,String nmAct){
        for(ToDo t : toDo){
            if(nmTodo.equalsIgnoreCase(t.getTitle())){
                t.getCheckList().deCheckAct(nmAct);
                return;
            }
        }
    }

    public ArrayList<ToDo> print(){
        ArrayList<ToDo> todoList = new ArrayList<>();
        for(ToDo t : toDo){
            todoList.add(t);
        }
        return todoList;
    }

    public ArrayList<Activity> srcTodoPrint(String nmTodo){
        ArrayList<Activity> activities = new ArrayList<>();
        for(ToDo t : toDo){
            if(nmTodo.equalsIgnoreCase(t.getTitle())){
                return t.getCheckList().getActivities();
            }
        }
        return activities;
    }

    public void srcTodoSwap(String nmTodo,int i){
        for(ToDo t: toDo){
            if(t.getTitle().equalsIgnoreCase(nmTodo)){
                Collections.swap(toDo,toDo.indexOf(t.getTitle()), i);
            }
        }
    }

    public void printRange(LocalDate range){
        for(ToDo t: toDo){
            //long giorniRimanenti = ChronoUnit.DAYS.between(oggi, dataScadenza);
            if(t.getExpiration().compareTo(range)>=0){
                System.out.println(t.getTitle()+" | ");//+ t.getDescription());
            }
        }
    }
}
