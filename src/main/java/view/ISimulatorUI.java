package view;

/**
 * Contract that the View exposes to the Controller.
 * Controller calls these to read user inputs and to push results back to the UI.
 */
public interface ISimulatorUI {

    /* ----- Inputs the Controller asks the View for ----- */
    double getTime();
    long getDelay();

    /* ----- Outputs the Controller pushes back to the View ----- */
    void setEndingTime(double time);

    /** Animation surface owned by the View. */
    IVisualisation getVisualisation();
}
