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

public final class Effect {
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
        
        protected Transition() { }
        
        private Transition(boolean addToList) {
            VALUES.add(this);
        }

        public abstract double apply(double position);
    }
    
    public static final class Motion {
        private static final List<Motion> VALUES = new ArrayList<Motion>();
        
        public static final Motion NONE = new Motion(0, 1, Transition.LINEAR, "NONE");
        public static final Motion SLOW_LINEAR = new Motion(2000, 25, Transition.LINEAR, "SLOW_LINEAR");
        public static final Motion SLOW_SMOOTH = new Motion(2000, 25, Transition.SMOOTH, "SLOW_SMOOTH");
        public static final Motion SLOW_WOBBLE = new Motion(2000, 25, Transition.WOBBLE, "SLOW_WOBBLE");
        public static final Motion SLOW_FLICKER = new Motion(2000, 25, Transition.FLICKER, "SLOW_FLICKER");
        public static final Motion SLOW_PULSE = new Motion(2000, 25, Transition.PULSE, "SLOW_PULSE");
        public static final Motion LINEAR = new Motion(1000, 25, Transition.LINEAR, "LINEAR");
        public static final Motion SMOOTH = new Motion(1000, 25, Transition.SMOOTH, "SMOOTH");
        public static final Motion WOBBLE = new Motion(1000, 25, Transition.WOBBLE, "WOBBLE");
        public static final Motion FLICKER = new Motion(1000, 25, Transition.FLICKER, "FLICKER");
        public static final Motion PULSE = new Motion(1000, 25, Transition.PULSE, "PULSE");
        public static final Motion FAST_LINEAR = new Motion(500, 25, Transition.LINEAR, "FAST_LINEAR");
        public static final Motion FAST_SMOOTH = new Motion(500, 25, Transition.SMOOTH, "FAST_SMOOTH");
        public static final Motion FAST_WOBBLE = new Motion(500, 25, Transition.WOBBLE, "FAST_WOBBLE");
        public static final Motion FAST_FLICKER = new Motion(500, 25, Transition.FLICKER, "FAST_FLICKER");
        public static final Motion FAST_PULSE = new Motion(500, 25, Transition.PULSE, "FAST_PULSE");
        
        private static int nextOrdinal = 0;
        
        public static final Motion valueOf(String value) {
            value = value.toUpperCase();
            
            for (Motion t : VALUES) {
                if (t.stringValue.equals(value)) return t;
            }
            
            String[] parts = value.split("\\s+");
            int duration = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            int frames = parts.length > 1 ? Integer.parseInt(parts[1]) : 25;
            Transition transition = parts.length > 2 ? Transition.valueOf(parts[2].toUpperCase()) : Transition.SMOOTH;

            for (Motion t : VALUES) {
                if (t.duration == duration && t.frames == frames && t.transition == transition) return t;
            }
            
            return new Motion(duration, frames, transition);
        }
        
        public static final Motion[] values() {
            return VALUES.toArray(new Motion[VALUES.size()]);
        }
        
        private int ordinal;
        private String stringValue;
        private String name;
        private int duration;
        private int frames;
        private Transition transition;
        
        public Motion(int duration) {
            this(duration, 0, null, null);
        }
        
        public Motion(int duration, int frames) {
            this(duration, frames, null, null);
        }

        public Motion(int duration, int frames, Transition transition) {
            this(duration, frames, transition, null);
        }
        
        private Motion(int duration, int frames, Transition transition, String name) {
            if (duration < 0) throw new IllegalArgumentException("duration{" + duration + "} < 0");
            if (frames == 0) frames = 25;
            if (frames < 1 || frames > 100) throw new IllegalArgumentException("frames{" + duration + "} < 1 || frames > 100");
            if (transition == null) transition = Transition.SMOOTH;
            this.duration = duration;
            this.frames = frames;
            this.transition = transition;
            
            if (name == null) {
                stringValue = duration + " " + frames + (duration == 0 && frames == 1 ? "" : " " + transition);
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
            return o instanceof Motion && toString().equals(o.toString()); 
        }
        
        public int hashCode() {
            return toString().hashCode();
        }
        
        public String toString() {
            return stringValue;
        }
    }
    
    private Effect() {
        
    }
}
