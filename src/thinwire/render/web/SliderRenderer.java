/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.Slider;

/**
 * @author Ted C. Howard
 */
public class SliderRenderer extends RangeComponentRenderer {
    private static final String SLIDER_CLASS = "tw_Slider";
    
    public void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(SLIDER_CLASS, wr, c, container);
        addClientSideProperty(Slider.PROPERTY_CURRENT_INDEX);
        super.render(wr, c, container);
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        
        if (name.equals(Slider.PROPERTY_CURRENT_INDEX)) {
            int value = Integer.parseInt((String) event.getValue());
            setPropertyChangeIgnored(name, value, true);
            ((Slider) comp).setCurrentIndex(value);
        } else {
            setPropertyChangeIgnored(name, true);
            super.componentChange(event);
        }
        setPropertyChangeIgnored(name, false);
    }

}
