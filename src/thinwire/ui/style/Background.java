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
public class Background implements StyleGroup<Background> {
    public static final String PROPERTY_BACKGROUND_COLOR = "backgroundColor";
        
    private Style parent;
    private Style defaultStyle;
    private Color color;
    
    Background(Style parent, Style defaultStyle) {
        this.parent = parent;
        this.defaultStyle = defaultStyle;
        if (defaultStyle != null) copy(defaultStyle.getBackground());
    }
    
    public Object getValue(String propertyName) {
        return getBackgroundValue(this, propertyName);
    }
    
    public Object getDefaultValue(String propertyName) {
        if (defaultStyle == null) throw new IllegalStateException("defaultStyle == null");        
        return getBackgroundValue(defaultStyle.getBackground(), propertyName);
    }
    
    private static Object getBackgroundValue(Background background, String propertyName) {
        if (propertyName.equals(PROPERTY_BACKGROUND_COLOR)) {
            return background.getColor();
        } else {
            throw new IllegalArgumentException("property '" + propertyName + "' is unknown");
        }
    }
     
    public void copy(Background background) {
        if (background == null) throw new IllegalArgumentException("background == null");
        setColor(background.getColor());
    }
    
    public Style getParent() {
        return parent;
    }    
    
    public Color getColor() {
        if (color == null)  throw new IllegalStateException("color == null");
        return color;
    }
    
    public void setColor(Color color) {
        if (color == null && defaultStyle != null) color = defaultStyle.getBackground().getColor();        
        if (color == null) throw new IllegalArgumentException("color == null");
        Color oldColor = this.color;
        this.color = color;        
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_BACKGROUND_COLOR, oldColor, this.color);
    }
}
