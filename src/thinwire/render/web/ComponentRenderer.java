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
package thinwire.render.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.logging.Level;

import thinwire.render.Renderer;
import thinwire.ui.event.*;
import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.MaskEditorComponent;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.style.*;

/**
 * @author Joshua J. Gertzen
 */
abstract class ComponentRenderer implements Renderer, WebComponentListener  {
    static final String SET_STYLE = "setStyle";
    static final String REGISTER_EVENT_NOTIFIER = "registerEventNotifier";
    static final String UNREGISTER_EVENT_NOTIFIER = "unregisterEventNotifier";
    static final String SET_ENABLED = "setEnabled";
    static final String SET_FOCUS = "setFocus";
    static final String SET_FOCUS_CAPABLE = "setFocusCapable";
    static final String SET_X = "setX";
    static final String SET_Y = "setY";
    static final String SET_WIDTH = "setWidth";
    static final String SET_HEIGHT = "setHeight";
    static final String SET_VISIBLE = "setVisible";
    static final String SET_PROPERTY_WITH_EFFECT = "setPropertyWithEffect";

    //Shared by other renderers
    static final String DESTROY = "destroy";
    static final String SET_TEXT = "setText";
    static final String SET_EDIT_MASK = "setEditMask";    
    static final String SET_SELECTION_RANGE = "setSelectionRange";
    static final String SET_IMAGE = "setImage";
    static final String SET_ALIGN_X = "setAlignX";
    
    static final Logger log = Logger.getLogger(ComponentRenderer.class.getName()); 
    static final Pattern REGEX_DOUBLE_SLASH = Pattern.compile("\\\\"); 
    static final Pattern REGEX_DOUBLE_QUOTE = Pattern.compile("\"");
    static final Pattern REGEX_CRLF = Pattern.compile("\\r?\\n");                 
    private static final Object NO_VALUE = new Object();
    
    private Map<String, Object> ignoredProperties = new HashMap<String, Object>(3);
    private List<String> remoteFiles;
    private Object[] quickArgs3;
    private Object[] quickArgs2;
    private Object[] quickArgs1;
    private StringBuffer initProps = new StringBuffer();
    String jsClass;
    Component comp;
    WindowRenderer wr;
    ContainerRenderer cr;
    Integer id;
    
	void render(WindowRenderer wr, Component comp, ComponentRenderer container) {
        this.wr = wr;
        this.cr = container instanceof ContainerRenderer ? (ContainerRenderer)container : null;
        this.comp = comp;        
        id = wr.addComponentId(comp);
        if (this instanceof WebComponentListener) wr.ai.setWebComponentListener(id, (WebComponentListener)this);
        FX.Type visibleChange = comp.getStyle().getFX().getVisibleChange();
        
        if (!isPropertyChangeIgnored(Component.PROPERTY_X)) addInitProperty(Component.PROPERTY_X, comp.getX());
        if (!isPropertyChangeIgnored(Component.PROPERTY_Y)) addInitProperty(Component.PROPERTY_Y, comp.getY());
        if (!isPropertyChangeIgnored(Component.PROPERTY_WIDTH)) addInitProperty(Component.PROPERTY_WIDTH, comp.getWidth());
        if (!isPropertyChangeIgnored(Component.PROPERTY_HEIGHT)) addInitProperty(Component.PROPERTY_HEIGHT, comp.getHeight());
        if (!isPropertyChangeIgnored(Component.PROPERTY_VISIBLE)) addInitProperty(Component.PROPERTY_VISIBLE, visibleChange == FX.Type.NONE ? comp.isVisible() : Boolean.FALSE);
        if (!isPropertyChangeIgnored(Component.PROPERTY_ENABLED)) addInitProperty(Component.PROPERTY_ENABLED, comp.isEnabled());
        if (!comp.isFocusCapable()) addInitProperty(Component.PROPERTY_FOCUS_CAPABLE, false);         
        if (comp.isFocus()) addInitProperty(Component.PROPERTY_FOCUS, true);
                
        if (jsClass != null) {            
            initProps.insert(0, '{');            
            initProps.setCharAt(initProps.length() - 1, '}');            
            Object[] args = new Object[] {jsClass, id,
                    cr == null ? (container == null ? 0 : container.id) : cr.id,
                    initProps};
            initProps = null;
            wr.ai.callClientFunction(false, "tw_newComponent", args);
        }
        
        setStyle(Background.PROPERTY_BACKGROUND_COLOR, true);
        setStyle(Border.PROPERTY_BORDER_COLOR, true);
        setStyle(Border.PROPERTY_BORDER_SIZE, true);
        setStyle(Border.PROPERTY_BORDER_TYPE, true);
        setStyle(Font.PROPERTY_FONT_FAMILY, true);
        setStyle(Font.PROPERTY_FONT_SIZE, true);
        setStyle(Font.PROPERTY_FONT_COLOR, true);
        setStyle(Font.PROPERTY_FONT_BOLD, true);
        setStyle(Font.PROPERTY_FONT_ITALIC, true);
        setStyle(Font.PROPERTY_FONT_UNDERLINE, true);
        if (visibleChange == FX.Type.SMOOTH && !isPropertyChangeIgnored(Component.PROPERTY_VISIBLE) && comp.isVisible()) setPropertyWithEffect(Component.PROPERTY_VISIBLE, Boolean.TRUE, Boolean.FALSE, SET_VISIBLE, FX.PROPERTY_FX_VISIBLE_CHANGE);        
        ((WebApplication)WebApplication.current()).setPackagePrivateMember("renderer", comp, this);

        if (comp.isFocusCapable() && ((Container)wr.comp).getComponentWithFocus() == null) comp.setFocus(true);
	}
    
