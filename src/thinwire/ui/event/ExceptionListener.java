/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.util.EventListener;

/**
 * @author Joshua J. Gertzen
 */
public interface ExceptionListener extends EventListener {
    public void exceptionOccurred(ExceptionEvent ev);
}