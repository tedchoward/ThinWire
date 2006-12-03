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
package thinwire.ui.style;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joshua J. Gertzen
 */
public class FX {
    public static final String PROPERTY_FX_POSITION_CHANGE = "fXPositionChange";
    public static final String PROPERTY_FX_SIZE_CHANGE = "fXSizeChange";
    public static final String PROPERTY_FX_VISIBLE_CHANGE = "fXVisibleChange";
    public static final String PROPERTY_FX_OPACITY_CHANGE = "fXOpacityChange";
    public static final String PROPERTY_FX_OPACITY = "fXOpacity";

    public static abstract class Transition {
        private static List<Transition> VALUES = new ArrayList<Transition>();
        
        public static final Transition LINEAR = new Transition(true) {
            public double apply(double position) {
                return position;
            }
            
            public String toString() {
                return "LINEAR";
            }
        };

        public static final Transition SMOOTH = new Transition(true) {
            public double apply(double position) {
                return (-Math.cos(position * Math.PI)/2) + 0.5;
            }
            
            public String toString() {
                return "SMOOTH";
            }
        };
        
        public static final Transition WOBBLE = new Transition(true) {
            public double apply(double position) {
                return (-Math.cos(position * Math.PI * (9 * position)) / 2) + 0.5;
            }
            
            public String toString() {
                return "WOBBLE";
            }
        };

        public static final Transition FLICKER = new Transition(true) {
            public double apply(double position) {
                return ((-Math.cos(position * Math.PI) / 4) + 0.75) + Math.random();
            }
            
            public String toString() {
                return "FLICKER";
            }
        };
        
        public static final Transition PULSE = new Transition(true) {
            public double apply(double position) {
                return Math.floor(position * 10) % 2 == 0 ?
                        (position * 10 - Math.floor(position * 10)) : 
                            1 - (position * 10 - Math.floor(position * 10));
            }
            
            public String toString() {
                return "PULSE";
            }
        };
        
        public static final Transition valueOf(String value) {
            value = value.toUpperCase();
            
            for (Transition t : VALUES) {
                if (t.toString().equals(value)) return t;
            }
            
            throw new IllegalArgumentException("the specified value '" + value + "' does not identify a known transition");
        }
        
        public Transition() { }
        
        private Transition(boolean addToList) {
            VALUES.add(this);
        }

        public abstract double apply(double position);
    }
    
    public static final class Type {
        private static final List<Type> VALUES = new ArrayList<Type>();
        
        public static final Type NONE = new Type(0, 1, Transition.LINEAR, "NONE");
        public static final Type SLOW_LINEAR = new Type(2000, 25, Transition.LINEAR, "SLOW_LINEAR");
        public static final Type SLOW_SMOOTH = new Type(2000, 25, Transition.SMOOTH, "SLOW_SMOOTH");
        public static final Type SLOW_WOBBLE = new Type(2000, 25, Transition.WOBBLE, "SLOW_WOBBLE");
        public static final Type SLOW_FLICKER = new Type(2000, 25, Transition.FLICKER, "SLOW_FLICKER");
        public static final Type SLOW_PULSE = new Type(2000, 25, Transition.PULSE, "SLOW_PULSE");
        public static final Type LINEAR = new Type(1000, 25, Transition.LINEAR, "LINEAR");
        public static final Type SMOOTH = new Type(1000, 25, Transition.SMOOTH, "SMOOTH");
        public static final Type WOBBLE = new Type(1000, 25, Transition.WOBBLE, "WOBBLE");
        public static final Type FLICKER = new Type(1000, 25, Transition.FLICKER, "FLICKER");
        public static final Type PULSE = new Type(1000, 25, Transition.PULSE, "PULSE");
        public static final Type FAST_LINEAR = new Type(500, 25, Transition.LINEAR, "FAST_LINEAR");
        public static final Type FAST_SMOOTH = new Type(500, 25, Transition.SMOOTH, "FAST_SMOOTH");
        public static final Type FAST_WOBBLE = new Type(500, 25, Transition.WOBBLE, "FAST_WOBBLE");
        public static final Type FAST_FLICKER = new Type(500, 25, Transition.FLICKER, "FAST_FLICKER");
        public static final Type FAST_PULSE = new Type(500, 25, Transition.PULSE, "FAST_PULSE");
        
        private static int nextOrdinal = 0;
        
        public static final Type valueOf(String value) {
            value = value.toUpperCase();
            
            for (Type t : VALUES) {
                if (t.stringValue.equals(value)) return t;
            }
            
            String[] parts = value.split("\\s+");
            int duration = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            int frames = parts.length > 1 ? Integer.parseInt(parts[1]) : 25;
            Transition transition = parts.length > 2 ? Transition.valueOf(parts[2].toUpperCase()) : Transition.SMOOTH;

            for (Type t : VALUES) {
                if (t.duration == duration && t.frames == frames && t.transition == transition) return t;
            }
            
            return new Type(duration, frames, transition);
        }
        
        public static final Type[] values() {
            return VALUES.toArray(new Type[VALUES.size()]);
        }
        
        private int ordinal;
        private String stringValue;
        private String name;
        private int duration;
        private int frames;
        private Transition transition;
        
        public Type(int duration) {
            this(duration, 0, null, null);
        }
        
