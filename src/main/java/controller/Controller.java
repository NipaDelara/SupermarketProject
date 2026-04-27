package controller;

import javafx.application.Platform;
import simu.framework.IEngine;
import simu.model.MyEngine;
import view.ISimulatorUI;

public class Controller implements IControllerVtoM, IControllerMtoV {   // NEW
	private IEngine engine;
	private final ISimulatorUI ui;

	public Controller(ISimulatorUI ui) {

		this.ui = ui;
	}

	/* Engine control: */
	@Override
	public void startSimulation() {
		engine = new MyEngine(this); // new Engine thread is created for every simulation
		engine.setSimulationTime(ui.getTime());
		engine.setDelay(ui.getDelay());

		ui.getVisualisation().clearDisplay();
		((Thread) engine).start();
		//((Thread)engine).run(); // Never like this, why?
	}

	@Override
	public void decreaseSpeed() { //  // Slow down simulation: increase delay

		if (engine != null) {
			long newDelay = (long) (engine.getDelay() * 1.10);

			// Maximum delay limit
			engine.setDelay(Math.min(newDelay, 2000));

			System.out.println("Slow down -> delay: " + engine.getDelay());
		}
	}

	@Override
	public void increaseSpeed() {  // Speed up simulation: decrease delay

		if (engine != null) {
			long newDelay = (long) (engine.getDelay() * 0.90);

			// Minimum delay limit
			engine.setDelay(Math.max(newDelay, 10));

			System.out.println("Speed up -> delay: " + engine.getDelay());
		}
	}


	/* Simulation results passing to the UI
	 * Because FX-UI updates come from engine thread, they need to be directed to the JavaFX thread
	 */
	@Override
	public void showEndTime(double time) {
		Platform.runLater(()->ui.setEndingTime(time));
	}

	@Override
	public void visualiseCustomer() {
		Platform.runLater(() -> ui.getVisualisation().newCustomer());
	}
}
