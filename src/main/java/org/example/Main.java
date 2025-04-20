package org.example;

import model.ApplicationManagement;
import model.User;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

       Scanner scannInt = new Scanner(System.in);
       Scanner scannString = new Scanner(System.in);

       ApplicationManagement management = new ApplicationManagement();

       int scelta1 = 0;
       int scelta2 = 0;

       boolean loggato = false;

       do{
           System.out.println("----------------------------------");
           System.out.println("1) Crea Utente");
           System.out.println("2) Login Utente");
           System.out.println("3) Esci");
           System.out.println("----------------------------------");
           if(scelta1 == 1){
               System.out.println("Inserire NickName");
               String nickname = scannString.nextLine();
               System.out.println("Inserire Email");
               String email = scannString.nextLine();
               System.out.println("Inserire Password");
               String password = scannString.nextLine();
               User u = new User(nickname,email,password);
           }else if(scelta == 2){

           }

       }while(scelta1 != 3);





    }
}