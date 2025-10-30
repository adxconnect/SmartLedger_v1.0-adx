package src.UI;

import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import src.FinanceManager;
import src.auth.AuthManager;
import src.auth.SessionContext;
import src.db.DBHelper;

/**
 * Entry point that enforces authentication before showing the full Finance Manager UI.
 */
public final class FinanceManagerApp {

    private FinanceManagerApp() {
        // Utility class
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FinanceManagerApp::launch);
    }

    private static void launch() {
        SessionContext.clear();
        DBHelper helper = null;
        try {
            helper = new DBHelper();
            AuthManager authManager = new AuthManager(helper.getConnection());
            LoginDialog dialog = new LoginDialog(null, authManager);
            dialog.setVisible(true);
            if (!dialog.isSucceeded() || !SessionContext.isLoggedIn()) {
                JOptionPane.showMessageDialog(null, "Sign-in is required to use Finance Manager.");
                System.exit(0);
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Unable to initialize authentication: " + ex.getMessage(), "Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        } finally {
            if (helper != null) {
                helper.close();
            }
        }

        try {
            FinanceManager migrationManager = new FinanceManager();
            try {
                migrationManager.backfillLegacyRecordsToCurrentAccount();
            } finally {
                migrationManager.close();
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Session Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Legacy data migration warning: " + ex.getMessage(), "Migration Warning", JOptionPane.WARNING_MESSAGE);
        }

        try {
            FinanceManagerFullUI ui = new FinanceManagerFullUI();
            ui.setVisible(true);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
