package src.UI;

import javax.swing.*;
import java.awt.*;

public class ShowOtpDialog extends JDialog {

    public ShowOtpDialog(Frame owner, String otp) {
        super(owner, "Your OTP", true); // Modal
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JLabel infoLabel = new JLabel("<html><center>For security, please enter the OTP below.<br>(Simulating OTP delivery)</center></html>", SwingConstants.CENTER);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel otpLabel = new JLabel(otp);
        otpLabel.setFont(new Font("Arial", Font.BOLD, 24));
        otpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        otpLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose()); // Just close the dialog

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(okButton);

        add(infoLabel, BorderLayout.NORTH);
        add(otpLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(250, 0));
        setLocationRelativeTo(owner);
    }
}