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
package thinwire.ui.style;

/**
 * @author Joshua J. Gertzen
 */
public class Border {
    public static final String PROPERTY_BORDER_COLOR = "borderColor";
    public static final String PROPERTY_BORDER_SIZE = "borderSize";
    public static final String PROPERTY_BORDER_TYPE = "borderType";

    public enum Type {
        NONE, SOLID, DOUBLE, INSET, OUTSET, RIDGE, GROOVE, DASHED, DOTTED;
        
        public String toString() {
            return name().toLowerCase();
        }
    };
    
    private Style parent;
    private Style defaultStyle;
    private Type type;
    private int size = -1;
    private Color color;
    
    Border(Style parent, Style defaultStyle) {
        this.parent = parent;
        this.defaultStyle = defaultStyle;        
        if (defaultStyle != null) copy(defaultStyle.getBorder());
    }
    
    public Object getValue(String propertyName) {
        return getBorderValue(this, propertyName);
    }
    
    public Object getDefaultValue(String propertyName) {
        if (defaultStyle == null) throw new IllegalStateException("defaultStyle == null");        
        return getBorderValue(defaultStyle.getBorder(), propertyName);
    }
    
    private Object getBorderValue(Border border, String propertyName) {
        if (propertyName.equals(PROPERTY_BORDER_TYPE)) {
            return border.getType();
        } else if (propertyName.equals(PROPERTY_BORDER_SIZE)) {
            return border.getSize();
        } else if (propertyName.equals(PROPERTY_BORDER_COLOR)) {
            return border.getColor();
        } else {
            throw new IllegalArgumentException("property '" + propertyName + "' is unknown");
        }
    }
    
    public void copy(Border border) {
        if (border == null) throw new IllegalArgumentException("border == null");
        setType(border.getType());
        setSize(border.getSize());
        setColor(border.getColor());
    }
    
    public Style getParent() {
        return parent;
    }    
    
    public Type getType() {
        if (type == null) throw new IllegalStateException("type == null");
        return type;
    }

    public void setType(Type type) {
        if (type == null && defaultStyle != null) type = defaultStyle.getBorder().getType();
        if (type == null) throw new IllegalArgumentException("type == null");
        Type oldType = this.type; 
        this.type = type;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_BORDER_TYPE, oldType, this.type);        
    }
    
    public int getSize() {
        if (size < 0) throw new IllegalStateException("size < 0");
        return size;
    }
    
    public void setSize(int size) {
        if (size < 0 && defaultStyle != null) size = defaultStyle.getBorder().getSize();
        if (size < 0 || size > 32) throw new IllegalArgumentException("size < 0 || size > 32");
        int oldSize = this.size;
        this.size = size;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_BORDER_SIZE, oldSize, size);
    }
    
    public Color getColor() {
        if (color == null)  throw new IllegalStateException("color == null");
        return color;
    }
    
    public void setColor(Color color) {
        if (color == null && defaultStyle != null) color = defaultStyle.getBorder().getColor();        
        if (color == null) throw new IllegalArgumentException("color == null");
        Color oldColor = this.color;
        this.color = color;        
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_BORDER_COLOR, oldColor, this.color);
    }
}
