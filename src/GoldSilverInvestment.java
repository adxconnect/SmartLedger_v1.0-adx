package src;

public class GoldSilverInvestment {
    private String type; // "Gold" or "Silver"
    private double weight; // grams
    private double pricePerGram;

    public GoldSilverInvestment(String type, double weight, double pricePerGram) {
        this.type = type;
        this.weight = weight;
        this.pricePerGram = pricePerGram;
    }

    public double currentValue() {
        return weight * pricePerGram;
    }

    public String toString() {
        return type + " Investment: " + weight + "g at ₹" + pricePerGram + "/g, Value: ₹" + currentValue();
    }
    public String toCSV() {
        return type + "," + weight + "," + pricePerGram;
    }

    public static GoldSilverInvestment fromCSV(String line) {
        String[] p = line.split(",");
        return new GoldSilverInvestment(
            p[0],                       // type
            Double.parseDouble(p[1]),   // weight
            Double.parseDouble(p[2])    // pricePerGram
        );
    }
    public String getMetalType() { return type; }
public double getWeight() { return weight; }
public double getPricePerGram() { return pricePerGram; }

}
