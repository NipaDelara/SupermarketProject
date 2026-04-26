package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

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

	public Customer() {
		id = i++;

		arrivalTime = Clock.getInstance().getTime();

		// random item count: 10–40
		this.itemCount = 10 + (int) (Math.random() * 31);

		Trace.out(Trace.Level.INFO,
				"New customer #" + id + " arrived at " + arrivalTime +
						" with trolley and " + itemCount + " items.");
	}

	public int getId() {
		return id;
	}

	public int getItemCount() {
		return itemCount;
	}

	public double getRemovalTime() {
		return removalTime;
	}

	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public double getShoppingStartTime() {
		return shoppingStartTime;
	}

	public void setShoppingStartTime(double shoppingStartTime) {
		this.shoppingStartTime = shoppingStartTime;
	}

	public double getShoppingEndTime() {
		return shoppingEndTime;
	}

	public void setShoppingEndTime(double shoppingEndTime) {
		this.shoppingEndTime = shoppingEndTime;
	}

	public double getCheckoutStartTime() {
		return checkoutStartTime;
	}

	public void setCheckoutStartTime(double checkoutStartTime) {
		this.checkoutStartTime = checkoutStartTime;
	}

	public double getCheckoutEndTime() {
		return checkoutEndTime;
	}

	public void setCheckoutEndTime(double checkoutEndTime) {
		this.checkoutEndTime = checkoutEndTime;
	}

	public double getPaymentTime() {
		return paymentTime;
	}

	public void setPaymentTime(double paymentTime) {
		this.paymentTime = paymentTime;
	}

	public void reportPaymentSuccessful() {
		paymentTime = Clock.getInstance().getTime();

		Trace.out(Trace.Level.INFO,
				"Customer #" + id + " payment successful at " + paymentTime + ".");
	}

	public void reportResults() {
		removalTime = Clock.getInstance().getTime();

		Trace.out(Trace.Level.INFO, "\nCustomer #" + id + " ready to exit.");
		Trace.out(Trace.Level.INFO, "Customer #" + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " shopping started: " + shoppingStartTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " shopping ended: " + shoppingEndTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " checkout started: " + checkoutStartTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " checkout ended: " + checkoutEndTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " payment successful: " + paymentTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " exited: " + removalTime);
		Trace.out(Trace.Level.INFO, "Customer #" + id + " total time in supermarket: " + (removalTime - arrivalTime));

		sum += (removalTime - arrivalTime);
		double mean = sum / id;

		System.out.println("Current mean customer time in supermarket: " + mean);
	}
}