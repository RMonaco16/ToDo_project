package org.example;

import controller.ApplicationManagement;
import model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        LocalDate l = LocalDate.now();//prende la data odierna


        Scanner scann = new Scanner(System.in);

       ApplicationManagement management = new ApplicationManagement();

       int scelta1 = 0;
       int scelta2 = 0;

       boolean loggato = false;

       do{
           System.out.println("----------------------------------");
           System.out.println("1) Crea Utente");
           System.out.println("2) Login Utente");
           System.out.println("0) Esci");
           System.out.println("----------------------------------");

           scelta1 = scann.nextInt();
           scann.nextLine();
           if(scelta1 == 1){
               System.out.println("Inserire NickName");
               String nickname = scann.nextLine();
               System.out.println("Inserire Email");
               String email = scann.nextLine();
               System.out.println("Inserire Password");
               String password = scann.nextLine();
               User u = new User(nickname,email,password);
               management.addUser(u);
           }else if(scelta1 == 2){
               System.out.println("Inserire Email");
               String email = scann.nextLine();
               System.out.println("Inserire Password");
               String password = scann.nextLine();
               if(management.login(email,password)){
                   do{

                       System.out.println("------- MENU APPLICAZIONE TO-DO -------");
                       System.out.println("1) Crea Bacheca");
                       System.out.println("2) Elimina Bacheca ");
                       System.out.println("3) Crea ToDo in una Bacheca ");
                       System.out.println("4) Inserisci un'Attività in un ToDo di una Bacheca");
                       System.out.println("5) Rimuovi un'Attività da un ToDo di una bacheca");
                       System.out.println("6) Condividi un ToDo con un Utente");
                       System.out.println("7) Spunta un'Attività Svolta in un ToDo di una bacheca");
                       System.out.println("8) Stampa tutti i ToDo di una Bacheca");
                       System.out.println("9) Stampa Attività di un ToDo di una bacheca");
                       System.out.println("10) Stampa Cronologia Attività Svolte di un Utente");
                       System.out.println("11) Elimina un'Attività dalla Cronologia");
                       System.out.println("12) Elimina tutte le Attività dalla Cronologia");
                       System.out.println("13) Stampa Archivio dei ToDo Svolti");
                       System.out.println("14) Scegli posizione toDo in una bachehca");
                       System.out.println("15) Visualizzare toDo in scadenza entro la data (gg/mm/aa)");
                       System.out.println();
                       System.out.println("0) Esci dall'applicazione");
                       System.out.println("----------------------------------------");

                       scelta2 =scann.nextInt();
                       scann.nextLine();
                       if(scelta2 == 1){
                           System.out.println("Inserire tipo della bacheca(1 = Universita, 2 = Lavoro, 3 =  Tempo Libero)");
                           int tipoBacheca = scann.nextInt();
                           scann.nextLine();
                           System.out.println("Inserire descrizione");
                           String descrizione = scann.nextLine();
                           if(tipoBacheca == 1){
                               Board b = new Board(TypeBoard.UNIVERSITY, descrizione);
                               management.addBoard(email, b);
                           } else if (tipoBacheca == 2) {
                               Board b = new Board(TypeBoard.WORK, descrizione);
                               management.addBoard(email, b);
                           } else if (tipoBacheca == 3) {
                               Board b = new Board(TypeBoard.FREETIME, descrizione);
                               management.addBoard(email, b);
                           }else{
                               System.out.println("Non valido...");
                           }
                       }else if(scelta2 == 2){
                           System.out.println("inserire tipo della bacheca da eliminare(Universita, Lavoro, Tempo Libero)");
                           String tipo = scann.nextLine();
                           management.deleteBoard(email, tipo);
                       }else if(scelta2 == 3){
                           System.out.println("inserire bacheca nella quale inserire toDo: (Universita, Lavoro, Tempo Libero)");
                           String tipo = scann.nextLine();
                           System.out.println("Inserire Titolo del toDo");
                           String titoloToDo = scann.nextLine();
                           System.out.println("Inserire desctizione del toDo");
                           String descrizioneToDo = scann.nextLine();
                           System.out.println("Inserire la data di scadenza da verificare");
                           String data = scann.nextLine();
                           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                           LocalDate dataScadenza = LocalDate.parse(data,formatter);
                           boolean stato = false;
                           CheckList c = new CheckList();
                           ToDo t = new ToDo(titoloToDo,descrizioneToDo,stato,c,dataScadenza);
                           management.addToDoInBoard(email,tipo,t);
                       }else if(scelta2 == 4){
                           System.out.println("Inserire nome della Bacheca");
                           String nomeBacheca = scann.nextLine();
                           System.out.println("Inserire Titolo del toDo");
                           String titoloToDo = scann.nextLine();
                           System.out.println("Inserire nome attivita");
                           String nomeA = scann.nextLine();
                           boolean stato = false;
                           Activity act = new Activity(nomeA,stato);
                           management.addActivity(email,titoloToDo,nomeBacheca,act);
                       }else if(scelta2 == 5){
                           System.out.println("Inserire nome della Bacheca");
                           String nomeBacheca = scann.nextLine();
                           System.out.println("Inserire Titolo del toDo");
                           String titoloToDo = scann.nextLine();
                           System.out.println("Inserire nome attivita da rimuovere");
                           String nomeA = scann.nextLine();
                           management.removeActivity(email,titoloToDo,nomeBacheca,nomeA);
                       } else if (scelta2 == 6) {
                           System.out.println("Inserire nome della Bacheca");
                           String nomeBacheca = scann.nextLine();
                           System.out.println("Inserire Titolo del toDo da condividere");
                           String titoloToDo = scann.nextLine();
                           System.out.println("Inserire Email dell'utente a cui condividere il toDo");
                           String mailUtente = scann.nextLine();
                           management.shareToDo(email, mailUtente,nomeBacheca,titoloToDo);
                       }else if(scelta2 == 7){
                           System.out.println("Inserire nome della Bacheca");
                           String nomeBacheca = scann.nextLine();
                           System.out.println("Inserire Titolo del toDo");
                           String titoloToDo = scann.nextLine();
                           System.out.println("Inserire nome attivita da Spuntare");
                           String nomeA = scann.nextLine();
                           management.checkActivity(email,nomeBacheca,titoloToDo,nomeA);
                       } else if (scelta2 == 8) {
                           System.out.println("Inserire nome della Bacheca di cui stampare i toDo");
                           String nomeBacheca = scann.nextLine();
                           management.printTodo(email,nomeBacheca);
                       } else if (scelta2 == 9) {
                           System.out.println("Inserire nome della Bacheca");
                           String nomeBacheca = scann.nextLine();
                           System.out.println("Inserire Titolo del toDo di cui stampare le attivita");
                           String titoloToDo = scann.nextLine();
                           management.printActs(email,nomeBacheca,titoloToDo);
                       } else if (scelta2 == 10) {
                           management.printHistory(email);
                       } else if (scelta2 == 11) {
                           System.out.println("Inserire nome della attivita da rimuovere dalla Cronologia");
                           String nomeAct = scann.nextLine();
                           management.rmvHistoryAct(email,nomeAct);
                       } else if (scelta2 == 12) {
                           management.dltHistory(email);
                       } else if (scelta2 == 13) {
                           System.out.println("Inserire nome della Bacheca di cui stampare l'Archivio");
                           String nomeBacheca = scann.nextLine();
                           management.printArchive(email,nomeBacheca);
                       } else if (scelta2 == 14) {
                           System.out.println("Inserire nome della Bacheca");
                           String nomeBacheca = scann.nextLine();
                           System.out.println("Inserire Titolo del toDo da spostare");
                           String titoloToDo = scann.nextLine();
                           System.out.println("Inserire la posizione in cui si vuole il toDo");
                           int posiz = scann.nextInt();
                           scann.nextLine();
                           posiz += 1;
                           management.swapToDo(email,nomeBacheca,titoloToDo,posiz);
                       } else if (scelta2 == 15) {
                           System.out.println("Inserire nome della Bacheca di cui verificarne scadenza toDo");
                           String nomeBacheca = scann.nextLine();
                           System.out.println("Inserire la data di scadenza da verificare");
                           String data = scann.nextLine();
                           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                           LocalDate dataScadenza = LocalDate.parse(data,formatter);
                           management.printTodoRange(email,nomeBacheca,dataScadenza);
                       }
                   }while(scelta2 != 0);
               }
           }

       }while(scelta1 != 0);

        System.out.println("Applicazione Chiusa");



    }
}