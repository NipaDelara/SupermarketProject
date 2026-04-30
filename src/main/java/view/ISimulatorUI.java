package view;

/**
 * Contract that the View exposes to the Controller.
 * Controller calls these to read user inputs and to push results back to the UI.
 *
 * NEW methods added by Noel (View work):
 *   - getArrivalRate(), getRegularCheckouts(), getSelfCheckouts()
 * Existing Controller code is unaffected because new methods have safe defaults.
 */
public interface ISimulatorUI {

    /* ----- Inputs the Controller asks the View for ----- */
    double getTime();
    long getDelay();

    /** Mean inter-arrival time (minutes). Default keeps current behavior. */
    default double getArrivalRate() { return 15.0; }

    /** Number of regular checkout counters configured by the user. */
    default int getRegularCheckouts() { return 1; }

    /** Number of self-checkout stations configured by the user. */
    default int getSelfCheckouts() { return 1; }

    /* ----- Outputs the Controller pushes back to the View ----- */
    void setEndingTime(double time);

    /** Animation surface owned by the View. */
    IVisualisation getVisualisation();
}
