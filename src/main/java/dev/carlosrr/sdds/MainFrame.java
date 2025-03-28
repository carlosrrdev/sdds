package dev.carlosrr.sdds;

import dev.carlosrr.sdds.util.ConfigManager;
import dev.carlosrr.sdds.util.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Student Directory Database Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);  // Center on screen
        setLayout(new BorderLayout());

        // Create content panel
        ContentPanel contentPanel = new ContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        // Create menu bar with reference to content panel
        MenuBar menuBar = new MenuBar(contentPanel);
        setJMenuBar(menuBar);

        // Make window visible
        setVisible(true);
        loadLastUsedDatabase(contentPanel);
    }

    private void loadLastUsedDatabase(ContentPanel contentPanel) {
        String lastDbPath = ConfigManager.getLastDatabasePath();
        if (lastDbPath == null || lastDbPath.isEmpty()) {
            return; // No last database to load
        }

        File dbFile = new File(lastDbPath);
        if (!dbFile.exists() || !dbFile.isFile()) {
            System.out.println("Last used database file no longer exists: " + lastDbPath);
            ConfigManager.clearLastDatabasePath();
            return;
        }

        // Try to load the database
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + lastDbPath;

            try (Connection conn = DriverManager.getConnection(url)) {
                if (DatabaseManager.validateDatabase(lastDbPath)) {
                    DatabaseManager.setCurrentDbPath(lastDbPath);
                    contentPanel.updateDbFileStatus(lastDbPath);
                    contentPanel.loadDatabaseData("");  // Load all records
                    System.out.println("Successfully loaded last used database: " + lastDbPath);
                } else {
                    System.out.println("Last used database has invalid schema: " + lastDbPath);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading last used database: " + e.getMessage());
        }
    }

}