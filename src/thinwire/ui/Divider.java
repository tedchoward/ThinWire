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

import thinwire.render.Renderer;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.DropEvent;
import thinwire.ui.event.DropListener;

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
public class Divider extends AbstractComponent implements ActionEventComponent, DropEventComponent {    
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>(this, EventListenerImpl.ACTION_VALIDATOR);    
    private EventListenerImpl<DropListener> dei = new EventListenerImpl<DropListener>(this);
    
    /**
     * Constructs a new Divider. 
     */
    public Divider() {
        setFocusCapable(false);
    }    
       
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
    }

    public void addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
    }
    
    public void addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
    }    
    
    public void removeActionListener(ActionListener listener) {
        aei.removeListener(listener);
    }

    public void fireAction(ActionEvent ev) {
        aei.fireAction(ev, null);
    }

    /**
     * A convienence method that is equivalent to <code>fireAction(new ActionEvent(this, action))</code>.
     * @param action the action name
     */
    public void fireAction(String action) {
        aei.fireAction(new ActionEvent(this, action), null);
    }
    
    public void addDropListener(DropEventComponent dragComponent, DropListener listener) {
        dei.addListener(dragComponent, listener);
    }
    
    public void addDropListener(DropEventComponent[] dragComponents, DropListener listener) {
        dei.addListener(dragComponents, listener);
    }    
    
    public void removeDropListener(DropListener listener) {
        dei.removeListener(listener);
    }    

    public void fireDrop(DropEvent ev) {
        dei.fireDrop(ev);
    }
}
