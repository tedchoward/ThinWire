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
package thinwire.ui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractHierarchyComponent<HI extends AbstractHierarchyComponent.Item> extends AbstractComponent implements HierarchyComponent<HI> {        
    private static class ChildList<I extends Item> extends AbstractList<I> {
        private List<I> l = new ArrayList<I>(3);
        private Item parent;

        private ChildList(Item parent) {
            this.parent = parent;
        }

        public void add(int index, I item) {
            if (item.parent != null) throw new IllegalStateException("item.getParent() != null");
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
            if (item.parent != null) throw new IllegalStateException("item.getParent() != null");
            I ret = l.set(index, item);
            ret.setParent(null);
            item.setParent(parent);
            AbstractHierarchyComponent hier = parent.getHierarchy();
            if (hier != null) hier.icei.fireItemChange(parent, Type.SET, index, ret, item);
            return ret;
        }

        public I remove(int index) {
            modCount++;
            I item = l.remove(index);
            item.setParent(null);
            AbstractHierarchyComponent hier = parent.getHierarchy();
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

        public Object getUserObject() {
            return userObject;
        }

        public void setUserObject(Object value) {
            Object oldValue = this.userObject;
            this.userObject = value;
            H hier = getHierarchy();
            if (hier != null) hier.firePropertyChange(this, PROPERTY_ITEM_USER_OBJECT, oldValue, this.userObject);
        }
        //#IFDEF V1_1_COMPAT
        
        /**
         * @return value from getUserObject()
         * @deprecated in favor of getUserObject
         * @see #getUserObject()
         */
        public Object getValue() {
            if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use getUserObject() instead.");            
            return getUserObject();
        }

        /**
         * @param value value to pass to setUserObject()
         * @deprecated in favor of setUserObject
         * @see #setUserObject(Object)
         */
        public void setValue(Object value) {
            if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use setUserObject() instead.");            
            setUserObject(value);
        }
        //#ENDIF
        
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

        private void setParent(Object parent) {
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
    private EventListenerImpl<ItemChangeListener> icei = new EventListenerImpl<ItemChangeListener>(this);
    
    AbstractHierarchyComponent(HI rootItem, EventListenerImpl.SubTypeValidator actionValidator) {
        super(actionValidator);
        this.rootItem = rootItem;
        rootItem.setParent(this);
    }
    
    public HI getRootItem() {
        return rootItem;
    }

    public void addItemChangeListener(ItemChangeListener listener) {
        icei.addListener(listener);
    }

    public void removeItemChangeListener(ItemChangeListener listener) {
        icei.removeListener(listener);
    }    
    //#IFDEF V1_1_COMPAT
    
    /**
     * Register an ActionListener that will be notified when a Menu action occurs. Actions: Menu.ACTION_CLICK - Fired when a
     * Menu.Item is clicked.
     * @param listener the listener that is to be notified when a Menu action occurs.
     * @deprecated for performance reasons, this form as been deprecated.  Use the named action form instead.
     */
    public void addActionListener(ActionListener listener) {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use addActionListener(action, listener) instead.");        
        addActionListener(ACTION_CLICK, listener);
    }
    //#ENDIF
}
