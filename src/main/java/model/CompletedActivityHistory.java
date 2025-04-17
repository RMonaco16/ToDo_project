package model;

import java.util.ArrayList;

public class CompletedActivityHistory {
    private ArrayList<Activity> activityHistory = new ArrayList<>();

    public CompletedActivityHistory(){

    }

    public ArrayList<Activity> getActivityHistory() {
        return activityHistory;
    }

    public void setActivityHistory(ArrayList<Activity> activityHistory) {
        this.activityHistory = activityHistory;
    }
}
