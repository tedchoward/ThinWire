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
public final class ActionEvent extends EventObject {
    private String stringValue;
    private Component sourceComponent;
    private String action;
    private int sourceComponentX;
    private int sourceComponentY;
    private int sourceX;
    private int sourceY;

    public ActionEvent(String action, Component sourceComponent) {
        this(action, sourceComponent, null, -1, -1, -1, -1, true);
    }

    public ActionEvent(String action, Component sourceComponent, int sourceComponentX, int sourceComponentY) {
        this(action, sourceComponent, null, sourceComponentX, sourceComponentY, -1, -1, true);
    }
    
    public ActionEvent(String action, Component sourceComponent, Object source) {
        this(action, sourceComponent, source, -1, -1, -1, -1, true);
    }
    
    public ActionEvent(String action, Component sourceComponent, Object source, int sourceComponentX, int sourceComponentY, int sourceX, int sourceY) {
        this(action, sourceComponent, source, sourceComponentX, sourceComponentY, sourceX, sourceY, true);
    }
    
    private ActionEvent(String action, Component sourceComponent, Object source, int sourceComponentX, int sourceComponentY, int sourceX, int sourceY, boolean init) {
        super(source == null ? sourceComponent : source);
        if (sourceComponent == null) throw new IllegalArgumentException("sourceComponent == null");
        if (init && sourceComponentX == -1) sourceComponentX = 0;
        if (init && sourceComponentY == -1) sourceComponentY = 0;
        if (sourceComponentX < 0) throw new IllegalArgumentException("sourceComponentX{" + sourceComponentX + "} < 0");
        if (sourceComponentY < 0) throw new IllegalArgumentException("sourceComponentY{" + sourceComponentY + "} < 0");
        if (init && sourceX == -1) sourceX = sourceComponentX;
        if (init && sourceY == -1) sourceY = sourceComponentY;
        if (sourceX < 0) throw new IllegalArgumentException("sourceX{" + sourceX + "} < 0");
        if (sourceY < 0) throw new IllegalArgumentException("sourceY{" + sourceY + "} < 0");
        if (action == null || !action.equals(Component.ACTION_CLICK) && !action.equals(Component.ACTION_DOUBLE_CLICK)) throw new IllegalArgumentException("action == null || !action.equals(ActionEventComponent.ACTION_CLICK) && !action.equals(ActionEventComponent.ACTION_DOUBLE_CLICK)");
        
        this.action = action;
        this.sourceComponent = sourceComponent;
        this.sourceComponentX = sourceComponentX;
        this.sourceComponentY = sourceComponentY;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
    }
    
    public String getAction() {
        return action;
    }
    
    public Component getSourceComponent() {
        return sourceComponent;
    }

    public int getSourceComponentX() {
        return sourceComponentX;
    }
    
    public int getSourceComponentY() {
        return sourceComponentY;
    }

    public int getSourceX() {
        return sourceX;
    }
    
    public int getSourceY() {
        return sourceY;
    }
    
    public boolean equals(Object o) {
        return o instanceof ActionEvent && toString().equals(o.toString());
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
    
    public String toString() {
        if (stringValue == null) stringValue = "ActionEvent{action:" + action + 
            ",sourceComponent:" + sourceComponent.getClass().getName() + "@" + System.identityHashCode(sourceComponent) +
            ",source:" + source + "@" + System.identityHashCode(source) + 
            ",sourceComponentX:" + sourceComponentX + ",sourceComponentY:" + sourceComponentY + 
            ",sourceX:" + sourceX + ",sourceY:" + sourceY + "}";
        return stringValue;
    }
}
