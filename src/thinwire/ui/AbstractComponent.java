/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.ui;

import java.util.Map;
import java.util.HashMap;

import thinwire.render.Renderer;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.event.KeyPressEvent;
import thinwire.ui.event.KeyPressListener;
import thinwire.ui.style.*;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractComponent implements Component {
    private static final String COMPAT_MODE_PROP = Application.class.getName() + ".compatMode";
    static boolean isCompatModeOn() {
        String value = System.getProperty(COMPAT_MODE_PROP);
        return value != null && value.toUpperCase().equals("TRUE");        
    }
    
    private static Map<Class, Style> defaultStyles = new HashMap<Class, Style>();
    
    static {
        Style s = new Style();
        Font f = s.getFont(); 
        f.setFamily(Font.Family.SANS_SERIF);
        f.setColor(Color.WINDOWTEXT);
        f.setSize(8);
        f.setItalic(false);
        f.setBold(false);
        f.setUnderline(false);
        
        s.getBackground().setColor(Color.TRANSPARENT);
        
        Border b = s.getBorder();
        b.setType(Border.Type.NONE);
        b.setSize(0);
        b.setColor(Color.TRANSPARENT);
        
        setDefaultStyle(Component.class, s);
    }    
    
    static Style getDefaultStyle(Class clazz) {
        Style style;
        
        do {
            style = defaultStyles.get(clazz);
            clazz = clazz.getSuperclass();
        } while(style == null && clazz != null);
        
        return style;
    }
    
    static void setDefaultStyle(Class<? extends Component> clazz, Style s) {
        defaultStyles.put(clazz, s);
    }
    
    Application app;
    private Object parent;
    private Label label;
    private Style style;
    private EventListenerImpl<PropertyChangeListener> pcei;
    private EventListenerImpl<KeyPressListener> kpei;
    private Object userObject;
    private boolean focusCapable = true;
    private boolean focus;
    private boolean enabled = true;
    private int height;
    private int width;
    private int x;
    private int y;
    private boolean visible;
    private boolean ignoreFirePropertyChange;
    
    AbstractComponent() {
        this(true);
    }
    
    AbstractComponent(boolean visible) {
        this.visible = visible;
        app = Application.current();
        pcei = new EventListenerImpl<PropertyChangeListener>(app.getGloalPropertyChangeListenerImpl());
        kpei = new EventListenerImpl<KeyPressListener>();
        
        this.style = new Style(getDefaultStyle(this.getClass()), this) {
            protected void firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                AbstractComponent.this.firePropertyChange(source, propertyName, oldValue, newValue);
            }
        };
    }
        
    void setRenderer(Renderer r) {
        pcei.setRenderer(r);
        kpei.setRenderer(r);                
    }
    
    String getStandardPropertyUnsupportedMsg(String propertyName, boolean read) {
        return this.getClass().getName() + " does not support " + (read ? "reading from" : "writing to") + " the property: " + propertyName;
    }
    

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
    @Deprecated
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!isCompatModeOn()) {
            throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use the addPropertyChangeListener(propertyName, listener) form instead");
        } else if (this instanceof TextField || this instanceof DropDownGridBox || this instanceof TextArea) {
            addPropertyChangeListener(new String[]{PROPERTY_FOCUS, EditorComponent.PROPERTY_TEXT}, listener);
        } else if (this instanceof CheckBox || this instanceof RadioButton) {
            addPropertyChangeListener(CheckBox.PROPERTY_CHECKED, listener);
        } else if (this instanceof GridBox) {
            addPropertyChangeListener(GridBox.Row.PROPERTY_ROW_SELECTED, listener);
        } else if (this instanceof TabFolder) {
            addPropertyChangeListener(TabFolder.PROPERTY_CURRENT_INDEX, listener);
        } else if (this instanceof Window) {
            addPropertyChangeListener(PROPERTY_VISIBLE, listener);
        } else {
            throw new IllegalStateException("this method is deprecated as of v1.2; compat mode is on, but you are trying to listent to an unsupported component.  Use the addPropertyChangeListener(propertyName, listener) form instead.");
        }        
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcei.addListener(propertyName, listener);
    }
    
    public void addPropertyChangeListener(String[] propertyNames, PropertyChangeListener listener) {
        pcei.addListener(propertyNames, listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcei.removeListener(listener);
    }

    final boolean firePropertyChange(Object source, String propertyName, int oldValue, int newValue) {
        if (oldValue == newValue) return false;
        if (ignoreFirePropertyChange) return true;
        pcei.firePropertyChange(source, propertyName, oldValue, newValue);            
        return true;
    }

    final boolean firePropertyChange(Object source, String propertyName, boolean oldValue, boolean newValue) {
        if (oldValue == newValue) return false;
        if (ignoreFirePropertyChange) return true;
        pcei.firePropertyChange(source, propertyName, oldValue, newValue);
        return true;
    }

    final boolean firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (oldValue == newValue || (oldValue != null && oldValue.equals(newValue)) || (newValue != null && newValue.equals(oldValue))) return false;
        if (ignoreFirePropertyChange) return true;
        pcei.firePropertyChange(source, propertyName, oldValue, newValue);        
        return true;
    }
        
    public void addKeyPressListener(String keyPressCombo, KeyPressListener listener) {
        keyPressCombo = KeyPressEvent.normalizeKeyPressCombo(keyPressCombo);
        kpei.addListener(keyPressCombo, listener);
    }
    
    public void addKeyPressListener(String[] keyPressCombos, KeyPressListener listener) {
        if (keyPressCombos != null) {
            for (int i = keyPressCombos.length; --i >= 0;) {                
                if (keyPressCombos[i] != null) keyPressCombos[i] = KeyPressEvent.normalizeKeyPressCombo(keyPressCombos[i]);
            }
        }
        
        kpei.addListener(keyPressCombos, listener);
    }
    
    public void removeKeyPressListener(KeyPressListener listener) {
        kpei.removeListener(listener);
    }
    
    public void fireKeyPress(String keyPressCombo) {
        kpei.fireKeyPress(keyPressCombo);
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
            if (o instanceof AbstractContainer) {
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
        if (!this.isFocusCapable()) throw new IllegalStateException("!this.isFocusCapable()");
            
        if (parent instanceof AbstractContainer || parent == null) {
            if (this.focus == focus) return;
            
            if (focus) {
                if (parent != null) {
                    AbstractContainer container = (AbstractContainer)parent;                    
                    Component childWithFocus = container.getChildWithFocus();
                    
                    while (childWithFocus == null) {
                        container = (AbstractContainer)container.getParent();
                        if (container == null) break;
                        childWithFocus = container.getChildWithFocus();
                    }
                    
                    if (childWithFocus != null) childWithFocus.setFocus(false);
                }
                
    		    firePropertyChange(this, PROPERTY_FOCUS, this.focus, this.focus = true);
    		                    
    		    if (parent != null) {
                    AbstractContainer container = (AbstractContainer)parent;
                    container.setChildWithFocus(this);
                    container.setFocus(true);
    		    }
            } else {
                app.setPriorFocus(this);
                
                if (this instanceof AbstractContainer) {
                    Component childWithFocus = ((Container)this).getChildWithFocus();                      
                    if (childWithFocus != null) childWithFocus.setFocus(false);
                } else if (parent != null) {
                    AbstractContainer container = (AbstractContainer)parent;
                    Component childWithFocus = container.getChildWithFocus();
                    if (childWithFocus == this) container.setChildWithFocus(null);                    
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
        
    public void setPosition(int x, int y) {
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
    
    public void setSize(int width, int height) {
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
    }
    
    public void setBounds(int x, int y, int width, int height) {
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