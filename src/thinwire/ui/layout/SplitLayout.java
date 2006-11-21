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
public final class SplitLayout extends AbstractLayout {
    public enum Maximize {NONE, FIRST, SECOND};
    
    private WebApplication app;
    private double split;
    private boolean splitVertical;
    private Maximize maximize;
    private Label divider;
    private int margin;
    private int dividerSize;
    private boolean layoutInProgress;
        
    public SplitLayout(double split) {
        this(split, false, 0, 4);
    }
    
    public SplitLayout(double split, boolean splitVertical) {
        this(split, splitVertical, 0, 4);
    }
    
    public SplitLayout(double split, boolean splitVertical, int margin) {
        this(split, splitVertical, margin, 4);
    }
    
    public SplitLayout(double split, boolean splitVertical, int margin, int dividerSize) {
        app = (WebApplication)Application.current();
        divider = new Label();
        divider.addPropertyChangeListener(new String[] {Component.PROPERTY_X, Component.PROPERTY_Y}, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (!layoutInProgress && SplitLayout.this.container != null) {
                    double value = (Integer)ev.getNewValue() - SplitLayout.this.margin;
                    
                    if (SplitLayout.this.split < 1) {
                        int contValue = ev.getPropertyName().equals(Component.PROPERTY_X) ? SplitLayout.this.container.getInnerWidth() : SplitLayout.this.container.getInnerHeight();
                        contValue -= SplitLayout.this.dividerSize + SplitLayout.this.margin * 2;
                        value = Math.floor(value / contValue * 1000 + .5) / 1000; 
                    }
                    
                    SplitLayout.this.setSplit(value);
                }
            }
        });

        setSplit(split);
        setSplitVertical(splitVertical);
        setMargin(margin);
        setDividerSize(dividerSize);
        setMaximize(null);
        setAutoLayout(true);
    }
    
    public void setContainer(Container<Component> container) {
        if (this.container != null) {
            Integer id = app.getComponentId(divider);
            if (id != null) app.clientSideMethodCall("tw_SplitLayout", "destroy", id);
            this.container.getChildren().remove(divider);
        }
        
        if (container != null) {
            app.invokeAfterRendered(divider, new RenderStateListener() {
                public void renderStateChange(RenderStateEvent ev) {
                    app.clientSideMethodCall("tw_SplitLayout", "newInstance", ev.getId(), margin);                
                }
            });               

            divider.setVisible(false);
            container.getChildren().add(divider);
        }
        
        super.setContainer(container);
    }
    
    public double getSplit() {
        return split;
    }

    public void setSplit(double split) {
        this.split = split;
        if (autoLayout) apply();
    }

    public boolean isSplitVertical() {
        return splitVertical;
    }
    
    public void setSplitVertical(boolean splitVertical) {
        this.splitVertical = splitVertical;
        
        if (splitVertical) {
            this.divider.setSize(4, 8);
        } else {
            this.divider.setSize(8, 4);
        }
        
        if (autoLayout) apply();
    }
    
    public int getMargin() {
        return margin;
    }
    
    public void setMargin(int margin) {
        if (margin < 0 || margin >= Short.MAX_VALUE) throw new IllegalArgumentException("margin < 0 || margin >= " + Short.MAX_VALUE);
        this.margin = margin;
        Integer id = app.getComponentId(divider);
        if (id != null) app.clientSideMethodCall("tw_SplitLayout", "setMargin", id, margin);
        if (autoLayout) apply();
    }
    
    public int getDividerSize() {
        return dividerSize;
    }
    
    public void setDividerSize(int dividerSize) {
        this.dividerSize = dividerSize;
        if (autoLayout) apply();
    }
    
    public Style getDividerStyle() {
        return divider.getStyle();
    }

    public boolean isMaximized() {
        return maximize != Maximize.NONE;
    }
    
    public Maximize getMaximize() {
        return maximize;
    }
        
    public void setMaximize(Maximize maximize) {
        if (maximize == null) maximize = Maximize.NONE;
        this.maximize = maximize;
        if (autoLayout) apply();
    }
    
    public void apply() {
       if (container == null) return;
       int innerHeight = container.getInnerHeight();
       int innerWidth = container.getInnerWidth();
       if (innerHeight < 10 || innerWidth < 10) return;
       layoutInProgress = true;
       int firstSize = (splitVertical ? innerWidth : innerHeight) - margin * 2;
       int dividerSize = this.dividerSize;
       int secondSize;
       
       if (maximize == Maximize.NONE) {
           firstSize -= dividerSize;
           secondSize = firstSize;
           
           if (split >= 1) {
               firstSize = (int)Math.floor(split);
           } else {
               firstSize *= split;
           }
       
           secondSize -= firstSize;
       } else {
           dividerSize = 0;
           
           if (maximize == Maximize.FIRST) {
               secondSize = 0;
           } else {
               secondSize = firstSize;
               firstSize = 0;
           }
       }
       
       List<Component> children = container.getChildren();
       
       for (int i = children.size(), cnt = 0; --i >= 0;) {
           Component c = children.get(i);
           
           if (c == divider) {
               if (maximize == Maximize.NONE) {
                   if (splitVertical) {
                       c.setBounds(firstSize + margin, margin, dividerSize, innerHeight - (margin * 2));
                   } else {                   
                       c.setBounds(margin, firstSize + margin, innerWidth - (margin * 2), dividerSize);                   
                   }
                   
                   c.setVisible(true);
               } else {
                   c.setVisible(false);
               }
           } else if (cnt == 0) {
               if (maximize == Maximize.FIRST) {
                   c.setVisible(false);
               } else {
                   if (splitVertical) {
                       c.setBounds(firstSize + dividerSize + margin, margin, secondSize, innerHeight - (margin * 2));
                   } else {
                       c.setBounds(margin, firstSize + dividerSize + margin, innerWidth - (margin * 2), secondSize);
                   }
               
                   c.setVisible(true);
               }
               
               cnt++;
           } else if (cnt == 1) {
               if (maximize == Maximize.SECOND) {
                   c.setVisible(false);
               } else {
                   if (splitVertical) {
                       c.setBounds(margin, margin, firstSize, innerHeight - (margin * 2));
                   } else {
                       c.setBounds(margin, margin, innerWidth - (margin * 2), firstSize);
                   }
                   
                   c.setVisible(true);
               }
               
               cnt++;
           } else {
               c.setVisible(false);
           }
       }
       
       layoutInProgress = false;
    }
}
