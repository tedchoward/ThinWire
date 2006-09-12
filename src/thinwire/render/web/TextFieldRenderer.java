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

import thinwire.ui.AlignX;
import thinwire.ui.Component;
import thinwire.ui.TextField;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class TextFieldRenderer extends ComponentRenderer {
    private static final String TEXTFIELD_CLASS = "tw_TextField";
    private static final String SET_INPUT_HIDDEN = "setInputHidden";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        jsClass = TEXTFIELD_CLASS;
		TextField tf = (TextField)c;
        addInitProperty(TextField.PROPERTY_TEXT, tf.getText());
        addInitProperty(TextField.PROPERTY_INPUT_HIDDEN, tf.isInputHidden());
        addInitProperty(TextField.PROPERTY_EDIT_MASK, getEditMaskTextLength(tf));
        addInitProperty(TextField.PROPERTY_ALIGN_X, tf.getAlignX().name().toLowerCase());
        super.render(wr, c, container);
	}
    
    public void propertyChange(PropertyChangeEvent pce) {
        Object newValue = pce.getNewValue();
        String name = pce.getPropertyName();       

        if (name.equals(TextField.PROPERTY_TEXT)) {
            if (resetPropertyChangeIgnored(name, newValue)) return;            
            postClientEvent(SET_TEXT, newValue);
        } else {
            if (resetPropertyChangeIgnored(name)) return;
            
            if (name.equals(TextField.PROPERTY_INPUT_HIDDEN)) {
                postClientEvent(SET_INPUT_HIDDEN, newValue);
            } else if (name.equals(TextField.PROPERTY_EDIT_MASK) || name.equals(TextField.PROPERTY_MAX_LENGTH)) {
                postClientEvent(SET_EDIT_MASK, getEditMaskTextLength(comp));
            } else if (name.equals(TextField.PROPERTY_ALIGN_X)) {
                postClientEvent(SET_ALIGN_X, ((AlignX)newValue).name().toLowerCase());
            } else if (name.equals(TextField.PROPERTY_SELECTION_BEGIN_INDEX) || name.equals(TextField.PROPERTY_SELECTION_END_INDEX)) {
                TextField tf = (TextField)comp;
                postClientEvent(SET_SELECTION_RANGE, new Integer(tf.getSelectionBeginIndex()), new Integer(tf.getSelectionEndIndex()));
            } else {
                super.propertyChange(pce);
            }
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        TextField tf = (TextField)comp;
        
        if (name.equals(TextField.PROPERTY_TEXT)) {
            setPropertyChangeIgnored(name, value, true);
            tf.setText(value);
        } else {
            setPropertyChangeIgnored(name, true);

            if (name.equals("selectionRange")) {
                int index = value.indexOf(',');
                int beginIndex = Integer.parseInt(value.substring(0, index));
                int endIndex = Integer.parseInt(value.substring(index + 1));
                int textLength = tf.getText().length();
                
                if (beginIndex >= 0 && beginIndex <= textLength && endIndex >= 0 && endIndex <= textLength) {        
                    setPropertyChangeIgnored(TextField.PROPERTY_SELECTION_BEGIN_INDEX, true);
                    setPropertyChangeIgnored(TextField.PROPERTY_SELECTION_END_INDEX, true);
                    tf.setSelectionRange(beginIndex, endIndex);
                    setPropertyChangeIgnored(TextField.PROPERTY_SELECTION_BEGIN_INDEX, false);
                    setPropertyChangeIgnored(TextField.PROPERTY_SELECTION_END_INDEX, false);
                }
            } else {
                super.componentChange(event);
            }
        }
        
        setPropertyChangeIgnored(name, false);
    }
}
