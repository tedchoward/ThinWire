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
public class Divider extends AbstractComponent {    
    /**
     * Constructs a new Divider. 
     */
    public Divider() {
        setFocusCapable(false);
    }    
}
