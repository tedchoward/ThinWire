/*
 * Created on Jul 5, 2006
  */
package thinwire.ui.style;

import thinwire.render.web.WebApplication;
import thinwire.ui.Application;
import thinwire.ui.Component;

public class Effect implements StyleGroup<Effect> {
    public static final String PROPERTY_EFFECT_POSITION_CHANGE = "effectPositionChange";
    public static final String PROPERTY_EFFECT_SIZE_CHANGE = "effectSizeChange";
    public static final String PROPERTY_EFFECT_VISIBLE_CHANGE = "effectVisibleChange";

    public enum Type {
        NONE, SMOOTH;
        
        public String toString() {
            return this.name().toLowerCase();
        }
    }
    
    public static class FX {
        public static void setX(Component comp, int x) {
            setX(comp, x, getOptimalTime(comp.getX(), x), false);        
        }
        
        public static void setX(Component comp, int x, int time) {
            setX(comp, x, time, false);
        }
        
        public static void setX(Component comp, int x, int time, boolean exactTime) {
            setBounds(comp, x, comp.getY(), comp.getWidth(), comp.getHeight(), time, exactTime);                
        }

        public static void setY(Component comp, int y) {
            setY(comp, y, getOptimalTime(comp.getY(), y), false);        
        }
        
        public static void setY(Component comp, int y, int time) {
            setY(comp, y, time, false);
        }
        
        public static void setY(Component comp, int y, int time, boolean exactTime) {
            setBounds(comp, comp.getX(), y, comp.getWidth(), comp.getHeight(), time, exactTime);                
        }

        public static void setWidth(Component comp, int width) {
            setWidth(comp, width, getOptimalTime(comp.getWidth(), width), false);        
        }
        
        public static void setWidth(Component comp, int width, int time) {
            setWidth(comp, width, time, false);
        }
        
        public static void setWidth(Component comp, int width, int time, boolean exactTime) {
            setBounds(comp, comp.getX(), comp.getY(), width, comp.getHeight(), time, exactTime);                
        }

        public static void setHeight(Component comp, int height) {
            setHeight(comp, height, getOptimalTime(comp.getHeight(), height), false);        
        }
        
        public static void setHeight(Component comp, int height, int time) {
            setHeight(comp, height, time, false);
        }
        
        public static void setHeight(Component comp, int height, int time, boolean exactTime) {
            setBounds(comp, comp.getX(), comp.getY(), comp.getWidth(), height, time, exactTime);                
        }
        
        public static void setSize(Component comp, int width, int height) {
            int time = Math.max(getOptimalTime(comp.getWidth(), width), getOptimalTime(comp.getHeight(), height));
            setSize(comp, width, height, time, false);
        }

        public static void setSize(Component comp, int width, int height, int time) {
            setSize(comp, width, height, time, false);        
        }

        public static void setSize(Component comp, int width, int height, int time, boolean exactTime) {
            setBounds(comp, comp.getX(), comp.getY(), width, height, time, exactTime);        
        }
        
        public static void setPosition(Component comp, int x, int y) {
            int time = Math.max(getOptimalTime(comp.getX(), x), getOptimalTime(comp.getY(), y));
            setPosition(comp, x, y, time, false);
        }
            
        public static void setPosition(Component comp, int x, int y, int time) {
            setPosition(comp, x, y, time, false);
        }
        
        public static void setPosition(Component comp, int x, int y, int time, boolean exactTime) {
            setBounds(comp, x, y, comp.getWidth(), comp.getHeight(), time, exactTime);
        }    

        public static void setBounds(Component comp, int x, int y, int width, int height) {
            int time = Math.max(Math.max(getOptimalTime(comp.getX(), x), getOptimalTime(comp.getY(), y)),
                    Math.max(getOptimalTime(comp.getWidth(), width), getOptimalTime(comp.getHeight(), height)));
            setBounds(comp, x, y, width, height, time, false);
        }
        
        public static void setBounds(Component comp, int x, int y, int width, int height, int time) {
            setBounds(comp, x, y, width, height, time, false);
        }    

