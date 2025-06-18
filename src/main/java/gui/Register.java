package gui;

import controller.ApplicationManagement;
import model.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Register {
    private JTextField textNickName;
    private JTextField textEmail;
    private JPasswordField passwordField1;
    private JButton registerButton;
    private JPanel panelRegister;
    private JButton loginButton;

    private JFrame frame;

    public Register (JFrame frameChiamante, ApplicationManagement controller) {
        JFrame frame = new JFrame("Register");
        frame.setContentPane(panelRegister);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(500,500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String passwordStr = new String(passwordField1.getPassword());
                if(textEmail.getText().isEmpty() || textNickName.getText().isEmpty() || passwordStr.equals("")){
                    JOptionPane.showMessageDialog(panelRegister,"Enter all values","missing values",JOptionPane.WARNING_MESSAGE);
                }else{
                    User u = new User(textNickName.getText(), textEmail.getText(),passwordStr);
                    boolean esistente = controller.addUser(u);
                    if(esistente == false){
                        JOptionPane.showMessageDialog(panelRegister,"email already exists","This email is already in use",JOptionPane.WARNING_MESSAGE);
                    }

                    frameChiamante.setVisible(true);
                    frame.setVisible(false);
                    frame.dispose();
                }
            }
        });
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frameChiamante.setVisible(true);
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }



}
