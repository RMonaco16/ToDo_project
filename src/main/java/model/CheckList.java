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
        int doppione = 0;
        for(int i = 0; i < activities.size(); i++){
            if(this.activities.get(i).getName().equalsIgnoreCase(act.getName()) ){
                doppione = 1;
            }
        }
        if(doppione != 1){
            activities.add(act);
            System.out.println("Attività aggiunta corretamente.");
        }else{
            System.out.println("Questa attivita esiste gia...");
        }
    }

    public void rmvActivity(String nameActivity){
        for(Activity a : activities){
            if (a.getName().equalsIgnoreCase(nameActivity)){
                activities.remove(a);
                System.out.println("Attività rimossa corretamente.");
                return;
            }
            System.out.println("Attività non trovata.");
        }
    }

    public void checkAct(String nmAct, String dataCompletamento){
        for(Activity a : activities){
            if(nmAct.equalsIgnoreCase(a.getName())){
                a.setState(true);
                a.setCompletionDate(dataCompletamento);
                return;
            }
        }
    }

    public void printActs(){
        for(Activity a: activities){
            System.out.print(a.getName()+" | "+a.getState());
        }
    }

}
