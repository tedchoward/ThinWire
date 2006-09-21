/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.util.EventObject;

/**
 * @author Joshua J. Gertzen
 */
public final class ActionEvent extends EventObject {
    private String action;
    
    public ActionEvent(Object source, String action) {
        super(source);
        this.action = action;
    }
    
    public String getAction() {
        return action;
    }
}
