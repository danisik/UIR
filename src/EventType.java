package uirApp;
/**
 * Typy událostí vyskytující se v datových souborech (csv)
 * @author Vojtech Danisik
 *
 */
public enum EventType {
	
	POLICY("po"),
	INDUSTRY("pr"),
	AGRICULTURE("ze"),
	SPORT("sp"),
	CULTURE("ku"),
	CRIMINALITY("kr"),
	WEATHER("pc"),
	OTHER("ji"),
	NOT_EVENT("-");
	
	private String shortcut;
	
	private EventType(String shortcut) {
		this.shortcut = shortcut;
	}
	
	public String getShortcut() {
		return shortcut;
	}
}
