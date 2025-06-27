package gui;

import controller.ApplicationManagement;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Home {
    public JPanel panelHome;
    private JButton ADDButton;
    private JPanel panelBoards;
    private JFrame jFrame;
    private JLabel emptyLabel;
    private AddBoard addBoardWindow = null;
    private Chronology chronologyWindow = null;
    private DeleteBoardForm deleteBoardFormWindow = null;

    private ArrayList<Board> userBoards;
    private User user;

    public Home(ApplicationManagement controller, JFrame frameVecchio, String emailUtente) {
        frameVecchio.dispose();

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

        panelBoards = new JPanel();
        panelBoards.setOpaque(false);
        panelBoards.setLayout(new BoxLayout(panelBoards, BoxLayout.Y_AXIS));

        jFrame = new JFrame("Home");
        jFrame.setContentPane(panelHome);
        jFrame.pack();
        jFrame.setSize(600, 400);
        jFrame.setLocationRelativeTo(null);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JButton goBackButton = new JButton("\u21E6");
        goBackButton.setFont(new Font("Dialog", Font.PLAIN, 24));
        setupSecondaryButton(goBackButton, Color.decode("#6B7280"));
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
        historyButton.setFont(new Font("Dialog", Font.PLAIN, 24));
        setupSecondaryButton(historyButton, Color.decode("#6B7280"));
        historyButton.addActionListener(e -> {
            if (chronologyWindow == null || !chronologyWindow.getFrame().isVisible()) {
                chronologyWindow = new Chronology(controller, jFrame, emailUtente);
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

        panelHome.add(topPanel, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));
        centerWrapper.add(panelBoards);
        panelHome.add(centerWrapper, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JButton deleteButton = new JButton("\uD83D\uDDD1\uFE0F");
        deleteButton.setFont(new Font("Dialog", Font.PLAIN, 24));
        setupSecondaryButton(deleteButton, Color.decode("#6B7280"));
        deleteButton.addActionListener(e -> {
            if (deleteBoardFormWindow == null || !deleteBoardFormWindow.getFrame().isVisible()) {
                deleteBoardFormWindow = new DeleteBoardForm(controller, jFrame, emailUtente, Home.this);
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
        ADDButton.setFont(new Font("Dialog", Font.BOLD, 24));
        setupPrimaryButton(ADDButton, Color.decode("#A8BDB5"));
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addPanel.setOpaque(false);
        addPanel.add(ADDButton);
        bottomPanel.add(addPanel, BorderLayout.EAST);

        panelHome.add(bottomPanel, BorderLayout.SOUTH);

        ADDButton.addActionListener(e -> {
            if (addBoardWindow == null || !addBoardWindow.getFrame().isVisible()) {
                addBoardWindow = new AddBoard(controller, jFrame, emailUtente, Home.this);
            } else {
                addBoardWindow.getFrame().toFront();
                addBoardWindow.getFrame().requestFocus();
            }
        });

        emptyLabel = new JLabel("No boards available.");
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelBoards.add(emptyLabel);

        user = controller.findUserByEmail(emailUtente);
        userBoards = controller.printBoard(emailUtente);

        if (userBoards != null && !userBoards.isEmpty()) {
            for (Board board : userBoards) {
                if (board != null) {
                    addBoardButton(board, controller, emailUtente);
                }
            }
        }

        jFrame.setVisible(true);
    }

    public void addBoardButton(Board board, ApplicationManagement controller, String emailUtente) {
        if (emptyLabel != null && emptyLabel.getParent() != null) {
            panelBoards.remove(emptyLabel);
            emptyLabel = null;
        }

        // Creo il testo con emoji
        String buttonText = getEmojiForType(board.getType()) + " " + board.getType().toString();

        JButton boardButton = new JButton(buttonText);
        boardButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16)); // font emoji-friendly

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

    private String getEmojiForType(TypeBoard type) {
        switch (type) {
            case WORK: return "\uD83D\uDCBC";       //
            case UNIVERSITY: return "\uD83C\uDF93"; //
            case FREETIME: return "\uD83C\uDF7F";  //
            default: return "\u2753";               //  (punto interrogativo come fallback)
        }
    }

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

    public void clearDeleteBoardFormWindow() {
        deleteBoardFormWindow = null;
    }

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