        public static void setBounds(Component comp, int x, int y, int width, int height, int time, boolean exactTime) {
            WebApplication app = (WebApplication)Application.current();        
            Integer id = app.getComponentId(comp);
            
            if (id == null) {
                comp.setPosition(x, y);
            } else {
                if (time > 10000) time = 10000;
                app.callClientFunction(false, "tw_Animation", "setBounds", new Object[] {id, x, y, width, height, time, exactTime});
            }
        }    

        public static void setVisible(Component comp, boolean visible) {
            setVisible(comp, visible, getOptimalTime(0, 100), false);
        }
        
        public static void setVisible(Component comp, boolean visible, int time) {
            setVisible(comp, visible, time, false);        
        }
        
        public static void setVisible(Component comp, boolean visible, int time, boolean exactTime) {
            WebApplication app = (WebApplication)Application.current();        
            Integer id = app.getComponentId(comp);
            
            if (id == null) {
                comp.setVisible(visible);
            } else {
                if (time > 10000) time = 10000;
                app.callClientFunction(false, "tw_Animation", "setVisible", new Object[] {id, visible, time, exactTime});
            }
        }
        
        private static int getOptimalTime(int oldValue, int newValue) {
            int time = newValue - oldValue;
            if (time < 0) time = ~time + 1;
            time *= 0.33;
            return time;
        }    
        
        private FX() {}
    }    
    
    private Style parent;
    private Style defaultStyle;
    private Type positionChange;
    private Type sizeChange;
    private Type visibleChange;
    
    Effect(Style parent, Style defaultStyle) {
        this.parent = parent;
        this.defaultStyle = defaultStyle;        
        if (defaultStyle != null) copy(defaultStyle.getEffect());        
    }    
    
    public Object getValue(String propertyName) {
        return getEffectValue(this, propertyName);
    }
    
    public Object getDefaultValue(String propertyName) {
        if (defaultStyle == null) throw new IllegalStateException("defaultStyle == null");
        return getEffectValue(defaultStyle.getEffect(), propertyName);
    }
    
    private static Object getEffectValue(Effect effect, String propertyName) {
        if (propertyName.equals(PROPERTY_EFFECT_POSITION_CHANGE)) {
            return effect.getPositionChange();
        } else if (propertyName.equals(PROPERTY_EFFECT_SIZE_CHANGE)) {
            return effect.getSizeChange();            
        } else if (propertyName.equals(PROPERTY_EFFECT_VISIBLE_CHANGE)) {
            return effect.getVisibleChange();            
        } else {
            throw new IllegalArgumentException("property '" + propertyName + "' is unknown");
        }
    }
        
    public void copy(Effect effect) {
        if (effect == null) throw new IllegalArgumentException("effect == null");
        setPositionChange(effect.getPositionChange());
        setSizeChange(effect.getSizeChange());
        setVisibleChange(effect.getVisibleChange());
    }
        
    public Style getParent() {
        return parent;
    }
    
    public Type getPositionChange() {
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        return positionChange;
    }
    
    public void setPositionChange(Type positionChange) {
        if (positionChange == null && defaultStyle != null) positionChange = defaultStyle.getEffect().getPositionChange();
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        Type oldPositionChange = this.positionChange;
        this.positionChange = positionChange;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_EFFECT_POSITION_CHANGE, oldPositionChange, this.positionChange);        
    }
    
    public Type getSizeChange() {
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        return sizeChange;
    }
       
    public void setSizeChange(Type sizeChange) {
        if (sizeChange == null && defaultStyle != null) sizeChange = defaultStyle.getEffect().getSizeChange();
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        Type oldSizeChange = this.sizeChange;
        this.sizeChange = sizeChange;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_EFFECT_SIZE_CHANGE, oldSizeChange, this.sizeChange);
    }

    public Type getVisibleChange() {
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        return visibleChange;
    }

    public void setVisibleChange(Type visibleChange) {
        if (visibleChange == null && defaultStyle != null) visibleChange = defaultStyle.getEffect().getVisibleChange();
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        Type oldVisibleChange = this.visibleChange;
        this.visibleChange = visibleChange;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_EFFECT_VISIBLE_CHANGE, oldVisibleChange, this.visibleChange);
    }
}
