package src.UI;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import src.CreditCard;
import src.FinanceManager;
import src.FixedDeposit;
import src.GoldSilverInvestment;
import src.MutualFund;
import src.RecurringDeposit;
import src.SavingsAccount;
import src.Transaction;
import java.sql.SQLException;
import java.util.List;

public class FinanceManagerFullUI extends JFrame {
    private FinanceManager manager;
    private DefaultTableModel transactionModel, bankModel, fdModel, rdModel, ccModel, gssModel, mfModel;
    private JTable transactionTable, bankTable, fdTable;

    public FinanceManagerFullUI() {
        setTitle("Finance Manager - MySQL Edition");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            manager = new FinanceManager(); // Connect DB
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "MySQL connection failed: " + e.getMessage());
            System.exit(1);
        }

        // JTabbedPane setup
        JTabbedPane tabs = new JTabbedPane();
        add(tabs);

        // ----- TRANSACTIONS PANEL -----
        String[] tcols = {"Date", "Category", "Type", "Amount", "Description"};
        transactionModel = new DefaultTableModel(tcols, 0);
        transactionTable = new JTable(transactionModel);
        JPanel tPanel = new JPanel(new BorderLayout());
        tPanel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);
        JButton addTxnBtn = new JButton("Add Transaction");
        addTxnBtn.addActionListener(e -> openTransactionDialog());
        tPanel.add(addTxnBtn, BorderLayout.SOUTH);
        tabs.addTab("Transactions", tPanel);
        refreshTransactions();

        // ----- BANK ACCOUNTS PANEL (Empty for now) -----
        String[] bcols = {"Account Number", "Account Type", "Balance", "Holder", "Bank Name", "IFSC"};
        bankModel = new DefaultTableModel(bcols, 0);
        bankTable = new JTable(bankModel);
        JPanel bPanel = new JPanel(new BorderLayout());
        bPanel.add(new JScrollPane(bankTable), BorderLayout.CENTER);
        JButton addBankBtn = new JButton("Add Account");
        addBankBtn.addActionListener(e -> openSavingsAccountDialog());
        bPanel.add(addBankBtn, BorderLayout.SOUTH);
        tabs.addTab("Bank Accounts", bPanel);
        refreshBanks();

String[] fdcols = {"Account Number", "Principal", "Rate", "Tenure (months)", "Start Date", "Holder"};
fdModel = new DefaultTableModel(fdcols, 0);
fdTable = new JTable(fdModel);
JPanel fdPanel = new JPanel(new BorderLayout());
fdPanel.add(new JScrollPane(fdTable), BorderLayout.CENTER);
JButton addFDBtn = new JButton("Add FD");
fdPanel.add(addFDBtn, BorderLayout.SOUTH);
tabs.addTab("Fixed Deposits", fdPanel);
addFDBtn.addActionListener(e -> openFixedDepositDialog());
refreshFixedDeposits();

String[] rdcols = {"Account Number","Monthly Amount","Rate","Tenure (months)","Start Date","Holder"};
rdModel = new DefaultTableModel(rdcols, 0);
JTable rdTable = new JTable(rdModel);
JPanel rdPanel = new JPanel(new BorderLayout());
rdPanel.add(new JScrollPane(rdTable), BorderLayout.CENTER);
JButton addRDBtn = new JButton("Add Recurring Deposit");
rdPanel.add(addRDBtn, BorderLayout.SOUTH);
tabs.addTab("Recurring Deposits", rdPanel);
addRDBtn.addActionListener(e -> openRecurringDepositDialog());
refreshRecurringDeposits();

String[] cccols = {"Card Name", "Credit Limit", "Expenses", "Amount To Pay", "Days Left"};
ccModel = new DefaultTableModel(cccols, 0);
JTable ccTable = new JTable(ccModel);
JPanel ccPanel = new JPanel(new BorderLayout());
ccPanel.add(new JScrollPane(ccTable), BorderLayout.CENTER);
JButton addCCBtn = new JButton("Add Credit Card");
ccPanel.add(addCCBtn, BorderLayout.SOUTH);
tabs.addTab("Credit Cards", ccPanel);

addCCBtn.addActionListener(e -> openCreditCardDialog());
refreshCreditCards();

// Gold & Silver Panel
String[] gssCols = {"Metal Type", "Weight (g)", "Price/g", "Total Value"};
gssModel = new DefaultTableModel(gssCols, 0);
JTable gssTable = new JTable(gssModel);
JPanel gssPanel = new JPanel(new BorderLayout());
gssPanel.add(new JScrollPane(gssTable), BorderLayout.CENTER);
JButton addGSSBtn = new JButton("Add Gold/Silver");
gssPanel.add(addGSSBtn, BorderLayout.SOUTH);
tabs.addTab("Gold/Silver Investments", gssPanel);
addGSSBtn.addActionListener(e -> openGoldSilverDialog());
refreshGoldSilver();

