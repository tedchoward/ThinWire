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
package thinwire.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import thinwire.render.Renderer;
import thinwire.ui.event.*;

/**
 * @author Joshua J. Gertzen
 */
class EventListenerImpl<E extends EventListener> {
    private Map<E, Set<Object>> specificListeners;
    private Class<E> type;
    private Renderer renderer;
    private Component comp;
    private SubTypeValidator subTypeValidator;
            
    interface SubTypeValidator {
        public Object validate(Object subType);
    }
    
    static SubTypeValidator DEFAULT_VALIDATOR = new SubTypeValidator() {
        public Object validate(Object subType) {
            return subType != null && !subType.equals("") ? subType : null;
        }
    };
    
    static final EventListenerImpl.SubTypeValidator ACTION_VALIDATOR = new EventListenerImpl.SubTypeValidator() {
        public Object validate(Object subType) {
            return subType != null && (subType.equals(Component.ACTION_CLICK) || subType.equals(Component.ACTION_DOUBLE_CLICK)) ? subType : null;
        }
    };
    
    static final EventListenerImpl.SubTypeValidator KEY_PRESS_VALIDATOR = new EventListenerImpl.SubTypeValidator() {
        public Object validate(Object subType) {
            return KeyPressEvent.normalizeKeyPressCombo((String)subType);
        }
    };
    
    static Class getSourceType(Component comp) {
        Class clazz;
        
        if (comp instanceof GridBox) {
            clazz = GridBox.Range.class;
        } else if (comp instanceof DateBox) {
            clazz = Date.class;
        } else if (comp instanceof Tree) {
            clazz = Tree.Item.class;
        } else if (comp instanceof Menu) {
            clazz = Menu.Item.class;
        } else {
            clazz = comp.getClass();
        }
        
        return clazz;
    }
    
    EventListenerImpl(Component comp, Class<E> type) {
        this(comp, type, null, null);
    }

    EventListenerImpl(Component comp, Class<E> type, SubTypeValidator subTypeValidator) {
        this(comp, type, subTypeValidator, null);
    }

    EventListenerImpl(Component comp, Class<E> type, SubTypeValidator subTypeValidator, EventListenerImpl<E> copy) {
        this.subTypeValidator = subTypeValidator == null ? DEFAULT_VALIDATOR : subTypeValidator;
        this.type = type;
        
        if (copy != null && copy.hasListeners()) {
            for (Map.Entry<E, Set<Object>> e : copy.specificListeners.entrySet()) {
                Set<Object> subTypes = e.getValue();
                
                if (subTypes == null) {
                    this.addListener(e.getKey());
                } else {
                    E listener = e.getKey();
                    
                    for (Object subType : subTypes) {
                        this.addListener(subType, listener);
                    }
                }
            }
        }

        this.comp = comp;
    }    
    
    private boolean hasListeners() {
        return specificListeners != null && specificListeners.size() > 0;
    }
    
    void setRenderer(Renderer r) {
        this.renderer = r;                                   
        
        if (specificListeners != null && renderer != null) {
            Set<Object> allSubTypes = new HashSet<Object>();
            
            for (Map.Entry<E, Set<Object>> e : specificListeners.entrySet()) {
                EventListener listener = e.getKey();
                
                //Do not process the renderer's listener
                if (listener != r) { 
                    Set<Object> subTypes = e.getValue();
                    
                    if (subTypes == null) {
                        allSubTypes.clear();
                        break;
                    } else {
                        allSubTypes.addAll(subTypes);
                    }
                }
            }
            
            if (allSubTypes.size() > 0) renderer.eventSubTypeListenerInit(type, allSubTypes);
        }
    }
    
    void addListener(E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        addEventListener(null, listener);
    }
    
    void addListener(Object eventSubType, E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if ((eventSubType = subTypeValidator.validate(eventSubType)) == null) throw new IllegalArgumentException("eventSubType is not valid:" + eventSubType);
        addEventListener(eventSubType, listener);
    }

    void addListener(Object[] eventSubTypes, E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if (eventSubTypes == null || eventSubTypes.length == 0) throw new IllegalArgumentException("eventSubTypes == null || eventSubTypes.length == 0");

        for (int i = eventSubTypes.length; --i >= 0;) {
            if ((eventSubTypes[i] = subTypeValidator.validate(eventSubTypes[i])) == null) throw new IllegalArgumentException("eventSubType[" + i + "] is not valid:" + eventSubTypes[i]);
        }
        
        addEventListener(eventSubTypes, listener);
    }
    
    private void addEventListener(Object eventSubType, E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");            
        if (specificListeners == null) specificListeners = new HashMap<E, Set<Object>>(3);
        
        //If no sub type specified, then add non-specific listener
        if (eventSubType == null) {
            if (specificListeners.containsKey(listener)) {
                Set<Object> subTypes = specificListeners.get(listener);                    
                if (subTypes != null) throw new IllegalArgumentException("the specified listener has already been added for specific event types:" + subTypes);
            } else {
                specificListeners.put(listener, null);
            }
        } else {
            Set<Object> subTypes = specificListeners.get(listener);
            
            if (subTypes == null) {
                if (specificListeners.containsKey(listener)) throw new IllegalArgumentException("the specified listener has already been added without specific event sub types");                    
                subTypes = new HashSet<Object>(2);                        
                specificListeners.put(listener, subTypes);
            }                
            
            if (eventSubType instanceof Object[]) {
                for (Object subType : (Object[])eventSubType) {
                   subTypes.add(subType);
                   if (renderer != null) renderer.eventSubTypeListenerAdded(type, subType);
                }
            } else {
                subTypes.add(eventSubType);
                if (renderer != null) renderer.eventSubTypeListenerAdded(type, eventSubType);
            }
        }
    }

