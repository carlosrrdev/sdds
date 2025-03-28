package dev.carlosrr.sdds;

import dev.carlosrr.sdds.util.DatabaseCreator;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class CreateDBDialog extends JDialog {

    private final JTextField directoryField;
    private final JButton createButton;
    private final JProgressBar progressBar;

    private String selectedDirectory = "";

    public CreateDBDialog() {
        // Configure the dialog
        setTitle("Create new database file");
        setSize(500, 200);
        setLocationRelativeTo(null);  // Center on screen
        setModal(true);  // Makes dialog modal
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Main content panel with some padding
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Directory selection components
        JLabel directoryLabel = new JLabel("Select Directory:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        contentPanel.add(directoryLabel, gbc);

        directoryField = new JTextField(20);
        directoryField.setEditable(false);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        contentPanel.add(directoryField, gbc);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> selectDirectory());
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        contentPanel.add(browseButton, gbc);

        // Create DB button
        createButton = new JButton("Create DB");
        createButton.addActionListener(e -> createDatabase());
        createButton.setEnabled(false); // Disabled until directory selected
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        contentPanel.add(createButton, gbc);

        // Add content panel to the dialog
        add(contentPanel, BorderLayout.CENTER);

        // Progress bar panel at the bottom
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true); // Show percentage text
        progressBar.setString("Ready");
        progressBar.setValue(0);

        progressPanel.add(progressBar, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);

        // Show the dialog
        setVisible(true);
    }

    private void selectDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Disable the "All files" option
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedDirectory = selectedFile.getAbsolutePath();
            directoryField.setText(selectedDirectory);
            createButton.setEnabled(true); // Enable the create button now
        }
    }

    private void createDatabase() {
        // Disable the create button while processing
        createButton.setEnabled(false);

        // Reset progress bar
        progressBar.setValue(0);
        progressBar.setString("Starting...");

        // Use SwingWorker to avoid freezing the UI
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return DatabaseCreator.createDatabase(selectedDirectory, progressBar);
            }

            @Override
            protected void done() {
                try {
                    String dbPath = get();
                    JOptionPane.showMessageDialog(
                            CreateDBDialog.this,
                            "Database created successfully at:\n" + dbPath,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    dispose(); // Close dialog after successful completion
                } catch (Exception e) {
                    progressBar.setValue(0);
                    progressBar.setString("Error");
                    JOptionPane.showMessageDialog(
                            CreateDBDialog.this,
                            "Error creating database: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    createButton.setEnabled(true); // Re-enable button to try again
                }
            }
        };

        worker.execute();
    }
}