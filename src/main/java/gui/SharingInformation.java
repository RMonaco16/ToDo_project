package gui;

import controller.ApplicationManagement;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SharingInformation {

    private JPanel panelSharingInformation;
    private JTextField textNickname;
    private JTextField textEmail;
    private JTable tableInformazioniUsers;
    private JButton deleteSelectedButton;
    private Runnable onSharingEnded;


    public SharingInformation(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente, String nomeTodo, String boardName, Runnable onSharingEnded) {

        // Inizializza la tabella con i membri attuali
        updateSharingMember(controller, emailUtente, boardName, nomeTodo);

        // Azione bottone elimina
        deleteSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tableInformazioniUsers.getSelectedRow();
                if (selectedRow >= 0) {
                    String emailDaEliminare = tableInformazioniUsers.getValueAt(selectedRow, 1).toString();

                    boolean successo = controller.rimuoviUtenteDaSharing(emailUtente, emailDaEliminare, boardName, nomeTodo);
                    if (successo) {
                        JOptionPane.showMessageDialog(null, "User removed from sharing!");
                        updateSharingMember(controller, emailUtente, boardName, nomeTodo);  // Ricarica la tabella aggiornata
                    } else {
                        if(emailUtente.equalsIgnoreCase(emailDaEliminare) && emailUtente.equalsIgnoreCase(controller.getToAdministratorMail(emailUtente,nomeTodo))){
                            JOptionPane.showMessageDialog(null, "you cannot eliminate yourself", "Warning", JOptionPane.WARNING_MESSAGE);
                        }else{
                            JOptionPane.showMessageDialog(null, "Only the administrator can manage users", "Administrator Only", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a row before proceeding.");
                }
            }
        });
        this.onSharingEnded = onSharingEnded;

    }

    public void updateSharingMember(ApplicationManagement controller, String emailUtente, String boardName, String nomeTodo) {
        // Recupera info amministratore
        String adminName = controller.getToAdministratorNick(emailUtente, nomeTodo);
        String adminEmail = controller.getToAdministratorMail(emailUtente, nomeTodo);

        textNickname.setText(adminName);
        textEmail.setText(adminEmail);

        textNickname.setEditable(false);
        textEmail.setEditable(false);

        // Recupera utenti condivisi
        ArrayList<User> sharedUsers = controller.getToDoUserShared(emailUtente, nomeTodo);

        //  Chiude la finestra se non ci sono più utenti condivisi
        if (sharedUsers == null || sharedUsers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No more shared users. The Sharing was deleted.", "Info", JOptionPane.INFORMATION_MESSAGE);


            // NOTIFICA alla GUI principale di aggiornare
            if (onSharingEnded != null) {
                onSharingEnded.run();
            }

            Window window = SwingUtilities.getWindowAncestor(panelSharingInformation);
            if (window != null) {
                window.dispose();
            }
            return;
        }

        // Altrimenti aggiorna la tabella
        String[] columnNames = {"Name", "Email"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (User u : sharedUsers) {
            model.addRow(new Object[]{u.getNickname(), u.getEmail()});
        }

        tableInformazioniUsers.setModel(model);
        styleRedButton(deleteSelectedButton);

    }

    public JPanel getPanel() {
        return panelSharingInformation;
    }

    private void styleRedButton(JButton button) {
        Color baseColor = new Color(220, 53, 69); // Rosso acceso (Bootstrap-style)
        Color hoverColor = baseColor.darker();

        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(baseColor.darker(), 2));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(160, 40));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createLineBorder(hoverColor, 2));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createLineBorder(baseColor.darker(), 2));
            }
        });
    }

}