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
package thinwire.ui;

import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.ui.layout.Layout;
import thinwire.ui.style.Font;
import thinwire.ui.style.Background;
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
    static final int TABS_HEIGHT = 20;
    private static final String[] BOUNDS_PROPERTIES = new String[] {Component.PROPERTY_X, Component.PROPERTY_Y, Component.PROPERTY_WIDTH, Component.PROPERTY_HEIGHT};
    
    private static final String[] STYLE_PROPERTIES = {Font.PROPERTY_FONT_BOLD, Font.PROPERTY_FONT_COLOR, Font.PROPERTY_FONT_FAMILY,
        Font.PROPERTY_FONT_ITALIC, Font.PROPERTY_FONT_SIZE, Font.PROPERTY_FONT_UNDERLINE, Font.PROPERTY_FONT_STRIKE,
        Background.PROPERTY_BACKGROUND_COLOR, Background.PROPERTY_BACKGROUND_IMAGE, Background.PROPERTY_BACKGROUND_POSITION,
        Background.PROPERTY_BACKGROUND_REPEAT
    };
    
	private int currentIndex = -1;
	
	public TabFolder() {
        addItemChangeListener(new ItemChangeListener() {
            public void itemChange(ItemChangeEvent ev) {
                Type type = ev.getType();
                TabSheet oSheet = (TabSheet)ev.getOldValue();
                TabSheet nSheet = (TabSheet)ev.getNewValue();                
                
                if (type == Type.REMOVE || type == Type.SET) {
                    oSheet.boundsChanged(0, 0, 0, 0);
                    if (getChildren().size() == 0) setCurrentIndex(-1);
                }
                
                if (type == Type.ADD || type == Type.SET) {
                    nSheet.boundsChanged(getX(), getY(), getWidth(), getHeight());
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
        
        addPropertyChangeListener(BOUNDS_PROPERTIES, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                int x = getX(), y = getY(), width = getWidth(), height = getHeight();
                
                for (TabSheet ts : getChildren()) {
                    ts.boundsChanged(x, y, width, height);
                }
            }
        });
        
        addPropertyChangeListener(STYLE_PROPERTIES, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                String propertyName = ev.getPropertyName();
                Object o = ev.getNewValue();

                for (TabSheet ts : getChildren()) {
                    Style s = ts.getStyle();
                    s.setProperty(propertyName, o);
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

    /**
     * This property is unsupported by the TabFolder component.
     * @throws UnsupportedOperationException indicating this property is not supported by TabFolder.
     */
    public ScrollType getScrollType() {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_SCROLL_TYPE, true));        
    }

    /**
     * This property is unsupported by the TabFolder component.
     * @throws UnsupportedOperationException indicating this property is not supported by TabFolder.
     */
    public void setScrollType(ScrollType scrollType) {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_SCROLL_TYPE, false));        
    }

    /**
     * This property is unsupported by the TabFolder component.
     * @throws UnsupportedOperationException indicating this property is not supported by TabFolder.
     */
    public Layout getLayout() {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_LAYOUT, true));        
    }

    /**
     * This property is unsupported by the TabFolder component.
     * @throws UnsupportedOperationException indicating this property is not supported by TabFolder.
     */
    public void setLayout(Layout layout) {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_LAYOUT, false));        
    }
}
