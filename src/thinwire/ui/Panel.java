/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

/**
 * <code>Panel</code> is a direct implementation of <code>Container</code>.
 * <p>
 * Example: <br>
 * <img src="doc-files/Panel-1.png">
 * <br>
 * <pre>
 *    TextField tf = new TextField("[Enter Value Here!]");
 *    tf.setBounds(5, 5, 150, 25);
 *       
 *    Panel p = new Panel();
 *    p.setBounds(10, 10, 200, 100);
 *    p.getChildren().add(tf);
 *    
 *    Frame f = Application.current().getFrame();
 *    f.getChildren().add(p);
 * </pre>
 * </p>
 * @author Joshua J. Gertzen
 */
public class Panel extends AbstractContainer<Component> {
    public Panel() {
        
    }
}
