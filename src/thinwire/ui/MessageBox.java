/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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

import java.util.ArrayList;
import java.util.List;

import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;

/**
 * A <code>MessageBox</code> displays a message (or a component) and allows a
 * user to respond.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/MessageBox-1.png"> <br>
 * 
 * <pre>
 * MessageBox.confirm(&quot;resources/ngLF/info.png&quot;, &quot;ThinWire&quot;,
 *         &quot;Get ready for ThinWire&quot;);
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
 * </table>
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public final class MessageBox {
	private static final int TEXT_LINE_HEIGHT = 16;
	private static final int TEXT_CHAR_WIDTH = 6;
	private static final int ICON_SIZE = 32;
	private static final int SPACING = 5;
	private static final int MIN_BUTTON_HEIGHT = 25;
	private static final int MIN_BUTTON_WIDTH = 40;
	private static final int DEFAULT_COMPONENT_HEIGHT = 40;
	private static final int DEFAULT_COMPONENT_WIDTH = 150;
    private static final Application.Local<List<MessageBox>> messageBoxStack = new Application.Local<List<MessageBox>>() {
        protected synchronized List<MessageBox> initialValue() {            
            return new ArrayList<MessageBox>();
        }
    };   
    
	private Dialog dialog;
	private String title = "";
	private String icon = "";
	private String text = "";
	private Component component;
	private String buttons;	
	private int buttonId;
    private boolean hasButtons;
	
    /**
     * Display a dialog window without buttons.<p>
     * 
     * Adds the MessageBox to the MessageBox stack on the application
     * instance.<br>
     * 
     * This method will not block.<br>
     * 
     * For multiple line messages, separate the lines with "\n".
     * 
     * @param message The dialog message, a \n separated sequence of lines.
     */
    public static void show(String message) {
        MessageBox box = new MessageBox();
        box.setText(message);
        box.show();
    }

    /**
     * Display a dialog window without buttons.<p>
     * 
     * Adds the MessageBox to the MessageBox stack on the application
     * instance.<br>
     * 
     * This method will not block.<br>
     * 
     * For multiple line messages, separate the lines with "\n".
     * 
     * @param title the dialog title
     * @param message The dialog message, a \n separated sequence of lines.
     */
    public static void show(String title, String message) {
        MessageBox box = new MessageBox();
        box.setTitle(title);
        box.setText(message);
        box.show();
    }

    /**
	 * Display a dialog window without buttons.<p>
     * 
	 * Adds the MessageBox to the MessageBox stack on the application
	 * instance.<br>
     * 
     * This method will not block.<br>
	 * 
	 * For multiple line messages, separate the lines with "\n".
	 * 
	 * @param icon name of the image file without path info or file extension.
	 * @param title the dialog title
	 * @param message The dialog message, a \n separated sequence of lines.
	 */
	public static void show(String icon, String title, String message) {
		MessageBox box = new MessageBox();
		box.setTitle(title);
		box.setIcon(icon);
		box.setText(message);
		box.show();
	}

    /**
     * Display a dialog window with an OK button and message.<p>
     * 
     * Sample usage:
     * <pre>
     * MessageBox.confirm("Invalid Return by Time\nInvalid Zip Code");
     * </pre>
     * 
     * For multiple line messages, separate the lines with "\n".
     * 
     * @param message  The dialog message, a \n separated sequence of lines.
     */
    public static void confirm(String message) {
        confirm(null, null, message, null);
    }
	
    /**
     * Display a dialog window with an OK button, title and message.<p>
     * 
     * Sample usage:
     * <pre>
     * MessageBox.confirm("Operator Message", 
     *   "Invalid Return by Time\nInvalid Zip Code");
     * </pre>
     * 
     * For multiple line messages, separate the lines with "\n".
     * 
     * @param title  the dialog title
     * @param message  The dialog message, a \n separated sequence of lines.
     */
    public static void confirm(String title, String message) {
        confirm(null, title, message, null);
    }
    
	/**
	 * Display a dialog window with an OK button, icon, title and message.<p>
	 * 
	 * Sample usage:
	 * <pre>
	 * MessageBox.confirm("error", "Operator Message", 
	 *   "Invalid Return by Time\nInvalid Zip Code");
	 * </pre>
	 * 
	 * For multiple line messages, separate the lines with "\n".
	 * 
	 * @param icon name of the image file without path info or file extension.
	 * @param title  the dialog title
	 * @param message  The dialog message, a \n separated sequence of lines.
	 */
	public static void confirm(String icon, String title, String message) {
		confirm(icon, title, message, null);
	}

	/**
	 * Display a dialog window with specified buttons, message and icons.<p>
	 * 
	 * Sample usage:
	 * <pre>
	 * MessageBox.confirm(&quot;question&quot;, &quot;Operator Message&quot;,
	 * 		&quot;Would you like to shut down the server?&quot;, 
	 *      &quot;text1;image1|text2,image2&quot;);
	 * </pre>
	 * 
	 * For multiple line messages, separate the lines with "\n".<p>
	 * 
	 * The buttons string should specify text and optionally an image for each button.
	 * Use a semi-colon to separate the text and image for a button, and a 
	 * pipe to separate one button's info from that of the next.<br>
	 * e.g.  "text1;image1|text2;image2|......|textk;imagek"<p>
	 * 
	 * The buttons string can also specify int return values for the buttons.<br>
	 * e.g.  "text1;image1;5|text2;image2;17|......|textK;imageK;27"<p>
	 * 
	 * Warning:  Give all of the buttons return values or none of them.  If you don't specify
	 * return values, a button's return value will depend on the order in which it is added
	 * to the dialog.  Also, return values must be integers greater than or equal to 0.
	 * 
	 * @param icon The dialog's icon.
	 * @param title  The dialog's title.
	 * @param message  The dialog's message.
	 * @param buttons specifies what text and icon each button will have
	 * @return the clicked button's return value
	 *            
	 */
	public static int confirm(String icon, String title, String message, String buttons) {
		return confirm(icon, title, message, null, buttons);
	}	
	
	/**
	 * Display a dialog window with the specified icon, title,
	 *   component, and buttons.
	 * @param icon the name of the icon, optional
	 * @param title the MessageBox title
	 * @param component the Component for the content panel.
	 * @param buttons a string specifying the buttons
	 * @return the clicked button's return value
	 */
	public static int confirm(String icon, String title, Component component, String buttons) {
		return confirm(icon, title, null, component, buttons);
	}
	
	/**
	 * Display a dialog window with the specified icon, title,
	 *   message, component, and buttons.<p>
   * Note:  Don't specify both a message and a component.
	 * @param icon the name of the icon, optional
	 * @param title the MessageBox title
	 * @param message the message for this MessageBox
	 * @param component the Component for the content panel.
	 * @param buttons a string specifying the buttons
	 * @return the clicked button's return value
	 */
	private static int confirm(String icon, String title, String message, Component component, String buttons) {
		MessageBox box = new MessageBox();
		box.setTitle(title);
		box.setIcon(icon);
		box.setText(message);
		box.setComponent(component);
		box.setButtons(buttons);
		return box.confirm();
	}

	/**
	 * Closes the current message box.
	 */
	public static void closeCurrent() {
		List<MessageBox> l = getMessageBoxes();		
		if (l.size() > 0) l.get(l.size() - 1).close();
	}
	
	/**
	 * Returns a list of messageboxes.
	 * @return the current Application's list of MessageBoxes.
	 */
	private static List<MessageBox> getMessageBoxes() {
		return messageBoxStack.get();
	}	
	
	/**
	 * Returns the title of the MessageBox.
	 * @return the title of this MessageBox
	 */
	public String getTitle() {
	    return title;
	}
	
	/**
	 * Set the title for the MessageBox.
	 * @param title the MessageBox title
	 */
	public void setTitle(String title){
		title = title == null ? "" : title;
		this.title = title;
	}
	
	/**
	 * Returns the message in the message box.
	 * @return the message in this MessageBox
	 */
	public String getText() {
	    return text;
	}
	
	/**
	 * Set the message for the MessageBox.<p>
	 * For multiple line messages, separate the lines with "\n".<br>
	 * Don't set both a message and a Component.
	 *
	 * @param text the message
	 */
	public void setText(String text){
		text = text == null ? "" : text;
		this.text = text;
	}
	
	/**
	 * Returns the component for the MessageBox.
	 * @return a Component
	 */
	public Component getComponent() {
	    return component;
	}
	
	/**
	 * Set the component for the MessageBox.<p>
	 * The component will appear in the content panel.<br>
	 * Don't set both a message and a Component.
	 * @param component the content for the MessageBox.
	 */
	public void setComponent(Component component) {
		this.component = component;
	}	

	/**
	 * Returns the icon for the message box.
	 * @return the name for the icon for the MessageBox
	 */
	public String getIcon() {
	    return icon;
	}
	
	/**
	 * Set the icon for the MessageBox.  It appears in the
	 * top left corner.
	 * 
	 * @param icon  name of the icon
	 */
	public void setIcon(String icon) {
		icon = icon == null ? "" : icon;
		this.icon = icon;
	}
	
	/**
	 * Returns the string that sets all the buttons for the message box.
	 * @return the string describing this MessageBox's buttons
	 */
	public String getButtons() {
	    return buttons;
	}
	
	/**
   * Sets the buttons based on a string.
   * </p>
   * Example:
   * <pre>
   * currentMB.setButtons(&quot;Delete;ERASE|Activate;STAR|Cancel;CANCEL&quot;);
   * </pre>
   * @param buttons a String describing the buttons
   */
	public void setButtons(String buttons) {
		buttons = buttons == null ? "" : buttons;
		this.buttons = buttons;
	}
		
	/**
	 * Makes the MessageBox visible. <p>
   * 
   * Adds the MessageBox to the MessageBox stack on the application
   * instance.<p>
   * 
   * This method is non-blocking.
	 */
	public void show() {
        if (dialog != null) throw new IllegalStateException("messageBox is already open");
	    dialog = getDialog();
		getMessageBoxes().add(this);
	    dialog.setWaitForWindow(false);
		dialog.setVisible(true);
	}
		
	/**
	 * Make the MessageBox visible, and then block.
	 * @return the clicked button's return value
	 */
	public int confirm() {
        if (dialog != null) throw new IllegalStateException("messageBox is already open");
		if (buttons == null || buttons.length() == 0) buttons = "OK";
        hasButtons = true;
	    dialog = getDialog();
        dialog.setWaitForWindow(true);
		dialog.setVisible(true);
		return buttonId;
	}
	
	/**
	 * Close the message box.
	 *
	 */
	public void close() {
	    if (this.dialog == null) throw new IllegalStateException("messageBox is not open");
	    getMessageBoxes().remove(this);
        if (component != null) dialog.getChildren().remove(component);
	    dialog.setVisible(false);
	    dialog = null;
	}
    	
	private Dialog getDialog() {	    
		Dialog dialog = new Dialog();
		dialog.setTitle(title);
		List<Component> items = dialog.getChildren();
		int x = SPACING;
		int y = SPACING;
		
		if (icon.length() > 0) {
		    Image image = new Image();
		    image.setImage(icon);
		    image.setX(SPACING);
		    image.setY(SPACING);
		    image.setWidth(ICON_SIZE);
		    image.setHeight(ICON_SIZE);
		    items.add(image);
		    x += ICON_SIZE + SPACING;
		}		
				
		Component content = getContent();
        Component buttons = hasButtons ? getButtonPanel() : null;	    
        content.setY(y);
	    y += content.getHeight() + SPACING;
		
        if (hasButtons) {
            buttons.setY(y);
    		y += buttons.getHeight() + SPACING;
        }
        
		int maxWidth = dialog.getTitle().length() * TEXT_CHAR_WIDTH + 40; //20 is X button		
		if (content.getWidth() > maxWidth) maxWidth = content.getWidth();
		if (hasButtons && buttons.getWidth() > maxWidth) maxWidth = buttons.getWidth();
		
		if (content.getWidth() == maxWidth)
		    content.setX(x);
		else {
		    int diff = maxWidth - content.getWidth();
		    content.setX(x + (diff > 0 ? diff / 2 : 0));
		}

        if (hasButtons) {
    		if (buttons.getWidth() == maxWidth)
    		    buttons.setX(x);
    		else {
    		    int diff = maxWidth - buttons.getWidth();
    		    buttons.setX(x + (diff > 0 ? diff / 2 : 0));
    		}
        }

		x += maxWidth + SPACING;
		items.add(content);
		if (hasButtons) items.add(buttons);
		dialog.setWidth(x);
        if (icon.length() > 0 && y < ICON_SIZE + SPACING * 2) y = ICON_SIZE + SPACING * 2; 
		dialog.setHeight(y + 22);
        Frame f = Application.current().getFrame();
        
        int width = f.getInnerWidth();
        int height = f.getInnerHeight();
        if (width < 0) width = 560;
        if (height < 0) height = 400;        
        x = (width / 2) - (dialog.getWidth() / 2);
        y = (height / 2) - (dialog.getHeight() / 2);
        if (x < 0) x = 0;
        if (y < 0) y = 0;
		dialog.setX(x);
		dialog.setY(y);
		return dialog;
	}	
		
	private Component getButtonPanel() {	    
		Panel panel = new Panel();

		if (buttons != null) {
			class ButtonDef {
			    String text = "";
			    String imageFileName;
			    int id;
			    int width;
			}
			
			int currentX = 0;
		    
		    String[] buttonLines = buttons.split("\\|");
		    ButtonDef[] buttonDefs = new ButtonDef[buttonLines.length];
		    int maxHeight = 0;
		    
		    for (int i = 0; i < buttonLines.length; i++) {
		        String[] def = buttonLines[i].split(";");
		        ButtonDef bd = new ButtonDef();		        
	            buttonDefs[i] = bd;
	            
		        if (def.length > 0 && def[0].length() > 0) bd.text = def[0];		        		        
		        if (def.length > 1 && def[1].length() > 0) bd.imageFileName = def[1];
	            bd.id = def.length > 2 && def[2].length() > 0 ? Integer.valueOf(def[2]).intValue() : i;
	            
	            int width = bd.text.length() * TEXT_CHAR_WIDTH + 30;
	            if (width < MIN_BUTTON_WIDTH) width = MIN_BUTTON_WIDTH;
	            int height = 0;
	            
	            if (bd.imageFileName != null) {
	                height = 16 + 11;
		            width += 16;
	            }	           

	            if (height < MIN_BUTTON_HEIGHT) height = MIN_BUTTON_HEIGHT;
	            if (maxHeight < height) maxHeight = height;
	            bd.width = width;
		    }
		    		    
		    for (int i = 0; i < buttonDefs.length; i++) {		    
		        Button button = new Button();
		        button.setText(buttonDefs[i].text);
	            if (buttonDefs[i].imageFileName != null && buttonDefs[i].imageFileName.length() > 0) button.setImage(buttonDefs[i].imageFileName);
	            
                if (i == 0) {
                    button.setStandard(true);
                    if (component == null) button.setFocus(true);
                }
                
	            button.setWidth(buttonDefs[i].width);
	            button.setHeight(maxHeight);
	            button.setX(currentX);
	            button.setY(0);
	            final int id = buttonDefs[i].id;
	            
	            button.addActionListener("click", new ActionListener() {
	                public void actionPerformed(ActionEvent ev) {
	                    if (ev.getAction().equals(Button.ACTION_CLICK)) {
		                    MessageBox.this.buttonId = id;
                            if (component != null) dialog.getChildren().remove(component);
                            dialog.setVisible(false);
	                    }
	                }	                
	            });

	            panel.getChildren().add(button);
	            currentX += button.getWidth() + SPACING;
		    }
		    
		    panel.setWidth(currentX - SPACING);
		    panel.setHeight(maxHeight);
		}
		
		return panel;
	}
	
	private Component getContent() {
	    Component comp;
	    
		if (text != null && !"".equals(text)) {
		    String[] lines = text.split("\\r?\\n");
		    
		    int width = 0;
		    
		    for (int i = 0; i < lines.length; i++) {
		        int length = lines[i].length();
		        
		        if (length > width)
		            width = lines[i].length();
		    }

		    width *= TEXT_CHAR_WIDTH;
		    int y = 0;
		    Panel content = new Panel();
		    List<Component> items = content.getChildren();

		    for (int i = 0; i < lines.length; i++) {
		        Label label = new Label();
		        label.setText(lines[i]);
		        label.setAlignX(Label.AlignX.CENTER);
		        label.setHeight(TEXT_LINE_HEIGHT);
		        label.setWidth(width);
		        label.setX(0);
		        label.setY(y);
		        items.add(label);
		        y += label.getHeight();
		    }
		    
		    content.setWidth(width);
		    content.setHeight(y + SPACING);
		    comp = content;
		} else if (component != null) {
		    comp = component;			
		    if (comp.getWidth() == 0) comp.setWidth(DEFAULT_COMPONENT_WIDTH);
		    if (comp.getHeight() == 0) comp.setHeight(DEFAULT_COMPONENT_HEIGHT);
            comp.setFocus(true);
		} else
		    comp = new Panel();
		
	    return comp;
	}
}