package simu.model;

public class Statistics {

    private int completedCustomers = 0;
    private double totalTime = 0;

    public void addCustomer(Customer c) {
        completedCustomers++;
        totalTime += (c.getRemovalTime() - c.getArrivalTime());
    }

    public void printReport() {
        System.out.println("---- SIMULATION REPORT ----");
        System.out.println("Customers completed: " + completedCustomers);

        if (completedCustomers > 0) {
            System.out.println("Average time: " + (totalTime / completedCustomers));
        }
    }
}
