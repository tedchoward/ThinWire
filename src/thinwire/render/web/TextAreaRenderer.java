/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.TextArea;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class TextAreaRenderer extends ComponentRenderer {
    private static final String TEXTAREA_CLASS = "tw_TextArea";
    private static final String SET_MAX_LENGTH = "setMaxLength";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        jsClass = TEXTAREA_CLASS;
        TextArea ta = (TextArea)c;
        addInitProperty(TextArea.PROPERTY_TEXT, ta.getText());
        addInitProperty(TextArea.PROPERTY_MAX_LENGTH, ta.getMaxLength());
        super.render(wr, c, container);        		
	}
        
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        
        if (name.equals(TextArea.PROPERTY_TEXT)) {
            Object newValue = pce.getNewValue();
            if (isPropertyChangeIgnored(name, newValue)) return;
            postClientEvent(SET_TEXT, newValue);
        } else { 
            if (isPropertyChangeIgnored(name)) return;
            
            if (name.equals(TextArea.PROPERTY_MAX_LENGTH)) {
                postClientEvent(SET_MAX_LENGTH, pce.getNewValue());            
            } else if (name.equals(TextArea.PROPERTY_SELECTION_BEGIN_INDEX) || name.equals(TextArea.PROPERTY_SELECTION_END_INDEX)) {
                TextArea ta = (TextArea)comp;
                int beginIndex = ta.getSelectionBeginIndex();
                int endIndex = ta.getSelectionEndIndex();
                postClientEvent(SET_SELECTION_RANGE, beginIndex, endIndex);
            } else {
                super.propertyChange(pce);
            }
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        TextArea ta = (TextArea)comp;
        
        if (name.equals(TextArea.PROPERTY_TEXT)) {
            setPropertyChangeIgnored(name, value, true);
            ta.setText(value);
        } else {
            setPropertyChangeIgnored(name, true);
            
            if (name.equals("selectionRange")) {
                int index = value.indexOf(',');
                int beginIndex = Integer.parseInt(value.substring(0, index));
                int endIndex = Integer.parseInt(value.substring(index + 1));
                int textLength = ta.getText().length();
                
                if (beginIndex >= 0 && beginIndex <= textLength && endIndex >= 0 && endIndex <= textLength) {                
                    setPropertyChangeIgnored(TextArea.PROPERTY_SELECTION_BEGIN_INDEX, true);
                    setPropertyChangeIgnored(TextArea.PROPERTY_SELECTION_END_INDEX, true);
                    ta.setSelectionRange(beginIndex, endIndex);
                    setPropertyChangeIgnored(TextArea.PROPERTY_SELECTION_BEGIN_INDEX, false);
                    setPropertyChangeIgnored(TextArea.PROPERTY_SELECTION_END_INDEX, false);
                }
            } else {
                super.componentChange(event);
            }
        }
        
        setPropertyChangeIgnored(name, false);            
    }
}
