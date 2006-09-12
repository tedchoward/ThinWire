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
package thinwire.ui;

import java.util.regex.Pattern;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractTextComponent extends AbstractComponent implements TextComponent {
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