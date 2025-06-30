package dao;

import db.ConnessioneDatabase;
import model.Activity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO {

    private Connection conn;

    public ActivityDAO() {
        conn = ConnessioneDatabase.getInstance().getConnection();
    }


}
