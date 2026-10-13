package util;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.sql.SQLException;

/**
 * Utility for user-friendly dialog messages and file choosers.
 */
public class DialogUtil {

    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title != null ? title : "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title != null ? title : "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title != null ? title : "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int choice = JOptionPane.showConfirmDialog(
            parent,
            message,
            title != null ? title : "Confirm Action",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return choice == JOptionPane.YES_OPTION;
    }

    /**
     * Translates SQLExceptions into clear, user-friendly messages for viva demonstration.
     */
    public static void handleSQLException(Component parent, SQLException ex, String operationContext) {
        String message;
        int errorCode = ex.getErrorCode();

        switch (errorCode) {
            case 1: // ORA-00001: Unique constraint violated
                message = "Duplicate record detected. A unique constraint was violated.\n(" + ex.getMessage() + ")";
                break;
            case 2292: // ORA-02292: Integrity constraint violated - child record found
                message = "Cannot delete or alter this record because related child records depend on it.\n" +
                          "Please cancel or remove dependent records first.";
                break;
            case 2291: // ORA-02291: Integrity constraint violated - parent key not found
                message = "Referenced parent record does not exist in the database.";
                break;
            case 1400: // ORA-01400: cannot insert NULL into
                message = "A required database field cannot be empty (NULL constraint violated).";
                break;
            case 1017: // ORA-01017: invalid username/password
                message = "Database connection error: Invalid database username or password.";
                break;
            case 12541: // ORA-12541: TNS:no listener
                message = "Oracle Database Listener is not running on the specified host/port.";
                break;
            default:
                message = ex.getMessage();
                break;
        }

        System.err.println("[SQLException] Operation: " + operationContext + " | Code: " + errorCode + " | " + ex.getMessage());
        showError(parent, operationContext + ":\n\n" + message, "Database Error (ORA-" + errorCode + ")");
    }

    /**
     * Prompts the user to choose a CSV destination path.
     */
    public static File chooseCsvSaveFile(Component parent, String defaultFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Report to CSV");
        chooser.setSelectedFile(new File(defaultFileName));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Comma Delimited (*.csv)", "csv"));
        int res = chooser.showSaveDialog(parent);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".csv")) {
                f = new File(f.getParentFile(), f.getName() + ".csv");
            }
            return f;
        }
        return null;
    }
}
