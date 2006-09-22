/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.render.Renderer;
import thinwire.render.web.WebApplication;
import thinwire.ui.event.ActionListener;

/**
 * A <code>Hyperlink</code> is a screen component that acts like a standard
 * hyperlink.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Hyperlink-1.png"> <br>
 * 
 * <pre>
 * Hyperlink hl = new Hyperlink(&quot;Custom Credit Systems Home Page&quot;,
 *         &quot;http://www.customcreditsystems.com&quot;);
 * hl.setBounds(25, 25, 175, 20);
 * MessageBox.confirm(&quot;&quot;, &quot;Hyperlink Test&quot;, hl, &quot;OK&quot;);
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
 * <td>Enter</td>
 * <td>Fires Action( Action = Hyperlink.ACTION_CLICK )</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public final class Hyperlink extends AbstractTextComponent implements ActionEventComponent {    
    private static ThreadLocal<Integer> targetId = new ThreadLocal<Integer>() {
        protected synchronized Integer initialValue() {
            return 0;
        }
    };
    
    public static final String PROPERTY_LOCATION = "location";

    public static void openLocation(String location) {
        openLocation(location, null);
    }
    
    public static void openLocation(String location, String target) {
        if (target == null || target.length() < 1) {
            Integer id = targetId.get();
            targetId.set(id + 1);
            target = "olhl" + id;
        }
        
        ((WebApplication)WebApplication.current()).clientSideMethodCall("tw_Hyperlink", "openLocation", location, target);
    }
    
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>();    
    private String location = "";
        
    public Hyperlink() {}
    
    public Hyperlink(String text) {
        this(text, null);
    }

    public Hyperlink(String text, String location) {
        this.setText(text);
        this.setLocation(location);
    }
    
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
    }
    
    /**
     * Add an actionListener which associates an action (ex: "click") with some method call.
     * @param action the action to specficially be notified of
     * @param listener the listener to add
     */
    public void addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
    }
    
    /**
     * Add an actionListener which associates an action (ex: "click") with some method call.
     * @param actions the actions to specficially be notified of
     * @param listener the listener to add
     */
    public void addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
    }    
    
    /**
     * Removes an existing actionListener.
     * @param listener the listener to remove
     */
    public void removeActionListener(ActionListener listener) {
        aei.removeListener(listener);
    }
    
    /**
     * Programmatically signals an action which triggers the appropriate listener which calls
     * the desired method.
     * @param action the action name
     */
    public void fireAction(String action) {
        aei.fireAction(this, action);
    }    
    
    public void setLocation(String location) {
        String oldLocation = this.location;
        location = location == null ? "" : location;
        this.location = location;
        firePropertyChange(this, PROPERTY_LOCATION, oldLocation, location);        
    }
    
    public String getLocation() {
        return this.location;
    }    
}
