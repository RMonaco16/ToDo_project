package gui;

import controller.ApplicationManagement;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class Register {
    private JTextField textNickName;
    private JTextField textEmail;
    private JPasswordField passwordField1;
    private JButton registerButton;
    private JPanel panelRegister;
    private JButton loginButton;
    private JLabel imageIcon;
    private JFrame frame;

    public Register (JFrame frameChiamante, ApplicationManagement controller) {

        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/chat_image.png"));
        Image originalImage = originalIcon.getImage();

        int diameter = 100; // dimensione cerchio

        Image scaledImage = getScaledImage(originalImage, diameter, diameter);
        Image circularImage = makeCircularImage(scaledImage, diameter);

        imageIcon.setIcon(new ImageIcon(circularImage));
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
                String nickname = textNickName.getText().trim();
                String email = textEmail.getText().trim();
                String passwordStr = new String(passwordField1.getPassword()); // avoid trimming password

                // GUI validations
                if (nickname.isEmpty()) {
                    JOptionPane.showMessageDialog(panelRegister,
                            "Nickname cannot be empty.",
                            "Missing Nickname Field",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (email.isEmpty()) {
                    JOptionPane.showMessageDialog(panelRegister,
                            "Email cannot be empty.",
                            "Missing Email Field",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (passwordStr.isEmpty()) {
                    JOptionPane.showMessageDialog(panelRegister,
                            "Password cannot be empty.",
                            "Missing Password Field",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    JOptionPane.showMessageDialog(panelRegister,
                            "Invalid email. Please enter a valid email (example: name@domain.com).",
                            "Invalid Email",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!controller.isPasswordValid(passwordStr)) {
                    JOptionPane.showMessageDialog(panelRegister,
                            "Password must contain at least 8 characters, including at least one letter and one number.",
                            "Weak Password",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Create user
                User u = new User(nickname, email, passwordStr);
                boolean added = controller.addUser(u);

                if (!added) {
                    JOptionPane.showMessageDialog(panelRegister,
                            "Registration failed: email is already in use or data is invalid.",
                            "Registration Failed",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(panelRegister,
                        "Registration successful!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                frameChiamante.setVisible(true);
                frame.setVisible(false);
                frame.dispose();
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

    public Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }


    public Image makeCircularImage(Image image, int diameter) {
        BufferedImage mask = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = mask.createGraphics();
        applyQualityRenderingHints(g2);

        // Crea cerchio pieno come maschera
        g2.fill(new Ellipse2D.Double(0, 0, diameter, diameter));
        g2.dispose();

        BufferedImage output = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        g2 = output.createGraphics();
        applyQualityRenderingHints(g2);

        // Imposta maschera come clip
        g2.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));

        // Disegna immagine ridimensionata all’interno del cerchio
        g2.drawImage(image, 0, 0, diameter, diameter, null);
        g2.dispose();

        return output;
    }

    private void applyQualityRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}