        public Type(int duration, int frames) {
            this(duration, frames, null, null);
        }

        public Type(int duration, int frames, Transition transition) {
            this(duration, frames, transition, null);
        }
        
        private Type(int duration, int frames, Transition transition, String name) {
            if (duration < 0) throw new IllegalArgumentException("duration{" + duration + "} < 0");
            if (frames == 0) frames = 25;
            if (frames < 1 || frames > 100) throw new IllegalArgumentException("frames{" + duration + "} < 1 || frames > 100");
            if (transition == null) transition = Transition.SMOOTH;
            this.duration = duration;
            this.frames = frames;
            this.transition = transition;
            
            if (name == null) {
                stringValue = duration + " " + frames + " " + transition;
                ordinal = -1;
                this.name = "";
            } else {
                this.name = name.toUpperCase();
                stringValue = name.toLowerCase();
                ordinal = nextOrdinal++;
                VALUES.add(this);
            }
        }

        public int getDuration() { 
            return duration;
        }

        public int getFrames() {
            return frames;
        }
        
        public Transition getTransition() {
            return transition;
        }
        
        public String name() {
            return name.toUpperCase();
        }
        
        public int ordinal() {        
            return ordinal;
        }
        
        public boolean equals(Object o) {
            return o instanceof Type && toString().equals(o.toString()); 
        }
        
        public int hashCode() {
            return toString().hashCode();
        }
        
        public String toString() {
            return stringValue;
        }
    }
    
    private Style parent;
    private Type positionChange;
    private Type sizeChange;
    private Type visibleChange;
    private Type opacityChange;
    private int opacity = -1;
    
    FX(Style parent) {
        this.parent = parent;
        if (parent.defaultStyle != null) copy(parent.defaultStyle.getFX());        
    }    
        
    public void copy(FX fx) {
        copy(fx, false);
    }

    public void copy(FX fx, boolean onlyIfDefault) {
        if (fx == null) throw new IllegalArgumentException("fx == null");

        if (onlyIfDefault) {
            FX df = parent.defaultStyle.getFX();
            if (getPositionChange().equals(df.getPositionChange())) setPositionChange(fx.getPositionChange());
            if (getSizeChange().equals(df.getSizeChange())) setSizeChange(fx.getSizeChange());
            if (getVisibleChange().equals(df.getVisibleChange())) setVisibleChange(fx.getVisibleChange());
            if (getOpacityChange().equals(df.getOpacityChange())) setOpacityChange(fx.getOpacityChange());
            if (getOpacity() == df.getOpacity()) setOpacity(fx.getOpacity());
        } else {
            setPositionChange(fx.getPositionChange());
            setSizeChange(fx.getSizeChange());
            setVisibleChange(fx.getVisibleChange());
            setOpacityChange(fx.getOpacityChange());
            setOpacity(fx.getOpacity());
        }
    }
        
    public Style getParent() {
        return parent;
    }
    
    public Type getPositionChange() {
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        return positionChange;
    }
    
    public void setPositionChange(Type positionChange) {
        if (positionChange == null && parent.defaultStyle != null) positionChange = parent.defaultStyle.getFX().getPositionChange();
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        Type oldPositionChange = this.positionChange;
        this.positionChange = positionChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_POSITION_CHANGE, oldPositionChange, this.positionChange);        
    }
    
    public Type getSizeChange() {
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        return sizeChange;
    }
       
    public void setSizeChange(Type sizeChange) {
        if (sizeChange == null && parent.defaultStyle != null) sizeChange = parent.defaultStyle.getFX().getSizeChange();
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        Type oldSizeChange = this.sizeChange;
        this.sizeChange = sizeChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_SIZE_CHANGE, oldSizeChange, this.sizeChange);
    }

    public Type getVisibleChange() {
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        return visibleChange;
    }
    
    public void setVisibleChange(Type visibleChange) {
        if (visibleChange == null && parent.defaultStyle != null) visibleChange = parent.defaultStyle.getFX().getVisibleChange();
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        Type oldVisibleChange = this.visibleChange;
        this.visibleChange = visibleChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_VISIBLE_CHANGE, oldVisibleChange, this.visibleChange);
    }

    public Type getOpacityChange() {
        if (opacityChange == null) throw new IllegalArgumentException("opacityChange == null");
        return opacityChange;
    }
    
    public void setOpacityChange(Type opacityChange) {
        if (opacityChange == null && parent.defaultStyle != null) opacityChange = parent.defaultStyle.getFX().getOpacityChange();
        if (opacityChange == null) throw new IllegalArgumentException("opacityChange == null");
        Type oldOpacityChange = this.opacityChange;
        this.opacityChange = opacityChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_OPACITY_CHANGE, oldOpacityChange, this.opacityChange);
    }
    
    public int getOpacity() {
        if (opacity == -1) throw new IllegalStateException("opacity not initialized");
        return opacity;
    }
    
    public void setOpacity(int opacity) {
        if (opacity <= 0 && parent.defaultStyle != null) opacity = parent.defaultStyle.getFX().getOpacity();
        if (opacity <= 0 && opacity > 100) throw new IllegalArgumentException("opacity <= 0 || opacity > 100");
        int oldOpacity = this.opacity;
        this.opacity = opacity;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_OPACITY, oldOpacity, opacity);
    }
}
