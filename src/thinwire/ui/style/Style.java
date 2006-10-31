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

/**
 * @author Joshua J. Gertzen
 */
public class Style {  
    private static final Style DEFAULT_STYLE;
    static {
        Style s = new Style();
        Font f = s.getFont();
        f.setFamily(Font.Family.SANS_SERIF);
        f.setColor(Color.BLACK);
        f.setItalic(false);
        f.setBold(false);
        f.setUnderline(false);
        f.setSize(8);
        
        s.getBackground().setColor(Color.WHITE);
        s.getBackground().setImage("");
        s.getBackground().setRepeat(Background.Repeat.NONE);
        s.getBackground().setPosition(Background.Position.LEFT_TOP);

        Border b = s.getBorder();
        b.setColor(Color.WHITE);
        b.setSize(0);
        b.setType(Border.Type.NONE);
        b.setImage("");
        
        FX fx = s.getFX();
        fx.setPositionChange(FX.Type.NONE);
        fx.setSizeChange(FX.Type.NONE);
        fx.setVisibleChange(FX.Type.NONE);
        
        DEFAULT_STYLE = s;
    }
    
    private Object parent;
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
        getFont().copy(style.getFont(), onlyIfDefault);
        getBackground().copy(style.getBackground(), onlyIfDefault);
        getBorder().copy(style.getBorder(), onlyIfDefault);
        getFX().copy(style.getFX());        
    }
    
    public Object getParent() {
        return parent;
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