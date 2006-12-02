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

/**
 * This is a text field screen element.
 * <p>
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/TextField-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;TextField Test&quot;);
 * dlg.setBounds(25, 25, 450, 100);
 * 
 * final TextField tf = new TextField();
 * tf.setBounds(25, 25, 150, 20);
 * 
 * Button btn = new Button(&quot;Numeric Mask ###.##&quot;);
 * btn.setBounds(200, 20, 150, 30);
 * 
 * btn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent ev) {
 *         tf.setEditMask(&quot;###.##&quot;);
 *         tf.setFocus(true);
 *     }
 * });
 * 
 * dlg.getChildren().add(tf);
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
 * <UL>
 * <LI>There's no need to set both a maxLength property and an editMask.
 * <LI>If they conflict - e.g. the maxLength value is different from the length
 * of the editMask - the minimum of the 2 will be used in validation.
 * <LI>Setting maxLength to a value k is equivalent to setting an edit mask of
 * k occurrences of 'x' - the anything goes mask character.
 * </UL>
 * 
 * @author Joshua J. Gertzen
 */
public class TextField extends AbstractMaskEditorComponent {
    public static final String PROPERTY_INPUT_HIDDEN = "inputHidden";
       
	private boolean inputHidden = false;	
    
	/**
	 * Constructs a new TextField with no text.
	 */
	public TextField() {
	    
	}
	
	/**
	 * Constructs a new TextField with the specified text.
	 * @param text the text to display in the the TextField.
	 */
	public TextField(String text) {
	    setText(text);
	}
           
    public void setText(String text) {
        formattedText = text = text == null ? "" : text;
        super.setText(getUnformattedText(text));        
    }    
    
	/**
	 * Returns whether the input is hidden (as in a password field).
	 * @return true if the input should be hidden
	 */
	public boolean isInputHidden() {
		return inputHidden;
	}

	/**
	 * Hides the text typed into the field with asterisks (as in a password field).
	 * @param inputHidden (Default = false)
	 */
	public void setInputHidden(boolean inputHidden) {
	    boolean oldInputHidden = this.inputHidden;
		this.inputHidden = inputHidden;
		firePropertyChange(this, PROPERTY_INPUT_HIDDEN, oldInputHidden, inputHidden);
	}
}
