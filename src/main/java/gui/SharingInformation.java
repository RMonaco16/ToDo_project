package gui;

import controller.ApplicationManagement;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Pannello GUI che mostra le informazioni di condivisione di un ToDo,
 * inclusi gli utenti con cui è condiviso, e permette la rimozione
 * di utenti dal sharing se chi usa è amministratore.
 */
public class SharingInformation {

    private JPanel panelSharingInformation;
    private JTextField textNickname;
    private JTextField textEmail;
    private JTable tableInformazioniUsers;
    private JButton deleteSelectedButton;
    private Runnable onSharingEnded;

    /**
     * Costruttore che inizializza il pannello di gestione della condivisione,
     * mostrando gli utenti con cui è condiviso un ToDo e abilitando la rimozione
     * solo per gli amministratori.
     *
     * @param controller    Controller per la gestione dell'applicazione.
     * @param vecchioFrame  Frame chiamante (non utilizzato direttamente).
     * @param emailUtente   Email dell'utente corrente.
     * @param nomeTodo     Nome del ToDo condiviso.
     * @param boardName    Nome della bacheca.
     * @param onSharingEnded Runnable eseguito quando la condivisione termina (es. tutti gli utenti rimossi).
     */
    public SharingInformation(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente, String nomeTodo, String boardName, Runnable onSharingEnded) {

        // Controlla se l'utente è amministratore del ToDo
        boolean isAdmin = controller.isUserAdminOfToDo(emailUtente, boardName, nomeTodo);

        // Aggiorna la tabella con i membri attuali
        updateSharingMember(controller, emailUtente, boardName, nomeTodo, isAdmin);

        // Se non è admin, nasconde il bottone elimina e disabilita tabella
        if (!isAdmin) {
            deleteSelectedButton.setVisible(false);
            tableInformazioniUsers.setEnabled(false);
        }

        // Azione bottone elimina utente selezionato
        deleteSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tableInformazioniUsers.getSelectedRow();
                if (selectedRow >= 0) {
                    String emailDaEliminare = tableInformazioniUsers.getValueAt(selectedRow, 1).toString();

                    boolean successo = controller.rimuoviUtenteDaSharing(emailUtente, emailDaEliminare, boardName, nomeTodo);
                    if (successo) {
                        JOptionPane.showMessageDialog(null, "User removed from sharing!");
                        updateSharingMember(controller, emailUtente, boardName, nomeTodo,isAdmin);  // Aggiorna tabella
                    } else {
                        // Messaggi di errore specifici
                        if(emailUtente.equalsIgnoreCase(emailDaEliminare) && emailUtente.equalsIgnoreCase(controller.getToAdministratorMail(emailUtente,nomeTodo))){
                            JOptionPane.showMessageDialog(null, "You cannot eliminate yourself", "Warning", JOptionPane.WARNING_MESSAGE);
                        } else {
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

    /**
     * Aggiorna la tabella con gli utenti con cui è condiviso il ToDo.
     * Mostra anche il nome e l'email dell'amministratore.
     * Se non ci sono più utenti condivisi, notifica e chiude il pannello.
     *
     * @param controller  Controller dell'applicazione.
     * @param emailUtente Email dell'utente corrente.
     * @param boardName   Nome della bacheca.
     * @param nomeTodo    Nome del ToDo.
     * @param isAdmin     True se l'utente è amministratore del ToDo.
     */
    public void updateSharingMember(ApplicationManagement controller, String emailUtente, String boardName, String nomeTodo, boolean isAdmin) {
        // Recupera info amministratore
        String adminName = controller.getToAdministratorNick(emailUtente, nomeTodo);
        String adminEmail = controller.getToAdministratorMail(emailUtente, nomeTodo);

        textNickname.setText(adminName);
        textEmail.setText(adminEmail);

        textNickname.setEditable(false);
        textEmail.setEditable(false);

        ArrayList<User> sharedUsers = controller.getToDoUserShared(emailUtente, nomeTodo);

        if (sharedUsers == null || sharedUsers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No more shared users. The Sharing was deleted.", "Info", JOptionPane.INFORMATION_MESSAGE);

            if (onSharingEnded != null) onSharingEnded.run();

            Window window = SwingUtilities.getWindowAncestor(panelSharingInformation);
            if (window != null) window.dispose();
            return;
        }

        String[] columnNames = {"Name", "Email"};

        // Modello tabella non editabile
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (User u : sharedUsers) {
            model.addRow(new Object[]{u.getNickname(), u.getEmail()});
        }

        tableInformazioniUsers.setModel(model);

        if (isAdmin) {
            styleRedButton(deleteSelectedButton); // Applica stile al bottone elimina
        }
    }

    /**
     * Restituisce il pannello principale di questo componente GUI.
     *
     * @return JPanel con le informazioni di sharing.
     */
    public JPanel getPanel() {
        return panelSharingInformation;
    }

    /**
     * Applica uno stile "rosso" e interattivo a un bottone (per il pulsante elimina).
     *
     * @param button JButton a cui applicare lo stile.
     */
    private void styleRedButton(JButton button) {
        Color baseColor = new Color(220, 53, 69); // Rosso acceso (stile Bootstrap)
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
