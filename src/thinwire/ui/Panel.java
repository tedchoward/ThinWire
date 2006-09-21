/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
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
public class Panel extends AbstractContainer<Component> {
    public Panel() {
        
    }
}
