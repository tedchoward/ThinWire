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
package thinwire.ui;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import thinwire.render.Renderer;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.KeyPressEvent;
import thinwire.ui.event.KeyPressListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

/**
 * @author Joshua J. Gertzen
 */
class EventListenerImpl<E extends EventListener> {
    private Map<E, Set<String>> specificListeners;
    private Renderer renderer;
            
    EventListenerImpl() {
        this(null);
    }

    EventListenerImpl(EventListenerImpl<E> copy) {
        if (copy != null && copy.hasListeners()) {
            for (Map.Entry<E, Set<String>> e : copy.specificListeners.entrySet()) {
                Set<String> subTypes = e.getValue();
                
                if (subTypes == null) {
                    this.addListener(e.getKey());
                } else {
                    E listener = e.getKey();
                    
                    for (String subType : subTypes) {
                        this.addListener(subType, listener);
                    }
                }
            }
        }
    }    
    
    private boolean hasListeners() {
        return specificListeners != null && specificListeners.size() > 0;
    }
    
    void setRenderer(Renderer r) {
        this.renderer = r;                                   
        
        if (specificListeners != null && renderer != null) {
            Set<String> allSubTypes = new HashSet<String>();
            Class<? extends EventListener> clazz = null;
            
            for (Map.Entry<E, Set<String>> e : specificListeners.entrySet()) {
                EventListener listener = e.getKey();
                
                //Do not process the renderer's listener
                if (listener != r) { 
                    if (clazz == null) clazz = listener.getClass();
                    Set<String> subTypes = e.getValue();
                    
                    if (subTypes == null) {
                        allSubTypes.clear();
                        break;
                    } else {
                        allSubTypes.addAll(subTypes);
                    }
                }
            }
            
            if (allSubTypes.size() > 0) renderer.eventSubTypeListenerInit(clazz, allSubTypes);
        }
    }
    
    void addListener(E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        addEventListener(null, listener);
    }
    
    void addListener(String eventSubType, E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if (eventSubType == null || eventSubType.length() == 0) throw new IllegalArgumentException("eventSubType == null || eventSubType.length() == 0");
        addEventListener(eventSubType, listener);
    }

    void addListener(String[] eventSubTypes, E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if (eventSubTypes == null || eventSubTypes.length == 0) throw new IllegalArgumentException("eventSubTypes == null || eventSubTypes.length == 0");

        for (int i = eventSubTypes.length; --i >= 0;) {
            if (eventSubTypes[i] == null || eventSubTypes[i].length() == 0) throw new IllegalArgumentException("eventSubTypes[" + i + "] == null || eventSubTypes[" + i + "].length() == 0");
        }
        
        addEventListener(eventSubTypes, listener);
    }
    
    private void addEventListener(Object eventSubType, E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");            
        if (specificListeners == null) specificListeners = new HashMap<E, Set<String>>(3);
        
        //If no sub type specified, then add non-specific listener
        if (eventSubType == null) {
            if (specificListeners.containsKey(listener)) {
                Set<String> subTypes = specificListeners.get(listener);                    
                if (subTypes != null) throw new IllegalArgumentException("the specified listener has already been added for specific event types:" + subTypes);
            } else {
                specificListeners.put(listener, null);
            }
        } else {
            Set<String> subTypes = specificListeners.get(listener);
            
            if (subTypes == null) {
                if (specificListeners.containsKey(listener)) throw new IllegalArgumentException("the specified listener has already been added without specific event sub types");                    
                subTypes = new HashSet<String>(2);                        
                specificListeners.put(listener, subTypes);
            }                
            
            if (eventSubType instanceof String[]) {
                for (String subType : (String[])eventSubType) {
                   subTypes.add(subType);
                   if (renderer != null) renderer.eventSubTypeListenerAdded(listener.getClass(), subType);
                }
            } else {
                subTypes.add((String)eventSubType);
                if (renderer != null) renderer.eventSubTypeListenerAdded(listener.getClass(), (String)eventSubType);
            }
        }
    }

