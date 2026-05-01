package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;
import eduni.distributions.Uniform;
import eduni.distributions.Normal;

public class Customer {
	private double arrivalTime;
	private double removalTime;

	// Stage timestamps
	private double shoppingStartTime;
	private double shoppingEndTime;
	private double checkoutStartTime;
	private double checkoutEndTime;
	private double paymentTime;

	private int itemCount;

	private final int id;
	private static int i = 1;
	private static double sum = 0;

	public Customer(SimulationConfig config) {
		id = i++;

		arrivalTime = Clock.getInstance().getTime();


		arrivalTime = Clock.getInstance().getTime();
		// Generate number of items based on config
		if ("uniform".equals(config.itemCountDist)) {
			itemCount = (int) new Uniform(config.minItems, config.maxItems).sample();
		} else if ("normal".equals(config.itemCountDist)) {
			int count = (int) new Normal(config.avgItems, config.stdItems).sample();
			itemCount = Math.max(1, count);
		} else {
			// default
			itemCount = 10;
		}
		Trace.out(Trace.Level.INFO,
				"New customer #" + id + " arrived at " + arrivalTime +
						" with trolley and " + itemCount + " items.");
	}

	public int getItemCount() { return itemCount; }

	public double getRemovalTime() { return removalTime; }
	public void setRemovalTime(double removalTime) { this.removalTime = removalTime; }

	public double getArrivalTime() { return arrivalTime; }
	public void setArrivalTime(double arrivalTime) { this.arrivalTime = arrivalTime; }

	public double getShoppingStartTime() { return shoppingStartTime; }
	public void setShoppingStartTime(double time) { this.shoppingStartTime = time; }

	public double getShoppingEndTime() { return shoppingEndTime; }
	public void setShoppingEndTime(double time) { this.shoppingEndTime = time; }

	public double getCheckoutStartTime() { return checkoutStartTime; }
	public void setCheckoutStartTime(double time) { this.checkoutStartTime = time; }

	public double getCheckoutEndTime() { return checkoutEndTime; }
	public void setCheckoutEndTime(double time) { this.checkoutEndTime = time; }

	public void reportPaymentSuccessful() {
		Trace.out(Trace.Level.INFO, "Customer " + id + " paid at " + checkoutEndTime);
	}

	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nCustomer " + id + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer " + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO, "Customer " + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO, "Customer " + id + " stayed: " + (removalTime - arrivalTime));
		sum += (removalTime - arrivalTime);
		double mean = sum / id;
		System.out.println("Current mean service time: " + mean);
	}
}