/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

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
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

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
public class TextArea extends AbstractEditorComponent {    
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
