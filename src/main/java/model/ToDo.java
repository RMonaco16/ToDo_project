package model;

import javax.swing.*;
import java.sql.SQLOutput;
import java.time.LocalDate;


public class ToDo {
    private String title;
    private String description;
    private String colore;
    private int position;
    private String image;
    private LocalDate expiration;
    private boolean state;
    private CheckList checkList;//la  to Do ha una checklist al suo interno

    public ToDo(String title, /*String description,*/ boolean state, CheckList checkList) {
        this.title = title;
        this.description = description;
        this.state = state;
        this.checkList = checkList;
    }

    // getter and setter
    public String getColore() {
        return colore;
    }

    public void setColore(String colore) {
        this.colore = colore;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isState() {
        this.checkList = checkList;
        if(!(checkList.getActivities().isEmpty()))
            for(Activity a: checkList.getActivities()){
                if(a.getState()==false);
                return false;
            }
        return true;
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





