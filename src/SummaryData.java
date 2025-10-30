package src;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable-style aggregate snapshot describing the current health of the
 * finance workspace. The FinanceManager populates this before sending it to
 * the UI, which can then render or export it.
 */
public class SummaryData {

    private String companyName;
    private String designation;
    private String holderName;
    private LocalDateTime generatedAt;
    private final TransactionSummary transactions = new TransactionSummary();
    private final BankSummary bank = new BankSummary();
    private final DepositSummary deposits = new DepositSummary();
    private final InvestmentSummary investments = new InvestmentSummary();
    private final LoanSummary loans = new LoanSummary();
    private final CardSummary cards = new CardSummary();
    private final TaxSummary tax = new TaxSummary();

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public TransactionSummary getTransactions() {
        return transactions;
    }

    public BankSummary getBank() {
        return bank;
    }

    public DepositSummary getDeposits() {
        return deposits;
    }

    public InvestmentSummary getInvestments() {
        return investments;
    }

    public LoanSummary getLoans() {
        return loans;
    }

    public CardSummary getCards() {
        return cards;
    }

    public TaxSummary getTax() {
        return tax;
    }

    // ---------------------------------------------------------------------
    // Transaction summary
    public static class TransactionSummary {
        private int totalCount;
        private double totalIncome;
        private double totalExpense;
        private double netBalance;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public double getTotalIncome() {
            return totalIncome;
        }

        public void setTotalIncome(double totalIncome) {
            this.totalIncome = totalIncome;
        }

        public double getTotalExpense() {
            return totalExpense;
        }

        public void setTotalExpense(double totalExpense) {
            this.totalExpense = totalExpense;
        }

        public double getNetBalance() {
            return netBalance;
        }

        public void setNetBalance(double netBalance) {
            this.netBalance = netBalance;
        }
    }

    // ---------------------------------------------------------------------
    // Bank summary
    public static class BankSummary {
        private int accountCount;
        private int uniqueHolderCount;
        private double totalBalance;
        private String topAccountLabel;
        private double topAccountBalance;

        public int getAccountCount() {
            return accountCount;
        }

        public void setAccountCount(int accountCount) {
            this.accountCount = accountCount;
        }

        public int getUniqueHolderCount() {
            return uniqueHolderCount;
        }

        public void setUniqueHolderCount(int uniqueHolderCount) {
            this.uniqueHolderCount = uniqueHolderCount;
        }

        public double getTotalBalance() {
            return totalBalance;
        }

        public void setTotalBalance(double totalBalance) {
            this.totalBalance = totalBalance;
        }

        public String getTopAccountLabel() {
            return topAccountLabel;
        }

        public void setTopAccountLabel(String topAccountLabel) {
            this.topAccountLabel = topAccountLabel;
        }

        public double getTopAccountBalance() {
            return topAccountBalance;
        }

        public void setTopAccountBalance(double topAccountBalance) {
            this.topAccountBalance = topAccountBalance;
        }
    }

    // ---------------------------------------------------------------------
    // Deposit summary
    public static class DepositSummary {
        private int totalCount;
        private double totalFdPrincipal;
        private double totalFdMaturityEstimate;
        private double totalRdContribution;
        private double totalRdMaturityEstimate;
        private double totalGullakBalance;
        private double totalGullakDue;
        private List<MaturityInfo> maturityHighlights = new ArrayList<>();

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public double getTotalFdPrincipal() {
            return totalFdPrincipal;
        }

        public void setTotalFdPrincipal(double totalFdPrincipal) {
            this.totalFdPrincipal = totalFdPrincipal;
        }

        public double getTotalFdMaturityEstimate() {
            return totalFdMaturityEstimate;
        }

        public void setTotalFdMaturityEstimate(double totalFdMaturityEstimate) {
            this.totalFdMaturityEstimate = totalFdMaturityEstimate;
        }

        public double getTotalRdContribution() {
            return totalRdContribution;
        }

        public void setTotalRdContribution(double totalRdContribution) {
            this.totalRdContribution = totalRdContribution;
        }

        public double getTotalRdMaturityEstimate() {
            return totalRdMaturityEstimate;
        }

