package model;

import java.time.LocalDate;


public class ToDo {
    private String title;
    private String description;
    private String color;
    private int position;
    private String image;
    //private url
    private LocalDate expiration;
    private boolean state;
    boolean condiviso;
    private CheckList checkList;//la  to Do ha una checklist al suo interno

    public ToDo(String title, /*String description,*/ boolean state, CheckList checkList, boolean condivione) {
        this.title = title;
        //this.description = description;
        this.state = state;
        this.checkList = checkList;
        this.condiviso = condivione;
    }

    // getter and setter


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

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

    public boolean isState() {
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

    public boolean isCondiviso() {
        return condiviso;
    }

    public void setCondiviso(boolean condiviso) {
        this.condiviso = condiviso;
    }
}





