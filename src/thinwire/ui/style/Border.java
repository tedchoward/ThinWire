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
import java.util.List;

import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 */
public class Border {
    public static final String PROPERTY_BORDER_COLOR = "borderColor";
    public static final String PROPERTY_BORDER_SIZE = "borderSize";
    public static final String PROPERTY_BORDER_TYPE = "borderType";
    public static final String PROPERTY_BORDER_IMAGE = "borderImage";
    
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
    private ImageInfo imageInfo = new ImageInfo(null);
    
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
            if (getImage().equals(db.getImage())) setImage(border.getImage());            
        } else {
            setType(border.getType());
            setSize(border.getSize());
            setColor(border.getColor());
            setImage(border.getImage());
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
    
    public String getImage() {
        return imageInfo.getName();
    }
    
    public void setImage(String image) {
        if (image == null && parent.defaultStyle != null) image = parent.defaultStyle.getBorder().getImage();
        if (image == null) throw new IllegalArgumentException("image == null && defaultStyle.getBorder().getImage() == null");
        String oldImage = imageInfo.getName();        
        imageInfo = new ImageInfo(image);
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BORDER_IMAGE, oldImage, imageInfo.getName());
    }
    
    public ImageInfo getImageInfo() {
        return imageInfo;
    }
}
