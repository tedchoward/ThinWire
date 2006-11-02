/*
                         ThinWire(TM) RIA Ajax Framework
               Copyright (C) 2003-2006 Custom Credit Systems
  
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.
  
  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  Place, Suite 330, Boston, MA 02111-1307 USA
 
  Users wishing to use this library in proprietary products which are not 
  themselves to be released under the GNU Public License should contact Custom
  Credit Systems for a license to do so.
  
                Custom Credit Systems, Richardson, TX 75081, USA.
                           http://www.thinwire.com
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.render.Renderer;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.util.ImageInfo;

/**
 * A component that displays an image. Only PNG, JPEG or GIF are supported.
 * <p>
 * The format for the image name can be one of the following:
 * <ol>
 * <li>A file that is pathed relative to the context.</li>
 * <li>A file that is fully pathed.</li>
 * <li>A file that is loaded as a class resource. Syntax:
 * class:///[fully-qualified-classname]/[folder-under-class-pacakge]/[image-name]
 * example: class:///thinwire.ui.layout.SplitLayout/resources/Image.png</li>
 * </ol>
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Image-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;Image Test&quot;);
 * dlg.setBounds(25, 25, 300, 140);
 * 
 * for (int i = 0; i &lt; 10; i++) {
 *  Image img = new Image(&quot;resources/ngLF/star.png&quot;);
 * 	img.setBounds(25 + (i * 21), 25, 20, 20);
 * 	dlg.getChildren().add(img);
 * }
 * for (int i = 0; i &lt; 10; i++) {
 * 	Image img = new Image(&quot;resources/ngLF/bolt.png&quot;);
 * 	img.setBounds(25 + (i * 21), 46, 20, 20);
 * 	dlg.getChildren().add(img);
 * }
 * Button btn = new Button(&quot;Ok&quot;, &quot;resources/ngLF/ok.png&quot;);
 * btn.setBounds(110, 70, 80, 30);
 * dlg.getChildren().add(btn);
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
public class Image extends AbstractComponent implements ImageComponent, ActionEventComponent {
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>(this, EventListenerImpl.ACTION_VALIDATOR);    
	private ImageInfo imageInfo = new ImageInfo(null);
    
	/**
	 * Constructs a new Image with no image fileName.
	 */
	public Image() {
	    this(null);
	}
	
	/**
	 * Constructs a new Image with the specified fileName as its image.
	 * @param fileName a file name that specifies an image to display.
	 */
	public Image(String fileName) {
	    if (fileName != null) {
            setImage(fileName);
            if (imageInfo.getWidth() >= 0 && imageInfo.getHeight() >= 0) setSize(imageInfo.getWidth(), imageInfo.getHeight());
        }
        
        setFocusCapable(false);
	}
        
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
    }    
    
    public void addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
    }
    
    public void addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
    }    
    
    public void removeActionListener(ActionListener listener) {
        aei.removeListener(listener);
    }    

    public void fireAction(ActionEvent ev) {
        aei.fireAction(ev, null);
    }

    /**
     * A convienence method that is equivalent to <code>fireAction(new ActionEvent(this, action))</code>.
     * @param action the action name
     */
    public void fireAction(String action) {
        aei.fireAction(new ActionEvent(this, action), null);
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
}
