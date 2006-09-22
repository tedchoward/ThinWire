/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
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
    private Type type;
    private int size = -1;
    private Color color;
    
    Border(Style parent) {
        this.parent = parent;
        if (parent.defaultStyle != null) copy(parent.defaultStyle.getBorder());
    }
            
    public void copy(Border border) {
        copy(border, false);
    }

    public void copy(Border border, boolean onlyIfDefault) {
        if (border == null) throw new IllegalArgumentException("border == null");
        
        if (onlyIfDefault) {
            Border db = parent.defaultStyle.getBorder();
            if (getType().equals(db.getType())) setType(border.getType());
            if (getSize() == db.getSize()) setSize(border.getSize());
            if (getColor().equals(db.getColor())) setColor(border.getColor());            
        } else {
            setType(border.getType());
            setSize(border.getSize());
            setColor(border.getColor());
        }
    }
    
    public Style getParent() {
        return parent;
    }    
    
    public Type getType() {
        if (type == null) throw new IllegalStateException("type == null");
        return type;
    }

    public void setType(Type type) {
        if (type == null && parent.defaultStyle != null) type = parent.defaultStyle.getBorder().getType();
        if (type == null) throw new IllegalArgumentException("type == null");
        Type oldType = this.type; 
        this.type = type;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BORDER_TYPE, oldType, this.type);        
    }
    
    public int getSize() {
        if (size < 0) throw new IllegalStateException("size < 0");
        return size;
    }
    
    public void setSize(int size) {
        if (size < 0 && parent.defaultStyle != null) size = parent.defaultStyle.getBorder().getSize();
        if (size < 0 || size > 32) throw new IllegalArgumentException("size < 0 || size > 32");
        int oldSize = this.size;
        this.size = size;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BORDER_SIZE, oldSize, size);
    }
    
    public Color getColor() {
        if (color == null)  throw new IllegalStateException("color == null");
        return color;
    }
    
    public void setColor(Color color) {
        if (color == null && parent.defaultStyle != null) color = parent.defaultStyle.getBorder().getColor();        
        if (color == null) throw new IllegalArgumentException("color == null");
        Color oldColor = this.color;
        this.color = color;        
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BORDER_COLOR, oldColor, this.color);
    }
}
