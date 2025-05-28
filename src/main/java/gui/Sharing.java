package gui;

import controller.ApplicationManagement;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Sharing {

    private JPanel panelSharing;
    private JTextField textEmail;
    private JButton shareButton;
    private JComboBox comboBoxToDo;

    public Sharing(ApplicationManagement controller, String emailUtente, JFrame vecchioFrame, String tipoBacheca){

        shareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                condividivisione(emailUtente,textEmail.getText(),tipoBacheca,controller);
            }
        });
    }

    public void condividivisione(String emailCreatore, String emailDaCondividere, String bacheca, ApplicationManagement controller){

    }

}
