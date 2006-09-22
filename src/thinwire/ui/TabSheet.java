/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import java.util.List;

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
	private Image.Detail imageDetail = new Image.Detail();
    
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

	/**
	 * @return the filename of the image on the tabsheet
	 */
	public String getImage() {
	    return imageDetail.image;
	}

	/**
     * Places an image on a tabsheet
     * @param image The file name or resource name of the image.
     * @throws IllegalArgumentException thrown if the image file does not exist
     * @throws RuntimeException thrown if an I/O error occurs
     */
    public void setImage(String image) {
        String oldImage = this.imageDetail.image;
        Image.updateImageDetail(imageDetail, image);        
        firePropertyChange(this, PROPERTY_IMAGE, oldImage, this.imageDetail.image);
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
}
