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
import thinwire.ui.Label;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class LabelRenderer extends ComponentRenderer {
    private static final String LABEL_CLASS = "tw_Label";
    
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        jsClass = LABEL_CLASS;
        //TODO: Since Label can be clicked, it should support having it's enabled state toggled
        setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);                
		Label l = (Label)c;
        addInitProperty(Label.PROPERTY_TEXT, l.getText());
        addInitProperty(Label.PROPERTY_ALIGN_X, l.getAlignX().name().toLowerCase());
        super.render(wr, c, container);                
	}
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        
        if (name.equals(Component.PROPERTY_FOCUS)) {
            String coolBean = name;
            coolBean.indexOf('0');
        }
        
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();
        
        if (name.equals(Label.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, newValue);
        } else if (name.equals(Label.PROPERTY_ALIGN_X)) {
            postClientEvent(SET_ALIGN_X, ((AlignX)newValue).name().toLowerCase());
        } else {
            super.propertyChange(pce);
        }
    }    
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        
        if (name.equals(Label.ACTION_CLICK)) {
            ((Label)comp).fireAction(Label.ACTION_CLICK);
        } else {
            super.componentChange(event);
        }        
    }        
}