    void setStyle(String propertyName, boolean isNotDefault) {
        Object value = comp.getStyle().getValue(propertyName);
        if (isNotDefault && value.equals(comp.getStyle().getDefaultValue(propertyName))) return;
        
        if (value instanceof Color) {
            value = ((Color)value).toRGBString();
        } else {
            value = value.toString();
        }

        postClientEvent(SET_STYLE, propertyName, value);
    }
        
    void destroy() {
        ((WebApplication)WebApplication.current()).setPackagePrivateMember("renderer", comp, null);
        wr.ai.setWebComponentListener(id, null);
        comp.removePropertyChangeListener(this);
        wr.removeComponentId(comp);
        ignoredProperties.clear();
        comp = null;
        wr = null;
        id = null;
        quickArgs3 = null;
        quickArgs2 = null;
        quickArgs1 = null;
        initProps = new StringBuffer();
        ignoredProperties = new HashMap<String, Object>(3);
        
        if (remoteFiles != null) {
            for (String s : remoteFiles) {
                try {
                    RemoteFileMap.INSTANCE.remove(s);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Local file no longer exists", e);
                }
            }
        }
        remoteFiles = null;
    }
    
    void addInitProperty(String name, Object value) {
        initProps.append(name).append(':');
        WebApplication.encodeObject(initProps, value);
        initProps.append(',');
    }

    public void eventSubTypeListenerInit(Class<? extends EventListener> clazz, Set<String> subTypes) {
        if (ActionListener.class.isAssignableFrom(clazz)) {
            for (String subType : subTypes) {
                postClientEvent(REGISTER_EVENT_NOTIFIER, "action", subType);
            }            
        } else if (KeyPressListener.class.isAssignableFrom(clazz)) {
            for (String subType : subTypes) {
                postClientEvent(REGISTER_EVENT_NOTIFIER, "keyPress", subType);
            }
        }
    }
    
    public void eventSubTypeListenerAdded(Class<? extends EventListener> clazz, String subType) {
        if (ActionListener.class.isAssignableFrom(clazz)) {
            postClientEvent(REGISTER_EVENT_NOTIFIER, "action", subType);
        } else if (KeyPressListener.class.isAssignableFrom(clazz)) {
            postClientEvent(REGISTER_EVENT_NOTIFIER, "keyPress", subType);
        }
    }
    
