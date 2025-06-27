package gui;

import controller.ApplicationManagement;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class Chronology {

    private JPanel panelCronologia;
    private JTable tableCronologia;
    private JButton deleteSelectedButton;
    private JButton deleteAllButton;
    private DefaultTableModel defaultTableModel;
    private JFrame nuovoFrame;

    public Chronology(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente) {

        // Creo il pannello con sfondo a gradiente simile a Home
        panelCronologia = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                int width = getWidth();
                int height = getHeight();
                Color startColor = Color.decode("#F9F5F0");
                Color endColor = Color.decode("#D3C7B8");
                GradientPaint gp = new GradientPaint(0, 0, startColor, 0, height, endColor);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();
            }
        };
        panelCronologia.setLayout(new BorderLayout(10,10));
        panelCronologia.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Titolo
        JLabel titleLabel = new JLabel("Chronology");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panelCronologia.add(titleLabel, BorderLayout.NORTH);

        // Tabella con dati
        String[] columns = {"Completion Date", "Activity Name"};
        defaultTableModel = new DefaultTableModel(columns, 0);
        tableCronologia = new JTable(defaultTableModel);
        tableCronologia.setFillsViewportHeight(true);
        tableCronologia.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tableCronologia.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        JScrollPane scrollPane = new JScrollPane(tableCronologia);
        panelCronologia.add(scrollPane, BorderLayout.CENTER);

        // Carica dati dal controller
        ArrayList<Activity> attivitaCronologia = controller.returnCompletedActivity(emailUtente);
        if (attivitaCronologia == null || attivitaCronologia.isEmpty()) {
            JOptionPane.showMessageDialog(panelCronologia, "There are no completed activities.", "Empty Timeline", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Activity act : attivitaCronologia) {
                defaultTableModel.addRow(new Object[]{act.getCompletionDate(), act.getName()});
            }
        }

        // Pannello pulsanti centrato
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setOpaque(false);

        // Pulsanti con emoji Unicode corretta
        deleteSelectedButton = new JButton("\uD83D\uDDD1\uFE0F Delete Selected");
        deleteAllButton = new JButton("\u274E Delete All");

        // Imposta font emoji-friendly
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 18);
        deleteSelectedButton.setFont(emojiFont);
        deleteAllButton.setFont(emojiFont);

        // Impostazioni stile (background, colore, bordi)
        setupButtonWithEmojiFont(deleteSelectedButton);
        setupButtonWithEmojiFont(deleteAllButton);

        buttonPanel.add(deleteSelectedButton);
        buttonPanel.add(deleteAllButton);

        panelCronologia.add(buttonPanel, BorderLayout.SOUTH);

        // Azioni pulsanti
        deleteSelectedButton.addActionListener(e -> {
            int selectedRow = tableCronologia.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panelCronologia, "Select a row to delete.", "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String nomeAttivita = (String) tableCronologia.getValueAt(selectedRow, 1);
            controller.rmvHistoryAct(emailUtente, nomeAttivita);
            defaultTableModel.removeRow(selectedRow);
        });

        deleteAllButton.addActionListener(e -> {
            int rowCount = defaultTableModel.getRowCount();
            if (rowCount == 0) {
                JOptionPane.showMessageDialog(panelCronologia, "The history is already empty.", "No activity", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(panelCronologia,
                    "Are you sure you want to delete all completed tasks?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.dltHistory(emailUtente);
                defaultTableModel.setRowCount(0);
            }
        });

        // Finestra JFrame
        nuovoFrame = new JFrame("Chronology");
        nuovoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        nuovoFrame.setContentPane(panelCronologia);
        nuovoFrame.setSize(450, 650);
        nuovoFrame.setLocationRelativeTo(null);
        nuovoFrame.setVisible(true);
    }

    // Metodo helper per impostare font emoji-friendly e stile pulsante
    private void setupButtonWithEmojiFont(JButton button) {
        Color baseColor = Color.decode("#A8BDB5");
        Color hoverColor = baseColor.darker();

        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(baseColor, 2));
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
                button.setBorder(BorderFactory.createLineBorder(baseColor, 2));
            }
        });
    }

    // Per controllo finestre multiple
    public JFrame getFrame() {
        return nuovoFrame;
    }
}
