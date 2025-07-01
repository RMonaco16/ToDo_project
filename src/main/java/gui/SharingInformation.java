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
 * Classe che rappresenta l'interfaccia grafica per la gestione della condivisione
 * di un'attività (ToDo) tra più utenti.
 * Permette all'amministratore di visualizzare e rimuovere utenti associati al ToDo.
 */
public class SharingInformation {

    private JPanel panelSharingInformation;
    private JTextField textNickname;
    private JTextField textEmail;
    private JTable tableInformazioniUsers;
    private JButton deleteSelectedButton;
    private Runnable onSharingEnded;


    /**
     * Costruttore della classe.
     *
     * @param controller      Controller per la gestione dell'applicazione.
     * @param emailUtente     Email dell'utente corrente.
     * @param nomeTodo        Nome dell'attività (ToDo).
     * @param boardName       Nome della bacheca su cui è presente il ToDo.
     * @param onSharingEnded  Azione da eseguire quando termina la condivisione.
     */
    public SharingInformation(ApplicationManagement controller, String emailUtente, String nomeTodo, String boardName, Runnable onSharingEnded) {

        // Inizializza la tabella con i membri attuali
        boolean isAdmin = controller.isUserAdminOfToDo(emailUtente, boardName, nomeTodo);

        // Inizializza la tabella con i membri attuali
        updateSharingMember(controller, emailUtente, boardName, nomeTodo, isAdmin);

        if (!isAdmin) {
            deleteSelectedButton.setVisible(false);
            tableInformazioniUsers.setEnabled(false);
        }

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
                        updateSharingMember(controller, emailUtente, boardName, nomeTodo,isAdmin);  // Ricarica la tabella aggiornata
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

    /**
     * Aggiorna le informazioni visualizzate nella GUI:
     * - mostra amministratore corrente,
     * - carica gli utenti con cui il ToDo è condiviso nella tabella.
     *
     * @param controller   Controller dell'applicazione.
     * @param emailUtente  Email dell'utente che sta visualizzando l'interfaccia.
     * @param boardName    Nome della bacheca associata al ToDo.
     * @param nomeTodo     Nome del ToDo condiviso.
     * @param isAdmin      True se l'utente corrente è amministratore del ToDo.
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

        // Tabella non editabile
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
            styleRedButton(deleteSelectedButton); // Applica stile solo se visibile
        }
    }


    /**
     * Restituisce il pannello principale contenente l'interfaccia di condivisione.
     *
     * @return Il pannello JPanel associato all'interfaccia grafica.
     */
    public JPanel getPanel() {
        return panelSharingInformation;
    }


    /**
     * Applica uno stile grafico "rosso" al bottone (effetto hover incluso).
     *
     * @param button Il bottone a cui applicare lo stile.
     */
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
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createLineBorder(hoverColor, 2));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createLineBorder(baseColor.darker(), 2));
            }
        });
    }
}