package gui;

import controller.ApplicationManagement;
import model.ToDo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Sharing {

    private JPanel panelSharing;
    private JTextField textEmail;
    private JButton shareButton;
    private JComboBox comboBoxToDo;
    private JFrame nuovoFrame;

    public Sharing(ApplicationManagement controller, String emailUtente, JFrame vecchioFrame, String tipoBacheca){

        // Mostra la GUI
        nuovoFrame = new JFrame("Sharing ToDo");
        nuovoFrame.setContentPane(panelSharing);
        nuovoFrame.pack();
        nuovoFrame.setSize(500, 300);
        nuovoFrame.setLocationRelativeTo(null);
        nuovoFrame.setVisible(true);

        //-----------Popolamento Combo Box---------
        ArrayList<ToDo> listaToDo = controller.printTodo(emailUtente,tipoBacheca); // Ottieni la lista
        comboBoxToDo.removeAllItems(); // Pulisci la comboBox
        comboBoxToDo.addItem("--"); // Placeholder nullo

        for (int i = 0; i < listaToDo.size(); i++) {
            if(listaToDo.get(i).isCondiviso() != true){
                comboBoxToDo.addItem(listaToDo.get(i).getTitle()); // Aggiungi ogni elemento alla comboBox
            }
        }
        //------------------------------------------

        //-----------------Condividi---------------
        shareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textEmail.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(panelSharing, "enter an email.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (comboBoxToDo.getSelectedItem() == null || comboBoxToDo.getSelectedItem().equals("--")) {
                    JOptionPane.showMessageDialog(panelSharing, "Select a valid ToDo.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }else{
                    condividivisione(emailUtente,textEmail.getText(),tipoBacheca,controller);
                }
            }
        });
        //-----------------------------------------

    }

    public void condividivisione(String emailCreatore, String emailDaCondividere, String bacheca, ApplicationManagement controller){
        String toDoName = (String) comboBoxToDo.getSelectedItem();
        boolean risultato = controller.shareToDo(emailCreatore,emailDaCondividere,bacheca,toDoName);
        if (risultato == false) {
            JOptionPane.showMessageDialog(panelSharing, "User does not exist.", "Not found", JOptionPane.INFORMATION_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(panelSharing, "ToDo shared successfully!!.", "Sharing done", JOptionPane.INFORMATION_MESSAGE);
            nuovoFrame.setVisible(false);
            nuovoFrame.dispose();
        }

    }

    public JFrame getFrame() {
        return this.nuovoFrame;
    }


}
