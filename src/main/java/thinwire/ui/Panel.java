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
 * <code>Panel</code> is a direct implementation of <code>Container</code>.
 * <p>
 * Example: <br>
 * <img src="doc-files/Panel-1.png">
 * <br>
 * <pre>
 *    TextField tf = new TextField("[Enter Value Here!]");
 *    tf.setBounds(5, 5, 150, 25);
 *       
 *    Panel p = new Panel();
 *    p.setBounds(10, 10, 200, 100);
 *    p.getChildren().add(tf);
 *    
 *    Frame f = Application.current().getFrame();
 *    f.getChildren().add(p);
 * </pre>
 * </p>
 * @author Joshua J. Gertzen
 */
public class Panel extends AbstractContainer<Panel, Component> {
    public Panel() {
        
    }
}
