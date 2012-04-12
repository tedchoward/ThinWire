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

import java.util.HashMap;
import java.util.Map;

import thinwire.ui.Component;
import thinwire.ui.Frame;
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
        addInitProperty(Window.PROPERTY_TITLE, w instanceof Frame ? w.getTitle() : parseRichText(w.getTitle()));
        if (compToId == null) compToId = new HashMap<Component, Integer>(w.getChildren().size());
        super.render(wr, c, container);
        Menu m = w.getMenu();
        
        if (m != null) {
        	if (mr == null) mr = (MenuRenderer)ai.getRenderer(m);
        	mr.render(wr, m, this);
        }
        
        log.fine("Showing window with id:" + id);
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
    	Integer id = compToId.get(comp);

    	if (id == null) {
        	id = ai.getNextComponentId();
            compToId.put(comp, id);
        }
    	
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
                (mr = (MenuRenderer)ai.getRenderer(m)).render(wr, m, this);            
            }
        }
    }
}
