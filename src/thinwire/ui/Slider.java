/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * A Slider is a screen element that has a cursor that can be set to any position between zero and a specified length.
 * Sliders are either horizontal or vertical depending on their dimensions.  If the width is greater than the height,
 * then the Slider is horizontal, otherwise it is vertical.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Slider-1.png"> <br>
 * 
 * <pre>
 * Frame f = Application.current().getFrame();
 * f.setTitle(&quot;Slider Test&quot;);
 * Dialog d = new Dialog(&quot;Slider Test&quot;);
 * d.setBounds(10, 10, 250, 150);
 * final Slider s = new Slider(5, 3);
 * s.setBounds(10, 10, 100, 20);
 * s.addPropertyChangeListener(Slider.PROPERTY_CURRENT_INDEX, new PropertyChangeListener() {
 *     public void propertyChange(PropertyChangeEvent ev) {
 *         tf.setText(String.valueOf((Integer) ev.getNewValue()));
 *     }
 * });
 * d.getChildren().add(s);
 * final TextField tf = new TextField();
 * tf.setBounds(10, 40, 50, 20);
 * tf.setText(String.valueOf(s.getCurrentIndex()));
 * d.getChildren().add(tf);
 * Button b = new Button(&quot;SetValue&quot;);
 * b.setBounds(70, 35, 60, 30);
 * b.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent ev) {
 *         s.setCurrentIndex(Integer.parseInt(tf.getText()));
 *     }
 * });
 * d.getChildren().add(b);
 * f.getChildren().add(d);
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
 * <td>Right Arrow</td>
 * <td>Increments the cursorIndex by 1 and Fires PropertyChangeEvent( propertyName = Slider.PROPERTY_CURSOR_INDEX )</td>
 * <td>Only if the component is horizontal.</td>
 * </tr>
 * <tr>
 * <td>Left Arrow</td>
 * <td>Decrements the cursorIndex by 1 and Fires PropertyChangeEvent( propertyName = Slider.PROPERTY_CURSOR_INDEX )</td>
 * <td>Only if the component is horizontal.</td>
 * </tr>
 * <tr>
 * <td>Up Arrow</td>
 * <td>Increments the cursorIndex by 1 and Fires PropertyChangeEvent( propertyName = Slider.PROPERTY_CURSOR_INDEX )</td>
 * <td>Only if the component is vertical.</td>
 * </tr>
 * <tr>
 * <td>Down Arrow</td>
 * <td>Decrements the cursorIndex by 1 and Fires PropertyChangeEvent( propertyName = Slider.PROPERTY_CURSOR_INDEX )</td>
 * <td>Only if the component is vertical.</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author Ted C. Howard
 */
public class Slider extends AbstractRangeComponent {     
    /**
     * Constructs a new <code>Slider</code> with a length of 100 and initial currentIndex of 0.
     *
     */
    public Slider() {
        this(100);
    }
    
    /**
     * Constructs a new <code>Slider</code> with the specified length and an initial currentIndex of 0.
     * @param length the number of increments on the Slider
     */
    public Slider(int length) {
        this(length, 0);
    }
    
    /**
     * Constructs a new <code>Slider</code> with the specifed length and initial currentIndex.
     * @param length the number of increments on the Slider
     * @param currentIndex the initial position of the cursor
     */
    public Slider(int length, int currentIndex) {
        setLength(length);
        setCurrentIndex(currentIndex);
    }

}
