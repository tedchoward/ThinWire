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
