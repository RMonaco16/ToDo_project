package gui;

import controller.ApplicationManagement;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 * Classe che rappresenta la schermata di registrazione.
 * Permette all'utente di inserire nickname, email e password
 * per creare un nuovo account utente.
 */
public class Register {
    private JTextField textNickName;
    private JTextField textEmail;
    private JPasswordField passwordField1;
    private JButton registerButton;
    private JPanel panelRegister;
    private JButton loginButton;
    private JLabel imageIcon;
    private JFrame frame;

    /**
     * Costruttore che inizializza la schermata di registrazione,
     * configurando l'interfaccia grafica, i colori e le azioni.
     *
     * @param frameChiamante JFrame della schermata chiamante (login) per tornare indietro.
     * @param controller     Controller per la gestione dell'applicazione.
     */
    public Register(JFrame frameChiamante, ApplicationManagement controller) {

        // Carica e imposta immagine circolare
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/chat_image.png"));
        Image originalImage = originalIcon.getImage();

        int diameter = 100; // diametro cerchio immagine

        Image scaledImage = getScaledImage(originalImage, diameter, diameter);
        Image circularImage = makeCircularImage(scaledImage, diameter);

        imageIcon.setIcon(new ImageIcon(circularImage));

        // Crea e configura la finestra di registrazione
        JFrame frame = new JFrame("Register");
        frame.setContentPane(panelRegister);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Applica colori alla GUI
        panelRegister.setBackground(Color.decode("#F9F5F0")); // sfondo crema chiaro

        loginButton.setBackground(Color.decode("#6B7280"));   // grigio medio
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createLineBorder(Color.decode("#6B7280"), 2));

        registerButton.setBackground(Color.decode("#A8BDB5")); // verde salvia chiaro
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorder(BorderFactory.createLineBorder(Color.decode("#A8BDB5"), 2));

        // Colori e hover effect per i bottoni
        Color loginColor = Color.decode("#A8BDB5");
        Color loginHover = loginColor.darker();

        Color registerColor = Color.decode("#6B7280");
        Color registerHover = registerColor.darker();

        // Effetti hover loginButton
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

        // Effetti hover registerButton
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

        // Colori campi testo e password
        textNickName.setBackground(Color.WHITE);
        textEmail.setBackground(Color.WHITE);
        passwordField1.setBackground(Color.WHITE);
        textNickName.setForeground(Color.decode("#374151"));
        textEmail.setForeground(Color.decode("#374151"));
        passwordField1.setForeground(Color.decode("#374151"));

        /**
         * Azione al click sul bottone registra:
         * valida i dati inseriti, crea un nuovo utente e lo aggiunge al controller.
         */
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nickname = textNickName.getText().trim();
                String email = textEmail.getText().trim();
                String passwordStr = new String(passwordField1.getPassword()); // non fare trim su password

                // Validazioni GUI
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

                // Creazione utente
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

                // Torna alla schermata precedente (login)
                frameChiamante.setVisible(true);
                frame.setVisible(false);
                frame.dispose();
            }
        });

        /**
         * Azione al click sul bottone login: chiude questa finestra e torna a login.
         */
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frameChiamante.setVisible(true);
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }

    /**
     * Ridimensiona un'immagine alla larghezza e altezza specificate,
     * mantenendo alta la qualità dell'interpolazione.
     *
     * @param srcImg Immagine originale da ridimensionare.
     * @param w      Larghezza desiderata.
     * @param h      Altezza desiderata.
     * @return       Immagine ridimensionata.
     */
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

    /**
     * Crea un'immagine circolare ritagliando l'immagine data in un cerchio di diametro specificato.
     *
     * @param image    Immagine da ritagliare.
     * @param diameter Diametro del cerchio finale.
     * @return         Immagine circolare ritagliata.
     */
    public Image makeCircularImage(Image image, int diameter) {
        BufferedImage mask = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = mask.createGraphics();
        applyQualityRenderingHints(g2);

        // Crea maschera circolare piena
        g2.fill(new Ellipse2D.Double(0, 0, diameter, diameter));
        g2.dispose();

        BufferedImage output = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        g2 = output.createGraphics();
        applyQualityRenderingHints(g2);

        // Applica maschera come clip per il disegno circolare
        g2.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));

        // Disegna immagine ridimensionata nel cerchio
        g2.drawImage(image, 0, 0, diameter, diameter, null);
        g2.dispose();

        return output;
    }

    /**
     * Applica suggerimenti di rendering di alta qualità al Graphics2D passato.
     *
     * @param g2 Graphics2D su cui applicare le impostazioni.
     */
    private void applyQualityRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}
