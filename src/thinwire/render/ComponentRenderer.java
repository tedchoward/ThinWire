package thinwire.render;

import java.util.EventListener;
import java.util.Set;

import thinwire.render.web.WebComponentEvent;
import thinwire.ui.event.PropertyChangeEvent;

public interface ComponentRenderer {

	public abstract void eventSubTypeListenerInit(
			Class<? extends EventListener> clazz, Set<Object> subTypes);

	public abstract void eventSubTypeListenerAdded(
			Class<? extends EventListener> clazz, Object subType);

	public abstract void eventSubTypeListenerRemoved(
			Class<? extends EventListener> clazz, Object subType);

	public abstract void componentChange(WebComponentEvent event);

	public abstract void propertyChange(PropertyChangeEvent pce);

}