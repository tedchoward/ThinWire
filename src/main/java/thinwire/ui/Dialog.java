/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package thinwire.ui;

/**
 * A Dialog is a window with a title that is usually associated to a Frame.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Dialog-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;Dialog Test&quot;);
 * dlg.setBounds(25, 25, 600, 400);
 * 
 * final TextField tBox = new TextField();
 * tBox.setBounds(25, 25, 150, 20);
 * dlg.getChildren().add(tBox);
 * 
 * final Button btn1 = new Button(&quot;Add Text&quot;);
 * btn1.setBounds(200, 20, 100, 30);
 * dlg.getChildren().add(btn1);
 * 
 * final Button btn2 = new Button(&quot;Numeric Mask&quot;);
 * btn2.setBounds(310, 20, 100, 30);
 * dlg.getChildren().add(btn2);
 * 
 * final Button btn3 = new Button(&quot;Right Align&quot;);
 * btn3.setBounds(420, 20, 100, 30);
 * dlg.getChildren().add(btn3);
 * 
 * ActionListener clickListener = new ActionListener() {
 *     public void actionPerformed(ActionEvent e) {
 *         Button btn = (Button) e.getSource();
 * 
 *         if (btn == btn1) {
 *             tBox.setText(tBox.getText() + &quot;913JQP-&quot;);
 *         } else if (btn == btn2) {
 *             tBox.setEditMask(&quot;#########&quot;);
 *         } else if (btn == btn3) {
 *             tBox.setAlignX(AlignX.RIGHT);
 *         }
 *     }
 * };
 * 
 * btn1.addActionListener(Button.ACTION_CLICK, clickListener);
 * btn2.addActionListener(Button.ACTION_CLICK, clickListener);
 * btn3.addActionListener(Button.ACTION_CLICK, clickListener);
 * 
 * Divider dv = new Divider();
 * dv.setBounds(25, 70, 550, 5);
 * dlg.getChildren().add(dv);
 * 
 * final TextArea ta = new TextArea();
 * ta.setBounds(25, 100, 350, 200);
 * dlg.getChildren().add(ta);
 * 
 * Button btn4 = new Button(&quot;Add Text&quot;);
 * btn4.setBounds(420, 100, 100, 30);
 * dlg.getChildren().add(btn4);
 * btn4.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent e) {
 *         ta.setText(ta.getText() + &quot;913JQP-&quot;);
 *     }
 * });
 * 
 * dlg.setVisible(true);
 * </pre>
 * 
 * </p>
 * <p>
 * @author Joshua J. Gertzen
 */
public class Dialog extends AbstractWindow<Dialog> {
    public static final String PROPERTY_RESIZE_ALLOWED = "resizeAllowed";
    public static final String PROPERTY_REPOSITION_ALLOWED = "repositionAllowed";
    public static final String PROPERTY_MODAL = "modal";
    public static final String PROPERTY_WAIT_FOR_WINDOW = "waitForWindow";
    
    private static final int TITLE_BAR_HEIGHT = 18 + 1;
        
    private boolean resizeAllowed;
    private boolean repositionAllowed = true;
    private boolean modal = true;
    
    /**
     * Constructs a new Dialog with no title. 
     */
    public Dialog() {
        this(null);
    }
    
    /**
     * Constructs a new Dialog with the specified title.
     * @param title the title to display.
     */
    public Dialog(String title) {        
        setTitle(title);
        setWidth(320);
        setHeight(240);        
    }

    /**
	 * Returns whether the Dialog can be resized by dragging the bottom-right
	 * corner.
	 * 
	 * @return true if the Dialog can be resized
	 */
    public boolean isResizeAllowed() {
        return resizeAllowed;
    }

    /**
	 * Set whether the Dialog can be resized by dragin the bottom-right corner.
	 * 
	 * @param resizeAllowed (Default = false)
	 */
    public void setResizeAllowed(boolean resizeAllowed) {
        boolean oldResizeAllowed = this.resizeAllowed;
        this.resizeAllowed = resizeAllowed;
        firePropertyChange(this, PROPERTY_RESIZE_ALLOWED, oldResizeAllowed, this.resizeAllowed);
    }

    /**
	 * Returns whether the Dialog can be drug around the frame
	 * 
	 * @return true is the Diagog is draggable
	 */
    public boolean isRepositionAllowed() {
        return repositionAllowed;
    }

    /**
	 * Set whether the Dialog can be drug around the screen.
	 * 
	 * @param repositionAllowed (Default = true)
	 */
    public void setRepositionAllowed(boolean repositionAllowed) {
        boolean oldRepositionAllowed = this.repositionAllowed;
        this.repositionAllowed = repositionAllowed;
        firePropertyChange(this, PROPERTY_REPOSITION_ALLOWED, oldRepositionAllowed, this.repositionAllowed);
    }
    
    /**
     * Returns whether the Dialog is modal.
     * @return true if the Dialog is modal
     */
    public boolean isModal() {
        return modal;
    }
    
    /**
	 * Set whether the Dialog is modal. If the Dialog is modal, when it is
	 * visible, all other components are prevented from receiving actions.
	 * 
	 * @param modal (Default = true)
	 */
    public void setModal(boolean modal) {
        if (this.isVisible()) throw new IllegalStateException("You cannot change the modal state of a visible Dialog");
        boolean oldModal = this.modal;
        this.modal = modal;
        firePropertyChange(this, PROPERTY_MODAL, oldModal, this.modal);
    }
    
    public int getInnerHeight() {
        int innerHeight = super.getInnerHeight() - TITLE_BAR_HEIGHT - (getMenu() == null ? 0 : MENU_BAR_HEIGHT);
        return innerHeight < 0 ? 0 : innerHeight;
    }
        
    /**
     * Makes the Dialog visible.
     * @see thinwire.ui.Component#setVisible(boolean)
     * @param visible (Default = false)
     */
    public void setVisible(boolean visible) {
        if (isVisible() != visible) {
            Application app = Application.current();
            Frame f = app.getFrame();
            f.dialogVisibilityChanged(this, visible);            
            
            if (visible) {
                app.showWindow(this);
                super.setVisible(visible);
            } else {
                app.hideWindow(this);
                super.setVisible(false);
            }
        }
    }
}
