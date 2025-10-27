package src;
public class MutualFund {
    private double amount;
    private double expectedRate;
    private int years;

    public MutualFund(double amount, double expectedRate, int years) {
        this.amount = amount;
        this.expectedRate = expectedRate;
        this.years = years;
    }

    public double calculateGrowth() {
        return amount * Math.pow(1 + expectedRate/100, years);
    }

    public String toString() {
        return "Mutual Fund: ₹" + amount + ", Expected Rate: " + expectedRate + "%, Years: " + years + ", Future Value: ₹" + calculateGrowth();
    }
    public String toCSV() {
        return amount + "," + expectedRate + "," + years;
    }

    public static MutualFund fromCSV(String line) {
        String[] p = line.split(",");
        return new MutualFund(
            Double.parseDouble(p[0]), // amount
            Double.parseDouble(p[1]), // expectedRate
            Integer.parseInt(p[2])    // years
        );
    }
    public double getAmount() { return amount; }
public double getAnnualRate() { return expectedRate; }
public int getYears() { return years; }
// Add this method to compute maturity value:
public double getMaturityAmount() {
    // Compound Interest: A = P * (1 + r/100) ^ n
    return amount * Math.pow(1 + expectedRate/100, years);
}

}
