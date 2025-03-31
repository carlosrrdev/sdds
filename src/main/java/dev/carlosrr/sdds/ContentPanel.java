package dev.carlosrr.sdds;

import dev.carlosrr.sdds.util.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ContentPanel extends JPanel {

    private final JLabel statusLabel;
    private final JTextField searchField;
    private final JTable resultsTable;
    private final DefaultTableModel tableModel;
    private static final int DATE_COLUMN_INDEX = 2; // Index of date_of_birth column

    public ContentPanel() {
        // Set layout
        setLayout(new BorderLayout());

        // Create search field with label
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel searchLabel = new JLabel("Search: ");
        searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Search as the user types
                loadDatabaseData(searchField.getText());
            }
        });

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        add(searchPanel, BorderLayout.NORTH);

        // Create table with specified columns
        String[] columnNames = {"lastname", "firstname", "date_of_birth"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make cells non-editable
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        // Set a custom renderer for the date column
        resultsTable.getColumnModel().getColumn(DATE_COLUMN_INDEX).setCellRenderer(new DateCellRenderer());
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setCellSelectionEnabled(false);
        resultsTable.setRowSelectionAllowed(true);
        resultsTable.setColumnSelectionAllowed(false);


        // Add double-click listener to open directory
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    int row = resultsTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        openDirectoryForRow(row);
                    }
                }
            }
        });

        // Add table to scroll pane to enable scrolling
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create a status label at the bottom
        statusLabel = new JLabel("No DB file loaded");
        statusLabel.setForeground(new Color(239, 93, 93));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add some padding around the status label
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // Add the status panel to the bottom of the content panel
        add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * Opens the directory for the selected row
     */
    private void openDirectoryForRow(int row) {
        if (DatabaseManager.getCurrentDbPath() == null) {
            JOptionPane.showMessageDialog(this,
                    "No database is currently loaded.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get lastname, firstname, and date_of_birth from the selected row
        String lastName = (String) tableModel.getValueAt(row, 0);
        String firstName = (String) tableModel.getValueAt(row, 1);
        String dateOfBirth = (String) tableModel.getValueAt(row, 2);

        // Convert formatted date back to database format if needed
        if (dateOfBirth.contains("/")) {
            dateOfBirth = dateOfBirth.replaceAll("/", "");
        }

        // Retrieve dir_path from database using these values
        String dirPath = getDirectoryPath(lastName, firstName, dateOfBirth);

        if (dirPath != null && !dirPath.isEmpty()) {
            openDirectory(dirPath);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not find directory path for selected record.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Retrieves directory path from database for the given student record
     */
    private String getDirectoryPath(String lastName, String firstName, String dateOfBirth) {
        String dirPath = null;

        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DatabaseManager.getCurrentDbPath();

            String query = "SELECT dir_path FROM directories WHERE " +
                    "lastname = ? AND firstname = ? AND dob = ?";

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, lastName);
                pstmt.setString(2, firstName);
                pstmt.setString(3, dateOfBirth);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dirPath = rs.getString("dir_path");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error retrieving directory path: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return dirPath;
    }

    /**
     * Opens the directory in the system file explorer
     */
    private void openDirectory(String dirPath) {
        try {
            File directory = new File(dirPath);

            // Check if directory exists
            if (!directory.exists()) {
                JOptionPane.showMessageDialog(this,
                        "Directory does not exist: " + dirPath,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if Desktop is supported
            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(this,
                        "Desktop operations are not supported on this platform.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Open the directory
            Desktop desktop = Desktop.getDesktop();
            desktop.open(directory);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error opening directory: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates the database file status label
     */
    public void updateDbFileStatus(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            statusLabel.setText("No DB file loaded");
            statusLabel.setForeground(new Color(208, 103, 103));
        } else {
            statusLabel.setText("DB file: " + filePath);
            statusLabel.setForeground(new Color(17, 180, 109)); // Dark green
        }

        // Reset search field when changing databases
        searchField.setText("");
    }

    /**
     * Loads data from the database into the table
     */
    public void loadDatabaseData(String searchTerm) {
        DefaultTableModel newModel = DatabaseManager.searchRecords(searchTerm);
        tableModel.setRowCount(0); // Clear existing rows

        // Copy rows from the new model to the existing model
        for (int i = 0; i < newModel.getRowCount(); i++) {
            Object[] rowData = new Object[3];
            for (int j = 0; j < 3; j++) {
                rowData[j] = newModel.getValueAt(i, j);
            }
            tableModel.addRow(rowData);
        }

        // Update the status label with record count
        int recordCount = tableModel.getRowCount();
        String dbPath = DatabaseManager.getCurrentDbPath();
        if (dbPath != null && !dbPath.isEmpty()) {
            statusLabel.setText("DB file: " + dbPath + " | Records found: " + recordCount);
        }
    }

    /**
     * Custom cell renderer to format date strings
     */
    private static class DateCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            // Get the default renderer component
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                String dateStr = value.toString().trim();

                // Format only if the value looks like a date with the expected format
                if (dateStr.matches("\\d{8}")) {
                    try {
                        // Extract month, day, year
                        String month = dateStr.substring(0, 2);
                        String day = dateStr.substring(2, 4);
                        String year = dateStr.substring(4, 8);

                        // Format as MM/DD/YYYY
                        setText(month + "/" + day + "/" + year);
                    } catch (Exception e) {
                        // If formatting fails, use the original text
                        setText(dateStr);
                    }
                } else {
                    setText(dateStr);
                }
            }

            return c;
        }
    }
}