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
package thinwire.ui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.ui.style.Style;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractContainer extends AbstractComponent implements Container {
    private class ItemChangeList extends AbstractList<Component> {
        private ArrayList<Component> l = new ArrayList<Component>();

        private void processRemove(Component c) {
            ((AbstractComponent)c).setParent(null);
            if (standardButton == c) updateStandardButton((Button)c, false);
            if (childWithFocus == c) AbstractContainer.this.setChildWithFocus(null);            
        }

        private void processAdd(Component c) {
            if (c == AbstractContainer.this) throw new IllegalArgumentException("cannot add component to itself");
            if (c.getParent() != null)
                throw new IllegalArgumentException("cannot add component to multiple containers or twice to the same container");
                        
            ((AbstractComponent)c).setParent(AbstractContainer.this);
            
            if (c instanceof Button && ((Button)c).isStandard()) {                             
                updateStandardButton((Button)c, true);
            } else if (c instanceof AbstractContainer) {
                Button b = ((AbstractContainer)c).getStandardButton();                
                if (b != null) updateStandardButton(b, true);
            }
            
            if (c.isFocus()) {
                if (!c.isFocus()) c.setFocus(true);
                AbstractContainer container = AbstractContainer.this;
                
                while (childWithFocus == null) {
                    container = (AbstractContainer)container.getParent();
                    if (container == null) break;
                    childWithFocus = container.getChildWithFocus();
                }
                                
		        if (childWithFocus != null) childWithFocus.setFocus(false);
	        	AbstractContainer.this.setChildWithFocus(c);
	        	AbstractContainer.this.setFocus(true);
	        }
        }

        public Component get(int index) {
            return l.get(index);
        }

        public void add(int index, Component o) {
            l.add(index, o);
            processAdd(o);
            icei.fireItemChange(this, Type.ADD, new Integer(index), null, o);
        }

        public Component remove(int index) {
            Component ret = l.get(index);
            l.remove(index);
            processRemove(ret);
            icei.fireItemChange(this, Type.REMOVE, new Integer(index), ret, null);
            return ret;
        }

        public Component set(int index, Component o) {
            Component ret = l.set(index, o);
            processRemove(ret);
            processAdd(o);
            icei.fireItemChange(this, Type.SET, new Integer(index), ret, o);
            return ret;
        }

        public int size() {
            return l.size();
        }
    }

    static {
        Style s = new Style(getDefaultStyle(Component.class)); //inherit defaults from Component class
        setDefaultStyle(Container.class, s);
    }
    
    static final int BORDER_WIDTH = 2;
    static final int PADDING_WIDTH = 1;
    static final int CALC_BORDER_SUB = BORDER_WIDTH * 2;
    static final int CALC_BORDER_PADDING_SUB = (BORDER_WIDTH + PADDING_WIDTH) * 2;    

    private ScrollType scroll = ScrollType.NONE;
    private EventListenerImpl<ItemChangeListener> icei = new EventListenerImpl<ItemChangeListener>();
    private List<Component> children;
    private Component childWithFocus;
    private Button standardButton;
    
    AbstractContainer() {        
        this(true);
    }
    
    AbstractContainer(boolean visible) {
        super(visible);
        children = new ItemChangeList();
    }
    

    void updateStandardButton(Button button, boolean standard) {
        if (this.standardButton == button) {
            if (!standard) {
                this.standardButton.setStandard(false);
                this.standardButton = null;
            } else {
                //nothing, because it's already set;
            }
        } else {
            if (standard) {
                if (this.standardButton != null) this.standardButton.setStandard(false);
                this.standardButton = button;
            } else {
                //nothing, because a non-standard button that doesn't match, does not update the current
            }
        }

        Object parent = getParent();        
        if (parent instanceof AbstractContainer)((AbstractContainer)parent).updateStandardButton(button, standard);
    }

    Button getStandardButton() {
        return standardButton;
    }
        
    public void setScroll(ScrollType scrollType) {
        if (scrollType == null) throw new IllegalArgumentException("scrollType == null");
        ScrollType oldScroll = this.scroll;
        this.scroll = scrollType;
        firePropertyChange(this, PROPERTY_SCROLL, oldScroll, scrollType);
    }
    
    public ScrollType getScroll() {
        return scroll;
    }

    public void addItemChangeListener(ItemChangeListener listener) {
        icei.addListener(listener);
    }

    public void removeItemChangeListener(ItemChangeListener listener) {
        icei.removeListener(listener);
    }

    public List<Component> getChildren() {
        return children;
    }

    void setChildWithFocus(Component childWithFocus) {
        this.childWithFocus = childWithFocus;
    }

    public Component getChildWithFocus() {
        return childWithFocus;
    }
    
    public Component getComponentWithFocus() {
        Component ret = null;
        Object root = this;
        Object parent;
        
        //Walk up the tree to the root container
        while (root instanceof AbstractContainer && (parent = ((AbstractContainer)root).getParent()) != null)
            root = parent;

        //If the root is a container, walk down the children with focus to get the component with focus.
        if (root instanceof AbstractContainer) {
            ret = (AbstractContainer)root;
                        
	        while (ret instanceof AbstractContainer)
	            ret = ((Container)ret).getChildWithFocus();
    	}
    
        return ret;
    }
    
    public int getInnerWidth() {
        return this.getWidth();
    }
    
    public int getInnerHeight() {        
        return this.getHeight();
    }    
}
