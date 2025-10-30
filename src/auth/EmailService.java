package src.auth;

/**
 * Simple email service for development/testing.
 * In a real application, you would integrate with an actual email service like:
 * - JavaMail API with Gmail SMTP
 * - SendGrid
 * - AWS SES
 * 
 * For now, this prints OTP to console (free solution for development)
 */
public class EmailService {
    
    /**
     * Simulates sending an email by printing to console.
     * In production, replace this with actual email sending logic.
     * 
     * @param toEmail recipient email address
     * @param subject email subject
     * @param body email body
     * @return true if "sent" successfully
     */
    public static boolean sendEmail(String toEmail, String subject, String body) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 EMAIL SIMULATION (Console Output)");
        System.out.println("=".repeat(60));
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("-".repeat(60));
        System.out.println(body);
        System.out.println("=".repeat(60) + "\n");
        return true;
    }
    
    /**
     * Sends a password reset OTP email.
     * 
     * @param toEmail recipient email
     * @param accountName account holder name
     * @param otp the one-time password
     * @return true if sent successfully
     */
    public static boolean sendPasswordResetOtp(String toEmail, String accountName, String otp) {
        String subject = "Finance Manager - Password Reset OTP";
        String body = String.format(
            "Hi %s,\n\n" +
            "You requested to reset your password for Finance Manager.\n\n" +
            "Your One-Time Password (OTP) is: %s\n\n" +
            "This OTP is valid for 5 minutes.\n\n" +
            "If you did not request this password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Finance Manager Team",
            accountName,
            otp
        );
        return sendEmail(toEmail, subject, body);
    }
}
