/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.render.web;

import thinwire.ui.AlignTextComponent.AlignX;
import thinwire.ui.Component;
import thinwire.ui.EditorComponent;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
public abstract class EditorComponentRenderer extends TextComponentRenderer {
    private static final String SET_SELECTION_RANGE = "setSelectionRange";    
    private static final String VIEW_STATE_SELECTION_RANGE = "selectionRange";
    
    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        EditorComponent ed = (EditorComponent)c;
        addClientSideProperty(EditorComponent.PROPERTY_TEXT);
        addClientSideProperty(EditorComponent.PROPERTY_SELECTION_BEGIN_INDEX, VIEW_STATE_SELECTION_RANGE);
        addClientSideProperty(EditorComponent.PROPERTY_SELECTION_END_INDEX, VIEW_STATE_SELECTION_RANGE);
        addClientSideProperty(EditorComponent.PROPERTY_CURSOR_INDEX, VIEW_STATE_SELECTION_RANGE);
        addInitProperty(EditorComponent.PROPERTY_ALIGN_X, ed.getAlignX().name().toLowerCase());
        super.render(wr, c, container);
    }    
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;

        if (name.equals(EditorComponent.PROPERTY_ALIGN_X)) {
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
            setPropertyChangeIgnored(name, true);
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
