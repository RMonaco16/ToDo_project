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
            propertiesButton.addActionListener(e->{

                JDialog propertiesDialog = new JDialog(frame,"Properties",false);
                propertiesDialog.setLayout(new GridLayout(6,1));

                JPanel titlePanel = new JPanel(new BorderLayout(1,2));
                JLabel titleToDoLabel = new JLabel("Title:");
                JTextField titleField = new JTextField(20);
                titleField.setText(t.getTitle());
                titlePanel.add(titleToDoLabel,BorderLayout.WEST);
                titlePanel.add(titleField,BorderLayout.EAST);
                propertiesDialog.add(titlePanel);

                JPanel expirationPanel = new JPanel(new BorderLayout(1,2));
                JLabel expirationToDoLabel = new JLabel("Expiration:");
                JTextField expirationField = new JTextField(20);
                String expirationText = (t.getExpiration() != null) ? t.getExpiration().toString() : "";
                expirationField.setText(expirationText);

                expirationPanel.add(expirationToDoLabel,BorderLayout.WEST);
                expirationPanel.add(expirationField,BorderLayout.EAST);
                propertiesDialog.add(expirationPanel);

                JPanel descriptionPanel = new JPanel(new BorderLayout(1,2));
                JLabel descriptionToDoLabel = new JLabel("Description:");
                JTextField descriptionField = new JTextField(20);
                descriptionField.setText(t.getDescription());
                descriptionPanel.add(descriptionToDoLabel,BorderLayout.WEST);
                descriptionPanel.add(descriptionField,BorderLayout.EAST);
                propertiesDialog.add(descriptionPanel);

                JPanel imagePanel = new JPanel(new BorderLayout(1,2));
                JLabel imageToDoLabel = new JLabel("Image URL:");
                ImageIcon image = new ImageIcon(t.getImage());
                JLabel imageToDo = new JLabel(image);
                imagePanel.add(imageToDoLabel,BorderLayout.WEST);
                imagePanel.add(imageToDo,BorderLayout.EAST);
                propertiesDialog.add(imagePanel);

                JPanel colorPanel = new JPanel(new BorderLayout(1,2));
                JLabel colorToDoLabel = new JLabel("Color:");
                JTextField colorField = new JTextField(20);
                descriptionField.setText(t.getDescription());
                colorPanel.add(colorToDoLabel,BorderLayout.WEST);
                colorPanel.add(colorField,BorderLayout.EAST);
                propertiesDialog.add(colorPanel);

                JPanel statePanel = new JPanel(new BorderLayout(1,2));
                JLabel stateToDoLabel = new JLabel("Stato:");
                JLabel stateField = new JLabel();
                //String expirationText = (t.getExpiration() != null) ? t.getExpiration().toString() : "";
                String stateToDo = (t.getState()==true) ? "✅":"❌";
                stateField.setText(stateToDo);
                titlePanel.add(titleToDoLabel,BorderLayout.WEST);
                titlePanel.add(titleField,BorderLayout.EAST);
                propertiesDialog.add(titlePanel);

                propertiesDialog.pack();
                propertiesDialog.setLocationRelativeTo(frame);
                propertiesDialog.setVisible(true);
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
