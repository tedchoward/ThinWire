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

import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 */
public class Background {
    public static final String PROPERTY_BACKGROUND_COLOR = "backgroundColor";
    public static final String PROPERTY_BACKGROUND_IMAGE = "backgroundImage";
    
    private Style parent;
    private Color color;
    private ImageInfo imageInfo = new ImageInfo();
    
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
            if (getImage().equals(db.getImage())) setImage(background.getImage());
        } else {
            setColor(background.getColor());
            setImage(background.getImage());
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
        if (color == null) throw new IllegalArgumentException("color == null && defaultStyle.getBackground().getColor() == null");
        Color oldColor = this.color;
        this.color = color;        
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BACKGROUND_COLOR, oldColor, this.color);
    }
    
    public String getImage() {
        return imageInfo.getName();
    }
    
    public void setImage(String image) {
        if (image == null && parent.defaultStyle != null) image = parent.defaultStyle.getBackground().getImage();
        if (image == null) throw new IllegalArgumentException("image == null && defaultStyle.getBackground().getImage() == null");
        String oldImage = imageInfo.getName();        
        imageInfo.setName(image);
        if (parent != null) parent.firePropertyChange(this, PROPERTY_BACKGROUND_IMAGE, oldImage, imageInfo.getName());
    }
}
