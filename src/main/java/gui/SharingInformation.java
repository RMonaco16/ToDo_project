package gui;

import controller.ApplicationManagement;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SharingInformation {

    private JPanel panelSharingInformation;
    private JTextField textNickname;
    private JTextField textEmail;
    private JTable tableInformazioniUsers;
    private JButton deleteSelectedButton;
    private DefaultTableModel defaultTableModel;

    public SharingInformation(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente, String nomeTodo) {



        // Imposta nome e email dell'amministratore
        String adminName = controller.getToAdministratorNick(emailUtente, nomeTodo);
        String adminEmail = controller.getToAdministratorMail(emailUtente, nomeTodo);

        textNickname.setText(adminName);
        textEmail.setText(adminEmail);

        textNickname.setEditable(false);
        textEmail.setEditable(false);

        // Recupera gli utenti con cui è condiviso il To-Do
        ArrayList<User> sharedUsers = controller.getToDoUserShared(emailUtente, nomeTodo);

        // Configura la tabella
        String[] columnNames = {"Name", "Email"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (User u : sharedUsers) {
            model.addRow(new Object[]{u.getNickname(), u.getEmail()});
        }

        tableInformazioniUsers.setModel(model);

        deleteSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tableInformazioniUsers.getSelectedRow();//prende la riga selezionata
                if (selectedRow >= 0) {
                    // Supponiamo che l'email sia nella colonna 1
                    String emailDaEliminare = tableInformazioniUsers.getValueAt(selectedRow, 1).toString();//prende lattributo alla riga selezionata in base alla colonna

                    boolean successo = controller.rimuoviUtenteDaCondivisione(emailUtente, nomeTodo, emailDaEliminare);
                    if (successo) {
                        JOptionPane.showMessageDialog(null, "User removed from sharing!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Only the administrator can manage users", "administrator only",JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a row before proceeding.");
                }
            }
        });

    }

    public JPanel getPanel() {
        return panelSharingInformation;
    }

}
