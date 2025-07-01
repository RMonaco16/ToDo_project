package gui;

import controller.ApplicationManagement;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class Register {
    private static final String FRAME_TITLE = "Register";
    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 500;

    private static final String IMAGE_PATH = "/images/chat_image.png";
    private static final int IMAGE_DIAMETER = 100;

    private static final Color BACKGROUND_COLOR = Color.decode("#F9F5F0");  // Cream background
    private static final Color LOGIN_COLOR = Color.decode("#6B7280");       // Gray medium (login button)
    private static final Color REGISTER_COLOR = Color.decode("#A8BDB5");    // Green sage (register button)
    private static final Color TEXT_COLOR = Color.decode("#374151");        // Dark neutral text

    private JTextField textNickName;
    private JTextField textEmail;
    private JPasswordField passwordField1;
    private JButton registerButton;
    private JButton loginButton;
    private JPanel panelRegister;
    private JLabel imageIcon;

    private final JFrame frame;
    private final JFrame callerFrame;
    private final ApplicationManagement controller;

    public Register(JFrame callerFrame, ApplicationManagement controller) {
        this.callerFrame = callerFrame;
        this.controller = controller;
        this.frame = new JFrame(FRAME_TITLE);

        initUI();
        configureFrame();
        addListeners();
    }

    private void initUI() {
        setupImageIcon();
        styleComponents();
    }

    private void setupImageIcon() {
        ImageIcon originalIcon = new ImageIcon(getClass().getResource(IMAGE_PATH));
        Image originalImage = originalIcon.getImage();

        Image scaledImage = getScaledImage(originalImage, IMAGE_DIAMETER, IMAGE_DIAMETER);
        Image circularImage = makeCircularImage(scaledImage, IMAGE_DIAMETER);

        imageIcon.setIcon(new ImageIcon(circularImage));
    }

    private void styleComponents() {
        panelRegister.setBackground(BACKGROUND_COLOR);

        styleButton(loginButton, LOGIN_COLOR);
        styleButton(registerButton, REGISTER_COLOR);

        textNickName.setBackground(Color.WHITE);
        textEmail.setBackground(Color.WHITE);
        passwordField1.setBackground(Color.WHITE);

        textNickName.setForeground(TEXT_COLOR);
        textEmail.setForeground(TEXT_COLOR);
        passwordField1.setForeground(TEXT_COLOR);
    }

    private void styleButton(JButton button, Color baseColor) {
        final Color hoverColor = baseColor.darker();

        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(baseColor, 2));
        button.setFocusPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createLineBorder(hoverColor, 2));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createLineBorder(baseColor, 2));
            }
        });
    }

    private void addListeners() {
        registerButton.addActionListener(e -> handleRegister());
        loginButton.addActionListener(e -> switchToCallerFrame());
    }

    private void handleRegister() {
        String nickname = textNickName.getText().trim();
        String email = textEmail.getText().trim();
        String passwordStr = new String(passwordField1.getPassword()); // no trim for password

        if (nickname.isEmpty()) {
            showWarning("Nickname cannot be empty.", "Missing Nickname Field");
            return;
        }
        if (email.isEmpty()) {
            showWarning("Email cannot be empty.", "Missing Email Field");
            return;
        }
        if (passwordStr.isEmpty()) {
            showWarning("Password cannot be empty.", "Missing Password Field");
            return;
        }

        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Invalid email. Please enter a valid email (example: name@domain.com).", "Invalid Email");
            return;
        }

        if (!controller.isPasswordValid(passwordStr)) {
            showWarning("Password must contain at least 8 characters, including at least one letter and one number.",
                    "Weak Password");
            return;
        }

        User user = new User(nickname, email, passwordStr);
        boolean added = controller.addUser(user);

        if (!added) {
            showError("Registration failed: email is already in use or data is invalid.", "Registration Failed");
            return;
        }

        showInfo("Registration successful!", "Success");
        switchToCallerFrame();
    }

    private void switchToCallerFrame() {
        callerFrame.setVisible(true);
        frame.setVisible(false);
        frame.dispose();
    }

    private void showWarning(String message, String title) {
        JOptionPane.showMessageDialog(panelRegister, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(panelRegister, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message, String title) {
        JOptionPane.showMessageDialog(panelRegister, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void configureFrame() {
        frame.setContentPane(panelRegister);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public Image getScaledImage(Image srcImg, int w, int h) {
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

        g2.fill(new Ellipse2D.Double(0, 0, diameter, diameter));
        g2.dispose();

        BufferedImage output = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        g2 = output.createGraphics();
        applyQualityRenderingHints(g2);

        g2.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));
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