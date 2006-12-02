/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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
package thinwire.render.web;

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
final class TreeRenderer extends ComponentRenderer implements ItemChangeListener {
    private static final String TREE_CLASS = "tw_Tree";
    private static final String ITEM_REMOVE = "itemRemove";
    private static final String ITEM_EXPAND = "itemExpand";
    private static final String ITEM_CHANGE = "itemChange";
    private static final String ITEM_SELECT = "itemSelect";
    private static final String SET_ROOT_ITEM_VISIBLE = "setRootItemVisible";
    private static final String ITEM_ADD = "itemAdd";
    private Tree tree;
    
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {
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
    
    void destroy() {
        super.destroy();
        tree.removeItemChangeListener(this);
        tree = null;
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
            } else if (name.equals(HierarchyComponent.Item.PROPERTY_ITEM_IMAGE) || name.equals(HierarchyComponent.Item.PROPERTY_ITEM_TEXT)) {
                Object newTextValue = RICH_TEXT_PARSER.parseRichText(((Tree.Item)source).getText(), this);
                if (newTextValue instanceof String) newTextValue = getEscapedText((String) newTextValue);
                postClientEvent(ITEM_CHANGE, fullIndex, newTextValue, getQualifiedURL(((Tree.Item)source).getImage()));
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
            postClientEvent(ITEM_REMOVE, fullIndex);
        }
        
        if (type == ItemChangeEvent.Type.ADD || type == ItemChangeEvent.Type.SET) {
            Object newTextValue = RICH_TEXT_PARSER.parseRichText(newValue.getText(), this);
            if (newTextValue instanceof String) newTextValue = getEscapedText((String) newTextValue);
            postClientEvent(ITEM_ADD, fullIndex, newTextValue, getQualifiedURL(newValue.getImage()));
            if (newValue.hasChildren()) renderChildren(newValue);
            if (((Tree.Item)newValue.getParent()).isExpanded() && newValue.getIndex() == 0) {
                postClientEvent(ITEM_EXPAND, fullIndex(((Tree.Item)newValue.getParent())), true);
            }
        }
    }
    
    private void renderChildren(Tree.Item parent) {
        //XXX: This may not be the most efficient way to handle this, consider sending initdata instead.        
        for (Tree.Item item : parent.getChildren()) {
            int index = item.getIndex();
            String fullIndex = String.valueOf(index);            
            if (parent != tree.getRootItem()) fullIndex = fullIndex(parent) + "." + index;  
            Object newTextValue = RICH_TEXT_PARSER.parseRichText(item.getText(), this);
            if (newTextValue instanceof String) newTextValue = getEscapedText((String) newTextValue);
            postClientEvent(ITEM_ADD, fullIndex, newTextValue, getQualifiedURL(item.getImage()));            
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
        Object textValue = RICH_TEXT_PARSER.parseRichText(ri.getText(), this);
        if (textValue instanceof String) {
            sb.append(",rt:\"").append(getEscapedText((String) textValue)).append("\"");
        } else {
            sb.append(",rt:").append(textValue);
        }
        sb.append(",ri:\"").append(getQualifiedURL(ri.getImage())).append('"');        
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
                sb.append(",tm:\"").append(getQualifiedURL(ti.getImage())).append('"');
                Object textValue = RICH_TEXT_PARSER.parseRichText(ti.getText(), this);
                if (textValue instanceof String) {
                    sb.append(",tt:\"").append(getEscapedText((String) textValue)).append("\"");
                } else {
                    sb.append(",tt:").append(textValue);
                }
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
