package simu.model;

//--- parameters that the user can change

public class SimulationConfig {
    // Arrival process
    public String arrivalDistType = "negexp";
    public double arrivalMean = 15;
    public double arrivalStd = 5;

    // Shopping area service times
    public String shoppingDistType = "normal";
    public double produceMean = 8, produceStd = 2;
    public double dairyMean = 6, dairyStd = 2;
    public double groceryMean = 10, groceryStd = 3;
    public double beveragesMean = 7, beveragesStd = 2;

    // Checkout service times

    public double regularCheckoutMean = 6, regularCheckoutStd = 2;
    public double selfCheckoutMean = 4, selfCheckoutStd = 1;

    // Routing probabilities (shopping area)
    public double probProduce = 0.25;
    public double probDairy = 0.25;
    public double probGrocery = 0.25;
    public double probBeverages = 0.25;   // sum = 1

    // Checkout decision
    public int selfMaxItems = 10;
    // Checkout decision: item threshold
    public int selfCheckoutMaxItems = 10;

    // (Optional) customer item count distribution
    public String itemCountDist = "uniform";
    public int minItems = 1, maxItems = 30;
    public double avgItems = 15, stdItems = 5;
}