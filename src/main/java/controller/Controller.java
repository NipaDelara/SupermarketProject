package controller;

import javafx.application.Platform;
import simu.dao.SimulationRun;
import simu.dao.SimulationRunDAO;
import simu.framework.IEngine;
import simu.model.MyEngine;
import simu.model.SimulationConfig;
import view.ISimulatorUI;

import java.sql.SQLException;

public class Controller implements IControllerVtoM, IControllerMtoV {   // NEW
	private IEngine engine;
	private final ISimulatorUI ui;

	// Persistence (Noel) — set in startSimulation, used in showEndTime
	private final SimulationRunDAO runDao = new SimulationRunDAO();
	private SimulationConfig lastConfig;
	private int customersSeen;

	public Controller(ISimulatorUI ui) {

		this.ui = ui;
	}

	/* Engine control: */

	@Override
	public void startSimulation(SimulationConfig config) {
		// Remember the config so showEndTime can persist it (Noel)
		this.lastConfig = config;
		this.customersSeen = 0;

		engine = new MyEngine(this, config);  // pass config
		engine.setSimulationTime(ui.getTime());
		engine.setDelay(ui.getDelay());
		ui.getVisualisation().clearDisplay();
		((Thread) engine).start();
	}


	/* Simulation results passing to the UI
	 * Because FX-UI updates come from engine thread, they need to be directed to the JavaFX thread
	 */
	@Override
	public void showEndTime(double time) {
		Platform.runLater(()->ui.setEndingTime(time));

		// === Persistence hook (Noel) =====================================
		// Save the finished run to MariaDB. Failures are logged but never
		// allowed to crash the simulator — the GUI must keep working
		// even if the database is offline.
		try {
			persistRun(time);
		} catch (Exception ex) {
			System.err.println("[Controller] Could not persist run: " + ex.getMessage());
		}
	}

	@Override
	public void visualiseCustomer() {
		// Track for persistence (Noel)
		customersSeen++;
		Platform.runLater(() -> ui.getVisualisation().newCustomer());
	}
	@Override
	public void visualiseCustomerStages(
			int entrance,
			int shopping,
			int decision,
			int regularCheckout,
			int selfCheckout,
			int exit
	) {
		customersSeen = entrance + shopping + decision + regularCheckout + selfCheckout + exit;

		Platform.runLater(() -> ui.getVisualisation().showCustomerStages(
				entrance,
				shopping,
				decision,
				regularCheckout,
				selfCheckout,
				exit
		));
	}
	// Override methods for pause,resume and step
	@Override
	public void pauseSimulation() {
		if (engine != null) {
			engine.pauseSimulation();
		}
	}

	@Override
	public void resumeSimulation() {
		if (engine != null) {
			engine.resumeSimulation();
		}
	}

	@Override
	public void stepSimulation() {
		if (engine != null) {
			engine.stepSimulation();
		}
	}

	@Override
	public void resetSimulation() {
		if (engine != null) {
			engine.pauseSimulation();
			engine = null;
		}

		customersSeen = 0;
		lastConfig = null;

		Platform.runLater(() -> {
			ui.getVisualisation().clearDisplay();
			ui.setEndingTime(0);
		});

		System.out.println("[Controller] Simulation reset.");
	}

	/* ======================== Helpers (Noel) ======================== */

	private void persistRun(double endTime) throws SQLException {
		if (lastConfig == null) {
			System.err.println("[Controller] No config recorded; skipping persistence.");
			return;
		}
		SimulationRun row = new SimulationRun();
		row.setSimulationTimeMinutes(ui.getTime());
		row.setDelayMs(ui.getDelay());
		row.setArrivalDistType(lastConfig.arrivalDistType);
		row.setArrivalMean(lastConfig.arrivalMean);
		row.setArrivalStd(lastConfig.arrivalStd);
		row.setSelfMaxItems(lastConfig.selfMaxItems);
		row.setEndTimeMinutes(endTime);
		row.setCustomersProcessed(customersSeen);
		row.setNotes("Auto-saved from Controller.showEndTime()");

		// Throughput = customers / hours
		double hours = Math.max(endTime / 60.0, 0.0001);
		row.addMetric("throughput", customersSeen / hours, "cust/h");

		long id = runDao.save(row);
		System.out.println("[Controller] Persisted simulation run id=" + id +
				" (customers=" + customersSeen + ", endTime=" + endTime + ")");
	}
}
