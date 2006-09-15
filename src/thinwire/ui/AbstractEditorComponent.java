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
abstract class AbstractEditorComponent extends AbstractTextComponent implements EditorComponent {    
    private int maxLength = 0;    
    private int selectionBeginIndex;
    private int selectionEndIndex;
    private int cursorIndex;
    private AlignX alignX = AlignX.LEFT;    
    
    public void setSelectionRange(int selectionBeginIndex, int selectionEndIndex) {
        validateSelectionIndex(selectionBeginIndex);
        validateSelectionIndex(selectionEndIndex);
        if (selectionBeginIndex > selectionEndIndex) throw new IllegalArgumentException("selectionBeginIndex > selectionEndIndex");
        applySelectionRange(selectionBeginIndex, selectionEndIndex);
    }
    
    public int getSelectionBeginIndex() {
        return selectionBeginIndex;
    }
    
    public void setSelectionBeginIndex(int selectionBeginIndex) {
        validateSelectionIndex(selectionBeginIndex);
        
        if (selectionEndIndex < 0 || selectionEndIndex > getText().length() || selectionEndIndex <= selectionBeginIndex) {
            applySelectionRange(selectionBeginIndex, selectionBeginIndex);
        } else {
            applySelectionRange(selectionBeginIndex, selectionEndIndex);
        }
    }
    
    public int getSelectionEndIndex() {
        return selectionEndIndex;
    }
    
    public void setSelectionEndIndex(int selectionEndIndex) {
        validateSelectionIndex(selectionEndIndex);

        if (selectionBeginIndex < 0 || selectionBeginIndex > getText().length() || selectionBeginIndex >= selectionEndIndex) {
            applySelectionRange(selectionEndIndex, selectionEndIndex);
        } else {
            applySelectionRange(selectionBeginIndex, selectionEndIndex);
        }
    }
    
    public int getCursorIndex() {
        return cursorIndex;
    }

    public void setCursorIndex(int index) {
        setSelectionRange(index, index);
    }
    
    private void validateSelectionIndex(int index) {
        if (index < 0 || index > getText().length()) throw new IllegalArgumentException("[index=" + index + ",length=" + getText().length() + "]: " + "index < 0 || index > text.length()");        
    }
    
    private void applySelectionRange(int selectionBeginIndex, int selectionEndIndex) {        
        int oldSelectionBeginIndex = this.selectionBeginIndex;
        int oldSelectionEndIndex = this.selectionEndIndex;
        int oldCursorIndex = this.cursorIndex;
        this.selectionBeginIndex = selectionBeginIndex;
        this.selectionEndIndex = selectionEndIndex;
        this.cursorIndex = selectionBeginIndex == selectionEndIndex ? selectionEndIndex : -1;
        firePropertyChange(this, PROPERTY_SELECTION_BEGIN_INDEX, oldSelectionBeginIndex, selectionBeginIndex);            
        firePropertyChange(this, PROPERTY_SELECTION_END_INDEX, oldSelectionEndIndex, selectionEndIndex);                
        firePropertyChange(this, PROPERTY_CURSOR_INDEX, oldCursorIndex, cursorIndex);                
    }

    public void setMaxLength(int maxLength) {
        int oldMaxLength = this.maxLength;
        maxLength = ((maxLength < 0) ? 0 : maxLength);
        this.maxLength = maxLength;
        firePropertyChange(this, PROPERTY_MAX_LENGTH, oldMaxLength, maxLength);
    }

    public int getMaxLength() {
        return maxLength;
    }
    

    public AlignX getAlignX() {
        return alignX;
    }   

    public void setAlignX(AlignX alignX) {
        if (alignX == null) throw new IllegalArgumentException(PROPERTY_ALIGN_X + " == null");
        AlignX oldAlignX = this.alignX;
        this.alignX = alignX;
        firePropertyChange(this, PROPERTY_ALIGN_X, oldAlignX, alignX);
    }    
}