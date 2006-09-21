/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.RadioButton;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class RadioButtonRenderer extends ComponentRenderer {
    private static final String RADIOBUTTON_CLASS = "tw_RadioButton";
    private static final String SET_CHECKED = "setChecked";
    
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(RADIOBUTTON_CLASS, wr, c, container);
		RadioButton rb = (RadioButton)c;
        addClientSideProperty(RadioButton.PROPERTY_CHECKED);
        addInitProperty(RadioButton.PROPERTY_TEXT, rb.getText());
        addInitProperty(RadioButton.PROPERTY_CHECKED, rb.isChecked());
        super.render(wr, c, container);
	}
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        super.propertyChange(pce);
        Object newValue = pce.getNewValue();        

        if (name.equals(RadioButton.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, newValue);
        } else if (name.equals(RadioButton.PROPERTY_CHECKED)) {
            postClientEvent(SET_CHECKED, newValue);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        RadioButton rb = (RadioButton)comp;
        
        if (name.equals(RadioButton.PROPERTY_CHECKED)) {
            setPropertyChangeIgnored(RadioButton.PROPERTY_CHECKED, true);
            rb.setChecked(Boolean.valueOf(value).booleanValue());
            setPropertyChangeIgnored(RadioButton.PROPERTY_CHECKED, false);
        } else {
            super.componentChange(event);
        }
    }    
}
