/*
                         ThinWire(TM) RIA Ajax Framework
               Copyright (C) 2003-2006 Custom Credit Systems
  
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.
  
  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  Place, Suite 330, Boston, MA 02111-1307 USA
 
  Users wishing to use this library in proprietary products which are not 
  themselves to be released under the GNU Public License should contact Custom
  Credit Systems for a license to do so.
  
                Custom Credit Systems, Richardson, TX 75081, USA.
                           http://www.thinwire.com
 #VERSION_HEADER#
 */
package thinwire.ui;

import java.util.List;

import thinwire.ui.layout.UnitModel;

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
    
    public static final String PROPERTY_UNIT_MODEL = "unitModel";

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
     * Returns the usable inner width of the Container. Always returns the value
     * in pixels regardless of the UnitModel.
     * 
     * @return the usable inner width of the Container.
     */
    int getInnerWidth();

    /**
     * Returns the usable inner height of the Container. Always returns the
     * value in pixels regardless of the UnitModel.
     * 
     * @return the usable inner height of the Container.
     */
    int getInnerHeight();
    
    void setUnitModel(UnitModel unitModel);
    
    UnitModel getUnitModel();

}