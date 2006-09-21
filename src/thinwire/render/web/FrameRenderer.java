/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;

/**
 * @author Joshua J. Gertzen
 */
final class FrameRenderer extends WindowRenderer {  
    private static final String FRAME_CLASS = "tw_Frame";
    private WebApplication app;
    
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(FRAME_CLASS, wr, c, container);
        setPropertyChangeIgnored(Component.PROPERTY_X, true);
        setPropertyChangeIgnored(Component.PROPERTY_Y, true);
        setPropertyChangeIgnored(Component.PROPERTY_WIDTH, true);
        setPropertyChangeIgnored(Component.PROPERTY_HEIGHT, true);
        app = (WebApplication)WebApplication.current();
        super.render(wr, c, null);
	}
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        
        if (name.equals("frameSize")) {
            String[] ary = value.split(",");
            app.setPackagePrivateMember("innerWidth", comp, Integer.valueOf(ary[0]));
            app.setPackagePrivateMember("innerHeight", comp, Integer.valueOf(ary[1]));            
            app.setPackagePrivateMember("frameSize", comp, new Integer[]{Integer.valueOf(ary[2]), Integer.valueOf(ary[3])});
        } else {
            super.componentChange(event);
        }
    }    
}
