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

/**
 * A Divider provides a visual separation between two sections of a container.
 * <p>
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/Divider-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;Divider Test&quot;);
 * dlg.setBounds(25, 25, 250, 150);
 * 
 * Divider horizDiv = new Divider();
 * horizDiv.setBounds(10, 10, 210, 10);
 * dlg.getChildren().add(horizDiv);
 * 
 * Divider vertDiv = new Divider();
 * vertDiv.setBounds(110, 30, 10, 90);
 * dlg.getChildren().add(vertDiv);
 * 
 * dlg.setVisible(true);
 * </pre>
 * 
 * </p>
 * @author Joshua J. Gertzen
 */
public class Divider extends AbstractComponent<Divider> {    
    /**
     * Constructs a new Divider. 
     */
    public Divider() {
        setFocusCapable(false);
    }    
}
