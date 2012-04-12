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

import java.util.List;

import thinwire.util.ImageInfo;

/**
 * @author Joshua J. Gertzen
 */
public interface HierarchyComponent<HI extends HierarchyComponent.Item> extends ItemChangeEventComponent {
    public interface Item<H extends HierarchyComponent, I extends Item> {
        public static final String PROPERTY_ITEM_IMAGE = "itemImage";
        public static final String PROPERTY_ITEM_TEXT = "itemText";
        public static final String PROPERTY_ITEM_USER_OBJECT = "itemUserObject";
        
        /**
         * Get the text that is displayed for the item in the menu.
         * @return the text that is displayed for the item, or an empty string if none.
         */
        public String getText();

        /**
         * Set the text that is displayed for the item in the menu.
         * @param text the text that should be displayed for the item.
         */
        public void setText(String text);

        /**
         * Get the image file name that is displayed on the left side of the item.
         * @return the image that is displayed for the item, or an empty string if none.
         */
        public String getImage();

        /**
         * Set the image file name that is displayed on the left side of the item.
         * @param image the name of the image file that should be displayed for the item.
         * @throws IllegalArgumentException if the image file does not exist.
         * @throws RuntimeException if the system cannot determine the image file's canonical path.
         */
        public void setImage(String image);

        /**
         * Returns an immutable <code>ImageInfo</code> class that provides information
         * about the assigned image, such as width, height, format, etc.
         * @return an immutable <code>ImageInfo</code> describing this item's image.
         */
        public ImageInfo getImageInfo();
        
        /**
         * Get the user defined value for this item.
         * @return the user defined value for this item if one is defined, null otherwise.
         */
        public Object getUserObject();

        /**
         * Set a user defined value for this item.
         * @param value an object that should be associated to this item.
         */
        public void setUserObject(Object userObject);        
        
        /**
         * A convenience method that returns the index of this item in it's parents children List. Equivalent to
         * <code>item.getParent().getChildren().indexOf(item);</code>.
         * @return the index of this item in it's parents children List.
         * @throws IllegalStateException if this item has not yet been added to another Menu.Item or if this item is the root item
         *         for the menu.
         */
        public int getIndex();        
        
        public H getHierarchy();        

        /**
         * Get the parent object of this item.
         * @return if this item is the root item, then this returns the Menu, otherwise this returns the parentMenu.Item or null if
         *         this item has not yet been added to another Menu.Item.
         */
        public Object getParent();

        public boolean hasChildren();

        /**
         * Get the <code>List</code> that contains the children of this item.
         * @return the <code>List</code> that contains the children of this item or an empty <code>List</code> if there are no
         *         children.
         */
        public List<I> getChildren();
    }
    
    /**
     * Get the root item of the Menu.
     * @return the root item of the Menu.
     */
    public HI getRootItem();
}
