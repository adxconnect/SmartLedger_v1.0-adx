package src.auth;

public final class SessionContext {

    private static Account currentAccount;

    private SessionContext() {
        // Prevent instantiation
    }

    public static void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static Integer getCurrentAccountId() {
        return (currentAccount != null) ? currentAccount.getId() : null;
    }

    public static void clear() {
        currentAccount = null;
    }

    public static boolean isLoggedIn() {
        return currentAccount != null;
    }
}