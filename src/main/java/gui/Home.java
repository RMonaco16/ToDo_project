package gui;

import controller.ApplicationManagement;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Classe che rappresenta la schermata principale (Home) dell'applicazione,
 * dove l'utente può visualizzare, aggiungere o eliminare le proprie board.
 */
public class Home {
    public JPanel panelHome;
    private JButton ADDButton;
    private JPanel panelBoards;
    private JFrame jFrame;
    private JLabel emptyLabel;
    private AddBoard addBoardWindow = null;
    private Chronology chronologyWindow = null;
    private DeleteBoardForm deleteBoardFormWindow = null;
    private static final String DIALOG_TITLE = "Dialog";
    private static final String COLOR_GRAY_HEX = "#6B7280";

    private ArrayList<Board> userBoards;
    private User user;

    /**
     * Costruttore che crea e mostra la schermata Home per l'utente.
     *
     * @param controller  L'oggetto controller per la gestione dell'applicazione.
     * @param frameVecchio Il frame precedente da chiudere.
     * @param emailUtente  L'email dell'utente attualmente loggato.
     */
    public Home(ApplicationManagement controller, JFrame frameVecchio, String emailUtente) {
        frameVecchio.dispose();

        // Creazione del pannello principale con sfondo sfumato
        initPanelHome();
        initFrame();

        JPanel topPanel = setupTopPanel(controller, frameVecchio, emailUtente);
        JPanel bottomPanel = setupBottomPanel(controller, emailUtente);
        setupCenterPanel();

        panelHome.add(topPanel, BorderLayout.NORTH);
        panelHome.add(bottomPanel, BorderLayout.SOUTH);

        loadUserBoards(controller, emailUtente);

        jFrame.setVisible(true);
    }

    private void initPanelHome() {
        panelHome = new JPanel() {
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
        panelHome.setLayout(new BorderLayout());
    }

    private void initFrame() {
        jFrame = new JFrame("Home");
        jFrame.setContentPane(panelHome);
        jFrame.pack();
        jFrame.setSize(600, 400);
        jFrame.setLocationRelativeTo(null);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

        // Setup dei bottoni nella parte superiore (indietro e cronologia)
    private JPanel setupTopPanel(ApplicationManagement controller, JFrame frameVecchio, String emailUtente) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton goBackButton = new JButton("\u21E6");
        goBackButton.setFont(new Font(DIALOG_TITLE, Font.PLAIN, 24));
        setupSecondaryButton(goBackButton, Color.decode(COLOR_GRAY_HEX));
        goBackButton.addActionListener(e -> {
            jFrame.setVisible(false);
            jFrame.dispose();
            frameVecchio.setVisible(true);
            controller.logout();
        });
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBorder(new EmptyBorder(5, 10, 5, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(goBackButton);
        topPanel.add(leftPanel, BorderLayout.WEST);

        JButton historyButton = new JButton("\uD83D\uDD58");
        historyButton.setFont(new Font(DIALOG_TITLE, Font.PLAIN, 24));
        setupSecondaryButton(historyButton, Color.decode(COLOR_GRAY_HEX));
        historyButton.addActionListener(e -> {
            if (chronologyWindow == null || !chronologyWindow.getFrame().isVisible()) {
                chronologyWindow = new Chronology(controller, emailUtente);
            } else {
                chronologyWindow.getFrame().toFront();
                chronologyWindow.getFrame().requestFocus();
            }
        });
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBorder(new EmptyBorder(5, 0, 5, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(historyButton);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel setupBottomPanel(ApplicationManagement controller, String emailUtente) {
        // Pannello centrale con la lista delle board
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));
        centerWrapper.add(panelBoards);
        panelHome.add(centerWrapper, BorderLayout.CENTER);

        // Pannello inferiore con bottoni per eliminare e aggiungere board
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JButton deleteButton = new JButton("\uD83D\uDDD1\uFE0F");
        deleteButton.setFont(new Font(DIALOG_TITLE, Font.PLAIN, 24));
        setupSecondaryButton(deleteButton, Color.decode(COLOR_GRAY_HEX));
        deleteButton.addActionListener(e -> {
            if (deleteBoardFormWindow == null || !deleteBoardFormWindow.getFrame().isVisible()) {
                deleteBoardFormWindow = new DeleteBoardForm(controller, jFrame, emailUtente, this);
            } else {
                deleteBoardFormWindow.getFrame().toFront();
                deleteBoardFormWindow.getFrame().requestFocus();
            }
        });
        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deletePanel.setBorder(new EmptyBorder(5, 10, 5, 0));
        deletePanel.setOpaque(false);
        deletePanel.add(deleteButton);
        bottomPanel.add(deletePanel, BorderLayout.WEST);

        ADDButton = new JButton("+");
        ADDButton.setFont(new Font(DIALOG_TITLE, Font.BOLD, 24));
        setupPrimaryButton(ADDButton, Color.decode("#A8BDB5"));
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addPanel.setOpaque(false);
        addPanel.add(ADDButton);
        bottomPanel.add(addPanel, BorderLayout.EAST);

        panelHome.add(bottomPanel, BorderLayout.SOUTH);

        // Apre la finestra per aggiungere una nuova board
        ADDButton.addActionListener(e -> {
            if (addBoardWindow == null || !addBoardWindow.getFrame().isVisible()) {
                addBoardWindow = new AddBoard(controller, emailUtente, this);
            } else {
                addBoardWindow.getFrame().toFront();
                addBoardWindow.getFrame().requestFocus();
            }
        });

        return bottomPanel;
    }

    private void setupCenterPanel() {
        panelBoards = new JPanel();
        panelBoards.setOpaque(false);
        panelBoards.setLayout(new BoxLayout(panelBoards, BoxLayout.Y_AXIS));
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));
        centerWrapper.add(panelBoards);
        panelHome.add(centerWrapper, BorderLayout.CENTER);
    }

    private void loadUserBoards(ApplicationManagement controller, String emailUtente) {
        // Label visualizzata quando non ci sono board
        emptyLabel = new JLabel("No boards available.");
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelBoards.add(emptyLabel);

        user = controller.findUserByEmail(emailUtente);
        userBoards = controller.printBoard(emailUtente);

        if (userBoards != null && !userBoards.isEmpty()) {
            panelBoards.remove(emptyLabel);
            for (Board board : userBoards) {
                if (board != null) {
                    addBoardButton(board, controller, emailUtente);
                }
            }
        }
    }


    /**
     * Aggiunge un pulsante corrispondente a una board nella lista visualizzata.
     *
     * @param board        La board da rappresentare con il pulsante.
     * @param controller   Il controller dell'applicazione.
     * @param emailUtente  L'email dell'utente corrente.
     */
    public void addBoardButton(Board board, ApplicationManagement controller, String emailUtente) {
        if (emptyLabel != null && emptyLabel.getParent() != null) {
            panelBoards.remove(emptyLabel);
            emptyLabel = null;
        }

        // Crea il testo del pulsante con emoji rappresentativa del tipo di board
        String buttonText = getEmojiForType(board.getType()) + " " + board.getType().toString();

        JButton boardButton = new JButton(buttonText);
        boardButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16)); // font compatibile con emoji

        Dimension buttonSize = new Dimension(400, 60);
        boardButton.setPreferredSize(buttonSize);
        boardButton.setMaximumSize(buttonSize);
        boardButton.setMinimumSize(buttonSize);
        boardButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        Color baseColor = Color.decode("#A8BDB5");
        Color hoverColor = baseColor.darker();

        boardButton.setBackground(baseColor);
        boardButton.setForeground(Color.WHITE);
        boardButton.setBorder(BorderFactory.createLineBorder(baseColor, 2));
        boardButton.setFocusPainted(false);
        boardButton.setOpaque(true);

        boardButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boardButton.setBackground(hoverColor);
                boardButton.setBorder(BorderFactory.createLineBorder(hoverColor, 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                boardButton.setBackground(baseColor);
                boardButton.setBorder(BorderFactory.createLineBorder(baseColor, 2));
            }
        });

