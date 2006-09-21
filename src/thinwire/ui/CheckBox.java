/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.Style;

/**
 * A CheckBox is a screen element that can either be checked or cleared, and operates independently of other elements.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/CheckBox-1.png"> <br>
 * 
 * <pre>
 * final CheckBox cb = new CheckBox(&quot;I am checked!&quot;);
 * cb.setChecked(true);
 * cb.setBounds(20, 20, 150, 30);
 * cb.addPropertyChangeListener(CheckBox.PROPERTY_CHECKED, new PropertyChangeListener() {
 *     public void propertyChange(PropertyChangeEvent pce) {
 *         if (pce.getNewValue() == Boolean.TRUE) {
 *             cb.setText(&quot;I am checked&quot;);
 *         } else {
 *             cb.setText(&quot;I am unchecked&quot;);
 *         }
 *     }
 * });
 * 
 * Dialog d = new Dialog(&quot;CheckBox Test&quot;);
 * d.setBounds(20, 20, 200, 100);
 * d.getChildren().add(cb);
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
 * <td>Fires PropertyChangeEvent( propertyName = CheckBox.PROPERTY_CHECKED )</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * </table>
 * </p>
 * @author Joshua J. Gertzen
 */
public class CheckBox extends AbstractTextComponent implements CheckedComponent {
    public static final String PROPERTY_CHECKED = "checked";
    
    static {
        Style s = new Style(getDefaultStyle(Component.class)); //inherit defaults from Component class
        s.getBackground().setColor(Color.WINDOW);
               
        //TODO: You can't set the border values for a CheckBox because the Button is an image.  But at least this will reflect the way the image is displayed.
        Border b = s.getBorder();
        b.setSize(2);
        b.setType(Border.Type.INSET);
        b.setColor(Color.BUTTONFACE);
        
        setDefaultStyle(CheckBox.class, s);
    }    
    
    private boolean checked;
	
	/**
	 * Constructs a new CheckBox with no text.
	 */
	public CheckBox() {
        this(null);
	}
	
	/**
	 * Constructs a new CheckBox with the specified text.
	 * @param text the text to display on the right side of the CheckBox.
	 */
	public CheckBox(String text) {
	    this(text, false);
	}
    
    /**
     * Constructs a new CheckBox with the specified text and initial checked state.
     * @param text the text to display on the right side of the CheckBox.
     * @param checked the initial checked state
     */
    public CheckBox(String text, boolean checked) {
        setText(text);
        setChecked(checked);
    }

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
	    boolean oldChecked = this.checked;
		this.checked = checked;
		firePropertyChange(this, PROPERTY_CHECKED, oldChecked, checked);		
	}	
}
