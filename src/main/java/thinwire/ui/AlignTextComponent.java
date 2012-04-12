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

/**
 * @author Joshua J. Gertzen
 */
public interface AlignTextComponent extends TextComponent {
    /**
     * AlignX handles text alignment in screen elements.  Text can be left, right, or center.
     */
    public enum AlignX {LEFT, RIGHT, CENTER}
    
    public static final String PROPERTY_ALIGN_X = "alignX";

    /**
     * Get this label's text justification
     * @return this label's text justification
     */
    public AlignX getAlignX();    
    
    /**
     * Sets the text justification of the label (left, right, or center).
     * @param alignX (Default = AlignX.LEFT)
     * @throws IllegalArgumentException if alignX is null 
     */
    public void setAlignX(AlignX alignX);
}
