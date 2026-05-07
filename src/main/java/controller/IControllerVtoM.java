package controller;

import simu.model.SimulationConfig;

/* interface for the UI */
public interface IControllerVtoM {
	public void startSimulation(SimulationConfig config);

    // Override methods for pause, resume, step and reset
    void pauseSimulation();

	void resumeSimulation();

	void stepSimulation();

	void resetSimulation();
}
