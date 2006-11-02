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

import java.util.Date;

import thinwire.render.Renderer;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;

/**
 * A <code>DateBox</code> is a <code>Component</code> that displays a
 * month-view calendar. The user can click the arrows in the header to change
 * the currently displayed month. Clicking on a day, selects that day and fires
 * a <code>PROPERTY_SELECTED_DATE</code> property change event.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/DateBox-1.png"> <br>
 * 
 * <pre>
 * final Dialog dlg = new Dialog(&quot;DateBox Test&quot;);
 * dlg.setBounds(10, 10, 320, 240);
 * DateBox db = new DateBox();
 * db.setBounds(10, 10, dlg.getInnerWidth() - 20, dlg.getInnerHeight() - 20);
 * dlg.getChildren().add(db);
 * dlg.setVisible(true);
 * </pre>
 * 
 * </p>
 * <p>
 * <b>Keyboard Navigation:</b><br>
 * <table border="1">
 * <tr>
 * <td>KEY</td>
 * <td>RESPONSE</td>
 * <td>NOTE</td>
 * </tr>
 * </table>
 * </p>
 * @author Ted C. Howard
 */
public class DateBox extends AbstractComponent implements ActionEventComponent {
    
    public static final String PROPERTY_SELECTED_DATE = "selectedDate";
    
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>(this, EventListenerImpl.ACTION_VALIDATOR);
    private Date selectedDate;
    
    public DateBox() {
        this(new Date());
    }
    
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
    }
    
    /**
     * Creates a new <code>DateBox</code> with the specified <code>selectedDate</code>.
     * @param selectedDate the <code>Date</code> to be initially selected in the component
     */
    public DateBox(Date selectedDate) {
        setSelectedDate(selectedDate);
    }
    
    /**
     * Returns the currently selected date.
     * @return a <code>Date</code> object representing the currently selected date
     */
    public Date getSelectedDate() {
        return selectedDate;
    }
    
    /**
     * Sets the selected date in the component.
     * @param selectedDate the new <code>Date</code> to be selected in the component
     */
    public void setSelectedDate(Date selectedDate) {
        Date oldDate = this.selectedDate;
        this.selectedDate = selectedDate;
        firePropertyChange(this, PROPERTY_SELECTED_DATE, oldDate, this.selectedDate);
    }

    public void addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
    }

    public void addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
    }

    public void removeActionListener(ActionListener listener) {
        aei.addListener(listener);
    }
    
    public void fireAction(ActionEvent ev) {
        aei.fireAction(ev, Date.class);
    }
    
    /**
     * A convienence method that is equivalent to <code>fireAction(new ActionEvent(this, action, date))</code>.
     * @param action the action that occured.
     * @param date the Date cell in the DateBox on which the action occured.
     */
    public void fireAction(String action, Date date) {
        aei.fireAction(new ActionEvent(this, action, date), Date.class);
    }
}
