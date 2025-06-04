package model;

import javax.swing.*;
import java.sql.SQLOutput;
import java.time.LocalDate;


public class ToDo {
    private String title;
    private String description;
    private String color;
    private int position;
    private String image;
    private LocalDate expiration;
    private boolean state;
    private CheckList checkList;//la  to Do ha una checklist al suo interno

    public ToDo(String title, /*String description,*/ boolean state, CheckList checkList) {
        this.title = title;
        this.description = description;
        this.state = false;
        this.checkList = checkList;
    }

    // getter and setter
    public String getColor() {
        return color;
    }

    public void setColor(String colore) {
        this.color = colore;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isState() {
        return state;
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





