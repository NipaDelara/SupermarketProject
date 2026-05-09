package view;

/**
 * Drawing surface contract used by the Controller to push visual events
 * into the supermarket animation.
 *
 * Original methods (kept for backwards compatibility):
 *   - clearDisplay(): wipes the canvas (called at simulation start)
 *   - newCustomer(): notifies that a new customer entered the system
 *
 * Optional new methods (default no-ops so old code keeps compiling):
 *   - customerToCheckout(boolean): customer left shopping, joined a checkout queue
 *   - customerLeft(boolean): customer finished checkout and exited the store
 */
public interface IVisualisation {
    void clearDisplay();
    void newCustomer();

    /** A customer reached a checkout queue. true=regular, false=self. */
    default void customerToCheckout(boolean regular) { /* default no-op */ }

    /** A customer finished service and exited. true=regular, false=self. */
    default void customerLeft(boolean regular) { /* default no-op */ }

    void showCustomerStages(
            int entrance,
            int shopping,
            int decision,
            int regularCheckout,
            int selfCheckout,
            int exit);

}
