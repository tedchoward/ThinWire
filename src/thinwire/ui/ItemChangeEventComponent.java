/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.ui.event.ItemChangeListener;

/**
 * @author Joshua J. Gertzen
 */
public interface ItemChangeEventComponent extends Component {
    /**
     * Adds a listener which executes a method when something is changed.
     * @param listener the listener to add
     */
    public void addItemChangeListener(ItemChangeListener listener);

    /**
     * Removes an existing itemChangeListener.
     * @param listener the listener to remove
     */
    public void removeItemChangeListener(ItemChangeListener listener);
}
