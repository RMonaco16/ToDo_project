package model;

import java.time.LocalDate;


public class ToDo {
    private String title;
    private String description;
    //private String colore;
    private int position;
    //private immagine
    //private url
    private LocalDate expiration;
    private boolean state;
    private CheckList checkList;//la  to Do ha una checklist al suo interno

    public ToDo(String title, String description, int position, boolean state, CheckList checkList) {
        this.title = title;
        this.description = description;
        this.position = position;
        this.state = state;
        this.checkList = checkList;
    }

    // getter and setter
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public CheckList getCheckList() {
        return checkList;
    }

    public void setCheckList(CheckList checkList) {
        this.checkList = checkList;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }
}





