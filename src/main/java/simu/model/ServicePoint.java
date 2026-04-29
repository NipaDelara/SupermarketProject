package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;

import java.util.LinkedList;

public class ServicePoint {

	private final LinkedList<Customer> jono = new LinkedList<Customer>(); // Data Structure used
	private ContinuousGenerator generator;   // Service time distribution
	private final EventList eventList;   // Event list where the next departure event is added
	private final EventType eventTypeScheduled;  // Departure event type for this service point

	private boolean reserved = false;     // True when this service point is busy

	public ServicePoint(ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi){
		this.eventList = tapahtumalista;
		this.generator = generator;
		this.eventTypeScheduled = tyyppi;
				
	}
	// Add customer to the queue
	public void addQueue(Customer customer) {
		jono.add(customer);
	}

	// Remove serviced customer
	public Customer removeQueue(){
		reserved = false;
		return jono.poll();
	}

	// Start service for the first customer in the queue
	public void beginService() {
		reserved = true;

		double serviceTime = generator.sample();

		// Prevent negative service time if Normal distribution gives negative value
		if (serviceTime < 0) {
			serviceTime = 0;
		}
		eventList.add(new Event(
				eventTypeScheduled,
				Clock.getInstance().getTime() + serviceTime
		));
	}

	// Allows changing service time distribution later from GUI/input
	public void setGenerator(ContinuousGenerator generator) {
		this.generator = generator;}



	public boolean isReserved(){

		return reserved;
	}
	// Check if there are customers in queue
	public boolean isOnQueue() {

		return jono.size() != 0;
	}
	// Added for statistics / GUI display
	public int getQueueLength() {
		return jono.size();}
}
