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
public class Font implements StyleGroup<Font> {
    public static final String PROPERTY_FONT_UNDERLINE = "fontUnderline";
    public static final String PROPERTY_FONT_ITALIC = "fontItalic";
    public static final String PROPERTY_FONT_BOLD = "fontBold";
    public static final String PROPERTY_FONT_SIZE = "fontSize";
    public static final String PROPERTY_FONT_COLOR = "fontColor";
    public static final String PROPERTY_FONT_FAMILY = "fontFamily";
    
    public static enum Family {
        SERIF, SANS_SERIF, CURSIVE, FANTASY, MONOSPACE;
        
        /*private static final Pattern FONT_NAME_PATTERN = Pattern.compile("[\\w|\\-| ]+");
        
        private ThreadLocal<String> optimalFontNames = new ThreadLocal<String>() {
            protected String initialValue() {
                return "";
            }
        };
                
        public void setOptimalFontNames(String optimialFontNames) {
            StringBuffer sb = new StringBuffer();            
            
            for (String s : optimialFontNames.split(",")) {
                s = s.trim();
                if (!FONT_NAME_PATTERN.matcher(s).matches()) throw new IllegalArgumentException("Font name '" + s + "' must match the regex pattern '" + FONT_NAME_PATTERN + "'");                
                sb.append(s).append(',');
            }
            
            sb.deleteCharAt(sb.length() - 1);
            optimalFontNames.set(sb.toString());
        }
        
        public String getOptimialFontNames() {
            return optimalFontNames.get();
        }*/
        
        public String toString() {
            return name().toLowerCase().replace('_', '-');
        }
    }
    
    private Style parent;
    private Style defaultStyle;
    private Family family;
    private Color color;
    private int size;
    private int bold = -1;
    private int italic = -1;
    private int underline = -1;
    
    Font(Style parent, Style defaultStyle) {
        this.parent = parent;
        this.defaultStyle = defaultStyle;        
        if (defaultStyle != null) copy(defaultStyle.getFont());
    }
    
    public Object getValue(String propertyName) {
        return getFontValue(this, propertyName);
    }
    
    public Object getDefaultValue(String propertyName) {
        if (defaultStyle == null) throw new IllegalStateException("defaultStyle == null");
        return getFontValue(defaultStyle.getFont(), propertyName);
    }
    
    private static Object getFontValue(Font font, String propertyName) {        
        if (propertyName.equals(PROPERTY_FONT_FAMILY)) {
            return font.getFamily();
        } else if (propertyName.equals(PROPERTY_FONT_COLOR)) {
            return font.getColor();
        } else if (propertyName.equals(PROPERTY_FONT_SIZE)) {
            return font.getSize();
        } else if (propertyName.equals(PROPERTY_FONT_BOLD)) {
            return font.isBold();
        } else if (propertyName.equals(PROPERTY_FONT_ITALIC)) {
            return font.isItalic();
        } else if (propertyName.equals(PROPERTY_FONT_UNDERLINE)) {
            return font.isUnderline();
        } else {
            throw new IllegalArgumentException("property '" + propertyName + "' is unknown");
        }
    }
        
    public void copy(Font font) {
        if (font == null) throw new IllegalArgumentException("font == null");
        setFamily(font.getFamily());
        setColor(font.getColor());
        setSize(font.getSize());
        setBold(font.isBold());
        setItalic(font.isItalic());
        setUnderline(font.isUnderline());
    }
    
    public Style getParent() {
        return parent;
    }
            
    public Family getFamily() {
        return family;
    }
    
    public void setFamily(Family family) {        
        if (family == null && defaultStyle != null) family = defaultStyle.getFont().getFamily();
        if (family == null) throw new IllegalArgumentException("family == null");
        Family oldFamily = this.family;
        this.family = family;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_FONT_FAMILY, oldFamily, family);
    }
    
    public Color getColor() {
        if (color == null)  throw new IllegalStateException("color == null");
        return color;
    }
    
    public void setColor(Color color) {
        if (color == null && defaultStyle != null) color = defaultStyle.getFont().getColor();        
        if (color == null) throw new IllegalArgumentException("color == null");
        Color oldColor = this.color;
        this.color = color;        
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_FONT_COLOR, oldColor, this.color);
    }    
    
    public int getSize() {
        if (size <= 0) throw new IllegalStateException("size <= 0");
        return size;
    }
    
    public void setSize(int size) {
        if (size <= 0 && defaultStyle != null) size = defaultStyle.getFont().getSize();
        if (size <= 0 || size > 128) throw new IllegalArgumentException("size <= 0 || size > 128");
        int oldSize = this.size;
        this.size = size;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_FONT_SIZE, oldSize, size);
    }
    
    public boolean isBold() {
        if (bold == -1) throw new IllegalStateException("bold not initialized");
        return bold == 1 ? true : false;
    }
    
    public void setBold(boolean bold) {       
        boolean oldBold = this.bold == 1;
        this.bold = bold == true ? 1 : 0;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_FONT_BOLD, oldBold, bold);
    }

    public boolean isItalic() {
        if (italic == -1) throw new IllegalStateException("italic not initialized");
        return italic == 1 ? true : false;
    }
    
    public void setItalic(boolean italic) {
        boolean oldItalic = this.italic == 1;
        this.italic = italic == true ? 1 : 0;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_FONT_ITALIC, oldItalic, italic);
    }
    
    public boolean isUnderline() {
        if (underline == -1) throw new IllegalStateException("underline not initialized");
        return underline == 1 ? true : false;
    }
    
    public void setUnderline(boolean underline) {
        boolean oldUnderline = this.underline == 1;
        this.underline = underline == true ? 1 : 0;
        if (parent != null) parent.firePropertyChange(parent, PROPERTY_FONT_UNDERLINE, oldUnderline, underline);        
    }
}
