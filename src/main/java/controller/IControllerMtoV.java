package controller;

/**
 * Contract the simulation Engine uses to push events into the View
 * (through the Controller, which dispatches them to the JavaFX thread).
 *
 * Original methods (Delara): showEndTime, visualiseCustomer.
 *
 * View-driven additions (Noel) — declared as default methods so MyEngine
 * does not have to call them and any existing Controller still compiles.
 *   - visualiseCustomerToCheckout(boolean regular): customer left shopping
 *     and joined a checkout queue. true = regular, false = self.
 *   - visualiseCustomerLeft(boolean regular): customer finished checkout
 *     and exited the store.
 */
public interface IControllerMtoV {
    void showEndTime(double time);
    void visualiseCustomer();

    /** Notify the View that a customer entered a checkout queue. */
    default void visualiseCustomerToCheckout(boolean regular) { /* no-op */ }

    /** Notify the View that a customer finished service and exited. */
    default void visualiseCustomerLeft(boolean regular) { /* no-op */ }
}
