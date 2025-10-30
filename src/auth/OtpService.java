package src.auth;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_TTL_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Map<String, OtpEntry> otpStore = new HashMap<>();

    public static String generateOtp(String email) {
    int ceiling = (int) Math.pow(10, OTP_LENGTH);
    String otp = String.format("%0" + OTP_LENGTH + "d", RANDOM.nextInt(ceiling));
        otpStore.put(email.toLowerCase(), new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES)));
        return otp;
    }

    public static boolean verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email.toLowerCase());
        if (entry == null || entry.expiresAt.isBefore(LocalDateTime.now())) {
            otpStore.remove(email.toLowerCase());
            return false;
        }
        boolean match = entry.code.equals(otp);
        if (match) {
            otpStore.remove(email.toLowerCase());
        }
        return match;
    }

    private static class OtpEntry {
        private final String code;
        private final LocalDateTime expiresAt;

        private OtpEntry(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }
}