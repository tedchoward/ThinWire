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
 * A ProgressBar is a screen element that has a visible selection that can be set to any size between zero and a specified length.
 * ProgressBars are either horizontal or vertical depending on their dimensions.  If the width is greater than the height,
 * then the ProgressBar is horizontal, otherwise it is vertical.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/ProgressBar-1.png"> <br>
 * 
 * <pre>
 * Frame f = Application.current().getFrame();
 * f.setTitle(&quot;ProgressBar Test&quot;);
 * Dialog d = new Dialog(&quot;ProgressBar Test&quot;);
 * d.setBounds(10, 10, 250, 150);
 * final ProgressBar pb = new ProgressBar(5, 3);
 * pb.setBounds(10, 10, 100, 20);
 * d.getChildren().add(pb);
 * final TextField tf = new TextField();
 * tf.setBounds(10, 40, 50, 20);
 * tf.setText(String.valueOf(pb.getCurrentIndex()));
 * d.getChildren().add(tf);
 * Button b = new Button(&quot;SetValue&quot;);
 * b.setBounds(70, 35, 60, 30);
 * b.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent ev) {
 *         pb.setCurrentIndex(Integer.parseInt(tf.getText()));
 *     }
 * });
 * d.getChildren().add(b);
 * f.getChildren().add(d);
 * </pre>
 * 
 * </p>
 * 
 * @author Ted C. Howard
 *
 */
public class ProgressBar extends AbstractRangeComponent<Slider> {
    /**
     * Constructs a new <code>ProgressBar</code> with a length of 100 and a currentIndex of 0.
     *
     */
    public ProgressBar() {
        this(100, 0);
    }
    
    /**
     * Constructs a new <code>ProgressBar</code> with the specified length and a currentIndex of 0.
     * @param length
     */
    public ProgressBar(int length) {
        this(length, 0);
    }
    
    /**
     * Constructs a new <code>ProgressBar</code> with the specified length and currentIndex.
     * @param length
     * @param currentIndex
     */
    public ProgressBar(int length, int currentIndex) {
        setLength(length);
        setCurrentIndex(currentIndex);
    }
}
