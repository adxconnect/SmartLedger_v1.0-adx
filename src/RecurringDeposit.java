package src;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class RecurringDeposit {
    private double monthlyDeposit;
    private double rate;
    private int months;

    public RecurringDeposit(double monthlyDeposit, double rate, int months) {
        this.monthlyDeposit = monthlyDeposit;
        this.rate = rate;
        this.months = months;
    }

    public double calculateMaturity() {
        double n = months;
        double r = rate / 400; // quarterly
        return monthlyDeposit * n + (monthlyDeposit * n * (n+1) / 2 * r);
    }

    public String toString() {
        return "Recurring Deposit: ₹" + monthlyDeposit + " per month, Rate: " + rate + "%, Months: " + months + ", Maturity: ₹" + calculateMaturity();
    }
    public String toCSV() {
        return monthlyDeposit + "," + rate + "," + months;
    }
    public static RecurringDeposit fromCSV(String line) {
        String[] p = line.split(",");
        return new RecurringDeposit(Double.parseDouble(p[0]), Double.parseDouble(p[1]), Integer.parseInt(p[2]));
    }
    private List<RecurringDeposit> recurringDeposits = new ArrayList<>();

public void saveRecurringDeposits(String filename) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
        for (RecurringDeposit rd : recurringDeposits) pw.println(rd.toCSV());
    } catch (IOException e) { e.printStackTrace(); }
}

public void loadRecurringDeposits(String filename) {
    recurringDeposits.clear();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null)
            recurringDeposits.add(RecurringDeposit.fromCSV(line));
    } catch (IOException e) { System.out.println("No Recurring Deposit file found."); }
}

}
