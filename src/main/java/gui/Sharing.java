package gui;

import controller.ApplicationManagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Finestra grafica per condividere un ToDo con un altro utente.
 * Permette all'utente amministratore di selezionare un ToDo non condiviso
 * e indicare l'email dell'utente con cui condividerlo.
 */
public class Sharing {

    private JFrame nuovoFrame;
    private JPanel panelSharing;
    private JTextField textEmail;
    private JButton shareButton;
    private JComboBox<String> comboBoxToDo;

    private ApplicationManagement controller;
    private String emailUtente;
    private String tipoBacheca;
    private Runnable onShareSuccess;

    /**
     * Costruttore che inizializza la finestra di condivisione.
     *
     * @param controller     Controller per la gestione dell'applicazione.
     * @param emailUtente    Email dell'utente corrente (amministratore).
     * @param vecchioFrame   Frame chiamante (non usato direttamente qui).
     * @param tipoBacheca    Tipo di bacheca di cui condividere il ToDo.
     * @param onShareSuccess Runnable eseguito al termine con successo della condivisione.
     */
    public Sharing(ApplicationManagement controller, String emailUtente, JFrame vecchioFrame, String tipoBacheca, Runnable onShareSuccess) {
        this.controller = controller;
        this.emailUtente = emailUtente;
        this.tipoBacheca = tipoBacheca;
        this.onShareSuccess = onShareSuccess;

        // Configura finestra
        nuovoFrame = new JFrame();
        nuovoFrame.setUndecorated(true);
        nuovoFrame.setSize(450, 250);
        nuovoFrame.setLocationRelativeTo(null);
        nuovoFrame.setLayout(new BorderLayout());

        // Barra titolo personalizzata
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Color.decode("#374151"));
        titleBar.setPreferredSize(new Dimension(nuovoFrame.getWidth(), 40));

        JLabel titleLabel = new JLabel(" Share ToDo");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(Color.decode("#374151"));
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        closeButton.addActionListener(e -> {
            nuovoFrame.setVisible(false);
            nuovoFrame.dispose();
        });
        titleBar.add(closeButton, BorderLayout.EAST);

        // Drag per spostare la finestra
        final Point clickPoint = new Point();
        titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                clickPoint.setLocation(e.getPoint());
            }
        });
        titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                Point location = nuovoFrame.getLocation();
                nuovoFrame.setLocation(location.x + e.getX() - clickPoint.x,
                        location.y + e.getY() - clickPoint.y);
            }
        });

        // Pannello principale contenuti
        panelSharing = new JPanel();
        panelSharing.setLayout(new BoxLayout(panelSharing, BoxLayout.Y_AXIS));
        panelSharing.setBorder(new EmptyBorder(20, 30, 20, 30));
        panelSharing.setBackground(Color.decode("#F3F4F6"));

        // Campo email destinatario
        JLabel emailLabel = new JLabel("User Email:");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panelSharing.add(emailLabel);

        textEmail = new JTextField();
        textEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panelSharing.add(textEmail);
        panelSharing.add(Box.createVerticalStrut(15));

        // ComboBox per selezionare ToDo
        JLabel toDoLabel = new JLabel("Select ToDo:");
        toDoLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panelSharing.add(toDoLabel);

        comboBoxToDo = new JComboBox<>();
        comboBoxToDo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panelSharing.add(comboBoxToDo);
        panelSharing.add(Box.createVerticalStrut(20));

        // Bottone per condividere
        shareButton = new JButton("Share");
        shareButton.setBackground(Color.decode("#A8BDB5"));
        shareButton.setForeground(Color.WHITE);
        shareButton.setFocusPainted(false);
        shareButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        shareButton.setPreferredSize(new Dimension(100, 30));
        shareButton.addActionListener(this::handleShare);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(panelSharing.getBackground());
        buttonPanel.add(shareButton);

        // Aggiunta componenti al frame
        nuovoFrame.add(titleBar, BorderLayout.NORTH);
        nuovoFrame.add(panelSharing, BorderLayout.CENTER);
        nuovoFrame.add(buttonPanel, BorderLayout.SOUTH);

        // Popola la ComboBox con i ToDo disponibili
        popolaComboBox();

        nuovoFrame.setVisible(true);
    }

    /**
     * Popola la ComboBox con la lista dei ToDo non ancora condivisi
     * che l'utente amministratore può condividere.
     */
    private void popolaComboBox() {
        ArrayList<String> listaToDo = controller.getToDoAdminNonCondivisi(emailUtente, tipoBacheca);

        comboBoxToDo.removeAllItems();
        comboBoxToDo.addItem("--");

        for (String todo : listaToDo) {
            comboBoxToDo.addItem(todo);
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Share":
     * verifica validità dati e avvia la condivisione.
     *
     * @param e Evento di azione generato dal pulsante.
     */
    private void handleShare(ActionEvent e) {
        String email = textEmail.getText();
        String selectedToDo = (String) comboBoxToDo.getSelectedItem();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(panelSharing, "Inserisci un'email.", "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedToDo == null || selectedToDo.equals("--")) {
            JOptionPane.showMessageDialog(panelSharing, "Seleziona un ToDo valido.", "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        condividiToDo(emailUtente, email, tipoBacheca, selectedToDo);
    }

    /**
     * Esegue la condivisione di un ToDo da un utente creatore a un altro utente.
     * Controlla se l'utente creatore è effettivamente amministratore del ToDo.
     *
     * @param emailCreatore    Email dell'utente che condivide il ToDo.
     * @param emailDaCondividere Email dell'utente destinatario della condivisione.
     * @param bacheca          Tipo di bacheca.
     * @param toDoName         Nome del ToDo da condividere.
     */
    public void condividiToDo(String emailCreatore, String emailDaCondividere, String bacheca, String toDoName) {
        if (!controller.isUserAdminOfToDo(emailCreatore, bacheca, toDoName)) {
            JOptionPane.showMessageDialog(panelSharing, "Non puoi condividere un ToDo che non gestisci.", "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean risultato = controller.shareToDo(emailCreatore, emailDaCondividere, bacheca, toDoName);

        if (!risultato) {
            JOptionPane.showMessageDialog(panelSharing, "Utente non trovato o errore nella condivisione.", "Errore", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(panelSharing, "ToDo condiviso con successo!", "Condivisione completata", JOptionPane.INFORMATION_MESSAGE);
            nuovoFrame.dispose();
            if (onShareSuccess != null) onShareSuccess.run();
        }
    }

    /**
     * Restituisce il frame creato per la condivisione.
     *
     * @return JFrame della finestra di condivisione.
     */
    public JFrame getFrame() {
        return nuovoFrame;
    }
}
