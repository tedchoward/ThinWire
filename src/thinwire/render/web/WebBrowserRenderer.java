/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.WebBrowser;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class WebBrowserRenderer extends ComponentRenderer {	
    private static final String WEBBROWSER_CLASS = "tw_WebBrowser";
    private static final String SET_LOCATION = "setLocation";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(WEBBROWSER_CLASS, wr, c, container); 
        addInitProperty(WebBrowser.PROPERTY_LOCATION, getQualifiedURL(((WebBrowser)c).getLocation()));        
        super.render(wr, c, container);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();        

        if (name.equals(WebBrowser.PROPERTY_LOCATION)) {
            postClientEvent(SET_LOCATION, getQualifiedURL((String)newValue));
        } else {
            super.propertyChange(pce);
        }
    }    
}
