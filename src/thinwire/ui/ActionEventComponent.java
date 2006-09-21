/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import thinwire.ui.event.ActionListener;

/**
 * @author Joshua J. Gertzen
 */
public interface ActionEventComponent extends Component {
    public static final String ACTION_CLICK = "click";

    
    /**
     * Add an actionListener which associates an action (ex: "click") with some method call.
     * @param action the action to specficially be notified of
     * @param listener the listener to add
     */
    public void addActionListener(String action, ActionListener listener);

    /**
     * Add an actionListener which associates an action (ex: "click") with some method call.
     * @param actions the actions to specficially be notified of
     * @param listener the listener to add
     */
    public void addActionListener(String[] actions, ActionListener listener);
    
    /**
     * Unregister an ActionListener from Menu action occurred notifications.
     * @param listener the listener that should no longer receive action occurred notifications.
     */
    public void removeActionListener(ActionListener listener);
}
