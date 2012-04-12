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

import thinwire.ui.event.ItemChangeListener;

/**
 * @author Joshua J. Gertzen
 */
public interface ItemChangeEventComponent extends Component {
    /**
     * Adds a listener which executes a method when something is changed.
     * @param listener the listener to add
     */
    public Component addItemChangeListener(ItemChangeListener listener);

    /**
     * Removes an existing itemChangeListener.
     * @param listener the listener to remove
     */
    public Component removeItemChangeListener(ItemChangeListener listener);
}
