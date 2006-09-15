/*
 * Created on Jul 8, 2006
  */
package thinwire.render.web;

import thinwire.ui.AlignX;
import thinwire.ui.Component;
import thinwire.ui.EditorComponent;
import thinwire.ui.event.PropertyChangeEvent;

abstract class EditorComponentRenderer extends ComponentRenderer {
    private static final String SET_SELECTION_RANGE = "setSelectionRange";    
    private static final String VIEW_STATE_SELECTION_RANGE = "selectionRange";
    
    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        EditorComponent ed = (EditorComponent)c;
        addClientSideProperty(EditorComponent.PROPERTY_TEXT);
        addClientSideProperty(EditorComponent.PROPERTY_SELECTION_BEGIN_INDEX, VIEW_STATE_SELECTION_RANGE);
        addClientSideProperty(EditorComponent.PROPERTY_SELECTION_END_INDEX, VIEW_STATE_SELECTION_RANGE);
        addClientSideProperty(EditorComponent.PROPERTY_CURSOR_INDEX, VIEW_STATE_SELECTION_RANGE);
        addInitProperty(EditorComponent.PROPERTY_TEXT, ed.getText());
        addInitProperty(EditorComponent.PROPERTY_ALIGN_X, ed.getAlignX().name().toLowerCase());
        super.render(wr, c, container);
    }    
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;

        if (name.equals(EditorComponent.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, pce.getNewValue());
        } else if (name.equals(EditorComponent.PROPERTY_ALIGN_X)) {
            postClientEvent(SET_ALIGN_X, ((AlignX)pce.getNewValue()).name().toLowerCase());
        } else if (name.equals(EditorComponent.PROPERTY_SELECTION_BEGIN_INDEX) || name.equals(EditorComponent.PROPERTY_SELECTION_END_INDEX)) {
            //TODO: This may fire twice if the begin index and end index change at the same time.
            EditorComponent ed = (EditorComponent)comp;
            postClientEvent(SET_SELECTION_RANGE, new Integer(ed.getSelectionBeginIndex()), new Integer(ed.getSelectionEndIndex()));
        } else {
            super.propertyChange(pce);
        }
    }    
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        EditorComponent ed = (EditorComponent)comp;
        
        if (name.equals(EditorComponent.PROPERTY_TEXT)) {
            setPropertyChangeIgnored(name, value, true);
            ed.setText(value);
            setPropertyChangeIgnored(name, false);
        } else {
            if (name.equals(VIEW_STATE_SELECTION_RANGE)) {
                int index = value.indexOf(',');
                int beginIndex = Integer.parseInt(value.substring(0, index));
                int endIndex = Integer.parseInt(value.substring(index + 1));
                int textLength = ed.getText().length();
                
                if (beginIndex >= 0 && beginIndex <= textLength && endIndex >= 0 && endIndex <= textLength) {                
                    setPropertyChangeIgnored(EditorComponent.PROPERTY_SELECTION_BEGIN_INDEX, true);
                    setPropertyChangeIgnored(EditorComponent.PROPERTY_SELECTION_END_INDEX, true);
                    ed.setSelectionRange(beginIndex, endIndex);
                    setPropertyChangeIgnored(EditorComponent.PROPERTY_SELECTION_BEGIN_INDEX, false);
                    setPropertyChangeIgnored(EditorComponent.PROPERTY_SELECTION_END_INDEX, false);
                }
            } else {
                super.componentChange(event);
            }
        }        
    }
}
