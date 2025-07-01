package gui;

import controller.ApplicationManagement;
import model.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Finestra GUI per la cancellazione di una board esistente.
 * Mostra una combo box con le board dell'utente, permette di selezionare una e cancellarla.
 */
public class DeleteBoardForm {
    private JPanel panelDeleteBoard;
    private JButton deleteButton;
    private JComboBox<String> comboBoxBoards;
    private JFrame nuovoFrame;

    /**
     * Costruttore che crea e visualizza la finestra di cancellazione board.
     * Carica le board esistenti tramite il controller e le inserisce nella combo box.
     * Gestisce le azioni del pulsante Delete e la chiusura della finestra.
     *
     * @param controller riferimento al controller dell'applicazione
     * @param vecchioFrame frame genitore da cui è stata aperta la finestra (per messaggi)
     * @param emailUtente email dell'utente corrente
     * @param home riferimento all'istanza della home per aggiornare la GUI dopo cancellazione
     */
    public DeleteBoardForm(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente, Home home) {
        // Ottengo la lista di board non nulle
        ArrayList<Board> listaBoard = controller.printBoard(emailUtente);
        ArrayList<Board> boardsNonNulle = new ArrayList<>();
        for (Board board : listaBoard) {
            if (board != null) {
                boardsNonNulle.add(board);
            }
        }

        // Se non ci sono board, avviso e non apro la finestra
        if (boardsNonNulle.isEmpty()) {
            JOptionPane.showMessageDialog(vecchioFrame, "No boards available", "No Boards", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pannello principale con sfondo a gradiente
        panelDeleteBoard = new JPanel() {
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
        panelDeleteBoard.setLayout(new BorderLayout(10, 10));
        panelDeleteBoard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Titolo
        JLabel titleLabel = new JLabel("Delete Board");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panelDeleteBoard.add(titleLabel, BorderLayout.NORTH);

        // Pannello centrale con label e combo box
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel selectLabel = new JLabel("Select a board:");
        selectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(selectLabel, gbc);

        comboBoxBoards = new JComboBox<>();
        comboBoxBoards.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboBoxBoards.addItem("--");
        for (Board board : boardsNonNulle) {
            comboBoxBoards.addItem(board.getType().toString());
        }
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerPanel.add(comboBoxBoards, gbc);

        panelDeleteBoard.add(centerPanel, BorderLayout.CENTER);

        // Pannello pulsante Delete con effetti hover
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        deleteButton.setBackground(Color.decode("#C44E4E"));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setOpaque(true);
        deleteButton.setPreferredSize(new Dimension(120, 40));

        Color baseColor = Color.decode("#C44E4E");
        Color hoverColor = baseColor.darker();
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                deleteButton.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                deleteButton.setBackground(baseColor);
            }
        });

        buttonPanel.add(deleteButton);
        panelDeleteBoard.add(buttonPanel, BorderLayout.SOUTH);

        // Listener sul pulsante Delete
        deleteButton.addActionListener((ActionEvent e) -> {
            if (comboBoxBoards.getSelectedItem() == null || comboBoxBoards.getSelectedItem().equals("--")) {
                JOptionPane.showMessageDialog(panelDeleteBoard, "Select a valid Board.", "Error", JOptionPane.WARNING_MESSAGE);
            } else {
                eliminazioneBachecaSelezionata(controller, emailUtente, home);
                nuovoFrame.dispose();
            }
        });

        // Creazione e visualizzazione JFrame
        nuovoFrame = new JFrame("Delete Board");
        nuovoFrame.setContentPane(panelDeleteBoard);
        nuovoFrame.setSize(400, 250);
        nuovoFrame.setLocationRelativeTo(null);
        nuovoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        nuovoFrame.setVisible(true);

        // Listener chiusura per pulire la finestra nella Home
        nuovoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                home.clearDeleteBoardFormWindow();
            }
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                home.clearDeleteBoardFormWindow();
            }
        });
    }

    /**
     * Elimina la board selezionata dalla combo box tramite il controller
     * e aggiorna la lista delle board nella home.
     *
     * @param controller riferimento al controller applicativo
     * @param emailUtente email dell'utente corrente
     * @param home riferimento alla home per aggiornare la vista
     */
    public void eliminazioneBachecaSelezionata(ApplicationManagement controller, String emailUtente, Home home) {
        String boardName = comboBoxBoards.getSelectedItem().toString();
        controller.deleteBoard(emailUtente, boardName);
        home.refreshBoards(controller, emailUtente);
    }

    /**
     * Restituisce il frame associato a questa finestra.
     *
     * @return il JFrame della finestra DeleteBoardForm
     */
    public JFrame getFrame() {
        return nuovoFrame;
    }
}
