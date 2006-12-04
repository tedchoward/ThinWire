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

/**
 * @author Joshua J. Gertzen
 */
public class Style {  
    public static final String PROPERTY_OPACITY = "opacity";
    public static final String PROPERTY_COLOR = "color";
    
    private static final Style DEFAULT_STYLE;
    static {
        Style s = new Style();
        s.setOpacity(100);
        s.setColor(Color.THREEDFACE);
        
        Font f = s.getFont();
        f.setFamily(Font.Family.SANS_SERIF);
        f.setColor(Color.BLACK);
        f.setItalic(false);
        f.setBold(false);
        f.setUnderline(false);
        f.setStrike(false);
        f.setSize(8);
        
        Background bg = s.getBackground();
        bg.setColor(Color.WHITE);
        bg.setImage("");
        bg.setRepeat(Background.Repeat.NONE);
        bg.setPosition(Background.Position.LEFT_TOP);

        Border b = s.getBorder();
        b.setColor(Color.WHITE);
        b.setSize(0);
        b.setType(Border.Type.NONE);
        b.setImage("");
        
        FX fx = s.getFX();
        fx.setPositionChange(Effect.Motion.NONE);
        fx.setSizeChange(Effect.Motion.NONE);
        fx.setVisibleChange(Effect.Motion.NONE);
        fx.setOpacityChange(Effect.Motion.NONE);
        fx.setColorChange(Effect.Motion.NONE);
        
        DEFAULT_STYLE = s;
    }
    
    private Object parent;
    private Color color;
    private int opacity = -1;
    private Font font;
    private Background background;
    private Border border;
    private FX fx;
    Style defaultStyle;
    
    public Style() {
        this(null, null);
    }
    
    public Style(Style defaultStyle) {
       this(defaultStyle, null); 
    }
    
    protected Style(Style defaultStyle, Object parent) {
        this.parent = parent;
        
        if (defaultStyle == null) {
            this.defaultStyle = DEFAULT_STYLE;        
        } else {
            this.defaultStyle = new Style();
            this.defaultStyle.copy(defaultStyle);
        }
        
        this.font = new Font(this);
        this.background = new Background(this);
        this.border = new Border(this);
        this.fx = new FX(this);
        if (this.defaultStyle != null) this.copy(this.defaultStyle);
    }

    //NOTE: This is overridden by Component so it can receive these property change notifications
    protected void firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        
    }
        
    public void copy(Style style) {
        copy(style, false);
    }

    public void copy(Style style, boolean onlyIfDefault) {
        if (style == null) throw new IllegalArgumentException("style == null");
        
        if (onlyIfDefault) {
            if (getColor().equals(defaultStyle.getColor())) setColor(style.getColor());
            if (getOpacity() == defaultStyle.getOpacity()) setOpacity(style.getOpacity());
        } else {
            setColor(style.getColor());
            setOpacity(style.getOpacity());
        }
        
        getFont().copy(style.getFont(), onlyIfDefault);
        getBackground().copy(style.getBackground(), onlyIfDefault);
        getBorder().copy(style.getBorder(), onlyIfDefault);
        getFX().copy(style.getFX());        
    }
    
    public void setProperty(String name, Object value) {
        if (name.equals(PROPERTY_OPACITY)) {
            setOpacity((Integer)value);
        } else if (name.equals(PROPERTY_COLOR)) {
            setColor((Color)value);
        } else if (name.startsWith("background")) {
            getBackground().setStyleProperty(name, value);
        } else if (name.startsWith("border")) {
            getBorder().setProperty(name, value);
        } else if (name.startsWith("font")) {
            getFont().setProperty(name, value); 
        } else if (name.startsWith("fX")) {
            getFX().setProperty(name, value); 
        } else {
            throw new IllegalArgumentException("unknown style property '" + name + "'");
        }
    }
    
    public Object getProperty(String name) {
        Object ret;
        
        if (name.equals(PROPERTY_OPACITY)) {
            ret = getOpacity();
        } else if (name.equals(PROPERTY_COLOR)) {
            ret = getColor();
        } else if (name.startsWith("background")) {
            ret = getBackground().getStyleProperty(name);
        } else if (name.startsWith("border")) {
            ret = getBorder().getProperty(name);
        } else if (name.startsWith("font")) {
            ret = getFont().getProperty(name); 
        } else if (name.startsWith("fX")) {
            ret = getFX().getProperty(name); 
        } else {
            throw new IllegalArgumentException("unknown style property '" + name + "'");
        }
        
        return ret;
    }
    
    public Object getParent() {
        return parent;
    }
    
    public Color getColor() {
        if (color == null)  throw new IllegalStateException("color not initialized");
        return color;
    }
    
    public void setColor(Color color) {
        if (color == null && defaultStyle != null) color = defaultStyle.getColor();        
        if (color == null) throw new IllegalArgumentException("color == null && defaultStyle.getColor() == null");
        Color oldColor = this.color;
        this.color = color;        
        if (parent != null) firePropertyChange(this, PROPERTY_COLOR, oldColor, this.color);
    }
    
    public int getOpacity() {
        if (opacity == -1) throw new IllegalStateException("opacity not initialized");
        return opacity;
    }
    
    public void setOpacity(int opacity) {
        if (opacity <= 0 && defaultStyle != null) opacity = defaultStyle.getOpacity();
        if (opacity <= 0 && opacity > 100) throw new IllegalArgumentException("opacity <= 0 || opacity > 100");
        int oldOpacity = this.opacity;
        this.opacity = opacity;
        if (parent != null) firePropertyChange(this, PROPERTY_OPACITY, oldOpacity, opacity);
    }
    
    public Font getFont() {
        return font;
    }
    
    public Background getBackground() {
        return background;
    }
    
    public Border getBorder() {
        return border;
    }               
    
    public FX getFX() {
        return fx;
    }               
}