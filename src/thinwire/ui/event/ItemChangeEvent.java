/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.ui.event;

import java.util.EventObject;

import thinwire.ui.GridBox;
import thinwire.ui.ItemChangeEventComponent;

/**
 * Sends a trigger to a listener when something changes in an item.
 * @author Joshua J. Gertzen
 */
public final class ItemChangeEvent extends EventObject {
    private String stringValue;
    private ItemChangeEventComponent sourceComponent;    
    private Type type;
	private Object position;
	private Object oldValue;
	private Object newValue;
    
    public static enum Type { ADD, REMOVE, SET; }	
	    
    public ItemChangeEvent(ItemChangeEventComponent sourceComponent, Type type, Object position, Object oldValue, Object newValue) {
        this(sourceComponent, null, type, position, oldValue, newValue);
    }
    
    public ItemChangeEvent(ItemChangeEventComponent sourceComponent, Object source, Type type, Object position, Object oldValue, Object newValue) {
        super(source == null ? sourceComponent : source);
        if (sourceComponent == null) throw new IllegalArgumentException("sourceComponent == null");
        if (type == null) throw new IllegalArgumentException("type == null");
        if (position == null) throw new IllegalArgumentException("position == null");
        if (!(position instanceof Integer || position instanceof GridBox.Range)) throw new IllegalArgumentException("!(position instanceof Integer || position instanceof GridBox.Range)");
        this.sourceComponent = sourceComponent;
	    this.type = type;
	    this.position = position;
		this.oldValue = oldValue;
		this.newValue = newValue;
    }
    
    public ItemChangeEventComponent getSourceComponent() {
        return sourceComponent;
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

    public boolean equals(Object o) {
        return o instanceof PropertyChangeEvent && toString().equals(o.toString());
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
    
    public String toString() {
        if (stringValue == null) stringValue = "ItemChangeEvent{sourceComponent:" + sourceComponent.getClass().getName() + "@" + System.identityHashCode(sourceComponent) + 
            ",source:" + source + ",type:" + type + ",position:" + position + ",newValue:" + newValue + ",oldValue:" + oldValue + "}";
        return stringValue;
    }
}
