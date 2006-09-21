/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.Image;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class ImageRenderer extends ComponentRenderer {
    private static final String IMAGE_CLASS = "tw_Image";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(IMAGE_CLASS, wr, c, container);
        addInitProperty(Image.PROPERTY_IMAGE, getRemoteNameForLocalFile(((Image)c).getImage()));
        //TODO: Since Image can be clicked, it should support having it's enabled state toggled
        setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);        
        super.render(wr, c, container);                
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;        
        
        if (name.equals(Image.PROPERTY_IMAGE)) {
            postClientEvent(SET_IMAGE, getRemoteNameForLocalFile((String)pce.getNewValue()));
        } else {
            super.propertyChange(pce);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        
        if (name.equals(Image.ACTION_CLICK)) {
            ((Image)comp).fireAction(Image.ACTION_CLICK);
        } else {
            super.componentChange(event);
        }        
    }        
}
