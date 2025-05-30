package model;

import java.util.Date;

public class Activity {
    private String name;
    private boolean state;
    private String completionDate;

    public Activity(String name, boolean state){
        this.name = name;
        this.state = state;
    }

    // getter and setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }
}
