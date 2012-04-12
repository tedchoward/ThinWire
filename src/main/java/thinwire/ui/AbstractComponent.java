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
abstract class AbstractComponent<C extends Component> implements Component {
    //#IFDEF V1_1_COMPAT    
    private static final String COMPAT_MODE_PROP = Application.class.getName() + ".compatMode";
    static boolean isCompatModeOn() {
        String value = System.getProperty(COMPAT_MODE_PROP);
        return value != null && value.toUpperCase().equals("TRUE");        
    }
    
    //#ENDIF
    
    Application app;
    private Object parent;
    private LabelComponent label;
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

    @SuppressWarnings("unchecked")
	public C addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcei.addListener(propertyName, listener);
        return (C)this;
    }
    
    @SuppressWarnings("unchecked")
	public C addPropertyChangeListener(String[] propertyNames, PropertyChangeListener listener) {
        pcei.addListener(propertyNames, listener);
        return (C)this;
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

    @SuppressWarnings("unchecked")
	public C addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
        return (C)this;
    }
    
    @SuppressWarnings("unchecked")
	public C addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
        return (C)this;
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
    
    @SuppressWarnings("unchecked")
	public C addDropListener(Component dragComponent, DropListener listener) {
        dei.addListener(dragComponent, listener);
        return (C)this;
    }
    
    @SuppressWarnings("unchecked")
	public C addDropListener(Component[] dragComponents, DropListener listener) {
        dei.addListener(dragComponents, listener);
        return (C)this;
    }    
    
    public void removeDropListener(DropListener listener) {
        dei.removeListener(listener);
    }    

    public void fireDrop(DropEvent ev) {
        dei.fireDrop(ev);
    }
    
    @SuppressWarnings("unchecked")
	public void fireDrop(Component dragComponent) {
        fireDrop(new DropEvent(this, dragComponent));
    }
    
    @SuppressWarnings("unchecked")
	public void fireDrop(Component dragComponent, Object dragObject) {
        fireDrop(new DropEvent(this, null, dragComponent, dragObject));
    }
        
    @SuppressWarnings("unchecked")
	public C addKeyPressListener(String keyPressCombo, KeyPressListener listener) {
        kpei.addListener(keyPressCombo, listener);
        return (C)this;
    }
    
    @SuppressWarnings("unchecked")
	public C addKeyPressListener(String[] keyPressCombos, KeyPressListener listener) {
        kpei.addListener(keyPressCombos, listener);
        return (C)this;
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
    
    @SuppressWarnings("unchecked")
	public Container<Component> getContainer() {
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
    
    public LabelComponent getLabel() {
        return label;
    }    
    
    void setLabel(LabelComponent label) {
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

    @SuppressWarnings("unchecked")
	public void setFocus(boolean focus) {
        if (!this.isFocusCapable()) throw new IllegalStateException(this.getClass().getSimpleName() + ": !this.isFocusCapable()");
            
        if (parent instanceof Container || parent == null) {
            if (this.focus == focus) return;
            
            if (focus) {
            	
            	// Determine if the component is a new container (a container that has just been given focus by application code
            	//  or has a component that has focus, but hasn't yet been rendered)
            	boolean newContainer = false;
            	if (this instanceof Container) {
            		Container cont = (Container) this;
            		Component cwf = cont.getChildWithFocus();
            		
            		while (cwf != null && cwf instanceof Container) cwf = ((Container) cwf).getChildWithFocus();
            		if (cwf == null || !cwf.equals(cont.getComponentWithFocus())) newContainer = true;
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
        
    @SuppressWarnings("unchecked")
	public C setPosition(int x, int y) {
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
        
        return (C)this;
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
    
    @SuppressWarnings("unchecked")
	public C setSize(int width, int height) {
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
        
        return (C)this;
    }
    
    @SuppressWarnings("unchecked")
	public C setBounds(int x, int y, int width, int height) {
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
        return (C)this;
    }
    
    public Object getLimit() {
        return limit;
    }
    
    @SuppressWarnings("unchecked")
	public C setLimit(Object limit) {
        Object oldLimit = this.limit;
        this.limit = limit;
        firePropertyChange(this, PROPERTY_LIMIT, oldLimit, limit);
        return (C)this;
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