/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package thinwire.ui;

import java.util.List;

import thinwire.ui.layout.Layout;

/**
 * A <code>Container</code> is a <code>Component</code> that maintains a collection of other <code>Component</code>s as a group.
 * Additionally, <code>Container</code> is the foundation of all other container types in the framework such as <code>Panel</code>, <code>Frame</code>,
 * <code>Dialog</code>, <code>TabSheet</code>, etc.  It is worth noting that components within a <code>Container</code>, maintain there
 * X and Y coordinates relative to the <code>Container</code> itself.  Therefore, a <code>Component</code> with an X value of 5 that
 * is placed in a <code>Container</code> with an X value of 10, will actually be positioned at X coordinate 15.
 * @author Joshua J. Gertzen
 */
@SuppressWarnings("unchecked")
public interface Container<T extends Component> extends ItemChangeEventComponent {
    public enum ScrollType {NONE, AS_NEEDED, ALWAYS}
    
    /**
     * Contains the formal property name for the scroll type of the container.
     * @see #setScrollType(ScrollType)
     * @see #getScrollType()
     * @see ScrollType
     */
    public static final String PROPERTY_SCROLL_TYPE = "scrollType";
    
    /**
     * Contains the formal property name for the layout manager of the container.
     * @see #setLayout(Layout)
     * @see #getLayout()
     * @see thinwire.ui.layout.Layout
     */
    public static final String PROPERTY_LAYOUT = "layout";

    /**
     * Gets the current scrollType defined for the Container.
     * @return the current scrollType defined for the Container.
     */
    ScrollType getScrollType();

    /**
     * Sets the scrollType for the X and Y axis of this Container.
     * By default the scroll type is ScrollType.NONE.
     * @param scrollType one of the enum ScrollType constants.
     * @see #PROPERTY_SCROLL
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setScrollType(ScrollType scrollType);

    /**
     * Gets the current layout manager that is responsible for sizing and
     * positioning components of this <code>Container</code>.
     * @return the </code>Layout</code> implementation used by this <code>Container</code>.
     * @see #setLayout(Layout)
     * @see Component#getLimit
     * @see Component#setLimit(Object)
     * @see thinwire.ui.layout.Layout
     */
    Layout getLayout();

    /**
     * Sets the layout manager that is used to size and position components of this <code>Container</code>.
     * If a <code>Layout</code> is not specified for a container, then you must size and position of the components
     * within a container manually.
     * <b>Default:</b> null (i.e. fixed absolute coordinate positioning)
     * @param layout any class implementing the <code>Layout</code> interface, or null.
     * @see #PROPERTY_LAYOUT
     * @see Component#getLimit
     * @see Component#setLimit(Object)
     * @see thinwire.ui.layout.Layout
     * @see thinwire.ui.event.PropertyChangeEvent
     */
    void setLayout(Layout layout);
    
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
     * Returns the usable inner width of the <code>Container</code>.
     * @return the usable inner width of the <code>Container</code>.
     */
    int getInnerWidth();

    /**
     * Returns the usable inner height of the <code>Container</code>.
     * @return the usable inner height of the <code>Container</code>.
     */
    int getInnerHeight();
}
