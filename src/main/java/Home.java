import controller.ApplicationManagement;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Home {
    public JPanel panelHome;
    private JButton ADDButton;
    private JPanel panelBoards;
    private JFrame jFrame;

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

        // Pannello centrale per le bacheche con padding simmetrico
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80)); // padding laterale
        panelBoards.setLayout(new BoxLayout(panelBoards, BoxLayout.Y_AXIS));
        centerWrapper.add(panelBoards); // pannello con i bottoni
        panelHome.add(centerWrapper, BorderLayout.CENTER);

        // Pannello in basso per il bottone ADD, allineato a destra
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(ADDButton);
        panelHome.add(bottomPanel, BorderLayout.SOUTH);

        // Listener per aprire la finestra AddBoard
        ADDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddBoard addBoard = new AddBoard(controller, jFrame, emailUtente, Home.this);
            }
        });

        jFrame.setVisible(true);
    }

    // Metodo per aggiungere un bottone per ogni bacheca
    public void addBoardButton(Board board) {
        JButton boardButton = new JButton(board.getType().toString());

        // Imposta dimensioni consistenti
        Dimension buttonSize = new Dimension(400, 70); // Modifica qui la dimensione
        boardButton.setPreferredSize(buttonSize);
        boardButton.setMaximumSize(buttonSize);
        boardButton.setMinimumSize(buttonSize);

        // Allinea al centro
        boardButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Aggiungi bottone al pannello
        panelBoards.add(Box.createVerticalStrut(10)); // spazio verticale
        panelBoards.add(boardButton);
        panelBoards.revalidate();
        panelBoards.repaint();
    }

}
