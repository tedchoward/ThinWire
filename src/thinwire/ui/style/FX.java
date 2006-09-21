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
public class FX implements StyleGroup<FX> {
    public static final String PROPERTY_FX_POSITION_CHANGE = "fxPositionChange";
    public static final String PROPERTY_FX_SIZE_CHANGE = "fxSizeChange";
    public static final String PROPERTY_FX_VISIBLE_CHANGE = "fxVisibleChange";
    private static final ClassReflector<FX> reflect = new ClassReflector<FX>(FX.class, "PROPERTY_", "fx");
    
    public enum Type {
        NONE, SMOOTH;
        
        public String toString() {
            return this.name().toLowerCase();
        }
    }
    
    private Style parent;
    private Style defaultStyle;
    private Type positionChange;
    private Type sizeChange;
    private Type visibleChange;
    
    FX(Style parent, Style defaultStyle) {
        this.parent = parent;
        this.defaultStyle = defaultStyle;        
        if (defaultStyle != null) copy(defaultStyle.getFX());        
    }    
        
    public Object getProperty(String propertyName) {
        return reflect.getProperty(this, propertyName);
    }
    
    public void setProperty(String propertyName, Object value) {
        reflect.setProperty(this, propertyName, value);
    }

    public Object getPropertyDefault(String propertyName) {
        if (defaultStyle == null) throw new IllegalStateException("defaultStyle == null");
        return reflect.getProperty(defaultStyle.getFX(), propertyName);
    }
        
    public void copy(FX fx) {
        if (fx == null) throw new IllegalArgumentException("fx == null");
        
        for (String name : reflect.getPropertyNames()) {
            setProperty(name, fx.getProperty(name));
        }
    }
        
    public Style getParent() {
        return parent;
    }
    
    public Type getPositionChange() {
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        return positionChange;
    }
    
    public void setPositionChange(Type positionChange) {
        if (positionChange == null && defaultStyle != null) positionChange = defaultStyle.getFX().getPositionChange();
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        Type oldPositionChange = this.positionChange;
        this.positionChange = positionChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_POSITION_CHANGE, oldPositionChange, this.positionChange);        
    }
    
    public Type getSizeChange() {
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        return sizeChange;
    }
       
    public void setSizeChange(Type sizeChange) {
        if (sizeChange == null && defaultStyle != null) sizeChange = defaultStyle.getFX().getSizeChange();
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        Type oldSizeChange = this.sizeChange;
        this.sizeChange = sizeChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_SIZE_CHANGE, oldSizeChange, this.sizeChange);
    }

    public Type getVisibleChange() {
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        return visibleChange;
    }

    public void setVisibleChange(Type visibleChange) {
        if (visibleChange == null && defaultStyle != null) visibleChange = defaultStyle.getFX().getVisibleChange();
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        Type oldVisibleChange = this.visibleChange;
        this.visibleChange = visibleChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_VISIBLE_CHANGE, oldVisibleChange, this.visibleChange);
    }
}
