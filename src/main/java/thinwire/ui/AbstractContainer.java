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
package thinwire.ui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.ui.layout.Layout;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractContainer<C extends Container<T>, T extends Component> extends AbstractComponent<C> implements Container<T> {
    private class ChildList extends AbstractList<T> {
        private ArrayList<T> l = new ArrayList<T>();

        @SuppressWarnings("unchecked")
		private void processRemove(T c) {
            ((AbstractComponent)c).setParent(null);
            if (standardButton == c) updateStandardButton((Button)c, false);
            if (childWithFocus == c) AbstractContainer.this.setChildWithFocus(null);            
        }

        @SuppressWarnings("unchecked")
		private void processAdd(T c) {
            ((AbstractComponent)c).setParent(AbstractContainer.this);
            
            if (c instanceof Button && ((Button)c).isStandard()) {                             
                updateStandardButton((Button)c, true);
            } else if (c instanceof AbstractContainer) {
                Button b = ((AbstractContainer)c).getStandardButton();                
                if (b != null) updateStandardButton(b, true);
            }
            
            if (c.isFocus()) {
                if (!c.isFocus()) c.setFocus(true);
                Container<T> container = AbstractContainer.this;
                
                while (childWithFocus == null) {
                    container = (Container<T>)container.getParent();
                    if (container == null) break;
                    childWithFocus = container.getChildWithFocus();
                }
                                
		        if (childWithFocus != null) childWithFocus.setFocus(false);
	        	AbstractContainer.this.setChildWithFocus(c);
	        	AbstractContainer.this.setFocus(true);
	        }
        }

        public T get(int index) {
            return l.get(index);
        }

        public void add(int index, T o) {
            if (o == null) throw new IllegalArgumentException("o == null");
            if (o == AbstractContainer.this) throw new IllegalArgumentException("cannot add component to itself");
            if (o.getParent() != null) throw new IllegalArgumentException("cannot add component to multiple containers or twice to the same container");
            if (o instanceof Dialog) throw new IllegalArgumentException("cannot add a dialog to a container");
            if (!TabFolder.class.isInstance(AbstractContainer.this) && o instanceof TabSheet) throw new IllegalArgumentException("cannot add a TabSheet to a container that is not a TabFolder");
            l.add(index, o);
            processAdd(o);
            icei.fireItemChange(null, Type.ADD, new Integer(index), null, o);
        }

        public T remove(int index) {
            T ret = l.get(index);
            l.remove(index);
            processRemove(ret);
            icei.fireItemChange(null, Type.REMOVE, new Integer(index), ret, null);
            return ret;
        }

        public T set(int index, T o) {
            if (o == null) throw new IllegalArgumentException("o == null");
            if (o == AbstractContainer.this) throw new IllegalArgumentException("cannot add component to itself");
            if (o.getParent() != null) {
                if (o == l.get(index)) return o;
                throw new IllegalArgumentException("cannot add component to multiple containers or twice to the same container");
            }
            
            T ret = l.set(index, o);
            processRemove(ret);
            processAdd(o);
            icei.fireItemChange(null, Type.SET, new Integer(index), ret, o);
            return ret;
        }

        public int size() {
            return l.size();
        }
    }

    private EventListenerImpl<ItemChangeListener> icei;
    private List<T> children;
    private ScrollType scroll = ScrollType.NONE;
    private Layout layout;
    private T childWithFocus;
    private Button standardButton;
    
    AbstractContainer() {
    	EventListenerImpl<ItemChangeListener> gicei = app == null ? null : app.getGlobalListenerSet(ItemChangeListener.class, false);
    	icei = new EventListenerImpl<ItemChangeListener>(this, ItemChangeListener.class, null, gicei);
        children = new ChildList();
    }

    @SuppressWarnings("unchecked")
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
    
    public ScrollType getScrollType() {
        return scroll;
    }
        
    public void setScrollType(ScrollType scrollType) {
        if (scrollType == null) throw new IllegalArgumentException("scrollType == null");
        ScrollType oldScroll = this.scroll;
        this.scroll = scrollType;
        firePropertyChange(this, PROPERTY_SCROLL_TYPE, oldScroll, scrollType);
    }
    
    public Layout getLayout() {
        return layout;
    }
    
    @SuppressWarnings("unchecked")
    public void setLayout(Layout layout) {
        Layout oldLayout = this.layout;
        this.layout = layout;
        if (oldLayout != null && oldLayout.getContainer() == this) oldLayout.setContainer(null);
        if (layout != null && layout.getContainer() != this) layout.setContainer((Container<Component>)this);
        firePropertyChange(this, PROPERTY_LAYOUT, oldLayout, layout);
    }

    @SuppressWarnings("unchecked")
    public C addItemChangeListener(ItemChangeListener listener) {
        icei.addListener(listener);
        return (C)this;
    }

    @SuppressWarnings("unchecked")
    public C removeItemChangeListener(ItemChangeListener listener) {
        icei.removeListener(listener);
        return (C)this;
    }

    public List<T> getChildren() {
        return children;
    }

    void setChildWithFocus(T childWithFocus) {
        this.childWithFocus = childWithFocus;
    }

    public T getChildWithFocus() {
        return childWithFocus;
    }
    
    @SuppressWarnings("unchecked")
    public T getComponentWithFocus() {
        T ret = null;
        Object root = app != null ? app.getFrame() : this;
        Object parent;
        
        
        //Walk up the tree to the root container
        while (root instanceof Container && (parent = ((Container)root).getParent()) != null) {
            root = parent;
        }

        //If the root is a container, walk down the children with focus to get the component with focus.
        if (root instanceof Container) {
            ret = (T)root;
                        
	        while (ret instanceof Container) {
	            ret = ((Container<T>)ret).getChildWithFocus();
	        }
    	}
    
        return ret;
    }
    
    public int getInnerWidth() {
        int innerWidth = getWidth() - getStyle().getBorder().getSize() * 2;
        return innerWidth < 0 ? 0 : innerWidth;
    }
    
    public int getInnerHeight() {
        int innerHeight = getHeight() - getStyle().getBorder().getSize() * 2;
        return innerHeight < 0 ? 0 : innerHeight;
    }    
}
