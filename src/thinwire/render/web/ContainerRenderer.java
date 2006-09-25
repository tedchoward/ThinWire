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

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.ScrollType;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
class ContainerRenderer extends ComponentRenderer implements ItemChangeListener {
    private static final String CONTAINER_CLASS = "tw_Container";
    private static final String SET_SCROLL = "setScroll";
    private static final String REMOVE_COMPONENT = "removeComponent";            

    private Map<Component, ComponentRenderer> compToRenderer;
    
	void render(WindowRenderer wr, Component comp, ComponentRenderer container) {
        if (jsClass == null) init(CONTAINER_CLASS, wr, comp, container);            
        setPropertyChangeIgnored(Component.PROPERTY_FOCUS, true);
        setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);
        Container<Component> c = (Container<Component>)comp;
        if (!isPropertyChangeIgnored(Container.PROPERTY_SCROLL)) addInitProperty(Container.PROPERTY_SCROLL, getScrollTypeCode(c.getScroll()));
        
        c.addItemChangeListener(this);
        super.render(wr, comp, container);
        renderChildren(c);
                
        comp = c.getChildWithFocus();
        
        if (comp != null && !(comp instanceof Container)) {
            ComponentRenderer cr = compToRenderer.get(comp);
            if (!cr.isPropertyChangeIgnored(Component.PROPERTY_FOCUS)) cr.postClientEvent(SET_FOCUS, true);
        }
	}
    
    int getScrollTypeCode(ScrollType st) {
        int sti;
                
        if (st == ScrollType.AS_NEEDED) {
            sti = 1;
        } else if (st == ScrollType.ALWAYS) {
            sti = 2;
        } else {
            sti = 0;
        }    
        
        return sti;
    }
    
    void destroy() {
        Container<Component> c = (Container<Component>)comp;
        c.removeItemChangeListener(this);
        super.destroy();
        
        for (Component comp : c.getChildren()) {
            compToRenderer.get(comp).destroy();
        }
        
        compToRenderer.clear();
        compToRenderer = null;
    }
    
    void renderChildren(Container<Component> c) {
        List<Component> children = c.getChildren();
        this.compToRenderer = new HashMap<Component, ComponentRenderer>(children.size());
        
        for (Component comp : children) {
            renderChild(comp);
        }
    }
    
    void renderChild(Component comp) {
        ComponentRenderer r = wr.ai.getRenderer(comp);
        compToRenderer.put(comp, r);
        r.render(wr, comp, this);        
    }
    
    boolean isFullyRendered() {
        return compToRenderer.size() == ((Container)comp).getChildren().size();
    }
    
    public void propertyChange(PropertyChangeEvent pce) {        
        if (pce.getPropertyName().equals(Container.PROPERTY_SCROLL)) {
            if (isPropertyChangeIgnored(Container.PROPERTY_SCROLL)) return;
            postClientEvent(SET_SCROLL, getScrollTypeCode((ScrollType)pce.getNewValue()));
        } else {
            super.propertyChange(pce);
        }
    }
    
    public void itemChange(ItemChangeEvent ice) {
        ItemChangeEvent.Type type = ice.getType();
        
        if (type == ItemChangeEvent.Type.REMOVE || type == ItemChangeEvent.Type.SET) {
            Component comp = (Component)ice.getOldValue();
            postClientEvent(REMOVE_COMPONENT, wr.getComponentId(comp));
            ((ComponentRenderer)compToRenderer.remove(comp)).destroy();
        }
        
        if (type == ItemChangeEvent.Type.ADD || type == ItemChangeEvent.Type.SET) {
            renderChild((Component)ice.getNewValue());
        }
    }    
}
