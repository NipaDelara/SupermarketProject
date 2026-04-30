package controller;

/**
 * Contract the View uses to drive the simulation engine through the Controller.
 *
 * Original methods (Delara): startSimulation, increaseSpeed, decreaseSpeed.
 *
 * View-driven additions (Noel) — declared as default methods so the existing
 * Controller implementation continues to compile. When Delara wires the real
 * pause/step/reset logic in Controller.java the defaults will be overridden.
 */
public interface IControllerVtoM {
    void startSimulation();
    void increaseSpeed();
    void decreaseSpeed();

    /** Pause the running simulation. Default: log only. */
    default void pauseSimulation() {
        System.out.println("[Controller] pauseSimulation() not implemented yet");
    }

    /** Resume from a paused state. Default: log only. */
    default void resumeSimulation() {
        System.out.println("[Controller] resumeSimulation() not implemented yet");
    }

    /** Advance one event while paused. Default: log only. */
    default void stepSimulation() {
        System.out.println("[Controller] stepSimulation() not implemented yet");
    }

    /** Stop the current run and clear results. Default: log only. */
    default void resetSimulation() {
        System.out.println("[Controller] resetSimulation() not implemented yet");
    }
}
