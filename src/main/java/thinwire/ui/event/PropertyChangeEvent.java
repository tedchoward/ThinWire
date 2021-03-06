/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package thinwire.ui.event;

import java.util.EventObject;

import thinwire.ui.Component;

/**
 * @author Joshua J. Gertzen
 */
public final class PropertyChangeEvent extends EventObject {
    private String stringValue;
    private Component sourceComponent;
    private String propertyName;
    private Object newValue;
    private Object oldValue;

    public PropertyChangeEvent(String propertyName, Object oldValue, Object newValue, Component sourceComponent) {
        this(propertyName, oldValue, newValue, sourceComponent, null);
    }
    
    public PropertyChangeEvent(String propertyName, Object oldValue, Object newValue, Component sourceComponent, Object source) {
        super(source == null ? sourceComponent : source);
        if (sourceComponent == null) throw new IllegalArgumentException("sourceComponent == null");
        if (propertyName == null || propertyName.length() == 0) throw new IllegalArgumentException("propertyName == null || propertyName.length() == 0");
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.sourceComponent = sourceComponent;
    }
    
    public String getPropertyName() {
        return propertyName;
    }

    public Component getSourceComponent() {
        return sourceComponent;
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
        if (stringValue == null) stringValue = "PropertyChangeEvent{propertyName:" + propertyName + 
            ",oldValue:" + oldValue + ",newValue:" + newValue +
            ",sourceComponent:" + sourceComponent.getClass().getName() + "@" + System.identityHashCode(sourceComponent) + 
            ",source:" + source + "@" + System.identityHashCode(source) + "}";  
        return stringValue;
    }
}
