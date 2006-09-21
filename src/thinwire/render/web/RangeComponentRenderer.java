package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.RangeComponent;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.style.FX;

abstract class RangeComponentRenderer extends ComponentRenderer {
    
    public void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        addInitProperty(RangeComponent.PROPERTY_LENGTH, ((RangeComponent) c).getLength());
        addInitProperty(RangeComponent.PROPERTY_CURRENT_INDEX, ((RangeComponent) c).getCurrentIndex());
        super.render(wr, comp, container);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        Object newValue = pce.getNewValue();
        String name = pce.getPropertyName();
        
        if (isPropertyChangeIgnored(name)) return;
        if (name.equals(RangeComponent.PROPERTY_LENGTH)) {
            postClientEvent("setLength", (Integer) newValue);
        } else if (name.equals(RangeComponent.PROPERTY_CURRENT_INDEX)) {
            setPropertyWithEffect(name, (Integer) newValue, (Integer) pce.getOldValue(), "setCurrentIndex", FX.PROPERTY_FX_POSITION_CHANGE);
        } else {
            super.propertyChange(pce);
        }
    }

}
