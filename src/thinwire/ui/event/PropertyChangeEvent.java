/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.util.EventObject;

/**
 * @author Joshua J. Gertzen
 */
public final class PropertyChangeEvent extends EventObject {
    private String propertyName;
    private Object newValue;
    private Object oldValue;
    
    public PropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source);
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    public String getPropertyName() {
        return propertyName;
    }
    
    public Object getNewValue() {
        return newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }
}
