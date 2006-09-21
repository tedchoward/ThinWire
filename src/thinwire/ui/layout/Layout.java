/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.layout;

import thinwire.ui.Container;
import thinwire.ui.Component;

/**
 * @author Joshua J. Gertzen
 */
public interface Layout {
    boolean isAutoLayout();
    void setAutoLayout(boolean autoLayout);
    Container<Component> getContainer();
    void setContainer(Container<Component> container);
    void apply();
}
