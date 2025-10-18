package src;
public class SavingsAccount {
    private double balance;
    private double rate; // Annual interest rate (%)
    private int years;

    public SavingsAccount(double balance, double rate, int years) {
        this.balance = balance;
        this.rate = rate;
        this.years = years;
    }

    public double calculateInterest() {
        // Simple interest formula
        return balance * rate/100 * years;
    }

    public String toString() {
        return "Savings Account: ₹" + balance + ", Rate: " + rate + "%, Years: " + years + ", Interest: ₹" + calculateInterest();
    }
}
