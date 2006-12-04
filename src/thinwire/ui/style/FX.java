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

import static thinwire.ui.style.Effect.*;

/**
 * @author Joshua J. Gertzen
 */
public class FX {
    public static final String PROPERTY_FX_POSITION_CHANGE = "fXPositionChange";
    public static final String PROPERTY_FX_SIZE_CHANGE = "fXSizeChange";
    public static final String PROPERTY_FX_VISIBLE_CHANGE = "fXVisibleChange";
    public static final String PROPERTY_FX_OPACITY_CHANGE = "fXOpacityChange";
    
    private Style parent;
    private Motion positionChange;
    private Motion sizeChange;
    private Motion visibleChange;
    private Motion opacityChange;
    
    FX(Style parent) {
        this.parent = parent;
        if (parent.defaultStyle != null) copy(parent.defaultStyle.getFX());        
    }    
        
    public void copy(FX fx) {
        copy(fx, false);
    }

    public void copy(FX fx, boolean onlyIfDefault) {
        if (fx == null) throw new IllegalArgumentException("fx == null");

        if (onlyIfDefault) {
            FX df = parent.defaultStyle.getFX();
            if (getPositionChange().equals(df.getPositionChange())) setPositionChange(fx.getPositionChange());
            if (getSizeChange().equals(df.getSizeChange())) setSizeChange(fx.getSizeChange());
            if (getVisibleChange().equals(df.getVisibleChange())) setVisibleChange(fx.getVisibleChange());
            if (getOpacityChange().equals(df.getOpacityChange())) setOpacityChange(fx.getOpacityChange());
        } else {
            setPositionChange(fx.getPositionChange());
            setSizeChange(fx.getSizeChange());
            setVisibleChange(fx.getVisibleChange());
            setOpacityChange(fx.getOpacityChange());
        }
    }
        
    
    public void setProperty(String name, Object value) {
        if (name.equals(FX.PROPERTY_FX_POSITION_CHANGE)) {
            setPositionChange((Effect.Motion)value);
        } else if (name.equals(FX.PROPERTY_FX_SIZE_CHANGE)) {
            setSizeChange((Effect.Motion)value);
        } else if (name.equals(FX.PROPERTY_FX_VISIBLE_CHANGE)) {
            setVisibleChange((Effect.Motion)value);
        } else if (name.equals(FX.PROPERTY_FX_OPACITY_CHANGE)) {
            setOpacityChange((Effect.Motion)value);
        } else {
            throw new IllegalArgumentException("unknown style property '" + name + "'");
        }
    }
    
    public Object getProperty(String name) {
        Object ret;
        
        if (name.equals(FX.PROPERTY_FX_POSITION_CHANGE)) {
            ret = getPositionChange();
        } else if (name.equals(FX.PROPERTY_FX_SIZE_CHANGE)) {
            ret = getSizeChange();
        } else if (name.equals(FX.PROPERTY_FX_VISIBLE_CHANGE)) {
            ret = getVisibleChange();
        } else if (name.equals(FX.PROPERTY_FX_OPACITY_CHANGE)) {
            ret = getOpacityChange();
        } else {
            throw new IllegalArgumentException("unknown style property '" + name + "'");
        }
        
        return ret;
    }
    
    public Style getParent() {
        return parent;
    }
    
    public Motion getPositionChange() {
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        return positionChange;
    }
    
    public void setPositionChange(Motion positionChange) {
        if (positionChange == null && parent.defaultStyle != null) positionChange = parent.defaultStyle.getFX().getPositionChange();
        if (positionChange == null) throw new IllegalArgumentException("positionChange == null");
        Motion oldPositionChange = this.positionChange;
        this.positionChange = positionChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_POSITION_CHANGE, oldPositionChange, this.positionChange);        
    }
    
    public Motion getSizeChange() {
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        return sizeChange;
    }
       
    public void setSizeChange(Motion sizeChange) {
        if (sizeChange == null && parent.defaultStyle != null) sizeChange = parent.defaultStyle.getFX().getSizeChange();
        if (sizeChange == null) throw new IllegalArgumentException("sizeChange == null");
        Motion oldSizeChange = this.sizeChange;
        this.sizeChange = sizeChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_SIZE_CHANGE, oldSizeChange, this.sizeChange);
    }

    public Motion getVisibleChange() {
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        return visibleChange;
    }
    
    public void setVisibleChange(Motion visibleChange) {
        if (visibleChange == null && parent.defaultStyle != null) visibleChange = parent.defaultStyle.getFX().getVisibleChange();
        if (visibleChange == null) throw new IllegalArgumentException("visibleChange == null");
        Motion oldVisibleChange = this.visibleChange;
        this.visibleChange = visibleChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_VISIBLE_CHANGE, oldVisibleChange, this.visibleChange);
    }

    public Motion getOpacityChange() {
        if (opacityChange == null) throw new IllegalArgumentException("opacityChange == null");
        return opacityChange;
    }
    
    public void setOpacityChange(Motion opacityChange) {
        if (opacityChange == null && parent.defaultStyle != null) opacityChange = parent.defaultStyle.getFX().getOpacityChange();
        if (opacityChange == null) throw new IllegalArgumentException("opacityChange == null");
        Motion oldOpacityChange = this.opacityChange;
        this.opacityChange = opacityChange;
        if (parent != null) parent.firePropertyChange(this, PROPERTY_FX_OPACITY_CHANGE, oldOpacityChange, this.opacityChange);
    }
}
