/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui;

import java.util.List;

/**
 * A <code>Container</code> is a <code>Component</code> that maintains a collection of other <code>Component</code>s as a group.
 * Additionally, <code>Container</code> is the foundation of all other container types in the framework such as <code>Panel</code>, <code>Frame</code>,
 * <code>Dialog</code>, <code>TabSheet</code>, etc.  It is worth noting that components within a <code>Container</code>, maintain there
 * X and Y coordinates relative to the <code>Container</code> itself.  Therefore, a <code>Component</code> with an X value of 5 that
 * is placed in a <code>Container</code> with an X value of 10, will actually be positioned at X coordinate 15.
 * @author Joshua J. Gertzen
 */
public interface Container<T extends Component> extends ItemChangeEventComponent {

    /**
     * Contains the formal property name for the scroll type of the container.
     * @see #setScroll(ScrollType)
     * @see #getScroll()
     * @see ScrollType
     */
    public static final String PROPERTY_SCROLL = "scroll";

    /**
     * Sets the scrollType for the X and Y axis of this Container.
     * By default the scroll type is ScrollType.NONE.
     * @param scrollType one of the enum ScrollType constants.
     */
    void setScroll(ScrollType scrollType);

    /**
     * Gets the current scrollType defined for the Container.
     * @return the current scrollType defined for the Container.
     */
    ScrollType getScroll();

    /**
     * Returns a list of components in the container.
     * @return this Container's children.
     */
    List<T> getChildren();

    /**
     * Get the child Component for this container that currently has the focus.
     * If this container contains another container and that container contains the 
     * Component at the bottom of the component hiearchy with the focus, this method
     * will return the <code>Container</code> contained by this container, not the
     * <code>Component</code>.  If you want the component at the bottom of the component
     * hiearchy that has the focus, use <code>getComponentWithFocus()</code>.
     * @return the child Component with focus, or null if there is no child with focus.
     */
    T getChildWithFocus();

    /**
     * Get the Component at the bottom of the component hiearchy that has the focus.
     * This method first walks up the component hiearchy to find the root Container, then
     * it walks down the hiearchy to locate the component at the bottom of the component
     * hiearchy that has the focus.
     * @return the Component at the bottom of component hiearchy that has the focus, or null if no component does.
     */
    T getComponentWithFocus();

    /**
     * Returns the usable inner width of the Container.
     * @return the usable inner width of the Container.
     */
    int getInnerWidth();

    /**
     * Returns the usable inner height of the Container.
     * @return the usable inner height of the Container.
     */
    int getInnerHeight();

}