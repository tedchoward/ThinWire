/*
 * Created on Jul 11, 2006
  */
package thinwire.render;

import java.util.EventListener;

public interface RenderStateListener extends EventListener {
    void renderStateChange(RenderStateEvent ev);
}
