package model;

import java.util.ArrayList;

public class CompletedActivityHistory {
    private ArrayList<Activity> activityHistory = new ArrayList<>();

    public void  AddActivityHistory(Activity a){
        activityHistory.add(a);
        System.out.println("Attivita aggiunta a cronologia");
    }



    public ArrayList<Activity> getActivityHistory() {
        return activityHistory;
    }

    public void setActivityHistory(ArrayList<Activity> activityHistory) {
        this.activityHistory = activityHistory;
    }

}
