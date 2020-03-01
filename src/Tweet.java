package uirApp;

/**
 * Uložení tweetu do třídy 
 * @author Vojtech Danisik
 *
 */
public class Tweet {
	
	/** Text tweetu */
	private String message;
	/** vektor reprezentující tweet */
	private double[] vector;
	/** typ eventu, určený metodou*/
	private EventType event;
	/** typ eventu, ze souboru */
	private EventType eventReal;
	/** značí zda je event (1 - ano, 0 - ne), určený metodou */
	private boolean isEvent;
	/** značí zda je event (1 - ano, 0 - ne), ze souboru */
	private boolean isEventReal;
	
	public Tweet(String eventShortcut, String message) {
		this.message = message;
		for(EventType et: EventType.values()) {
			if(et.getShortcut().equals(eventShortcut)) {
				this.eventReal = et;
				if(!et.equals(EventType.NOT_EVENT)) {
					isEventReal = true;
				}
				else isEventReal = false;
				break;
			}
		}
	}
	
	public EventType getEventReal() {
		return eventReal;
	}

	public boolean getIsEventReal() {
		return isEventReal;
	}

	public boolean getIsEvent() {
		return isEvent;
	}

	public void setIsEvent(boolean isEvent) {
		this.isEvent = isEvent;
	}

	public String getMessage() {
		return message;
	}
	
	public double[] getVector() {
		return vector;
	}

	public void setVector(double[] vector) {
		this.vector = vector;
	}

	public EventType getEvent() {
		return event;
	}

	public void setEvent(EventType event) {
		this.event = event;
		if(event == EventType.NOT_EVENT) isEvent = false;
		else isEvent = true;
	}

	@Override
	public String toString() {
		return message;
	}
}
