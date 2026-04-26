package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

public class Customer {
	private double arrivalTime;
	private double removalTime;

	//stage timestamps
	private double shoppingStartTime;   //customer starts shopping
	private double shoppingEndTime;     //customer finishes shopping
	private double checkoutStartTime;   //checkout begins
	private double checkoutEndTime;     //checkout service ends
	private double paymentTime;         //payment is completed

	private int itemCount;

	private final int id;
	private static int i = 1;
	private static long sum = 0;

	public Customer() {
	    id = i++;

		arrivalTime = Clock.getInstance().getTime();

        int itemCount = 10 + (int) (Math.random() * 31);
		Trace.out(Trace.Level.INFO,
				"New customer #" + id + " arrived at  " + arrivalTime +
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

	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nCustomer " + id + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer "   + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " stayed: "  + (removalTime - arrivalTime));

		sum += (removalTime - arrivalTime);
		double mean = sum/id;
		System.out.println("Current mean of the customer service times " + mean);
	}

}
