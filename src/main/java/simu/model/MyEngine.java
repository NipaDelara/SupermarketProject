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

	private final ServicePoint produceSP;
	private final ServicePoint dairySP;
	private final ServicePoint grocerySP;
	private final ServicePoint beveragesSP;
	private final ServicePoint regularCheckoutSP;
	private final ServicePoint selfCheckoutSP;

	public MyEngine(IControllerMtoV controller) {
		super(controller);

		servicePoints = new ServicePoint[6];

		produceSP = new ServicePoint(new Normal(8, 2), eventList, EventType.DEP_PRODUCE);
		dairySP = new ServicePoint(new Normal(6, 2), eventList, EventType.DEP_DAIRY);
		grocerySP = new ServicePoint(new Normal(10, 3), eventList, EventType.DEP_GROCERY);
		beveragesSP = new ServicePoint(new Normal(7, 2), eventList, EventType.DEP_BEVERAGES);

		regularCheckoutSP = new ServicePoint(new Normal(6, 2), eventList, EventType.DEP_REGULAR_CHECKOUT);
		selfCheckoutSP = new ServicePoint(new Normal(4, 1), eventList, EventType.DEP_SELF_CHECKOUT);

		servicePoints[0] = produceSP;
		servicePoints[1] = dairySP;
		servicePoints[2] = grocerySP;
		servicePoints[3] = beveragesSP;
		servicePoints[4] = regularCheckoutSP;
		servicePoints[5] = selfCheckoutSP;

		arrivalProcess = new ArrivalProcess(new Negexp(15, 5), eventList, EventType.ARR1);
	}

	public void setServicePointDistribution(int idx, String distType, double... params) {
		if (idx < 0 || idx >= servicePoints.length) return;

		ContinuousGenerator newGen = createGenerator(distType, params);
		servicePoints[idx].setGenerator(newGen);
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
				a = new Customer();
				a.setShoppingStartTime(Clock.getInstance().getTime());

				int route = (int) (Math.random() * 4);

				if (route == 0) {
					produceSP.addQueue(a);
					if (!produceSP.isReserved()) produceSP.beginService();
				} else if (route == 1) {
					dairySP.addQueue(a);
					if (!dairySP.isReserved()) dairySP.beginService();
				} else if (route == 2) {
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
				a.setCheckoutEndTime(Clock.getInstance().getTime());
				a.reportPaymentSuccessful();
				a.reportResults();

				if (regularCheckoutSP.isOnQueue()) regularCheckoutSP.beginService();
				break;

			case DEP_SELF_CHECKOUT:
				a = selfCheckoutSP.removeQueue();
				a.setCheckoutEndTime(Clock.getInstance().getTime());
				a.reportPaymentSuccessful();
				a.reportResults();

				if (selfCheckoutSP.isOnQueue()) selfCheckoutSP.beginService();
				break;
		}
	}

	private void routeToCheckout(Customer a) {
		a.setCheckoutStartTime(Clock.getInstance().getTime());

		if (a.getItemCount() <= 10) {
			selfCheckoutSP.addQueue(a);
			if (!selfCheckoutSP.isReserved()) selfCheckoutSP.beginService();
		} else {
			regularCheckoutSP.addQueue(a);
			if (!regularCheckoutSP.isReserved()) regularCheckoutSP.beginService();
		}
	}

	@Override
	protected void results() {
		controller.showEndTime(Clock.getInstance().getTime());
	}
}