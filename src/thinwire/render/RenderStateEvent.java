/*
 * Created on Jul 11, 2006
  */
package thinwire.render;

import java.util.EventObject;

import thinwire.ui.Component;

public class RenderStateEvent extends EventObject {
    private Integer id;
    
    public RenderStateEvent(Component source, Integer id) {
        super(source);
        this.id = id;
    }
    
    public Integer getId() {
        return id;
    }
}
