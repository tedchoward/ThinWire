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
package thinwire.ui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import thinwire.ui.event.ActionListener;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractHierarchyComponent<C extends HierarchyComponent<HI>, HI extends AbstractHierarchyComponent.Item> extends AbstractComponent<C> implements HierarchyComponent<HI> {        
    private static class ChildList<I extends Item> extends AbstractList<I> {
        private List<I> l = new ArrayList<I>(3);
        private Item parent;

        private ChildList(Item parent) {
            this.parent = parent;
        }

        public void add(int index, I item) {
            if (item.getParent() != null) throw new IllegalStateException("item.getParent() != null");
            l.add(index, item);
            modCount++;
            item.setParent(parent);
            AbstractHierarchyComponent hier = parent.getHierarchy();
            if (hier != null) hier.icei.fireItemChange(parent, Type.ADD, index, null, item);
        }

        public I get(int index) {
            return l.get(index);
        }

        public I set(int index, I item) {
            if (item.getParent() != null) throw new IllegalStateException("item.getParent() != null");
            AbstractHierarchyComponent hier = parent.getHierarchy();
            I ret = l.get(index);
            hier.removingItem(ret);
            l.set(index, item);
            ret.setParent(null);
            item.setParent(parent);
            if (hier != null) hier.icei.fireItemChange(parent, Type.SET, index, ret, item);
            return ret;
        }

        public I remove(int index) {
            modCount++;
            AbstractHierarchyComponent hier = parent.getHierarchy();
            I item = l.get(index);
            hier.removingItem(item);
            l.remove(index);
            item.setParent(null);
            if (hier != null) hier.icei.fireItemChange(parent, Type.REMOVE, index, item, null);
            return item;
        }

        public int size() {
            return l.size();
        }
    }    
    
    static abstract class Item<H extends AbstractHierarchyComponent, I extends AbstractHierarchyComponent.Item> implements HierarchyComponent.Item<H, I> {
        private String text = "";
        private ImageInfo imageInfo = new ImageInfo(null);
        private Object userObject;        
        private Object parent;
        private List<I> children;
                
        public String getText() {
            return text;
        }

        public void setText(String text) {
            String oldText = this.text;
            if (text == null) text = "";
            this.text = text;
            H hier = getHierarchy();
            if (hier != null) hier.firePropertyChange(this, PROPERTY_ITEM_TEXT, oldText, text);
        }
        
        public String getImage() {
            return imageInfo.getName();
        }

        public void setImage(String image) {
            String oldImage = this.imageInfo.getName();
            imageInfo = new ImageInfo(image);        
            H hier = getHierarchy();
            if (hier != null) hier.firePropertyChange(this, PROPERTY_ITEM_IMAGE, oldImage, this.imageInfo.getName());
        }
        
        public ImageInfo getImageInfo() {
            return imageInfo;
        }

        /**
         * Get the developer/user defined object that has been associated to this Tree.Item.
         * @return the general purpose object that has been associated to this Tree.Item.
         */
        public Object getUserObject() {
            return userObject;
        }

        /**
         * Set the developer/user defined object for this Tree.Item.
         */
        public void setUserObject(Object value) {
            Object oldValue = this.userObject;
            this.userObject = value;
            H hier = getHierarchy();
            if (hier != null) hier.firePropertyChange(this, PROPERTY_ITEM_USER_OBJECT, oldValue, this.userObject);
        }
        
        public int getIndex() {
            if (parent == null) throw new IllegalStateException("getParent() == null");
            if (parent instanceof HierarchyComponent) throw new IllegalStateException("getParent() instanceof Hierarchy");
            return ((Item) parent).getChildren().indexOf(this);
        }
        
        public H getHierarchy() {
            Object parent = this;            
            
            while (parent != null && !(parent instanceof AbstractHierarchyComponent))
                parent = ((I)parent).getParent();
        
            return (H)parent;
        }
        
        public Object getParent() {
            return parent;
        }

        void setParent(Object parent) {
            this.parent = parent;
        }            
        
        public boolean hasChildren() {
            return children == null ? false : children.size() > 0;
        }
        
        public List<I> getChildren() {
            if (children == null) children = new ChildList<I>(this);
            return children;
        }
        
        public String toString() {
            return "text:" + text + ",image:" + imageInfo.getName() + ",children.size():" + (children == null ? 0 : children.size()) +
                ",parent:" + (parent == null ? null : parent.getClass().getName() + "@" + System.identityHashCode(parent));
        }
    }

    private HI rootItem;    
    private EventListenerImpl<ItemChangeListener> icei;
    
    AbstractHierarchyComponent(HI rootItem, EventListenerImpl.SubTypeValidator actionValidator) {
        super(actionValidator);
    	EventListenerImpl<ItemChangeListener> gicei = app == null ? null : app.getGlobalListenerSet(ItemChangeListener.class, false);
    	icei = new EventListenerImpl<ItemChangeListener>(this, ItemChangeListener.class, null, gicei);        
        this.rootItem = rootItem;
        rootItem.setParent(this);
    }
    
    void removingItem(HI item) {
        
    }
    
    public HI getRootItem() {
        return rootItem;
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
}
