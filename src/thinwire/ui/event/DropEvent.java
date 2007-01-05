/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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

/**
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public final class DropEvent extends EventObject {
    private String stringValue;
    private Component sourceComponent;
    private Component dragComponent;
    private Object dragObject;
    private int sourceComponentX;
    private int sourceComponentY;
    private int sourceX;
    private int sourceY;
    private int dragComponentX;
    private int dragComponentY;
    private int dragX;
    private int dragY;
    
    public DropEvent(Component sourceComponent, Component dragComponent) {
        this(sourceComponent, null, -1, -1, -1, -1, dragComponent, null, -1, -1, -1, -1, true);
    }

    public DropEvent(Component sourceComponent, int sourceComponentX, int sourceComponentY, Component dragComponent, int dragComponentX, int dragComponentY) {
        this(sourceComponent, null, sourceComponentX, sourceComponentY, -1, -1, dragComponent, null, dragComponentX, dragComponentY, -1, -1, true);
    }

    public DropEvent(Component sourceComponent, Object source, Component dragComponent, Object dragObject) {
        this(sourceComponent, source, -1, -1, -1, -1, dragComponent, dragObject, -1, -1, -1, -1, true);
    }

    public DropEvent(Component sourceComponent, Object source, int sourceComponentX, int sourceComponentY, int sourceX, int sourceY, Component dragComponent, Object dragObject, int dragComponentX, int dragComponentY, int dragX, int dragY) {        
        this(sourceComponent, source, sourceComponentX, sourceComponentY, sourceX, sourceY, dragComponent, dragObject, dragComponentX, dragComponentY, dragX, dragY, true);
    }
    
    private DropEvent(Component sourceComponent, Object source, int sourceComponentX, int sourceComponentY, int sourceX, int sourceY, Component dragComponent, Object dragObject, int dragComponentX, int dragComponentY, int dragX, int dragY, boolean init) {        
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

        if (dragComponent == null) throw new IllegalArgumentException("dragComponent == null");
        if (dragObject == null) dragObject = dragComponent;
        if (init && dragComponentX == -1) dragComponentX = 0;
        if (init && dragComponentY == -1) dragComponentY = 0;
        if (dragComponentX < 0) throw new IllegalArgumentException("dragComponentX{" + dragComponentX + "} < 0");
        if (dragComponentY < 0) throw new IllegalArgumentException("dragComponentY{" + dragComponentY + "} < 0");
        if (init && dragX == -1) dragX = dragComponentX;
        if (init && dragY == -1) dragY = dragComponentY;
        if (dragX < 0) throw new IllegalArgumentException("dragX{" + dragX + "} < 0");
        if (dragY < 0) throw new IllegalArgumentException("dragY{" + dragY + "} < 0");

        this.sourceComponent = sourceComponent;
        this.sourceComponentX = sourceComponentX;
        this.sourceComponentY = sourceComponentY;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        
        this.dragComponent = dragComponent;
        this.dragObject = dragObject;
        this.dragComponentX = dragComponentX;
        this.dragComponentY = dragComponentY;
        this.dragX = dragX;
        this.dragY = dragY;
    }
    
    public Component getSourceComponent() {
        return sourceComponent;
    }
    
    public Component getDragComponent() {
        return dragComponent;
    }
        
    public Object getDragObject() {
        return dragObject;
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
    
    public int getDragComponentX() {
        return dragComponentX;
    }
    
    public int getDragComponentY() {
        return dragComponentY;
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
        if (stringValue == null) stringValue = "DropEvent{" + 
        "sourceComponent:" + sourceComponent.getClass().getName() + "@" + System.identityHashCode(sourceComponent) +
        ",source:" + source + "@" + System.identityHashCode(source) + 
        ",sourceComponentX:" + sourceComponentX + ",sourceComponentY:" + sourceComponentY + 
        ",sourceX:" + sourceX + ",sourceY:" + sourceY +
        ",dragComponent:" + dragComponent.getClass().getName() + "@" + System.identityHashCode(dragComponent) +
        ",dragObject:" + dragObject + "@" + System.identityHashCode(dragObject) + 
        ",dragComponentX:" + dragComponentX + ",dragComponentY:" + dragComponentY + 
        ",dragX:" + dragX + ",dragY:" + dragY +
        "}";
        return stringValue;
    }
}
