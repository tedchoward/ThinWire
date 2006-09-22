/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.TabFolder;
import thinwire.ui.TabSheet;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
class TabSheetRenderer extends ContainerRenderer {
    private static final String TABSHEET_CLASS = "tw_TabSheet";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(TABSHEET_CLASS, wr, c, container);
        //a tabsheet does not support x, y, width, height, enabled or visible
        setPropertyChangeIgnored(Component.PROPERTY_X, true);
        setPropertyChangeIgnored(Component.PROPERTY_Y, true);
        setPropertyChangeIgnored(Component.PROPERTY_WIDTH, true);
        setPropertyChangeIgnored(Component.PROPERTY_HEIGHT, true);
        setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);
        TabSheet ts = (TabSheet)c;
        addInitProperty(TabSheet.PROPERTY_TEXT, ts.getText());
        addInitProperty(TabSheet.PROPERTY_IMAGE, getRemoteNameForLocalFile(ts.getImage()));
        addInitProperty("tabIndex", ((TabFolder)ts.getParent()).getChildren().indexOf(ts));        
        super.render(wr, c, container);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        Object newValue = pce.getNewValue();        
        
        if (name.equals(TabSheet.PROPERTY_IMAGE)) {
            postClientEvent(SET_IMAGE, getRemoteNameForLocalFile((String)newValue));
        } else if (name.equals(TabSheet.PROPERTY_TEXT)) {
            postClientEvent(SET_TEXT, newValue);
        } else {
            super.propertyChange(pce);
        }
    }
}
