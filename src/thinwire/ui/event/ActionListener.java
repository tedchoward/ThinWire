/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.util.EventListener;

/**
 * This is an interface that extends java.util.EventListener and responds to actions that occur
 * 	(ex: click a button).
 * @author Joshua J. Gertzen
 */
public interface ActionListener extends EventListener {
    public void actionPerformed(ActionEvent ev);
}
