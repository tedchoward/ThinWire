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
package thinwire.render.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thinwire.ui.Component;
import thinwire.ui.Window;
import thinwire.ui.HierarchyComponent;
import thinwire.ui.Menu;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.KeyPressEvent;
import thinwire.ui.event.KeyPressListener;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 * @author Tom E. Kimball
 */
final class MenuRenderer extends ComponentRenderer implements ItemChangeListener {    
    private static final String MENU_CLASS = "tw_Menu";
    private static final String ITEM_REMOVE = "itemRemove";
    private static final String ITEM_LOAD = "itemLoad";
    private static final String ITEM_SET_TEXT = "itemSetText";
    private static final String ITEM_SET_KEY_PRESS_COMBO = "itemSetKeyPressCombo";
    private static final String ITEM_SET_IMAGE_URL = "itemSetImageUrl";
    private static final String ITEM_SET_ENABLED = "itemSetEnabled";
    private Menu menu;
    private Map<Menu.Item, KeyPressListener> itemToKeyPressListeners;
    private StringBuilder sb;
	
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {        
        init(MENU_CLASS, wr, c, container);
        boolean windowMenu = container instanceof WindowRenderer;

        //a menu does not support the focus, enabled, x, y, width or height properties
        if (windowMenu) {
            setPropertyChangeIgnored(Component.PROPERTY_X, true);
            setPropertyChangeIgnored(Component.PROPERTY_Y, true);
            setPropertyChangeIgnored(Component.PROPERTY_WIDTH, true);
            setPropertyChangeIgnored(Component.PROPERTY_VISIBLE, true);
        }

        setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);
        setPropertyChangeIgnored(Component.PROPERTY_FOCUS, true);

