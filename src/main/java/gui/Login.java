package gui;

import controller.ApplicationManagement;

import javax.swing.*;
import java.awt.*;
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



        // Applica colori della palette
        panelLogin.setBackground(Color.decode("#F9F5F0")); // Sfondo crema chiaro

        loginButton.setBackground(Color.decode("#A8BDB5"));   // Primario: verde salvia
        loginButton.setForeground(Color.WHITE);               // Testo bianco
        loginButton.setBorder(BorderFactory.createLineBorder(Color.decode("#A8BDB5"), 2));

        registerButton.setBackground(Color.decode("#6B7280")); // Secondario: grigio medio
        registerButton.setForeground(Color.WHITE);             // Testo bianco
        registerButton.setBorder(BorderFactory.createLineBorder(Color.decode("#6B7280"), 2));

        // Colori normali
        Color loginColor = Color.decode("#A8BDB5");
        Color loginHover = loginColor.darker();

        Color registerColor = Color.decode("#6B7280");
        Color registerHover = registerColor.darker();

// Login button
        loginButton.setBackground(loginColor);
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createLineBorder(loginColor, 2));
        loginButton.setFocusPainted(false);
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(loginHover);
                loginButton.setBorder(BorderFactory.createLineBorder(loginHover, 2));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(loginColor);
                loginButton.setBorder(BorderFactory.createLineBorder(loginColor, 2));
            }
        });

// Register button
        registerButton.setBackground(registerColor);
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorder(BorderFactory.createLineBorder(registerColor, 2));
        registerButton.setFocusPainted(false);
        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(registerHover);
                registerButton.setBorder(BorderFactory.createLineBorder(registerHover, 2));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(registerColor);
                registerButton.setBorder(BorderFactory.createLineBorder(registerColor, 2));
            }
        });


// (Opzionale) Colore campo testo e password
        textField1.setBackground(Color.WHITE);
        passwordField1.setBackground(Color.WHITE);
        textField1.setForeground(Color.decode("#374151"));       // Testo scuro neutro
        passwordField1.setForeground(Color.decode("#374151"));   // Testo scuro neutro



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
