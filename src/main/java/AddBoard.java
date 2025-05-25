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

    private Home home;  // riferimento alla Home

    public AddBoard(ApplicationManagement controller, JFrame vecchioFrame, String emailUtente, Home home) {

        this.home = home;//creazione di home passata da parametro per metodi
        // Mostra la GUI
        JFrame nuovoFrame = new JFrame("Aggiungi bacheca");
        nuovoFrame.setContentPane(panelAddBoard);
        nuovoFrame.setSize(500, 300);
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

    //Crea una una Board e chiama il metodo per aggiungerla ad Home
    public void addNewBoard(ApplicationManagement controller, String emailUtente) {
        String descrizione = textDescription.getText().trim();
        boolean creatoCorrettamente = false;

        if (universityRadioButton.isSelected() && !descrizione.isEmpty()) {
            Board b = new Board(TypeBoard.UNIVERSITY, descrizione);
            creatoCorrettamente = controller.addBoard(emailUtente,b);
            if(creatoCorrettamente){
                JOptionPane.showMessageDialog(null, "Board created successfully!");
                home.addBoardButton(b);
            }else{
                JOptionPane.showMessageDialog(null, "This board already exists!");
            }
        } else if (workRadioButton.isSelected() && !descrizione.isEmpty()) {
            Board b = new Board(TypeBoard.WORK, descrizione);
            creatoCorrettamente = controller.addBoard(emailUtente,b);
            if(creatoCorrettamente){
                JOptionPane.showMessageDialog(null, "Board created successfully!");
                home.addBoardButton(b);
            }else{
                JOptionPane.showMessageDialog(null, "This board already exists!");
            }
        } else if (freeTimeRadioButton.isSelected() && !descrizione.isEmpty()) {
            Board b = new Board(TypeBoard.FREETIME, descrizione);
            creatoCorrettamente = controller.addBoard(emailUtente,b);
            if(creatoCorrettamente){
                JOptionPane.showMessageDialog(null, "Board created successfully!");
                home.addBoardButton(b);
            }else{
                JOptionPane.showMessageDialog(null, "This board already exists!");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Select a board and enter a description before creating!");
        }
    }
}