    void removeListener(E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if (specificListeners == null) return;            
        if (renderer == null) {
            specificListeners.remove(listener);
        } else {
            outer: for (String subType : specificListeners.remove(listener)) {                
                //If there is another listener listening to the same sub type then we don't
                //want to tell the renderer that the subtype is no longer being listened to.
                for (Set<String> subTypes : specificListeners.values()) {
                    if (subTypes.contains(subType)) continue outer;  
                }
                
                renderer.eventSubTypeListenerRemoved(listener.getClass(), subType);
            }
        }
    }
    
    void firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if(!hasListeners() && renderer == null) return;
        PropertyChangeEvent pce = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
        if (renderer != null) renderer.propertyChange(pce);
        if (hasListeners()) fireEvent(pce, propertyName);        
    }
        
    void fireAction(Object source, String action) {
        if (action == null || !action.equals(ActionEventComponent.ACTION_CLICK)) throw new IllegalArgumentException("the specified action is not supported");        
        if (!hasListeners()) return;
        ActionEvent ae = new ActionEvent(source, action);
        fireEvent(ae, action);
    }
    
    void fireKeyPress(Object source, String keyPressCombo) {
        if (!hasListeners()) return;
        KeyPressEvent kpe = new KeyPressEvent(source, keyPressCombo);
        fireEvent(kpe, kpe.getKeyPressCombo());        
    }
        
    void fireItemChange(Object source, ItemChangeEvent.Type type, int rowIndex, int columnIndex, Object oldValue, Object newValue) {
        if (!hasListeners()) return;
        ItemChangeEvent ice = new ItemChangeEvent(source, type, new GridBox.CellPosition(rowIndex, columnIndex), oldValue, newValue);
        fireEvent(ice, null);
    }

    void fireItemChange(Object source, ItemChangeEvent.Type type, int index, Object oldValue, Object newValue) {
        if (!hasListeners()) return;
        ItemChangeEvent ice = new ItemChangeEvent(source, type, index, oldValue, newValue);            
        fireEvent(ice, null);
    }
                
    private void fireEvent(EventObject eo, String eventSubType) {
        List<EventListener> listeners = new ArrayList<EventListener>(specificListeners.size());
            
        for (Map.Entry<E, Set<String>> e : specificListeners.entrySet()) {
            Set<String> subTypes = e.getValue();
            
            if (subTypes == null) {
                listeners.add(e.getKey());
            } else {
                for (String subType : subTypes) {
                    if (subType.equals(eventSubType)) {
                        listeners.add(e.getKey());
                        break;
                    }
                }
            }
        }

        for (EventListener el : listeners) {
            try {
                if (eo instanceof PropertyChangeEvent)
                    ((PropertyChangeListener) el).propertyChange((PropertyChangeEvent) eo);
                else if (eo instanceof ItemChangeEvent)
                    ((ItemChangeListener) el).itemChange((ItemChangeEvent) eo);
                else if (eo instanceof ActionEvent)
                    ((ActionListener) el).actionPerformed((ActionEvent) eo);
                else if (eo instanceof KeyPressEvent)
                    ((KeyPressListener) el).keyPress((KeyPressEvent) eo);
                else {
                    if (el == null)
                        throw new IllegalStateException("EventListener is null"
                                + (eo == null ? "" : ", event object type is: " + eo.getClass().getName()));
                    else if (eo == null)
                        throw new IllegalStateException("EventObject is null for event listener " + el.getClass().getName());
                    else
                        throw new IllegalStateException("EventListener " + el.getClass().getName() + " is unsupported");
                }
            } catch (Throwable e) {
                Application app = Application.current();
                
                if (app == null) {
                    if (e instanceof RuntimeException) {                    
                        throw (RuntimeException)e;
                    } else {
                        throw new RuntimeException(e);
                    }
                } else {
                    app.reportException(app, e);
                }
            }
        }
    }
}