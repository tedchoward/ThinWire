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
package thinwire.ui.style;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 */
public class Background {
    public static final String PROPERTY_BACKGROUND_COLOR = "backgroundColor";
    public static final String PROPERTY_BACKGROUND_IMAGE = "backgroundImage";
    public static final String PROPERTY_BACKGROUND_REPEAT = "backgroundRepeat";
    public static final String PROPERTY_BACKGROUND_POSITION = "backgroundPosition";
    
    public enum Repeat {
        NONE, BOTH, X, Y;
        
        public String toString() {
            return name().toLowerCase();
        }
    }
    
    public static class Position {
        private static List<Position> VALUES = new ArrayList<Position>();
        
        public static final Position LEFT_TOP = new Position("left top", 0, 0);
        public static final Position LEFT_CENTER = new Position("left center", 0, 50);
        public static final Position LEFT_BOTTOM = new Position("left bottom", 0, 100);
        public static final Position CENTER_TOP = new Position("center top", 50, 0);
        public static final Position CENTER = new Position("center", 50, 50);
        public static final Position CENTER_BOTTOM = new Position("center bottom", 50, 100);
        public static final Position RIGHT_TOP = new Position("right top", 100, 0);
        public static final Position RIGHT_CENTER = new Position("right center", 100, 50);
        public static final Position RIGHT_BOTTOM = new Position("right bottom", 100, 100);
        static {
            Collections.sort(VALUES, new Comparator<Position>() {
                public int compare(Position c1, Position c2) {
                    int o1 = c1.ordinal();
                    int o2 = c2.ordinal();
                    
                    if (o1 == o2) {
                        return 0;
                    } else if (o1 > o2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }            
            });
        }
        
        private static final Pattern REGEX_PERCENT = Pattern.compile("(\\d{1,3})[%]\\s+(\\d{1,3})[%]"); 
        private static int nextOrdinal = 0;        
        
        public static final Position valueOf(String positionId) {
            if (positionId == null) throw new IllegalArgumentException("positionId == null");
            positionId = positionId.trim();
            if (positionId.equals("")) throw new IllegalArgumentException("positionId.equals(\"\")");
            Matcher m = REGEX_PERCENT.matcher(positionId);

            if (m.find()) {
                int x = Integer.parseInt(m.group(1));
                int y = Integer.parseInt(m.group(2));
                
                if (x < 0 || x > 100 || y < 0 || y > 100)
                    throw new IllegalArgumentException("x < 0 || x > 100 || y < 0 || y > 100 : " + positionId);

                for (Position p : VALUES) {
                    if (p.x == x && p.y == y) return p;
                }
                
                return new Position(null, x, y);
            } else {
                positionId = positionId.toUpperCase().replace(' ', '_');
                
                for (Position p : VALUES) {
                    if (p.name.equals(positionId)) {
                        return p;
                    }
                }
                
                throw new IllegalArgumentException("positionId '" + positionId + "' is not a valid position format name");
            }
        }
        
        public static final Position[] values() {
            return VALUES.toArray(new Position[VALUES.size()]);
        }
        
        private int x;
        private int y;
        private int ordinal;
        private String name;
        private String stringName;
        
        private Position(String name, int x, int y) {
            this.x = x;
            this.y = y;
            
            if (name == null) {
                this.ordinal = -1;
                this.stringName = x + "% " + y + "%";
                this.name = "";
            } else {
                this.ordinal = nextOrdinal++;
                this.stringName = name;
                this.name = name.toUpperCase().replaceAll("[- ]", "_");
                VALUES.add(this);
            }
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public String name() {
            return name; 
        }
        
        public int ordinal() {        
            return ordinal;
        }
        
        public int hashCode() {
            return toString().hashCode();
        }
        
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Position)) {
                return false;
            } else {
                Position p = (Position)o;
                return this.x == p.x && this.y == p.y;
            }
        }

        public String toString() {
            return stringName;
        }
    }
    
    private Style parent;
    private Color color;
    private ImageInfo imageInfo = new ImageInfo();
    private Repeat repeat;
    private Position position;

    Background(Style parent) {
        this.parent = parent;
        if (parent.defaultStyle != null) copy(parent.defaultStyle.getBackground());
    }
    
    public void copy(Background background) {
        copy(background, false);
    }

    public void copy(Background background, boolean onlyIfDefault) {
        if (background == null) throw new IllegalArgumentException("background == null");
        
        if (onlyIfDefault) {
            Background db = parent.defaultStyle.getBackground();
            if (getColor().equals(db.getColor())) setColor(background.getColor());
            if (getImage().equals(db.getImage())) setImage(background.getImage());
            if (getRepeat().equals(db.getRepeat())) setRepeat(background.getRepeat());
            if (getPosition().equals(db.getPosition())) setPosition(background.getPosition());
        } else {
            setColor(background.getColor());
            setImage(background.getImage());
            setRepeat(background.getRepeat());
            setPosition(background.getPosition());
        }
    }

    public Style getParent() {
        return parent;
    }    
    
    public Color getColor() {
        if (color == null)  throw new IllegalStateException("color == null");
        return color;
    }
    
    public void setColor(Color color) {
        if (color == null && parent.defaultStyle != null) color = parent.defaultStyle.getBackground().getColor();        
        if (color == null) throw new IllegalArgumentException("color == null && defaultStyle.getBackground().getColor() == null");
        Color oldColor = this.color;
        this.color = color;        
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BACKGROUND_COLOR, oldColor, this.color);
    }
    
    public String getImage() {
        return imageInfo.getName();
    }
    
    public void setImage(String image) {
        if (image == null && parent.defaultStyle != null) image = parent.defaultStyle.getBackground().getImage();
        if (image == null) throw new IllegalArgumentException("image == null && defaultStyle.getBackground().getImage() == null");
        String oldImage = imageInfo.getName();        
        imageInfo.setName(image);
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BACKGROUND_IMAGE, oldImage, imageInfo.getName());
    }

    public void setRepeat(Repeat repeat) {
        if (repeat == null && parent.defaultStyle != null) repeat = parent.defaultStyle.getBackground().getRepeat();
        if (repeat == null) throw new IllegalArgumentException("repeat == null && defaultStyle.getBackground().getRepeat() == null");
        Repeat oldRepeat = this.repeat;
        this.repeat = repeat;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BACKGROUND_REPEAT, oldRepeat, repeat);
    }
    
    public Repeat getRepeat() {
        if (repeat == null) throw new IllegalStateException("repeat not initialized");
        return repeat;
    }
    
    public void setPosition(Position position) {
        if (position == null && parent.defaultStyle != null) position = parent.defaultStyle.getBackground().getPosition();
        if (position == null) throw new IllegalArgumentException("position == null && defaultStyle.getBackground().getPosition() == null");
        Position oldPosition = this.position;
        this.position = position;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BACKGROUND_POSITION, oldPosition, position);
    }
    
    public Position getPosition() {
        if (position == null) throw new IllegalStateException("position not initialized");
        return position;
    }
}
