/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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

import thinwire.render.RenderStateEvent;
import thinwire.render.RenderStateListener;
import thinwire.render.Renderer;
import thinwire.ui.event.*;
import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.DateBox;
import thinwire.ui.GridBox;
import thinwire.ui.HierarchyComponent;
import thinwire.ui.Menu;
import thinwire.ui.Tree;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.style.*;
import thinwire.ui.style.FX.Transition;

/**
 * @author Joshua J. Gertzen
 */
abstract class ComponentRenderer implements Renderer, WebComponentListener  {
    static final String SET_STYLE = "setStyle";
    static final String SET_STYLES = "setStyles";
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
    static final String SET_FX_OPACITY = "setOpacity";
    static final String SET_PROPERTY_WITH_EFFECT = "setPropertyWithEffect";

    //Shared by other renderers
    static final String DESTROY = "destroy";
    static final String SET_IMAGE = "setImage";
    static final String SET_ALIGN_X = "setAlignX";
    
    static final String CLIENT_EVENT_DROP = "drop";
    
    static final Logger log = Logger.getLogger(ComponentRenderer.class.getName()); 
    static final Pattern REGEX_DOUBLE_SLASH = Pattern.compile("\\\\"); 
    static final Pattern REGEX_DOUBLE_QUOTE = Pattern.compile("\"");
    static final Pattern REGEX_CRLF = Pattern.compile("\\r?\\n");         
    
    static final RichTextParser RICH_TEXT_PARSER = new RichTextParser();
    
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
        if (this instanceof WebComponentListener) wr.ai.setWebComponentListener(id, this);
        FX.Type visibleChange = comp.getStyle().getFX().getVisibleChange();
        addClientSideProperty(Component.PROPERTY_FOCUS);
        
        if (!isPropertyChangeIgnored(Component.PROPERTY_X)) addInitProperty(Component.PROPERTY_X, comp.getX());
        if (!isPropertyChangeIgnored(Component.PROPERTY_Y)) addInitProperty(Component.PROPERTY_Y, comp.getY());
        if (!isPropertyChangeIgnored(Component.PROPERTY_WIDTH)) addInitProperty(Component.PROPERTY_WIDTH, comp.getWidth());
        if (!isPropertyChangeIgnored(Component.PROPERTY_HEIGHT)) addInitProperty(Component.PROPERTY_HEIGHT, comp.getHeight());        
        if (!isPropertyChangeIgnored(FX.PROPERTY_FX_OPACITY) && comp.getStyle().getFX().getOpacity() != 100) addInitProperty("opacity", comp.getStyle().getFX().getOpacity());        
        if (!isPropertyChangeIgnored(Component.PROPERTY_VISIBLE)) addInitProperty(Component.PROPERTY_VISIBLE, 
                visibleChange != FX.Type.NONE && cr != null && cr.isFullyRendered() ? Boolean.FALSE : comp.isVisible());                
        if (!isPropertyChangeIgnored(Component.PROPERTY_ENABLED)) addInitProperty(Component.PROPERTY_ENABLED, comp.isEnabled());
        if (!comp.isFocusCapable()) addInitProperty(Component.PROPERTY_FOCUS_CAPABLE, false);         
        
        Style defaultStyle = wr.ai.getDefaultStyle(comp.getClass());
        addInitProperty("styleClass", wr.ai.styleToStyleClass.get(defaultStyle));
        StringBuilder styleProps = wr.ai.getStyleValues(this, new StringBuilder(), comp.getStyle(), defaultStyle);
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
    
    private void setStyle(String propertyName, Object oldValue) {
        if (propertyName.startsWith("fx") || isPropertyChangeIgnored(propertyName)) return;
        Style s = comp.getStyle();
        Object value;
        
        if (propertyName.equals(Background.PROPERTY_BACKGROUND_COLOR)) {
            value = s.getBackground().getColor();
        } else if (propertyName.equals(Background.PROPERTY_BACKGROUND_IMAGE)) {
            value = s.getBackground().getImage();
        } else if (propertyName.equals(Background.PROPERTY_BACKGROUND_REPEAT)) {
            value = s.getBackground().getRepeat();
        } else if (propertyName.equals(Background.PROPERTY_BACKGROUND_POSITION)) {
            value = s.getBackground().getPosition();
        } else if (propertyName.equals(Border.PROPERTY_BORDER_COLOR)) {
            if (s.getBorder().getType() == Border.Type.NONE) return;
            value = s.getBorder().getColor();
        } else if (propertyName.equals(Border.PROPERTY_BORDER_SIZE)) {
            value = s.getBorder().getSize();
        } else if (propertyName.equals(Border.PROPERTY_BORDER_TYPE)) {            
            value = s.getBorder().getType();
        } else if (propertyName.equals(Border.PROPERTY_BORDER_IMAGE)) {
            value = s.getBorder().getImageInfo();
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
        } else if (propertyName.equals(Font.PROPERTY_FONT_STRIKE)) {
            value = s.getFont().isUnderline();
        } else {
            throw new IllegalArgumentException("unknown property '" + propertyName + "'");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        
        if (value instanceof Border.Type) {
            if (value == Border.Type.NONE) {
                value = Border.Type.SOLID;
                wr.ai.getStyleValue(this, sb, Border.PROPERTY_BORDER_COLOR, s.getBackground().getColor());
            } else if (oldValue == Border.Type.NONE) {
                wr.ai.getStyleValue(this, sb, Border.PROPERTY_BORDER_COLOR, s.getBorder().getColor());
            }
        }
        
        wr.ai.getStyleValue(this, sb, propertyName, value);
        sb.setCharAt(sb.length() - 1, '}');
        postClientEvent(SET_STYLES, sb);
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

    public void eventSubTypeListenerInit(Class<? extends EventListener> clazz, Set<Object> subTypes) {
        for (Object subType : subTypes) {
            eventSubTypeListenerAdded(clazz, subType);
        }
    }
    
    public void eventSubTypeListenerAdded(Class<? extends EventListener> clazz, Object subType) {
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
        } else if (DropListener.class.isAssignableFrom(clazz)) {
            final Component dragComponent = (Component)subType;
            
            wr.ai.invokeAfterRendered(dragComponent, new RenderStateListener() {
                public void renderStateChange(RenderStateEvent ev) {
                    wr.ai.clientSideMethodCall(wr.ai.getComponentId(dragComponent), "addDragTarget", id);
                }
            });
        }
    }
    
    public void eventSubTypeListenerRemoved(Class<? extends EventListener> clazz, Object subType) {
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
        } else if (DropListener.class.isAssignableFrom(clazz)) {
            Integer dragComponentId = wr.ai.getComponentId((Component)subType);
            if (dragComponentId != null) wr.ai.clientSideMethodCall(dragComponentId, "removeDragTarget", id);
        }
    }    
    
