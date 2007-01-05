/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
   	            email: info@thinwire.com    ph: +1 (888) 644-6405
 	                        http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
#VERSION_HEADER#
*/
package thinwire.ui;

import java.util.Date;

import thinwire.ui.event.ActionEvent;

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
public class DateBox extends AbstractComponent {
    public static final String PROPERTY_SELECTED_DATE = "selectedDate";
    
    private Date selectedDate;
    
    public DateBox() {
        this(new Date());
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
    
    public void fireAction(ActionEvent ev) {
        if (ev == null) throw new IllegalArgumentException("ev == null");
        if (!(ev.getSource() instanceof Date)) throw new IllegalArgumentException("!(ev.getSource() instanceof Date)");
        setSelectedDate((Date)ev.getSource());        
        super.fireAction(ev);
    }
}
