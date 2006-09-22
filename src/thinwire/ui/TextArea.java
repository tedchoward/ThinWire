/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * This is a multiline text field screen element.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/TextArea-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog();
 * dlg.setBounds(25, 25, 325, 225);
 * dlg.setTitle(&quot;TextArea Test&quot;);
 * 
 * TextArea tArea = new TextArea();
 * tArea.setBounds(25, 25, 275, 100);
 * 
 * Label lbl = new Label();
 * lbl.setBounds(25, 150, 275, 30);
 * lbl.setLabelFor(tArea);
 * 
 * tArea.addPropertyChangeListener(TextArea.PROPERTY_TEXT, new PropertyChangeListener() {
 *     public void propertyChange(PropertyChangeEvent ev) {
 *         TextArea source = (TextArea) ev.getSource();
 *         source.getLabel().setText(&quot;Text Length (updated when TextArea loses focus): &quot;
 *             + source.getText().length());
 *     }
 * });
 * 
 * tArea.setText(&quot;Sample text for the TextArea&quot;);
 * 
 * dlg.getChildren().add(tArea);
 * dlg.getChildren().add(lbl);
 * dlg.setVisible(true);
 * </pre>
 * 
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public final class TextArea extends AbstractEditorComponent {    
	/**
	 * Constructs a new TextArea with no text.
	 */
	public TextArea() {
	}
	
	/**
	 * Constructs a new TextArea with the specified text.
	 * @param text the text to display in the TextArea.
	 */
	public TextArea(String text) {
	    setText(text);
	}
}
