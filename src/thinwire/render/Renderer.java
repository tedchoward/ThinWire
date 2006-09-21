/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render;

import java.util.EventListener;
import java.util.Set;

import thinwire.ui.event.PropertyChangeListener;

/**
 * @author Joshua J. Gertzen
 */
public interface Renderer extends PropertyChangeListener {
    void eventSubTypeListenerInit(Class<? extends EventListener> clazz, Set<String> subTypes);
    void eventSubTypeListenerAdded(Class<? extends EventListener> clazz, String subType);
    void eventSubTypeListenerRemoved(Class<? extends EventListener> clazz, String subType);
}
