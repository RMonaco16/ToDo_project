package gui;

import controller.ApplicationManagement;
import model.*;

import javax.swing.*;
import java.awt.*;
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
    private Runnable onShareSuccess;

    public Sharing(ApplicationManagement controller, String emailUtente, JFrame vecchioFrame, String tipoBacheca, Runnable onShareSuccess){
        this.controller = controller;
        this.emailUtente = emailUtente;
        this.tipoBacheca = tipoBacheca;
        this.onShareSuccess = onShareSuccess;

        // Mostra la GUI
        nuovoFrame = new JFrame("Sharing ToDo");
        nuovoFrame.setContentPane(panelSharing);
        nuovoFrame.pack();
        nuovoFrame.setSize(400, 200);
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
                    JOptionPane.showMessageDialog(panelSharing, "Enter an email.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (comboBoxToDo.getSelectedItem() == null || comboBoxToDo.getSelectedItem().equals("--")) {
                    JOptionPane.showMessageDialog(panelSharing, "Please select a valid ToDo.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                } else {
                    condividiToDo(emailUtente, textEmail.getText(), tipoBacheca, (String) comboBoxToDo.getSelectedItem());
                }
            }
        });
        //-----------------------------------------
    }

    private void popolaComboBox() {
        ArrayList<String> listaToDo = controller.getToDoAdminNonCondivisi(emailUtente, tipoBacheca);//ArrayList<ToDo> listaToDo = controller.getToDoAdminNonCondivisi(emailUtente, tipoBacheca);

        comboBoxToDo.removeAllItems();
        comboBoxToDo.addItem("--");

            for (String todo : listaToDo) {
            comboBoxToDo.addItem(todo);
        }
    }

    public void condividiToDo(String emailCreatore, String emailDaCondividere, String bacheca, String toDoName){
        if (!controller.isUserAdminOfToDo(emailCreatore, bacheca, toDoName)) {
            JOptionPane.showMessageDialog(panelSharing, "You can't share a ToDo that you don't manage.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean risultato = controller.shareToDo(emailCreatore, emailDaCondividere, bacheca, toDoName);
        if (!risultato) {
            JOptionPane.showMessageDialog(panelSharing, "User not found or sharing error.", "Error", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(panelSharing, "ToDo shared successfully!", "Sharing completed", JOptionPane.INFORMATION_MESSAGE);
            nuovoFrame.setVisible(false);
            nuovoFrame.dispose();

            if (onShareSuccess != null) {
                onShareSuccess.run();
            }
        }
    }

    public JFrame getFrame() {
        return this.nuovoFrame;
    }
}
