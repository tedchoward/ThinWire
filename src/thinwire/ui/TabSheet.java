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

import thinwire.ui.style.Style;


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
    private boolean allowSizeChange;
	private Image.Detail imageDetail = new Image.Detail();
	
    static {
        Style s = new Style(getDefaultStyle(Component.class)); //inherit defaults from Component class
        setDefaultStyle(TabSheet.class, s);
    }
    
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
    
    void sizeChanged(int width, int height) {
        try {
            allowSizeChange = true;
            super.setSize(width, height);
        } finally {
            allowSizeChange = false;
        }
    }
    
    @Override
    public void setWidth(int width) {
        if (allowSizeChange) {
            super.setWidth(width);
        } else if (!isCompatModeOn()) {
            throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_WIDTH, false));
        }
    }

    @Override
    public void setHeight(int height) {
        if (allowSizeChange) {
            super.setHeight(height);
        } else if (!isCompatModeOn()) {
            throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_HEIGHT, false));
        }
    }    
    
    @Override
    public int getX() {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_X, true));        
    }
    
    @Override
    public void setX(int x) {
        if (!isCompatModeOn()) throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_X, false));
    }

    @Override
    public int getY() {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_Y, true));        
    }

    @Override
    public void setY(int y) {
        if (!isCompatModeOn()) throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_Y, false));
    }   
    
    @Override
    public boolean isVisible() {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_VISIBLE, true));        
    }

    @Override
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_VISIBLE, false));
    }    
}
