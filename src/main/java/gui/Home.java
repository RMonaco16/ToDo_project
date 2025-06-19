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
    private AddBoard addBoardWindow = null;//mi istanzio addBoard per poi verificare se gia aperta
    private Chronology chronologyWindow = null;//stessa cosa per controllo gia aperta
    private DeleteBoardForm deleteBoardFormWindow = null;//variabile per controllare stato deleteBoard

    private ArrayList<Board> userBoards;
    private User user;

    public Home(ApplicationManagement controller, JFrame frameVecchio, String emailUtente) {
        frameVecchio.dispose();

        jFrame = new JFrame("Home");
        jFrame.setContentPane(panelHome);
        jFrame.pack();
        jFrame.setSize(600, 400);
        jFrame.setLocationRelativeTo(null);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panelHome.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());

        JButton goBackButton = new JButton("⤶");
        goBackButton.setFont(new Font("Dialog", Font.PLAIN, 20));
        goBackButton.addActionListener(e -> {
            jFrame.setVisible(false);
            jFrame.dispose();
            frameVecchio.setVisible(true);
            controller.logout();
        });
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBorder(new EmptyBorder(5, 10, 5, 0));
        leftPanel.add(goBackButton);
        topPanel.add(leftPanel, BorderLayout.WEST);

        JButton historyButton = new JButton("\uD83D\uDD58");
        historyButton.setFont(new Font("Dialog", Font.PLAIN, 20));
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
        rightPanel.add(historyButton);
        topPanel.add(rightPanel, BorderLayout.EAST);

        panelHome.add(topPanel, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));
        panelBoards.setLayout(new BoxLayout(panelBoards, BoxLayout.Y_AXIS));
        centerWrapper.add(panelBoards);
        panelHome.add(centerWrapper, BorderLayout.CENTER);

        // BOTTOM PANEL con DELETE a sinistra e ADD a destra
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Bottone DELETE (a sinistra)
        JButton deleteButton = new JButton("🗑️");
        deleteButton.setFont(new Font("Dialog", Font.PLAIN, 20));

        // apertura della finestra e Condizione per non farla aprire piu volte
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
        deletePanel.add(deleteButton);
        bottomPanel.add(deletePanel, BorderLayout.WEST);

        // Bottone ADD (a destra)
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addPanel.add(ADDButton);
        bottomPanel.add(addPanel, BorderLayout.EAST);

        panelHome.add(bottomPanel, BorderLayout.SOUTH);

        // apertura della finestra per aggiungere una bacheca e Condizione per non farla aprire piu volte
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

        // Recupera user e sue board
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

    // Metodo per aggiungere un bottone per ogni bacheca ─────────────────────────────
    public void addBoardButton(Board board, ApplicationManagement controller, String emailUtente) {
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
                new BoardGui(controller, jFrame, emailUtente, board.getType().toString());
            }
        });

        // Aggiungi spazio verticale e il bottone
        panelBoards.add(Box.createVerticalStrut(10));
        panelBoards.add(boardButton);

        panelBoards.revalidate();
        panelBoards.repaint();
    }

    //-------- refresh delle boards aggiornato dopo averne rimossa una ----------
    public void refreshBoards(ApplicationManagement controller, String emailUtente) {
        panelBoards.removeAll(); // Rimuove tutto

        ArrayList<Board> userBoards = controller.printBoard(emailUtente);
        if (userBoards != null && !userBoards.isEmpty()) {
            for (Board board : userBoards) {
                if (board != null) {
                    addBoardButton(board, controller, emailUtente);//reinserisce quelle esistenti
                }
            }
        }
        panelBoards.revalidate();//aggiorna il layout
        panelBoards.repaint();// aggiorna la grafica visiva
    }

    //setta deleteBoardFormWindow a null.
    public void clearDeleteBoardFormWindow() {
        deleteBoardFormWindow = null;
    }

}
