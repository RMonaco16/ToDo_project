package gui;

import controller.ApplicationManagement;
import model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Sharing {

    private JPanel panelSharing;
    private JTextField textEmail;
    private JButton shareButton;
    private JComboBox<String> comboBoxToDo;
    private JFrame nuovoFrame;
    private ApplicationManagement controller;
    private String emailUtente;
    private String tipoBacheca;

    public Sharing(ApplicationManagement controller, String emailUtente, JFrame vecchioFrame, String tipoBacheca){
        this.controller = controller;
        this.emailUtente = emailUtente;
        this.tipoBacheca = tipoBacheca;

        // Mostra la GUI
        nuovoFrame = new JFrame("Sharing ToDo");
        nuovoFrame.setContentPane(panelSharing);
        nuovoFrame.pack();
        nuovoFrame.setSize(500, 300);
        nuovoFrame.setLocationRelativeTo(null);
        nuovoFrame.setVisible(true);

        //-----------Popolamento Combo Box---------
        popolaComboBox();
        //------------------------------------------

        //-----------------Condividi---------------
        shareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textEmail.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(panelSharing, "Inserisci una email.", "Errore", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (comboBoxToDo.getSelectedItem() == null || comboBoxToDo.getSelectedItem().equals("--")) {
                    JOptionPane.showMessageDialog(panelSharing, "Seleziona un ToDo valido.", "Errore", JOptionPane.WARNING_MESSAGE);
                    return;
                } else {
                    condividiToDo(emailUtente, textEmail.getText(), tipoBacheca, (String) comboBoxToDo.getSelectedItem());
                }
            }
        });
        //-----------------------------------------
    }

    private void popolaComboBox() {
        ArrayList<ToDo> listaToDo = controller.getToDoAdminNonCondivisi(emailUtente, tipoBacheca);

        comboBoxToDo.removeAllItems();
        comboBoxToDo.addItem("--");

        for (ToDo todo : listaToDo) {
            comboBoxToDo.addItem(todo.getTitle());
        }
    }



    public void condividiToDo(String emailCreatore, String emailDaCondividere, String bacheca, String toDoName){
        if (!controller.isUserAdminOfToDo(emailCreatore, bacheca, toDoName)) {
            JOptionPane.showMessageDialog(panelSharing, "Non puoi condividere un ToDo che non amministri.", "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean risultato = controller.shareToDo(emailCreatore, emailDaCondividere, bacheca, toDoName);
        if (!risultato) {
            JOptionPane.showMessageDialog(panelSharing, "Utente non trovato o errore nello sharing.", "Errore", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(panelSharing, "ToDo condiviso con successo!", "Condivisione completata", JOptionPane.INFORMATION_MESSAGE);
            nuovoFrame.setVisible(false);
            nuovoFrame.dispose();
        }
    }



    public JFrame getFrame() {
        return this.nuovoFrame;
    }
}
