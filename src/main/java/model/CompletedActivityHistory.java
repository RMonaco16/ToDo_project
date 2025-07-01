package model;

import java.util.ArrayList;

/**
 * Rappresenta la cronologia delle attività completate.
 * Contiene una lista di oggetti {@link Activity} che sono stati completati.
 */
public class CompletedActivityHistory {

    private ArrayList<Activity> activityHistory = new ArrayList<>();

    /**
     * Aggiunge un'attività alla cronologia delle attività completate.
     *
     * @param a l'attività completata da aggiungere alla cronologia
     */
    public void AddActivityHistory(Activity a){
        activityHistory.add(a);
        System.out.println("Attività aggiunta a cronologia");
    }

    /**
     * Restituisce la lista della cronologia delle attività completate.
     *
     * @return una lista di oggetti Activity completati
     */
    public ArrayList<Activity> getActivityHistory() {
        return activityHistory;
    }

    /**
     * Imposta una nuova lista di attività completate nella cronologia.
     *
     * @param activityHistory la lista da assegnare alla cronologia
     */
    public void setActivityHistory(ArrayList<Activity> activityHistory) {
        this.activityHistory = activityHistory;
    }
}
