package dev.carlosrr.sdds;

import javax.swing.*;

import com.formdev.flatlaf.FlatDarkLaf;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
       SwingUtilities.invokeLater(MainFrame::new);
    }
}
