package model;

import java.util.ArrayList;

/**
 * Rappresenta la condivisione di un ToDo tra un amministratore e un gruppo di membri.
 */
public class Sharing {

    private User administrator;
    private ToDo toDo;
    private ArrayList<User> members = new ArrayList<>();

    /**
     * Costruttore che inizializza una condivisione con un amministratore e un ToDo specifico.
     *
     * @param administrator l'utente che amministra la condivisione
     * @param todo il ToDo oggetto della condivisione
     */
    public Sharing(User administrator, ToDo todo){
        this.administrator = administrator;
        this.toDo = todo;
    }

    /**
     * Restituisce l'amministratore della condivisione.
     *
     * @return l'utente amministratore
     */
    public User getAdministrator() {
        return administrator;
    }

    /**
     * Imposta l'amministratore della condivisione.
     *
     * @param administrator il nuovo amministratore da assegnare
     */
    public void setAdministrator(User administrator) {
        this.administrator = administrator;
    }

    /**
     * Restituisce il ToDo condiviso.
     *
     * @return il ToDo condiviso
     */
    public ToDo getToDo() {
        return toDo;
    }

    /**
     * Imposta il ToDo da condividere.
     *
     * @param toDo il nuovo ToDo da associare alla condivisione
     */
    public void setToDo(ToDo toDo) {
        this.toDo = toDo;
    }

    /**
     * Restituisce la lista degli utenti membri della condivisione.
     *
     * @return una lista di utenti membri
     */
    public ArrayList<User> getMembers() {
        return members;
    }

    /**
     * Imposta la lista degli utenti membri della condivisione.
     *
     * @param members la lista di utenti da assegnare come membri
     */
    public void setMembers(ArrayList<User> members) {
        this.members = members;
    }
}
