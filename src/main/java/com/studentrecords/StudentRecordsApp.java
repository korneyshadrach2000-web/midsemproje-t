package com.studentrecords;

import javax.swing.*;

/**
 * Entry point for the Student Records Manager (offline desktop application).
 */
public class StudentRecordsApp {
    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Startup Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
