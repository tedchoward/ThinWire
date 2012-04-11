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
public class Label extends AbstractLabelComponent<Label> {
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
}