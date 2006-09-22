/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.render.Renderer;
import thinwire.ui.event.ActionListener;

/**
 * A Divider provides a visual separation between two sections of a container.
 * <p>
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Divider-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;Divider Test&quot;);
 * dlg.setBounds(25, 25, 250, 150);
 * 
 * Divider horizDiv = new Divider();
 * horizDiv.setBounds(10, 10, 210, 10);
 * dlg.getChildren().add(horizDiv);
 * 
 * Divider vertDiv = new Divider();
 * vertDiv.setBounds(110, 30, 10, 90);
 * dlg.getChildren().add(vertDiv);
 * 
 * dlg.setVisible(true);
 * </pre>
 * 
 * </p>
 * @author Joshua J. Gertzen
 */
public final class Divider extends AbstractComponent implements ActionEventComponent {    
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>();    
    
    /**
     * Constructs a new Divider. 
     */
    public Divider() {
        setFocusCapable(false);
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
}
