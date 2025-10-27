package src;
public class SavingsAccount {
    private int id;
    private String accountNumber, accountType, holderName, bankName, ifscCode;
    private double balance;
    public SavingsAccount(String accountNumber, String accountType, double balance, String holderName, String bankName, String ifscCode) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.holderName = holderName;
        this.bankName = bankName;
        this.ifscCode = ifscCode;
    }
    public SavingsAccount(int id, String accountNumber, String accountType, double balance, String holderName, String bankName, String ifscCode) {
        this(accountNumber, accountType, balance, holderName, bankName, ifscCode);
        this.id = id;
    }
    // Getters
    public int getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    public String getHolderName() { return holderName; }
    public String getBankName() { return bankName; }
    public String getIfscCode() { return ifscCode; }
    // For tables
    public Object[] toObjectArray() {
        return new Object[]{accountNumber, accountType, balance, holderName, bankName, ifscCode};
    }
}
