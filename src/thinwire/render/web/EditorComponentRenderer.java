/*
                         ThinWire(TM) RIA Ajax Framework
               Copyright (C) 2003-2006 Custom Credit Systems
  
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.
  
  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  Place, Suite 330, Boston, MA 02111-1307 USA
 
  Users wishing to use this library in proprietary products which are not 
  themselves to be released under the GNU Public License should contact Custom
  Credit Systems for a license to do so.
  
                Custom Credit Systems, Richardson, TX 75081, USA.
                           http://www.thinwire.com
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.AlignX;
import thinwire.ui.Component;
import thinwire.ui.EditorComponent;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
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
