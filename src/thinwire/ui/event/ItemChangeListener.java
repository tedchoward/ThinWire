/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.util.EventListener;

/**
 * Extension of java.util.EventListener which provides an interface to respond to changes.
 * @author Joshua J. Gertzen
 */
public interface ItemChangeListener extends EventListener {
    public void itemChange(ItemChangeEvent ev);
}
