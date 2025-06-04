package gui;

import controller.ApplicationManagement;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login {
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton loginButton;
    private JButton registerButton;
    private JPanel panelLogin;
    private static JFrame frame;
    private ApplicationManagement controller;

    public Login() {
        controller = new ApplicationManagement();

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String passwordStr = new String(passwordField1.getPassword());
                if(!(controller.login(textField1.getText(), passwordStr))){
                    JOptionPane.showMessageDialog(loginButton,"User not found!");
                }else{
                    Home home = new Home(controller,frame,textField1.getText());
                }
            }

        });

        frame.getRootPane().setDefaultButton(loginButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Register register = new Register(frame,controller);
                frame.setVisible(false);
            }
        });
    }

    public static void main(String[] args) {
        frame = new JFrame("Login");
        frame.setContentPane(new Login().panelLogin);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(500,500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}
