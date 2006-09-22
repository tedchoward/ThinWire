/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import thinwire.render.Renderer;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;

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
            if (parent.getHierarchy() != null) parent.getHierarchy().icei.fireItemChange(parent, Type.REMOVE, index, item, null);
            return item;
        }

        public int size() {
            return l.size();
        }
    }    
    
    static class Item<H extends AbstractHierarchyComponent, I extends AbstractHierarchyComponent.Item> implements HierarchyComponent.Item<H, I> {        
        private String text = "";
        private Image.Detail imageDetail = new Image.Detail();
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
            return imageDetail.image;
        }

        public void setImage(String image) {
            String oldImage = this.imageDetail.image;
            Image.updateImageDetail(imageDetail, image);        
            H hier = getHierarchy();
            if (hier != null) hier.firePropertyChange(this, PROPERTY_ITEM_IMAGE, oldImage, this.imageDetail.image);
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
    }

    private HI rootItem;    
    private EventListenerImpl<ItemChangeListener> icei = new EventListenerImpl<ItemChangeListener>();
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>();
    
    AbstractHierarchyComponent(HI rootItem) {
        this.rootItem = rootItem;
        rootItem.setParent(this);
    }
    
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
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
        aei.addListener(ACTION_CLICK, listener);
    }
    //#ENDIF

    public void addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
    }
    
    public void addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
    }    

    public void removeActionListener(ActionListener listener) {
        aei.removeListener(listener);
    }

    /**
     * Programmatically cause an action to occur for the given Menu.Item.
     * @param action the name of the action to trigger, such as Menu.ACTION_CLICK.
     * @param item the Menu.Item that the action should be triggered for.
     */
    public void fireAction(String action, HI item) {
        if (action == null || !action.equals(ACTION_CLICK)) throw new IllegalArgumentException("the specified action is not supported");                
        if (item == null) throw new IllegalArgumentException("item == null");
        aei.fireAction(item, action);
    }
}
