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
import thinwire.ui.event.ActionListener;
import thinwire.util.ImageInfo;

/**
 * A Button is a component that typically causes an action when activated.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Button-1.png"> <br>
 * 
 * <pre>
 * Button b = new Button(&quot;Click Me!&quot;);
 * b.setBounds(20, 20, 150, 30);
 * b.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent e) {
 *         ((Button) e.getSource()).setText(&quot;You Clicked Me!&quot;);
 *     }
 * });
 * 
 * Dialog d = new Dialog(&quot;Button Test&quot;);
 * d.setBounds(20, 20, 200, 100);
 * d.getChildren().add(b);
 * d.setVisible(true);
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
 * <td>Fires ActionEvent( action = Button.ACTION_CLICK )</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * <tr>
 * <td>Enter</td>
 * <td>Fires ActionEvent( action = Button.ACTION_CLICK )</td>
 * <td> Only if the 'standard' property is set to 'true' and ANY component in the Window has focus. The only exception is if you are
 * focused on a component that has it's own behavior for for the Enter key, in which case Ctrl-Enter may be used. </td>
 * </tr>
 * <tr>
 * <td>Ctrl-Enter</td>
 * <td>Fires ActionEvent( action=Button.ACTION_CLICK )</td>
 * <td>Only if the 'standard' property is set to 'true' and ANY component in the Window has focus.</td>
 * </tr>
 * </table>
 * </p>
 * @author Joshua J. Gertzen
 */
public class Button extends AbstractTextComponent implements ImageComponent, ActionEventComponent {
    public static final String PROPERTY_STANDARD = "standard";

    private boolean standard;
	private ImageInfo imageDetail = new ImageInfo();
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>();
	
	/**
	 * Constructs a new Button with no text or image.
	 */
	public Button() {        
	    this(null, null);
	}
	
	/**
	 * Constructs a new Button with the specified text and no image.
	 * @param text the text to display on the Button.
	 */
	public Button(String text) {
	    this(text, null);
	}
	
	/**
	 * Constructs a new Button with the specified text and image.
	 * @param text the text to display on the Button.
	 * @param image the name of a image file resource to display on the Button.
	 */
	public Button(String text, String image) {
	    setText(text);
	    setImage(image);
	}	
	    
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
    }
        
	/**
	 * @return the filename of the image on the button.
	 */
	public String getImage() {
	    return imageDetail.getName();
	}
	
	/**
	 * Places an image on a button.
	 * @param image The file name or resource name of the image.
	 * @throws IllegalArgumentException thrown if the image file does not exist
	 * @throws RuntimeException thrown if an I/O error occurs
	 */
	public void setImage(String image) {
        String oldImage = this.imageDetail.getName();
        imageDetail.setName(image);        
        firePropertyChange(this, PROPERTY_IMAGE, oldImage, this.imageDetail.getName());
    }	

	/**
	 * Returns whether the button is the standard button (i.e. default).
	 * @return true if this button is the standard button
	 */
	public final boolean isStandard() {
	    return standard;
	}
	
	/**
	 * Sets whether this button is the standard button.
	 * 
	 * If this button is a
	 * There can only be a single standard button per <code>Window</code>,
	 * therefore setting the standard flag on this button will is the button in a window that will be 
	 * @param standard true to make this the standard button, false otherwise.
	 * @throws UnsupportedOperationException if this Button's parent is not a Container.
	 */
	public final void setStandard(boolean standard) {
        boolean oldStandard = this.standard;
	    this.standard = standard;	        
	    Object o = getParent();
	    
	    if (o instanceof AbstractContainer) {	        
	        if (standard && !oldStandard) {
	        	((AbstractContainer)o).updateStandardButton(this, true);
	        } else if (!standard && oldStandard) {
	            ((AbstractContainer)o).updateStandardButton(this, false);
	        }	        
	    }
	    
	    firePropertyChange(this, PROPERTY_STANDARD, oldStandard, standard);
	}
    //#IFDEF V1_1_COMPAT
    
	/**
	 * Add an actionListener that receives "click" notification from this object.
     * Equivalent to addActionListener("click", listener);
	 * @param listener the listener to add
	 * @deprecated for performance reasons, this form as been deprecated.  Use the named action form instead.
	 */
	public void addActionListener(ActionListener listener) {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use addActionListener(action, listener) instead.");        
        aei.addListener(ACTION_CLICK, listener);
	}
    //#ENDIF

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
}
