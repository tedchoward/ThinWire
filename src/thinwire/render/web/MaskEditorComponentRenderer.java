/*
 * Created on Jul 8, 2006
  */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.MaskEditorComponent;
import thinwire.ui.event.PropertyChangeEvent;

abstract class MaskEditorComponentRenderer extends EditorComponentRenderer {
    private static final String SET_EDIT_MASK = "setEditMask";    
    
    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        MaskEditorComponent med = (MaskEditorComponent)c;
        addInitProperty(MaskEditorComponent.PROPERTY_EDIT_MASK, getEditMaskTextLength(med));
        super.render(wr, c, container);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();       
        if (isPropertyChangeIgnored(name)) return;

        if (name.equals(MaskEditorComponent.PROPERTY_EDIT_MASK) || name.equals(MaskEditorComponent.PROPERTY_MAX_LENGTH)) {
            postClientEvent(SET_EDIT_MASK, getEditMaskTextLength((MaskEditorComponent)comp));
        } else {
            super.propertyChange(pce);
        }
    }
    
    private String getEditMaskTextLength(MaskEditorComponent maskEditor) {
        String editMask = maskEditor.getEditMask();        
        int maxLength = maskEditor.getMaxLength();
        if (editMask.equals("") && maxLength > 0) editMask = "<=" + maxLength;
        return editMask;
    }    
}
