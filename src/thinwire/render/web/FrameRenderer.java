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

/**
 * @author Joshua J. Gertzen
 */
final class FrameRenderer extends WindowRenderer {  
    private static final String FRAME_CLASS = "tw_Frame";
    private WebApplication app;
    
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(FRAME_CLASS, wr, c, container);
        setPropertyChangeIgnored(Component.PROPERTY_X, true);
        setPropertyChangeIgnored(Component.PROPERTY_Y, true);
        setPropertyChangeIgnored(Component.PROPERTY_WIDTH, true);
        setPropertyChangeIgnored(Component.PROPERTY_HEIGHT, true);
        app = (WebApplication)WebApplication.current();
        super.render(wr, c, null);
	}
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        
        if (name.equals("frameSize")) {
            String[] ary = value.split(",");
            app.setPackagePrivateMember("innerWidth", comp, Integer.valueOf(ary[0]));
            app.setPackagePrivateMember("innerHeight", comp, Integer.valueOf(ary[1]));            
            app.setPackagePrivateMember("frameSize", comp, new Integer[]{Integer.valueOf(ary[2]), Integer.valueOf(ary[3])});
        } else {
            super.componentChange(event);
        }
    }    
}
