package model;

import java.util.Date;

/**
 * Rappresenta una singola attività con un nome, uno stato (completata o meno)
 * e una data di completamento opzionale.
 */
public class Activity {
    private String name;
    private boolean state;
    private String completionDate;

    /**
     * Costruttore che inizializza un'attività con nome e stato.
     *
     * @param name  il nome dell'attività
     * @param state lo stato dell'attività (true se completata, false altrimenti)
     */
    public Activity(String name, boolean state){
        this.name = name;
        this.state = state;
    }

    /**
     * Costruttore vuoto per la creazione di un'attività senza inizializzazione.
     */
    public Activity(){}

    /**
     * Restituisce il nome dell'attività.
     *
     * @return il nome dell'attività
     */
    public String getName() {
        return name;
    }

    /**
     * Imposta il nome dell'attività.
     *
     * @param name il nuovo nome dell'attività
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Restituisce lo stato dell'attività.
     *
     * @return true se l'attività è completata, false altrimenti
     */
    public boolean getState() {
        return state;
    }

    /**
     * Imposta lo stato dell'attività.
     *
     * @param state true se l'attività è completata, false altrimenti
     */
    public void setState(boolean state) {
        this.state = state;
    }

    /**
     * Restituisce la data di completamento dell'attività, se presente.
     *
     * @return la data di completamento come stringa (può essere null)
     */
    public String getCompletionDate() {
        return completionDate;
    }

    /**
     * Imposta la data di completamento dell'attività.
     *
     * @param completionDate la data di completamento come stringa
     */
    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }
}
