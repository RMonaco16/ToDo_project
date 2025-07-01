package dao;

import db.DatabaseConnection;

import java.sql.*;

public class ActivityDAO {

    private Connection conn;

    public ActivityDAO() {
        conn = DatabaseConnection.getInstance().getConnection();
    }


}
