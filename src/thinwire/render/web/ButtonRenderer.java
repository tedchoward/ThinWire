/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Button;
import thinwire.ui.Component;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class ButtonRenderer extends ComponentRenderer {
    private static final String BUTTON_CLASS = "tw_Button";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(BUTTON_CLASS, wr, c, container);
        Button b = (Button)c;
        addInitProperty(Button.PROPERTY_TEXT, b.getText());
        addInitProperty(Button.PROPERTY_IMAGE, getRemoteNameForLocalFile(b.getImage()));
        addInitProperty(Button.PROPERTY_STANDARD, b.isStandard());        
        super.render(wr, c, container);
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();        

        if (name.equals(Button.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, newValue);
        } else if (name.equals(Button.PROPERTY_IMAGE)) {
            postClientEvent(SET_IMAGE, getRemoteNameForLocalFile((String)newValue));
        } else {
            super.propertyChange(pce);
        }
    }   
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        Button b = (Button)comp;
        
        if (name.equals(Button.ACTION_CLICK)) {
            b.fireAction(Button.ACTION_CLICK);
        } else {
            super.componentChange(event);
        }
    }        	
}
