/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import java.util.HashMap;
import java.util.Map;

import thinwire.ui.Component;
import thinwire.ui.Menu;
import thinwire.ui.Window;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
class WindowRenderer extends ContainerRenderer {
    private static final String SET_TITLE = "setTitle";
    private static final String SET_MENU = "setMenu";            

    private Map<Component, Integer> compToId;
    private MenuRenderer mr;
    WebApplication ai;
    
    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        setPropertyChangeIgnored(Component.PROPERTY_VISIBLE, true);
        Window w = (Window)c;        
        addInitProperty("title", w.getTitle());
        compToId = new HashMap<Component, Integer>(w.getChildren().size());        
        super.render(wr, c, container);
        Menu m = w.getMenu();
        if (m != null) (mr = new MenuRenderer()).render(wr, m, this);
    }
    
    void destroy() {
        ai.clientSideMethodCall(id, DESTROY);
        if (mr != null) mr.destroy();
        super.destroy();
        mr = null;
        ai = null;
        compToId.clear();
        compToId = null;
    }
    
    Integer getComponentId(Component comp) {
        return compToId.get(comp);
    }
    
    Integer addComponentId(Component comp) {
        Integer id = ai.getNextComponentId();
        compToId.put(comp, id);
        return id;
    }

    Integer removeComponentId(Component comp) {
        return compToId.remove(comp);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {        
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;
        super.propertyChange(pce);
        Object newValue = pce.getNewValue();        

        if (name.equals(Window.PROPERTY_TITLE)) {
            postClientEvent(SET_TITLE, newValue);
        } else if (name.equals(Window.PROPERTY_MENU)) {
            if (mr != null) mr.destroy();            
            
            if (newValue == null) {
                mr = null;
                postClientEvent(SET_MENU, newValue);                
            } else {
                Menu m = (Menu)newValue;
                (mr = new MenuRenderer()).render(wr, m, this);            
            }
        }
    }
}
