package model;

import java.util.Date;

public class Activity {
    private String name;
    private boolean state;
    private Date completionDate;

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

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

}
