package gui;

import controller.ApplicationManagement;
import model.Activity;
import model.CheckList;
import model.ToDo;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BoardGui {
    private JButton undoButton;
    private JButton a️Button;
    private JButton addButton;
    private JPanel BoardGui;
    private JPanel panelToDoMain;
    private JButton shareButton;
    private JButton searchButton;
    private JTextField filter;
    private JLabel textFilter;
    private JFrame frame;
    private JScrollPane scrollPanelToDo;

    public BoardGui(ApplicationManagement controller, JFrame vecchioFrame, String email, String nameBoard) {
        frame = new JFrame(nameBoard);
        frame.setContentPane(BoardGui);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        vecchioFrame.setVisible(false);

        // Set layout verticale per contenitore principale
        panelToDoMain.setLayout(new BoxLayout(panelToDoMain, BoxLayout.Y_AXIS));

        // Collega panelToDoMain allo scroll pane
        scrollPanelToDo.setViewportView(panelToDoMain);
        scrollPanelToDo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanelToDo.getVerticalScrollBar().setUnitIncrement(16);

        frame.setVisible(true);

        undoButton.addActionListener(e -> {
            frame.setVisible(false);
            vecchioFrame.setVisible(true);
            frame.dispose();
        });

        addButton.addActionListener(e -> {
            JDialog newToDo = new JDialog(frame, "New Activity", true);
            newToDo.setSize(300, 150);
            newToDo.setLocationRelativeTo(frame);
            newToDo.setResizable(false);

            JPanel dialog = new JPanel();
            dialog.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            dialog.setLayout(new BorderLayout(10, 10));

            JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
            inputPanel.add(new JLabel("Name:"), BorderLayout.NORTH);
            JTextField nameToDo = new JTextField();
            inputPanel.add(nameToDo, BorderLayout.CENTER);

            JButton doneButton = new JButton("Done");
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(doneButton);

            dialog.add(inputPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);


            doneButton.addActionListener(ae -> {
                try {
                    String nameToDoText = nameToDo.getText();
//                    String expirationText = expirationField.getText();
//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//                    LocalDate expirationDate = LocalDate.parse(expirationText, formatter);

                    CheckList checkList = new CheckList();
                    ToDo todo = new ToDo(nameToDoText, false, checkList);
                    controller.addToDoInBoard(email, nameBoard, todo);

                    updateToDoList(controller, email, nameBoard);

                    newToDo.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(nameToDo, "Formato data non valido!", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            });

            newToDo.setContentPane(dialog);
            newToDo.setVisible(true);
        });

        shareButton.addActionListener(e -> new Sharing(controller, email, vecchioFrame, nameBoard));

        updateToDoList(controller, email, nameBoard);
    }

    private void updateToDoList(ApplicationManagement controller, String email, String nameBoard) {
        panelToDoMain.removeAll();

        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        int count = 0;

        for (ToDo t : controller.printTodo(email, nameBoard)) {

            JPanel titleToDo = new JPanel(new BorderLayout());

            JLabel titleLabel = new JLabel("ToDo: " + t.getTitle());
            titleLabel.setFont(new Font(null, Font.BOLD, 22));
            JButton propertiesButton = new JButton("≡");
            propertiesButton.setFont(new Font(null, Font.BOLD, 22));

            titleToDo.add(titleLabel, BorderLayout.WEST);
            titleToDo.add(propertiesButton, BorderLayout.EAST);


            DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Attività", "Fatto"}, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 1 ? Boolean.class : String.class;
                }
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 1;
                }
            };

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
                }
            });

            if (!controller.printActs(email, nameBoard, t.getTitle()).isEmpty()) {
                for (Activity a : controller.printActs(email, nameBoard, t.getTitle())) {
                    tableModel.addRow(new Object[]{a.getName(), a.getState()});
                }
            }

            JTable table = new JTable(tableModel);

            // Imposta larghezza preferita delle colonne
            table.getColumnModel().getColumn(0).setPreferredWidth(300);
            table.getColumnModel().getColumn(1).setPreferredWidth(50);

            table.setFillsViewportHeight(true);
            JScrollPane tableScroll = new JScrollPane(table);

            int rowCount = table.getRowCount();
            int rowHeight = table.getRowHeight();

            int maxHeight = 200; // altezza fissa massima visibile per la tabella
            int totalHeight = rowCount * table.getRowHeight();

            int scrollHeight = Math.min(totalHeight, maxHeight);

            // Limita l'altezza visibile a maxHeight, aggiunge scrollbar se necessario
            table.setPreferredScrollableViewportSize(new Dimension(380, scrollHeight));
            tableScroll.setPreferredSize(new Dimension(400, scrollHeight + 20));

            JPanel todoPanel = new JPanel(new BorderLayout(5, 5));
            todoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            todoPanel.add(titleToDo, BorderLayout.NORTH);

            todoPanel.setPreferredSize(new Dimension(495, 300)); // dimensione fissa iniziale

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(tableScroll, BorderLayout.CENTER);

            JPanel buttonInToDo = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton addActivityButton = new JButton("Add Activity");
            JButton rmvActivityButton = new JButton("Remove Activity");
            buttonInToDo.add(addActivityButton);
            buttonInToDo.add(rmvActivityButton);

            centerPanel.add(buttonInToDo, BorderLayout.SOUTH);

            todoPanel.add(centerPanel, BorderLayout.CENTER);

            addActivityButton.addActionListener(e -> {
                JDialog newAct = new JDialog(frame, "New Activity", true);
                newAct.setSize(300, 150);
                newAct.setLocationRelativeTo(frame);
                newAct.setResizable(false);

                JPanel dialog = new JPanel();
                dialog.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
                dialog.setLayout(new BorderLayout(10, 10));

                JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
                inputPanel.add(new JLabel("Name:"), BorderLayout.NORTH);
                JTextField nameAct = new JTextField();
                inputPanel.add(nameAct, BorderLayout.CENTER);

                JButton doneButton = new JButton("Done");
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.add(doneButton);

                dialog.add(inputPanel, BorderLayout.CENTER);
                dialog.add(buttonPanel, BorderLayout.SOUTH);

                doneButton.addActionListener(ae -> {
                    try {
                        String nameToDoText = nameAct.getText();
                        Activity activity = new Activity(nameToDoText, false);
                        controller.addActivity(email, t.getTitle(), nameBoard, activity);
                        updateToDoList(controller, email, nameBoard);
                        newAct.dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(newAct, "Qualcosa è andato storto.", "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                });

                newAct.setContentPane(dialog);
                newAct.setVisible(true);
            });

            rmvActivityButton.addActionListener(e->{
                try {
                    int rowTable = table.getSelectedRow();
                    String activityName = (String) table.getValueAt(rowTable,0);

                    controller.removeActivity(email,t.getTitle(),nameBoard,activityName);
                    updateToDoList(controller, email, nameBoard);
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(table, "Selezionare l'attività prima di rimuoverla","Qualcosa è andato storto.", JOptionPane.ERROR_MESSAGE);
                }
            });

            /*  •	Titolo (obbligatorio)
                •	Data di scadenza
                •	Descrizione dettagliata
                •	Immagine
                •	Colore di sfondo personalizzabile
                •	Stato di completament
            */
            propertiesButton.addActionListener(e -> {
                        JDialog dialog = new JDialog(frame, "Properties", false);
                        JPanel propertiesDialog = new JPanel();
                        propertiesDialog.setLayout(new BoxLayout(propertiesDialog, BoxLayout.Y_AXIS));
                        propertiesDialog.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));


                        // Title
                        JPanel titlePanel = new JPanel(new BorderLayout(5, 2));
                        titlePanel.add(new JLabel("Title:"), BorderLayout.WEST);
                        JTextField titleField = new JTextField(t.getTitle(), 20);
                        titlePanel.add(titleField, BorderLayout.CENTER);
                        propertiesDialog.add(titlePanel);

                        // Expiration
                        JPanel expirationPanel = new JPanel(new BorderLayout(5, 2));
                        expirationPanel.add(new JLabel("Expiration:"), BorderLayout.WEST);
                        JTextField expirationField = new JTextField(
                                t.getExpiration() != null ? t.getExpiration().toString() : "", 20
                        );
                        expirationPanel.add(expirationField, BorderLayout.CENTER);
                        propertiesDialog.add(expirationPanel);

                        // Description
                        JPanel descriptionPanel = new JPanel(new BorderLayout(5, 2));
                        descriptionPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
                        JTextArea descriptionArea = new JTextArea(t.getDescription(), 3, 20);
                        descriptionArea.setLineWrap(true);
                        descriptionArea.setWrapStyleWord(true);
                        JScrollPane scrollPane = new JScrollPane(descriptionArea);
                        descriptionPanel.add(scrollPane, BorderLayout.CENTER);
                        propertiesDialog.add(descriptionPanel);

                        // Image
                        JPanel imagePanel = new JPanel(new BorderLayout(5, 2));
                        imagePanel.add(new JLabel("Image URL:"), BorderLayout.WEST);
                        //ImageIcon image = new ImageIcon(t.getImage());
                        JTextField url = new JTextField();
                        imagePanel.add(url, BorderLayout.CENTER);
                        propertiesDialog.add(imagePanel);

                        // Color
                        JPanel colorPanel = new JPanel(new BorderLayout(5, 2));
                        colorPanel.add(new JLabel("Color:"), BorderLayout.WEST);
                        JTextField colorField = new JTextField(20);
                        colorField.setText(""); // oppure t.getColor() se hai un campo colore
                        colorPanel.add(colorField, BorderLayout.CENTER);
                        propertiesDialog.add(colorPanel);

                        // State
                        JPanel statePanel = new JPanel(new BorderLayout(5, 2));
                        statePanel.add(new JLabel("Stato:"), BorderLayout.WEST);
                        JLabel stateField = new JLabel(t.isState() ? "✅" : "❌");
                        statePanel.add(stateField, BorderLayout.CENTER);
                        propertiesDialog.add(statePanel);

                        JButton saveButton = new JButton("Save");
                        propertiesDialog.add(saveButton, BorderLayout.EAST);

                        dialog.add(propertiesDialog);

                        dialog.pack();
                        dialog.setLocationRelativeTo(frame);
                        dialog.setResizable(false);
                        dialog.setVisible(true);

                saveButton.addActionListener(z -> {
                    String expirationString = expirationField.getText().trim();
                    LocalDate date = null;

                    if (!expirationString.isEmpty()) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            date = LocalDate.parse(expirationString, formatter);
                        } catch (DateTimeParseException ex) {
                            JOptionPane.showMessageDialog(null, "Formato data non valido. Usa gg-MM-aaaa");
                            return;
                        }
                    }

                    controller.editToDo(
                            email,
                            nameBoard,
                            t.getTitle(),                      // titolo da cercare
                            titleField.getText(),              // nuovo titolo
                            descriptionArea.getText(),
                            date,                              // può essere null
                            url.getText(),
                            colorField.getText()
                    );
                    updateToDoList(controller, email, nameBoard);
                    dialog.dispose();
                });
            });



            rowPanel.add(todoPanel);
            count++;

            if (count % 3 == 0) {
                panelToDoMain.add(rowPanel);
                rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            }
        }

        if (count % 3 != 0) {
            panelToDoMain.add(rowPanel);
        }

        panelToDoMain.revalidate();
        panelToDoMain.repaint();
    }
}
