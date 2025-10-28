package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import src.Deposit;
import src.FinanceManager;

public class GullakDialog extends JDialog {

    private FinanceManager manager;
    private Deposit gullakDeposit; // The specific Gullak deposit being managed
    private FinanceManagerFullUI parentUI;

    // UI Components
    private Map<Integer, JTextField> countFields;
    private JLabel totalLabel;
    private JLabel dueAmountLabel;
    private JLabel lastUpdatedLabel;
    private static final int[] DENOMINATIONS = {500, 200, 100, 50, 20, 10, 5, 2, 1};
    private static final DecimalFormat currencyFormat = new DecimalFormat("₹ #,##0.00");

    public GullakDialog(Frame owner, FinanceManager manager, Deposit gullakDeposit, FinanceManagerFullUI parentUI) {
        super(owner, "Manage Gullak: " + (gullakDeposit.getHolderName() != null ? gullakDeposit.getHolderName() : gullakDeposit.getDescription()), true);
        this.manager = manager;
        this.gullakDeposit = gullakDeposit;
        this.parentUI = parentUI;
        this.countFields = new HashMap<>();

        initComponents();
        loadGullakData();

        //pack(); // Auto-size based on components
        setSize(450, 600); // Set a fixed size might be better here
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // --- Top Info Panel ---
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        totalLabel = new JLabel("Current Total: ₹ 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        dueAmountLabel = new JLabel("Amount Due (Withdrawn): ₹ 0.00");
        dueAmountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lastUpdatedLabel = new JLabel("Last Updated: Never");
        infoPanel.add(totalLabel);
        infoPanel.add(dueAmountLabel);
        infoPanel.add(lastUpdatedLabel);
        add(infoPanel, BorderLayout.NORTH);

        // --- Denomination Counts Panel ---
        JPanel countsPanel = new JPanel(new GridBagLayout());
        countsPanel.setBorder(BorderFactory.createTitledBorder("Denomination Counts"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        for (int denom : DENOMINATIONS) {
            gbc.gridx = 0;
            gbc.gridy = row;
            countsPanel.add(new JLabel("₹" + denom + " x "), gbc);

            gbc.gridx = 1;
            JTextField countField = new JTextField("0", 5); // Field width 5
            countsPanel.add(countField, gbc);
            countFields.put(denom, countField);

            row++;
        }
        // Add a flexible vertical space at the bottom
        gbc.gridy = row; gbc.weighty = 1.0;
        countsPanel.add(new Box.Filler(new Dimension(0,0), new Dimension(0,0), new Dimension(0, Short.MAX_VALUE)), gbc);

        add(new JScrollPane(countsPanel), BorderLayout.CENTER); // Put counts in scroll pane

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0)); // Use GridLayout for equal button sizes
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JButton updateCountsButton = new JButton("Update Counts");
        JButton withdrawButton = new JButton("Withdraw");
        JButton depositButton = new JButton("Deposit/Clear Due");
        JButton closeButton = new JButton("Close");

        buttonPanel.add(updateCountsButton);
        buttonPanel.add(withdrawButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        updateCountsButton.addActionListener(e -> updateCounts());
        withdrawButton.addActionListener(e -> performWithdrawal());
        depositButton.addActionListener(e -> performDeposit());
        closeButton.addActionListener(e -> dispose());
    }

    private void loadGullakData() {
        Map<Integer, Integer> counts = gullakDeposit.getDenominationCounts();
        if (counts != null) {
            for (int denom : DENOMINATIONS) {
                JTextField field = countFields.get(denom);
                if (field != null) {
                    field.setText(String.valueOf(counts.getOrDefault(denom, 0)));
                }
            }
        }
        updateDisplayLabels(); // Update total, due, last updated
    }

    private void updateDisplayLabels() {
        double calculatedTotal = calculateTotalFromFields();
        totalLabel.setText("Current Total: " + currencyFormat.format(calculatedTotal));
        dueAmountLabel.setText("Amount Due (Withdrawn): " + currencyFormat.format(gullakDeposit.getGullakDueAmount()));
        lastUpdatedLabel.setText("Last Updated: " + (gullakDeposit.getLastUpdated() != null ? gullakDeposit.getLastUpdated() : "Never"));
    }

    private double calculateTotalFromFields() {
        double total = 0;
        try {
            for (int denom : DENOMINATIONS) {
                JTextField field = countFields.get(denom);
                if (field != null) {
                    int count = Integer.parseInt(field.getText().trim());
                    if (count < 0) count = 0; // No negative counts
                    total += denom * count;
                }
            }
        } catch (NumberFormatException e) {
            // Handle error - maybe show a message or return -1?
            System.err.println("Invalid number in count field.");
            return -1; // Indicate error
        }
        return total;
    }

    // --- Action Methods ---

    private void updateCounts() {
        try {
            Map<Integer, Integer> newCounts = new HashMap<>();
            for (int denom : DENOMINATIONS) {
                JTextField field = countFields.get(denom);
                int count = Integer.parseInt(field.getText().trim());
                if (count < 0) {
                     JOptionPane.showMessageDialog(this, "Counts cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                     return; // Stop processing
                }
                newCounts.put(denom, count);
            }

            // Keep the current due amount when just updating counts
            double currentDue = gullakDeposit.getGullakDueAmount();

            manager.updateGullakDetails(gullakDeposit.getId(), newCounts, currentDue);
            // Reload data from DB to ensure consistency (optional but good practice)
             gullakDeposit.setDenominationCounts(newCounts); // Update local object too
             gullakDeposit.calculateTotalFromDenominations(); // Recalculate local total

            updateDisplayLabels(); // Update display
            parentUI.refreshDeposits(); // Refresh the main list
            JOptionPane.showMessageDialog(this, "Counts updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number entered in count fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "Database error updating counts: " + sqle.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performWithdrawal() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter amount to withdraw:", "Withdraw Cash", JOptionPane.QUESTION_MESSAGE);
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return; // User cancelled
        }

        try {
            double amount = Double.parseDouble(amountStr.trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Withdrawal amount must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double currentTotal = calculateTotalFromFields();
            if(currentTotal < 0) { // Check if calculation failed
                 JOptionPane.showMessageDialog(this, "Invalid counts entered. Cannot calculate total.", "Input Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            
            // For simplicity, we just increase the due amount.
            // A more complex version could try to deduce which notes were removed.
            double newDueAmount = gullakDeposit.getGullakDueAmount() + amount;
            
            // Keep the current counts as they are in the fields
            Map<Integer, Integer> currentCounts = new HashMap<>();
             for (int denom : DENOMINATIONS) {
                 currentCounts.put(denom, Integer.parseInt(countFields.get(denom).getText().trim()));
             }

            // Update DB with current counts and NEW due amount
            manager.updateGullakDetails(gullakDeposit.getId(), currentCounts, newDueAmount);
            
            // Update local object
            gullakDeposit.setGullakDueAmount(newDueAmount);
            // No need to set counts again as they didn't change logic-wise

            updateDisplayLabels(); // Update display
            parentUI.refreshDeposits(); // Refresh the main list
            JOptionPane.showMessageDialog(this, "Withdrawal recorded. Amount due updated.", "Withdrawal", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "Database error recording withdrawal: " + sqle.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void performDeposit() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter amount deposited / clearing due:", "Deposit Cash", JOptionPane.QUESTION_MESSAGE);
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return; // User cancelled
        }

        try {
            double amount = Double.parseDouble(amountStr.trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Deposit amount must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double currentDue = gullakDeposit.getGullakDueAmount();
            double newDueAmount = Math.max(0, currentDue - amount); // Reduce due, don't go below zero
            double amountAppliedToDue = currentDue - newDueAmount;
            // Any remaining amount is considered a 'new' deposit, although we don't track it separately here

            // Important: We assume the user has ALREADY updated the counts fields
            // before clicking this button to reflect the new cash added.
            Map<Integer, Integer> currentCounts = new HashMap<>();
             for (int denom : DENOMINATIONS) {
                  int count = Integer.parseInt(countFields.get(denom).getText().trim());
                  if (count < 0) throw new NumberFormatException("Negative count");
                 currentCounts.put(denom, count);
             }

            // Update DB with the counts from fields and the NEW due amount
            manager.updateGullakDetails(gullakDeposit.getId(), currentCounts, newDueAmount);
            
            // Update local object
            gullakDeposit.setDenominationCounts(currentCounts);
            gullakDeposit.setGullakDueAmount(newDueAmount);
            gullakDeposit.calculateTotalFromDenominations(); // Recalculate local total

            updateDisplayLabels(); // Update display
            parentUI.refreshDeposits(); // Refresh the main list
            JOptionPane.showMessageDialog(this, "Deposit recorded. Amount due reduced by " + currencyFormat.format(amountAppliedToDue) + ".", "Deposit", JOptionPane.INFORMATION_MESSAGE);


        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid amount or count entered.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "Database error recording deposit: " + sqle.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}