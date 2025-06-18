package model;

import java.awt.*;
import java.time.LocalDate;

public class ToDo {
    private String title;
    private String description;
    private Color color;
    private String image;
    private LocalDate expiration;
    private boolean state;
    private boolean condiviso;
    private CheckList checkList;
    private String ownerEmail;  // Nuovo campo per identificare l'amministratore/proprietario

    public ToDo(String title, /*String description,*/ boolean state, CheckList checkList, boolean condiviso, String ownerEmail) {
        this.title = title;
        // this.description = description;  // Se vuoi tenerla, abilita
        this.state = state;
        this.checkList = checkList;
        this.condiviso = condiviso;
        this.ownerEmail = ownerEmail;
    }

    // Getters e setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean isCondiviso() {
        return condiviso;
    }

    public void setCondiviso(boolean condiviso) {
        this.condiviso = condiviso;
    }

    public CheckList getCheckList() {
        return checkList;
    }

    public void setCheckList(CheckList checkList) {
        this.checkList = checkList;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public void checkActivity(String activityName, String dataCompletamento) {
        for (Activity a : getCheckList().getActivities()) {
            if (a.getName().equalsIgnoreCase(activityName)) {
                a.setState(true);
                a.setCompletionDate(dataCompletamento);
                break;
            }
        }
    }

    public void deCheckActivity(String activityName) {
        for (Activity a : getCheckList().getActivities()) {
            if (a.getName().equalsIgnoreCase(activityName)) {
                a.setState(false);
                a.setCompletionDate(null);
                break;
            }
        }
    }

    public void checkIfComplete() {
        boolean allComplete = true;
        for (Activity a : getCheckList().getActivities()) {
            if (!a.getState()) {
                allComplete = false;
                break;
            }
        }
        this.state = allComplete; // aggiorna stato ToDo
    }

}
