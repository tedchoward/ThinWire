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
import thinwire.ui.Dialog;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
public final class DialogRenderer extends WindowRenderer {
    private static final String DIALOG_CLASS = "tw_Dialog";
    private static final String SET_RESIZE_ALLOWED = "setResizeAllowed";
    private static final String SET_REPOSITION_ALLOWED = "setRepositionAllowed";
    private static final String SET_MODAL = "setModal";

	void render(WindowRenderer wr, Component c, ComponentRenderer container) {        
        init(DIALOG_CLASS, wr, c, container);
        Dialog d = (Dialog)c;
        addInitProperty(Dialog.PROPERTY_RESIZE_ALLOWED, d.isResizeAllowed());
        addInitProperty(Dialog.PROPERTY_REPOSITION_ALLOWED, d.isRepositionAllowed());
        
        if (!d.isModal()) {
            for (Dialog diag : wr.ai.getFrame().getDialogs()) {
                if (diag != d && diag.isModal()) {
                    throw new IllegalStateException("You cannot display a non-modal dialog while a modal dialog is visible.");
                }
            }
        }
        
        addInitProperty(Dialog.PROPERTY_MODAL, d.isModal());
        addClientSideProperty(Component.PROPERTY_X);
        addClientSideProperty(Component.PROPERTY_Y);
        addClientSideProperty(Component.PROPERTY_WIDTH);
        addClientSideProperty(Component.PROPERTY_HEIGHT);
        super.render(wr, c, container);
	}
    
    public void propertyChange(PropertyChangeEvent pce) {        
        if (pce.getPropertyName().equals(Dialog.PROPERTY_RESIZE_ALLOWED)) {            
            postClientEvent(SET_RESIZE_ALLOWED, pce.getNewValue());            
        } else if (pce.getPropertyName().equals(Dialog.PROPERTY_REPOSITION_ALLOWED)) {            
            postClientEvent(SET_REPOSITION_ALLOWED, pce.getNewValue());            
        } else if (pce.getPropertyName().equals(Dialog.PROPERTY_MODAL)) {            
            postClientEvent(SET_MODAL, pce.getNewValue());            
        } else {
            super.propertyChange(pce);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        Dialog d = (Dialog)comp;
        
        if (name.equals("closeClick")) {
            d.setVisible(false);
        } else {
            super.componentChange(event);
        }
    }
}
