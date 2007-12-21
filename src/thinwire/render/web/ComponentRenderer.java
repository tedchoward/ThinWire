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
package thinwire.render.web;

import java.util.*;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.logging.Level;

import thinwire.render.*;
import thinwire.ui.event.*;
import thinwire.ui.*;
import thinwire.ui.style.*;

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
    static final String SET_OPACITY = "setOpacity";
    static final String SET_PROPERTY_WITH_EFFECT = "setPropertyWithEffect";

    //Shared by other renderers
    static final String DESTROY = "destroy";
    static final String SET_IMAGE = "setImage";
    static final String SET_ALIGN_X = "setAlignX";
    
    static final String CLIENT_EVENT_DROP = "drop";
    
    static final Logger log = Logger.getLogger(ComponentRenderer.class.getName()); 
    
    private static final Pattern REGEX_DOUBLE_SLASH = Pattern.compile("\\\\"); 
    private static final Pattern REGEX_DOUBLE_QUOTE = Pattern.compile("\"");
    private static final Pattern REGEX_CRLF = Pattern.compile("\\r?\\n|\\r");         

    private static final Object NO_VALUE = new Object();
        
    private List<String> remoteFiles;
    private StringBuilder initProps = new StringBuilder();
    private Map<String, Object> ignoredProperties = new HashMap<String, Object>(3);
    private Map<String, String> clientSideProps = new HashMap<String, String>();    
	private RichTextParser richTextParser;
	private Component baseComp;
	
    String jsClass;
    Component comp;
    WindowRenderer wr;
    ContainerRenderer cr;
	ComponentRenderer pr;
    Integer id;
    
    void init(String jsClass, WindowRenderer wr, Component comp, ComponentRenderer container) {
        this.jsClass = jsClass;
        this.wr = wr;
        this.cr = container instanceof ContainerRenderer ? (ContainerRenderer)container : null;
        this.pr = container;
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
        Effect.Motion visibleChange = comp.getStyle().getFX().getVisibleChange();
        addClientSideProperty(Component.PROPERTY_FOCUS);
        
        if (!isPropertyChangeIgnored(Component.PROPERTY_X)) addInitProperty(Component.PROPERTY_X, comp.getX());
        if (!isPropertyChangeIgnored(Component.PROPERTY_Y)) addInitProperty(Component.PROPERTY_Y, comp.getY());
        if (!isPropertyChangeIgnored(Component.PROPERTY_WIDTH)) addInitProperty(Component.PROPERTY_WIDTH, comp.getWidth());
        if (!isPropertyChangeIgnored(Component.PROPERTY_HEIGHT)) addInitProperty(Component.PROPERTY_HEIGHT, comp.getHeight());        
        if (!isPropertyChangeIgnored(Style.PROPERTY_OPACITY) && comp.getStyle().getOpacity() != 100) addInitProperty(Style.PROPERTY_OPACITY, comp.getStyle().getOpacity());        
        if (!isPropertyChangeIgnored(Component.PROPERTY_VISIBLE)) addInitProperty(Component.PROPERTY_VISIBLE, 
                visibleChange != Effect.Motion.NONE && cr != null && cr.isFullyRendered() ? Boolean.FALSE : comp.isVisible());                
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
            initProps.setLength(0);
        }

    	baseComp = comp;
    	
    	if (pr != null) {
        	ComponentRenderer base = this;
        	
	    	while (!(base.pr instanceof ContainerRenderer)) {
	    		base = base.pr;
	    	}
	    	
	    	if (base.comp != null) baseComp = base.comp;
    	}

        if (visibleChange != Effect.Motion.NONE && !isPropertyChangeIgnored(Component.PROPERTY_VISIBLE) && comp.isVisible() && cr != null && cr.isFullyRendered())
            setPropertyWithEffect(Component.PROPERTY_VISIBLE, Boolean.TRUE, Boolean.FALSE, SET_VISIBLE, FX.PROPERTY_FX_VISIBLE_CHANGE);
        
        wr.ai.setPackagePrivateMember("renderer", comp, this);        
        
        if (comp.isFocusCapable() && comp.isEnabled() && comp.getParent() instanceof Container && ((Container)wr.comp).getComponentWithFocus() == null) comp.setFocus(true);
        
        wr.ai.flushRenderCallbacks(comp, id);
	}
	
    private void setStyle(String propertyName, Object oldValue) {
        if (propertyName.startsWith("fx") || isPropertyChangeIgnored(propertyName)) return;
        Style s = comp.getStyle();
        if (propertyName.equals(Border.PROPERTY_BORDER_TYPE) && s.getBorder().getType() == Border.Type.IMAGE) return;
        Object value;
        
        if (propertyName.equals(Border.PROPERTY_BORDER_COLOR)) {
            if (s.getBorder().getType() == Border.Type.NONE) return;
            value = s.getBorder().getColor();
        } else if (propertyName.equals(Border.PROPERTY_BORDER_IMAGE)) {
            value = s.getBorder().getImageInfo();
        } else if (propertyName.equals(Font.PROPERTY_FONT_UNDERLINE) || propertyName.equals(Font.PROPERTY_FONT_STRIKE)) {
        	value = new Boolean[]{s.getFont().isUnderline(), s.getFont().isStrike()};
        } else {
            value = s.getProperty(propertyName);
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
        comp = null;
        id = null;
        ignoredProperties.clear();
        clientSideProps.clear();
        initProps.setLength(0);
        
        if (remoteFiles != null) {
            for (String s : remoteFiles) {
                RemoteFileMap.INSTANCE.remove(wr.ai, s);
            }
        }

        wr = null;
        remoteFiles = null;
    }
    
    void addInitProperty(String name, Object value) {
        initProps.append(name).append(':');
        initProps.append(stringValueOf(value));        
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
    
    private Map<Component, RenderStateListener> dragRenderListeners;
    
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

            if (dragRenderListeners == null) dragRenderListeners = new HashMap<Component, RenderStateListener>();
            
            final RenderStateListener dragRenderListener = new RenderStateListener() {
                public void renderStateChange(RenderStateEvent ev) {
                	
                    if (wr == null) {
                        if (dragRenderListeners != null && dragRenderListeners.containsValue(this)) {
                            ((WebApplication)WebApplication.current()).removeRenderStateListener(comp, this);
                            dragRenderListeners.remove(this);
                            if (dragRenderListeners.size() == 0) dragRenderListeners = null;
                        }
                    } else {
                        wr.ai.clientSideMethodCall(wr.ai.getComponentId(dragComponent), "addDragTarget", id);
                    }
                }
            };
        
            dragRenderListeners.put(dragComponent, dragRenderListener);
            wr.ai.addRenderStateListener(dragComponent, dragRenderListener);
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
            if (dragRenderListeners != null && dragRenderListeners.get(subType) != null) wr.ai.removeRenderStateListener((Component)subType, dragRenderListeners.get(subType));
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

        if (!baseComp.isEnabled() || !baseComp.isVisible()) {
			if (log.isLoggable(Level.WARNING)) {
    			log.log(Level.WARNING, "Denied attempt by client code to fire event '" + name + "' on " + 
    						(baseComp.isEnabled() ? "non-visible" : "disabled") + " component {" + comp +
    						(comp instanceof TextComponent ? ":'" + ((TextComponent)comp).getText() + "'}": "}") + 
    						" with value '" + event.getValue() + "'");
    		}
				
    		return;
    	}
    	
        if (name.equals(Component.ACTION_CLICK) || name.equals(Component.ACTION_DOUBLE_CLICK)) {
            String actionIgnoreProperty;
            String value = (String)event.getValue();
            String[] vals = value.split(",", -1);
            int x = getInt(vals[0]);
            int y = getInt(vals[1]);
            int srcX = x;
            int srcY = y;
            
            if (vals.length == 5) {
            	srcX = getInt(vals[2]);
            	srcY = getInt(vals[3]);
            	value = vals[4];
            } else {
            	value = vals[2];
            }
            
            if (this.comp instanceof DateBox) {
                actionIgnoreProperty = DateBox.PROPERTY_SELECTED_DATE;
            } else if (this.comp instanceof GridBox) {
                actionIgnoreProperty = GridBox.Row.PROPERTY_ROW_SELECTED;
            } else if (comp instanceof Tree) {
                actionIgnoreProperty = Tree.Item.PROPERTY_ITEM_SELECTED;
            } else if (comp instanceof Container) {
                actionIgnoreProperty = null;
                value = "";
            } else {
                actionIgnoreProperty = null;
            }
            
            Object evObj = getEventObject(comp, value);
            
            if (comp instanceof Menu && !((Menu.Item)evObj).isEnabled()) {
				if (log.isLoggable(Level.WARNING)) {
	    			log.log(Level.WARNING, "Denied attempt by client code to fire event '" + name + "' on " + 
	    						"disabled Menu.Item{" + comp + ":'" + ((Menu.Item)evObj).getText() + "'}" + 
	    						" with value '" + value + "'");
	    		}
				
				return;
            }
            
            if (actionIgnoreProperty != null) setPropertyChangeIgnored(actionIgnoreProperty, true);
            comp.fireAction(new ActionEvent(name, comp, evObj, x, y, srcX, srcY));
            if (actionIgnoreProperty != null) setPropertyChangeIgnored(actionIgnoreProperty, false);
        } else if (event.getName().equals(CLIENT_EVENT_DROP)) {
            String[] parts = ((String)event.getValue()).split(",", -1);
            Component dragComponent = (Component)wr.ai.getComponentFromId(Integer.parseInt(parts[1]));
            int dragComponentX = getInt(parts[3]);
            int dragComponentY = getInt(parts[4]);
            int sourceComponentX = getInt(parts[5]);
            int sourceComponentY = getInt(parts[6]);
            comp.fireDrop(new DropEvent(comp, getEventObject(comp, parts[0]), sourceComponentX, sourceComponentY, sourceComponentX, sourceComponentY, dragComponent, getEventObject(dragComponent, parts[2]), dragComponentX, dragComponentY, dragComponentX, dragComponentY));
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
            comp.fireKeyPress(new KeyPressEvent((String)event.getValue(), comp));
        }
    }

    void setPropertyWithEffect(String propertyName, Object newValue, Object oldValue, String standardMethod, String styleProp) {
        Effect.Motion type;
        FX fx = comp.getStyle().getFX();
        
        if (styleProp.equals(FX.PROPERTY_FX_VISIBLE_CHANGE)) {
            type = fx.getVisibleChange();
        } else if (styleProp.equals(FX.PROPERTY_FX_OPACITY_CHANGE)) {
            type = fx.getOpacityChange();
        } else if (styleProp.equals(FX.PROPERTY_FX_POSITION_CHANGE)) {
            type = fx.getPositionChange();
        } else if (styleProp.equals(FX.PROPERTY_FX_SIZE_CHANGE)) {
            type = fx.getSizeChange();
        } else {
            type = fx.getColorChange();
        }
        
        Effect.Motion NONE = Effect.Motion.NONE;
        
        if (type == NONE || (type.getDuration() == NONE.getDuration()) && type.getFrames() == NONE.getFrames()) {
        	if (newValue instanceof Color) {
	        	Color color = (Color)oldValue;
	            if (color.isSystemColor()) color = wr.ai.getSystemColor(color.toString());
	            oldValue = color.toHexString();
	            
	            color = (Color)newValue;
	            if (color.isSystemColor()) color = wr.ai.getSystemColor(color.toString());
	            newValue = color.toHexString();
        	}
        	
            postClientEvent(standardMethod, newValue);
        } else {
            int time = type.getDuration();
            StringBuffer seq = new StringBuffer();
            Effect.Transition trans = type.getTransition();
            int steps = (int)Math.floor(time / type.getFrames() + .5) - 1;
            double step = Math.floor(1.0 / steps * 100000) / 100000; //percent of each step;
            
            if (styleProp.equals(FX.PROPERTY_FX_COLOR_CHANGE)) {
                Color prev = (Color)oldValue;
                if (prev.isSystemColor()) prev = wr.ai.getSystemColor(prev.toString());
                Color next = (Color)newValue;
                if (next.isSystemColor()) next = wr.ai.getSystemColor(next.toString());
                
                if (prev == null || next == null) {
                    postClientEvent(standardMethod, newValue);
                } else {
                    int rChange = next.getRed() - prev.getRed();
                    if (rChange < 0) rChange = ~rChange + 1;
                    int gChange = next.getGreen() - prev.getGreen();
                    if (gChange < 0) gChange = ~gChange + 1;
                    int bChange = next.getBlue() - prev.getBlue();
                    if (bChange < 0) bChange = ~bChange + 1;
                    seq.append('[');
        
                    for (int i = 1; i <= steps; i++) {
                        int rSize = (int)Math.floor(trans.apply(step * i) * rChange);
                        rSize = next.getRed() < prev.getRed() ? prev.getRed() - rSize : prev.getRed() + rSize;
                        int gSize = (int)Math.floor(trans.apply(step * i) * gChange);
                        gSize = next.getGreen() < prev.getGreen() ? prev.getGreen() - gSize : prev.getGreen() + gSize;
                        int bSize = (int)Math.floor(trans.apply(step * i) * bChange);
                        bSize = next.getBlue() < prev.getBlue() ? prev.getBlue() - bSize : prev.getBlue() + bSize;
                        seq.append("\"rgb(").append(rSize).append(',').append(gSize).append(',').append(bSize).append(")\"").append(',');
                    }
                    
                    seq.append('"').append(next.toHexString()).append('"').append(']');
                    wr.ai.clientSideMethodCall(id, SET_PROPERTY_WITH_EFFECT, propertyName, time, seq);
                }
            } else {
                int prev, next;
                
                if (styleProp.equals(FX.PROPERTY_FX_VISIBLE_CHANGE)) {
                    int opacity = comp.getStyle().getOpacity();
                    prev = ((Boolean)oldValue).booleanValue() ? opacity : 0;
                    next = ((Boolean)newValue).booleanValue() ? opacity : 0;
                } else {
                    prev = (Integer)oldValue;
                    next = (Integer)newValue;
                }
                
                int change = next - prev;
                if (change < 0) change = ~change + 1;
                seq.append('[');
    
                for (int i = 1; i <= steps; i++) {
                    int size = (int)Math.floor(trans.apply(step * i) * change);                    
                    seq.append(next < prev ? prev - size : prev + size).append(',');
                }
                
                seq.append(next).append(']');
                wr.ai.clientSideMethodCall(id, SET_PROPERTY_WITH_EFFECT, propertyName, time, seq, newValue);
            }	
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
        } else if (name.equals(Background.PROPERTY_BACKGROUND_COLOR) ||
                name.equals(Border.PROPERTY_BORDER_COLOR) || name.equals(Font.PROPERTY_FONT_COLOR)) {
            String setMethod = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            setPropertyWithEffect(name, pce.getNewValue(), pce.getOldValue(), setMethod, FX.PROPERTY_FX_COLOR_CHANGE);
        } else if (name.equals(Style.PROPERTY_OPACITY)) {
            setPropertyWithEffect(name, pce.getNewValue(), pce.getOldValue(), SET_OPACITY, FX.PROPERTY_FX_OPACITY_CHANGE);
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
    
    static String stringValueOf(Object o) {
        String ret;
        
        if (o == null) {
            ret = "null";
        } else if (o instanceof Integer) {
            ret = String.valueOf(((Integer) o).intValue());
        } else if (o instanceof Number) {
            ret = String.valueOf(((Number) o).doubleValue());
        } else if (o instanceof Boolean) {
            ret = String.valueOf(((Boolean) o).booleanValue());
        } else if (o instanceof StringBuilder) {
            ret = o.toString();
        } else if (o instanceof Color) {
        	ret = "\"" + ((Color) o).toHexString() + "\"";
        } else {
            ret = '"' + ComponentRenderer.getEscapedText(o.toString(), false) + '"';
        }
        
        return ret;
    }
    
    static String getEscapedText(String s, boolean CRLFToSpace) {
        s = s.replace('\0', ' ');
        s = REGEX_DOUBLE_SLASH.matcher(s).replaceAll("\\\\\\\\");        
        s = REGEX_DOUBLE_QUOTE.matcher(s).replaceAll("\\\\\"");
        s = REGEX_CRLF.matcher(s).replaceAll(CRLFToSpace ? " " : "\\\\r\\\\n");
        return s;
    }
        
    Object parseRichText(String value) {
        if (value == null) return "";
    	if (this instanceof EditorComponentRenderer) return value;
    	if (richTextParser == null) richTextParser = new RichTextParser(this);
    	return richTextParser.parse(value);
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
            WindowRenderer wr = this instanceof WindowRenderer ? (WindowRenderer)this : this.wr;
            
            if (location.startsWith("file") || location.startsWith("class") || wr.ai.getRelativeFile(location).exists()) {
                if (!location.startsWith("class")) {
                	if (location.startsWith("file:///")) location = location.substring(7);
                	location = wr.ai.getRelativeFile(location).getAbsolutePath();
                }
                if (remoteFiles == null) remoteFiles = new ArrayList<String>(5);
                remoteFiles.add(location);
                location = WebApplication.REMOTE_FILE_PREFIX + RemoteFileMap.INSTANCE.add(wr.ai, location);
            }
        } else {
            location = "";
        }

        return location;
    }
    
    void removeFileFromMap(String location) {
    	location = location.replaceAll(WebApplication.REMOTE_FILE_PREFIX + "(.*)", "$1");
		RemoteFileMap.INSTANCE.remove(wr.ai, location);
    	remoteFiles.remove(location);
    }
}
