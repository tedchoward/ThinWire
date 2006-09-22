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
import thinwire.ui.style.Background;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.FX;
import thinwire.ui.style.Font;
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
public class TabFolder extends AbstractContainer<TabSheet> {        
    public static final String PROPERTY_CURRENT_INDEX = "currentIndex";
    private static final int TABS_HEIGHT = 20;
    
	private int currentIndex = -1;
	
	public TabFolder() {
		super(true);
        
        addItemChangeListener(new ItemChangeListener() {
            public void itemChange(ItemChangeEvent ev) {
                Type type = ev.getType();
                TabSheet oSheet = (TabSheet)ev.getOldValue();
                TabSheet nSheet = (TabSheet)ev.getNewValue();                
                
                if (type == Type.REMOVE || type == Type.SET) {
                    oSheet.sizeChanged(0, 0);
                    if (getChildren().size() == 0) setCurrentIndex(-1);
                }
                
                if (type == Type.ADD || type == Type.SET) {
                    nSheet.sizeChanged(getInnerWidth(), getInnerHeight());
                    Style ss = nSheet.getStyle();
                    Style s = getStyle();
                    ss.getBorder().copy(s.getBorder(), false);
                    ss.getBackground().copy(s.getBackground(), true);
                    ss.getFont().copy(s.getFont(), true);
                    ss.getFX().copy(s.getFX(), true);
                    if (getChildren().size() == 1) setCurrentIndex(0); 
                }
            }
        });
        
        addPropertyChangeListener(DropDown.STYLE_PROPERTIES, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                String propertyName = ev.getPropertyName();
                Object o = ev.getNewValue();

                for (TabSheet ts : getChildren()) {
                    Style s = ts.getStyle();
                    DropDown.setStyleValue(s, propertyName, o);
                }
            }
        });
	}
    
	/**
	 * Sets the current tab sheet.
	 * @param currentIndex (Default = 0)
	 */
	public void setCurrentIndex(int currentIndex) {
        if (currentIndex < -1 || currentIndex >= getChildren().size()) throw new IllegalArgumentException("currentIndex < 0 || currentIndex >= getChildren().size()");
		int oldCurrentIndex = this.currentIndex;
		this.currentIndex = currentIndex;
	 	firePropertyChange(this, PROPERTY_CURRENT_INDEX, oldCurrentIndex, this.currentIndex);
	}
	
	public int getCurrentIndex() {
		return currentIndex;
	}
    
    public int getInnerHeight() {
        int innerHeight = super.getInnerHeight() - TABS_HEIGHT;
        return innerHeight < 0 ? 0 : innerHeight;
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
