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
import thinwire.ui.DropDown;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
public final class DropDownRenderer extends MaskEditorComponentRenderer {
    private static final String DROPDOWN_CLASS = "tw_DropDown";
    private static final String SET_EDIT_ALLOWED = "setEditAllowed";
    private static final String SET_COMPONENT = "setComponent";
    private static final int MIN_SIZE = 25;
    private ComponentRenderer ddcr;

	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
	    init(DROPDOWN_CLASS, wr, c, container);
	    DropDown dd = (DropDown)c;        
        addInitProperty(DropDown.PROPERTY_EDIT_ALLOWED, dd.isEditAllowed());
        super.render(wr, c, container);
        Component ddc = dd.getComponent();
        if (ddc.getWidth() == 0 || ddc.getWidth() < dd.getWidth()) ddc.setWidth(dd.getView().getOptimalWidth());
        if (ddc.getHeight() == 0 || ddc.getHeight() < MIN_SIZE) ddc.setHeight(dd.getView().getOptimalHeight());
        ddc.setVisible(false);
 
        //TODO REFRESH Is this ok in a refresh scenario?
        ddcr = (ComponentRenderer)wr.ai.getRenderer(ddc);
        ddcr.setPropertyChangeIgnored(Component.PROPERTY_FOCUS, true);
        ddcr.setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);
        ddcr.setPropertyChangeIgnored(Component.PROPERTY_X, true);
        ddcr.setPropertyChangeIgnored(Component.PROPERTY_Y, true);
        ddcr.render(wr, ddc, this);
        
        postClientEvent(SET_COMPONENT, wr.ai.getComponentId(ddc));
	}
    
    void destroy() {
        super.destroy();
        ddcr.destroy();
        ddcr = null;
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();

        if (name.equals(DropDown.PROPERTY_EDIT_ALLOWED)) {
            postClientEvent(SET_EDIT_ALLOWED, newValue);
        } else if (name.equals(DropDown.PROPERTY_COMPONENT)) {
            if (ddcr != null) ddcr.destroy();
            DropDown dd = (DropDown) comp;
            Component ddc = dd.getComponent();
            if (ddc.getWidth() == 0 || ddc.getWidth() < dd.getWidth()) ddc.setWidth(dd.getView().getOptimalWidth());
            if (ddc.getHeight() == 0 || ddc.getHeight() < MIN_SIZE) ddc.setHeight(dd.getView().getOptimalHeight());
            ddc.setVisible(false);
            ddcr = (ComponentRenderer)wr.ai.getRenderer(ddc);
            ddcr.setPropertyChangeIgnored(Component.PROPERTY_FOCUS, true);
            ddcr.setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);
            ddcr.setPropertyChangeIgnored(Component.PROPERTY_X, true);
            ddcr.setPropertyChangeIgnored(Component.PROPERTY_Y, true);
            ddcr.render(wr, ddc, this);
            postClientEvent(SET_COMPONENT, wr.ai.getComponentId(ddc));
        } else {
            super.propertyChange(pce);
        }
    }
}
