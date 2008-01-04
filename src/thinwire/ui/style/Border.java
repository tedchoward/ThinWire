/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

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
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.ui.style;

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
        NONE, SOLID, DOUBLE, INSET, OUTSET, RIDGE, GROOVE, DASHED, DOTTED, IMAGE;
        
        public String toString() {
            return name().toLowerCase();
        }
    };
    
    private Style parent;
    private Type type;
    private int size = -1;
    private Color color;
    private ImageInfo imageInfo = new ImageInfo(null);
    private Type imageType;
    private String stringValue;
    
    Border(Style parent) {
        this.parent = parent;
        if (parent.defaultStyle != null) copy(parent.defaultStyle.getBorder());
    }
    
    private void clearStringValue() {
        this.stringValue = null;
        if (parent != null) parent.stringValue = null;
    }

    public String toString() {
        if (stringValue == null) stringValue = "Border{color:" + getColor() + ",image:" + getImage() + 
            ",size:" + getSize() + ",type:" + getType() + "}";
        return stringValue;
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Border)) return false;
        if (this == o) return true;
        return this.toString().equals(o.toString());
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
            setImage(border.getImage());
            setSize(border.getSize());
            setColor(border.getColor());
            setType(border.getType());
        }
    }
    
    public void setProperty(String name, Object value) {
        if (name.equals(Border.PROPERTY_BORDER_COLOR)) {
            setColor((Color)value);
        } else if (name.equals(Border.PROPERTY_BORDER_TYPE)) {
            setType((Border.Type)value);
        } else if (name.equals(Border.PROPERTY_BORDER_SIZE)) {
            setSize((Integer)value); 
        } else if (name.equals(Border.PROPERTY_BORDER_IMAGE)) {
            setImage((String)value); 
        } else {
            throw new IllegalArgumentException("unknown style property '" + name + "'");
        }
    }
    
    public Object getProperty(String name) {
        Object ret;
        
        if (name.equals(Border.PROPERTY_BORDER_COLOR)) {
            ret = getColor();
        } else if (name.equals(Border.PROPERTY_BORDER_TYPE)) {
            ret = getType();
        } else if (name.equals(Border.PROPERTY_BORDER_SIZE)) {
            ret = getSize(); 
        } else if (name.equals(Border.PROPERTY_BORDER_IMAGE)) {
            ret = getImage(); 
        } else {
            throw new IllegalArgumentException("unknown style property '" + name + "'");
        }
        
        return ret;
    }

    public Style getParent() {
        return parent;
    }    
    
    public Type getType() {
        if (type == null) throw new IllegalStateException("type == null");
        return type;
    }

    public void setType(Type type) {
        if (type == null && parent.defaultStyle != null) {
            type = parent.defaultStyle.getBorder().getType();
            if (type == Type.IMAGE) setImage(parent.defaultStyle.getBorder().getImage());
        }
        if (type == null) throw new IllegalArgumentException("type == null");
        if (type == Type.IMAGE && getImage().length() == 0) throw new IllegalStateException("type == Type.IMAGE && getImage().length() == 0");
        Type oldType = this.type; 
        this.clearStringValue();
        this.type = type;
        if (oldType == Type.IMAGE && type != Type.IMAGE) setImage("");
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BORDER_TYPE, oldType, this.type);
    }
    
    public int getSize() {
        if (size < 0) throw new IllegalStateException("size < 0");
        return size;
    }
    
    public void setSize(int size) {
        if (size < 0 && parent.defaultStyle != null) size = parent.defaultStyle.getBorder().getSize();
        if (size < 0 || size > 128) throw new IllegalArgumentException("size < 0 || size > 128");
        int oldSize = this.size;
        this.clearStringValue();
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
        this.clearStringValue();
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
        this.clearStringValue();
        imageInfo = new ImageInfo(image);
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BORDER_IMAGE, oldImage, imageInfo.getName());
        
        if (imageInfo.getName().length() > 0) {
            if (type != Type.IMAGE) {
                imageType = type;
                setType(Type.IMAGE);
            }
        } else if (oldImage.length() > 0) {
            if (getType() == Type.IMAGE) {
                if (imageType == null && parent.defaultStyle.getBorder().getType() == Type.IMAGE) imageType = Type.SOLID;
                setType(imageType);
            }

            imageType = null;
        }
    }
    
    public ImageInfo getImageInfo() {
        return imageInfo;
    }
}
