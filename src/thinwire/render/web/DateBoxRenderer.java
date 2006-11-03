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

import java.text.SimpleDateFormat;
import java.util.Date;

import thinwire.ui.Component;
import thinwire.ui.DateBox;
import thinwire.ui.event.PropertyChangeEvent;

final class DateBoxRenderer extends ComponentRenderer {
    private static final String DATE_BOX_CLASS = "tw_DateBox";
    static final SimpleDateFormat dateBoxFormat = new SimpleDateFormat("MM/dd/yyyy");
    
    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(DATE_BOX_CLASS, wr, c, container);
        addClientSideProperty(DateBox.PROPERTY_SELECTED_DATE);
        addInitProperty(DateBox.PROPERTY_SELECTED_DATE, dateBoxFormat.format(((DateBox) c).getSelectedDate()));
        addInitProperty("today", dateBoxFormat.format(new Date()));
        super.render(wr, c, container);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        Object newValue = pce.getNewValue();
        if (name.equals(DateBox.PROPERTY_SELECTED_DATE)) {
            postClientEvent("setSelectedDate", dateBoxFormat.format((Date) newValue));
        } else {
            super.propertyChange(pce);
        }
        
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        DateBox db = (DateBox) comp;
        
        if (name.equals(DateBox.PROPERTY_SELECTED_DATE)) {
            Date dt = (Date)getEventObject(comp, (String)event.getValue());
            setPropertyChangeIgnored(name, true);
            db.setSelectedDate(dt);
            setPropertyChangeIgnored(name, false);
        } else if (!componentChangeFireAction(event, DateBox.PROPERTY_SELECTED_DATE) && !componentChangeFireDrop(event)) {
            super.componentChange(event);
        }
    }
}
