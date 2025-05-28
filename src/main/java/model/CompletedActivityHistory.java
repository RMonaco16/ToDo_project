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

    public ArrayList<Activity> print(){
        ArrayList<Activity> listaAtt = new ArrayList<>();
        for(Activity a :activityHistory){
            System.out.println(a.getName()+" | "+a.getCompletionDate());
            listaAtt.add(a);
        }
        return listaAtt;
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
