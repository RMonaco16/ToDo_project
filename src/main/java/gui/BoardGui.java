package gui;

import controller.ApplicationManagement;
import model.Activity;
import model.Board;
import model.CheckList;
import model.ToDo;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

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
    private Sharing sharingWindow = null;//per verificare apertura finestre
    private JFrame sharingInfoFrame = null;

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
                    ToDo todo = new ToDo(nameToDoText, false, checkList, false, email);
                    if(!controller.addToDoInBoard(email, nameBoard, todo))
                        JOptionPane.showMessageDialog(newToDo, "Name already used","Errore", JOptionPane.ERROR_MESSAGE);


                    updateToDoList(controller, email, nameBoard);

                    newToDo.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(nameToDo, "Formato data non valido!", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            });

            newToDo.setContentPane(dialog);
            newToDo.setVisible(true);
        });

        //apertura della finestra e Condizione per non farla aprire piu volte
        shareButton.addActionListener(e -> {
            if (sharingWindow == null || !sharingWindow.getFrame().isVisible()) {
                sharingWindow = new Sharing(controller, email, vecchioFrame, nameBoard);
                updateToDoList(controller, email, nameBoard);
            } else {
                sharingWindow.getFrame().toFront();
                sharingWindow.getFrame().requestFocus();
            }
        });

        deleteButton.addActionListener(e -> {
            JDialog dialogDelete = new JDialog(frame, "New Activity", true);
            dialogDelete.setSize(300, 150);
            dialogDelete.setLocationRelativeTo(frame);
            dialogDelete.setResizable(false);

            JPanel panelDelete = new JPanel();
            panelDelete.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            panelDelete.setLayout(new BorderLayout(10, 10));

            JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
            inputPanel.add(new JLabel("Select ToDo to delete:"), BorderLayout.NORTH);
            JComboBox<String> toDoToDelete = new JComboBox<>();
            toDoToDelete.addItem("");
            for(ToDo t:controller.printTodo(email,nameBoard)){
                toDoToDelete.addItem(t.getTitle());
            }

            inputPanel.add(toDoToDelete, BorderLayout.CENTER);

            JButton doneButton = new JButton("Delete");
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(doneButton);

            panelDelete.add(inputPanel, BorderLayout.CENTER);
            panelDelete.add(buttonPanel, BorderLayout.SOUTH);

            dialogDelete.setContentPane(panelDelete);

            doneButton.addActionListener(x->{
               if(toDoToDelete.getSelectedItem()==null||toDoToDelete.getSelectedItem().equals("")){
                   JOptionPane.showMessageDialog(dialogDelete, "First select the todo", "Errore",JOptionPane.ERROR_MESSAGE);
               }else{
                   if(controller.deleteToDo(email,nameBoard,toDoToDelete.getSelectedItem().toString())){
                       JOptionPane.showMessageDialog(dialogDelete, "all deleted correctly");
                       dialogDelete.dispose();
                       updateToDoList(controller, email, nameBoard);
                   }else{
                       JOptionPane.showMessageDialog(dialogDelete, "you are not the administrator of this ToDo", "Warning",JOptionPane.WARNING_MESSAGE);
                   }

               }
            });

            dialogDelete.setVisible(true);
        });


        updateToDoList(controller, email, nameBoard);
    }

    private void updateToDoList(ApplicationManagement controller, String email, String nameBoard) {
        panelToDoMain.removeAll();

        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        int count = 0;

        for (ToDo t : controller.getVisibleToDos(controller.findUserByEmail(email), nameBoard)) {

            JPanel titleToDo = new JPanel(new BorderLayout());
            JPanel ToDoButton = new JPanel();
            if(t.isCondiviso()){
                JButton sharingInformationButton = new JButton("👥");
                sharingInformationButton.setFont(new Font(null, Font.BOLD, 22));
                ToDoButton.add(sharingInformationButton);

                //ActionListener bottone 👥
                sharingInformationButton.addActionListener(e -> {
                    if (sharingInfoFrame == null || !sharingInfoFrame.isVisible()) {
                        sharingInfoFrame = new JFrame("Sharing Information");
                        sharingInfoFrame.setSize(500, 200);
                        sharingInfoFrame.setLocationRelativeTo(frame);
                        sharingInfoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                        SharingInformation sharingInformation = new SharingInformation(controller,frame,email,t.getTitle());
                        sharingInfoFrame.setContentPane(sharingInformation.getPanel());

                        // Quando viene chiusa, resetta il riferimento
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
                        // Porta la finestra già aperta in primo piano
                        sharingInfoFrame.toFront();
                        sharingInfoFrame.requestFocus();
                    }
                });


            }else{
                JLabel label = new JLabel();
                ToDoButton.add(label);
            }


            JLabel titleLabel = new JLabel("ToDo: " + t.getTitle());
            titleLabel.setFont(new Font(null, Font.BOLD, 20));
            JButton propertiesButton = new JButton("≡");
            propertiesButton.setFont(new Font(null, Font.BOLD, 20));


            ToDoButton.add(propertiesButton);

            titleToDo.add(titleLabel, BorderLayout.WEST);
            titleToDo.add(ToDoButton, BorderLayout.EAST);


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

            ArrayList<Activity> activities = controller.printActs(t.getOwnerEmail(), nameBoard, t.getTitle());
            if (!activities.isEmpty()) {
                for (Activity a : activities) {
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
                        controller.addActivity(t.getOwnerEmail(), t.getTitle(), nameBoard, activity);
                        updateToDoList(controller, email, nameBoard);
                        newAct.dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(newAct, "Qualcosa è andato storto.", "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                });

                newAct.setContentPane(dialog);
                newAct.setVisible(true);
            });

            rmvActivityButton.addActionListener(e -> {
                try {
                    int rowTable = table.getSelectedRow();
                    if (rowTable == -1) {
                        throw new IllegalStateException("Nessuna riga selezionata");
                    }

                    String activityName = (String) table.getValueAt(rowTable, 0);

                    // 🔑 Rimozione attività
                    controller.removeActivity(t.getOwnerEmail(), t.getTitle(), nameBoard, activityName);

                    updateToDoList(controller, email, nameBoard);
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(table,
                            "Seleziona prima un'attività da rimuovere.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace(); // utile per debugging
                    JOptionPane.showMessageDialog(table,
                            "Errore durante la rimozione dell'attività.",
                            "Errore",
                            JOptionPane.ERROR_MESSAGE);
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
                statePanel.add(new JLabel("State:"), BorderLayout.WEST);
                JLabel stateField = new JLabel(t.isState() ? "✅" : "❌");
                statePanel.add(stateField, BorderLayout.CENTER);
                propertiesDialog.add(statePanel);

                //Board also if want change
                JPanel baordPanel = new JPanel(new BorderLayout(5,2));
                JLabel boardLabel = new JLabel("in Board:");
                JComboBox boardComboBox = new JComboBox();
                boardComboBox.addItem(nameBoard);
                for(Board b: controller.printBoard(email)){
                    if(b != null && b.getType() != null && !b.getType().toString().equals(nameBoard)){
                        boardComboBox.addItem(b.getType().toString());
                    }
                }
                baordPanel.add(boardLabel);
                baordPanel.add(boardComboBox,BorderLayout.EAST);
                propertiesDialog.add(baordPanel,BorderLayout.WEST);


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
                    // se ritorna true ha torvato un altro to-do con lo stesso nome
                    if (controller.editToDo(email, nameBoard, t.getTitle(), titleField.getText(), descriptionArea.getText(), date, url.getText(), colorField.getText())){
                        JOptionPane.showMessageDialog(saveButton,"name already in use");
                    }
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