    void removeListener(E listener) {
        if (listener == null) throw new IllegalArgumentException("listener == null");
        if (specificListeners == null) return;            
        if (renderer == null) {
            specificListeners.remove(listener);
        } else {
            outer: for (Object subType : specificListeners.remove(listener)) {                
                //If there is another listener listening to the same sub type then we don't
                //want to tell the renderer that the subtype is no longer being listened to.
                for (Set<Object> subTypes : specificListeners.values()) {
                    if (subTypes.contains(subType)) continue outer;  
                }
                
                renderer.eventSubTypeListenerRemoved(type, subType);
            }
        }
    }
    
    void firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if(!hasListeners() && renderer == null) return;
        PropertyChangeEvent pce = new PropertyChangeEvent(propertyName, oldValue, newValue, comp, source);
        if (renderer != null) renderer.propertyChange(pce);
        if (hasListeners()) fireEvent(pce, propertyName);        
    }

    void fireAction(ActionEvent ev) {
        if (ev == null) throw new IllegalArgumentException("ev == null");
        if (comp != ev.getSourceComponent()) throw new IllegalArgumentException("this != ev.getSourceComponent()");
        Class sourceType = getSourceType(ev.getSourceComponent());
        
        if (sourceType == null) {
            if (ev.getSource() != ev.getSourceComponent()) throw new IllegalArgumentException("ev.getSource() != ev.getSourceComponent()");
        } else {
            if (!(sourceType.isInstance(ev.getSource()))) throw new IllegalArgumentException("!(ev.getSource() instanceof " + sourceType.getName() + ")");
        }
        
        if (!hasListeners()) return;
        fireEvent(ev, ev.getAction());
    }
    
    void fireDrop(DropEvent ev) {
        if (ev == null) throw new IllegalArgumentException("ev == null");
        if (comp != ev.getSourceComponent()) throw new IllegalArgumentException("this != ev.getSourceComponent()");
        Class sourceType = getSourceType(ev.getSourceComponent());
        
        if (sourceType == null) {
            if (ev.getSource() != ev.getSourceComponent()) throw new IllegalArgumentException("ev.getSource() != ev.getSourceComponent()");
        } else {
            if (!(sourceType.isInstance(ev.getSource())) && ev.getSource() != ev.getSourceComponent()) throw new IllegalArgumentException("!(ev.getSource() instanceof " + sourceType.getName() + ") && ev.getSource() != ev.getSourceComponent()");
        }

        Class dragType = getSourceType(ev.getDragComponent());
        
        if (dragType == null) {
            if (ev.getDragObject() != ev.getDragComponent()) throw new IllegalArgumentException("ev.getDragObject() != ev.getDragComponent()");
        } else {
            if (!(dragType.isInstance(ev.getDragObject())) && ev.getSource() != ev.getSourceComponent()) throw new IllegalArgumentException("!(ev.getDragObject() instanceof " + dragType.getName() + ") && ev.getSource() != ev.getSourceComponent()");
        }
        
        if (!hasListeners()) return;
        fireEvent(ev, ev.getDragComponent());
    }
    
    void fireKeyPress(KeyPressEvent kpe) {
        if (!hasListeners()) return;
        fireEvent(kpe, kpe.getKeyPressCombo());        
    }
    
    void fireItemChange(ItemChangeEvent.Type type, int columnIndex, int rowIndex, Object oldValue, Object newValue) {
        if (!hasListeners()) return;
        ItemChangeEvent ice = new ItemChangeEvent((GridBox)comp, type, new GridBox.Range((GridBox)comp, columnIndex, rowIndex), oldValue, newValue);
        fireEvent(ice, null);
    }

    void fireItemChange(HierarchyComponent.Item source, ItemChangeEvent.Type type, int index, Object oldValue, Object newValue) {
        if (!hasListeners()) return;
        ItemChangeEvent ice = new ItemChangeEvent((ItemChangeEventComponent)comp, source, type, index, oldValue, newValue);            
        fireEvent(ice, null);
    }
                
    private void fireEvent(EventObject eo, Object eventSubType) {
        List<EventListener> listeners = new ArrayList<EventListener>(specificListeners.size());
            
        for (Map.Entry<E, Set<Object>> e : specificListeners.entrySet()) {
            Set<Object> subTypes = e.getValue();
            
            if (subTypes == null) {
                listeners.add(e.getKey());
            } else {
                for (Object subType : subTypes) {
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
                else if (eo instanceof DropEvent)
                    ((DropListener) el).dropPerformed((DropEvent) eo);
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