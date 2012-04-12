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
        } else {
            super.componentChange(event);
        }
    }
}
