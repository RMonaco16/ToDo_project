package gui;

import controller.ApplicationManagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class Sharing {

    private static final String FONT_FAMILY = "SansSerif";

    private static final String BACKGROUND_COLOR_HEX = "#F3F4F6";
    private static final String TITLE_BAR_COLOR_HEX = "#374151";
    private static final String BUTTON_COLOR_HEX = "#A8BDB5";

    private final JFrame frame;
    private  JPanel panelSharing;
    private  JTextField textEmail;
    private  JComboBox<String> comboBoxToDo;
    private  JButton shareButton;

    private final ApplicationManagement controller;
    private final String emailUtente;
    private final String tipoBacheca;
    private final Runnable onShareSuccess;

    public Sharing(ApplicationManagement controller, String emailUtente,
                   String tipoBacheca, Runnable onShareSuccess) {
        this.controller = controller;
        this.emailUtente = emailUtente;
        this.tipoBacheca = tipoBacheca;
        this.onShareSuccess = onShareSuccess;

        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setSize(450, 250);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        panelSharing = createContentPanel();
        textEmail = new JTextField();
        comboBoxToDo = new JComboBox<>();
        shareButton = new JButton("Share");

        JPanel titleBar = createTitleBar();
        JPanel buttonPanel = createButtonPanel();

        setupComponents();
        populateComboBox();

        frame.add(titleBar, BorderLayout.NORTH);
        frame.add(panelSharing, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Color.decode(TITLE_BAR_COLOR_HEX));
        titleBar.setPreferredSize(new Dimension(frame.getWidth(), 40));

        JLabel titleLabel = new JLabel(" Share ToDo");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 16));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(Color.decode(TITLE_BAR_COLOR_HEX));
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        closeButton.addActionListener(e -> frame.dispose());
        titleBar.add(closeButton, BorderLayout.EAST);

        // Drag functionality
        final Point clickPoint = new Point();
        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clickPoint.setLocation(e.getPoint());
            }
        });
        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point location = frame.getLocation();
                frame.setLocation(location.x + e.getX() - clickPoint.x,
                        location.y + e.getY() - clickPoint.y);
            }
        });

        return titleBar;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.decode(BACKGROUND_COLOR_HEX));

        JLabel emailLabel = new JLabel("User Email:");
        emailLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
        panel.add(emailLabel);

        textEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panel.add(textEmail);
        panel.add(Box.createVerticalStrut(15));

        JLabel toDoLabel = new JLabel("Select ToDo:");
        toDoLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
        panel.add(toDoLabel);

        comboBoxToDo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panel.add(comboBoxToDo);
        panel.add(Box.createVerticalStrut(20));

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(panelSharing.getBackground());

        shareButton.setBackground(Color.decode(BUTTON_COLOR_HEX));
        shareButton.setForeground(Color.WHITE);
        shareButton.setFocusPainted(false);
        shareButton.setFont(new Font(FONT_FAMILY, Font.BOLD, 13));
        shareButton.setPreferredSize(new Dimension(100, 30));
        shareButton.addActionListener(this::handleShare);

        buttonPanel.add(shareButton);
        return buttonPanel;
    }

    private void setupComponents() {
        // Nessuna configurazione aggiuntiva necessaria al momento
    }

    private void populateComboBox() {
        comboBoxToDo.removeAllItems();
        comboBoxToDo.addItem("--");

        List<String> todoList = controller.getToDoAdminNonCondivisi(emailUtente, tipoBacheca);
        for (String todo : todoList) {
            comboBoxToDo.addItem(todo);
        }
    }

    private void handleShare(ActionEvent e) {
        String email = textEmail.getText().trim();
        String selectedToDo = (String) comboBoxToDo.getSelectedItem();

        if (email.isEmpty()) {
            showWarning("Enter an email.");
            return;
        }

        if (selectedToDo == null || selectedToDo.equals("--")) {
            showWarning("Please select a valid ToDo.");
            return;
        }

        shareToDo(emailUtente, email, tipoBacheca, selectedToDo);
    }

    private void shareToDo(String emailCreator, String emailToShare, String boardType, String toDoName) {
        if (!controller.isUserAdminOfToDo(emailCreator, boardType, toDoName)) {
            showWarning("You can't share a ToDo that you don't manage.");
            return;
        }

        boolean success = controller.shareToDo(emailCreator, emailToShare, boardType, toDoName);
        if (!success) {
            showInfo("User not found or sharing error.");
        } else {
            showInfo("ToDo shared successfully!");
            frame.dispose();
            if (onShareSuccess != null) onShareSuccess.run();
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(panelSharing, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(panelSharing, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public JFrame getFrame() {
        return frame;
    }
}
