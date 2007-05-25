/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
   	            email: info@thinwire.com    ph: +1 (888) 644-6405
 	                        http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
#VERSION_HEADER#
*/
package thinwire.ui.layout;

import java.util.logging.Logger;

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.Window;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;

/**
 * @author Joshua J. Gertzen
 */
public abstract class AbstractLayout implements Layout {
    private static final Logger log = Logger.getLogger(AbstractLayout.class.getName());
    
    protected Container<Component> container;
    protected String[] autoLayoutProps;
    protected boolean autoLayout;
    protected boolean limitLayout;
    protected int margin;
    protected int spacing;

    private final PropertyChangeListener pclContainer = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent pce) {
            if (autoLayout) apply();
        }
    };
    
    private final PropertyChangeListener pcl = new PropertyChangeListener() {
        private boolean ignoreChange;
        
        public void propertyChange(PropertyChangeEvent pce) {
            if (ignoreChange) return;

            if (limitLayout && pce.getPropertyName().equals(Component.PROPERTY_LIMIT)) {
                Component comp = (Component)pce.getSource();
                Object limit = getFormalLimit(comp);
                
                if (!comp.getLimit().equals(limit)) {
                    ignoreChange = true;
                    comp.setLimit(limit);
                    ignoreChange = false;
                }
            }
            
            if (autoLayout) apply();
        }
    };
    
    private final ItemChangeListener icl = new ItemChangeListener() {
        public void itemChange(ItemChangeEvent ice) {
            ItemChangeEvent.Type type = ice.getType();
            
            if (type == Type.REMOVE || type == Type.SET) {
                Component comp = (Component)ice.getOldValue();
                
                if (autoLayoutProps != null && comp != null) {
                    removeComponent(comp);
                    comp.removePropertyChangeListener(pcl);
                }
            }
            
            if (type == Type.ADD || type == Type.SET) {
                Component comp = (Component)ice.getNewValue();
                
                if (autoLayoutProps != null && comp != null) {
                    if (limitLayout) comp.setLimit(getFormalLimit(comp));
                    addComponent(comp);
                    comp.addPropertyChangeListener(autoLayoutProps, pcl);
                }
            }
            
            if (autoLayout) apply();
        }
    };
    
    protected AbstractLayout(String... autoLayoutProps) {
        if (autoLayoutProps.length > 0) {
            
            for (String name : autoLayoutProps) {
                if (name.equals(Component.PROPERTY_LIMIT)) {
                    limitLayout = true;
                    break;
                }
            }
            
            this.autoLayoutProps = autoLayoutProps;
        }
    }
    
    protected void addComponent(Component comp) {
        
    }
    
    protected void removeComponent(Component comp) {
         
    }
    
    protected Object getFormalLimit(Component comp) {
        return comp.getLimit();
    }
    
    public void setContainer(Container<Component> container) {
        if (container == this.container) return;
        boolean autoLayout = this.autoLayout;
        this.autoLayout = false;
        
        if (this.container != null) {
            for (Component comp : this.container.getChildren()) {
                if (autoLayoutProps != null && comp != null) {
                    removeComponent(comp);
                    comp.removePropertyChangeListener(pcl);
                }
            }
            
            this.container.removeItemChangeListener(icl);
            this.container.removePropertyChangeListener(pclContainer);
            this.container.setLayout(null);
        }
        
        this.container = container;
        
        if (container != null) {
            for (Component comp : container.getChildren()) {
                if (autoLayoutProps != null && comp != null) {
                    if (limitLayout) comp.setLimit(getFormalLimit(comp));
                    addComponent(comp);
                    comp.addPropertyChangeListener(autoLayoutProps, pcl);
                }
            }

            container.addItemChangeListener(icl);
            container.addPropertyChangeListener(container instanceof Window ? 
                    new String[] {Container.PROPERTY_WIDTH, Container.PROPERTY_HEIGHT, Window.PROPERTY_MENU} : 
                    new String[] {Container.PROPERTY_WIDTH, Container.PROPERTY_HEIGHT},
                    pclContainer);
            if (container.getLayout() != this) container.setLayout(this);
        }
        
        this.autoLayout = autoLayout;
        if (autoLayout) apply();
    }
    
    public Container<Component> getContainer() {
        return container;
    }
    
    public boolean isAutoApply() {
        return autoLayout;
    }

    public void setAutoApply(boolean autoLayout) {
        this.autoLayout = autoLayout;
        if (autoLayout) apply();
    }

	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		if (margin < 0 || margin >= Short.MAX_VALUE) throw new IllegalArgumentException("margin < 0 || margin >= " + Short.MAX_VALUE);
		this.margin = margin;
		if (autoLayout) apply();
	}

	public int getSpacing() {
		return spacing;
	}

	public void setSpacing(int spacing) {
		if (spacing < 0 || spacing >= Short.MAX_VALUE) throw new IllegalArgumentException("spacing < 0 || spacing >= " + Short.MAX_VALUE);
		this.spacing = spacing;
		if (autoLayout) apply();
	}
}
