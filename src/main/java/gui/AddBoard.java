package gui;

import controller.ApplicationManagement;
import model.Board;
import model.TypeBoard;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import java.awt.*;

/**
 * Finestra GUI per aggiungere una nuova bacheca (Board) all'applicazione.
 * Permette all'utente di selezionare il tipo di bacheca, inserire una descrizione
 * e creare la bacheca tramite il controller.
 */
public class AddBoard {
    private JRadioButton universityRadioButton;
    private JRadioButton workRadioButton;
    private JRadioButton freeTimeRadioButton;
    private JButton createBoardButton;
    private JTextField textDescription;
    private JPanel panelAddBoard;
    private JFrame nuovoFrame;

    private Home home;

    /**
     * Costruttore che crea e mostra la finestra per aggiungere una nuova bacheca.
     *
     * @param controller riferimento al controller dell'applicazione per la gestione delle board
     * @param vecchioFrame il frame genitore da cui si è aperta questa finestra
     * @param emailUtente email dell'utente corrente
     * @param home riferimento alla finestra principale per aggiornare la UI dopo la creazione
     */
    public AddBoard(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente, Home home) {
        this.home = home;

        // Inizializzazione pannello principale con sfondo a gradiente
        panelAddBoard = new JPanel() {
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
        panelAddBoard.setLayout(new BorderLayout(10, 10));
        panelAddBoard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Label titolo
        JLabel titleLabel = new JLabel("Add Board");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panelAddBoard.add(titleLabel, BorderLayout.NORTH);

        // Pannello centrale con radio button e campo descrizione
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Radio button per tipi di bacheca
        universityRadioButton = new JRadioButton("University");
        workRadioButton = new JRadioButton("Work");
        freeTimeRadioButton = new JRadioButton("Free Time");

        Font radioFont = new Font("Segoe UI", Font.PLAIN, 16);
        universityRadioButton.setFont(radioFont);
        workRadioButton.setFont(radioFont);
        freeTimeRadioButton.setFont(radioFont);

        universityRadioButton.setOpaque(false);
        workRadioButton.setOpaque(false);
        freeTimeRadioButton.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(universityRadioButton);
        group.add(workRadioButton);
        group.add(freeTimeRadioButton);

        // Aggiunta radio button al pannello
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(universityRadioButton, gbc);

        gbc.gridy = 1;
        centerPanel.add(workRadioButton, gbc);

        gbc.gridy = 2;
        centerPanel.add(freeTimeRadioButton, gbc);

        // Campo testo per la descrizione della bacheca
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        textDescription = new JTextField(20);
        textDescription.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Placeholder (prompt) per il campo descrizione
        PromptSupport.setPrompt("Insert Description", textDescription);
        PromptSupport.setForeground(Color.GRAY, textDescription);
        PromptSupport.setFontStyle(Font.ITALIC, textDescription);

        centerPanel.add(textDescription, gbc);
        panelAddBoard.add(centerPanel, BorderLayout.CENTER);

        // Pannello per il bottone di creazione
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        createBoardButton = new JButton("Create Board");
        createBoardButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        createBoardButton.setBackground(Color.decode("#A8BDB5"));
        createBoardButton.setForeground(Color.WHITE);
        createBoardButton.setFocusPainted(false);
        createBoardButton.setOpaque(true);
        createBoardButton.setPreferredSize(new Dimension(140, 40));

        // Cambia colore bottone al passaggio del mouse
        Color baseColor = Color.decode("#A8BDB5");
        Color hoverColor = baseColor.darker();
        createBoardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                createBoardButton.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                createBoardButton.setBackground(baseColor);
            }
        });

        buttonPanel.add(createBoardButton);
        panelAddBoard.add(buttonPanel, BorderLayout.SOUTH);

        // Azione al click del bottone "Create Board"
        createBoardButton.addActionListener(e -> {
            addNewBoard(controller, emailUtente);
            nuovoFrame.setVisible(false);
            nuovoFrame.dispose();
        });

        // Configurazione JFrame principale
        nuovoFrame = new JFrame("Add Board");
        nuovoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        nuovoFrame.setContentPane(panelAddBoard);
        nuovoFrame.pack();
        nuovoFrame.setSize(450, 350);
        nuovoFrame.setLocationRelativeTo(null);
        nuovoFrame.setVisible(true);
    }

    /**
     * Metodo per aggiungere una nuova bacheca tramite il controller.
     * Verifica che sia stato selezionato un tipo e inserita una descrizione.
     * Se la bacheca viene creata correttamente, aggiorna la UI della home.
     *
     * @param controller il controller dell'applicazione per la gestione delle board
     * @param emailUtente email dell'utente proprietario della bacheca
     */
    public void addNewBoard(ApplicationManagement controller, String emailUtente) {
        String descrizione = textDescription.getText().trim();
        boolean creatoCorrettamente = false;

        if (universityRadioButton.isSelected() && !descrizione.isEmpty()) {
            Board b = new Board(TypeBoard.UNIVERSITY, descrizione);
            creatoCorrettamente = controller.addBoard(emailUtente, b);
            if (creatoCorrettamente) {
                home.addBoardButton(b, controller, emailUtente);
            } else {
                JOptionPane.showMessageDialog(null, "This board already exists!");
            }
        } else if (workRadioButton.isSelected() && !descrizione.isEmpty()) {
            Board b = new Board(TypeBoard.WORK, descrizione);
            creatoCorrettamente = controller.addBoard(emailUtente, b);
            if (creatoCorrettamente) {
                home.addBoardButton(b, controller, emailUtente);
            } else {
                JOptionPane.showMessageDialog(null, "This board already exists!");
            }
        } else if (freeTimeRadioButton.isSelected() && !descrizione.isEmpty()) {
            Board b = new Board(TypeBoard.FREETIME, descrizione);
            creatoCorrettamente = controller.addBoard(emailUtente, b);
            if (creatoCorrettamente) {
                home.addBoardButton(b, controller, emailUtente);
            } else {
                JOptionPane.showMessageDialog(null, "This board already exists!");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Select a board and enter a description before creating!");
        }
    }

    /**
     * Restituisce il frame principale di questa finestra.
     *
     * @return il JFrame associato alla finestra AddBoard
     */
    public JFrame getFrame() {
        return nuovoFrame;
    }
}