    public int getInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        
        if (name.equals(Component.ACTION_CLICK) || name.equals(Component.ACTION_DOUBLE_CLICK)) {
            String actionIgnoreProperty;
            
            if (this.comp instanceof DateBox) {
                actionIgnoreProperty = DateBox.PROPERTY_SELECTED_DATE;
            } else if (this.comp instanceof GridBox) {
                actionIgnoreProperty = GridBox.Row.PROPERTY_ROW_SELECTED;
            } else if (comp instanceof Tree) {
                actionIgnoreProperty = Tree.Item.PROPERTY_ITEM_SELECTED;
            } else {
                actionIgnoreProperty = null;
            }
            
            if (actionIgnoreProperty != null) setPropertyChangeIgnored(actionIgnoreProperty, true);
            comp.fireAction(new ActionEvent(comp, getEventObject(comp, (String)event.getValue()), name));
            if (actionIgnoreProperty != null) setPropertyChangeIgnored(actionIgnoreProperty, false);
        } else if (event.getName().equals(CLIENT_EVENT_DROP)) {
            String[] parts = ((String)event.getValue()).split(",", -1);
            Component dragComponent = (Component)wr.ai.getComponentFromId(Integer.parseInt(parts[1]));
            int dragX = getInt(parts[3]);
            int dragY = getInt(parts[4]);
            int sourceX = getInt(parts[5]);
            int sourceY = getInt(parts[6]);
            comp.fireDrop(new DropEvent(comp, getEventObject(comp, parts[0]), sourceX, sourceY, dragComponent, getEventObject(dragComponent, parts[2]), dragX, dragY));
        } else if (name.equals("size")) {
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
        } else if (styleProp.equals(FX.PROPERTY_FX_OPACITY_CHANGE)) {
            type = fx.getOpacityChange();
        } else if (styleProp.equals(FX.PROPERTY_FX_POSITION_CHANGE)) {
            type = fx.getPositionChange();
        } else {
            type = fx.getSizeChange();
        }
        
        FX.Type NONE = FX.Type.NONE;
        
        if (type == NONE || (type.getDuration() == NONE.getDuration()) && type.getFrames() == NONE.getFrames()) {
            postClientEvent(standardMethod, newValue);
        } else {
            int time = type.getDuration();
            int prev, next;
            
            if (styleProp.equals(FX.PROPERTY_FX_VISIBLE_CHANGE)) {
                propertyName = "opacity";
                int opacity = comp.getStyle().getFX().getOpacity();
                prev = ((Boolean)oldValue).booleanValue() ? opacity : 0;
                next = ((Boolean)newValue).booleanValue() ? opacity : 0;
            } else {
                if (styleProp.equals(FX.PROPERTY_FX_OPACITY_CHANGE)) propertyName = "opacity";
                prev = (Integer)oldValue;
                next = (Integer)newValue;
            }
            
            int change = next - prev;
            if (change < 0) change = ~change + 1;
            int steps = (int)Math.floor(time / type.getFrames() + .5) - 1;
            Transition trans = type.getTransition();
            StringBuffer seq = new StringBuffer();
            seq.append('[');
            double step = Math.floor(1.0 / steps * 100000) / 100000; //percent of each step;

            for (int i = 1; i <= steps; i++) {
                int size = (int)Math.floor(trans.apply(step * i) * change);                    
                seq.append(next < prev ? prev - size : prev + size).append(',');
            }
            
            seq.append(next).append(']');
            wr.ai.clientSideMethodCall(id, SET_PROPERTY_WITH_EFFECT, propertyName, time, seq);
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
        } else if (name.equals(FX.PROPERTY_FX_OPACITY)) {
            setPropertyWithEffect(name, pce.getNewValue(), pce.getOldValue(), SET_FX_OPACITY, FX.PROPERTY_FX_OPACITY_CHANGE);
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

    final void setPropertyChangeIgnored(String name, boolean ignore) {
        if (ignore) {            
            ignoredProperties.put(name, NO_VALUE);
        } else {
            ignoredProperties.remove(name);
        }
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

    static Object getEventObject(Component comp, String data) {
        Object o;
        
        if (comp instanceof GridBox) {
            String[] values = data.split("@");
            o = new GridBox.Range((GridBox)comp, Integer.parseInt(values[0]), Integer.parseInt(values[1]));
        } else if (comp instanceof Tree || comp instanceof Menu) {
            o = TreeRenderer.fullIndexItem((HierarchyComponent)comp, data);
        } else if (comp instanceof DateBox) {
            try {
                o = DateBoxRenderer.dateBoxFormat.parse(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            o = null;
        }
        
        return o;
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
