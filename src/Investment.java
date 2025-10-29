package src;

public class Investment {

    private int id;
    // Common Details
    private String assetType;      // e.g., 'Indian Stocks', 'Mutual Fund', 'Gold', 'Real Estate'
    private String holderName;
    private String description;
    private String goal;
    private String startDate;      // YYYY-MM-DD
    private String accountDetails; // PAN/Email/Phone text block

    // Unit-based assets (stocks, MF, crypto, gold, silver)
    private String tickerSymbol;   // e.g., INFY
    private String exchange;       // e.g., NSE
    private double quantity;
    private double initialUnitCost;
    private double currentUnitPrice;

    // Other assets (real estate, bonds)
    private String propertyAddress;
    private int tenureYears;
    private double interestRate;

    // Constructor (DB load)
    public Investment(
            int id,
            String assetType,
            String holderName,
            String description,
            String goal,
            String startDate,
            String accountDetails,
            String tickerSymbol,
            String exchange,
            double quantity,
            double initialUnitCost,
            double currentUnitPrice,
            String propertyAddress,
            int tenureYears,
            double interestRate
    ) {
        this.id = id;
        this.assetType = assetType;
        this.holderName = holderName;
        this.description = description;
        this.goal = goal;
        this.startDate = startDate;
        this.accountDetails = accountDetails;
        this.tickerSymbol = tickerSymbol;
        this.exchange = exchange;
        this.quantity = quantity;
        this.initialUnitCost = initialUnitCost;
        this.currentUnitPrice = currentUnitPrice;
        this.propertyAddress = propertyAddress;
        this.tenureYears = tenureYears;
        this.interestRate = interestRate;
    }

    // Calculations
    public double getTotalInitialCost() {
        if ("Real Estate".equals(assetType) || "Structured Bond".equals(assetType) || "Others".equals(assetType)) {
            return initialUnitCost; // here initialUnitCost is total cost
        }
        return quantity * initialUnitCost;
    }

    public double getTotalCurrentValue() {
        if ("Real Estate".equals(assetType) || "Structured Bond".equals(assetType) || "Others".equals(assetType)) {
            return currentUnitPrice; // here currentUnitPrice is total value
        }
        return quantity * currentUnitPrice;
    }

    public double getProfitOrLoss() {
        return getTotalCurrentValue() - getTotalInitialCost();
    }

    public double getProfitOrLossPercentage() {
        double base = getTotalInitialCost();
        return base == 0 ? 0.0 : (getProfitOrLoss() / base) * 100.0;
    }

    @Override
    public String toString() {
        String name = (description != null && !description.isEmpty()) ? description : assetType;
        String holder = (holderName != null && !holderName.isEmpty()) ? holderName : "Default";
        return holder + " - " + name;
    }

    // Getters
    public int getId() { return id; }
    public String getAssetType() { return assetType; }
    public String getHolderName() { return holderName; }
    public String getDescription() { return description; }
    public String getGoal() { return goal; }
    public String getStartDate() { return startDate; }
    public String getAccountDetails() { return accountDetails; }
    public String getTickerSymbol() { return tickerSymbol; }
    public String getExchange() { return exchange; }
    public double getQuantity() { return quantity; }
    public double getInitialUnitCost() { return initialUnitCost; }
    public double getCurrentUnitPrice() { return currentUnitPrice; }
    public String getPropertyAddress() { return propertyAddress; }
    public int getTenureYears() { return tenureYears; }
    public double getInterestRate() { return interestRate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setAssetType(String assetType) { this.assetType = assetType; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public void setDescription(String description) { this.description = description; }
    public void setGoal(String goal) { this.goal = goal; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setAccountDetails(String accountDetails) { this.accountDetails = accountDetails; }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setInitialUnitCost(double initialUnitCost) { this.initialUnitCost = initialUnitCost; }
    public void setCurrentUnitPrice(double currentUnitPrice) { this.currentUnitPrice = currentUnitPrice; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }
    public void setTenureYears(int tenureYears) { this.tenureYears = tenureYears; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
}