    public void eventSubTypeListenerRemoved(Class<? extends EventListener> clazz, String subType) {
        if (ActionListener.class.isAssignableFrom(clazz)) {
            postClientEvent(UNREGISTER_EVENT_NOTIFIER, "action", subType);            
        } else if (KeyPressListener.class.isAssignableFrom(clazz)) {
            postClientEvent(UNREGISTER_EVENT_NOTIFIER, "keyPress", subType);
        }
    }    
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        
        if (name.equals("size")) {
            this.setPropertyChangeIgnored(Component.PROPERTY_WIDTH, true);
            this.setPropertyChangeIgnored(Component.PROPERTY_HEIGHT, true);
            String[] args = ((String)event.getValue()).split(",");
            comp.setSize(Integer.valueOf(args[0]), Integer.valueOf(args[1]));                        
            this.setPropertyChangeIgnored(Component.PROPERTY_WIDTH, false);
            this.setPropertyChangeIgnored(Component.PROPERTY_HEIGHT, false);
        } else if (name.equals("position")) {
            this.setPropertyChangeIgnored(Component.PROPERTY_X, true);
            this.setPropertyChangeIgnored(Component.PROPERTY_Y, true);
            String[] args = ((String)event.getValue()).split(",");
            comp.setPosition(Integer.valueOf(args[0]), Integer.valueOf(args[1]));            
            this.setPropertyChangeIgnored(Component.PROPERTY_X, false);
            this.setPropertyChangeIgnored(Component.PROPERTY_Y, false);
        } else if (name.equals("bounds")) {
            this.setPropertyChangeIgnored(Component.PROPERTY_X, true);
            this.setPropertyChangeIgnored(Component.PROPERTY_Y, true);
            this.setPropertyChangeIgnored(Component.PROPERTY_WIDTH, true);
            this.setPropertyChangeIgnored(Component.PROPERTY_HEIGHT, true);
            String[] args = ((String)event.getValue()).split(",");            
            comp.setBounds(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]));            
            this.setPropertyChangeIgnored(Component.PROPERTY_WIDTH, false);
            this.setPropertyChangeIgnored(Component.PROPERTY_HEIGHT, false);
            this.setPropertyChangeIgnored(Component.PROPERTY_X, false);
            this.setPropertyChangeIgnored(Component.PROPERTY_Y, false);
        } else if (name.equals(Component.PROPERTY_VISIBLE)) {
            this.setPropertyChangeIgnored(Component.PROPERTY_VISIBLE, true);
            comp.setVisible(((String)event.getValue()).toLowerCase().equals("true"));
            this.setPropertyChangeIgnored(Component.PROPERTY_VISIBLE, false);
        } else if (name.equals(Component.PROPERTY_FOCUS)) {
            setPropertyChangeIgnored(Component.PROPERTY_FOCUS, true);
            
            ContainerRenderer cr = this.cr;
            
            while (cr != null) { 
                cr.setPropertyChangeIgnored(Component.PROPERTY_FOCUS, true);
                cr = cr.cr;
            }
            
            comp.setFocus(Boolean.valueOf((String)event.getValue()).booleanValue());

            cr = this.cr;
            
            while (cr != null) { 
                cr.setPropertyChangeIgnored(Component.PROPERTY_FOCUS, false);
                cr = cr.cr;
            }
            
            setPropertyChangeIgnored(Component.PROPERTY_FOCUS, false);            
        } else if (name.equals("keyPress")) {
            comp.fireKeyPress((String)event.getValue());
        }
    }

    private void setPropertyWithEffect(String propertyName, Object newValue, Object oldValue, String standardMethod, String styleProp) {
        FX.Type type = (FX.Type)comp.getStyle().getValue(styleProp);
        
        if (type == FX.Type.SMOOTH) {
            int time;
            int unitSize;
            
            if (styleProp.equals(FX.PROPERTY_FX_VISIBLE_CHANGE)) {
                propertyName = "opacity";
                newValue = ((Boolean)newValue).booleanValue() ? 100 : 0;
                unitSize = 10;
                time = 250;
            } else {
                int dist = (Integer)newValue - (Integer)oldValue;
                if (dist < 0) dist = ~dist + 1;

                if (styleProp.equals(FX.PROPERTY_FX_POSITION_CHANGE)) {                
                    unitSize = 10;
                    time = dist;
                } else {
                    unitSize = 10;
                    time = dist;
                }
            }
            
            wr.ai.callClientFunction(false, id, SET_PROPERTY_WITH_EFFECT, new Object[] {propertyName, newValue, unitSize, time});
        } else {
            postClientEvent(standardMethod, newValue);
        }
    }    
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        
        if (name.equals(Component.PROPERTY_ENABLED)) {
            postClientEvent(SET_ENABLED, pce.getNewValue());
        } else if (name.equals(Component.PROPERTY_FOCUS)) {
            Object newValue = pce.getNewValue();
            if (((Boolean)newValue).booleanValue()) postClientEvent(SET_FOCUS, newValue);
        } else if (name.equals(Component.PROPERTY_FOCUS_CAPABLE)) {
            postClientEvent(SET_FOCUS_CAPABLE, pce.getNewValue());
        } else if (name.equals(Component.PROPERTY_X)) {
            setPropertyWithEffect(name, pce.getNewValue(), pce.getOldValue(), SET_X, FX.PROPERTY_FX_POSITION_CHANGE);
        } else if (name.equals(Component.PROPERTY_Y)) {
            setPropertyWithEffect(name, pce.getNewValue(), pce.getOldValue(), SET_Y, FX.PROPERTY_FX_POSITION_CHANGE);
        } else if (name.equals(Component.PROPERTY_WIDTH)) {
            setPropertyWithEffect(name, pce.getNewValue(), pce.getOldValue(), SET_WIDTH, FX.PROPERTY_FX_SIZE_CHANGE);
        } else if (name.equals(Component.PROPERTY_HEIGHT)) {
            setPropertyWithEffect(name, pce.getNewValue(), pce.getOldValue(), SET_HEIGHT, FX.PROPERTY_FX_SIZE_CHANGE);
        } else if (name.equals(Component.PROPERTY_VISIBLE)) {
            setPropertyWithEffect(name, pce.getNewValue(), pce.getOldValue(), SET_VISIBLE, FX.PROPERTY_FX_VISIBLE_CHANGE);
        } else {
            Object source = pce.getSource();            
            if (source instanceof Background || source instanceof Font || source instanceof Border) setStyle(pce.getPropertyName(), false);
        }
    }

    final void postClientEvent(String methodName, Object arg) {
        if (quickArgs1 == null) quickArgs1 = new Object[1];
        quickArgs1[0] = arg;
        wr.ai.callClientFunction(false, id, methodName, quickArgs1);
    }

    final void postClientEvent(String methodName, Object arg1, Object arg2) {
        if (quickArgs2 == null) quickArgs2 = new Object[2];
        quickArgs2[0] = arg1;
        quickArgs2[1] = arg2;
        wr.ai.callClientFunction(false, id, methodName, quickArgs2);
    }

    final void postClientEvent(String methodName, Object arg1, Object arg2, Object arg3) {
        if (quickArgs3 == null) quickArgs3 = new Object[3];
        quickArgs3[0] = arg1;
        quickArgs3[1] = arg2;
        quickArgs3[2] = arg3;
        wr.ai.callClientFunction(false, id, methodName, quickArgs3);
    }

    final void postClientEvent(String methodName, Object[] args) {
        wr.ai.callClientFunction(false, id, methodName, args);       
    }
    
    final void setPropertyChangeIgnored(String name, Object value, boolean ignore) {
        if (ignore) {            
            ignoredProperties.put(name, value);
        } else {
            ignoredProperties.remove(name);
        }
    }

    final void setPropertyChangeIgnored(String name, boolean ignore) {
        setPropertyChangeIgnored(name, NO_VALUE, ignore);
    }

    final boolean isPropertyChangeIgnored(String name, Object value) {
        if (ignoredProperties.containsKey(name)) {
            if (value == NO_VALUE) {
                return true;
            } else {
                Object ignoredValue = ignoredProperties.get(name);
                return ignoredValue == value || (ignoredValue != null && ignoredValue.equals(value));
            }
        } else {
            return false;
        }
    }
    
    final boolean isPropertyChangeIgnored(String name) {
        return isPropertyChangeIgnored(name, NO_VALUE);
    }
    
    final boolean resetPropertyChangeIgnored(String name, Object value) {
        boolean ret = isPropertyChangeIgnored(name, value);
        if (ret) ignoredProperties.remove(name);
        return ret;
    }

    final boolean resetPropertyChangeIgnored(String name) {
        return resetPropertyChangeIgnored(name, NO_VALUE);
    }

    static final String getEscapedText(String s) {
        s = REGEX_DOUBLE_SLASH.matcher(s).replaceAll("\\\\\\\\");        
        s = REGEX_DOUBLE_QUOTE.matcher(s).replaceAll("\\\\\"");
        s = REGEX_CRLF.matcher(s).replaceAll(" ");
        return s;
    }
    
    static final String getEditMaskTextLength(Component c) {
        if (c instanceof MaskEditorComponent) {
            MaskEditorComponent maskEditor = (MaskEditorComponent)c; 
            String editMask = maskEditor.getEditMask();        
            int maxLength = maskEditor.getMaxLength();
            if (editMask.equals("") && maxLength > 0) editMask = "<=" + maxLength;
            return editMask;
        } else {
            throw new IllegalArgumentException("(c instanceof Components.PropertyEditMask && c instanceof Components.PropertyMaxLength) == false");
        }
    }     
    
    final String getRemoteNameForLocalFile(String localName) {
        if (localName.trim().length() == 0) return "";
        if (!localName.startsWith("class:///")) localName = WebApplication.current().getRelativeFile(localName).getAbsolutePath();        
        String remoteName = RemoteFileMap.INSTANCE.add(localName);
        if (remoteFiles == null) remoteFiles = new ArrayList<String>(5);
        remoteFiles.add(localName);
        return remoteName;
    }
    
    final String getQualifiedURL(String location) {        
        if (location.trim().length() > 0) {
            URI uri;

            try {        
                uri = new URI(location);
            } catch (URISyntaxException e) {
                uri = null;
            }
                    
            if (uri == null || uri.getScheme() == null) uri = WebApplication.current().getRelativeFile(location).toURI();
            
            String scheme = uri.getScheme();        
            
            if (scheme.equals("file") || scheme.equals("class")) {            
                location = "%SYSROOT%" + getRemoteNameForLocalFile(location);            
            } else {
                location = uri.toString();
            }
        } else {
            location = "";
        }

        return location;
    }    
}
