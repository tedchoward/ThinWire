/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.CheckBox;
import thinwire.ui.Component;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class CheckBoxRenderer extends ComponentRenderer {
    private static final String CHECKBOX_CLASS = "tw_CheckBox";
    private static final String SET_CHECKED = "setChecked";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(CHECKBOX_CLASS, wr, c, container);
        CheckBox cb = (CheckBox)c;
        addClientSideProperty(CheckBox.PROPERTY_CHECKED);
        addInitProperty(CheckBox.PROPERTY_TEXT, cb.getText());
        addInitProperty(CheckBox.PROPERTY_CHECKED, cb.isChecked());
        super.render(wr, c, container);
	}
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();        

        if (name.equals(CheckBox.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, newValue);
        } else if (name.equals(CheckBox.PROPERTY_CHECKED)) {
            postClientEvent(SET_CHECKED, newValue);
        } else {
            super.propertyChange(pce);           
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        CheckBox cb = (CheckBox)comp;
        
        if (name.equals(CheckBox.PROPERTY_CHECKED)) {
            setPropertyChangeIgnored(CheckBox.PROPERTY_CHECKED, true);
            cb.setChecked(Boolean.valueOf(value).booleanValue());
            setPropertyChangeIgnored(CheckBox.PROPERTY_CHECKED, false);
        } else {
            super.componentChange(event);
        }
    }    
}
