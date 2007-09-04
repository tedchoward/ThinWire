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

import thinwire.render.Renderer;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.DropEvent;
import thinwire.ui.event.DropListener;
import thinwire.ui.event.KeyPressEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.event.KeyPressListener;
import thinwire.ui.style.*;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractComponent implements Component {
    //#IFDEF V1_1_COMPAT    
    private static final String COMPAT_MODE_PROP = Application.class.getName() + ".compatMode";
    static boolean isCompatModeOn() {
        String value = System.getProperty(COMPAT_MODE_PROP);
        return value != null && value.toUpperCase().equals("TRUE");        
    }
    
    //#ENDIF
    
    Application app;
    private Object parent;
    private Label label;
    private Style style;
    private EventListenerImpl<PropertyChangeListener> pcei;
    private EventListenerImpl<ActionListener> aei;
    private EventListenerImpl<DropListener> dei;
    private EventListenerImpl<KeyPressListener> kpei;
    private Object userObject;
    private boolean focusCapable = true;
    private boolean focus;
    private boolean enabled = true;
    private int x;
    private int y;
    private int width;
    private int height;
    private Object limit;
    private boolean visible;
    private boolean ignoreFirePropertyChange;
    
    AbstractComponent() {
        this(EventListenerImpl.ACTION_VALIDATOR);
    }
    
    AbstractComponent(EventListenerImpl.SubTypeValidator actionValidator) {
        this.visible = true;
        app = Application.current();
        
        EventListenerImpl<PropertyChangeListener> gpcei;
        EventListenerImpl<ActionListener> gaei;
        EventListenerImpl<DropListener> gdei;
        EventListenerImpl<KeyPressListener> gkpei;
        
        if (app == null) {
        	gpcei = null;
        	gaei = null;
        	gdei = null;
        	gkpei = null;
        } else {
        	gpcei = app.getGlobalListenerSet(PropertyChangeListener.class, false);
        	gaei = app.getGlobalListenerSet(ActionListener.class, false);
        	gdei = app.getGlobalListenerSet(DropListener.class, false);
        	gkpei = app.getGlobalListenerSet(KeyPressListener.class, false);
        }
        
        pcei = new EventListenerImpl<PropertyChangeListener>(this, PropertyChangeListener.class, null, gpcei);
        aei = new EventListenerImpl<ActionListener>(this, ActionListener.class, actionValidator, gaei);
        dei = new EventListenerImpl<DropListener>(this, DropListener.class, null, gdei);
        kpei = new EventListenerImpl<KeyPressListener>(this, KeyPressListener.class, EventListenerImpl.KEY_PRESS_VALIDATOR, gkpei);
            
        this.style = new Style(Application.getDefaultStyle(this.getClass()), this) {
            protected void firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                AbstractComponent.this.firePropertyChange(source, propertyName, oldValue, newValue);
            }
        };
    }
        
    void setRenderer(Renderer r) {
        pcei.setRenderer(r);
        aei.setRenderer(r);
        dei.setRenderer(r);
        kpei.setRenderer(r);                
    }
    
    String getStandardPropertyUnsupportedMsg(String propertyName, boolean read) {
        return this.getClass().getName() + " does not support " + (read ? "reading from" : "writing to") + " the property: " + propertyName;
    }
    //#IFDEF V1_1_COMPAT

    /**
     * Adds a specific PropertyChangeListener to the component dependent on this component's type.
     * This method is DEPRECATED as of v1.2 and should no longer be used because the original design
     * has the potential of causing performance issues.  The table below
     * outlines the details of this method so that you can craft the appropriate replacement.
     * NOTE: This method will throw an exception under all situations unless compat mode is on.
     * 
     * <table border="1">
     *     <tr><td>COMPONENT TYPE(S)</td>
     *         <td>LISTENS TO</td>
     *     </tr>
     *     <tr><td>{@link thinwire.ui.TextField}, 
     *             {@link thinwire.ui.DropDownGridBox},
     *             {@link thinwire.ui.TextArea}</td>
     *         <td>PROPERTY_FOCUS, PROPERTY_TEXT</td>
     *     </tr>
     *     <tr><td>{@link thinwire.ui.CheckBox},
     *             {@link thinwire.ui.RadioButton}</td>
     *         <td>PROPERTY_CHECKED</td>
     *     </tr>
     *     <tr><td>{@link thinwire.ui.GridBox}</td>
     *         <td>PROPERTY_SELECTED</td>
     *     </tr>
     *     <tr><td>{@link thinwire.ui.TabFolder}</td>
     *         <td>PROPERTY_CURRENT_INDEX</td>
     *     </tr>
     *     <tr><td>{@link thinwire.ui.Window}</td>
     *         <td>PROPERTY_VISIBLE</td>
     *     </tr>
     * </table>
     * @param listener the listener that will receive <code>PropertyChangeEvent</code> objects upon the property changing.
     * @throws IllegalStateException if compat mode is NOT on, or you invoke this method on a component not listed in the table above.
     * @throws IllegalArgumentException if <code>listener</code> is null.
     * @see #addPropertyChangeListener(String, PropertyChangeListener)
     * @deprecated for performance concerns.  Use {@link #addPropertyChangeListener(String, PropertyChangeListener)} instead.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!isCompatModeOn()) {
            throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use the addPropertyChangeListener(propertyName, listener) form instead");
        } else if (this instanceof TextField || this instanceof DropDownGridBox || this instanceof TextArea) {
            addPropertyChangeListener(new String[]{PROPERTY_FOCUS, EditorComponent.PROPERTY_TEXT}, listener);
        } else if (this instanceof CheckBox || this instanceof RadioButton) {
            addPropertyChangeListener(CheckedComponent.PROPERTY_CHECKED, listener);
        } else if (this instanceof GridBox) {
            addPropertyChangeListener(new String[]{GridBox.Row.PROPERTY_ROW_SELECTED, "selected"}, listener);
        } else if (this instanceof TabFolder) {
            addPropertyChangeListener(TabFolder.PROPERTY_CURRENT_INDEX, listener);
        } else if (this instanceof Window) {
            addPropertyChangeListener(PROPERTY_VISIBLE, listener);
        } else {
            throw new IllegalStateException("this method is deprecated as of v1.2; compat mode is on, but you are trying to listen to an unsupported component.  Use the addPropertyChangeListener(propertyName, listener) form instead.");
        }
    }
    //#ENDIF

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcei.addListener(propertyName, listener);
    }
    
    public void addPropertyChangeListener(String[] propertyNames, PropertyChangeListener listener) {
        pcei.addListener(propertyNames, listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcei.removeListener(listener);
    }

    protected final boolean firePropertyChange(Object source, String propertyName, int oldValue, int newValue) {
        if (oldValue == newValue) return false;
        if (ignoreFirePropertyChange) return true;
        pcei.firePropertyChange(source, propertyName, oldValue, newValue);            
        return true;
    }

    protected final boolean firePropertyChange(Object source, String propertyName, boolean oldValue, boolean newValue) {
        if (oldValue == newValue) return false;
        if (ignoreFirePropertyChange) return true;
        pcei.firePropertyChange(source, propertyName, oldValue, newValue);
        return true;
    }

    protected final boolean firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (oldValue == newValue || (oldValue != null && oldValue.equals(newValue)) || (newValue != null && newValue.equals(oldValue))) return false;
        if (ignoreFirePropertyChange) return true;
        pcei.firePropertyChange(source, propertyName, oldValue, newValue);        
        return true;
    }

    public void addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
    }
    
    public void addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
    }    
    
    public void removeActionListener(ActionListener listener) {
        aei.removeListener(listener);
    }

    public void fireAction(ActionEvent ev) {
        aei.fireAction(ev);
    }

    public void fireAction(String action) {
        fireAction(new ActionEvent(action, this));
    }
    
    public void fireAction(String action, Object source) {
        fireAction(new ActionEvent(action, this, source));
    }
    
    public void addDropListener(Component dragComponent, DropListener listener) {
        dei.addListener(dragComponent, listener);
    }
    
    public void addDropListener(Component[] dragComponents, DropListener listener) {
        dei.addListener(dragComponents, listener);
    }    
    
    public void removeDropListener(DropListener listener) {
        dei.removeListener(listener);
    }    

    public void fireDrop(DropEvent ev) {
        dei.fireDrop(ev);
    }
    
    public void fireDrop(Component dragComponent) {
        fireDrop(new DropEvent(this, dragComponent));
    }
    
    public void fireDrop(Component dragComponent, Object dragObject) {
        fireDrop(new DropEvent(this, null, dragComponent, dragObject));
    }
        
    public void addKeyPressListener(String keyPressCombo, KeyPressListener listener) {
        kpei.addListener(keyPressCombo, listener);
    }
    
    public void addKeyPressListener(String[] keyPressCombos, KeyPressListener listener) {
        kpei.addListener(keyPressCombos, listener);
    }
    
    public void removeKeyPressListener(KeyPressListener listener) {
        kpei.removeListener(listener);
    }
    
    public void fireKeyPress(KeyPressEvent ev) {
        kpei.fireKeyPress(ev);
    }
    
    public void fireKeyPress(String keyPressCombo) {
        fireKeyPress(new KeyPressEvent(keyPressCombo, this));
    }
    
    public Object getParent() {
        return parent;
    }
    
    void setParent(Object parent) {
        this.parent = parent;
    }    
    
    public Container getContainer() {
        Container c = null;        
        Object o = this.getParent();
        
        while (o != null) {
            if (o instanceof Container) {
                c = (Container)o;
                break;
            } else if (o instanceof Component) {
                o = ((Component)o).getParent();
            } else if (o instanceof GridBox.Row) {
                o = ((GridBox.Row)o).getParent();
            } else {
                throw new IllegalStateException("No known method of getting the parent for class '" + o.getClass() + "'");
            }
        }
        
        return c;
    }
    
    public Label getLabel() {
        return label;
    }    
    
    void setLabel(Label label) {
        this.label = label;
    }    
    
    public Object getUserObject() {
        return userObject;
    }
    
    public void setUserObject(Object userObject) {
        Object oldUserObject = this.userObject;
        this.userObject = userObject;
        firePropertyChange(this, PROPERTY_USER_OBJECT, oldUserObject, userObject);        
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        firePropertyChange(this, PROPERTY_ENABLED, oldEnabled, enabled);
    }

    public boolean isFocusCapable() {
        return focusCapable;
    }
    
    public void setFocusCapable(boolean focusCapable) {
        boolean oldFocusCapable = this.focusCapable;
        this.focusCapable = focusCapable;
        firePropertyChange(this, PROPERTY_FOCUS_CAPABLE, oldFocusCapable, focusCapable);
    }
       
    public final boolean isFocus() {
        return focus;
    }

    public void setFocus(boolean focus) {
        if (!this.isFocusCapable()) throw new IllegalStateException(this.getClass().getSimpleName() + ": !this.isFocusCapable()");
            
        if (parent instanceof Container || parent == null) {
            if (this.focus == focus) return;
            
            if (focus) {
            	
            	// Determine if the component is a new container (a container that has just been given focus by application code
            	//  or has a component thet has focus, but hasn't yet been rendered)
            	boolean newContainer = false;
            	if (this instanceof Container) {
            		Container cont = (Container) this;
            		Component cwf = cont.getChildWithFocus();
            		if (cwf != null) {
            			while (cwf != null && cwf instanceof Container) cwf = ((Container) cwf).getChildWithFocus();
        				if (cwf == null || !cwf.equals(cont.getComponentWithFocus())) newContainer = true;
            		} else {
            			newContainer = true;
            		}
            	}
            	
                if ((parent != null && (!(this instanceof Container) || newContainer)) || (this instanceof Dialog && newContainer)) {
                    Container container = parent != null ? (Container)parent : app.getFrame();                    
                    Component childWithFocus = container.getChildWithFocus();
                    
                    while (childWithFocus == null) {
                        container = (Container)container.getParent();
                        if (container == null) break;
                        childWithFocus = container.getChildWithFocus();
                    }

                    if (childWithFocus != null) childWithFocus.setFocus(false);
                }
                
    		    firePropertyChange(this, PROPERTY_FOCUS, this.focus, this.focus = true);
    		    
    		    if (parent != null || this instanceof Dialog) {
                    AbstractContainer container = parent != null ? (AbstractContainer)parent : app.getFrame();
                    container.setChildWithFocus(this);
                    if (app == null || !container.equals(app.getFrame())) container.setFocus(true);
    		    }
            } else {
            	app.setPriorFocus(this);
            	// We need to walk down the containment hierarchy nulling out the childWithFocus property
            	//  and setting each component's focus property to false, until we reach the final component
            	if (this instanceof Container) {
            		Component childWithFocus = ((Container)this).getChildWithFocus();                      
                    if (childWithFocus != null) {
                    	((AbstractContainer) this).setChildWithFocus(null);
                    	childWithFocus.setFocus(false);
                    }
            	} else if (parent != null) {
            		AbstractContainer container = (AbstractContainer)parent;
                    Component childWithFocus = container.getChildWithFocus();
                    if (this.equals(childWithFocus)) container.setChildWithFocus(null);
            	}
            	
            	firePropertyChange(this, PROPERTY_FOCUS, this.focus, this.focus = false);
            }
        } else
            throw new UnsupportedOperationException("the property 'focus' is not supported by " + this.getClass());
    }
    
    public Style getStyle() {
        return style;
    }
           
    private void rangeCheck(String propertyName, int value, int min, int max) {
        if (value < min || value >= max) throw new IllegalArgumentException(propertyName + " < " + min + " || " + propertyName + " >= " + max);
    }
    
    public int getX() {
        return x;
    }

    public void setX(int x) {
        rangeCheck(PROPERTY_X, x, Short.MIN_VALUE, Short.MAX_VALUE);
        int oldX = this.x;
        this.x = x;
        firePropertyChange(this, PROPERTY_X, oldX, x);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        rangeCheck(PROPERTY_Y, y, Short.MIN_VALUE, Short.MAX_VALUE);
        int oldY = this.y;
        this.y = y;
        firePropertyChange(this, PROPERTY_Y, oldY, y);
    }
        
    public Component setPosition(int x, int y) {
        rangeCheck(PROPERTY_X, x, Short.MIN_VALUE, Short.MAX_VALUE);
        rangeCheck(PROPERTY_Y, y, Short.MIN_VALUE, Short.MAX_VALUE);
        int oX = -1, oY = -1;
        boolean error = false;
        
        try {
            oX = getX();
            oY = getY();
            ignoreFirePropertyChange = true;
            setX(x);
            setY(y);
        } catch (RuntimeException e) {
            error = true;
            throw e;
        } finally {            
            if (error) {
                if (oX != -1 && oY != -1) {                    
                    setX(oX);
                    setY(oY);
                }
                
                ignoreFirePropertyChange = false;
            } else {
                ignoreFirePropertyChange = false;
                firePropertyChange(this, PROPERTY_X, oX, x);
                firePropertyChange(this, PROPERTY_Y, oY, y);                
            }
        }
        
        return this;
    }    

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        rangeCheck(PROPERTY_WIDTH, width, 0, Short.MAX_VALUE);
        int oldWidth = this.width;
        this.width = width;
        firePropertyChange(this, PROPERTY_WIDTH, oldWidth, width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        rangeCheck(PROPERTY_HEIGHT, height, 0, Short.MAX_VALUE);
        int oldHeight = this.height;
        this.height = height;
        firePropertyChange(this, PROPERTY_HEIGHT, oldHeight, height);
    }
    
    public Component setSize(int width, int height) {
        rangeCheck(PROPERTY_WIDTH, width, 0, Short.MAX_VALUE);
        rangeCheck(PROPERTY_HEIGHT, height, 0, Short.MAX_VALUE);
        int oWidth = -1, oHeight = -1;
        boolean error = false;
        
        try {
            oWidth = getWidth();
            oHeight = getHeight();
            ignoreFirePropertyChange = true;
            setWidth(width);
            setHeight(height);
        } catch (RuntimeException e) {
            error = true;
            throw e;
        } finally {            
            if (error) {
                if (oWidth != -1 && oHeight != -1) {                    
                    setWidth(oWidth);
                    setHeight(oHeight);
                }
                
                ignoreFirePropertyChange = false;
            } else {
                ignoreFirePropertyChange = false;
                firePropertyChange(this, PROPERTY_WIDTH, oWidth, width);
                firePropertyChange(this, PROPERTY_HEIGHT, oHeight, height);                
            }
        }
        
        return this;
    }
    
    public Component setBounds(int x, int y, int width, int height) {
        rangeCheck(PROPERTY_X, x, Short.MIN_VALUE, Short.MAX_VALUE);
        rangeCheck(PROPERTY_Y, y, Short.MIN_VALUE, Short.MAX_VALUE);
        rangeCheck(PROPERTY_WIDTH, width, 0, Short.MAX_VALUE);
        rangeCheck(PROPERTY_HEIGHT, height, 0, Short.MAX_VALUE);
        int oX = -1, oY = -1, oWidth = -1, oHeight = -1;
        boolean error = false;
        
        try {
            oX = getX();
            oY = getY();
            oWidth = getWidth();
            oHeight = getHeight();
            ignoreFirePropertyChange = true;
            setX(x);
            setY(y);
            setWidth(width);
            setHeight(height);
        } catch (RuntimeException e) {
            error = true;
            throw e;
        } finally {            
            if (error) {
                if (oX != -1 && oY != -1 && oWidth != -1 && oHeight != -1) {                    
                    setX(oX);
                    setY(oY);
                    setWidth(oWidth);
                    setHeight(oHeight);
                }
                
                ignoreFirePropertyChange = false;
            } else {
                ignoreFirePropertyChange = false;
                firePropertyChange(this, PROPERTY_X, oX, x);
                firePropertyChange(this, PROPERTY_Y, oY, y);                
                firePropertyChange(this, PROPERTY_WIDTH, oWidth, width);
                firePropertyChange(this, PROPERTY_HEIGHT, oHeight, height);                
            }
        }
        return this;
    }
    
    public Object getLimit() {
        return limit;
    }
    
    public Component setLimit(Object limit) {
        Object oldLimit = this.limit;
        this.limit = limit;
        firePropertyChange(this, PROPERTY_LIMIT, oldLimit, limit);
        return this;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        boolean oldVisible = this.visible;
        this.visible = visible;
        firePropertyChange(this, PROPERTY_VISIBLE, oldVisible, visible);
    }
}