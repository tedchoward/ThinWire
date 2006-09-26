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
package thinwire.ui.layout;

import java.util.List;

import thinwire.render.RenderStateEvent;
import thinwire.render.RenderStateListener;
import thinwire.render.web.WebApplication;
import thinwire.ui.*;
import thinwire.ui.event.*;
import thinwire.ui.style.Style;

/**
 * @author Joshua J. Gertzen
 */
public class SplitLayout implements Layout {
    public enum SplitType {VERTICAL, HORIZONTAL};
        
    private static final String RES_PATH = "class:///" + SplitLayout.class.getName() + "/resources/";
    private static final String CLIENT_SIDE_LIB = RES_PATH + "SplitLayout.js";
    
    private PropertyChangeListener pcl = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
            if (autoLayout) apply();
        }
    };
    
    private ItemChangeListener icl = new ItemChangeListener() {
        public void itemChange(ItemChangeEvent ev) {
            if (autoLayout) apply();
        }
    };
            
    private boolean autoLayout;
    private Container<Component> container;
    private SplitType split;
    private int dividerSize;
    private Label divider;
    private double size;
    private int maximized;
    private boolean layoutInProgress;
        
    public SplitLayout(Container<Component> container, SplitType split, double size) {
        final WebApplication app = (WebApplication)Application.current();        
        app.clientSideIncludeFile(CLIENT_SIDE_LIB);
        divider = new Label();
        divider.addPropertyChangeListener(new String[] {Component.PROPERTY_X, Component.PROPERTY_Y}, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (!layoutInProgress && SplitLayout.this.container != null) {
                    double value = (Integer)ev.getNewValue() + dividerSize;
                    
                    if (SplitLayout.this.size < 1) {
                        int contValue = ev.getPropertyName().equals(Component.PROPERTY_X) ? SplitLayout.this.container.getInnerWidth() : SplitLayout.this.container.getInnerHeight();
                        value = Math.floor(value / contValue * 1000 + .5) / 1000; 
                    }
                    
                    SplitLayout.this.setSize(value);
                }
            }
        });
                
        setSplit(split);
        setSize(size);
        setMaximized(false);
        setDividerSize(4);
        setContainer(container);
        setAutoLayout(true);
        
        app.invokeAfterRendered(divider, new RenderStateListener() {
            public void renderStateChange(RenderStateEvent ev) {
                app.clientSideMethodCall("tw_SplitLayout", "newInstance", ev.getId());                
            }
        });                
    }
        
    public Container<Component> getContainer() {
        return container;
    }
    
    public void setContainer(Container<Component> container) {
        if (this.container != null) {
            this.container.removeItemChangeListener(icl);
            this.container.removePropertyChangeListener(pcl);
            this.container.getChildren().remove(divider);            
        }
        
        this.container = container;
        divider.setSize(10, 2);
        divider.setVisible(false);
        this.container.getChildren().add(divider);
        this.container.addItemChangeListener(icl);
        String[] props;
        
        if (this.container instanceof Window) {
            props = new String[]{Container.PROPERTY_WIDTH, Container.PROPERTY_HEIGHT, Window.PROPERTY_MENU};
        } else {
            props = new String[]{Container.PROPERTY_WIDTH, Container.PROPERTY_HEIGHT};
        }
        
        this.container.addPropertyChangeListener(props, pcl);
        if (autoLayout) apply();
    }
    
    public boolean isAutoLayout() {
        return autoLayout;
    }

    public void setAutoLayout(boolean autoLayout) {
        this.autoLayout = autoLayout;
        if (autoLayout) apply();
    }    
    
    public SplitType getSplit() {
        return this.split;
    }
    
    public void setSplit(SplitType split) {
        if (split == null) throw new IllegalArgumentException("split == null");
        this.split = split;
        if (autoLayout) apply();
    }
    
    public int getDividerSize() {
        return dividerSize;
    }
    
    public void setDividerSize(int dividerSize) {
        if (dividerSize < 0 || dividerSize >= 32767) throw new IllegalArgumentException("dividerSize < 0 || dividerSize >= 32767");
        this.dividerSize = dividerSize;        
        this.divider.setVisible(dividerSize > 0);
        if (autoLayout) apply();
    }
    
    public Style getDividerStyle() {
        return divider.getStyle();
    }
    
    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
        if (autoLayout) apply();
    }
        
    public void setMaximized(boolean maximized) {
        setMaximized(maximized, false);
    }
    
    public void setMaximized(boolean maximized, boolean secondary) {        
        if (maximized) {
            if (container == null) throw new IllegalStateException("container == null");            
            int total = split == SplitType.VERTICAL ? container.getInnerWidth() : container.getInnerHeight();
            int fraction;
            
            if (size >= 1) {
                fraction = (int)Math.floor(size);
            } else {
                fraction = (int)Math.floor(total * size);
            }
            
            if (fraction >= total / 2) {
                this.maximized = secondary ? 1 : 0;
            } else {
                this.maximized = secondary ? 0 : 1;
            }
        } else {
            this.maximized = -1;
        }
        
        if (autoLayout) apply();
    }
    
    public boolean isMaximized() {
        return maximized != -1;
    }
    
    public void apply() {
       if (container == null) return;
       int innerHeight = container.getInnerHeight();
       int innerWidth = container.getInnerWidth();
       if (innerHeight < 10 || innerWidth < 10) return;
       layoutInProgress = true;
       int firstSize = (split == SplitType.VERTICAL ? innerWidth : innerHeight) - dividerSize;
       int secondSize;
       
       if (maximized == -1) {
           secondSize = firstSize;
           
           if (size >= 1) {
               firstSize = (int)Math.floor(size);
           } else {
               firstSize *= size;
           }
       
           secondSize -= firstSize;
       } else {
           if (maximized == 0) {
               secondSize = 0;
           } else {
               secondSize = firstSize;
               firstSize = 0;
           }
       }
       
       List<Component> children = container.getChildren();
       
       for (int i = children.size(); --i >= 0;) {
           Component c = children.get(i);
           
           if (i == 0) {
               if (maximized != 1) {
                   if (split == SplitType.VERTICAL) {
                       c.setBounds(0, 0, firstSize, innerHeight);
                   } else {
                       c.setBounds(0, 0, innerWidth, firstSize);
                   }
                   
                   c.setVisible(true);
               } else {
                   c.setVisible(false);
               }
           } else if (i == 1) {
               if (maximized != 0) {
                   if (split == SplitType.VERTICAL) {
                       c.setBounds(firstSize + dividerSize, 0, secondSize, innerHeight);
                   } else {
                       c.setBounds(0, firstSize + dividerSize, innerWidth, secondSize);
                   }
               
                   c.setVisible(true);
               } else {
                   c.setVisible(false);
               }
           } else if (c == divider) {               
               if (split == SplitType.VERTICAL) {
                   c.setBounds(firstSize, 0, dividerSize, innerHeight);
               } else {                   
                   c.setBounds(0, firstSize, innerWidth, dividerSize);                   
               }

               c.setVisible(true);
           } else {
               c.setVisible(false);
           }
       }
       
       layoutInProgress = false;
    }
}
