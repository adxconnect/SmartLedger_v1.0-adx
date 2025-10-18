package src;
public class FixedDeposit {
    private double principal;
    private double rate;
    private int years;

    public FixedDeposit(double principal, double rate, int years) {
        this.principal = principal;
        this.rate = rate;
        this.years = years;
    }

    public double calculateMaturity() {
        // Simple interest for FD (can be modified for compounding)
        return principal + (principal * rate/100 * years);
    }

    public String toString() {
        return "Fixed Deposit: ₹" + principal + ", Rate: " + rate + "%, Years: " + years + ", Maturity: ₹" + calculateMaturity();
    }
    public String toCSV() {
        return principal + "," + rate + "," + years;
    }
    public static FixedDeposit fromCSV(String line) {
        String[] p = line.split(",");
        return new FixedDeposit(Double.parseDouble(p[0]), Double.parseDouble(p[1]), Integer.parseInt(p[2]));
    }
    
}
