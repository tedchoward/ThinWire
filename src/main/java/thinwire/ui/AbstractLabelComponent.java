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
abstract class AbstractLabelComponent<C extends LabelComponent> extends AbstractTextComponent<C> implements LabelComponent {
    private AlignX alignX = AlignX.LEFT;
	private Component labelFor = null;
    private boolean wrapText;
        
    public AlignX getAlignX() {
        return alignX;
    }

    public void setAlignX(AlignX alignX) {
        if (alignX == null) throw new IllegalArgumentException(PROPERTY_ALIGN_X + " == null");
        AlignX oldAlignX = this.alignX;
        this.alignX = alignX;
        firePropertyChange(this, PROPERTY_ALIGN_X, oldAlignX, alignX);
    }    
        
	public Component getLabelFor() {
        return labelFor;
    }

    @SuppressWarnings("unchecked")
	public void setLabelFor(Component labelFor) {
        Component oldLabelFor = this.labelFor;
        this.labelFor = labelFor;
        if (labelFor != null) ((AbstractComponent)labelFor).setLabel(this);
        if (oldLabelFor != null) ((AbstractComponent) oldLabelFor).setLabel(null);
        firePropertyChange(this, PROPERTY_LABEL_FOR, oldLabelFor, labelFor);
    }
    
    public boolean isWrapText() {
        return wrapText;
    }
    
    public void setWrapText(boolean wrapText) {
        boolean oldWrap = this.wrapText;
        this.wrapText = wrapText;
        if (this.wrapText != oldWrap) firePropertyChange(this, PROPERTY_WRAP_TEXT, oldWrap, this.wrapText);
    }
}
