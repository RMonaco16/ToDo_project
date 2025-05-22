import controller.ApplicationManagement;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Home {
    public JPanel panelHome;
    private JButton ADDButton;
    private JFrame jFrame;

    public Home(ApplicationManagement controller, JFrame frameVecchio, String emailUtente) {
        // Chiudi il frame precedente
        frameVecchio.dispose();

        // Crea e mostra il nuovo frame
        jFrame = new JFrame("Home");
        jFrame.setContentPane(panelHome); // usa il nome corretto del pannello
        jFrame.setLocationRelativeTo(null); // centra la finestra
        jFrame.setSize(400, 300);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);


        ADDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddBoard addBoard = new AddBoard(controller, jFrame,emailUtente);
            }
        });
    }
}
