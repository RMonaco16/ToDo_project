package gui;

import controller.ApplicationManagement;
import model.Board;
import model.ToDo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DeleteBoardForm {
    private JPanel panelDeleteBoard;
    private JButton deleteButton;
    private JComboBox comboBoxBoards;
    private JFrame nuovoFrame;

    public DeleteBoardForm(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente,Home home){
        // Prima ottieni la lista
        ArrayList<Board> listaBoard = controller.printBoard(emailUtente);

        // Filtra le board non nulle
        ArrayList<Board> boardsNonNulle = new ArrayList<>();
        for (Board board : listaBoard) {
            if (board != null) {
                boardsNonNulle.add(board);
            }
        }

        // Se non ci sono board valide, mostra un messaggio e NON aprire il frame
        if (boardsNonNulle.isEmpty()) {
            JOptionPane.showMessageDialog(vecchioFrame, "No boards available", "No Boards", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Solo se ci sono board si crea e mostra il frame
        nuovoFrame = new JFrame("Delete Board");
        nuovoFrame.setContentPane(panelDeleteBoard);
        nuovoFrame.setSize(350, 300);
        nuovoFrame.setLocationRelativeTo(null);

        // Popola la comboBox
        comboBoxBoards.removeAllItems();
        comboBoxBoards.addItem("--");

        for (Board board : boardsNonNulle) {
            comboBoxBoards.addItem(board.getType());
        }

        // Listener del bottone
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (comboBoxBoards.getSelectedItem() == null || comboBoxBoards.getSelectedItem().equals("--")) {
                    JOptionPane.showMessageDialog(panelDeleteBoard, "Select a valid Board.", "Error", JOptionPane.WARNING_MESSAGE);
                } else {
                    eliminazioneBachecaSelezionata(controller, emailUtente,home);
                    nuovoFrame.dispose(); // Chiude la finestra dopo eliminazione
                }
            }
        });

        //listner per verificare che la finestra non venga aperta piu volte
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

        nuovoFrame.setVisible(true);
    }

    public void eliminazioneBachecaSelezionata(ApplicationManagement controller, String emailUtente,Home home){
        String boardName = comboBoxBoards.getSelectedItem().toString();
        controller.deleteBoard(emailUtente, boardName);
        home.refreshBoards(controller, emailUtente); //aggiorna la schermata chiamando il metodo di home
    }

    //metodo per restituire ad home lo stato del frame
    public JFrame getFrame() {
        return nuovoFrame;
    }


}

