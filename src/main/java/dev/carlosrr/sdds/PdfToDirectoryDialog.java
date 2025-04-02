package dev.carlosrr.sdds;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PdfToDirectoryDialog extends JDialog {

    private final JTextField entryDirectoryField;
    private final JTextField outputDirectoryField;
    private final JButton convertButton;
    private final JProgressBar progressBar;
    private String entryDirectory = "";
    private String outputDirectory = "";

    public PdfToDirectoryDialog() {
        setTitle("PDF to Directory Converter");
        setSize(500, 200);
        setLocationRelativeTo(null);
        setModal(true);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel entryDirectoryLabel = new JLabel("Entry Directory:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        contentPanel.add(entryDirectoryLabel, gbc);
        entryDirectoryField = new JTextField(20);
        entryDirectoryField.setEditable(true);
        gbc.gridx = 1;
        gbc.gridy = 0;
        contentPanel.add(entryDirectoryField, gbc);
        JButton entryDirectoryButton = new JButton("Browse");
        entryDirectoryButton.addActionListener(e -> setEntryDirectory());
        gbc.gridx = 2;
        gbc.gridy = 0;
        contentPanel.add(entryDirectoryButton, gbc);

        JLabel outputDirectoryLabel = new JLabel("Output Directory:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        contentPanel.add(outputDirectoryLabel, gbc);
        outputDirectoryField = new JTextField(20);
        outputDirectoryField.setEditable(true);
        gbc.gridx = 1;
        gbc.gridy = 1;
        contentPanel.add(outputDirectoryField, gbc);
        JButton outputDirectoryButton = new JButton("Browse");
        outputDirectoryButton.addActionListener(e -> setOutputDirectory());
        gbc.gridx = 2;
        gbc.gridy = 1;
        contentPanel.add(outputDirectoryButton, gbc);

        convertButton = new JButton("Start Conversion");
        convertButton.addActionListener(e -> beginConversion());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        contentPanel.add(convertButton, gbc);

        add(contentPanel, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setValue(0);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
    private void setEntryDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Entry Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            entryDirectory = selectedFile.getAbsolutePath();
            entryDirectoryField.setText(entryDirectory);
        }
    }
    private void setOutputDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Output Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            outputDirectory = selectedFile.getAbsolutePath();
            outputDirectoryField.setText(outputDirectory);
        }
    }

    private void beginConversion() {
        // Disable the convert button to prevent multiple clicks
        convertButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("Starting...");

        // Create a new SwingWorker
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Call the convert method here
                convert();
                return null;
            }

            @Override
            protected void done() {
                // Re-enable the convert button
                convertButton.setEnabled(true);
                progressBar.setString("Conversion finished");
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(PdfToDirectoryDialog.this,
                            "Conversion completed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                    JOptionPane.showMessageDialog(PdfToDirectoryDialog.this,
                            "Error during conversion: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };

        // Execute the worker
        worker.execute();
    }

    private void convert() {
        if (entryDirectory.isEmpty() || outputDirectory.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select both entry and output directories",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File entryDir = new File(entryDirectory);
        File outputDir = new File(outputDirectory);

        if (!entryDir.exists() || !entryDir.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                    "Entry directory does not exist",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Get all PDF files in the entry directory
        File[] pdfFiles = entryDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (pdfFiles == null || pdfFiles.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "No PDF files found in the entry directory",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        progressBar.setMaximum(pdfFiles.length);
        progressBar.setValue(0);

        // Process each PDF file
        for (int i = 0; i < pdfFiles.length; i++) {
            File pdfFile = pdfFiles[i];
            String fileName = pdfFile.getName();

            // Remove .pdf extension
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

            // Generate random 4-digit ID
            int randomId = 1000 + (int)(Math.random() * 9000);

            // Create new directory name (uppercase + ID)
            String newDirName = baseName.toUpperCase() + "_" + randomId;

            // Create the new directory in the output directory
            File newDir = new File(outputDir, newDirName);
            if (!newDir.exists()) {
                newDir.mkdir();
            }

            // Copy the PDF file to the new directory
            try {
                File destFile = new File(newDir, fileName);
                java.nio.file.Files.copy(
                        pdfFile.toPath(),
                        destFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            } catch (java.io.IOException e) {
                System.err.println("Error copying file: " + pdfFile.getName());
                e.printStackTrace();
            }

            // Update progress bar
            final int finalI = i + 1;
            SwingUtilities.invokeLater(() -> {
               progressBar.setValue(finalI + 1);
            });
        }
    }
}
