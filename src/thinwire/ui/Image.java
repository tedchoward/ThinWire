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

import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import thinwire.render.Renderer;
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
public final class Image extends AbstractComponent implements ImageComponent, ActionEventComponent {
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>();    
	private ImageInfo imageDetail = new ImageInfo();
    
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
            if (imageDetail.getWidth() >= 0 && imageDetail.getHeight() >= 0) setSize(imageDetail.getWidth(), imageDetail.getHeight());
        }
        
        setFocusCapable(false);
	}
        
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
    }    
    
    /**
     * Add an actionListener which associates an action (ex: "click") with some method call.
     * @param action the action to specficially be notified of
     * @param listener the listener to add
     */
    public void addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
    }
    
    /**
     * Add an actionListener which associates an action (ex: "click") with some method call.
     * @param actions the actions to specficially be notified of
     * @param listener the listener to add
     */
    public void addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
    }    
    
    /**
     * Removes an existing actionListener.
     * @param listener the listener to remove
     */
    public void removeActionListener(ActionListener listener) {
        aei.removeListener(listener);
    }    
    
    /**
     * Programmatically signals an action which triggers the appropriate listener which calls
     * the desired method.
     * @param action the action name
     */
    public void fireAction(String action) {
        aei.fireAction(this, action);
    }    
	
	/**
	 * Returns the name of the image file.
	 * @return a String
	 */
	public String getImage() {
	    return imageDetail.getName();
	}
	
	/**
     * Associates an image file with this <code>Image</code> object.
     * @param image name of the image file (or key associated with the name of the image file)
     * @throws IllegalArgumentException if the image file does not exist.
     * @throws UnsupportedOperationException if the file does not have an acceptable format.
     * @throws RuntimeException if an I/O problem occurs.
     */
    public void setImage(String image) {
        String oldImage = this.imageDetail.getName();
        imageDetail.setName(image);        
        firePropertyChange(this, PROPERTY_IMAGE, oldImage, this.imageDetail.getName());
    }
	
	/**
	 * Returns a string representation of the image including file name and size information.
	 */
	public String toString() {
	    return Image.class.getName() + "{fileName: " + imageDetail.getName() + 
	    	", imageHeight: " + imageDetail.getHeight() + ", imageWidth: " + imageDetail.getWidth() + "}";
	}
}
