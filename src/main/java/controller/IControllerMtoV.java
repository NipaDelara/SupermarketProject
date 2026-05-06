package controller;

/* interface for the engine */
public interface IControllerMtoV {

	void showEndTime(double time);

	void visualiseCustomer();

	void visualiseCustomerStages(
			int entrance,
			int shopping,
			int decision,
			int regularCheckout,
			int selfCheckout,
			int exit
	);
}