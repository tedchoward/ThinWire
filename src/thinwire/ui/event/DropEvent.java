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

import thinwire.ui.DropEventComponent;

/**
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public final class DropEvent extends EventObject {
    private DropEventComponent sourceComponent;
    private Object dragObject;
    private DropEventComponent dragComponent;
    
    public DropEvent(DropEventComponent sourceComponent, DropEventComponent dragComponent) {
        this(sourceComponent, null, dragComponent, null);
    }
    
    public DropEvent(DropEventComponent sourceComponent, Object source, DropEventComponent dragComponent, Object dragObject) {        
        super(source == null ? sourceComponent : source);
        if (sourceComponent == null || dragComponent == null) throw new IllegalArgumentException("sourceComponent == null || dragComponent == null");
        this.sourceComponent = sourceComponent;
        this.dragObject = dragObject == null ? dragComponent : dragObject;
        this.dragComponent = dragComponent;
    }
    
    public DropEventComponent getSourceComponent() {
        return sourceComponent;
    }
    
    public Object getDragObject() {
        return dragObject;
    }
    
    public DropEventComponent getDragComponent() {
        return dragComponent;
    }
}
