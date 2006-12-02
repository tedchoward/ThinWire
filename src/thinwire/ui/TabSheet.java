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

import java.util.List;

import thinwire.util.ImageInfo;

/**
 * A TabSheet is a Panel that can be layered, so that a user can switch between
 * tab sheets.
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
public class TabSheet extends AbstractContainer<Component> implements TextComponent, ImageComponent {
	private String text = "";
    private boolean allowBoundsChange;
	private ImageInfo imageInfo = new ImageInfo(null);
    
	/**
	 * Construct a new TabSheet with no text.
	 */
	public TabSheet() {
		this(null, null);
	}
	
	/**
	 * Creates a new TabSheet with the specified text.
	 * @param text the text to dispaly on the tab part of the TabSheet.
	 */
	public TabSheet(String text) {
	    this(text, null);
	}
	
	public TabSheet(String text, String image) {        
        setText(text);
		setImage(image);
	}
    
	public String getImage() {
	    return imageInfo.getName();
	}

    public void setImage(String image) {
        String oldImage = this.imageInfo.getName();
        imageInfo = new ImageInfo(image);        
        firePropertyChange(this, PROPERTY_IMAGE, oldImage, this.imageInfo.getName());
    }	

    public ImageInfo getImageInfo() {
        return imageInfo;
    }
    
	/**
	 * Returns the text displayed on the tab part of the TabSheet.
	 * @return the text displayed on the tab part of the TabSheet.
	 */
	public String getText() {
		return text;
	}	
	
	/**
	 * Sets the text that is displayed on the tab part of the TabSheet.
	 * @param text the text to be shown.
	 */
	public void setText(String text) {
		String oldText = this.text;
		this.text = text == null ? "" : text;
		firePropertyChange(this, PROPERTY_TEXT, oldText, this.text);
	}
    
    void boundsChanged(int x, int y, int width, int height) {
        try {
            allowBoundsChange = true;
            super.setBounds(x, y, width, height);
        } finally {
            allowBoundsChange = false;
        }
    }
    
    public int getInnerHeight() {
        int innerHeight = super.getInnerHeight() - TabFolder.TABS_HEIGHT;
        return innerHeight < 0 ? 0 : innerHeight;
    }    

    public void setWidth(int width) {
        if (allowBoundsChange) {
            super.setWidth(width);
        } else {
            TabFolder tf = (TabFolder)getParent();
            
            if (tf == null) {
                //#IFDEF V1_1_COMPAT        
                if (!isCompatModeOn())
                //#ENDIF
                throw new IllegalStateException("You must first add a TabSheet to a TabFolder before you can set the 'width' property");
            } else {
                tf.setWidth(width);
            }
        }
    }
    
    public void setHeight(int height) {
        if (allowBoundsChange) {
            super.setHeight(height);
        } else {
            TabFolder tf = (TabFolder)getParent();
            
            if (tf == null) {
                //#IFDEF V1_1_COMPAT        
                if (!isCompatModeOn())
                //#ENDIF
                throw new IllegalStateException("You must first add a TabSheet to a TabFolder before you can set the 'height' property");
            } else {
                tf.setHeight(height);
            }
        }
    }
    
    public void setX(int x) {
        if (allowBoundsChange) {
            super.setX(x);
        } else {
            TabFolder tf = (TabFolder)getParent();
            
            if (tf == null) {
                //#IFDEF V1_1_COMPAT        
                if (!isCompatModeOn())
                //#ENDIF
                throw new IllegalStateException("You must first add a TabSheet to a TabFolder before you can set the 'x' property");
            } else {
                tf.setX(x);
            }
        }
    }

    public void setY(int y) {
        if (allowBoundsChange) {
            super.setY(y);
        } else {
            TabFolder tf = (TabFolder)getParent();
            
            if (tf == null) {
                //#IFDEF V1_1_COMPAT        
                if (!isCompatModeOn())
                //#ENDIF
                throw new IllegalStateException("You must first add a TabSheet to a TabFolder before you can set the 'y' property");
            } else {
                tf.setY(y);
            }
        }
    }   
    
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        TabFolder tf = (TabFolder)getParent();
        
        if (tf != null) {
            List<TabSheet> l = tf.getChildren();
            int index = l.indexOf(this);
            
            if (index == tf.getCurrentIndex()) {            
                if (index + 1 < l.size()) {
                    index++;
                } else if (index > 0) {
                    index--;
                } else {
                    index = -1;
                }
                
                tf.setCurrentIndex(index);
            }
        }
    }

    /**
     * This property is unsupported by the TabSheet component.
     * @throws UnsupportedOperationException indicating this property is not supported by TabSheet.
     */
    public Object getLimit() {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_LIMIT, true));        
    }

    /**
     * This property is unsupported by the TabSheet component.
     * @throws UnsupportedOperationException indicating this property is not supported by TabSheet.
     */
    public Component setLimit(Object limit) {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_LIMIT, false));        
    }
}
