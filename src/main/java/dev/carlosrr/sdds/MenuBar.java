package dev.carlosrr.sdds;

import dev.carlosrr.sdds.util.ConfigManager;
import dev.carlosrr.sdds.util.DatabaseManager;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MenuBar extends JMenuBar {

    private final ContentPanel contentPanel;

    public MenuBar(ContentPanel contentPanel) {
        this.contentPanel = contentPanel;

        JMenu actionsMenu = getJMenu();
        JMenu helpMenu = new JMenu("Help");

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> System.out.println("About selected"));

        helpMenu.add(aboutItem);

        add(actionsMenu);
        add(helpMenu);
    }


    private JMenu getJMenu() {
        JMenu actionsMenu = new JMenu("Actions");

        JMenuItem createNewDBItem = new JMenuItem("Create new DB file");
        createNewDBItem.addActionListener(e -> {
            new CreateDBDialog();
        });

        JMenuItem loadExistingDBItem = new JMenuItem("Load existing DB");
        loadExistingDBItem.addActionListener(e -> loadExistingDB());

        actionsMenu.add(createNewDBItem);
        actionsMenu.add(loadExistingDBItem);
        return actionsMenu;
    }

    private void loadExistingDB() {
        JFileChooser fileChooser = getJFileChooser();

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String dbPath = selectedFile.getAbsolutePath();
            ConfigManager.setLastDatabasePath(dbPath);

            // Validate the database before loading
            if (DatabaseManager.validateDatabase(dbPath)) {
                DatabaseManager.setCurrentDbPath(dbPath);
                contentPanel.updateDbFileStatus(dbPath);
                contentPanel.loadDatabaseData("");  // Load all records
                JOptionPane.showMessageDialog(this,
                        "Database loaded successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid database format. Please select a valid student records database.",
                        "Invalid Database",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static JFileChooser getJFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select SQLite Database File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".db");
            }

            @Override
            public String getDescription() {
                return "SQLite Database Files (*.db)";
            }
        });
        return fileChooser;
    }
}