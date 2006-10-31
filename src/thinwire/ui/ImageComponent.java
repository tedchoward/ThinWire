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
package thinwire.ui;

import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 */
public interface ImageComponent extends Component {
    public static final String PROPERTY_IMAGE = "image";
    
    /**
     * Returns the image file name that was specified for this component.
     * @return the image file name that was specified for this component.
     */
    public String getImage();
    
    /**
     * Places an image on this component.
     * @param image The file name or resource name of the image.
     */
    public void setImage(String image);
    
    /**
     * Returns an immutable <code>ImageInfo</code> class that provides information
     * about the assigned image, such as width, height, format, etc.
     * @return an immutable <code>ImageInfo</code> describing this component's image, never null.
     */
    public ImageInfo getImageInfo();
}