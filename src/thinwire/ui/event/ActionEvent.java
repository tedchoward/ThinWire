/*
                         ThinWire(TM) RIA Ajax Framework
               Copyright (C) 2003-2006 Custom Credit Systems
  
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.
  
  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  Place, Suite 330, Boston, MA 02111-1307 USA
 
  Users wishing to use this library in proprietary products which are not 
  themselves to be released under the GNU Public License should contact Custom
  Credit Systems for a license to do so.
  
                Custom Credit Systems, Richardson, TX 75081, USA.
                           http://www.thinwire.com
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.util.EventObject;

import thinwire.ui.Component;
import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 */
public final class ActionEvent extends EventObject {
    private String stringValue;
    private Component sourceComponent;
    private int sourceX;
    private int sourceY;
    private String action;

    public ActionEvent(Component sourceComponent, String action) {
        this(sourceComponent, null, action);
    }

    public ActionEvent(Component sourceComponent, int sourceX, int sourceY, String action) {
        this(sourceComponent, null, sourceX, sourceY, action);
    }
    
    public ActionEvent(Component sourceComponent, Object source, String action) {
        this(sourceComponent, source, 0, 0, action);
    }
    
    public ActionEvent(Component sourceComponent, Object source, int sourceX, int sourceY, String action) {
        super(source == null ? sourceComponent : source);
        if (sourceComponent == null) throw new IllegalArgumentException("sourceComponent == null");
        if (sourceX < 0) throw new IllegalArgumentException("sourceX{" + sourceX + "} < 0");
        if (sourceY < 0) throw new IllegalArgumentException("sourceY{" + sourceY + "} < 0");
        if (action == null || !action.equals(Component.ACTION_CLICK) && !action.equals(Component.ACTION_DOUBLE_CLICK)) throw new IllegalArgumentException("action == null || !action.equals(ActionEventComponent.ACTION_CLICK) && !action.equals(ActionEventComponent.ACTION_DOUBLE_CLICK)");
        this.sourceComponent = sourceComponent;
        this.action = action;
    }
    
    public Component getSourceComponent() {
        return sourceComponent;
    }

    public int getSourceX() {
        return sourceX;
    }
    
    public int getSourceY() {
        return sourceY;
    }
    
    public String getAction() {
        return action;
    }
    
    public boolean equals(Object o) {
        return o instanceof ActionEvent && toString().equals(o.toString());
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
    
    public String toString() {
        if (stringValue == null) stringValue = "ActionEvent{sourceComponent:" + sourceComponent.getClass().getName() + "@" + System.identityHashCode(sourceComponent) + 
            ",source:" + source + ",sourceX:" + sourceX + ",sourceY:" + sourceY + ",action:" + action + "}";
        return stringValue;
    }
}
