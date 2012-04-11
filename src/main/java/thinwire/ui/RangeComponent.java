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
