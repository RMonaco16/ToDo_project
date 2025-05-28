package gui;

import controller.ApplicationManagement;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Chronology {

    private JPanel panelCronologia;
    private JTable tableCronologia;
    private JButton deleteSelectedButton;
    private JButton deleteAllButton;
    private DefaultTableModel defaultTableModel;

    public Chronology(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente) {

        // Mostra la GUI
        JFrame nuovoFrame = new JFrame("Cronologia attività");
        nuovoFrame.setContentPane(panelCronologia);
        nuovoFrame.setSize(450, 650);
        nuovoFrame.setLocationRelativeTo(null);
        nuovoFrame.setVisible(true);

        // ─── Tabella: Creazione colonne tabella ─────────────────────────────
        String[] array = {"Completation Date", "Activity Name"};
        defaultTableModel = new DefaultTableModel(array, 0);
        tableCronologia.setModel(defaultTableModel);

        // ─── Ottieni le attività completate ────────────────────────────────
        ArrayList<Activity> attivitaCronologia = controller.returnCompletedActivity(emailUtente);

        // ─── Gestione caso lista nulla o vuota ─────────────────────────────
        if (attivitaCronologia == null || attivitaCronologia.isEmpty()) {
            JOptionPane.showMessageDialog(panelCronologia, "There are no completed activities.", "Empty Timeline", JOptionPane.INFORMATION_MESSAGE);
        }

        // ─── Aggiunta righe alla tabella ──────────────────────────────────
        defaultTableModel.setRowCount(0); // pulizia
        for (int i = 0; i < attivitaCronologia.size(); i++) {
            defaultTableModel.addRow(new Object[]{
                    attivitaCronologia.get(i).getCompletionDate(),
                    attivitaCronologia.get(i).getName()
            });
        }

        deleteSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tableCronologia.getSelectedRow();

                // Caso: nessuna riga selezionata
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(panelCronologia, "Select a row to delete.", "No selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Ottieni il nome attività dalla colonna 1
                String nomeAttivita = (String) tableCronologia.getValueAt(selectedRow, 1);

                // Rimuovi dal controller
                controller.rmvHistoryAct(emailUtente, nomeAttivita);

                // Rimuovi dalla tabella
                defaultTableModel.removeRow(selectedRow);

            }
        });
        deleteAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowCount = defaultTableModel.getRowCount();

                // Caso: tabella già vuota
                if (rowCount == 0) {
                    JOptionPane.showMessageDialog(panelCronologia, "The history is already empty.", "No activity", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Conferma eliminazione
                int confirm = JOptionPane.showConfirmDialog(panelCronologia,
                        "Are you sure you want to delete all completed tasks?",
                        "Confirm deletion",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    // Rimuove dal controller
                    controller.dltHistory(emailUtente);
                    // Svuota tabella
                    defaultTableModel.setRowCount(0);
                }
            }
        });
    }
}
