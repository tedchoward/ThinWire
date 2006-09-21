/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.Style;

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
public final class TextField extends AbstractMaskEditorComponent {
    public static final String PROPERTY_INPUT_HIDDEN = "inputHidden";

    static {
        Style s = new Style(getDefaultStyle(Component.class)); //inherit defaults from Component class
        s.getBackground().setColor(Color.WINDOW);
        Border b = s.getBorder();
        b.setSize(2);
        b.setType(Border.Type.INSET);
        b.setColor(Color.THREEDFACE);        
        setDefaultStyle(TextField.class, s);
    }    
       
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
