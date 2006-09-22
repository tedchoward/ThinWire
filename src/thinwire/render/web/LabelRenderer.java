/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.AlignX;
import thinwire.ui.Component;
import thinwire.ui.Label;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class LabelRenderer extends ComponentRenderer {
    private static final String LABEL_CLASS = "tw_Label";
    private static final String SET_WRAP_TEXT = "setWrapText";
    
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(LABEL_CLASS, wr, c, container);
        //TODO: Since Label can be clicked, it should support having it's enabled state toggled
        setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);                
		Label l = (Label)c;
        addInitProperty(Label.PROPERTY_TEXT, l.getText());
        addInitProperty(Label.PROPERTY_ALIGN_X, l.getAlignX().name().toLowerCase());
        addInitProperty(Label.PROPERTY_WRAP_TEXT, l.isWrapText());
        super.render(wr, c, container);                
	}
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();
        
        if (name.equals(Label.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, newValue);
        } else if (name.equals(Label.PROPERTY_ALIGN_X)) {
            postClientEvent(SET_ALIGN_X, ((AlignX)newValue).name().toLowerCase());
        } else if (name.equals(Label.PROPERTY_WRAP_TEXT)) {
            postClientEvent(SET_WRAP_TEXT, newValue);
        } else {
            super.propertyChange(pce);
        }
    }    
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        
        if (name.equals(Label.ACTION_CLICK)) {
            ((Label)comp).fireAction(Label.ACTION_CLICK);
        } else {
            super.componentChange(event);
        }        
    }        
}
