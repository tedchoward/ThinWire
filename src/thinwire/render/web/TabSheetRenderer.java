/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
   	            email: info@thinwire.com    ph: +1 (888) 644-6405
 	                        http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
#VERSION_HEADER#
*/
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.TabFolder;
import thinwire.ui.TabSheet;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.style.Border;

/**
 * @author Joshua J. Gertzen
 */
class TabSheetRenderer extends ContainerRenderer {
    private static final String TABSHEET_CLASS = "tw_TabSheet";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(TABSHEET_CLASS, wr, c, container);
        //a tabsheet does not support x, y, width, height, enabled or visible
        setPropertyChangeIgnored(Component.PROPERTY_X, true);
        setPropertyChangeIgnored(Component.PROPERTY_Y, true);
        setPropertyChangeIgnored(Component.PROPERTY_WIDTH, true);
        setPropertyChangeIgnored(Component.PROPERTY_HEIGHT, true);
        setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);
        setPropertyChangeIgnored(Border.PROPERTY_BORDER_TYPE, true);
        setPropertyChangeIgnored(Border.PROPERTY_BORDER_COLOR, true);
        setPropertyChangeIgnored(Border.PROPERTY_BORDER_SIZE, true);
        setPropertyChangeIgnored(Border.PROPERTY_BORDER_IMAGE, true);
        TabSheet ts = (TabSheet)c;
        addInitProperty(TabSheet.PROPERTY_TEXT, parseRichText(ts.getText()));
        addInitProperty(TabSheet.PROPERTY_IMAGE, wr.ai.addResourceMapping(ts.getImage()));
        addInitProperty("tabIndex", ((TabFolder)ts.getParent()).getChildren().indexOf(ts));        
        super.render(wr, c, container);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        Object newValue = pce.getNewValue();        
        
        if (name.equals(TabSheet.PROPERTY_IMAGE)) {
        	wr.ai.removeResourceMapping((String)pce.getOldValue());
            postClientEvent(SET_IMAGE, wr.ai.addResourceMapping((String)newValue));
        } else if (name.equals(TabSheet.PROPERTY_TEXT)) {
            postClientEvent(TextComponentRenderer.SET_TEXT, parseRichText((String)newValue));
        } else {
            super.propertyChange(pce);
        }
    }
    
    void destroy() {
    	wr.ai.removeResourceMapping(((TabSheet)comp).getImage());
    	super.destroy();
    }
}
