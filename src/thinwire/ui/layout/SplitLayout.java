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
    private Label spacer;
    private int margin;
    private int spacing;
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
    
    public SplitLayout(double split, boolean splitVertical, int margin, int spacing) {
        app = (WebApplication)Application.current();
        spacer = new Label();
        spacer.addPropertyChangeListener(new String[] {Component.PROPERTY_X, Component.PROPERTY_Y}, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (!layoutInProgress && SplitLayout.this.container != null) {
                    double value = (Integer)ev.getNewValue() - SplitLayout.this.margin;
                    
                    if (SplitLayout.this.split < 1) {
                        int contValue = ev.getPropertyName().equals(Component.PROPERTY_X) ? SplitLayout.this.container.getInnerWidth() : SplitLayout.this.container.getInnerHeight();
                        contValue -= SplitLayout.this.spacing + SplitLayout.this.margin * 2;
                        value = Math.floor(value / contValue * 1000 + .5) / 1000; 
                    }
                    
                    SplitLayout.this.setSplit(value);
                }
            }
        });

        setSplit(split);
        setSplitVertical(splitVertical);
        setMargin(margin);
        setSpacing(spacing);
        setMaximize(null);
        setAutoApply(true);
    }
    
    private RenderStateListener spacerListener = new RenderStateListener() {
        public void renderStateChange(RenderStateEvent ev) {
            app.clientSideMethodCall("tw_SplitLayout", "newInstance", ev.getId(), margin);                
        }
    };
    
    public void setContainer(Container<Component> container) {
        if (this.container != null) {
            app.removeRenderStateListener(spacer, spacerListener);
            this.container.getChildren().remove(spacer);
        }
        
        if (container != null) {
            app.addRenderStateListener(spacer, spacerListener);               
            spacer.setVisible(false);
            container.getChildren().add(spacer);
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
            this.spacer.setSize(4, 8);
        } else {
            this.spacer.setSize(8, 4);
        }
        
        if (autoLayout) apply();
    }
    
    public int getMargin() {
        return margin;
    }
    
    public void setMargin(int margin) {
        if (margin < 0 || margin >= Short.MAX_VALUE) throw new IllegalArgumentException("margin < 0 || margin >= " + Short.MAX_VALUE);
        this.margin = margin;
        Integer id = app.getComponentId(spacer);
        if (id != null) app.clientSideMethodCall("tw_SplitLayout", "setMargin", id, margin);
        if (autoLayout) apply();
    }
    
    public int getSpacing() {
        return spacing;
    }
    
    public void setSpacing(int spacing) {
        this.spacing = spacing;
        if (autoLayout) apply();
    }
    
    public Style getSpacerStyle() {
        return spacer.getStyle();
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
       int spacing = this.spacing;
       int secondSize;
       
       if (maximize == Maximize.NONE) {
           firstSize -= spacing;
           secondSize = firstSize;
           
           if (split >= 1) {
               firstSize = (int)Math.floor(split);
           } else {
               firstSize *= split;
           }
       
           secondSize -= firstSize;
       } else {
           spacing = 0;
           
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
           
           if (c == spacer) {
               if (maximize == Maximize.NONE) {
                   if (splitVertical) {
                       c.setBounds(firstSize + margin, margin, spacing, innerHeight - (margin * 2));
                   } else {                   
                       c.setBounds(margin, firstSize + margin, innerWidth - (margin * 2), spacing);                   
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
                       c.setBounds(firstSize + spacing + margin, margin, secondSize, innerHeight - (margin * 2));
                   } else {
                       c.setBounds(margin, firstSize + spacing + margin, innerWidth - (margin * 2), secondSize);
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
