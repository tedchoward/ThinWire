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
