/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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
package thinwire.ui;

abstract class AbstractRangeComponent extends AbstractComponent implements RangeComponent {
    private int length;
    private int currentIndex;
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public void setCurrentIndex(int currentIndex) {
        if (currentIndex < 0 || currentIndex >= length) throw new IllegalArgumentException("currentIndex < 0 || currentIndex >= length");
        int oldIndex = this.currentIndex;
        this.currentIndex = currentIndex;
        firePropertyChange(this, PROPERTY_CURRENT_INDEX, oldIndex, this.currentIndex);
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength(int length) {
        if (length < 1) throw new IllegalArgumentException("length < 1");
        int oldLength = this.length;
        this.length = length;
        firePropertyChange(this, PROPERTY_LENGTH, oldLength, this.length);
    }
}
