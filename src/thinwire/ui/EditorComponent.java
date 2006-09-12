/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface EditorComponent extends TextComponent {
    public static final String PROPERTY_SELECTION_BEGIN_INDEX = "selectionBeginIndex";
    public static final String PROPERTY_SELECTION_END_INDEX = "selectionEndIndex";    
    public static final String PROPERTY_CURSOR_INDEX = "cursorIndex";
    public static final String PROPERTY_MAX_LENGTH = "maxLength";    
    
    public void setSelectionRange(int selectionBeginIndex, int selectionEndIndex);
    public int getSelectionBeginIndex();
    public void setSelectionBeginIndex(int selectionBeginIndex);
    public int getSelectionEndIndex();
    public void setSelectionEndIndex(int selectionEndIndex);
    public int getCursorIndex();
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