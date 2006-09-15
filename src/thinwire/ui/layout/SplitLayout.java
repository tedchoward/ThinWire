/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.ui.layout;

import java.util.List;

import thinwire.ui.*;
import thinwire.ui.event.*;

/**
 * @author Joshua J. Gertzen
 */
public class SplitLayout implements Layout {
    public enum SplitType {VERTICAL, HORIZONTAL};
    
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
    
    private static final String IMG_PATH = "class:///thinwire.ui.layout.SplitLayout/resources/";
    private static final String IMG_UP_ARROW = IMG_PATH + "splitLayoutUpArrow.png";
    private static final String IMG_DOWN_ARROW = IMG_PATH + "splitLayoutDownArrow.png";
    private static final String IMG_LEFT_ARROW = IMG_PATH + "splitLayoutLeftArrow.png";
    private static final String IMG_RIGHT_ARROW = IMG_PATH + "splitLayoutRightArrow.png";

    private boolean autoLayout;
    private Container container;
    private SplitType split;
    private int dividerSize;
    private Divider divider;
    private Image maxLeft;
    private Image maxRight;
    private double size;
    private boolean visibleMaximizeButtons;
    private int maximized;
        
    public SplitLayout(Container container, SplitType split, double size) {
        divider = new Divider();
        
        maxLeft = new Image();
        maxRight = new Image();

        ActionListener acl = new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (isMaximized()) {
                    setMaximized(false);
                } else {
                    boolean autoLayout = isAutoLayout();
                    setAutoLayout(false);
                    setMaximized(true);  
 
                    if ((ev.getSource() == maxLeft && maximized == 0) || (ev.getSource() == maxRight && maximized == 1)) {
                        setMaximized(true, true);
                    }
                    
                    setAutoLayout(autoLayout);
                }
            }            
        };
        
        maxLeft.addActionListener(Image.ACTION_CLICK, acl);        
        maxRight.addActionListener(Image.ACTION_CLICK, acl);        
        
        setSplit(split);
        setSize(size);
        setVisibleMaximizeButtons(true);
        setMaximized(false);
        setDividerSize(6);
        setContainer(container);
        setAutoLayout(true);
    }
        
    public Container getContainer() {
        return container;
    }
    
    public void setContainer(Container container) {
        if (this.container != null) {
            this.container.removeItemChangeListener(icl);
            this.container.removePropertyChangeListener(pcl);
            this.container.getChildren().remove(maxLeft);
            this.container.getChildren().remove(maxRight);
            this.container.getChildren().remove(divider);            
        }
        
        this.container = container;
        divider.setSize(10, 2);
        divider.setVisible(false);
        this.container.getChildren().add(divider);
        this.container.getChildren().add(maxLeft);
        this.container.getChildren().add(maxRight);
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
        this.dividerSize = dividerSize;        
        this.divider.setVisible(dividerSize > 0);
        this.setVisibleMaximizeButtons(dividerSize > 0);            
        if (autoLayout) apply();
    }
    
    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
        if (autoLayout) apply();
    }
    
    public boolean isVisibleMaximizeButtons() {
        return this.visibleMaximizeButtons; 
    }
    
    public void setVisibleMaximizeButtons(boolean visibleMaximizeButtons) {
       this.visibleMaximizeButtons = visibleMaximizeButtons;
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
               int imgSize = 8;
               
               if (split == SplitType.VERTICAL) {
                   c.setBounds(firstSize, 0, dividerSize, innerHeight);

                   if (visibleMaximizeButtons) {
                       if (dividerSize <= imgSize) {
                           maxLeft.setPosition(c.getX() - ((imgSize - dividerSize) / 2), c.getY() + 6);
                       } else {
                           maxLeft.setPosition(c.getX() + ((dividerSize - imgSize) / 2), c.getY() + 6);
                           imgSize = dividerSize;
                       }
                       
                       maxLeft.setSize(imgSize, 16);
                       maxLeft.setImage(maximized == 1 ? IMG_RIGHT_ARROW : IMG_LEFT_ARROW);                       
                       maxRight.setBounds(maxLeft.getX(), maxLeft.getY() + maxLeft.getHeight() + 2, imgSize, 16);                   
                       maxRight.setImage(maximized == 0 ? IMG_LEFT_ARROW : IMG_RIGHT_ARROW);
                   }
               } else {                   
                   c.setBounds(0, firstSize, innerWidth, dividerSize);                   

                   if (visibleMaximizeButtons) {
                       if (dividerSize <= imgSize) {
                           maxLeft.setPosition(c.getX() + 6, c.getY() - ((imgSize - dividerSize) / 2));
                       } else {
                           maxLeft.setPosition(c.getX() + 6, c.getY());
                           imgSize = dividerSize;
                       }
                       
                       maxLeft.setSize(16, imgSize);
                       maxLeft.setImage(maximized == 1 ? IMG_DOWN_ARROW : IMG_UP_ARROW);                       
                       maxRight.setBounds(maxLeft.getX() + maxLeft.getWidth() + 2, maxLeft.getY(), 16, imgSize);                   
                       maxRight.setImage(maximized == 0 ? IMG_UP_ARROW : IMG_DOWN_ARROW);
                   }
               }

               if (visibleMaximizeButtons) {
                   maxLeft.setVisible(true);
                   maxRight.setVisible(true);
               } else {
                   maxLeft.setVisible(false);
                   maxRight.setVisible(false);
               }

               c.setVisible(true);
           } else if (c != maxLeft && c != maxRight) {
               c.setVisible(false);
           }
       }
    }
}
