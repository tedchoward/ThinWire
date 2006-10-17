package thinwire.render.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import thinwire.ui.Component;
import thinwire.ui.DateBox;
import thinwire.ui.event.PropertyChangeEvent;

public class DateBoxRenderer extends ComponentRenderer {
    private static final String DATE_BOX_CLASS = "tw_DateBox";
    private static final SimpleDateFormat dateBoxFormat = new SimpleDateFormat("MM/dd/yyyy");
    
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
            String value = (String) event.getValue();
            try {
                Date dt = dateBoxFormat.parse(value);
                setPropertyChangeIgnored(name, dt, true);
                db.setSelectedDate(dt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                setPropertyChangeIgnored(name, false);
            }
        } else {
            super.componentChange(event);
        }
    }
}
