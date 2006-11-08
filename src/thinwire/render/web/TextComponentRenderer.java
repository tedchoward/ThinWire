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

import thinwire.ui.Component;
import thinwire.ui.TextComponent;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
abstract class TextComponentRenderer extends ComponentRenderer {
    static final String SET_TEXT = "setText";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        TextComponent tc = (TextComponent)c;
        addInitProperty(TextComponent.PROPERTY_TEXT, RICH_TEXT_PARSER.parseRichText(tc.getText(), this));
        super.render(wr, c, container);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;

        if (name.equals(TextComponent.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, RICH_TEXT_PARSER.parseRichText((String)pce.getNewValue(), this));
        } else {
            super.propertyChange(pce);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        if (!componentChangeFireDrop(event)) super.componentChange(event);
    }
}