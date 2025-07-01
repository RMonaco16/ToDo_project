package gui;

import controller.ApplicationManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 * Classe che rappresenta l'interfaccia grafica per la schermata di login.
 * Gestisce l'autenticazione dell'utente e la navigazione alla schermata principale o alla registrazione.
 */
public class Login {
    private static final String FRAME_TITLE = "Login";
    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 500;

    private static final String IMAGE_PATH = "/images/chat_image.png";
    private static final int IMAGE_DIAMETER = 100;

    private static final Color BACKGROUND_COLOR = Color.decode("#F9F5F0");
    private static final Color LOGIN_COLOR = Color.decode("#A8BDB5");
    private static final Color REGISTER_COLOR = Color.decode("#6B7280");
    private static final Color TEXT_COLOR = Color.decode("#374151");

    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton loginButton;
    private JButton registerButton;
    private JPanel panelLogin;
    private JLabel imageIcon;

    private final JFrame frame;
    private final ApplicationManagement controller;

    /**
     * Costruttore della classe Login.
     * Inizializza l'interfaccia e configura il frame principale.
     */
    public Login() {
        this.controller = new ApplicationManagement();
        this.frame = new JFrame(FRAME_TITLE);

        initUI();
        configureFrame();
    }

    /**
     * Inizializza l'interfaccia utente.
     */
    private void initUI() {
        setupImageIcon();
        styleComponents();
        addListeners();
    }

    /**
     * Carica e imposta l'immagine circolare di profilo/logo.
     */
    private void setupImageIcon() {
        ImageIcon originalIcon = new ImageIcon(getClass().getResource(IMAGE_PATH));
        Image originalImage = originalIcon.getImage();

        Image scaledImage = getScaledImage(originalImage, IMAGE_DIAMETER, IMAGE_DIAMETER);
        Image circularImage = makeCircularImage(scaledImage, IMAGE_DIAMETER);

        imageIcon.setIcon(new ImageIcon(circularImage));
    }

    /**
     * Applica lo stile grafico ai componenti dell'interfaccia.
     */
    private void styleComponents() {
        panelLogin.setBackground(BACKGROUND_COLOR);

        styleButton(loginButton, LOGIN_COLOR);
        styleButton(registerButton, REGISTER_COLOR);

        textField1.setBackground(Color.WHITE);
        textField1.setForeground(TEXT_COLOR);

        passwordField1.setBackground(Color.WHITE);
        passwordField1.setForeground(TEXT_COLOR);
    }

    /**
     * Applica lo stile a un bottone, inclusi effetti hover.
     *
     * @param button    Il bottone da stilizzare.
     * @param baseColor Il colore di base del bottone.
     */
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

    /**
     * Aggiunge gli event listener ai bottoni.
     */
    private void addListeners() {
        loginButton.addActionListener(e -> handleLogin());

        registerButton.addActionListener(e -> {
            new Register(frame, controller);
            frame.setVisible(false);
        });

        frame.getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Gestisce il login dell'utente.
     * Mostra un messaggio d'errore se le credenziali sono errate,
     * altrimenti apre la schermata principale (Home).
     */
    private void handleLogin() {
        String passwordStr = new String(passwordField1.getPassword());
        if (!controller.login(textField1.getText(), passwordStr)) {
            JOptionPane.showMessageDialog(loginButton, "User not found!");
        } else {
            new Home(controller, frame, textField1.getText());
        }
    }

    /**
     * Configura le proprietà principali del frame.
     */
    private void configureFrame() {
        frame.setContentPane(panelLogin);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Ridimensiona un'immagine alle dimensioni specificate.
     *
     * @param srcImg L'immagine sorgente.
     * @param w      Larghezza desiderata.
     * @param h      Altezza desiderata.
     * @return L'immagine ridimensionata.
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
     * Crea un'immagine circolare a partire da un'immagine quadrata.
     *
     * @param image    L'immagine di input.
     * @param diameter Il diametro del cerchio.
     * @return L'immagine circolare risultante.
     */
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

    /**
     * Applica rendering di alta qualità a un oggetto Graphics2D.
     *
     * @param g2 Il contesto grafico su cui applicare le impostazioni.
     */
    private void applyQualityRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /**
     * Metodo principale per avviare l'interfaccia utente del login.
     *
     * @param args Argomenti della linea di comando (non utilizzati).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}
