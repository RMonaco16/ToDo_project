package gui;

import controller.ApplicationManagement;
import model.Activity;
import model.CheckList;
import model.ToDo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BoardGui {
    private JButton undoButton;
    private JButton deleteButton;
    private JButton addButton;
    private JPanel BoardGui;
    private JPanel panelToDo;
    private JButton shareButton;
    private JButton searchButton;
    private JTextField filter;
    private JLabel textFilter;
    private JFrame frame;
    private JDialog nameToDo;
    private JScrollPane scrollPanelToDo;

    public BoardGui(ApplicationManagement controller, JFrame vecchioFrame, String email, String nameBoard) {
        frame = new JFrame(nameBoard);
        frame.setContentPane(BoardGui);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        panelToDo.setLayout(new GridLayout(0, 3));

        // Pulsante "indietro"
        undoButton.addActionListener(e -> {
            frame.setVisible(false);
            vecchioFrame.setVisible(true);
            frame.dispose();
        });

        // Pulsante per aggiungere ToDo
        addButton.addActionListener(e -> {
            nameToDo = new JDialog(frame, "Nuovo ToDo", true);
            nameToDo.setSize(300, 150);
            nameToDo.setLocationRelativeTo(frame);

            JPanel dialog = new JPanel(new GridLayout(3, 2));
            JTextField nameField = new JTextField();
            JTextField expirationField = new JTextField();
            JButton doneButton = new JButton("Fatto");

            dialog.add(new JLabel("Nome:"));
            dialog.add(nameField);
            dialog.add(new JLabel("Scadenza (dd-MM-yyyy):"));
            dialog.add(expirationField);
            dialog.add(new JLabel());  // spazio vuoto
            dialog.add(doneButton);

            doneButton.addActionListener(ae -> {
                try {
                    String nameToDoText = nameField.getText();
                    String expirationText = expirationField.getText();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    LocalDate expirationDate = LocalDate.parse(expirationText, formatter);

                    CheckList checkList = new CheckList();
                    ToDo todo = new ToDo(nameToDoText, false, checkList, expirationDate);
                    controller.addToDoInBoard(email, nameBoard, todo);

                    // Aggiorna la GUI
                    updateToDoList(controller, email, nameBoard);

                    nameToDo.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(nameToDo, "Formato data non valido!", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            });

            nameToDo.add(dialog);
            nameToDo.setVisible(true);
        });

        // Carica i ToDo iniziali
        updateToDoList(controller, email, nameBoard);

        //------Collegamento a Sharing----------
        shareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Sharing sharing = new Sharing(controller,email,vecchioFrame,nameBoard);
            }
        });
    }

    private void updateToDoList(ApplicationManagement controller, String email, String nameBoard) {
        panelToDo.removeAll(); // Rimuove i vecchi componenti

        for (ToDo t : controller.printTodo(email, nameBoard)) {
            // Titolo del ToDo
            JLabel titleLabel = new JLabel("ToDo: " + t.getTitle());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

            // Modello della tabella
            DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Attività", "Fatto"}, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 1 ? Boolean.class : String.class;
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 1; // Solo la checkbox è modificabile
                }
            };

            // Aggiungi attività
            for (Activity a : controller.printActs(email, nameBoard, t.getTitle())) {
                tableModel.addRow(new Object[]{a.getName(), false});
            }

            JTable table = new JTable(tableModel);
            table.setFillsViewportHeight(true);
            JScrollPane tableScroll = new JScrollPane(table);

            // Pannello contenitore
            JPanel todoPanel = new JPanel();
            todoPanel.setLayout(new BorderLayout(5, 5));
            todoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            todoPanel.add(titleLabel, BorderLayout.NORTH);
            todoPanel.add(tableScroll, BorderLayout.CENTER);

            panelToDo.add(todoPanel);
        }

        panelToDo.revalidate();
        panelToDo.repaint();
    }
}
