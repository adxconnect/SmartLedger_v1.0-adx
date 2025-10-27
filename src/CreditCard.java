package src;
public class CreditCard {
    
    private String cardName;
    private double limit, expenses, amountToPay;
    private int daysLeftToPay;
    public Object[] toObjectArray() {
        return new Object[]{cardName, limit, expenses, amountToPay, daysLeftToPay};
    }
    public int getDaysLeftToPay() { return daysLeftToPay; }

    public CreditCard(String cardName, double limit, double expenses, double amountToPay, int daysLeftToPay) {
        this.cardName = cardName;
        this.limit = limit;
        this.expenses = expenses;
        this.amountToPay = amountToPay;
        this.daysLeftToPay = daysLeftToPay;
    }

    public void makePayment(double amount) {
        if (amount > amountToPay) {
            System.out.println("Payment exceeds due amount, adjusting to full payment.");
            amount = amountToPay;
        }
        amountToPay -= amount;
        System.out.println("Payment successful! Remaining due: ₹" + amountToPay);
    }

    public double availableCredit() {
        return limit - expenses;
    }

    public void addExpense(double amount) {
        if (expenses + amount > limit) {
            System.out.println("Transaction declined! Credit limit exceeded.");
        } else {
            expenses += amount;
            amountToPay += amount;
            System.out.println("Expense added successfully. Total due: ₹" + amountToPay);
        }
    }

    public String toCSV() {
        return cardName + "," + limit + "," + expenses + "," + amountToPay + "," + daysLeftToPay;
    }

    public static CreditCard fromCSV(String line) {
        String[] parts = line.split("\t", -1);
        if(parts.length < 5) {
        System.out.println("Skipping corrupted credit card line: " + line);
        // Return a dummy credit card or throw your own exception
        return new CreditCard("", 0, 0, 0, 0);
    }
        return new CreditCard(
            parts[0],
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3]),
            Integer.parseInt(parts[4])
        );
    }

    @Override
    public String toString() {
        return "Card: " + cardName +
               " | Limit: ₹" + limit +
               " | Used: ₹" + expenses +
               " | Due: ₹" + amountToPay +
               " | Days Left to Pay: " + daysLeftToPay +
               " | Available Credit: ₹" + availableCredit();
    }
    public String getCardName() {
    return cardName;
}
public double getLimit() {
    return limit;
}
public double getExpenses() {
    return expenses;
}
public double getAmountToPay() {
    return amountToPay;
}

    
}
