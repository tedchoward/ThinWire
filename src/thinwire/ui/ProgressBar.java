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
 * A ProgressBar is a screen element that has a visible selection that can be set to any size between zero and a specified length.
 * ProgressBars are either horizontal or vertical depending on their dimensions.  If the width is greater than the height,
 * then the ProgressBar is horizontal, otherwise it is vertical.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/ProgressBar-1.png"> <br>
 * 
 * <pre>
 * Frame f = Application.current().getFrame();
 * f.setTitle(&quot;ProgressBar Test&quot;);
 * Dialog d = new Dialog(&quot;ProgressBar Test&quot;);
 * d.setBounds(10, 10, 250, 150);
 * final ProgressBar pb = new ProgressBar(5, 3);
 * pb.setBounds(10, 10, 100, 20);
 * d.getChildren().add(pb);
 * final TextField tf = new TextField();
 * tf.setBounds(10, 40, 50, 20);
 * tf.setText(String.valueOf(pb.getCurrentIndex()));
 * d.getChildren().add(tf);
 * Button b = new Button(&quot;SetValue&quot;);
 * b.setBounds(70, 35, 60, 30);
 * b.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent ev) {
 *         pb.setCurrentIndex(Integer.parseInt(tf.getText()));
 *     }
 * });
 * d.getChildren().add(b);
 * f.getChildren().add(d);
 * </pre>
 * 
 * </p>
 * 
 * @author Ted C. Howard
 *
 */
public class ProgressBar extends AbstractRangeComponent implements ActionEventComponent, DropEventComponent {
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>(this, EventListenerImpl.ACTION_VALIDATOR);    
    private EventListenerImpl<DropListener> dei = new EventListenerImpl<DropListener>(this);
    
    /**
     * Constructs a new <code>ProgressBar</code> with a length of 100 and a currentIndex of 0.
     *
     */
    public ProgressBar() {
        this(100, 0);
    }
    
    /**
     * Constructs a new <code>ProgressBar</code> with the specified length and a currentIndex of 0.
     * @param length
     */
    public ProgressBar(int length) {
        this(length, 0);
    }
    
    /**
     * Constructs a new <code>ProgressBar</code> with the specified length and currentIndex.
     * @param length
     * @param currentIndex
     */
    public ProgressBar(int length, int currentIndex) {
        setLength(length);
        setCurrentIndex(currentIndex);
    }
    
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
        dei.setRenderer(r);
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
        aei.fireAction(ev);
    }

    /**
     * A convienence method that is equivalent to <code>fireAction(new ActionEvent(this, action))</code>.
     * @param action the action name
     */
    public void fireAction(String action) {
        aei.fireAction(new ActionEvent(this, action));
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
