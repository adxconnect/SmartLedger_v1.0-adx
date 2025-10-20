package src.UI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import src.FinanceManager;
import src.Transaction;
import java.sql.SQLException;
import java.util.List;

public class FinanceManagerFullUI extends JFrame {
    private FinanceManager manager;
    private DefaultTableModel transactionModel;
    private JTable transactionTable;

    public FinanceManagerFullUI() {
        setTitle("Finance Manager - MySQL Edition");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            manager = new FinanceManager(); // Connect DB
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "MySQL connection failed: " + e.getMessage());
            System.exit(1);
        }

        String[] cols = {"Date", "Category", "Type", "Amount", "Description"};
        transactionModel = new DefaultTableModel(cols, 0);
        transactionTable = new JTable(transactionModel);
        add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton addBtn = new JButton("Add Transaction");
        controls.add(addBtn);
        add(controls, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openTransactionDialog());
        refreshTransactions();
    }

    private void refreshTransactions() {
        transactionModel.setRowCount(0);
        try {
            List<Transaction> txs = manager.getAllTransactions();
            for (Transaction t : txs) {
                transactionModel.addRow(new Object[]{t.getDate(), t.getCategory(), t.getType(), t.getAmount(), t.getDescription()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "ErrorLoading: " + e.getMessage());
        }
    }

    private void openTransactionDialog() {
        JDialog dlg = new JDialog(this, "New Transaction", true);
        dlg.setLayout(new GridLayout(6, 2));
        JTextField dateF = new JTextField();
        JTextField catF = new JTextField();
        JTextField typeF = new JTextField();
        JTextField amtF = new JTextField();
        JTextField descF = new JTextField();
        dlg.add(new JLabel("Date")); dlg.add(dateF);
        dlg.add(new JLabel("Category")); dlg.add(catF);
        dlg.add(new JLabel("Type (Income/Expense)")); dlg.add(typeF);
        dlg.add(new JLabel("Amount")); dlg.add(amtF);
        dlg.add(new JLabel("Description")); dlg.add(descF);
        JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
        dlg.add(ok); dlg.add(cancel);
        ok.addActionListener(ev -> {
            try {
                Transaction t = new Transaction(
                    dateF.getText(),
                    catF.getText(),
                    typeF.getText(),
                    Double.parseDouble(amtF.getText()),
                    descF.getText()
                );
                manager.saveTransaction(t);
                dlg.dispose();
                refreshTransactions();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
            }
        });
        cancel.addActionListener(ev -> dlg.dispose());
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinanceManagerFullUI().setVisible(true));
    }
}
