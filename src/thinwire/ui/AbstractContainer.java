/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
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
abstract class AbstractContainer<T extends Component> extends AbstractComponent implements Container<T> {
    private class ItemChangeList extends AbstractList<T> {
        private ArrayList<T> l = new ArrayList<T>();

        private void processRemove(T c) {
            ((AbstractComponent)c).setParent(null);
            if (standardButton == c) updateStandardButton((Button)c, false);
            if (childWithFocus == c) AbstractContainer.this.setChildWithFocus(null);            
        }

        private void processAdd(T c) {
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
            l.add(index, o);
            processAdd(o);
            icei.fireItemChange(this, Type.ADD, new Integer(index), null, o);
        }

        public T remove(int index) {
            T ret = l.get(index);
            l.remove(index);
            processRemove(ret);
            icei.fireItemChange(this, Type.REMOVE, new Integer(index), ret, null);
            return ret;
        }

        public T set(int index, T o) {
            T ret = l.set(index, o);
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
    private List<T> children;
    private T childWithFocus;
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

    public List<T> getChildren() {
        return children;
    }

    void setChildWithFocus(T childWithFocus) {
        this.childWithFocus = childWithFocus;
    }

    public T getChildWithFocus() {
        return childWithFocus;
    }
    
    public T getComponentWithFocus() {
        T ret = null;
        Object root = this;
        Object parent;
        
        //Walk up the tree to the root container
        while (root instanceof Container && (parent = ((Container)root).getParent()) != null)
            root = parent;

        //If the root is a container, walk down the children with focus to get the component with focus.
        if (root instanceof Container) {
            ret = (T)root;
                        
	        while (ret instanceof Container)
	            ret = ((Container<T>)ret).getChildWithFocus();
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
