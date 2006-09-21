/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.TextArea;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class TextAreaRenderer extends EditorComponentRenderer {
    private static final String TEXTAREA_CLASS = "tw_TextArea";
    private static final String SET_MAX_LENGTH = "setMaxLength";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(TEXTAREA_CLASS, wr, c, container);
        TextArea ta = (TextArea)c;
        addInitProperty(TextArea.PROPERTY_MAX_LENGTH, ta.getMaxLength());
        super.render(wr, c, container);        		
	}
        
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();        
        if (isPropertyChangeIgnored(name)) return;
        
        if (name.equals(TextArea.PROPERTY_MAX_LENGTH)) {
            postClientEvent(SET_MAX_LENGTH, pce.getNewValue());            
        } else {
            super.propertyChange(pce);
        }
    }
}
