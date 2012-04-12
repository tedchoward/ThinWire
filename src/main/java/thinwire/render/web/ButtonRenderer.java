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
package thinwire.render.web;

import thinwire.ui.Button;
import thinwire.ui.Component;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class ButtonRenderer extends TextComponentRenderer {
    private static final String BUTTON_CLASS = "tw_Button";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(BUTTON_CLASS, wr, c, container);
        Button b = (Button)c;
        addInitProperty(Button.PROPERTY_IMAGE, wr.ai.addResourceMapping(b.getImage()));
        addInitProperty(Button.PROPERTY_STANDARD, b.isStandard());        
        super.render(wr, c, container);
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();        

        if (name.equals(Button.PROPERTY_IMAGE)) {
        	wr.ai.removeResourceMapping((String)pce.getOldValue());
            postClientEvent(SET_IMAGE, wr.ai.addResourceMapping((String)newValue));
        } else {
            super.propertyChange(pce);
        }
    }
    
    void destroy() {
    	wr.ai.removeResourceMapping(((Button)comp).getImage());
    	super.destroy();
    }
}
