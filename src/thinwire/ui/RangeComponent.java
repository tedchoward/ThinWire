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

public interface RangeComponent extends Component {
    public static final String PROPERTY_LENGTH = "length";
    public static final String PROPERTY_CURRENT_INDEX = "currentIndex";
    
    /**
     * Retuns the current index
     * @return the index 
     * @see #setCurrentIndex(int)
     */
    public int getCurrentIndex();
    
    /**
     * Sets the current index<br>
     * <b>Events:</b>
     * <p>
     * If the prior values and new values differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_CURRENT_INDEX ) to be generated.
     * </p>
     * @param index the index of the cursor
     * @see #getCurrentIndex()
     * @see #PROPERTY_CURRENT_INDEX
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void setCurrentIndex(int currentIndex);
    
    /**
     * Returns the length of the range
     * @return the length of the range
     * @see #setLength(int)
     */
    public int getLength();
    
    /**
     * Sets the length of the range.  The length is the total number of increments.<br>
     * <b>Events:</b>
     * <p>
     * If the prior values and new values differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_LENGTH ) to be generated.
     * </p>
     * @param length the number of increments
     * @see #getLength()
     * @see #PROPERTY_LENGTH
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void setLength(int length);
}