String[] mfCols = {"Amount Invested", "Annual Rate (%)", "Years", "Maturity Value"};
mfModel = new DefaultTableModel(mfCols, 0);
JTable mfTable = new JTable(mfModel);
JPanel mfPanel = new JPanel(new BorderLayout());
mfPanel.add(new JScrollPane(mfTable), BorderLayout.CENTER);
JButton addMFBtn = new JButton("Add Mutual Fund");
mfPanel.add(addMFBtn, BorderLayout.SOUTH);
tabs.addTab("Mutual Funds", mfPanel);
addMFBtn.addActionListener(e -> openMutualFundDialog());
refreshMutualFunds();

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
        cancel.addActionListener(_ -> dlg.dispose());
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
    private void openSavingsAccountDialog() {
    JDialog dlg = new JDialog(this, "Add Bank Account", true);
    dlg.setLayout(new GridLayout(7,2));
    JTextField accF = new JTextField();
    JTextField typeF = new JTextField();
    JTextField balF = new JTextField();
    JTextField holderF = new JTextField();
    JTextField bankF = new JTextField();
    JTextField ifscF = new JTextField();
    dlg.add(new JLabel("Account Number")); dlg.add(accF);
    dlg.add(new JLabel("Account Type (Savings/Current)")); dlg.add(typeF);
    dlg.add(new JLabel("Balance")); dlg.add(balF);
    dlg.add(new JLabel("Holder Name")); dlg.add(holderF);
    dlg.add(new JLabel("Bank Name")); dlg.add(bankF);
    dlg.add(new JLabel("IFSC Code")); dlg.add(ifscF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(_ -> {
        try {
            SavingsAccount sa = new SavingsAccount(
                accF.getText(), typeF.getText(), Double.parseDouble(balF.getText()),
                holderF.getText(), bankF.getText(), ifscF.getText()
            );
            manager.saveSavingsAccount(sa);
            dlg.dispose();
            // Add refreshBanks() to reload table from DB
            refreshBanks();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}
// Also inside your FinanceManagerFullUI class:
private void openFixedDepositDialog() {
    JDialog dlg = new JDialog(this, "Add Fixed Deposit", true);
    dlg.setLayout(new GridLayout(7, 2));
    JTextField accNumF = new JTextField();
    JTextField principalF = new JTextField();
    JTextField rateF = new JTextField();
    JTextField tenureF = new JTextField();
    JTextField startDateF = new JTextField();
    JTextField holderF = new JTextField();
    dlg.add(new JLabel("Account Number")); dlg.add(accNumF);
    dlg.add(new JLabel("Principal")); dlg.add(principalF);
    dlg.add(new JLabel("Rate (%)")); dlg.add(rateF);
    dlg.add(new JLabel("Tenure (Months)")); dlg.add(tenureF);
    dlg.add(new JLabel("Start Date (DD-MM-YYYY)")); dlg.add(startDateF);
    dlg.add(new JLabel("Holder Name")); dlg.add(holderF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            FixedDeposit fd = new FixedDeposit(
                accNumF.getText(), Double.parseDouble(principalF.getText()),
                Double.parseDouble(rateF.getText()), Integer.parseInt(tenureF.getText()),
                startDateF.getText(), holderF.getText()
            );
            manager.saveFixedDeposit(fd);
            dlg.dispose();
            refreshFixedDeposits();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}

private void refreshFixedDeposits() {
    fdModel.setRowCount(0);
    try {
        for (FixedDeposit fd : manager.getAllFixedDeposits())
            fdModel.addRow(fd.toObjectArray());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading FDs: " + e.getMessage());
    }
}

private void refreshBanks() {
    bankModel.setRowCount(0);
    try {
        for (SavingsAccount sa : manager.getAllSavingsAccounts()) {
            bankModel.addRow(sa.toObjectArray());
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "ErrorLoading: " + e.getMessage());
    }
}
private void openRecurringDepositDialog() {
    JDialog dlg = new JDialog(this, "Add Recurring Deposit", true);
    dlg.setLayout(new GridLayout(7, 2));
    JTextField accNumF = new JTextField();
    JTextField amountF = new JTextField();
    JTextField rateF = new JTextField();
    JTextField tenureF = new JTextField();
    JTextField startDateF = new JTextField();
    JTextField holderF = new JTextField();
    dlg.add(new JLabel("Account Number")); dlg.add(accNumF);
    dlg.add(new JLabel("Monthly Amount")); dlg.add(amountF);
    dlg.add(new JLabel("Rate (%)")); dlg.add(rateF);
    dlg.add(new JLabel("Tenure (Months)")); dlg.add(tenureF);
    dlg.add(new JLabel("Start Date (DD-MM-YYYY)")); dlg.add(startDateF);
    dlg.add(new JLabel("Holder Name")); dlg.add(holderF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            RecurringDeposit rd = new RecurringDeposit(
                accNumF.getText(), Double.parseDouble(amountF.getText()),
                Double.parseDouble(rateF.getText()), Integer.parseInt(tenureF.getText()),
                startDateF.getText(), holderF.getText()
            );
            manager.saveRecurringDeposit(rd);
            dlg.dispose();
            refreshRecurringDeposits();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}

private void refreshRecurringDeposits() {
    rdModel.setRowCount(0);
    try {
        for (RecurringDeposit rd : manager.getAllRecurringDeposits())
            rdModel.addRow(rd.toObjectArray());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading RDs: " + e.getMessage());
    }
}
private void openCreditCardDialog() {
    JDialog dlg = new JDialog(this, "Add Credit Card", true);
    dlg.setLayout(new GridLayout(6,2));
    JTextField nameF = new JTextField(), limitF = new JTextField(), expF = new JTextField(), payF = new JTextField(), daysF = new JTextField();
    dlg.add(new JLabel("Card Name")); dlg.add(nameF);
    dlg.add(new JLabel("Credit Limit")); dlg.add(limitF);
    dlg.add(new JLabel("Expenses")); dlg.add(expF);
    dlg.add(new JLabel("Amount To Pay")); dlg.add(payF);
    dlg.add(new JLabel("Days Left")); dlg.add(daysF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            CreditCard cc = new CreditCard(
                nameF.getText(),
                Double.parseDouble(limitF.getText()),
                Double.parseDouble(expF.getText()),
                Double.parseDouble(payF.getText()),
                Integer.parseInt(daysF.getText())
            );
            manager.saveCreditCard(cc);
            dlg.dispose();
            refreshCreditCards();
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}

private void refreshCreditCards() {
    ccModel.setRowCount(0);
    try {
        for (CreditCard cc : manager.getAllCreditCards())
            ccModel.addRow(cc.toObjectArray());
    } catch(Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading Credit Cards: " + e.getMessage());
    }
}
private void refreshGoldSilver() {
    gssModel.setRowCount(0);
    try {
        for (GoldSilverInvestment gs : manager.getAllGoldSilverInvestments()) {
            double value = gs.getWeight() * gs.getPricePerGram();
            gssModel.addRow(new Object[]{gs.getMetalType(), gs.getWeight(), gs.getPricePerGram(), value});
        }
    } catch(Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading: " + e.getMessage());
    }
}

private void openGoldSilverDialog() {
    JDialog dlg = new JDialog(this, "Add Gold/Silver Investment", true);
    dlg.setLayout(new GridLayout(4,2));
    JTextField typeF = new JTextField();
    JTextField weightF = new JTextField();
    JTextField priceF = new JTextField();
    dlg.add(new JLabel("Metal Type (Gold/Silver):")); dlg.add(typeF);
    dlg.add(new JLabel("Weight (g):")); dlg.add(weightF);
    dlg.add(new JLabel("Price/g:")); dlg.add(priceF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            GoldSilverInvestment gs = new GoldSilverInvestment(
                typeF.getText(), Double.parseDouble(weightF.getText()), Double.parseDouble(priceF.getText())
            );
            manager.saveGoldSilverInvestment(gs);
            dlg.dispose();
            refreshGoldSilver();
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}
private void refreshMutualFunds() {
    mfModel.setRowCount(0);
    try {
        for (MutualFund mf : manager.getAllMutualFunds()) {
            mfModel.addRow(new Object[]{
                mf.getAmount(), mf.getAnnualRate(), mf.getYears(),
                mf.getMaturityAmount() // Calculation shown in table
            });
        }
    } catch(Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading: " + e.getMessage());
    }
}

private void openMutualFundDialog() {
    JDialog dlg = new JDialog(this, "Add Mutual Fund Investment", true);
    dlg.setLayout(new GridLayout(4,2));
    JTextField amtF = new JTextField();
    JTextField rateF = new JTextField();
    JTextField yearsF = new JTextField();
    dlg.add(new JLabel("Amount:")); dlg.add(amtF);
    dlg.add(new JLabel("Annual Rate (%):")); dlg.add(rateF);
    dlg.add(new JLabel("Years:")); dlg.add(yearsF);
    JButton ok = new JButton("Save"), cancel = new JButton("Cancel");
    dlg.add(ok); dlg.add(cancel);
    ok.addActionListener(ev -> {
        try {
            MutualFund mf = new MutualFund(
                Double.parseDouble(amtF.getText()), Double.parseDouble(rateF.getText()), Integer.parseInt(yearsF.getText())
            );
            manager.saveMutualFund(mf);
            dlg.dispose();
            refreshMutualFunds();
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage());
        }
    });
    cancel.addActionListener(ev -> dlg.dispose());
    dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinanceManagerFullUI().setVisible(true));
    }

}
