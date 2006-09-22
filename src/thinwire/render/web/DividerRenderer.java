/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Divider;
import thinwire.ui.Component;

/**
 * @author Joshua J. Gertzen
 */
final class DividerRenderer extends ComponentRenderer {
    private static final String DIVIDER_CLASS = "tw_Divider";

    public void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(DIVIDER_CLASS, wr, c, container);
        super.render(wr, c, container);
	}
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        
        if (name.equals(Divider.ACTION_CLICK)) {
            ((Divider)comp).fireAction(Divider.ACTION_CLICK);
        } else {
            super.componentChange(event);
        }        
    }        
}
