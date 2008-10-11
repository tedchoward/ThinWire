package thinwire.ui.event;

import java.util.EventObject;

/**
 * @author Thomas Smith
 *
 */
public class WindowEvent extends EventObject {
	private EventType eventType=EventType.WINDOW_SHOWN;

public static enum EventType{
	WINDOW_SHOWN,WINDOW_CLOSED
}
	public WindowEvent(Object source,EventType type) {
		super(source);
		this.eventType=type;
	}
	public EventType getEventType() {
		return eventType;
	}

}
