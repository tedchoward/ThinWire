/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

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
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.LabelComponent;
import thinwire.ui.AlignTextComponent.AlignX;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
public abstract class LabelComponentRenderer extends TextComponentRenderer {
    private static final String SET_WRAP_TEXT = "setWrapText";

    protected void render(WindowRenderer wr, Component c, ComponentRenderer container) {
		LabelComponent l = (LabelComponent)c;
        addInitProperty(LabelComponent.PROPERTY_ALIGN_X, l.getAlignX().name().toLowerCase());
        addInitProperty(LabelComponent.PROPERTY_WRAP_TEXT, l.isWrapText());
        super.render(wr, c, container);                
	}
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();
        
        if (name.equals(LabelComponent.PROPERTY_ALIGN_X)) {
            postClientEvent(SET_ALIGN_X, ((AlignX)newValue).name().toLowerCase());
        } else if (name.equals(LabelComponent.PROPERTY_WRAP_TEXT)) {
            postClientEvent(SET_WRAP_TEXT, newValue);
        } else {
            super.propertyChange(pce);
        }
    }    
}
