/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
   	            email: info@thinwire.com    ph: +1 (888) 644-6405
 	                        http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
#VERSION_HEADER#
*/
package thinwire.ui.event;

import java.util.EventObject;

import thinwire.ui.Component;
import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public final class DropEvent extends EventObject {
    private String stringValue;
    private Component sourceComponent;
    private int sourceX;
    private int sourceY;
    private Object dragObject;
    private Component dragComponent;
    private int dragX;
    private int dragY;
    
    public DropEvent(Component sourceComponent, Component dragComponent) {
        this(sourceComponent, null, 0, 0, dragComponent, null, 0, 0);

    }

    public DropEvent(Component sourceComponent, int sourceX, int sourceY, Component dragComponent, int dragX, int dragY) {
        this(sourceComponent, null, sourceX, sourceY, dragComponent, null, dragX, dragY);
    }    
    
    public DropEvent(Component sourceComponent, Object source, Component dragComponent, Object dragObject) {
        this(sourceComponent, source, 0, 0, dragComponent, dragObject, 0, 0);
    }
    
    public DropEvent(Component sourceComponent, Object source, int sourceX, int sourceY, Component dragComponent, Object dragObject, int dragX, int dragY) {        
        super(source == null ? sourceComponent : source);
        if (sourceComponent == null || dragComponent == null) throw new IllegalArgumentException("sourceComponent == null || dragComponent == null");
        if (sourceX < 0) throw new IllegalArgumentException("sourceX{" + sourceX + "} < 0");
        if (sourceY < 0) throw new IllegalArgumentException("sourceY{" + sourceY + "} < 0");
        if (dragX < 0) throw new IllegalArgumentException("dragX{" + dragX + "} < 0");
        if (dragY < 0) throw new IllegalArgumentException("dragY{" + dragY + "} < 0");
        this.sourceComponent = sourceComponent;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.dragObject = dragObject == null ? dragComponent : dragObject;
        this.dragComponent = dragComponent;
        this.dragX = dragX;
        this.dragY = dragY;
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
    
    public Object getDragObject() {
        return dragObject;
    }
    
    public Component getDragComponent() {
        return dragComponent;
    }
    
    public int getDragX() {
        return dragX;
    }
    
    public int getDragY() {
        return dragY;
    }
    
    public boolean equals(Object o) {
        return o instanceof DropEvent && toString().equals(o.toString());
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
    
    public String toString() {
        if (stringValue == null) stringValue = "DropEvent{sourceComponent:" + sourceComponent.getClass().getName() + "@" + System.identityHashCode(sourceComponent) + 
            ",source:" + source + ",sourceX:" + getSourceX() + ",sourceY:" + getSourceY() + 
            ",dragComponent:" + dragComponent.getClass().getName() + "@" + System.identityHashCode(dragComponent) + 
            ",dragObject:" + dragObject + ",dragX:" + getDragX() + ",dragY:" + getDragY() + "}";
        return stringValue;
    }
}
