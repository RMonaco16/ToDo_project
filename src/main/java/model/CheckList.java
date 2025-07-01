package model;

import java.util.ArrayList;

/**
 * Rappresenta una checklist composta da una lista di attività (Activity).
 */
public class CheckList {

    private ArrayList<Activity> activities = new ArrayList<>();

    /**
     * Costruttore vuoto della classe CheckList.
     * Inizializza la lista delle attività come vuota.
     */
    public CheckList() {
        //Costruttore
    }

    /**
     * Restituisce la lista delle attività contenute nella checklist.
     *
     * @return una lista di oggetti Activity
     */
    public ArrayList<Activity> getActivities() {
        return activities;
    }

    /**
     * Imposta la lista delle attività per questa checklist.
     *
     * @param activities una lista di oggetti Activity da assegnare
     */
    public void setActivities(ArrayList<Activity> activities) {
        this.activities = activities;
    }
}
