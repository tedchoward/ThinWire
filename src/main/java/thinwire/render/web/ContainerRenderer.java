/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.render.web;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.Container.ScrollType;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
public class ContainerRenderer extends ComponentRenderer implements ItemChangeListener {
    private static final String CONTAINER_CLASS = "tw_Container";
    private static final String SET_SCROLL_TYPE = "setScrollType";
    private static final String REMOVE_COMPONENT = "removeComponent";            

    private boolean fullyRendered;
    private Map<Component, ComponentRenderer> compToRenderer;
    
	protected void render(WindowRenderer wr, Component comp, ComponentRenderer container) {
        if (jsClass == null) init(CONTAINER_CLASS, wr, comp, container);
        setPropertyChangeIgnored(Component.PROPERTY_FOCUS, true);
        setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);
        Container<Component> c = (Container<Component>)comp;
        if (!isPropertyChangeIgnored(Container.PROPERTY_SCROLL_TYPE)) addInitProperty(Container.PROPERTY_SCROLL_TYPE, getScrollTypeCode(c.getScrollType()));
        
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
    
    protected void destroy() {
        Container<Component> c = (Container<Component>)comp;
        c.removeItemChangeListener(this);
        
        for (Component comp : c.getChildren()) {
            compToRenderer.get(comp).destroy();
        }

        super.destroy();
        compToRenderer.clear();
        compToRenderer = null;
    }
    
    void renderChildren(Container<Component> c) {
        List<Component> children = c.getChildren();
        if (compToRenderer == null) compToRenderer = new HashMap<Component, ComponentRenderer>(children.size()); 
        fullyRendered = false;
        
        for (Component comp : children) {
            renderChild(comp);
        }
        
        fullyRendered = true;
    } 
    
    void renderChild(Component comp) {
    	ComponentRenderer r = compToRenderer.get(comp);
    	if (r == null) compToRenderer.put(comp, r =(ComponentRenderer) wr.ai.getRenderer(comp));
        r.render(wr, comp, this);
    }
    
    boolean isFullyRendered() {
        return fullyRendered;
    }
    
    public void propertyChange(PropertyChangeEvent pce) {        
        if (pce.getPropertyName().equals(Container.PROPERTY_SCROLL_TYPE)) {
            if (isPropertyChangeIgnored(Container.PROPERTY_SCROLL_TYPE)) return;
            postClientEvent(SET_SCROLL_TYPE, getScrollTypeCode((ScrollType)pce.getNewValue()));
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
