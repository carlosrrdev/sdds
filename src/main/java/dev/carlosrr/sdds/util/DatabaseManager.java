package dev.carlosrr.sdds.util;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static String currentDbPath = null;

    /**
     * Sets the current database path
     * @param dbPath Path to the SQLite database file
     */
    public static void setCurrentDbPath(String dbPath) {
        currentDbPath = dbPath;
    }

    /**
     * Gets the current database path
     * @return Current database path or null if not set
     */
    public static String getCurrentDbPath() {
        return currentDbPath;
    }

    /**
     * Validates if the selected file is a valid SQLite database with the expected schema
     * @param dbPath Path to the database file
     * @return true if valid, false otherwise
     */
    public static boolean validateDatabase(String dbPath) {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the database
            String url = "jdbc:sqlite:" + dbPath;
            try (Connection conn = DriverManager.getConnection(url)) {
                // Check if the students table exists with expected columns
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT lastname, firstname, dob, dir_path FROM directories LIMIT 1")) {
                    // If we got here without exception, the table and columns exist
                    return true;
                } catch (SQLException e) {
                    // Table doesn't exist or has wrong schema
                    return false;
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            return false;
        }
    }

    /**
     * Searches for records in the database matching the search term
     * @param searchTerm The search term to match against any field
     * @return TableModel populated with matching records
     */
    public static DefaultTableModel searchRecords(String searchTerm) {
        if (currentDbPath == null) {
            return new DefaultTableModel(new String[]{"lastname", "firstname", "date_of_birth"}, 0);
        }

        String[] columnNames = {"lastname", "firstname", "date_of_birth"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + currentDbPath;
            String query = "SELECT lastname, firstname, dob FROM directories WHERE " +
                    "lastname LIKE ? OR firstname LIKE ? OR dob LIKE ?";

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                String searchPattern = "%" + searchTerm + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String lastName = rs.getString("lastname");
                        String firstName = rs.getString("firstname");
                        String dob = rs.getString("dob");

                        model.addRow(new Object[]{lastName, firstName, dob});
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    /**
     * Loads all records from the database
     * @return TableModel populated with all records
     */
    public static DefaultTableModel loadAllRecords() {
        return searchRecords("");  // Empty search returns all records
    }
}