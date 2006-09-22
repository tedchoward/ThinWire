/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.ui.event.KeyPressEvent;

/**
 * A component that displays a set of hierarchical data as a menu.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Menu-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;Menu Test&quot;);
 * dlg.setBounds(25, 25, 200, 200);
 * 
 * Menu mainMenu = new Menu();
 * Menu.Item fileItem = new Menu.Item(&quot;File&quot;);
 * Menu.Item newItem = new Menu.Item(&quot;New&quot;);
 * newItem.setUserObject(&quot;You clicked on the File-&gt;New menu item&quot;);
 * Menu.Item openItem = new Menu.Item(&quot;Open&quot;);
 * openItem.setUserObject(&quot;You clicked on the File-&gt;Open menu item&quot;);
 * 
 * mainMenu.addActionListener(Menu.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent ev) {
 *         Menu.Item selectedItem = (Menu.Item) ev.getSource();
 *         MessageBox.confirm(&quot;resources/ngLF/system.png&quot;, &quot;Menu Test&quot;, (String) selectedItem.getUserObject());
 *     }
 * });
 * 
 * dlg.setMenu(mainMenu);
 * mainMenu.getRootItem().getChildren().add(fileItem);
 * fileItem.getChildren().add(newItem);
 * fileItem.getChildren().add(openItem);
 * dlg.setVisible(true);
 * </pre>
 * 
 * </p>
 * <p>
 * <b>Keyboard Navigation:</b><br>
 * <table border="1">
 * <tr>
 * <td>KEY</td>
 * <td>RESPONSE</td>
 * <td>NOTE</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public class Menu extends AbstractHierarchyComponent<Menu.Item> {    
    /**
     * An object that represents an item in a <code>Menu</code> component.
     */
    public static class Item extends AbstractHierarchyComponent.Item<Menu, Menu.Item> {
        public static final String PROPERTY_ITEM_ENABLED = "itemEnabled";
        public static final String PROPERTY_ITEM_KEY_PRESS_COMBO = "itemKeyPressCombo";

        private String keyPressCombo = "";
        private boolean enabled = true;

        /**
         * Constructs a new Item with no text and no image.
         */
        public Item() {
            this(null, null, null);
        }        
        
        /**
         * Constructs a new Item with the specified text and no image.
         * @param text the text to display for the item.
         */     
        public Item(String text) {
            this(text, null, null);
        }
        
        /**
         * Constructs a new Item with the specified text and image.
         * @param text the text to display for the item.
         * @param image the image to display to the left of the text of the item.
         */
        public Item(String text, String image) {
            this(text, image, null);
        }
        
        /**
         * Constructs a new Item with the specified text and image.
         * @param text the text to display for the item.
         * @param image the image to display to the left of the text of the item.
         * @param keyPressCombo the key combination to associate to this Menu.Item.
         */
        public Item(String text, String image, String keyPressCombo) {
            if (text != null) setText(text);
            if (image != null) setImage(image);            
            if (keyPressCombo != null) setKeyPressCombo(keyPressCombo);
        }

        /**
         * Returns whether the item is selectable (enabled).
         * @return true if the field is enabled (Default = true)
         */
        public final boolean isEnabled() {
            return this.enabled;
        }

        /**
         * Sets the enabled property of the item
         * @param enabled True to make editable, false to disable the component (Default = true)
         */
        public final void setEnabled(boolean enabled) {
            boolean oldEnabled = this.enabled;
            this.enabled = enabled;
            Menu menu = getHierarchy();
            if (menu != null) menu.firePropertyChange(this, PROPERTY_ITEM_ENABLED, oldEnabled, enabled);
        }
        
        /**
         * Get a text representation of the key combo that will activate this item.
         * @return the text representation of the key combo that will activate this item,
         *  or an empty string if no key combo is set for this item.
         */
        public String getKeyPressCombo() {
            return keyPressCombo;
        }
        
        /**
         * Set the key combo / shortcut that will activate this item.
         * Once a key press combo is set, pressing the key combination
         * within the window that this menu is attached to will cause
         * the fireAction method to be called with the action set to "click". 
         * @param keyPressCombo key combo to assign to this item.
         * @see thinwire.ui.event.KeyPressEvent#encodeKeyPressCombo(boolean, boolean, boolean, String)
         */
        public void setKeyPressCombo(String keyPressCombo) {
            String oldKeyPressCombo = this.keyPressCombo;            
            keyPressCombo = keyPressCombo == null || keyPressCombo.length() == 0 ? "" : KeyPressEvent.normalizeKeyPressCombo(keyPressCombo);             
            this.keyPressCombo = keyPressCombo;
            Menu menu = getHierarchy();
            if (menu != null) menu.firePropertyChange(this, PROPERTY_ITEM_KEY_PRESS_COMBO, oldKeyPressCombo, keyPressCombo);                
        }
    }

    /**
     * Constructs a new Menu.
     */
    public Menu() {
        super(new Item());
    }

    public int getHeight() {
        Container cont = getContainer();

        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            return AbstractWindow.MENU_BAR_HEIGHT; 
        } else {
            return super.getHeight();
        }
    }    
    
    public void setHeight(int height) {
        Container cont = getContainer();
        
        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_HEIGHT, false)); 
        } else {
            super.setHeight(height);
        }        
    }
    
    public int getWidth() {
        Container cont = getContainer();
        
        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            return cont.getInnerWidth(); 
        } else {
            return super.getWidth();
        }
    }
    
    public void setWidth(int width) {
        Container cont = getContainer();
        
        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_WIDTH, false)); 
        } else {
            super.setWidth(width);
        }
    }
    
    public int getX() {
        Container cont = getContainer();
        
        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            return 0; 
        } else {
            return super.getX();
        }
    }    
    
    public void setX(int x) {
        Container cont = getContainer();
        
        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_X, false)); 
        } else {
            super.setX(x);
        }
    }
    
    public int getY() {
        Container cont = getContainer();
        
        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            return 0; 
        } else {
            return super.getY();
        }
    }    

    public void setY(int y) {
        Container cont = getContainer();
        
        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_Y, false)); 
        } else {
            super.setY(y);
        }
    }    

    public boolean isVisible() {
        Container cont = getContainer();
        
        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            return true; 
        } else {
            return super.isVisible();
        }
    }        
    
    public void setVisible(boolean visible) {
        Container cont = getContainer();
        
        if (cont instanceof Window && ((Window)cont).getMenu() == this) {
            throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_VISIBLE, false)); 
        } else {
            super.setVisible(visible);
        }
    }    
}