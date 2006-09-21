/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.util.EventListener;

/**
 * @author Joshua J. Gertzen
 */
public interface KeyPressListener extends EventListener {
    public void keyPress(KeyPressEvent ev);
}
