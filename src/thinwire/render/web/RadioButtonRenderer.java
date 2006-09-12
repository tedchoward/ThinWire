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

import thinwire.ui.Component;
import thinwire.ui.RadioButton;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class RadioButtonRenderer extends ComponentRenderer {
    private static final String RADIOBUTTON_CLASS = "tw_RadioButton";
    private static final String SET_CHECKED = "setChecked";
    
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        jsClass = RADIOBUTTON_CLASS;
		RadioButton rb = (RadioButton)c;
        addInitProperty("text", rb.getText());
        addInitProperty("checked", rb.isChecked());
        super.render(wr, c, container);
	}
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        super.propertyChange(pce);
        Object newValue = pce.getNewValue();        

        if (name.equals(RadioButton.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, newValue);
        } else if (name.equals(RadioButton.PROPERTY_CHECKED)) {
            postClientEvent(SET_CHECKED, newValue);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        RadioButton rb = (RadioButton)comp;
        
        if (name.equals(RadioButton.PROPERTY_CHECKED)) {
            setPropertyChangeIgnored(RadioButton.PROPERTY_CHECKED, true);
            rb.setChecked(Boolean.valueOf(value).booleanValue());
            setPropertyChangeIgnored(RadioButton.PROPERTY_CHECKED, false);
        } else {
            super.componentChange(event);
        }
    }    
}
