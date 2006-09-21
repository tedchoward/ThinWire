/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.Hyperlink;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class HyperlinkRenderer extends ComponentRenderer {	
    private static final String HYPERLINK_CLASS = "tw_Hyperlink";
    private static final String SET_LOCATION = "setLocation";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(HYPERLINK_CLASS, wr, c, container);
        Hyperlink hl = (Hyperlink)c;
        addInitProperty(Hyperlink.PROPERTY_TEXT, hl.getText());
        addInitProperty(Hyperlink.PROPERTY_LOCATION, getLocation(hl.getLocation()));
        super.render(wr, c, container);
    }
    
    private String getLocation(String value) {
        if (value.length() > 0) value = getQualifiedURL(value);
        return value;
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();        

        if (name.equals(Hyperlink.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, newValue);
        } else if (name.equals(Hyperlink.PROPERTY_LOCATION)) {
            postClientEvent(SET_LOCATION, getLocation((String)newValue));
        } else {
            super.propertyChange(pce);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        
        if (name.equals(Hyperlink.ACTION_CLICK)) {
            ((Hyperlink)comp).fireAction(Hyperlink.ACTION_CLICK);
        } else {
            super.componentChange(event);
        }        
    }    
}
