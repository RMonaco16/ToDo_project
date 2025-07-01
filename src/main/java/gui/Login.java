package gui;

import controller.ApplicationManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 * Classe che rappresenta la schermata di login dell'applicazione.
 * Permette all'utente di inserire le credenziali per effettuare il login
 * o di passare alla registrazione.
 */
public class Login {
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton loginButton;
    private JButton registerButton;
    private JPanel panelLogin;
    private JLabel imageIcon;
    private static JFrame frame;
    private ApplicationManagement controller;

    /**
     * Costruttore che inizializza la schermata di login,
     * configurando i componenti grafici, colori e comportamenti.
     */
    public Login() {
        controller = new ApplicationManagement();

        // Carica immagine e la ridimensiona a cerchio
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/chat_image.png"));
        Image originalImage = originalIcon.getImage();

        int diameter = 100; // diametro del cerchio

        Image scaledImage = getScaledImage(originalImage, diameter, diameter);
        Image circularImage = makeCircularImage(scaledImage, diameter);

        imageIcon.setIcon(new ImageIcon(circularImage));

        // Applica colori personalizzati ai componenti della GUI
        panelLogin.setBackground(Color.decode("#F9F5F0")); // sfondo crema chiaro

        loginButton.setBackground(Color.decode("#A8BDB5"));   // verde salvia primario
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createLineBorder(Color.decode("#A8BDB5"), 2));

        registerButton.setBackground(Color.decode("#6B7280")); // grigio secondario
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorder(BorderFactory.createLineBorder(Color.decode("#6B7280"), 2));

        // Definizione colori per hover effect
        Color loginColor = Color.decode("#A8BDB5");
        Color loginHover = loginColor.darker();

        Color registerColor = Color.decode("#6B7280");
        Color registerHover = registerColor.darker();

        // Configurazione effetti hover per loginButton
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

        // Configurazione effetti hover per registerButton
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

        // Colori campi di input testo e password
        textField1.setBackground(Color.WHITE);
        passwordField1.setBackground(Color.WHITE);
        textField1.setForeground(Color.decode("#374151"));       // testo scuro neutro
        passwordField1.setForeground(Color.decode("#374151"));   // testo scuro neutro

        // Azione al click sul bottone login: verifica credenziali
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String passwordStr = new String(passwordField1.getPassword());
                if (!(controller.login(textField1.getText(), passwordStr))) {
                    JOptionPane.showMessageDialog(loginButton, "User not found!");
                } else {
                    Home home = new Home(controller, frame, textField1.getText());
                }
            }
        });

        // Imposta il bottone login come predefinito per invio tasto Enter
        frame.getRootPane().setDefaultButton(loginButton);

        // Azione al click sul bottone registra: apre schermata registrazione
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Register register = new Register(frame, controller);
                frame.setVisible(false);
            }
        });
    }

    /**
     * Metodo main che avvia l'applicazione mostrando la schermata di login.
     *
     * @param args Argomenti da linea di comando (non utilizzati).
     */
    public static void main(String[] args) {
        frame = new JFrame("Login");
        frame.setContentPane(new Login().panelLogin);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
     * Applica suggerimenti di rendering di alta qualità al contesto grafico dato.
     *
     * @param g2 Oggetto Graphics2D su cui applicare i rendering hints.
     */
    private void applyQualityRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}
