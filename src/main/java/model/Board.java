package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Rappresenta una bacheca contenente una lista di attività (ToDo),
 * un tipo e una descrizione.
 */
public class Board {
    private TypeBoard type;
    private String description;
    private ArrayList<ToDo> toDo = new ArrayList<>();

    /**
     * Costruttore della classe Board che inizializza tipo e descrizione.
     *
     * @param type        il tipo della bacheca
     * @param description la descrizione della bacheca
     */
    public Board(TypeBoard type, String description) {
        this.type = type;
        this.description = description;
    }

    /**
     * Restituisce il tipo della bacheca.
     *
     * @return il tipo della bacheca
     */
    public TypeBoard getType() {
        return type;
    }

    /**
     * Imposta il tipo della bacheca.
     *
     * @param type il nuovo tipo da assegnare alla bacheca
     */
    public void setType(TypeBoard type) {
        this.type = type;
    }

    /**
     * Restituisce la descrizione della bacheca.
     *
     * @return la descrizione
     */
    public String getDescription() {
        return description;
    }

    /**
     * Imposta la descrizione della bacheca.
     *
     * @param description la nuova descrizione da assegnare
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Restituisce la lista di ToDo associati a questa bacheca.
     *
     * @return una lista di oggetti ToDo
     */
    public ArrayList<ToDo> getToDo() {
        return toDo;
    }

    /**
     * Imposta una nuova lista di ToDo per la bacheca.
     *
     * @param toDo la lista di ToDo da assegnare
     */
    public void setToDo(ArrayList<ToDo> toDo) {
        this.toDo = toDo;
    }

    /**
     * Aggiunge un nuovo ToDo alla bacheca se non esiste già un'attività con lo stesso titolo.
     *
     * @param todo il ToDo da aggiungere
     * @return true se il ToDo è stato aggiunto con successo, false se esiste già
     */
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
        } else {
            System.out.println("Un to do con questo titolo all'interno di questa bacheca già esiste...");
        }
        return nuova;
    }

    /**
     * Verifica se esiste già un ToDo con il titolo specificato nella bacheca.
     *
     * @param nameToDo il titolo da verificare
     * @return true se esiste già un ToDo con quel nome, false altrimenti
     */
    public boolean checkNameToDoAlreadyExisting(String nameToDo){
        for(ToDo t: toDo){
            if (t.getTitle().equalsIgnoreCase(nameToDo))
                return true;
        }
        return false;
    }
}
