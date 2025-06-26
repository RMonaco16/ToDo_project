package gui;

import controller.ApplicationManagement;
import model.User;

import javax.swing.*;
import java.awt.*;
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

        // Applica colori della palette
        panelRegister.setBackground(Color.decode("#F9F5F0")); // Sfondo crema chiaro

        loginButton.setBackground(Color.decode("#6B7280"));   // Primario: verde salvia
        loginButton.setForeground(Color.WHITE);               // Testo bianco
        loginButton.setBorder(BorderFactory.createLineBorder(Color.decode("#6B7280"), 2));

        registerButton.setBackground(Color.decode("#A8BDB5")); // Secondario: grigio medio
        registerButton.setForeground(Color.WHITE);             // Testo bianco
        registerButton.setBorder(BorderFactory.createLineBorder(Color.decode("#A8BDB5"), 2));

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
        textNickName.setBackground(Color.WHITE);
        textEmail.setBackground(Color.WHITE);
        passwordField1.setBackground(Color.WHITE);
        textNickName.setForeground(Color.decode("#374151"));       // Testo scuro neutro
        textEmail.setForeground(Color.decode("#374151"));       // Testo scuro neutro
        passwordField1.setForeground(Color.decode("#374151"));   // Testo scuro neutro

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
