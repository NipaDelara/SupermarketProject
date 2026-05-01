package controller;

import simu.model.SimulationConfig;

/* interface for the UI */
public interface IControllerVtoM {
		// public void startSimulation();
		public void startSimulation(SimulationConfig config);

		public void increaseSpeed();
		public void decreaseSpeed();

    // Override methods for pause,resume and step
    void pauseSimulation();

	void resumeSimulation();

	void stepSimulation();
}
