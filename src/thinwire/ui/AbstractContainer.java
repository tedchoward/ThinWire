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

import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.ui.layout.DefaultUnitModel;
import thinwire.ui.layout.UnitModel;

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

    private ScrollType scroll = ScrollType.NONE;
    private EventListenerImpl<ItemChangeListener> icei = new EventListenerImpl<ItemChangeListener>(this);
    private List<T> children;
    private T childWithFocus;
    private Button standardButton;
    private UnitModel unitModel;
    
    AbstractContainer() {        
        this(true);
    }
    
    AbstractContainer(boolean visible) {
        super(visible);
        children = new ItemChangeList();
        setUnitModel(new DefaultUnitModel());
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
        int innerWidth;
        if (getParent() != null) {
            innerWidth = ((Container) getParent()).getUnitModel().getWidth(this) - getStyle().getBorder().getSize() * 2;
        } else {
            innerWidth = getWidth() - getStyle().getBorder().getSize() * 2;
        }
        return innerWidth < 0 ? 0 : innerWidth;
    }
    
    public int getInnerHeight() {
        int innerHeight;
        if (getParent() != null) {
            innerHeight = ((Container) getParent()).getUnitModel().getHeight(this) - getStyle().getBorder().getSize() * 2;
        } else {
            innerHeight = getHeight() - getStyle().getBorder().getSize() * 2;
        }
        return innerHeight < 0 ? 0 : innerHeight;
    }    
    
    public void setUnitModel(UnitModel unitModel) {
        UnitModel oldModel = this.unitModel;
        this.unitModel = unitModel;
        this.unitModel.init(this);
        firePropertyChange(this, PROPERTY_UNIT_MODEL, oldModel, this.unitModel);
    }

    public UnitModel getUnitModel() {
        return unitModel;
    }
}
