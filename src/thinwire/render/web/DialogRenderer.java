/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.Dialog;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class DialogRenderer extends WindowRenderer {
    private static final String DIALOG_CLASS = "tw_Dialog";
    private static final String SET_RESIZE_ALLOWED = "setResizeAllowed";

	void render(WindowRenderer wr, Component c, ComponentRenderer container) {        
        init(DIALOG_CLASS, wr, c, container);
        addInitProperty(Dialog.PROPERTY_RESIZE_ALLOWED, ((Dialog)c).isResizeAllowed());
        addClientSideProperty(Component.PROPERTY_X);
        addClientSideProperty(Component.PROPERTY_Y);
        addClientSideProperty(Component.PROPERTY_WIDTH);
        addClientSideProperty(Component.PROPERTY_HEIGHT);
        super.render(wr, c, container);
	}
    
    public void propertyChange(PropertyChangeEvent pce) {        
        if (pce.getPropertyName().equals(Dialog.PROPERTY_RESIZE_ALLOWED)) {            
            postClientEvent(SET_RESIZE_ALLOWED, pce.getNewValue());            
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
