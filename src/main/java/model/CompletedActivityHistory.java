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

    public void print(){
        for(Activity a :activityHistory){
            System.out.println(a.getName()+" | "+a.getCompletionDate());
        }
    }

    public void rmvAct(String nmAct){
        for(Activity a: activityHistory){
            if(a.getName().equalsIgnoreCase(nmAct)){
                activityHistory.remove(a);
                break;
            }
        }
    }

    public void dltActs(){
        activityHistory.clear();
    }
}
