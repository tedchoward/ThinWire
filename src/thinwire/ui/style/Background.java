/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.style;

/**
 * @author Joshua J. Gertzen
 */
public class Background {
    public static final String PROPERTY_BACKGROUND_COLOR = "backgroundColor";
    
    private Style parent;
    private Color color;
    
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
        } else {
            setColor(background.getColor());
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
        if (color == null) throw new IllegalArgumentException("color == null");
        Color oldColor = this.color;
        this.color = color;        
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BACKGROUND_COLOR, oldColor, this.color);
    }
}
