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
    static final String SET_IMAGE = "setImage";
    static final String SET_ALIGN_X = "setAlignX";
    
    static final Logger log = Logger.getLogger(ComponentRenderer.class.getName()); 
    static final Pattern REGEX_DOUBLE_SLASH = Pattern.compile("\\\\"); 
    static final Pattern REGEX_DOUBLE_QUOTE = Pattern.compile("\"");
    static final Pattern REGEX_CRLF = Pattern.compile("\\r?\\n");                 
    private static final Object NO_VALUE = new Object();
    
    private Map<String, Object> ignoredProperties = new HashMap<String, Object>(3);
    private List<String> remoteFiles;
    private StringBuilder initProps = new StringBuilder();
    private Map<String, String> clientSideProps = new HashMap<String, String>();    
    String jsClass;
    Component comp;
    WindowRenderer wr;
    ContainerRenderer cr;
    Integer id;
    
    void init(String jsClass, WindowRenderer wr, Component comp, ComponentRenderer container) {
        this.jsClass = jsClass;
        this.wr = wr;
        this.cr = container instanceof ContainerRenderer ? (ContainerRenderer)container : null;
        this.comp = comp;
    }
    
    static String getSimpleClassName(Class type) {
        String text = type.getName();
        text = text.substring(text.lastIndexOf('.') + 1);
        return text;
    }    
    
	void render(WindowRenderer wr, Component comp, ComponentRenderer container) {
        id = wr.addComponentId(comp);
        if (this instanceof WebComponentListener) wr.ai.setWebComponentListener(id, (WebComponentListener)this);
        FX.Type visibleChange = comp.getStyle().getFX().getVisibleChange();
        addClientSideProperty(Component.PROPERTY_FOCUS);
        
        if (!isPropertyChangeIgnored(Component.PROPERTY_X)) addInitProperty(Component.PROPERTY_X, comp.getX());
        if (!isPropertyChangeIgnored(Component.PROPERTY_Y)) addInitProperty(Component.PROPERTY_Y, comp.getY());
        if (!isPropertyChangeIgnored(Component.PROPERTY_WIDTH)) addInitProperty(Component.PROPERTY_WIDTH, comp.getWidth());
        if (!isPropertyChangeIgnored(Component.PROPERTY_HEIGHT)) addInitProperty(Component.PROPERTY_HEIGHT, comp.getHeight());        
        if (!isPropertyChangeIgnored(Component.PROPERTY_VISIBLE)) addInitProperty(Component.PROPERTY_VISIBLE, 
                visibleChange != FX.Type.NONE && cr != null && cr.isFullyRendered() ? Boolean.FALSE : comp.isVisible());                
        if (!isPropertyChangeIgnored(Component.PROPERTY_ENABLED)) addInitProperty(Component.PROPERTY_ENABLED, comp.isEnabled());
        if (!comp.isFocusCapable()) addInitProperty(Component.PROPERTY_FOCUS_CAPABLE, false);         
        
        
        addInitProperty("styleClass", getSimpleClassName(comp.getClass()));
        StringBuilder styleProps = getInitialStyleInfo();
        if (styleProps.length() > 0) addInitProperty("styleProps", styleProps);
        
        if (comp.isFocus()) addInitProperty(Component.PROPERTY_FOCUS, true);

        Object parent = comp.getParent();
        if (parent instanceof Container) addInitProperty("insertAtIndex", ((Container)parent).getChildren().indexOf(comp));
        
        if (jsClass != null) {
            initProps.insert(0, '{');            
            initProps.setCharAt(initProps.length() - 1, '}');            
            wr.ai.clientSideFunctionCall("tw_newComponent", jsClass, id, 
                    cr == null ? (container == null ? 0 : container.id) : cr.id, 
                    initProps);
            initProps = null;
        }
        
        if (visibleChange == FX.Type.SMOOTH && !isPropertyChangeIgnored(Component.PROPERTY_VISIBLE) && comp.isVisible() && cr != null && cr.isFullyRendered())
            setPropertyWithEffect(Component.PROPERTY_VISIBLE, Boolean.TRUE, Boolean.FALSE, SET_VISIBLE, FX.PROPERTY_FX_VISIBLE_CHANGE);
        
        wr.ai.setPackagePrivateMember("renderer", comp, this);        
        
        if (comp.isFocusCapable() && ((Container)wr.comp).getComponentWithFocus() == null) comp.setFocus(true);
        
        wr.ai.flushRenderCallbacks(comp, id);        
	}

    private StringBuilder getInitialStyleInfo() {
        Style s = comp.getStyle();
        Style ds = wr.ai.getDefaultStyle(comp.getClass());

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Object value;
        Object defaultValue;
        
        value = s.getBackground().getColor();
        defaultValue = ds.getBackground().getColor();
        if (!value.equals(defaultValue)) sb.append("backgroundColor:\"").append(wr.ai.getColorValue((Color)value, false)).append("\",");

        value = s.getBorder().getType();
        defaultValue = ds.getBorder().getType();            
        
        if (!value.equals(defaultValue)) {
            Color borderColor = s.getBorder().getColor();
            
            if (value == Border.Type.NONE) {
                value = Border.Type.SOLID;
                borderColor = s.getBackground().getColor();
            } else if (borderColor.equals(ds.getBorder().getColor())) {
                borderColor = null;
            }

            sb.append("borderType:\"").append(value).append("\",");
            if (borderColor != null) sb.append("borderColor:\"").append(wr.ai.getColorValue((Color)borderColor, true)).append("\",");
        } else {
            value = s.getBorder().getColor();
            defaultValue = ds.getBorder().getColor();
            if (!value.equals(defaultValue)) sb.append("borderColor:\"").append(wr.ai.getColorValue((Color)value, true)).append("\",");
        }
        
        value = s.getBorder().getSize();
        defaultValue = ds.getBorder().getSize(); 
        if (!value.equals(defaultValue)) sb.append("borderSize:").append(value).append(",");
        
        value = s.getFont().getFamily();
        defaultValue = ds.getFont().getFamily(); 
        if (!value.equals(defaultValue)) sb.append("fontFamily:\"").append(value).append("\",");
            
        value = s.getFont().getSize();
        defaultValue = ds.getFont().getSize(); 
        if (!value.equals(defaultValue)) sb.append("fontSize:").append(value).append(",");
            
        value = s.getFont().getColor();
        defaultValue = ds.getFont().getColor(); 
        if (!value.equals(defaultValue)) sb.append("fontColor:\"").append(wr.ai.getColorValue((Color)value, false)).append("\",");
            
        value = s.getFont().isBold();
        defaultValue = ds.getFont().isBold(); 
        if (!value.equals(defaultValue)) sb.append("fontBold:").append(value).append(","); 
            
        value = s.getFont().isItalic();
        defaultValue = ds.getFont().isItalic(); 
        if (!value.equals(defaultValue)) sb.append("fontItalic:").append(value).append(","); 
            
        value = s.getFont().isUnderline();
        defaultValue = ds.getFont().isUnderline(); 
        if (!value.equals(defaultValue)) sb.append("fontUnderline:").append(value).append(",");
        
        if (sb.length() > 1) {
            sb.setCharAt(sb.length() - 1, '}');
        } else {
            sb.setLength(0);
        }
        
        return sb;
    }
    
    private void setStyle(String propertyName, Object oldValue) {
        if (propertyName.startsWith("fx") || isPropertyChangeIgnored(propertyName)) return;
        Style s = comp.getStyle();
        Style ds = wr.ai.getDefaultStyle(comp.getClass());
        Object value;
        
        if (propertyName.equals(Background.PROPERTY_BACKGROUND_COLOR)) {
            value = s.getBackground().getColor();
        } else if (propertyName.equals(Border.PROPERTY_BORDER_COLOR)) {
            if (s.getBorder().getType() == Border.Type.NONE) return;
            value = s.getBorder().getColor();
        } else if (propertyName.equals(Border.PROPERTY_BORDER_SIZE)) {
            value = s.getBorder().getSize();
        } else if (propertyName.equals(Border.PROPERTY_BORDER_TYPE)) {            
            value = s.getBorder().getType();
        } else if (propertyName.equals(Font.PROPERTY_FONT_FAMILY)) {
            value = s.getFont().getFamily();
        } else if (propertyName.equals(Font.PROPERTY_FONT_SIZE)) {
            value = s.getFont().getSize();
        } else if (propertyName.equals(Font.PROPERTY_FONT_COLOR)) {
            value = s.getFont().getColor();
        } else if (propertyName.equals(Font.PROPERTY_FONT_BOLD)) {
            value = s.getFont().isBold();
        } else if (propertyName.equals(Font.PROPERTY_FONT_ITALIC)) {
            value = s.getFont().isItalic();
        } else if (propertyName.equals(Font.PROPERTY_FONT_UNDERLINE)) {
            value = s.getFont().isUnderline();
        } else {
            throw new IllegalArgumentException("unknown property '" + propertyName + "'");
        }
        
        if (value instanceof Color) {
            value = wr.ai.getColorValue((Color)value, propertyName.equals(Border.PROPERTY_BORDER_COLOR));
        } else if (value instanceof Border.Type) {
            if (value == Border.Type.NONE) {
                value = Border.Type.SOLID;
                postClientEvent(SET_STYLE, Border.PROPERTY_BORDER_COLOR, wr.ai.getColorValue(s.getBackground().getColor(), true));
            } else if (oldValue == Border.Type.NONE) {
                postClientEvent(SET_STYLE, Border.PROPERTY_BORDER_COLOR, wr.ai.getColorValue(s.getBorder().getColor(), true));
            }
        } else {
            value = value.toString();
        }

        postClientEvent(SET_STYLE, propertyName, value);
    }
        
    void destroy() {
        wr.ai.setPackagePrivateMember("renderer", comp, null);
        wr.ai.setWebComponentListener(id, null);
        comp.removePropertyChangeListener(this);
        wr.removeComponentId(comp);
        ignoredProperties.clear();
        comp = null;
        wr = null;
        id = null;
        initProps = new StringBuilder();
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
    
    void addClientSideProperty(String name) {
        this.clientSideProps.put(name, name);
    }

    void addClientSideProperty(String name, String clientName) {
        this.clientSideProps.put(name, clientName);
    }

    public void eventSubTypeListenerInit(Class<? extends EventListener> clazz, Set<String> subTypes) {
        for (String subType : subTypes) {
            eventSubTypeListenerAdded(clazz, subType);
        }
    }
    
    public void eventSubTypeListenerAdded(Class<? extends EventListener> clazz, String subType) {
        if (PropertyChangeListener.class.isAssignableFrom(clazz)) {
            String prop = clientSideProps.get(subType);

            if (prop != null) {
                if (!prop.equals(subType)) {
                    String count = clientSideProps.get(prop);
                    
                    if (count == null) {                        
                        clientSideProps.put(prop, "1");
                        postClientEvent(REGISTER_EVENT_NOTIFIER, "propertyChange", prop);
                    } else {
                        clientSideProps.put(prop, String.valueOf(Integer.parseInt(count) + 1));
                    }
                } else {                    
                    postClientEvent(REGISTER_EVENT_NOTIFIER, "propertyChange", prop);
                }
            }
        } else if (ActionListener.class.isAssignableFrom(clazz)) {
            postClientEvent(REGISTER_EVENT_NOTIFIER, "action", subType);
        } else if (KeyPressListener.class.isAssignableFrom(clazz)) {
            postClientEvent(REGISTER_EVENT_NOTIFIER, "keyPress", subType);
        }
    }
    
    public void eventSubTypeListenerRemoved(Class<? extends EventListener> clazz, String subType) {
        if (PropertyChangeListener.class.isAssignableFrom(clazz)) {
            String prop = clientSideProps.get(subType);

            if (prop != null) {
                if (!prop.equals(subType)) {
                    int cnt = Integer.parseInt(clientSideProps.get(prop));
                    
                    if (cnt == 1) {               
                        clientSideProps.remove(prop);
                        postClientEvent(UNREGISTER_EVENT_NOTIFIER, "propertyChange", prop);
                    } else {
                        clientSideProps.put(prop, String.valueOf(cnt - 1));
                    }
                } else {                    
                    postClientEvent(UNREGISTER_EVENT_NOTIFIER, "propertyChange", prop);
                }
            }            
        } else if (ActionListener.class.isAssignableFrom(clazz)) {
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

    void setPropertyWithEffect(String propertyName, Object newValue, Object oldValue, String standardMethod, String styleProp) {
        FX.Type type;
        FX fx = comp.getStyle().getFX();
        
        if (styleProp.equals(FX.PROPERTY_FX_VISIBLE_CHANGE)) {
            type = fx.getVisibleChange();
        } else if (styleProp.equals(FX.PROPERTY_FX_POSITION_CHANGE)) {
            type = fx.getPositionChange();
        } else {
            type = fx.getSizeChange();
        }
        
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
            
            wr.ai.clientSideMethodCall(id, SET_PROPERTY_WITH_EFFECT, propertyName, newValue, unitSize, time);
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
            if (source instanceof Background || source instanceof Font || source instanceof Border) setStyle(pce.getPropertyName(), pce.getOldValue());
        }
    }

    final void postClientEvent(String methodName, Object... args) {
        wr.ai.clientSideMethodCall(id, methodName, args);
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
    
    final String getQualifiedURL(String location) {        
        if (location.trim().length() > 0) {
            URI uri;

            try {        
                uri = new URI(location);
            } catch (URISyntaxException e) {
                uri = null;
            }
                    
            if (uri == null || uri.getScheme() == null) uri = wr.ai.getRelativeFile(location).toURI();
            
            String scheme = uri.getScheme();        
            
            if (scheme.equals("file") || scheme.equals("class")) {
                if (!scheme.equals("class")) location = wr.ai.getRelativeFile(location).getAbsolutePath();
                if (remoteFiles == null) remoteFiles = new ArrayList<String>(5);
                remoteFiles.add(location);
                location = "%SYSROOT%" + RemoteFileMap.INSTANCE.add(location);
            } else {
                location = uri.toString();
            }
        } else {
            location = "";
        }

        return location;
    }    
}
