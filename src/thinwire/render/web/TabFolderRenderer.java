/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import thinwire.ui.Component;
import thinwire.ui.TabFolder;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class TabFolderRenderer extends ContainerRenderer {
    private static final String TABFOLDER_CLASS = "tw_TabFolder";
    private static final String SET_CURRENT_INDEX = "setCurrentIndex";

    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(TABFOLDER_CLASS, wr, c, container);
        addClientSideProperty(TabFolder.PROPERTY_CURRENT_INDEX);        
        setPropertyChangeIgnored(TabFolder.PROPERTY_SCROLL, true);
        super.render(wr, c, container);
        postClientEvent(SET_CURRENT_INDEX, new Integer(((TabFolder)c).getCurrentIndex()));        
	}

    public void propertyChange(PropertyChangeEvent pce) {        
        if (pce.getPropertyName().equals(TabFolder.PROPERTY_CURRENT_INDEX)) {
            if (isPropertyChangeIgnored(TabFolder.PROPERTY_CURRENT_INDEX)) return;        
            postClientEvent(SET_CURRENT_INDEX, pce.getNewValue());
        } else {
            super.propertyChange(pce);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();
        TabFolder tf = (TabFolder)comp;
        
        if (name.equals(TabFolder.PROPERTY_CURRENT_INDEX)) {
            setPropertyChangeIgnored(name, true);
            tf.setCurrentIndex(Integer.valueOf(value).intValue());
            setPropertyChangeIgnored(name, false);
        } else {
            super.componentChange(event);
        }
    }
}
