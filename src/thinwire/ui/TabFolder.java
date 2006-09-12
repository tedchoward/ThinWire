/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.ui;

import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.Style;

/**
 * A container for Tab Sheets. A Tab folder could sit in a dialog and have
 * multiple tabs.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/TabFolder-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;TabFolder Test&quot;);
 * dlg.setBounds(25, 25, 600, 400);
 * 
 * TabSheet tSheet1 = new TabSheet(&quot;Sheet 1&quot;);
 * TabSheet tSheet2 = new TabSheet(&quot;Sheet 2&quot;);
 * 
 * TabFolder tFolder = new TabFolder();
 * tFolder.setBounds(50, 25, 500, 300);
 * tFolder.getChildren().add(tSheet1);
 * tFolder.getChildren().add(tSheet2);
 * 
 * TextField tf = new TextField();
 * tf.setBounds(25, 25, 150, 20);
 * tSheet2.getChildren().add(tf);
 * 
 * Button firstButton = new Button(&quot;Change Tab Title 1&quot;);
 * firstButton.setBounds(50, 50, 150, 30);
 * firstButton.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent ev) {
 *         ((TabSheet) ((Button) ev.getSource()).getParent()).setText(&quot;New Title 1&quot;);
 *     }
 * });
 * tSheet1.getChildren().add(firstButton);
 * dlg.getChildren().add(tFolder);
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
public class TabFolder extends AbstractContainer {        
    public static final String PROPERTY_CURRENT_INDEX = "currentIndex";
    private static final int TABS_HEIGHT = 20;
    
    //TODO: It's not necessary, but conceivably you could have a style just for the tab itself.  In fact, for a more
    //      web like interface it might be nice to set certain things about a tab for each tab, such as the background color.
    //      Or possibly setting the background color for the active tab would be sufficient.
    static {
        Style s = new Style(getDefaultStyle(Component.class)); //inherit defaults from Component class
        s.getBackground().setColor(Color.THREEDFACE);
        
        Border b = s.getBorder();
        b.setSize(2);
        b.setType(Border.Type.OUTSET);
        b.setColor(Color.THREEDFACE);
        
        setDefaultStyle(TabFolder.class, s);
    }
    
    
	private int currentIndex;
	
	public TabFolder() {
		super(true);
        
        addItemChangeListener(new ItemChangeListener() {
            public void itemChange(ItemChangeEvent ev) {
                Type type = ev.getType();
                TabSheet oSheet = (TabSheet)ev.getOldValue();
                TabSheet nSheet = (TabSheet)ev.getNewValue();                
                if (type == Type.REMOVE || type == Type.SET) oSheet.sizeChanged(0, 0);                
                if (type == Type.ADD || type == Type.SET) nSheet.sizeChanged(getInnerWidth(), getInnerHeight());
            }
        });        
	}
    
	/**
	 * Sets the current tab sheet.
	 * @param index (Default = 0)
	 */
	public void setCurrentIndex(int index) {
		int oldIndex = this.currentIndex;
		this.currentIndex = index;
	 	firePropertyChange(this, PROPERTY_CURRENT_INDEX, oldIndex, this.currentIndex);
	}
	
	public int getCurrentIndex() {
		return currentIndex;
	}
    
    public int getInnerHeight() {
        int innerHeight = getHeight() - CALC_BORDER_PADDING_SUB - TABS_HEIGHT;
        return innerHeight < 0 ? 0 : innerHeight;
    }
    
    public int getInnerWidth() {
        int innerWidth = getWidth() - CALC_BORDER_PADDING_SUB;
        return innerWidth < 0 ? 0 : innerWidth;
    }
        
    private void updateTabSheetSize() {        
        int innerWidth = getInnerWidth();
        int innerHeight = getInnerHeight();
        
        for (Component ts : getChildren()) {
            ((TabSheet)ts).sizeChanged(innerWidth, innerHeight);
        }
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        updateTabSheetSize();
    }
    
    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        updateTabSheetSize();
    }
        
    @Override
    public void setScroll(ScrollType scrollType) {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_SCROLL, false));        
    }

    @Override
    public ScrollType getScroll() {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_SCROLL, true));        
    }
}