		menu = (Menu)c;
        sb = new StringBuilder();
        buildMenuChildrenInit((Menu.Item)menu.getRootItem(), true);
        menu.addItemChangeListener(this);
        addInitProperty("initData", sb);        
        addInitProperty("windowMenu", windowMenu);
        super.render(wr, c, container);
		sb.setLength(0);
	}
    
    void destroy() {        
        Window w = (Window)wr.comp;
        super.destroy();
        
        if (itemToKeyPressListeners != null) {
            for (KeyPressListener l : itemToKeyPressListeners.values()) {
                w.removeKeyPressListener(l);
            }
        }
        
        menu.removeItemChangeListener(this);
        menu = null;
        sb = null;
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        Object newValue = pce.getNewValue();
        Object source = pce.getSource();
        
        if (source instanceof Menu.Item) {
            Menu.Item item = (Menu.Item)source;
            String fullIndex = fullIndex(item);

            if (name.equals(HierarchyComponent.Item.PROPERTY_ITEM_TEXT)) {
                String text = (String)newValue;
                String oldText = (String)pce.getOldValue();
                boolean oldDivider = oldText.length() < 1;
                boolean newDivider = text.length() < 1;
                
                if (oldDivider != newDivider) {
                    postClientEvent(ITEM_REMOVE, fullIndex);
                    fullIndex = fullIndex((Menu.Item)item.getParent());
                    
                    if (text.length() == 0) {
                        buildDividerInit((Menu.Item)source, item.getIndex());
                    } else {
                        buildMenuInit((Menu.Item)source, item.getIndex(), false);
                    }
                    
                    postClientEvent(ITEM_LOAD, sb.toString(), fullIndex); 
                    sb.setLength(0);
                } else {
                    postClientEvent(ITEM_SET_TEXT, fullIndex, text);
                }
            } else if (name.equals(HierarchyComponent.Item.PROPERTY_ITEM_IMAGE)) {
                postClientEvent(ITEM_SET_IMAGE_URL, fullIndex, getQualifiedURL((String)newValue));                              
            } else if (name.equals(Menu.Item.PROPERTY_ITEM_KEY_PRESS_COMBO)) {
                setupKeyPressListener(item);
                postClientEvent(ITEM_SET_KEY_PRESS_COMBO, fullIndex, newValue);
            } else if (name.equals(Menu.Item.PROPERTY_ITEM_ENABLED)) {
                postClientEvent(ITEM_SET_ENABLED, fullIndex, newValue);
            }
        } else {
            super.propertyChange(pce);
        }
    }
    
    public void itemChange(ItemChangeEvent ice) {
        ItemChangeEvent.Type type = ice.getType();        
        Menu.Item container = (Menu.Item)ice.getSource();
        Integer pos = (Integer)ice.getPosition();
        
        if (type == ItemChangeEvent.Type.REMOVE || type == ItemChangeEvent.Type.SET) {
            String fullIndex = pos.toString();
            if (menu.getRootItem() != container) fullIndex = fullIndex(container) + "." + fullIndex;
            postClientEvent(ITEM_REMOVE, fullIndex);            
        }

        if (type == ItemChangeEvent.Type.ADD || type == ItemChangeEvent.Type.SET) {
            String fullIndex = fullIndex(container);
            Menu.Item nc = (Menu.Item)ice.getNewValue();
            
            if (nc.getText().length() == 0) { 
                buildDividerInit(nc, pos.intValue());
            } else {
                buildMenuInit(nc, pos.intValue(), false);
            }
            
            postClientEvent(ITEM_LOAD, sb, fullIndex); 
            sb.setLength(0);
        }
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String) event.getValue();
        
        if (name.equals(Menu.ACTION_CLICK)) {
            Menu.Item item = fullIndexItem(menu, value);
            menu.fireAction(Menu.ACTION_CLICK, item);
        } else {
            super.componentChange(event);
        }
    }    

    private void setupKeyPressListener(final Menu.Item item) {
        KeyPressListener listener = new KeyPressListener() {
            public void keyPress(KeyPressEvent ev) {
                Menu menu = item.getHierarchy();                        
                if (menu != null) menu.fireAction(Menu.ACTION_CLICK, item);
            }
        };

        Window w = (Window)wr.comp;
        
        if (itemToKeyPressListeners == null) {
            itemToKeyPressListeners = new HashMap<Menu.Item, KeyPressListener>();
        } else {
            KeyPressListener old = itemToKeyPressListeners.get(item);
            if (old != null) w.removeKeyPressListener(old);
        }
        
        itemToKeyPressListeners.put(item, listener);
        w.addKeyPressListener(item.getKeyPressCombo(), listener);
    }
    
	private void buildMenuInit(Menu.Item item, int index, boolean isRoot) {
		sb.append('{');
		sb.append("en:").append(item.isEnabled());
		String text = getEscapedText(item.getText());		
		if (text.length() > 0) sb.append(",t:\"").append(text.replaceAll("\"", "\\\"")).append('"');
        String keyPressCombo = item.getKeyPressCombo();
        
        if (keyPressCombo.length() > 0) {
            sb.append(",k:\"").append(keyPressCombo).append('"');
            setupKeyPressListener(item);
        }
        
		if (index != -1) sb.append(",x:").append(index);
		
		if (item.getChildren().size() > 0) {
			sb.append(",c:");
			buildMenuChildrenInit(item, false);
		}
		
		String img = item.getImage();
		if (img.length() > 0) sb.append(",g:\"").append(getQualifiedURL(item.getImage())).append('"');		
		sb.append('}');
	}
	
	private void buildMenuChildrenInit(Menu.Item menu, boolean isRoot) {
		sb.append('[');
		List<Menu.Item> content = menu.getChildren();
		
		for (int i = 0, cnt = content.size(); i < cnt; i++) {
			Menu.Item c = content.get(i);			

			if (isRoot || c.getText().length() > 0) { 
				buildMenuInit(c, -1, isRoot);
				sb.append(',');
			} else {
				buildDividerInit(c, -1);
				sb.append(',');
			}
		}
		
		if (sb.charAt(sb.length() - 1) == '[')
			sb.append(']');
		else
			sb.setCharAt(sb.length() - 1, ']');
	}

	private void buildDividerInit(Menu.Item menu, int index) {
		sb.append("{");
		sb.append("en:").append(menu.isEnabled());
		if (index != -1) sb.append(",x:").append(index);
		sb.append('}');
	}

	private static Menu.Item fullIndexItem(Menu root, String value) {
		Menu.Item mi = (Menu.Item)root.getRootItem();
        if (value.equals("rootItem")) return mi;
		String ary[] = value.split("\\.");
		
        for (int i=0; i < ary.length; i++) {
			int index = Integer.parseInt(ary[i]);
			mi = (Menu.Item) mi.getChildren().get(index);
		}
        
		if (ary.length == 0) mi = (Menu.Item)mi.getChildren().get(Integer.parseInt(value));
		return mi;
	}

	private static String fullIndex(Menu.Item item) {
        if (item == item.getHierarchy().getRootItem()) return "rootItem";        
		String value = String.valueOf(item.getIndex());
		Object root = item.getHierarchy().getRootItem();

        while (item.getParent() != root) {
			item = (Menu.Item) item.getParent();
			value = item.getIndex() + "." + value;
		}
        
		return value;  
	}
}
