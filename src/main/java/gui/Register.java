package gui;

import controller.ApplicationManagement;
import model.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Register {
    private JTextField textField1;
    private JTextField textField2;
    private JPasswordField passwordField1;
    private JButton registerButton;
    private JPanel panelRegister;

    private JFrame frame;

    public Register (JFrame frameChiamante, ApplicationManagement controller) {
        JFrame frame = new JFrame("Register");
        frame.setContentPane(panelRegister);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(500,500);

        frame.setVisible(true);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String passwordStr = new String(passwordField1.getPassword());
                User u = new User(textField1.getText(),textField2.getText(),passwordStr);
                controller.addUser(u);
                frameChiamante.setVisible(true);
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }



}
