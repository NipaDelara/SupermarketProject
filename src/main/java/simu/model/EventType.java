package simu.model;

import simu.framework.IEventType;

public enum EventType implements IEventType {
	//arrival event
	ARR1,

	//shopping area
	DEP_PRODUCE,
	DEP_DAIRY,
	DEP_GROCERY,
	DEP_BEVERAGE,

	//checkout areas
	DEP_REGULAR_CHECKOUT,
	DEP_SELF_CHECKOUT,

}
