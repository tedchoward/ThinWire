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

/**
 * A <code>Label</code> is the text that appears next to a control on a
 * screen.
 * <p>
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Label-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;Label Test&quot;);
 * dlg.setBounds(25, 25, 415, 150);
 * 
 * final Label lbl = new Label(&quot;Initial 1st Label&quot;);
 * lbl.setBounds(25, 25, 150, 30);
 * 
 * Button btn = new Button(&quot;Toggle Text&quot;);
 * btn.setBounds(300, 20, 100, 30);
 * 
 * btn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent ev) {
 *         if (&quot;Initial 1st Label&quot;.equals(lbl.getText())) {
 *             lbl.setText(&quot;The text has now been toggled.&quot;);
 *         } else {
 *             lbl.setText(&quot;Initial 1st Label&quot;);
 *         }
 *     }
 * });
 * 
 * dlg.getChildren().add(lbl);
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
public class Label extends AbstractTextComponent implements AlignTextComponent, ActionEventComponent {
    public static final String PROPERTY_LABEL_FOR = "labelFor";
    public static final String PROPERTY_WRAP_TEXT = "wrapText";
    
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>(this, EventListenerImpl.ACTION_VALIDATOR);
    private AlignX alignX = AlignX.LEFT;
    private Component labelFor = null;
    private boolean wrapText;

    /**
     * Constructs a new Label with no text.
     */
    public Label() {
       this(null); 
    }
    
    /**
     * Constructs a new Label with the specified text.
     * @param text the text to display on the Label.
     */
    public Label(String text) {
        if (text != null) setText(text);
        setFocusCapable(false);
        setWrapText(false);
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
        aei.fireAction(ev);
    }
    
    /**
     * A convienence method that is equivalent to <code>fireAction(new ActionEvent(this, action))</code>.
     * @param action the action name
     */
    public void fireAction(String action) {
        aei.fireAction(new ActionEvent(this, action));
    }
    
    public AlignX getAlignX() {
        return alignX;
    }

    public void setAlignX(AlignX alignX) {
        if (alignX == null) throw new IllegalArgumentException(PROPERTY_ALIGN_X + " == null");
        AlignX oldAlignX = this.alignX;
        this.alignX = alignX;
        firePropertyChange(this, PROPERTY_ALIGN_X, oldAlignX, alignX);
    }    
        
    /**
     * Returns the component that this label is associated with.
     * 
     * @return the Component associated with this Label.
     */
    public Component getLabelFor() {
        return labelFor;
    }

    /**
     * This method links a label with an onscreen component.
     * 
     * @param labelFor the component to link with the label
     */
    public void setLabelFor(Component labelFor) {
        Component oldLabelFor = this.labelFor;
        this.labelFor = labelFor;
        if (labelFor != null) ((AbstractComponent)labelFor).setLabel(this);
        firePropertyChange(this, PROPERTY_LABEL_FOR, oldLabelFor, labelFor);
    }
    
    public boolean isWrapText() {
        return wrapText;
    }
    
    public void setWrapText(boolean wrapText) {
        boolean oldWrap = this.wrapText;
        this.wrapText = wrapText;
        if (this.wrapText != oldWrap) firePropertyChange(this, PROPERTY_WRAP_TEXT, oldWrap, this.wrapText);
    }
}