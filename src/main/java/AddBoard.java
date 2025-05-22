import controller.ApplicationManagement;
import model.Board;
import model.TypeBoard;
import controller.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddBoard {
    private JRadioButton universityRadioButton;
    private JRadioButton workRadioButton;
    private JRadioButton freeTimeRadioButton;
    private JButton createBoardButton;
    private JTextField textDescription;
    private JPanel panelAddBoard;

    public AddBoard(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente) {
        // Mostra la GUI
        JFrame nuovoFrame = new JFrame("Aggiungi bacheca");
        nuovoFrame.setContentPane(panelAddBoard);
        nuovoFrame.setSize(300, 200);
        nuovoFrame.setLocationRelativeTo(null);
        nuovoFrame.setVisible(true);


        // Rendi i radio button esclusivi
        ButtonGroup group = new ButtonGroup();
        group.add(universityRadioButton);
        group.add(workRadioButton);
        group.add(freeTimeRadioButton);

        // Azione sul bottone
        createBoardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewBoard(controller, emailUtente);
                nuovoFrame.setVisible(false);
                nuovoFrame.dispose();
            }
        });


    }

    public void addNewBoard(ApplicationManagement controller, String emailUtente) {
        String descrizione = textDescription.getText().trim();

        if (universityRadioButton.isSelected() && !descrizione.isEmpty()) {
            Board b = new Board(TypeBoard.UNIVERSITY, descrizione);
            controller.addBoard(emailUtente,b);
            JOptionPane.showMessageDialog(null, "Board created successfully!");
        } else if (workRadioButton.isSelected() && !descrizione.isEmpty()) {
            Board b = new Board(TypeBoard.WORK, descrizione);
            controller.addBoard(emailUtente,b);
            JOptionPane.showMessageDialog(null, "Board created successfully!");
        } else if (freeTimeRadioButton.isSelected() && !descrizione.isEmpty()) {
            Board b = new Board(TypeBoard.FREETIME, descrizione);
            controller.addBoard(emailUtente,b);
            JOptionPane.showMessageDialog(null, "Board created successfully!");
        } else {
            JOptionPane.showMessageDialog(null, "Select a board and enter a description before creating!");
        }
    }
}
