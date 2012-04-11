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
package thinwire.render.web;

import java.util.ArrayList;
import java.util.List;

import thinwire.ui.Component;
import thinwire.ui.HierarchyComponent;
import thinwire.ui.Tree;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 * @author Tom E. Kimball
 */
public final class TreeRenderer extends ComponentRenderer implements ItemChangeListener {
    private static final String TREE_CLASS = "tw_Tree";
    private static final String ITEM_REMOVE = "itemRemove";
    private static final String ITEM_EXPAND = "itemExpand";
    private static final String ITEM_CHANGE = "itemChange";
    private static final String ITEM_SELECT = "itemSelect";
    private static final String SET_ROOT_ITEM_VISIBLE = "setRootItemVisible";
    private static final String ITEM_ADD = "itemAdd";
    private Tree tree;
    private List<String> resources;
    
    protected void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        init(TREE_CLASS, wr, c, container);
        tree = (Tree)c;
        StringBuilder sb = new StringBuilder();
        prepareInitData(sb);
        addClientSideProperty(Tree.Item.PROPERTY_ITEM_EXPANDED);
        addClientSideProperty(Tree.Item.PROPERTY_ITEM_SELECTED);
        addInitProperty("initData", sb);        
        tree.addItemChangeListener(this);
        super.render(wr, c, container);
        Tree.Item item = tree.getSelectedItem();
        if (item != null) postClientEvent(ITEM_SELECT, fullIndex((Tree.Item)item));
	}
    
    protected void destroy() {
    	if (resources != null) {
        	for (String uri : resources) {
	    		wr.ai.removeResourceMapping(uri);
	    	}
	    	
	    	resources = null;
    	}
    	
        super.destroy();
        tree.removeItemChangeListener(this);
        tree = null;
    }
    
    private String addResourceRef(String newUri, String oldUri) {
    	if (resources == null) {
    		resources = new ArrayList<String>(3);
    	} else if (oldUri != null) {
    		resources.remove(oldUri);
    		wr.ai.removeResourceMapping(oldUri);
    	}
    	
    	resources.add(newUri);
    	return wr.ai.addResourceMapping(newUri);
    }
    
    private void removeResourceRef(String oldUri) {
    	if (resources == null) return;
		resources.remove(oldUri);
		wr.ai.removeResourceMapping(oldUri);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        if (isPropertyChangeIgnored(name)) return;        
        Object source = pce.getSource();
        Object newValue = pce.getNewValue();

        if (source instanceof Tree.Item) {
            String fullIndex = fullIndex((Tree.Item)source);

            if (name.equals(Tree.Item.PROPERTY_ITEM_EXPANDED)) {
                if (((Tree.Item) source).hasChildren()) postClientEvent(ITEM_EXPAND, fullIndex, newValue);
            } else if (name.equals(HierarchyComponent.Item.PROPERTY_ITEM_TEXT)) {
            	Object newTextValue = GridBoxRenderer.getValue(this, newValue, null, null);
            	String image = ((Tree.Item)source).getImage();
                postClientEvent(ITEM_CHANGE, fullIndex, newTextValue, addResourceRef(image, image));
            } else if (name.equals(HierarchyComponent.Item.PROPERTY_ITEM_IMAGE)) {
            	Object newTextValue = GridBoxRenderer.getValue(this, ((Tree.Item)source).getText(), null, null);
                postClientEvent(ITEM_CHANGE, fullIndex, newTextValue, addResourceRef((String)newValue, (String)pce.getOldValue()));
            } else if (name.equals(Tree.Item.PROPERTY_ITEM_SELECTED)) {
                postClientEvent(ITEM_SELECT, fullIndex);
            }
        } else if (source instanceof Tree) {            
            if (name.equals(Tree.PROPERTY_ROOT_ITEM_VISIBLE)) {
                postClientEvent(SET_ROOT_ITEM_VISIBLE, newValue);                
            } else {
                super.propertyChange(pce);
            }
        } else {
            super.propertyChange(pce);
        }
    }
        
    public void itemChange(ItemChangeEvent ice) {
        ItemChangeEvent.Type type = ice.getType();
        Tree.Item newValue = (Tree.Item)ice.getNewValue();               
        Integer index = (Integer)ice.getPosition();
        Tree.Item parent = (Tree.Item)ice.getSource();
        String fullIndex = index.toString();
        if (parent != tree.getRootItem()) fullIndex = fullIndex(parent) + "." + index;
        
        if (type == ItemChangeEvent.Type.REMOVE || type == ItemChangeEvent.Type.SET) {
        	removeResourceRef(((Tree.Item)ice.getOldValue()).getImage());
            postClientEvent(ITEM_REMOVE, fullIndex);
        }
        
        if (type == ItemChangeEvent.Type.ADD || type == ItemChangeEvent.Type.SET) {
        	Object newTextValue = GridBoxRenderer.getValue(this, newValue.getText(), null, null);
            postClientEvent(ITEM_ADD, fullIndex, newTextValue, addResourceRef(newValue.getImage(), null));
            if (newValue.hasChildren()) renderChildren(newValue);
            if (((Tree.Item)newValue.getParent()).isExpanded() && newValue.getIndex() == 0) {
                postClientEvent(ITEM_EXPAND, fullIndex(((Tree.Item)newValue.getParent())), true);
            }
        }
        
        if (type == ItemChangeEvent.Type.ADD) {
        	Tree tree = newValue.getHierarchy();
			if (tree.getRootItem().getChildren().size() == 1 || newValue.isSelected()) newValue.setSelected(true);
        } else if (type == ItemChangeEvent.Type.SET) {
			if (((Tree.Item) ice.getOldValue()).isSelected()) (newValue).setSelected(true);
		}
    }
    
    private void renderChildren(Tree.Item parent) {
        //XXX: This may not be the most efficient way to handle this, consider sending initdata instead.        
        for (Tree.Item item : parent.getChildren()) {
            int index = item.getIndex();
            String fullIndex = String.valueOf(index);            
            if (parent != tree.getRootItem()) fullIndex = fullIndex(parent) + "." + index;  
            Object newTextValue = GridBoxRenderer.getValue(this, item.getText(), null, null);
            postClientEvent(ITEM_ADD, fullIndex, newTextValue, addResourceRef(item.getImage(), null));            
            if (item.getChildren().size() > 0) renderChildren(item);
        }
    }

    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();

        if (name.equals(Tree.Item.PROPERTY_ITEM_SELECTED)) {
            Tree.Item ti = (Tree.Item)fullIndexItem(tree, value);
            setPropertyChangeIgnored(name, true);
            ti.setSelected(true);
            setPropertyChangeIgnored(name, false);
        } else if (name.equals(Tree.Item.PROPERTY_ITEM_EXPANDED)) {
            Tree.Item ti = (Tree.Item)fullIndexItem(tree, value.substring(1));
            setPropertyChangeIgnored(name, true);
            ti.setExpanded(value.charAt(0) == 't');
            setPropertyChangeIgnored(name, false);            
        } else {
            super.componentChange(event);
        }
    }    

	static HierarchyComponent.Item fullIndexItem(HierarchyComponent root, String value) {
        if (value.equals("")) return null;
        HierarchyComponent.Item ti = (HierarchyComponent.Item)root.getRootItem();
		if (value.equals("rootItem")) return ti;
		String ary[] = value.split("\\.");
        
		for (int i=0; i < ary.length; i++) {
			int index = Integer.parseInt(ary[i]);
			ti = (HierarchyComponent.Item) ti.getChildren().get(index);
		}
        
		if (ary.length == 0) ti = (HierarchyComponent.Item)ti.getChildren().get(Integer.parseInt(value));
		return ti;
	}

	static String fullIndex(HierarchyComponent.Item item) {
		if (item == item.getHierarchy().getRootItem()) return "rootItem";
        StringBuilder sb = new StringBuilder(String.valueOf(item.getIndex()));
		Object root = item.getHierarchy().getRootItem();
        
		while (item.getParent() != root) {
			item = (HierarchyComponent.Item) item.getParent();
            sb.insert(0, '.').insert(0, item.getIndex());
		}
        
		return sb.toString();  
	}
    
	private void prepareInitData(StringBuilder sb) {
        Tree.Item ri = (Tree.Item)tree.getRootItem();
	    sb.append("{ch:").append(tree.getHeight());
	    sb.append(",cw:").append(tree.getWidth());
	    sb.append(",fo:").append(tree.isFocus());
	    sb.append(",rt:");
        GridBoxRenderer.getValue(this, ri.getText(), null, sb);
        sb.append(",ri:\"").append(addResourceRef(ri.getImage(), null)).append('"');        
	    sb.append(",re:").append(ri.isExpanded());
	    sb.append(",rv:").append(tree.isRootItemVisible());
	    sb.append(',');
        prepareInitData(sb, ri);
        sb.append('}');
	}
	
	private void prepareInitData(StringBuilder sb, Tree.Item tn) {
	    List<Tree.Item> l = tn.getChildren();
	    sb.append("ti:[");
        
        for (int i = 0; i < l.size(); i++) {
			Tree.Item ti = l.get(i);
			
            if (ti != null) {
			    sb.append("{ix:").append(i);
                sb.append(",tm:\"").append(addResourceRef(ti.getImage(), null)).append('"');
                sb.append(",tt:");
                GridBoxRenderer.getValue(this, ti.getText(), null, sb);
			    sb.append(",te:").append(ti.isExpanded());
                sb.append(',');
				prepareInitData(sb, ti);
				sb.append("},");
			} 
		}
        
        if (sb.charAt(sb.length() - 1) != '[')
            sb.setCharAt(sb.length() - 1, ']');
        else
            sb.append(']');
	}
}
