/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;

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
public class TabFolder extends AbstractContainer<TabSheet> {        
    public static final String PROPERTY_CURRENT_INDEX = "currentIndex";
    private static final int TABS_HEIGHT = 20;
    
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
        
        /*addPropertyChangeListener(new String[] {Border.PROPERTY_BORDER_COLOR, Border.PROPERTY_BORDER_SIZE, Border.PROPERTY_BORDER_TYPE,
                Font.PROPERTY_FONT_BOLD, Font.PROPERTY_FONT_COLOR, Font.PROPERTY_FONT_FAMILY, Font.PROPERTY_FONT_ITALIC, Font.PROPERTY_FONT_SIZE, Font.PROPERTY_FONT_UNDERLINE}, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                getStyle().setProperty(ev.getPropertyName(), ev.getNewValue());
            }
        });*/
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