        boardButton.addActionListener(e -> new BoardGui(controller, jFrame, emailUtente, board.getType().toString()));

        panelBoards.add(Box.createVerticalStrut(10));
        panelBoards.add(boardButton);

        panelBoards.revalidate();
        panelBoards.repaint();
    }

    /**
     * Restituisce l'emoji associata al tipo di board.
     *
     * @param type Il tipo di board.
     * @return Una stringa contenente l'emoji corrispondente.
     */
    private String getEmojiForType(TypeBoard type) {
        switch (type) {
            case WORK: return "\uD83D\uDCBC";       // Valigetta per lavoro
            case UNIVERSITY: return "\uD83C\uDF93"; // Cappello da laurea per università
            case FREETIME: return "\uD83C\uDF7F";  // Bicchiere per tempo libero
            default: return "\u2753";               // Punto interrogativo come fallback
        }
    }

    /**
     * Aggiorna la lista delle board visualizzate, ricaricandola dal controller.
     *
     * @param controller  Il controller dell'applicazione.
     * @param emailUtente L'email dell'utente corrente.
     */
    public void refreshBoards(ApplicationManagement controller, String emailUtente) {
        panelBoards.removeAll();
        ArrayList<Board> userBoards = controller.printBoard(emailUtente);
        if (userBoards != null && !userBoards.isEmpty()) {
            for (Board board : userBoards) {
                if (board != null) {
                    addBoardButton(board, controller, emailUtente);
                }
            }
        } else {
            emptyLabel = new JLabel("No boards available.");
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelBoards.add(emptyLabel);
        }
        panelBoards.revalidate();
        panelBoards.repaint();
    }

    /**
     * Resetta il riferimento alla finestra di eliminazione board,
     * utile per la gestione dello stato della GUI.
     */
    public void clearDeleteBoardFormWindow() {
        deleteBoardFormWindow = null;
    }

    /**
     * Configura un JButton come pulsante principale con colori personalizzati e effetti hover.
     *
     * @param button    Il JButton da configurare.
     * @param baseColor Il colore di base del pulsante.
     */
    private void setupPrimaryButton(JButton button, Color baseColor) {
        Color hoverColor = baseColor.darker();
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(baseColor, 2));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(60, 40));
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createLineBorder(hoverColor, 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createLineBorder(baseColor, 2));
            }
        });
    }

    /**
     * Configura un JButton come pulsante secondario con colori personalizzati e effetti hover.
     *
     * @param button    Il JButton da configurare.
     * @param baseColor Il colore di base del pulsante.
     */
    private void setupSecondaryButton(JButton button, Color baseColor) {
        Color hoverColor = baseColor.darker();
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(baseColor, 2));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 40));
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createLineBorder(hoverColor, 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createLineBorder(baseColor, 2));
            }
        });
    }
}