        public void setTotalRdMaturityEstimate(double totalRdMaturityEstimate) {
            this.totalRdMaturityEstimate = totalRdMaturityEstimate;
        }

        public double getTotalGullakBalance() {
            return totalGullakBalance;
        }

        public void setTotalGullakBalance(double totalGullakBalance) {
            this.totalGullakBalance = totalGullakBalance;
        }

        public double getTotalGullakDue() {
            return totalGullakDue;
        }

        public void setTotalGullakDue(double totalGullakDue) {
            this.totalGullakDue = totalGullakDue;
        }

        public List<MaturityInfo> getMaturityHighlights() {
            return Collections.unmodifiableList(maturityHighlights);
        }

        public void setMaturityHighlights(List<MaturityInfo> maturityHighlights) {
            this.maturityHighlights = new ArrayList<>(maturityHighlights);
        }

        public static class MaturityInfo {
            private final String label;
            private final String maturityDateLabel;
            private final double principalValue;
            private final double maturityValue;
            private final LocalDate dueDate;

            public MaturityInfo(String label, LocalDate dueDate, String maturityDateLabel,
                                double principalValue, double maturityValue) {
                this.label = label;
                this.dueDate = dueDate;
                this.maturityDateLabel = maturityDateLabel;
                this.principalValue = principalValue;
                this.maturityValue = maturityValue;
            }

            public String getLabel() {
                return label;
            }

            public String getMaturityDateLabel() {
                return maturityDateLabel;
            }

            public double getPrincipalValue() {
                return principalValue;
            }

            public double getMaturityValue() {
                return maturityValue;
            }

            public LocalDate getDueDate() {
                return dueDate;
            }
        }
    }

    // ---------------------------------------------------------------------
    // Investment summary
    public static class InvestmentSummary {
        private int totalCount;
        private double totalInitialValue;
        private double totalCurrentValue;
        private double totalProfitOrLoss;
        private List<InvestmentHighlight> topPerformers = new ArrayList<>();

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public double getTotalInitialValue() {
            return totalInitialValue;
        }

        public void setTotalInitialValue(double totalInitialValue) {
            this.totalInitialValue = totalInitialValue;
        }

        public double getTotalCurrentValue() {
            return totalCurrentValue;
        }

        public void setTotalCurrentValue(double totalCurrentValue) {
            this.totalCurrentValue = totalCurrentValue;
        }

        public double getTotalProfitOrLoss() {
            return totalProfitOrLoss;
        }

        public void setTotalProfitOrLoss(double totalProfitOrLoss) {
            this.totalProfitOrLoss = totalProfitOrLoss;
        }

        public List<InvestmentHighlight> getTopPerformers() {
            return Collections.unmodifiableList(topPerformers);
        }

        public void setTopPerformers(List<InvestmentHighlight> topPerformers) {
            this.topPerformers = new ArrayList<>(topPerformers);
        }

        public static class InvestmentHighlight {
            private final String label;
            private final String assetType;
            private final double currentValue;
            private final double profitOrLoss;
            private final double profitOrLossPercentage;

            public InvestmentHighlight(String label, String assetType, double currentValue,
                                       double profitOrLoss, double profitOrLossPercentage) {
                this.label = label;
                this.assetType = assetType;
                this.currentValue = currentValue;
                this.profitOrLoss = profitOrLoss;
                this.profitOrLossPercentage = profitOrLossPercentage;
            }

            public String getLabel() {
                return label;
            }

            public String getAssetType() {
                return assetType;
            }

            public double getCurrentValue() {
                return currentValue;
            }

            public double getProfitOrLoss() {
                return profitOrLoss;
            }

            public double getProfitOrLossPercentage() {
                return profitOrLossPercentage;
            }
        }
    }

    // ---------------------------------------------------------------------
    // Loan summary
    public static class LoanSummary {
        private int totalCount;
        private int activeCount;
        private int paidOffCount;
        private double totalPrincipal;
        private double totalPrincipalOutstanding;
        private double totalPrincipalPaidOff;
        private double totalMonthlyEmi;
        private double totalRepayableOutstanding;
        private List<LoanHighlight> keyLoans = new ArrayList<>();

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public void setActiveCount(int activeCount) {
            this.activeCount = activeCount;
        }

