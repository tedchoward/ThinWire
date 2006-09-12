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
package thinwire.render.web;

import thinwire.ui.AlignX;
import thinwire.ui.Component;
import thinwire.ui.DropDown;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class DropDownRenderer extends ComponentRenderer {
    private static final String DROPDOWNGRIDBOX_CLASS = "tw_DropDownGridBox";
    private static final String SET_EDIT_ALLOWED = "setEditAllowed";
    private GridBoxRenderer gbr;

	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        jsClass = DROPDOWNGRIDBOX_CLASS;
	    DropDown dd = (DropDown)c;        
	    addInitProperty(DropDown.PROPERTY_TEXT, GridBoxRenderer.getValue(dd.getText(), null));
        addInitProperty(DropDown.PROPERTY_EDIT_ALLOWED, dd.isEditAllowed());
        addInitProperty(DropDown.PROPERTY_EDIT_MASK, getEditMaskTextLength(dd));
        addInitProperty(DropDown.PROPERTY_ALIGN_X, dd.getAlignX().name().toLowerCase());
        super.render(wr, c, container);        
        (gbr = new GridBoxRenderer()).render(wr, dd.getComponent(), this);
	}
    
    void destroy() {
        super.destroy();
        gbr.destroy();
        gbr = null;
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        Object newValue = pce.getNewValue();

        if (name.equals(DropDown.PROPERTY_TEXT)) {
            if (isPropertyChangeIgnored(name, newValue)) return;
            postClientEvent(SET_TEXT, newValue);
        } else {
            if (isPropertyChangeIgnored(name)) return;

            if (name.equals(DropDown.PROPERTY_EDIT_ALLOWED)) {
                postClientEvent(SET_EDIT_ALLOWED, newValue);
            } else if (name.equals(DropDown.PROPERTY_EDIT_MASK) || name.equals(DropDown.PROPERTY_MAX_LENGTH)) {
                postClientEvent(SET_EDIT_MASK, getEditMaskTextLength(comp));
            } else if (name.equals(DropDown.PROPERTY_ALIGN_X)) {
                postClientEvent(SET_ALIGN_X, ((AlignX)newValue).name().toLowerCase());
            } else if (name.equals(DropDown.PROPERTY_SELECTION_BEGIN_INDEX) || name.equals(DropDown.PROPERTY_SELECTION_END_INDEX)) {
                DropDown dd = (DropDown)comp;
                postClientEvent(SET_SELECTION_RANGE, new Integer(dd.getSelectionBeginIndex()), new Integer(dd.getSelectionEndIndex()));
            } else if (name.equals(DropDown.PROPERTY_COMPONENT)) {
                if (gbr != null) gbr.destroy();
                gbr = new GridBoxRenderer();
                gbr.render(wr, ((DropDown)comp).getComponent(), this);
            } else {
                super.propertyChange(pce);
            }
        }
    }

    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        DropDown dd = (DropDown)comp;
        String value = (String)event.getValue();
        
        if (name.equals(DropDown.PROPERTY_TEXT)) {
            setPropertyChangeIgnored(name, value, true);
            dd.setText(value);
            setPropertyChangeIgnored(name, false);
        } else {
            if (name.equals("selectionRange")) {
                int index = value.indexOf(',');
                int beginIndex = Integer.parseInt(value.substring(0, index));
                int endIndex = Integer.parseInt(value.substring(index + 1));
                String text = dd.getText();

                if (beginIndex >= 0 && beginIndex <= text.length() && endIndex >= 0 && endIndex <= text.length()) {
                    setPropertyChangeIgnored(DropDown.PROPERTY_SELECTION_BEGIN_INDEX, true);
                    setPropertyChangeIgnored(DropDown.PROPERTY_SELECTION_END_INDEX, true);
                    dd.setSelectionRange(beginIndex, endIndex);
                    setPropertyChangeIgnored(DropDown.PROPERTY_SELECTION_BEGIN_INDEX, false);
                    setPropertyChangeIgnored(DropDown.PROPERTY_SELECTION_END_INDEX, false);
                }
            } else {
                super.componentChange(event);
            }
        }
    }
}
