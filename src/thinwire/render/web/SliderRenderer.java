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
import thinwire.ui.Slider;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Ted C. Howard
 */
public class SliderRenderer extends ComponentRenderer {
    private static final String SLIDER_CLASS = "tw_Slider";
    
    public void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(SLIDER_CLASS, wr, c, container);
        addInitProperty(Slider.PROPERTY_LENGTH, ((Slider) c).getLength());
        addInitProperty(Slider.PROPERTY_CURSOR_INDEX, ((Slider) c).getCursorIndex());
        super.render(wr, c, container);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        Object newValue = pce.getNewValue();
        String name = pce.getPropertyName();
        
        if (isPropertyChangeIgnored(name)) return;
        
        if (name.equals(Slider.PROPERTY_CURSOR_INDEX)) {
            postClientEvent("setCursorIndex", (Integer) newValue);
        } else if (name.equals(Slider.PROPERTY_LENGTH)) {
            postClientEvent("setLength", (Integer) newValue);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        
        if (name.equals(Slider.PROPERTY_CURSOR_INDEX)) {
            int value = Integer.parseInt((String) event.getValue());
            setPropertyChangeIgnored(name, value, true);
            ((Slider) comp).setCursorIndex(value);
        } else {
            setPropertyChangeIgnored(name, true);
            super.componentChange(event);
        }
        setPropertyChangeIgnored(name, false);
    }

}
