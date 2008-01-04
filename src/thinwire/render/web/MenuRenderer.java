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

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import thinwire.ui.Component;
import thinwire.ui.Window;
import thinwire.ui.HierarchyComponent;
import thinwire.ui.Menu;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.KeyPressEvent;
import thinwire.ui.event.KeyPressListener;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class MenuRenderer extends ComponentRenderer implements ItemChangeListener {    
	private static final Pattern REGEX_AMP = Pattern.compile("[&](\\w)");
	private static final Pattern REGEX_DAMP = Pattern.compile("[&]{1,2}");
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
    private List<String> resources;
	
	void render(WindowRenderer wr, Component c, ComponentRenderer container) {        
        init(MENU_CLASS, wr, c, container);

        boolean windowMenu = container instanceof WindowRenderer && comp.equals(((Window) ((WindowRenderer) container).comp).getMenu());

        //a menu does not support the focus, x, y, width or height properties
        if (windowMenu) {
            setPropertyChangeIgnored(Component.PROPERTY_X, true);
            setPropertyChangeIgnored(Component.PROPERTY_Y, true);
            setPropertyChangeIgnored(Component.PROPERTY_WIDTH, true);
            setPropertyChangeIgnored(Component.PROPERTY_VISIBLE, true);
        }

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

    void destroy() {
    	if (resources != null) {
	    	for (String uri : resources) {
	    		wr.ai.removeResourceMapping(uri);
	    	}
	    	
	    	resources = null;
    	}
    	
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
            String fullIndex = TreeRenderer.fullIndex(item);

            if (name.equals(HierarchyComponent.Item.PROPERTY_ITEM_TEXT)) {
                String text = (String)newValue;
                String oldText = (String)pce.getOldValue();
                boolean oldDivider = oldText.length() < 1;
                boolean newDivider = text.length() < 1;
                
                if (oldDivider != newDivider) {
                    postClientEvent(ITEM_REMOVE, fullIndex);
                    fullIndex = TreeRenderer.fullIndex((Menu.Item)item.getParent());
                    
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
                postClientEvent(ITEM_SET_IMAGE_URL, fullIndex, addResourceRef((String)newValue, (String)pce.getOldValue()));                              
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
            if (menu.getRootItem() != container) fullIndex = TreeRenderer.fullIndex(container) + "." + fullIndex;
            postClientEvent(ITEM_REMOVE, fullIndex);            
        }

        if (type == ItemChangeEvent.Type.ADD || type == ItemChangeEvent.Type.SET) {
            String fullIndex = TreeRenderer.fullIndex(container);
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

    private void setupKeyPressListener(final Menu.Item item) {
        Window w = (Window)wr.comp;
        
        if (itemToKeyPressListeners == null) itemToKeyPressListeners = new HashMap<Menu.Item, KeyPressListener>();
        KeyPressListener listener = itemToKeyPressListeners.get(item);

        if (listener == null) {
            listener = new KeyPressListener() {
                public void keyPress(KeyPressEvent ev) {
                    Menu menu = item.getHierarchy();                        
                    if (menu != null) menu.fireAction(new ActionEvent(Menu.ACTION_CLICK, menu, item));
                }
            };
        	
            itemToKeyPressListeners.put(item, listener);
            w.addKeyPressListener(item.getKeyPressCombo(), listener);
        }        
    }
    
	private void buildMenuInit(Menu.Item item, int index, boolean isRoot) {
		sb.append('{');
		sb.append("en:").append(item.isEnabled());
    	String text = item.getText();

    	if (text.indexOf('&') >= 0) {
    		text = REGEX_AMP.matcher(text).replaceFirst("<u>$1</u>");
    		text = REGEX_DAMP.matcher(text).replaceAll("&amp;");
    	}
	
        sb.append(",t:");
        GridBoxRenderer.getValue(this, text, null, sb);
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
		if (img.length() > 0) sb.append(",g:\"").append(addResourceRef(item.getImage(), null)).append('"');		
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
}
