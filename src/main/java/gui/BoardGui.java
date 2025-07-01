package gui;

import controller.ApplicationManagement;
import dao.CheckListDAO;
import dao.ToDoDAO;
import db.DatabaseConnection;
import model.Activity;
import model.Board;
import model.CheckList;
import model.ToDo;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.awt.Color;
import org.jdesktop.swingx.prompt.PromptSupport;

public class BoardGui {
    private JButton undoButton;
    private JButton addButton;
    private JButton deleteButton;
    private JPanel BoardGui;
    private JPanel panelToDoMain;
    private JButton shareButton;
    private JButton searchButton;
    private JTextField filter;
    private JLabel textFilter;
    private JFrame frame;
    private JScrollPane scrollPanelToDo;
    private JButton buttonTodayFilter;
    private JComboBox<String> comboBoxSortFilter;
    private Sharing sharingWindow = null;//per verificare apertura finestre
    private JFrame sharingInfoFrame = null;
    private JDialog dialog = null;
    private Color color;
    private JLabel stateField = new JLabel("❌");
    public static final String SANS_SERIF_FONT = "SansSerif";
    public static final String ERROR_MESSAGE = "Errore";

    public static final Color DONE_BUTTON_COLOR = Color.decode("#A8BDB5");
    public static final Color LIGHT_GRAY_BACKGROUND = Color.decode("#F3F4F6");
    public static final Color DARK_GRAY_TEXT = Color.decode("#374151");

    public BoardGui(ApplicationManagement controller, JFrame vecchioFrame, String email, String nameBoard) {
        setupMainFrame(nameBoard, vecchioFrame);
        setupToDoPanel();
        setupSortFilter();

        setupButtonsListeners(controller, email, nameBoard, vecchioFrame);

        updateToDoList(controller, email, nameBoard);

        styleAllButtons();
    }

    private void setupMainFrame(String nameBoard, JFrame vecchioFrame) {
        frame = new JFrame(nameBoard);
        frame.setContentPane(BoardGui);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        vecchioFrame.setVisible(false);
        frame.setVisible(true);
    }

    private void setupToDoPanel() {
        panelToDoMain.setLayout(new BoxLayout(panelToDoMain, BoxLayout.Y_AXIS));
        scrollPanelToDo.setViewportView(panelToDoMain);
        scrollPanelToDo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanelToDo.getVerticalScrollBar().setUnitIncrement(16);
    }

    private void setupSortFilter() {
        comboBoxSortFilter.addItem("-Show all ToDos-");
        comboBoxSortFilter.addItem("sort alphabetically");
        comboBoxSortFilter.addItem("Sort by deadline");
    }

    private void setupButtonsListeners(ApplicationManagement controller, String email, String nameBoard, JFrame vecchioFrame) {
        undoButton.addActionListener(e -> {
            frame.setVisible(false);
            vecchioFrame.setVisible(true);
            frame.dispose();
        });

        addButton.addActionListener(e -> showAddToDoDialog(controller, email, nameBoard));

        shareButton.addActionListener(e -> openShareWindow(controller, email, vecchioFrame, nameBoard));

        deleteButton.addActionListener(e -> showDeleteToDoDialog(controller, email, nameBoard));

        searchButton.addActionListener(src -> updateToDoList(controller, email, nameBoard));

        buttonTodayFilter.addActionListener(todayFilter -> {
            filter.setText("todayFilter");
            updateToDoList(controller, email, nameBoard);
        });
    }

