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
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface EditorComponent extends AlignTextComponent {
    public static final String PROPERTY_SELECTION_BEGIN_INDEX = "selectionBeginIndex";
    public static final String PROPERTY_SELECTION_END_INDEX = "selectionEndIndex";    
    public static final String PROPERTY_CURSOR_INDEX = "cursorIndex";
    public static final String PROPERTY_MAX_LENGTH = "maxLength";    
    
    /**
     * Sets the range of the selection<br>
     * <b>Events:</b>
     * <p>
     * If the prior values and new values differ, setting this property causes 2 <code>PropertyChangeEvent</code>s ( propertyName = PROPERTY_SELECTION_BEGIN_INDEX and propertyName = PROPERTY_SELECTION_END_INDEX ) to be generated.
     * </p>
     * @param selectionBeginIndex the index of the start of the selection
     * @param selectionEndIndex the index of the end of the selection
     * @see #setSelectionBeginIndex(int)
     * @see #setSelectionEndIndex(int)
     * @see #PROPERTY_SELECTION_BEGIN_INDEX
     * @see #PROPERTY_SELECTION_END_INDEX
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void setSelectionRange(int selectionBeginIndex, int selectionEndIndex);
    
    /**
     * Returns the index of the beginning of the selection
     * @return the index at the start of the selection or -1 if there is no selection
     * @see #setSelectionBeginIndex()
     */
    public int getSelectionBeginIndex();
    
    /**
     * Sets the index of the beginning of a selection<br>
     * <b>Events:</b>
     * <p>
     * If the prior values and new values differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_SELECTION_BEGIN_INDEX ) to be generated.
     * </p>
     * @param selectionBeginIndex the index of the start of the selection
     * @see #getSelectionBeginIndex()
     * @see #setSelectionRange(int, int)
     * @see #PROPERTY_SELECTION_BEGIN_INDEX
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void setSelectionBeginIndex(int selectionBeginIndex);
    
    /**
     * Returns the index of the end of the selection
     * @return the index of the end of the selection or -1 if there is no selection
     * @see #setSelectionEndIndex(int)
     */
    public int getSelectionEndIndex();
    
    /**
     * Sets the index of the end of the selection<br>
     * <b>Events:</b>
     * <p>
     * If the prior values and new values differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_SELECTION_END_INDEX ) to be generated.
     * </p>
     * @param selectionEndIndex the index of the end of the selection
     * @see #getSelectionEndIndex()
     * @see #setSelectionRange(int, int)
     * @see #PROPERTY_SELECTION_END_INDEX
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void setSelectionEndIndex(int selectionEndIndex);
    
    /**
     * Retuns the index of the current cursor position
     * @return the index of the cursor or -1 if there is a selection
     * @see #setCursorIndex(int)
     */
    public int getCursorIndex();
    
    /**
     * Sets the index of the cursor position<br>
     * <b>Events:</b>
     * <p>
     * If the prior values and new values differ, setting this property causes a <code>PropertyChangeEvent</code> ( propertyName = PROPERTY_CURSOR_INDEX ) to be generated.
     * </p>
     * @param index the index of the cursor
     * @see #getCursorIndex()
     * @see #PROPERTY_CURSOR_INDEX
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    public void setCursorIndex(int index);
    
    /**
     * Sets the editor max length.
     * @param maxLength The maxLength to set.
     */
    public void setMaxLength(int maxLength);

    /**
     * Gets the TextField's max length.
     * @return Returns the maxLength.
     */
    public int getMaxLength();    
}