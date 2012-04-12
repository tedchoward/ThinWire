/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