    private void showAddToDoDialog(ApplicationManagement controller, String email, String nameBoard) {
// Creo la dialog personalizzata senza decorazioni (stile uniforme)
        JDialog dialogAddToDo = new JDialog(frame, true);
        dialogAddToDo.setUndecorated(true);
        dialogAddToDo.setSize(370, 210);
        dialogAddToDo.setLocationRelativeTo(frame);
        dialogAddToDo.setLayout(new BorderLayout());

        // Barra titolo personalizzata
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(DARK_GRAY_TEXT);
        titleBar.setPreferredSize(new Dimension(dialogAddToDo.getWidth(), 40));

        JLabel titleLabel = new JLabel(" Add New ToDo");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 16));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(DARK_GRAY_TEXT);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 14));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(ev -> dialogAddToDo.dispose());
        titleBar.add(closeButton, BorderLayout.EAST);

        // Drag per spostare la finestra
        final Point clickPoint = new Point();
        titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                clickPoint.setLocation(e.getPoint());
            }
        });
        titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                Point location = dialogAddToDo.getLocation();
                dialogAddToDo.setLocation(location.x + e.getX() - clickPoint.x,
                        location.y + e.getY() - clickPoint.y);
            }
        });

        // Pannello principale
        JPanel panelAddToDo = new JPanel();
        panelAddToDo.setLayout(new BoxLayout(panelAddToDo, BoxLayout.Y_AXIS));
        panelAddToDo.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panelAddToDo.setBackground(LIGHT_GRAY_BACKGROUND);

        panelAddToDo.add(Box.createRigidArea(new Dimension(0, 15)));

        // Pannello input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setOpaque(false);

        JLabel label = new JLabel("Name:");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font(SANS_SERIF_FONT, Font.PLAIN, 14));
        label.setForeground(Color.DARK_GRAY);

        JTextField nameToDo = new JTextField();
        nameToDo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        nameToDo.setAlignmentX(Component.CENTER_ALIGNMENT);

        inputPanel.add(label);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        inputPanel.add(nameToDo);

        panelAddToDo.add(inputPanel);
        panelAddToDo.add(Box.createRigidArea(new Dimension(0, 20)));

        // Pannello bottoni
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton doneButton = new JButton("Done");
        doneButton.setBackground(DONE_BUTTON_COLOR); // rosso tenue
        doneButton.setForeground(Color.WHITE);
        doneButton.setFocusPainted(false);
        doneButton.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 12));
        doneButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        doneButton.addActionListener(ae -> {
            try {
                String nameToDoText = nameToDo.getText();

                CheckList checkList = new CheckList();
                ToDo todo = new ToDo(nameToDoText, false, checkList, false, email);

                // Controllo duplicati
                if (!controller.addToDoInBoard(email, nameBoard, todo)) {
                    JOptionPane.showMessageDialog(dialogAddToDo, "Name already used", ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE);
                    return;
                }

                updateToDoList(controller, email, nameBoard);
                dialogAddToDo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogAddToDo, ERROR_MESSAGE, ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(doneButton);
        panelAddToDo.add(buttonPanel);

        // Composizione finale
        dialogAddToDo.add(titleBar, BorderLayout.NORTH);
        dialogAddToDo.add(panelAddToDo, BorderLayout.CENTER);
        dialogAddToDo.setMinimumSize(new Dimension(370, 210));
        dialogAddToDo.setVisible(true);
    }

    private void openShareWindow(ApplicationManagement controller, String email, JFrame vecchioFrame, String nameBoard) {
        if (sharingWindow == null || !sharingWindow.getFrame().isVisible()) {
            sharingWindow = new Sharing(controller, email, nameBoard, () -> {
                updateToDoList(controller, email, nameBoard);
            });
        } else {
            sharingWindow.getFrame().toFront();
            sharingWindow.getFrame().requestFocus();
        }
    }

    private void showDeleteToDoDialog(ApplicationManagement controller, String email, String nameBoard) {
// Creo la dialog personalizzata senza decorazioni (senza bordi/nativo)
        JDialog dialogDeleteToDo = new JDialog(frame, true);
        dialogDeleteToDo.setUndecorated(true);
        dialogDeleteToDo.setSize(370, 210);
        dialogDeleteToDo.setLocationRelativeTo(frame);
        dialogDeleteToDo.setLayout(new BorderLayout());

        // Barra titolo personalizzata
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(DARK_GRAY_TEXT);
        titleBar.setPreferredSize(new Dimension(dialogDeleteToDo.getWidth(), 40));

        JLabel titleLabel = new JLabel(" Delete ToDo");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 16));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(DARK_GRAY_TEXT);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 14));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(ev -> dialogDeleteToDo.dispose());
        titleBar.add(closeButton, BorderLayout.EAST);

        // Drag per spostare la finestra
        final Point clickPoint = new Point();
        titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                clickPoint.setLocation(e.getPoint());
            }
        });
        titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                Point location = dialogDeleteToDo.getLocation();
                dialogDeleteToDo.setLocation(location.x + e.getX() - clickPoint.x,
                        location.y + e.getY() - clickPoint.y);
            }
        });

        // Pannello principale con sfondo uniforme #F3F4F6
        JPanel panelDeleteToDo = new JPanel();
        panelDeleteToDo.setLayout(new BoxLayout(panelDeleteToDo, BoxLayout.Y_AXIS));
        panelDeleteToDo.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panelDeleteToDo.setBackground(LIGHT_GRAY_BACKGROUND);

        // Titolo interno grande e in grassetto (sotto barra titolo)

        panelDeleteToDo.add(Box.createRigidArea(new Dimension(0, 15)));

        // Pannello selezione To-Do
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        selectionPanel.setOpaque(false);

        JLabel label = new JLabel("Select a ToDo to delete:");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font(SANS_SERIF_FONT, Font.PLAIN, 14));
        label.setForeground(Color.DARK_GRAY);

        JComboBox<String> toDoComboBox = new JComboBox<>();
        toDoComboBox.setMaximumSize(new Dimension(250, 25));
        toDoComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        toDoComboBox.setBackground(Color.WHITE);
        toDoComboBox.setForeground(Color.BLACK);
        toDoComboBox.addItem("");

        for (ToDo t : controller.printTodo(email, nameBoard)) {
            toDoComboBox.addItem(t.getTitle());
        }

        selectionPanel.add(label);
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        selectionPanel.add(toDoComboBox);
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        panelDeleteToDo.add(selectionPanel);

        // Pannello bottoni
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(220, 53, 69)); // rosso tenue
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 12));
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        deleteBtn.addActionListener(ev -> {
            String selected = (String) toDoComboBox.getSelectedItem();
            if (selected != null && !selected.trim().isEmpty()) {
                controller.deleteToDo(email, nameBoard, selected);
                updateToDoList(controller, email, nameBoard);
                dialogDeleteToDo.dispose();
            } else {
                JOptionPane.showMessageDialog(dialogDeleteToDo, "Please select a ToDo to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(deleteBtn);
        panelDeleteToDo.add(buttonPanel);

        // Montaggio componenti nella dialog
        dialogDeleteToDo.add(titleBar, BorderLayout.NORTH);
        dialogDeleteToDo.add(panelDeleteToDo, BorderLayout.CENTER);

        dialogDeleteToDo.setMinimumSize(new Dimension(370, 210));
        dialogDeleteToDo.setVisible(true);

        toDoComboBox.requestFocusInWindow();
    }

    private void styleAllButtons() {
        styleButton(addButton);
        styleButton(shareButton);
        styleButton(deleteButton);
        styleSmallButton(undoButton);
        styleSmallButton(searchButton);
        styleSmallButton(buttonTodayFilter);
        buttonTodayFilter.setPreferredSize(new Dimension(100, 40));
        buttonTodayFilter.setMargin(new Insets(10, 15, 10, 15));
    }

    //--metodo che serve ad aggiornare la grafica
    private void updateToDoList(ApplicationManagement controller, String email, String nameBoard) {
        panelToDoMain.removeAll();

        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        rowPanel.setBackground(Color.decode("#DAD2C8"));

        int count = 0;

        //-stampa dei to-do
        for (ToDo t : controller.orderedVisibleToDos(comboBoxSortFilter.getSelectedItem().toString(),controller.getVisibleToDos(controller.findUserByEmail(email), nameBoard,filter.getText()))) {

            JPanel titleToDo = new JPanel(new BorderLayout());
            JPanel ToDoButton = new JPanel();

            //-verifica se il to-do è stato condiviso e in tal caso gli aggiunge il pulsante SharingInformation--
            setupSharingButtonIfNeeded(t, ToDoButton, controller, frame, email, nameBoard);


            JLabel titleLabel = new JLabel("ToDo: " + t.getTitle());
            titleLabel.setFont(new Font(null, Font.BOLD, 20));

            //--Se il to-do è scaduto il nome del colore appare in ROSSO
            if (t.getExpiration() != null && t.getExpiration().isBefore(LocalDate.now())) {
                titleLabel.setForeground(Color.RED);
            }

            //--Bottone delle proprietà del to-do
            JButton propertiesButton = new JButton("≡");
            propertiesButton.setFont(new Font(null, Font.BOLD, 20));
            propertiesButton.setBorderPainted(false);
            propertiesButton.setContentAreaFilled(false);
            propertiesButton.setFocusPainted(false);
            propertiesButton.setMargin(new Insets(4, 8, 4, 8)); // margini piccoli

            Color hoverBackground = new Color(220, 220, 220); // grigio chiaro

            propertiesButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    propertiesButton.setContentAreaFilled(true);
                    propertiesButton.setBackground(hoverBackground);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    propertiesButton.setContentAreaFilled(false);
                }
            });


            ToDoButton.add(propertiesButton);

                titleToDo.add(titleLabel, BorderLayout.WEST);
                titleToDo.add(ToDoButton, BorderLayout.EAST);

            //--creazione di un tableModel per l'inserimento delle attività del to-do--
            DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Activity", "State"}, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 1 ? Boolean.class : String.class;
                }
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 1;
                }
            };


            addTableModelListener(tableModel, controller, email, nameBoard, t, stateField);


            //--Popolamento del tableMOdel con le attività--
            ArrayList<Activity> activities = controller.printActs(t.getOwnerEmail(), nameBoard, t.getTitle());
            for (Activity a : activities) {
                tableModel.addRow(new Object[]{a.getName(), a.getState()});
            }

            JTable table = new JTable(tableModel);

            // Imposta larghezza preferita delle colonne
            table.getColumnModel().getColumn(0).setPreferredWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(50);

            table.setFillsViewportHeight(true);
            JScrollPane tableScroll = new JScrollPane(table);

            int rowCount = table.getRowCount();

            int maxHeight = 200; // altezza fissa massima visibile per la tabella
            int totalHeight = rowCount * table.getRowHeight();

            int scrollHeight = Math.min(totalHeight, maxHeight);

            // Limita l'altezza visibile a maxHeight, aggiunge scrollbar se necessario
            table.setPreferredScrollableViewportSize(new Dimension(380, scrollHeight));
            tableScroll.setPreferredSize(new Dimension(400, scrollHeight + 20));

            JPanel todoPanel = new JPanel(new BorderLayout(5, 5));


            Color colorBackground = controller.getColorOfToDo(nameBoard, email, t.getTitle(), t.isCondiviso());

            // Colora solo lo sfondo del pannello.
            // Il to-do viene colorato solo in parte per fornire una visione chiara del to-do e le sue attivià all'utente,

            todoPanel.add(titleToDo, BorderLayout.NORTH);

            todoPanel.setPreferredSize(new Dimension(492, 300)); // dimensione fissa iniziale
            todoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            //colore to-do deve essere diverso al colore dello sfondo
            if (isValidColor(colorBackground)) {
                todoPanel.setBackground(colorBackground);
            }

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(tableScroll, BorderLayout.CENTER);

            JPanel buttonInToDo = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton addActivityButton = new JButton("Add Activity");
            JButton rmvActivityButton = new JButton("Remove Activity");

            styleButton(addActivityButton);
            styleButton(rmvActivityButton);


            buttonInToDo.add(addActivityButton);
            buttonInToDo.add(rmvActivityButton);

            centerPanel.add(buttonInToDo, BorderLayout.SOUTH);
            todoPanel.add(centerPanel, BorderLayout.CENTER);

            addActivityButton.addActionListener(e -> {
                JDialog dialogAddActivity = new JDialog(frame, true);
                dialogAddActivity.setUndecorated(true);
                dialogAddActivity.setSize(370, 180);
                dialogAddActivity.setLocationRelativeTo(frame);
                dialogAddActivity.setLayout(new BorderLayout());

                // Barra del titolo
                JPanel titleBar = new JPanel(new BorderLayout());
                titleBar.setBackground(DARK_GRAY_TEXT);
                titleBar.setPreferredSize(new Dimension(dialogAddActivity.getWidth(), 40));

                titleLabel.setText("Add New Activity");
                titleLabel.setForeground(Color.WHITE);
                titleLabel.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 16));
                titleBar.add(titleLabel, BorderLayout.WEST);

                JButton closeButton = new JButton("X");
                closeButton.setForeground(Color.WHITE);
                closeButton.setBackground(DARK_GRAY_TEXT);
                closeButton.setBorderPainted(false);
                closeButton.setFocusPainted(false);
                closeButton.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 14));
                closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                closeButton.addActionListener(ev -> dialogAddActivity.dispose());
                titleBar.add(closeButton, BorderLayout.EAST);

                // Drag per spostare la finestra
                final Point clickPoint = new Point();
                titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        clickPoint.setLocation(e.getPoint());
                    }
                });
                titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(java.awt.event.MouseEvent e) {
                        Point location = dialogAddActivity.getLocation();
                        dialogAddActivity.setLocation(location.x + e.getX() - clickPoint.x,
                                location.y + e.getY() - clickPoint.y);
                    }
                });

                // Pannello principale
                JPanel panelAddActivity = new JPanel();
                panelAddActivity.setLayout(new BoxLayout(panelAddActivity, BoxLayout.Y_AXIS));
                panelAddActivity.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
                panelAddActivity.setBackground(Color.decode("#F3F4F6"));

                // Label e campo input
                JLabel nameLabel = new JLabel("Name:");
                nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                nameLabel.setFont(new Font(SANS_SERIF_FONT, Font.PLAIN, 13));
                nameLabel.setForeground(Color.decode("#111827")); // scuro

                JTextField nameAct = new JTextField();
                nameAct.setMaximumSize(new Dimension(250, 25));
                nameAct.setAlignmentX(Component.CENTER_ALIGNMENT);

                panelAddActivity.add(Box.createVerticalGlue());
                panelAddActivity.add(nameLabel);
                panelAddActivity.add(Box.createRigidArea(new Dimension(0, 5)));
                panelAddActivity.add(nameAct);
                panelAddActivity.add(Box.createVerticalGlue());

                // Pannello bottone
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setOpaque(false);

                JButton doneButton = new JButton("Done");
                doneButton.setBackground(DONE_BUTTON_COLOR); // Verde stile coerente
                doneButton.setForeground(Color.WHITE);
                doneButton.setFocusPainted(false);
                doneButton.setFont(new Font(SANS_SERIF_FONT, Font.BOLD, 12));
                doneButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                doneButton.addActionListener(ae -> {
                    try {
                        String nameToDoText = nameAct.getText();
                        Activity activity = new Activity(nameToDoText, false);
                        controller.addActivity(t.getOwnerEmail(), t.getTitle(), nameBoard, activity);
                        updateToDoList(controller, email, nameBoard);
                        aggiornaStatoToDo(stateField ,email, nameBoard, t);
                        dialogAddActivity.dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialogAddActivity, "Something went wrong.", ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE);
                    }
                });

                buttonPanel.add(doneButton);
                panelAddActivity.add(Box.createRigidArea(new Dimension(0, 10)));
                panelAddActivity.add(buttonPanel);

                dialogAddActivity.add(titleBar, BorderLayout.NORTH);
                dialogAddActivity.add(panelAddActivity, BorderLayout.CENTER);
                dialogAddActivity.setVisible(true);
            });


            rmvActivityButton.addActionListener(e -> {
                try {
                    int rowTable = table.getSelectedRow();
                    if (rowTable == -1) {
                        throw new IllegalStateException("No rows selected");
                    }
                    String activityName = (String) table.getValueAt(rowTable, 0);

                    // Usa l'owner per rimuovere globalmente l'attività
                    controller.removeActivity(t.getOwnerEmail(), t.getTitle(), nameBoard, activityName);

                    updateToDoList(controller, email, nameBoard);
                    aggiornaStatoToDo(stateField ,email, nameBoard, t);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(table,
                            "First select an activity to remove.",
                            ERROR_MESSAGE,
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            propertiesButton.addActionListener(e -> {
                if (dialog != null && dialog.isShowing()) {
                    dialog.toFront();
                    return;
                }
                dialog = new JDialog(frame, "Properties", false);
                JPanel propertiesDialog = createPropertiesDialog(dialog, controller, email, nameBoard, t);
                dialog.setContentPane(propertiesDialog);
                dialog.pack();
                dialog.setLocationRelativeTo(frame);
                dialog.setResizable(false);
                dialog.setVisible(true);
            });




            rowPanel.add(todoPanel);
            count++;
            panelToDoMain.add(rowPanel);

            if (count % 3 == 0) {
                rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                rowPanel.setBackground(Color.decode("#DAD2C8"));
            }


        }

        filter.setText("");

        panelToDoMain.revalidate();
        panelToDoMain.repaint();

    }

    private boolean isValidColor(Color color) {
        return color != null && !Color.decode("#F9F5F0").equals(color);
    }

    private JPanel createPropertiesDialog(JDialog dialog, ApplicationManagement controller, String email, String nameBoard, ToDo t) {
        JPanel propertiesDialog = new JPanel();
        propertiesDialog.setLayout(new BoxLayout(propertiesDialog, BoxLayout.Y_AXIS));
        propertiesDialog.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        boolean isAdmin = controller.isUserAdminOfToDo(email, nameBoard, t.getTitle());
        final Color[] colorWrapper = new Color[] { t.getColor() };


        // Title
        JPanel titlePanel = new JPanel(new BorderLayout(5, 2));
        titlePanel.add(new JLabel("Title:"), BorderLayout.WEST);
        JTextField titleField = new JTextField(t.getTitle(), 20);
        titleField.setEditable(isAdmin);
        titlePanel.add(titleField, BorderLayout.CENTER);
        propertiesDialog.add(titlePanel);

        // Expiration
        JPanel expirationPanel = new JPanel(new BorderLayout(5, 2));
        expirationPanel.add(new JLabel("Expiration:"), BorderLayout.WEST);
        JTextField expirationField = new JTextField();
        expirationField.setText(t.getExpiration() != null ? t.getExpiration().format(formatter) : "");
        if (t.getExpiration() == null) {
            PromptSupport.setPrompt("(dd-MM-yyyy)", expirationField);
            PromptSupport.setForeground(Color.DARK_GRAY, expirationField);
            PromptSupport.setFontStyle(Font.ITALIC, expirationField);
        }
        expirationField.setEditable(isAdmin);
        expirationPanel.add(expirationField, BorderLayout.CENTER);
        propertiesDialog.add(expirationPanel);

        // Description
        JPanel descriptionPanel = new JPanel(new BorderLayout(5, 2));
        descriptionPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        JTextArea descriptionArea = new JTextArea(t.getDescription(), 3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(isAdmin);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        descriptionPanel.add(scrollPane, BorderLayout.CENTER);
        propertiesDialog.add(descriptionPanel);

        // Image Panel
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
        imagePanel.add(new JLabel("Image:"), BorderLayout.NORTH);
        JTextField url = new JTextField(t.getImage() != null ? t.getImage() : "");
        url.setEditable(isAdmin);
        imagePanel.add(url, BorderLayout.CENTER);

        JLabel imagePreview = new JLabel();
        imagePreview.setPreferredSize(new Dimension(100, 100));
        imagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        imagePanel.add(imagePreview, BorderLayout.SOUTH);

        caricaImmagineIniziale(t, imagePreview);

        JButton browseButton = new JButton("Sfoglia...");
        browseButton.setEnabled(isAdmin);
        browseButton.addActionListener(es -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Immagini", "jpg", "jpeg", "png", "gif"));

            int result = fileChooser.showOpenDialog(propertiesDialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String imagePath = selectedFile.getAbsolutePath();
                url.setText(imagePath);
                t.setImage(imagePath);

                ImageIcon icon = new ImageIcon(imagePath);
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                imagePreview.setIcon(new ImageIcon(img));
            }
        });
        imagePanel.add(browseButton, BorderLayout.EAST);
        propertiesDialog.add(imagePanel);

        // Color Panel
        JPanel colorPanel = new JPanel(new BorderLayout(5, 2));
        colorPanel.add(new JLabel("Color:"), BorderLayout.WEST);
        JButton selectColor = new JButton("Color background");
        selectColor.setEnabled(isAdmin);
        colorPanel.add(selectColor, BorderLayout.CENTER);

        JPanel colorPreview = new JPanel();
        colorPreview.setBackground(color);
        colorPreview.setPreferredSize(new Dimension(20, 20));
        colorPanel.add(colorPreview, BorderLayout.EAST);
        propertiesDialog.add(colorPanel);

        selectColor.addActionListener(e -> {
            Color colorSelected = JColorChooser.showDialog(null, "Scegli colore", colorWrapper[0]);
            if (colorSelected != null) {
                colorWrapper[0] = colorSelected;
                colorPreview.setBackground(colorSelected);
            }
        });

        // State Panel
        JPanel statePanel = new JPanel(new BorderLayout(5, 2));
        statePanel.add(new JLabel("State:"), BorderLayout.WEST);
        JLabel stateField = new JLabel(t.isState() ? "✅" : "❌");
        statePanel.add(stateField, BorderLayout.CENTER);
        propertiesDialog.add(statePanel);

        // Board Panel
        JPanel boardPanel = new JPanel(new BorderLayout(5, 2));
        JLabel boardLabel = new JLabel("in Board:");
        JComboBox<String> boardComboBox = new JComboBox<>();
        boardComboBox.addItem(nameBoard);
        for (Board b : controller.printBoard(email)) {
            if (b != null && b.getType() != null && !b.getType().toString().equals(nameBoard)) {
                boardComboBox.addItem(b.getType().toString());
            }
        }
        boardPanel.add(boardLabel, BorderLayout.WEST);
        boardPanel.add(boardComboBox, BorderLayout.EAST);
        propertiesDialog.add(boardPanel);

        // Save Button Panel
        JButton saveButton = new JButton("Save");
        styleButton(saveButton);
        saveButton.setPreferredSize(new Dimension(70, 30));
        saveButton.setEnabled(isAdmin);

        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        savePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        savePanel.add(saveButton);
        propertiesDialog.add(savePanel);

        // Setup listener separatamente (passa parametri necessari)
        setupSaveButtonListener(saveButton, controller, email, nameBoard, t,
                expirationField, formatter, boardComboBox, titleField,
                descriptionArea, url, colorWrapper, () -> updateToDoList(controller, email, nameBoard), dialog);

        // Stile bottoni piccoli
        styleSmallButton(browseButton);
        styleSmallButton(selectColor);
        styleSmallButton(saveButton);
        saveButton.setPreferredSize(new Dimension(70, 30));

        return propertiesDialog;
    }


    private void caricaImmagineIniziale(ToDo t, JLabel imagePreview) {
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(t.getImage());
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                imagePreview.setIcon(new ImageIcon(img));
            } catch (Exception ex) {
                System.out.println("Errore nel caricamento immagine: " + ex.getMessage());
            }
        }
    }


    private void setupSharingButtonIfNeeded(ToDo t, JPanel ToDoButton, ApplicationManagement controller, JFrame frame, String email, String nameBoard) {
        //-verifica se il to-do è stato condiviso e in tal caso gli aggiunge il pulsante SharingInformation--
        if (t.isCondiviso()) {
            final JButton sharingInformationButton = new JButton("👥");
            sharingInformationButton.setFont(new Font(null, Font.BOLD, 18));

            // Inizialmente trasparente
            sharingInformationButton.setContentAreaFilled(false);
            sharingInformationButton.setBorderPainted(false);
            sharingInformationButton.setFocusPainted(false);

            // Margini più piccoli (rende il bottone più compatto)
            sharingInformationButton.setMargin(new Insets(4, 8, 4, 8));

            // Colore hover
            Color hoverBackground = new Color(220, 220, 220); // grigio chiaro

            // Listener per effetto hover
            sharingInformationButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sharingInformationButton.setContentAreaFilled(true);
                    sharingInformationButton.setBackground(hoverBackground);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sharingInformationButton.setContentAreaFilled(false);
                }
            });

            ToDoButton.add(sharingInformationButton);

            sharingInformationButton.addActionListener(e -> {
                if (sharingInfoFrame == null || !sharingInfoFrame.isVisible()) {
                    sharingInfoFrame = new JFrame("Sharing Information");
                    sharingInfoFrame.setSize(300, 150);
                    sharingInfoFrame.setLocationRelativeTo(frame);
                    sharingInfoFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                    SharingInformation sharingInformation = new SharingInformation(
                            controller, email, t.getTitle(), nameBoard,
                            () -> updateToDoList(controller, email, nameBoard) // callback per aggiornare la lista
                    );
                    sharingInfoFrame.setContentPane(sharingInformation.getPanel());
                    sharingInfoFrame.setSize(500, 300);
                    sharingInfoFrame.setLocationRelativeTo(frame);

                    sharingInfoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            sharingInfoFrame = null;
                        }

                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            sharingInfoFrame = null;
                        }
                    });
                    sharingInfoFrame.setVisible(true);
                } else {
                    sharingInfoFrame.toFront();
                    sharingInfoFrame.requestFocus();
                }
            });
        } else {
            //--Se non condiviso, verifica se c'è un bottone di condivisione da rimuovere--
            for (Component comp : ToDoButton.getComponents()) {
                if (comp instanceof JButton btn && "👥".equals(btn.getText())) {
                    ToDoButton.remove(comp);
                    break;
                }
            }
        }
    }

    private void addTableModelListener(DefaultTableModel tableModel, ApplicationManagement controller, String email, String nameBoard, ToDo t, JLabel stateField) {
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 1 && e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                Boolean stato = (Boolean) tableModel.getValueAt(row, 1);
                String nomeAttivita = (String) tableModel.getValueAt(row, 0);
                if (stato) {
                    LocalDate date = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    String completitionDate = date.format(formatter);
                    controller.checkActivity(email, nameBoard, t.getTitle(), nomeAttivita, completitionDate);
                } else {
                    controller.deCheckActivity(email, nameBoard, t.getTitle(), nomeAttivita);
                }
                // aggiorna stato dopo rimozione
                aggiornaStatoToDo(stateField ,email, nameBoard, t);
                updateToDoList(controller, email, nameBoard);
            }
        });
    }


    private void setupSaveButtonListener(JButton saveButton, ApplicationManagement controller, String email, String nameBoard, ToDo t,
                                         JTextField expirationField, DateTimeFormatter formatter, JComboBox<String> boardComboBox,
                                         JTextField titleField, JTextArea descriptionArea, JTextField url, Color[] colorWrapper,
                                         Runnable updateToDoList, JDialog dialog) {

        saveButton.addActionListener(z -> {
            String expirationString = expirationField.getText().trim();
            LocalDate date = null;
            String nuovaBoard = boardComboBox.getSelectedItem().toString();

            if (!expirationString.isEmpty()) {
                try {
                    date = LocalDate.parse(expirationString, formatter);

                    // Controllo che la data non sia nel passato
                    if (date.isBefore(LocalDate.now())) {
                        JOptionPane.showMessageDialog(null, "The expiration date cannot be in the past.");
                        return;
                    }

                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid date format. Use dd-MM-yyyy");
                    return;
                }
            }

            // se ritorna true ha torvato un altro to-do con lo stesso nome
            if (!controller.editToDo(email, nameBoard, t.getTitle(), titleField.getText(), descriptionArea.getText(), date, url.getText(), colorWrapper[0])) {
                JOptionPane.showMessageDialog(saveButton, "name already in use");
            }

            checkBoardMove(controller, email, nameBoard, nuovaBoard, t);

            updateToDoList.run();
            dialog.dispose();
        });
    }

    private void checkBoardMove(ApplicationManagement controller, String email, String nameBoard, String nuovaBoard, ToDo t) {
        if (!nameBoard.equalsIgnoreCase(nuovaBoard)) {
            int result = controller.spostaToDoInBacheca(email, t.getTitle(), nuovaBoard, nameBoard);
            if (result == 1) {
                JOptionPane.showMessageDialog(null, "To-Do already exists in " + nuovaBoard + "dashboard", ERROR_MESSAGE, JOptionPane.WARNING_MESSAGE);
            } else if (result == 2) {
                JOptionPane.showMessageDialog(null, "You can't move a shared To-Do ", ERROR_MESSAGE, JOptionPane.WARNING_MESSAGE);
            } else if (result == 0) {
                JOptionPane.showMessageDialog(null, "To-Do moved to" + nuovaBoard + "board", "Moved correctly", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    private void aggiornaStatoToDo(JLabel stateField, String email, String nameBoard, ToDo t) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            ToDoDAO dao = new ToDoDAO(conn);
            CheckListDAO daoChecklist = new CheckListDAO(conn);

            int toDoId = daoChecklist.getToDoId(email, nameBoard, t.getTitle());
            try{
                boolean statoAggiornato = dao.getStateById(toDoId);

                // Aggiorna GUI nel thread corretto
                SwingUtilities.invokeLater(() -> {
                    stateField.setText(statoAggiornato ? "✅" : "❌");
                    stateField.revalidate();
                    stateField.repaint();
                });
            }catch(Exception e){
                e.printStackTrace();
            }


        } catch (Exception e) {
            System.err.println("Errore durante aggiornamento stato: " + e.getMessage());
        }
    }

    private void styleButton(JButton button) {
        Color baseColor = DONE_BUTTON_COLOR;
        Color hoverColor = baseColor.darker();
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(baseColor, 2));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(160, 40));

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

    private void styleSmallButton(JButton button) {
        Color baseColor = Color.decode("#6B7280");
        Color hoverColor = baseColor.brighter();

        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(baseColor, 1));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(55, 12));  // più piccolo

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createLineBorder(hoverColor, 1));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createLineBorder(baseColor, 1));
            }
        });
    }



}