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
import thinwire.ui.Hyperlink;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class HyperlinkRenderer extends LabelComponentRenderer {	
    private static final String HYPERLINK_CLASS = "tw_Hyperlink";
    private static final String SET_LOCATION = "setLocation";
    private static final String SET_TARGET = "setTarget";
    private static final String SET_VISIBLE_CHROME = "setVisibleChrome";
    private static final String SET_RESIZE_ALLOWED = "setResizeAllowed";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(HYPERLINK_CLASS, wr, c, container);
        Hyperlink hl = (Hyperlink)c;
        addInitProperty(Hyperlink.PROPERTY_LOCATION, wr.ai.addResourceMapping(hl.getLocation()));
        addInitProperty(Hyperlink.PROPERTY_TARGET, hl.getTarget());
        addInitProperty(Hyperlink.PROPERTY_VISIBLE_CHROME, hl.isVisibleChrome());
        addInitProperty(Hyperlink.PROPERTY_RESIZE_ALLOWED, hl.isResizeAllowed());
        super.render(wr, c, container);
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();        

        if (name.equals(Hyperlink.PROPERTY_LOCATION)) {
        	wr.ai.removeResourceMapping((String)pce.getOldValue());
            postClientEvent(SET_LOCATION, wr.ai.addResourceMapping((String)newValue));
        } else if (name.equals(Hyperlink.PROPERTY_TARGET)) {
            postClientEvent(SET_TARGET, newValue);
        } else if (name.equals(Hyperlink.PROPERTY_VISIBLE_CHROME)) {
            postClientEvent(SET_VISIBLE_CHROME, newValue);
        } else if (name.equals(Hyperlink.PROPERTY_RESIZE_ALLOWED)) {
            postClientEvent(SET_RESIZE_ALLOWED, newValue);
        } else {
            super.propertyChange(pce);
        }
    }
    
    void destroy() {
    	wr.ai.removeResourceMapping(((Hyperlink)comp).getLocation());
    	super.destroy();
    }
}
