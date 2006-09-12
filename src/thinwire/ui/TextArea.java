/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.ui;

import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.Style;

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
    static {
        Style s = new Style(getDefaultStyle(Component.class)); // inherit
																// defaults from
																// Component
																// class
        s.getBackground().setColor(Color.WINDOW);
        Border b = s.getBorder();
        b.setSize(2);
        b.setType(Border.Type.INSET);
        b.setColor(Color.THREEDFACE);        
        setDefaultStyle(TextArea.class, s);
    }    
    
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
