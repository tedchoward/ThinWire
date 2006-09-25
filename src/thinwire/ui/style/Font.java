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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thinwire.render.web.WebApplication;
import thinwire.ui.Application;

/**
 * @author Joshua J. Gertzen
 */
public class Font {
    public static final String PROPERTY_FONT_UNDERLINE = "fontUnderline";
    public static final String PROPERTY_FONT_ITALIC = "fontItalic";
    public static final String PROPERTY_FONT_BOLD = "fontBold";
    public static final String PROPERTY_FONT_SIZE = "fontSize";
    public static final String PROPERTY_FONT_COLOR = "fontColor";
    public static final String PROPERTY_FONT_FAMILY = "fontFamily";
    
    public static class Family {
        private static List<Family> VALUES = new ArrayList<Family>();
        
        public static final Family SERIF = new Family("serif", null);
        public static final Family SANS_SERIF = new Family("sans-serif", null);
        public static final Family CURSIVE = new Family("cursive", null);
        public static final Family FANTASY = new Family("fantasy", null);
        public static final Family MONOSPACE = new Family("monospace", null);
        static {
            Collections.sort(VALUES, new Comparator<Family>() {
                public int compare(Family c1, Family c2) {
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

        private static int nextOrdinal = 0;        
        
        public static final Family valueOf(String name) {
            if (name == null) throw new IllegalArgumentException("name == null");
            name = name.trim();
            if (name.equals("")) throw new IllegalArgumentException("name.equals(\"\")");
            int index = name.indexOf(',');
                        
            if (index >= 0) {
                Family parent = valueOf(name.substring(index + 1));
                name = name.substring(0, index).trim();
                if (name.equals("")) throw new IllegalArgumentException("name.equals(\"\")");
                return new Family(name, parent);
            } else {
                name = name.toUpperCase().replace('-', '_');
                
                for (Family f : VALUES) {
                    if (f.name.equals(name)) {
                        return f;
                    }
                }
                
                throw new IllegalArgumentException("name=" + name + " is not a valid base font family");
            }
        }
        
        public static final Family[] values() {
            return VALUES.toArray(new Family[VALUES.size()]);
        }
        
        private int ordinal;
        private String name;
        private String qualifiedName;
        private Family parent;

        private Family(String name, Family parent) {
            if (name == null) throw new IllegalArgumentException("name == null");
            name = name.trim();
            if (name.equals("")) throw new IllegalArgumentException("name.equals(\"\")");            
            if (!name.matches("[-\\w ]+")) throw new IllegalArgumentException("!name.matches(\"[-\\w ]+\")");
            this.ordinal = parent == null ? nextOrdinal++ : -1;
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            this.name = name.toUpperCase().replaceAll("[- ]", "_");
            this.parent = parent;
            
            if (parent == null) {
                VALUES.add(this);
            } else {
                sb.append(", ").append(parent.toString());                
            }
            
            qualifiedName = sb.toString();
        }
        
        public Family getParent() {
            return parent;
        }
        
        public Family sub(String name) {
            return new Family(name, this);
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
            if (o == null || !(o instanceof Family)) {
                return false;
            } else {
                Family f = (Family)o;
                return this.toString().equals(f.toString());
            }
        }        
        
        public String toString() {
            return qualifiedName;
        }
    }

    private static Application.Local<Map<String, Metrics>> fontMetrics = new Application.Local<Map<String,Metrics>>() {
        protected Map<String, Metrics> initialValue() {
            return new HashMap<String, Metrics>();
        }
    };
    
    private static class Metrics {
        private int[] widths;
        private int height;
    }
    
    private Style parent;
    private Family family;
    private Color color;
    private int size;
    private int bold = -1;
    private int italic = -1;
    private int underline = -1;
    private String computedState;
    
    Font(Style parent) {
        this.parent = parent;
        if (parent.defaultStyle != null) copy(parent.defaultStyle.getFont());
    }
    
    private String getComputedState() {
        if (computedState == null) {
            StringBuilder sb = new StringBuilder();
            sb.append('"').append(getFamily()).append('"').append(';');
            sb.append(getSize()).append(';');
            sb.append(isBold()).append(';');
            sb.append(isItalic()).append(';');
            sb.append(isUnderline());
            computedState = sb.toString();
        }
        
        return computedState;
    }
    
    private Metrics getMetrics() {
        String computedState = getComputedState();
        Map<String, Metrics> map = fontMetrics.get(); 
        Metrics fm = map.get(computedState);        
        
        if (fm == null) {
            fm = new Metrics();            
            String[] ary = ((WebApplication)WebApplication.current()).clientSideFunctionCallWaitForReturn("tw_getFontMetrics", getFamily(), getSize(), isBold(), isItalic(), isUnderline()).split(",");
            fm.widths = new int[ary.length - 1];
            fm.height = Integer.parseInt(ary[0]);
                            
            for (int i = 1, cnt = ary.length; i < cnt; i++) {
                fm.widths[i - 1] = Integer.parseInt(ary[i]);
            }
            
            map.put(computedState, fm);
        }
        
        return fm;
    }
    
    public int getStringWidth(String s) {
        Metrics fm = getMetrics();
        int[] widths = fm.widths;        
        int width = 0;
        
        for (int i = 0, cnt = s.length(); i < cnt; i++) {
            char c = s.charAt(i);
            byte b = (byte)c;
            int w = widths[b - 32];
            width += w;
        }
        
        return width;
    }
    
    public int getStringHeight(String s) {
        return getMetrics().height;
    }
        
    public void copy(Font font) {
        copy(font, false);
    }
    
    public void copy(Font font, boolean onlyIfDefault) {
        if (font == null) throw new IllegalArgumentException("font == null");
        
        if (onlyIfDefault) {
            Font df = parent.defaultStyle.getFont();
            if (getFamily().equals(df.getFamily())) setFamily(font.getFamily());
            if (getColor().equals(df.getColor())) setColor(font.getColor());
            if (getSize() == df.getSize()) setSize(font.getSize());
            if (isBold() == df.isBold()) setBold(font.isBold());
            if (isItalic() == df.isItalic()) setItalic(font.isItalic());
            if (isUnderline() == df.isUnderline()) setUnderline(font.isUnderline());            
        } else {
            setFamily(font.getFamily());
            setColor(font.getColor());
            setSize(font.getSize());
            setBold(font.isBold());
            setItalic(font.isItalic());
            setUnderline(font.isUnderline());
        }
    }
    
    public Style getParent() {
        return parent;
    }
            
    public Family getFamily() {
        return family;
    }
    
    public void setFamily(Family family) {        
        if (family == null && parent.defaultStyle != null) family = parent.defaultStyle.getFont().getFamily();
        if (family == null) throw new IllegalArgumentException("family == null");
        Family oldFamily = this.family;
        this.computedState = null;
        this.family = family;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FONT_FAMILY, oldFamily, family);
    }
    
    public Color getColor() {
        if (color == null)  throw new IllegalStateException("color == null");
        return color;
    }
    
    public void setColor(Color color) {
        if (color == null && parent.defaultStyle != null) color = parent.defaultStyle.getFont().getColor();        
        if (color == null) throw new IllegalArgumentException("color == null");
        Color oldColor = this.color;
        this.color = color;        
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FONT_COLOR, oldColor, this.color);
    }    
    
    public int getSize() {
        if (size <= 0) throw new IllegalStateException("size <= 0");
        return size;
    }
    
    public void setSize(int size) {
        if (size <= 0 && parent.defaultStyle != null) size = parent.defaultStyle.getFont().getSize();
        if (size <= 0 || size > 128) throw new IllegalArgumentException("size <= 0 || size > 128");
        int oldSize = this.size;
        this.computedState = null;        
        this.size = size;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FONT_SIZE, oldSize, size);
    }
    
    public boolean isBold() {
        if (bold == -1) throw new IllegalStateException("bold not initialized");
        return bold == 1 ? true : false;
    }
    
    public void setBold(boolean bold) {       
        boolean oldBold = this.bold == 1;
        this.computedState = null;        
        this.bold = bold == true ? 1 : 0;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FONT_BOLD, oldBold, bold);
    }

    public boolean isItalic() {
        if (italic == -1) throw new IllegalStateException("italic not initialized");
        return italic == 1 ? true : false;
    }
    
    public void setItalic(boolean italic) {
        boolean oldItalic = this.italic == 1;
        this.computedState = null;        
        this.italic = italic == true ? 1 : 0;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FONT_ITALIC, oldItalic, italic);
    }
    
    public boolean isUnderline() {
        if (underline == -1) throw new IllegalStateException("underline not initialized");
        return underline == 1 ? true : false;
    }
    
    public void setUnderline(boolean underline) {
        boolean oldUnderline = this.underline == 1;
        this.computedState = null;        
        this.underline = underline == true ? 1 : 0;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FONT_UNDERLINE, oldUnderline, underline);        
    }
}
