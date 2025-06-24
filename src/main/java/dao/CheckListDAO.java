package dao;

import db.ConnessioneDatabase;
import model.Activity;
import model.CheckList;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CheckListDAO {

    private final Connection conn;

    public CheckListDAO(Connection conn) {
        this.conn = conn;
    }

    // Carica tutte le attività associate a un ToDo (identificato dal suo titolo)
    public ArrayList<Activity> getActivities(String email, String boardType, String todoTitle) {
        ArrayList<Activity> activities = new ArrayList<>();

        String sql = """
        SELECT a.name, a.state, a.completion_date
        FROM activities a
        JOIN checklists c ON a.checklist_id = c.id
        JOIN todos t ON t.checklist_id = c.id
        JOIN boards b ON t.board_id = b.id
        WHERE b.user_email = ? AND b.type = ? AND t.title = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType.toUpperCase()); // 'UNIVERSITY', 'WORK', 'FREETIME'
            stmt.setString(3, todoTitle);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                boolean state = rs.getBoolean("state");
                Date completion = rs.getDate("completion_date");

                Activity activity = new Activity(name, state);
                if (completion != null) {
                    activity.setCompletionDate(completion.toString()); // o converti in LocalDate se preferisci
                }

                activities.add(activity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }


    // Salva una nuova attività associata a un ToDo
    public void addActivity(String email, String titleToDo, String boardType, Activity activity) {
        //  Trova ID della checklist collegata al ToDo
        int checklistId = -1;
        String checklistSql = """
        SELECT c.id
        FROM checklists c
        JOIN todos t ON t.checklist_id = c.id
        JOIN boards b ON t.board_id = b.id
        WHERE b.user_email = ? AND b.type = ? AND t.title = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(checklistSql)) {
            stmt.setString(1, email);
            stmt.setString(2, boardType.toUpperCase());
            stmt.setString(3, titleToDo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                checklistId = rs.getInt("id");
            } else {
                System.out.println("Checklist non trovata.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        //  Controllo: esiste già un'attività con lo stesso nome in questa checklist?
        String checkDuplicateSql = "SELECT 1 FROM activities WHERE checklist_id = ? AND LOWER(name) = LOWER(?)";

        try (PreparedStatement stmt = conn.prepareStatement(checkDuplicateSql)) {
            stmt.setInt(1, checklistId);
            stmt.setString(2, activity.getName());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Attività già esistente nella checklist.");
                return; // blocca inserimento
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Inserimento dell'attività
        String insertSql = "INSERT INTO activities (checklist_id, name, state, completion_date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, checklistId);
            stmt.setString(2, activity.getName());
            stmt.setBoolean(3, activity.getState());

            if (activity.getCompletionDate() != null) {
                stmt.setDate(4, Date.valueOf(activity.getCompletionDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.executeUpdate();
            System.out.println("Attività inserita correttamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void removeActivity(String email, String titleToDo, String board, String nameActivity) throws SQLException{
        try {
            // 1. Recupera l'ID della board per l'utente
            String boardQuery = "SELECT id FROM boards WHERE user_email = ? AND type = ?";
            try (PreparedStatement boardStmt = conn.prepareStatement(boardQuery)) {
                boardStmt.setString(1, email);
                boardStmt.setString(2, board.toUpperCase());
                ResultSet rsBoard = boardStmt.executeQuery();

                if (!rsBoard.next()) {
                    System.out.println("Bacheca non trovata.");
                    return;
                }

                int boardId = rsBoard.getInt("id");

                // 2. Recupera il ToDo e il checklist_id
                String todoQuery = "SELECT id, checklist_id FROM todos WHERE LOWER(title) = LOWER(?) AND board_id = ?";
                try (PreparedStatement todoStmt = conn.prepareStatement(todoQuery)) {
                    todoStmt.setString(1, titleToDo);
                    todoStmt.setInt(2, boardId);
                    ResultSet rsTodo = todoStmt.executeQuery();

                    if (!rsTodo.next()) {
                        System.out.println("ToDo non trovato.");
                        return;
                    }

                    int todoId = rsTodo.getInt("id");
                    int checklistId = rsTodo.getInt("checklist_id");

                    if (checklistId == 0) {
                        System.out.println("ToDo non ha una checklist associata.");
                        return;
                    }

                    // 3. Elimina l'attività specifica dalla checklist
                    String deleteAct = "DELETE FROM activities WHERE checklist_id = ? AND LOWER(name) = LOWER(?)";
                    try (PreparedStatement delActStmt = conn.prepareStatement(deleteAct)) {
                        delActStmt.setInt(1, checklistId);
                        delActStmt.setString(2, nameActivity);
                        int affected = delActStmt.executeUpdate();

                        if (affected == 0) {
                            System.out.println("Attività non trovata.");
                            return;
                        }
                    }

                    // 4. Verifica se la checklist è completamente completata
                    String checkQuery = "SELECT COUNT(*) AS incompleti FROM activities WHERE checklist_id = ? AND state = false";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                        checkStmt.setInt(1, checklistId);
                        ResultSet rsCheck = checkStmt.executeQuery();

                        if (rsCheck.next()) {
                            int incompleti = rsCheck.getInt("incompleti");

                            // 5. Aggiorna lo stato del ToDo (completato solo se tutte le attività lo sono)
                            String updateTodo = "UPDATE todos SET state = ? WHERE id = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateTodo)) {
                                updateStmt.setBoolean(1, incompleti == 0);
                                updateStmt.setInt(2, todoId);
                                updateStmt.executeUpdate();
                            }
                        }
                    }

                    System.out.println("Attività rimossa con successo.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la rimozione dell'attività: " + e.getMessage());
            e.printStackTrace();
        }
    }



    // Aggiorna lo stato e la data di completamento di una attività
    public void aggiornaActivity(String todoTitle, Activity activity) {
        String sql = "UPDATE activity SET state = ?, completion_date = ? WHERE name = ? AND todo_title = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, activity.getState());
            stmt.setString(2, activity.getCompletionDate());
            stmt.setString(3, activity.getName());
            stmt.setString(4, todoTitle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkActivity(String email, String board, String todo, String activity, String dataCompletamento) {
        String sql = "UPDATE activities SET state = ?, completion_date = ? " +
                "WHERE name = ? AND checklist_id = (" +
                "SELECT c.id FROM checklists c " +
                "JOIN todos t ON c.id = t.checklist_id " +
                "JOIN boards b ON t.board_id = b.id " +
                "WHERE b.user_email = ? AND b.type = ? AND t.title = ? LIMIT 1)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, true);

            if (dataCompletamento != null && !dataCompletamento.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                java.util.Date parsedDate = sdf.parse(dataCompletamento);
                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                stmt.setDate(2, sqlDate);
            } else {
                stmt.setDate(2, null);
            }

            stmt.setString(3, activity);
            stmt.setString(4, email);
            stmt.setString(5, board.toUpperCase());
            stmt.setString(6, todo);

            int updatedRows = stmt.executeUpdate();

            if (updatedRows == 0) {
                System.out.println("Nessuna attività aggiornata, controlla i parametri.");
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
    }


    public boolean uncheckActivity(String email, String board, String todo, String activity) {
        String sql = "UPDATE activities SET state = ?, completion_date = ? " +
                "WHERE name = ? AND checklist_id = (" +
                "SELECT c.id FROM checklists c " +
                "JOIN todos t ON c.id = t.checklist_id " +
                "JOIN boards b ON t.board_id = b.id " +
                "WHERE b.user_email = ? AND b.type = ? AND t.title = ? LIMIT 1)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, false);
            stmt.setNull(2, java.sql.Types.DATE);
            stmt.setString(3, activity);
            stmt.setString(4, email);
            stmt.setString(5, board.toUpperCase());
            stmt.setString(6, todo);

            int updatedRows = stmt.executeUpdate();

            return updatedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



}

