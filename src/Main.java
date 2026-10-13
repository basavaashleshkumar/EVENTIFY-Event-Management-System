import db.DatabaseConnection;
import ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main Entry Point for EVENTIFY - Event Management System.
 * College Database Systems Lab Project demonstrating Java Swing + JDBC + Oracle Database.
 */
public class Main {

    public static void main(String[] args) {
        // Enable High-DPI font rendering and anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            // Set System Look and Feel for native OS aesthetics
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Main] Could not set System Look and Feel: " + e.getMessage());
        }

        System.out.println("=================================================");
        System.out.println("   EVENTIFY - College Event Management System    ");
        System.out.println("      Java Swing + JDBC + Oracle Database        ");
        System.out.println("=================================================");

        // Test database connectivity
        boolean dbOk = DatabaseConnection.testConnection();
        if (dbOk) {
            System.out.println("[Main] Oracle Database connection test: SUCCESSFUL!");
        } else {
            System.err.println("[Main] WARNING: Oracle Database connection failed.");
            System.err.println("[Main] Verify config/db.properties and ensure Oracle XE service is running.");
        }

        // Launch Login GUI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
