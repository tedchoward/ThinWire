package thinwire.ui;

import java.util.Date;

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
}
