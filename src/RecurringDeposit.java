package src;

public class RecurringDeposit {
    private int id;
    private String accountNumber, startDate, holderName;
    private double monthlyAmount, rate;
    private int tenureMonths;

    public RecurringDeposit(String accountNumber, double monthlyAmount, double rate, int tenureMonths, String startDate, String holderName) {
        this.accountNumber = accountNumber;
        this.monthlyAmount = monthlyAmount;
        this.rate = rate;
        this.tenureMonths = tenureMonths;
        this.startDate = startDate;
        this.holderName = holderName;
    }

    public RecurringDeposit(int id, String accountNumber, double monthlyAmount, double rate, int tenureMonths, String startDate, String holderName) {
        this(accountNumber, monthlyAmount, rate, tenureMonths, startDate, holderName);
        this.id = id;
    }

    public Object[] toObjectArray() {
        return new Object[]{accountNumber, monthlyAmount, rate, tenureMonths, startDate, holderName};
    }

    public String getAccountNumber() { return accountNumber; }
    public double getMonthlyAmount() { return monthlyAmount; }
    public double getRate() { return rate; }
    public int getTenureMonths() { return tenureMonths; }
    public String getStartDate() { return startDate; }
    public String getHolderName() { return holderName; }
}
