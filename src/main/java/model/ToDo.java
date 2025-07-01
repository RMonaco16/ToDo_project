package model;

import java.awt.*;
import java.time.LocalDate;

/**
 * Rappresenta un'attività ToDo con titolo, descrizione, scadenza, stato, checklist,
 * informazioni di condivisione e proprietà grafiche.
 */
public class ToDo {
    private String title;
    private String description;
    private Color color;
    private String image;
    private LocalDate expiration;
    private boolean state;
    private boolean condiviso;
    private CheckList checkList;
    private String ownerEmail;  // Campo per identificare il proprietario/amministratore del ToDo

    /**
     * Costruttore vuoto.
     */
    public ToDo() {}

    /**
     * Costruttore che inizializza i campi principali del ToDo.
     *
     * @param title       il titolo del ToDo
     * @param state       lo stato del ToDo (true se completato)
     * @param checkList   la checklist associata
     * @param condiviso   indica se il ToDo è condiviso
     * @param ownerEmail  email del proprietario/amministratore del ToDo
     */
    public ToDo(String title, boolean state, CheckList checkList, boolean condiviso, String ownerEmail) {
        this.title = title;
        this.state = state;
        this.checkList = checkList;
        this.condiviso = condiviso;
        this.ownerEmail = ownerEmail;
    }

    /**
     * Restituisce il titolo del ToDo.
     *
     * @return il titolo
     */
    public String getTitle() {
        return title;
    }

    /**
     * Imposta il titolo del ToDo.
     *
     * @param title il nuovo titolo
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Restituisce la descrizione del ToDo.
     *
     * @return la descrizione
     */
    public String getDescription() {
        return description;
    }

    /**
     * Imposta la descrizione del ToDo.
     *
     * @param description la nuova descrizione
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Restituisce il colore associato al ToDo.
     *
     * @return il colore
     */
    public Color getColor() {
        return color;
    }

    /**
     * Imposta il colore del ToDo.
     *
     * @param color il nuovo colore
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Restituisce il percorso dell'immagine associata al ToDo.
     *
     * @return il percorso dell'immagine
     */
    public String getImage() {
        return image;
    }

    /**
     * Imposta l'immagine associata al ToDo.
     *
     * @param image il percorso dell'immagine
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Restituisce la data di scadenza del ToDo.
     *
     * @return la data di scadenza
     */
    public LocalDate getExpiration() {
        return expiration;
    }

    /**
     * Imposta la data di scadenza del ToDo.
     *
     * @param expiration la nuova data di scadenza
     */
    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }

    /**
     * Verifica se il ToDo è stato completato.
     *
     * @return true se completato, false altrimenti
     */
    public boolean isState() {
        return state;
    }

    /**
     * Imposta lo stato di completamento del ToDo.
     *
     * @param state true se completato, false altrimenti
     */
    public void setState(boolean state) {
        this.state = state;
    }

    /**
     * Verifica se il ToDo è condiviso.
     *
     * @return true se condiviso, false altrimenti
     */
    public boolean isCondiviso() {
        return condiviso;
    }

    /**
     * Imposta lo stato di condivisione del ToDo.
     *
     * @param condiviso true se condiviso, false altrimenti
     */
    public void setCondiviso(boolean condiviso) {
        this.condiviso = condiviso;
    }

    /**
     * Restituisce la checklist associata al ToDo.
     *
     * @return la checklist
     */
    public CheckList getCheckList() {
        return checkList;
    }

    /**
     * Imposta una nuova checklist per il ToDo.
     *
     * @param checkList la checklist da associare
     */
    public void setCheckList(CheckList checkList) {
        this.checkList = checkList;
    }

    /**
     * Restituisce l'email del proprietario del ToDo.
     *
     * @return l'email del proprietario
     */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /**
     * Imposta l'email del proprietario del ToDo.
     *
     * @param ownerEmail la nuova email del proprietario
     */
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
