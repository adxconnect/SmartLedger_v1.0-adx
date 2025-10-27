package src;

public class FixedDeposit {
    private int id;
    private String accountNumber, startDate, holderName;
    private double principal, rate;
    private int tenureMonths;

    public FixedDeposit(String accountNumber, double principal, double rate, int tenureMonths, String startDate, String holderName) {
        this.accountNumber = accountNumber;
        this.principal = principal;
        this.rate = rate;
        this.tenureMonths = tenureMonths;
        this.startDate = startDate;
        this.holderName = holderName;
    }

    public FixedDeposit(int id, String accountNumber, double principal, double rate, int tenureMonths, String startDate, String holderName) {
        this(accountNumber, principal, rate, tenureMonths, startDate, holderName);
        this.id = id;
    }

    public Object[] toObjectArray() {
        return new Object[]{accountNumber, principal, rate, tenureMonths, startDate, holderName};
    }

    public int getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public double getPrincipal() { return principal; }
    public double getRate() { return rate; }
    public int getTenureMonths() { return tenureMonths; }
    public String getStartDate() { return startDate; }
    public String getHolderName() { return holderName; }
}