        public int getPaidOffCount() {
            return paidOffCount;
        }

        public void setPaidOffCount(int paidOffCount) {
            this.paidOffCount = paidOffCount;
        }

        public double getTotalPrincipal() {
            return totalPrincipal;
        }

        public void setTotalPrincipal(double totalPrincipal) {
            this.totalPrincipal = totalPrincipal;
        }

        public double getTotalPrincipalOutstanding() {
            return totalPrincipalOutstanding;
        }

        public void setTotalPrincipalOutstanding(double totalPrincipalOutstanding) {
            this.totalPrincipalOutstanding = totalPrincipalOutstanding;
        }

        public double getTotalPrincipalPaidOff() {
            return totalPrincipalPaidOff;
        }

        public void setTotalPrincipalPaidOff(double totalPrincipalPaidOff) {
            this.totalPrincipalPaidOff = totalPrincipalPaidOff;
        }

        public double getTotalMonthlyEmi() {
            return totalMonthlyEmi;
        }

        public void setTotalMonthlyEmi(double totalMonthlyEmi) {
            this.totalMonthlyEmi = totalMonthlyEmi;
        }

        public double getTotalRepayableOutstanding() {
            return totalRepayableOutstanding;
        }

        public void setTotalRepayableOutstanding(double totalRepayableOutstanding) {
            this.totalRepayableOutstanding = totalRepayableOutstanding;
        }

        public List<LoanHighlight> getKeyLoans() {
            return Collections.unmodifiableList(keyLoans);
        }

        public void setKeyLoans(List<LoanHighlight> keyLoans) {
            this.keyLoans = new ArrayList<>(keyLoans);
        }

        public static class LoanHighlight {
            private final String label;
            private final String loanType;
            private final String status;
            private final double emiAmount;
            private final double principalAmount;
            private final double totalRepayable;

            public LoanHighlight(String label, String loanType, String status,
                                 double emiAmount, double principalAmount, double totalRepayable) {
                this.label = label;
                this.loanType = loanType;
                this.status = status;
                this.emiAmount = emiAmount;
                this.principalAmount = principalAmount;
                this.totalRepayable = totalRepayable;
            }

            public String getLabel() {
                return label;
            }

            public String getLoanType() {
                return loanType;
            }

            public String getStatus() {
                return status;
            }

            public double getEmiAmount() {
                return emiAmount;
            }

            public double getPrincipalAmount() {
                return principalAmount;
            }

            public double getTotalRepayable() {
                return totalRepayable;
            }
        }
    }

    // ---------------------------------------------------------------------
    // Card summary
    public static class CardSummary {
        private int totalCount;
        private int creditCardCount;
        private int debitCardCount;
        private double totalCreditLimit;
        private double totalCreditUsed;
        private double totalCreditAvailable;
        private double totalCreditDue;
        private List<CardHighlight> keyCards = new ArrayList<>();

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getCreditCardCount() {
            return creditCardCount;
        }

        public void setCreditCardCount(int creditCardCount) {
            this.creditCardCount = creditCardCount;
        }

        public int getDebitCardCount() {
            return debitCardCount;
        }

        public void setDebitCardCount(int debitCardCount) {
            this.debitCardCount = debitCardCount;
        }

        public double getTotalCreditLimit() {
            return totalCreditLimit;
        }

        public void setTotalCreditLimit(double totalCreditLimit) {
            this.totalCreditLimit = totalCreditLimit;
        }

        public double getTotalCreditUsed() {
            return totalCreditUsed;
        }

        public void setTotalCreditUsed(double totalCreditUsed) {
            this.totalCreditUsed = totalCreditUsed;
        }

        public double getTotalCreditAvailable() {
            return totalCreditAvailable;
        }

        public void setTotalCreditAvailable(double totalCreditAvailable) {
            this.totalCreditAvailable = totalCreditAvailable;
        }

        public double getTotalCreditDue() {
            return totalCreditDue;
        }

