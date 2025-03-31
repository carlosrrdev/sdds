package dev.carlosrr.sdds.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PdfToDirectoryDialog extends JDialog {

    private final JTextField entryDirectoryField;
    private final JTextField outputDirectoryField;
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

        JButton convertButton = new JButton("Start Conversion");
        convertButton.addActionListener(e -> {
            System.out.println("start conversion button clicked");
            System.out.println(entryDirectory);
            System.out.println(outputDirectory);
            //TODO add logic
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        contentPanel.add(convertButton, gbc);

        add(contentPanel, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JProgressBar progressBar = new JProgressBar();
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
}
