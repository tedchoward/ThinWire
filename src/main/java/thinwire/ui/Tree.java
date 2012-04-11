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
package thinwire.ui;

import java.util.List;

import thinwire.ui.event.ActionEvent;

/**
 * A component that displays a set of hierarchical data as an outline.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Tree-1.png"> <br>
 * 
 * <pre>
 * Dialog treeFrame = new Dialog(&quot;Tree Test&quot;);
 * treeFrame.setBounds(25, 25, 400, 350);
 * 
 * final Label label = new Label(&quot;???????&quot;);
 * label.setBounds(225, 25, 150, 20);
 * 
 * Tree tree = new Tree();
 * tree.setBounds(10, 10, 200, 300);
 * Tree.Item root = tree.getRootItem();
 * root.setText(&quot;System Root&quot;);
 * root.setImage(SCORE_IMAGE);
 * tree.setRootItemVisible(false);
 * root.setExpanded(false);
 * 
 * Tree.Item readme = new Tree.Item(&quot;readme&quot;, FILE_IMAGE);
 * root.getChildren().add(readme);
 * Tree.Item bill = new Tree.Item(&quot;bill&quot;, FOLDER_IMAGE);
 * root.getChildren().add(bill);
 * Tree.Item startup = new Tree.Item(&quot;startup&quot;, FILE_IMAGE);
 * bill.getChildren().add(startup);
 * Tree.Item billFiles = new Tree.Item(&quot;files&quot;, FOLDER_IMAGE);
 * bill.getChildren().add(billFiles);
 * Tree.Item billFilesAddress = new Tree.Item(&quot;Address List&quot;, FILE_IMAGE);
 * billFiles.getChildren().add(billFilesAddress);
 * Tree.Item billFilesPhone = new Tree.Item(&quot;Phone List&quot;, FILE_IMAGE);
 * billFiles.getChildren().add(billFilesPhone);
 * bill.setExpanded(true);
 * billFiles.setExpanded(true);
 * 
 * Tree.Item billMusic = new Tree.Item(&quot;music&quot;, FOLDER_IMAGE);
 * bill.getChildren().add(billMusic);
 * Tree.Item billMusicSong1 = new Tree.Item(&quot;song1.mp3&quot;, FILE_IMAGE);
 * billMusic.getChildren().add(billMusicSong1);
 * 
 * Tree.Item billPic = new Tree.Item(&quot;pictures&quot;, FOLDER_IMAGE);
 * bill.getChildren().add(billPic);
 * Tree.Item billPicHome = new Tree.Item(&quot;home&quot;, FOLDER_IMAGE);
 * billPic.getChildren().add(billPicHome);
 * Tree.Item billPicS = new Tree.Item(&quot;s.jpg&quot;, PICTURE_IMAGE);
 * billPicHome.getChildren().add(billPicS);
 * Tree.Item billPicP = new Tree.Item(&quot;p.jpg&quot;, PICTURE_IMAGE);
 * billPicHome.getChildren().add(billPicP);
 * Tree.Item billPicM = new Tree.Item(&quot;m.jpg&quot;, PICTURE_IMAGE);
 * billPicHome.getChildren().add(billPicM);
 * 
 * Tree.Item jim = new Tree.Item(&quot;jim&quot;, FOLDER_IMAGE);
 * root.getChildren().add(jim);
 * Tree.Item jimStartup = new Tree.Item(&quot;startup&quot;, FILE_IMAGE);
 * jim.getChildren().add(jimStartup);
 * Tree.Item copyright = new Tree.Item(&quot;copyright&quot;, FILE_IMAGE);
 * root.getChildren().add(copyright);
 * 
 * tree.addPropertyChangeListener(Tree.Item.PROPERTY_SELECTED, new PropertyChangeListener() {
 *     public void propertyChange(PropertyChangeEvent ev) {
 *         label.setText(((Tree.Item) ev.getSource()).getText());
 *     }
 * });
 * 
 * treeFrame.getChildren().add(tree);
 * treeFrame.getChildren().add(label);
 * treeFrame.setVisible(true);
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
 * <tr>
 * <td>Space</td>
 * <td>Fires PropertyChangeEvent( propertyName = Tree.Item.PROPERTY_CHECKED )</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public class Tree extends AbstractHierarchyComponent<Tree, Tree.Item> {    
    /**
     * An object that represents an item in a <code>Tree</code> component.
     */
    public final static class Item extends AbstractHierarchyComponent.Item<Tree, Tree.Item> {
        public static final String PROPERTY_ITEM_SELECTED = "itemSelected";
        public static final String PROPERTY_ITEM_EXPANDED = "itemExpanded";
        
        private boolean expanded;
        private boolean selected;
        

        /**
         * Constructs a new Item with no text and no image.
         */
        public Item() {
            this(null, null);
        }

        /**
         * Constructs a new Item with the specified text and no image.
         * @param text the text to display for the item.
         */
        public Item(String text) {
            this(text, null);
        }

        /**
         * Constructs a new Item with the specified text and image.
         * @param text the text to display for the item.
         * @param image the image to display to the left of the text of the item.
         */
        public Item(String text, String image) {
            if (text != null) setText(text);
            if (image != null) setImage(image);
        }

        /**
         * Get the selected state of the item.
         * @return true if this item is the selected item in the tree, false otherwise.
         */
        public boolean isSelected() {
        	return selected;
        }

        /**
         * Set the selected state of the item.
         * @param selected true if you want to select the item, false if you want to unselect it and select the root item.
         * @throws IllegalStateException if the item has not been added to a tree.
         */
        public void setSelected(boolean selected) {
        	this.selected = selected;
            Tree tree = getHierarchy();
            
            if (tree != null) {
	            if (selected && tree.selectedItem != this) {
	                if (tree.selectedItem != null) {
	                	tree.firePropertyChange(tree.selectedItem, PROPERTY_ITEM_SELECTED, true, false);
	                	tree.selectedItem.selected = false;
	                }
	                
	                tree.priorSelectedItem = tree.selectedItem;
	                tree.priorSelectedItem.selected = false;
	                tree.selectedItem = this;
	                tree.firePropertyChange(this, PROPERTY_ITEM_SELECTED, false, true);
	            } else if (!selected && tree.selectedItem == this) {
	                tree.firePropertyChange(this, PROPERTY_ITEM_SELECTED, true, false);
	                tree.priorSelectedItem = this;
	                tree.selectedItem = tree.getRootItem();
	                tree.firePropertyChange(tree.selectedItem, PROPERTY_ITEM_SELECTED, false, true);
	            }
            }
        }

        /**
         * Get the expanded state of the item.
         * @return true if this branch item in the tree is expanded and its children are shown, false otherwise.
         */
        public boolean isExpanded() {
            return expanded;
        }

        /**
         * Set the expanded state of the item.
         * @param expanded true if you want to expand this branch item and show its children, false if you want to collapse it and
         *        hide the children.
         */
        public void setExpanded(boolean expanded) {
            boolean oldExpanded = this.expanded;
            this.expanded = expanded;
            Tree tree = getHierarchy();
            if (tree != null) tree.firePropertyChange(this, PROPERTY_ITEM_EXPANDED, oldExpanded, expanded);
        }
        
        public String toString() {
            return "Tree.Item{" + super.toString() + ",selected:" + this.isSelected() + ",expanded:" + this.isExpanded() + "}";
        }
    }

    public static final String PROPERTY_ROOT_ITEM_VISIBLE = "rootItemVisible";
    
    private boolean rootItemVisible;
    private Item selectedItem;
    private Item priorSelectedItem;

    /**
     * Constructs a new Tree.
     */
    public Tree() {
        super(new Item(), EventListenerImpl.ACTION_VALIDATOR);
        selectedItem = getRootItem();
        selectedItem.setExpanded(true);
    }

    void removingItem(Tree.Item item) {
        if (priorSelectedItem == item) priorSelectedItem = null;
        
        if (selectedItem == item || childSelected(item)) {
            if (item.getParent() instanceof Tree.Item) {
                Tree.Item parent = (Tree.Item)item.getParent();
                List<Tree.Item> kids = parent.getChildren(); 
                int index = kids.indexOf(item);
                
                if ((index - 1) >= 0) {
                    kids.get(index - 1).setSelected(true);
                } else if ((index + 1) < parent.getChildren().size()) {
                    kids.get(index + 1).setSelected(true);
                } else {
                    parent.setSelected(true);
                }
            }
        }
    }
    
    private boolean childSelected(Tree.Item item) {
    	if (!item.hasChildren()) return false;
    	for (Tree.Item i : item.getChildren()) if (selectedItem.equals(i) || childSelected(i)) return true;
    	return false;
    }
    
    /**
     * Get the rootItemVisible state of the Tree.
     * @return true if the root item is visible, false otherwise.
     */
    public boolean isRootItemVisible() {
        return rootItemVisible;
    }

    /**
     * Set the rootItemVisible state of the Tree.
     * @param rootItemVisible true if you want the root item of the tree to be visible, false otherwise.
     */
    public void setRootItemVisible(boolean rootItemVisible) {
        boolean oldRootItemVisible = this.rootItemVisible;        
        this.rootItemVisible = rootItemVisible;
        Tree.Item root = getRootItem();
        
        if (rootItemVisible && (!root.hasChildren() || root.getChildren().size() == 0)) {
        	root.setSelected(true);
        } else if (!rootItemVisible && (root.hasChildren() && root.getChildren().size() > 0)) {
        	root.getChildren().get(0).setSelected(true);
        }
        
        firePropertyChange(this, PROPERTY_ROOT_ITEM_VISIBLE, oldRootItemVisible, rootItemVisible);
    }
    
    /**
     * Get the selected item of Tree.
     * @return the selected item of Tree.
     */
    public Item getSelectedItem() {
        return selectedItem;
    }

    /**
     * Gets the prior selected item of Tree.
     * @return the prior selected item of Tree.
     */
    public Item getPriorSelectedItem() {
        if (priorSelectedItem.getParent() == null || priorSelectedItem.getHierarchy() != this) priorSelectedItem = null;
        return priorSelectedItem;
    }

    public void fireAction(ActionEvent ev) {
        if (ev == null) throw new IllegalArgumentException("ev == null");
        if (!(ev.getSource() instanceof Item)) throw new IllegalArgumentException("!(ev.getSource() instanceof Tree.Item)");
        ((Item)ev.getSource()).setSelected(true);       
        super.fireAction(ev);
    }
}
