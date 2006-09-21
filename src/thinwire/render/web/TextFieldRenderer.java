/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.TextField;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class TextFieldRenderer extends MaskEditorComponentRenderer {
    private static final String TEXTFIELD_CLASS = "tw_TextField";
    private static final String SET_INPUT_HIDDEN = "setInputHidden";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(TEXTFIELD_CLASS, wr, c, container);
		TextField tf = (TextField)c;
        addInitProperty(TextField.PROPERTY_INPUT_HIDDEN, tf.isInputHidden());
        super.render(wr, c, container);
	}
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();       
        if (isPropertyChangeIgnored(name)) return;

        if (name.equals(TextField.PROPERTY_INPUT_HIDDEN)) {
            postClientEvent(SET_INPUT_HIDDEN, pce.getNewValue());
        } else {
            super.propertyChange(pce);
        }
    }
}
