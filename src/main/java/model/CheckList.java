package model;

import java.util.ArrayList;

public class CheckList {

    private ArrayList<Activity> activities = new ArrayList<>();

    public CheckList(){

    }

    public ArrayList<Activity> getActivities() {
        return activities;
    }

    public void setActivities(ArrayList<Activity> activities) {
        this.activities = activities;
    }
}
