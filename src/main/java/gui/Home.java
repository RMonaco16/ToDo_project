package gui;

import controller.ApplicationManagement;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.border.EmptyBorder;

public class Home {
    public JPanel panelHome;
    private JButton ADDButton;
    private JPanel panelBoards;
    private JFrame jFrame;
    private JLabel emptyLabel; // serve per il messaggio iniziale

    public Home(ApplicationManagement controller, JFrame frameVecchio, String emailUtente) {
        frameVecchio.dispose();

        // Imposta la finestra
        jFrame = new JFrame("Home");
        jFrame.setContentPane(panelHome);
        jFrame.setLocationRelativeTo(null);
        jFrame.setSize(600, 400);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Layout principale
        panelHome.setLayout(new BorderLayout());

        // Crea un pannello superiore unico con BorderLayout
        JPanel topPanel = new JPanel(new BorderLayout());

        // ─── Bottone Torna indietro (sinistra) ────────────────
        JButton goBackButton = new JButton("⤶");
        goBackButton.setFont(new Font("Dialog", Font.PLAIN, 20));
        goBackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.setVisible(false);
                jFrame.dispose();
                frameVecchio.setVisible(true);
            }
        });
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBorder(new EmptyBorder(5, 10, 5, 0)); // top, left, bottom, right
        leftPanel.add(goBackButton);
        topPanel.add(leftPanel, BorderLayout.WEST);

        // ─── Bottone Cronologia (destra) ──────────────────────
        JButton historyButton = new JButton("\uD83D\uDD58");
        historyButton.setFont(new Font("Dialog", Font.PLAIN, 20));
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Chronology cronologia = new Chronology(controller, jFrame, emailUtente);
            }
        });
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBorder(new EmptyBorder(5, 0, 5, 10)); // top, left, bottom, right
        rightPanel.add(historyButton);
        topPanel.add(rightPanel, BorderLayout.EAST);


        // Aggiungi il pannello superiore al pannello principale
        panelHome.add(topPanel, BorderLayout.NORTH);


        // ─── Center: pannello bacheche con padding simmetrico ─────────────────
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80)); // padding laterale
        panelBoards.setLayout(new BoxLayout(panelBoards, BoxLayout.Y_AXIS));
        centerWrapper.add(panelBoards);
        panelHome.add(centerWrapper, BorderLayout.CENTER);

        // ─── South: bottone ADD in basso a destra ─────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(ADDButton);
        panelHome.add(bottomPanel, BorderLayout.SOUTH);

        // Listener per aprire la finestra AddBoard
        ADDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddBoard(controller, jFrame, emailUtente, Home.this);
            }
        });

        // Messaggio iniziale se non ci sono bacheche
        emptyLabel = new JLabel("No boards available.");
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelBoards.add(emptyLabel);

        jFrame.setVisible(true);

        //  ────────────────Recupera e mostra tutte le bacheche salvate dell'utente ────────────────
        ArrayList<Board> userBoards = controller.printBoard(emailUtente);
        if (userBoards != null && !userBoards.isEmpty()) {
            for (Board board : userBoards) {
                if (board != null) {
                    addBoardButton(board, controller, emailUtente);
                }
            }
        }

    }

    // Metodo per aggiungere un bottone per ogni bacheca ─────────────────────────────
    public void addBoardButton(Board board,ApplicationManagement controller, String emailUtente) {
        // Rimuove la label se ancora presente
        if (emptyLabel != null && emptyLabel.getParent() != null) {
            panelBoards.remove(emptyLabel);
            emptyLabel = null;
        }

        JButton boardButton = new JButton(board.getType().toString());

        // Imposta dimensioni consistenti
        Dimension buttonSize = new Dimension(400, 70);
        boardButton.setPreferredSize(buttonSize);
        boardButton.setMaximumSize(buttonSize);
        boardButton.setMinimumSize(buttonSize);

        // Allinea al centro
        boardButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        //Action listener del bottone creato
        boardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Azione da eseguire al click sul bottone della bacheca
                // Esempio: aprire la board
                new BoardGui(controller, jFrame, emailUtente, board.getType().toString());
            }
        });

        // Aggiungi spazio verticale e il bottone
        panelBoards.add(Box.createVerticalStrut(10));
        panelBoards.add(boardButton);
        panelBoards.revalidate();
        panelBoards.repaint();
    }
}

