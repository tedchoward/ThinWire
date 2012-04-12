/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package thinwire.ui;

import java.util.regex.Pattern;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractTextComponent<C extends TextComponent> extends AbstractComponent<C> implements TextComponent {
    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("(?<!\\r)\\n|\\r(?!\\n)");

    private String text = "";

    public String getText() {
        return text;
    }

    public void setText(String text) {
        String oldText = this.text;
        setTextDirect(text);
        firePropertyChange(this, PROPERTY_TEXT, oldText, this.text);            
    }
    
    void setTextDirect(String text) {
        if (text == null) {
            this.text = "";
        } else {
            this.text = NEW_LINE_PATTERN.matcher(text).replaceAll("\r\n");
        }
    }
}
