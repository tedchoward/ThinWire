/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.DropDown;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class DropDownRenderer extends MaskEditorComponentRenderer {
    private static final String DROPDOWN_CLASS = "tw_DropDown";
    private static final String SET_EDIT_ALLOWED = "setEditAllowed";
    private ComponentRenderer ddcr;

	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
	    init(DROPDOWN_CLASS, wr, c, container);
	    DropDown dd = (DropDown)c;        
        addInitProperty(DropDown.PROPERTY_EDIT_ALLOWED, dd.isEditAllowed());
        super.render(wr, c, container);
        Component ddc = dd.getComponent();
        ddcr = wr.ai.getRenderer(ddc);
        ddcr.render(wr, ddc, this);
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
            Component ddc = ((DropDown)comp).getComponent();
            ddcr = wr.ai.getRenderer(ddc);
            ddcr.render(wr, ddc, this);
        } else {
            super.propertyChange(pce);
        }
    }
}
