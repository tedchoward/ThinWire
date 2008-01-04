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
package thinwire.util;

import java.util.*;

import thinwire.ui.Application;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.DropEvent;
import thinwire.ui.event.DropListener;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.KeyPressEvent;
import thinwire.ui.event.KeyPressListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

/**
 * @author Joshua J. Gertzen
 */
public abstract class EventBus<L extends EventListener, E extends EventObject, T> {
    private Map<L, Set<Object>> listeners;
    
    protected abstract void runListener(L el, E eo);
    protected abstract T validate(T subType);
    
    public void addListener(L listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        addEventListener(null, listener);
    }
    
    public void addListener(T type, L listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if ((type = validate(type)) == null) throw new IllegalArgumentException("eventSubType is not valid:" + type);
        addEventListener(type, listener);
    }

    public void addListener(T[] types, L listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if (types == null || types.length == 0) throw new IllegalArgumentException("eventSubTypes == null || eventSubTypes.length == 0");

        for (int i = types.length; --i >= 0;) {
            if ((types[i] = validate(types[i])) == null) throw new IllegalArgumentException("eventSubType[" + i + "] is not valid:" + types[i]);
        }
        
        addEventListener(types, listener);
    }
    
    private void addEventListener(Object eventSubType, L listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");            
        if (listeners == null) listeners = new HashMap<L, Set<Object>>(3);
        
        //If no sub type specified, then add non-specific listener
        if (eventSubType == null) {
            if (listeners.containsKey(listener)) {
                Set<Object> subTypes = listeners.get(listener);                    
                if (subTypes != null) throw new IllegalArgumentException("the specified listener has already been added for specific event types:" + subTypes);
            } else {
                listeners.put(listener, null);
            }
        } else {
            Set<Object> subTypes = listeners.get(listener);
            
            if (subTypes == null) {
                if (listeners.containsKey(listener)) throw new IllegalArgumentException("the specified listener has already been added without specific event sub types");                    
                subTypes = new HashSet<Object>(2);                        
                listeners.put(listener, subTypes);
            }                
            
            if (eventSubType instanceof Object[]) {
                for (Object subType : (Object[])eventSubType) {
                   subTypes.add(subType);
                }
            } else {
                subTypes.add(eventSubType);
            }
        }
    }

    public void removeListener(L listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if (listeners == null) return;            
        listeners.remove(listener);
    }
    
    protected void fireEvent(E eo, T eventSubType) {
    	if (listeners == null) return;
        List<L> l = new ArrayList<L>(listeners.size());
        
        for (Map.Entry<L, Set<Object>> e : listeners.entrySet()) {
            Set<Object> subTypes = e.getValue();
            
            if (subTypes == null) {
                l.add(e.getKey());
            } else {
                for (Object subType : subTypes) {
                    if (subType.equals(eventSubType)) {
                        l.add(e.getKey());
                        break;
                    }
                }
            }
        }

        for (L el : l) {
        	runListener(el, eo);
        }
    }
}
