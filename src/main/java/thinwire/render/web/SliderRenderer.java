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
import thinwire.ui.Slider;

/**
 * @author Ted C. Howard
 */
public final class SliderRenderer extends RangeComponentRenderer {
    private static final String SLIDER_CLASS = "tw_Slider";
    
    public void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(SLIDER_CLASS, wr, c, container);
        addClientSideProperty(Slider.PROPERTY_CURRENT_INDEX);
        super.render(wr, c, container);
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        Slider s = (Slider) comp;
        
        if (name.equals(Slider.PROPERTY_CURRENT_INDEX)) {
            int value = Integer.parseInt((String) event.getValue());
            setPropertyChangeIgnored(name, true);
            
            if (s.getLength() == 1) {
                s.setCurrentIndex(value - 1);
            } else {
                s.setCurrentIndex(value);
            }

            setPropertyChangeIgnored(name, false);
        } else {
            super.componentChange(event);
        }
    }

}
