/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.util.EventObject;

/**
 * Sends a trigger to a listener when something changes in an item.
 * @author Joshua J. Gertzen
 */
public final class ItemChangeEvent extends EventObject {
    private Type type;
	private Object position;
	private Object oldValue;
	private Object newValue;
    
    public static enum Type { ADD, REMOVE, SET; }	
	
    public ItemChangeEvent(Object source, Type type, Object position, Object oldValue, Object newValue) {
        super(source);
	    this.type = type;
	    this.position = position;
		this.oldValue = oldValue;
		this.newValue = newValue;
    }
    
    public Type getType() {
        return type;
    }
    
	public Object getPosition() {
		return position;
	}
	
	public Object getNewValue() {
		return newValue;
	}
	
	public Object getOldValue() {
		return oldValue;
	}	
}