        public void setTotalCreditDue(double totalCreditDue) {
            this.totalCreditDue = totalCreditDue;
        }

        public List<CardHighlight> getKeyCards() {
            return Collections.unmodifiableList(keyCards);
        }

        public void setKeyCards(List<CardHighlight> keyCards) {
            this.keyCards = new ArrayList<>(keyCards);
        }

        public static class CardHighlight {
            private final String cardName;
            private final String cardType;
            private final double creditLimit;
            private final double availableCredit;
            private final double amountDue;

            public CardHighlight(String cardName, String cardType, double creditLimit,
                                 double availableCredit, double amountDue) {
                this.cardName = cardName;
                this.cardType = cardType;
                this.creditLimit = creditLimit;
                this.availableCredit = availableCredit;
                this.amountDue = amountDue;
            }

            public String getCardName() {
                return cardName;
            }

            public String getCardType() {
                return cardType;
            }

            public double getCreditLimit() {
                return creditLimit;
            }

            public double getAvailableCredit() {
                return availableCredit;
            }

            public double getAmountDue() {
                return amountDue;
            }
        }
    }

    // ---------------------------------------------------------------------
    // Tax summary
    public static class TaxSummary {
        private int profileCount;
        private double totalGrossIncome;
        private double totalDeductions;
        private double totalTaxableIncome;
        private double totalTaxPaid;
        private String latestFinancialYear;
        private double latestYearTaxable;
        private double latestYearTaxPaid;
        private List<TaxProfileHighlight> keyProfiles = new ArrayList<>();

        public int getProfileCount() {
            return profileCount;
        }

        public void setProfileCount(int profileCount) {
            this.profileCount = profileCount;
        }

        public double getTotalGrossIncome() {
            return totalGrossIncome;
        }

        public void setTotalGrossIncome(double totalGrossIncome) {
            this.totalGrossIncome = totalGrossIncome;
        }

        public double getTotalDeductions() {
            return totalDeductions;
        }

        public void setTotalDeductions(double totalDeductions) {
            this.totalDeductions = totalDeductions;
        }

        public double getTotalTaxableIncome() {
            return totalTaxableIncome;
        }

        public void setTotalTaxableIncome(double totalTaxableIncome) {
            this.totalTaxableIncome = totalTaxableIncome;
        }

        public double getTotalTaxPaid() {
            return totalTaxPaid;
        }

        public void setTotalTaxPaid(double totalTaxPaid) {
            this.totalTaxPaid = totalTaxPaid;
        }

        public String getLatestFinancialYear() {
            return latestFinancialYear;
        }

        public void setLatestFinancialYear(String latestFinancialYear) {
            this.latestFinancialYear = latestFinancialYear;
        }

        public double getLatestYearTaxable() {
            return latestYearTaxable;
        }

        public void setLatestYearTaxable(double latestYearTaxable) {
            this.latestYearTaxable = latestYearTaxable;
        }

        public double getLatestYearTaxPaid() {
            return latestYearTaxPaid;
        }

        public void setLatestYearTaxPaid(double latestYearTaxPaid) {
            this.latestYearTaxPaid = latestYearTaxPaid;
        }

        public List<TaxProfileHighlight> getKeyProfiles() {
            return Collections.unmodifiableList(keyProfiles);
        }

        public void setKeyProfiles(List<TaxProfileHighlight> keyProfiles) {
            this.keyProfiles = new ArrayList<>(keyProfiles);
        }

        public static class TaxProfileHighlight {
            private final String profileName;
            private final String financialYear;
            private final double taxableIncome;
            private final double taxPaid;

            public TaxProfileHighlight(String profileName, String financialYear,
                                       double taxableIncome, double taxPaid) {
                this.profileName = profileName;
                this.financialYear = financialYear;
                this.taxableIncome = taxableIncome;
                this.taxPaid = taxPaid;
            }

            public String getProfileName() {
                return profileName;
            }

            public String getFinancialYear() {
                return financialYear;
            }

            public double getTaxableIncome() {
                return taxableIncome;
            }

            public double getTaxPaid() {
                return taxPaid;
            }
        }
    }
}
