package simu.model;

import controller.IControllerMtoV;
import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Negexp;
import eduni.distributions.Normal;
import eduni.distributions.Uniform;
import simu.framework.ArrivalProcess;
import simu.framework.Clock;
import simu.framework.Engine;
import simu.framework.Event;

public class MyEngine extends Engine {

	private final ArrivalProcess arrivalProcess;
	private final SimulationConfig config;
	private final ServicePoint produceSP;
	private final ServicePoint dairySP;
	private final ServicePoint grocerySP;
	private final ServicePoint beveragesSP;
	private final ServicePoint regularCheckoutSP;
	private final ServicePoint selfCheckoutSP;

    private int regularCheckoutCustomers = 0;
	private int selfCheckoutCustomers = 0;
	private int exitCustomers = 0;

	public MyEngine(IControllerMtoV controller, SimulationConfig config	) {
		super(controller);
		this.config = config;

		// Create distributions using config values
		ContinuousGenerator produceGen = createGenerator(config.shoppingDistType,
				config.produceMean, config.produceStd);
		ContinuousGenerator dairyGen = createGenerator(config.shoppingDistType,
				config.dairyMean, config.dairyStd);
		ContinuousGenerator groceryGen = createGenerator(config.shoppingDistType,
				config.groceryMean, config.groceryStd);
		ContinuousGenerator beveragesGen = createGenerator(config.shoppingDistType,
				config.beveragesMean, config.beveragesStd);
		ContinuousGenerator regularGen = createGenerator(config.shoppingDistType,
				config.regularCheckoutMean, config.regularCheckoutStd);
		ContinuousGenerator selfGen = createGenerator(config.shoppingDistType,
				config.selfCheckoutMean, config.selfCheckoutStd);


		produceSP = new ServicePoint(produceGen, eventList, EventType.DEP_PRODUCE);
		dairySP = new ServicePoint(dairyGen, eventList, EventType.DEP_DAIRY);
		grocerySP = new ServicePoint(groceryGen, eventList, EventType.DEP_GROCERY);
		beveragesSP = new ServicePoint(beveragesGen, eventList, EventType.DEP_BEVERAGES);
		regularCheckoutSP = new ServicePoint(regularGen, eventList, EventType.DEP_REGULAR_CHECKOUT);
		selfCheckoutSP = new ServicePoint(selfGen, eventList, EventType.DEP_SELF_CHECKOUT);

		servicePoints = new ServicePoint[]{produceSP, dairySP, grocerySP, beveragesSP,
				regularCheckoutSP, selfCheckoutSP};

		ContinuousGenerator arrivalGen = createGenerator(config.arrivalDistType,
				config.arrivalMean, config.arrivalStd);
		arrivalProcess = new ArrivalProcess(arrivalGen, eventList, EventType.ARR1);
	}

	private ContinuousGenerator createGenerator(String type, double... params) {
		return switch (type.toLowerCase()) {
			case "negexp" -> new Negexp(params[0]);
			case "normal" -> new Normal(params[0], params[1]);
			case "uniform" -> new Uniform(params[0], params[1]);
			default -> new Negexp(10); // fallback
		};
	}

	@Override
	protected void initialization() {
		arrivalProcess.generateNext();
	}

	@Override
	protected void runEvent(Event t) {
		Customer a;

		switch ((EventType) t.getType()) {
			case ARR1:
				// Create new customer with item count based on config
				a = new Customer(config);
				a.setShoppingStartTime(Clock.getInstance().getTime());

				// Choose shopping area using config probabilities
				double rand = Math.random();
				if (rand < config.probProduce) {
					produceSP.addQueue(a);
					if (!produceSP.isReserved()) produceSP.beginService();
				} else if (rand < config.probProduce + config.probDairy) {
					dairySP.addQueue(a);
					if (!dairySP.isReserved()) dairySP.beginService();
				} else if (rand < config.probProduce + config.probDairy + config.probGrocery) {
					grocerySP.addQueue(a);
					if (!grocerySP.isReserved()) grocerySP.beginService();
				} else {
					beveragesSP.addQueue(a);
					if (!beveragesSP.isReserved()) beveragesSP.beginService();
				}

				arrivalProcess.generateNext();
				controller.visualiseCustomer();
				break;

			case DEP_PRODUCE:
				a = produceSP.removeQueue();
				a.setShoppingEndTime(Clock.getInstance().getTime());
				routeToCheckout(a);
				if (produceSP.isOnQueue()) produceSP.beginService();
				break;

			case DEP_DAIRY:
				a = dairySP.removeQueue();
				a.setShoppingEndTime(Clock.getInstance().getTime());
				routeToCheckout(a);
				if (dairySP.isOnQueue()) dairySP.beginService();
				break;

			case DEP_GROCERY:
				a = grocerySP.removeQueue();
				a.setShoppingEndTime(Clock.getInstance().getTime());
				routeToCheckout(a);
				if (grocerySP.isOnQueue()) grocerySP.beginService();
				break;

			case DEP_BEVERAGES:
				a = beveragesSP.removeQueue();
				a.setShoppingEndTime(Clock.getInstance().getTime());
				routeToCheckout(a);
				if (beveragesSP.isOnQueue()) beveragesSP.beginService();
				break;

			case DEP_REGULAR_CHECKOUT:
				a = regularCheckoutSP.removeQueue();
				if (a == null) {
					System.err.println("Warning: regular checkout departure event but queue was empty.");
					break;
				}
				regularCheckoutCustomers = Math.max(0, regularCheckoutCustomers - 1);
				exitCustomers++;

				a.setCheckoutEndTime(Clock.getInstance().getTime());
				a.reportPaymentSuccessful();
				a.reportResults();
				updateVisuals();
				if (regularCheckoutSP.isOnQueue()) {
					regularCheckoutSP.beginService();
				}
				break;

			case DEP_SELF_CHECKOUT:
				a = selfCheckoutSP.removeQueue();
				if (a == null) {
					System.err.println("Warning: self checkout departure event but queue was empty.");
					break;
				}

				selfCheckoutCustomers = Math.max(0, selfCheckoutCustomers - 1);
				exitCustomers++;

				a.setCheckoutEndTime(Clock.getInstance().getTime());
				a.reportPaymentSuccessful();
				a.reportResults();

				updateVisuals();

				if (selfCheckoutSP.isOnQueue()){
					selfCheckoutSP.beginService();
				}
				break;
		}
	}

	private void routeToCheckout(Customer a) {

		a.setCheckoutStartTime(Clock.getInstance().getTime());

		if (a.getItemCount() <= config.selfMaxItems) {
			selfCheckoutSP.addQueue(a);
			if (!selfCheckoutSP.isReserved()) selfCheckoutSP.beginService();
		} else {
			regularCheckoutSP.addQueue(a);
			if (!regularCheckoutSP.isReserved()) regularCheckoutSP.beginService();
		}
	}
	private void updateVisuals() {
        int entranceCustomers = 0;
        int shoppingCustomers = 0;
        int decisionCustomers = 0;
        controller.visualiseCustomerStages(
                entranceCustomers,
                shoppingCustomers,
                decisionCustomers,
				regularCheckoutCustomers,
				selfCheckoutCustomers,
				exitCustomers
		);
	}

	@Override
	protected void results() {
		controller.showEndTime(Clock.getInstance().getTime());
	}
}