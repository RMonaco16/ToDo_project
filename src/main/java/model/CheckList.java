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

    //metodi
    public void addActivity(Activity act){
        activities.add(act);
        System.out.println("Attività aggiunta corretamente.");
    }

    public void rmvActivity(Activity act){
        for(Activity a : activities){
            if (a.getName().equalsIgnoreCase(act.getName())){
                activities.remove(act);
                System.out.println("Attività rimossa corretamente.");
                return;
            }
            System.out.println("Attività non trovata.");
        }
    }
}
