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

import thinwire.ui.Component;

/**
 * @author Joshua J. Gertzen
 */
final class FrameRenderer extends WindowRenderer {  
    private static final String FRAME_CLASS = "tw_Frame";
    
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(FRAME_CLASS, wr, c, container);
        setPropertyChangeIgnored(Component.PROPERTY_X, true);
        setPropertyChangeIgnored(Component.PROPERTY_Y, true);
        setPropertyChangeIgnored(Component.PROPERTY_WIDTH, true);
        setPropertyChangeIgnored(Component.PROPERTY_HEIGHT, true);
        super.render(wr, c, null);
	}
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        
        if (name.equals("frameSize")) {
            String[] ary = value.split(",");
            ai.setPackagePrivateMember("innerWidth", comp, Integer.valueOf(ary[0]));
            ai.setPackagePrivateMember("innerHeight", comp, Integer.valueOf(ary[1]));            
            ai.setPackagePrivateMember("frameSize", comp, new Integer[]{Integer.valueOf(ary[2]), Integer.valueOf(ary[3])});
        } else {
            super.componentChange(event);
        }
    }    
}
