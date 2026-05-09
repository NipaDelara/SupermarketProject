package controller;

/* interface for the engine */
public interface IControllerMtoV {
		public void showEndTime(double time);
		public void visualiseCustomer();

	void visualiseCustomerStages(
			int entranceCustomers,
			int shoppingCustomers,
			int decisionCustomers,
			int regularCheckoutCustomers,
			int selfCheckoutCustomers,
			int exitCustomers
	);

}
