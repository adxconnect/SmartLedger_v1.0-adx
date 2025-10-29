package src.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File; // Import File

import src.Card; // Import Card class

public class SensitiveCardDetailsDialog extends JDialog {

    private Card card;

    public SensitiveCardDetailsDialog(Frame owner, Card card) {
        super(owner, "Sensitive Card Details - " + card.getCardName(), true); // Modal
        this.card = card;
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Card Number
        gbc.gridx = 0; gbc.gridy = row;
        detailsPanel.add(new JLabel("Card Number:"), gbc);
        gbc.gridx = 1;
        JTextField numberField = new JTextField(card.getCardNumber());
        numberField.setEditable(false); // Read-only
        numberField.setFont(new Font("Monospaced", Font.PLAIN, 14)); // Use monospaced for number
        detailsPanel.add(numberField, gbc);
        row++;

        // Valid From / Thru
        gbc.gridx = 0; gbc.gridy = row;
        detailsPanel.add(new JLabel("Valid From:"), gbc);
        gbc.gridx = 1;
        JTextField fromField = new JTextField(card.getValidFrom() != null ? card.getValidFrom() : "N/A");
        fromField.setEditable(false);
        detailsPanel.add(fromField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        detailsPanel.add(new JLabel("Valid Thru:"), gbc);
        gbc.gridx = 1;
        JTextField thruField = new JTextField(card.getValidThrough());
        thruField.setEditable(false);
        detailsPanel.add(thruField, gbc);
        row++;

        // CVV
        gbc.gridx = 0; gbc.gridy = row;
        detailsPanel.add(new JLabel("CVV/CVC:"), gbc);
        gbc.gridx = 1;
        JTextField cvvField = new JTextField(card.getCvv());
        cvvField.setEditable(false);
        cvvField.setFont(new Font("Monospaced", Font.BOLD, 14));
        detailsPanel.add(cvvField, gbc);
        row++;

        // Images (Show paths for now, display later if needed)
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth=2;
        detailsPanel.add(new JSeparator(), gbc);
        gbc.gridwidth=1; row++;

        gbc.gridx = 0; gbc.gridy = row;
        detailsPanel.add(new JLabel("Front Image:"), gbc);
        gbc.gridx = 1;
        JLabel frontPathLabel = new JLabel(card.getFrontImagePath() != null ? card.getFrontImagePath() : "Not Set");
        detailsPanel.add(frontPathLabel, gbc);
        row++;
        // Basic image display (adjust size as needed)
        if (card.getFrontImagePath() != null && new File(card.getFrontImagePath()).exists()) {
            gbc.gridx=1; gbc.gridy=row;
            ImageIcon frontIcon = new ImageIcon(new ImageIcon(card.getFrontImagePath()).getImage().getScaledInstance(150, -1, Image.SCALE_SMOOTH));
            detailsPanel.add(new JLabel(frontIcon), gbc);
            row++;
        }


        gbc.gridx = 0; gbc.gridy = row;
        detailsPanel.add(new JLabel("Back Image:"), gbc);
        gbc.gridx = 1;
        JLabel backPathLabel = new JLabel(card.getBackImagePath() != null ? card.getBackImagePath() : "Not Set");
        detailsPanel.add(backPathLabel, gbc);
        row++;
        // Basic image display
         if (card.getBackImagePath() != null && new File(card.getBackImagePath()).exists()) {
            gbc.gridx=1; gbc.gridy=row;
            ImageIcon backIcon = new ImageIcon(new ImageIcon(card.getBackImagePath()).getImage().getScaledInstance(150, -1, Image.SCALE_SMOOTH));
            detailsPanel.add(new JLabel(backIcon), gbc);
            row++;
        }


        // Close Button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);

        add(new JScrollPane(detailsPanel), BorderLayout.CENTER); // Put details in scroll pane
        add(buttonPanel, BorderLayout.SOUTH);

        //setSize(450, 400); // Adjust size as needed
        pack(); // Auto-size
        setMinimumSize(new Dimension(400, 0));
        setLocationRelativeTo(owner);
    }
}