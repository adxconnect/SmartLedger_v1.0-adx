package src.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EnterOtpDialog extends JDialog {

    private String enteredOtp = null; // To store the entered OTP
    private JTextField otpField;

    public EnterOtpDialog(Frame owner) {
        super(owner, "Enter OTP", true); // Modal
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Close without saving if X clicked

        JLabel promptLabel = new JLabel("Please enter the 6-digit OTP:");
        promptLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        otpField = new JTextField(6); // Field width approx 6 digits
        otpField.setFont(new Font("Arial", Font.BOLD, 18));
        otpField.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fieldPanel.add(otpField);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));


        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");

        submitButton.addActionListener(e -> {
            enteredOtp = otpField.getText().trim();
            if (enteredOtp.length() == 6 && enteredOtp.matches("\\d{6}")) {
                dispose(); // Close dialog successfully
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a valid 6-digit OTP.", "Invalid OTP", JOptionPane.ERROR_MESSAGE);
                enteredOtp = null; // Reset if invalid
            }
        });

        cancelButton.addActionListener(e -> {
            enteredOtp = null; // Ensure OTP is null if cancelled
            dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        add(promptLabel, BorderLayout.NORTH);
        add(fieldPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(250, 0));
        setLocationRelativeTo(owner);
    }

    /**
     * Call this method after the dialog is closed to get the entered OTP.
     * Returns null if cancelled or invalid OTP was entered.
     */
    public String getEnteredOtp() {
        return enteredOtp;
    }
}
