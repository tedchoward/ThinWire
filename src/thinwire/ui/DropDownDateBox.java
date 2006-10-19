package thinwire.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;

/**
 * A <code>DropDownDateBox</code> is a <code>DropDown</code> that contains
 * a <code>DateBox</code>.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/DropDownDateBox-1.png"> <br>
 * 
 * <pre>
 * final Dialog dlg = new Dialog(&quot;DateBox Test&quot;);
 * dlg.setBounds(10, 10, 320, 240);
 * DropDownDateBox dd = new DropDownDateBox();
 * dd.setBounds(10, 10, 200, 20);
 * 
 * dlg.getChildren().add(dd);
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
 * <tr>
 * <td>Down Arrow</td>
 * <td>Drops the DateBox down.</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * <tr>
 * <td>Esc</td>
 * <td>Closes the DateBox</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * </table> See DateBox for additional keyboard support.
 * </p>
 * 
 * @author Ted C. Howard
 */
public class DropDownDateBox extends DropDown<DateBox> {
    
    private static class DefaultView extends DropDown.AbstractView<DateBox> {
        
        private static final float HEIGHT_MULTIPLIER = 17 / 20;
        private static final int MIN_WIDTH = 150;
        private static final int MIN_HEIGHT = 170;
        
        private SimpleDateFormat dateFormat;
        
        DefaultView() {

        }
        
        void init(DropDownDateBox ddDb, DateBox db) {
            super.init(ddDb, db);
            addCloseComponent(ddc);
            ddc.addActionListener(DateBox.ACTION_CLICK, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    dd.setText(getValue().toString());
                }
            });
        }

        public DropDownDateBox getDropDown() {
            return (DropDownDateBox) dd;
        }

        public int getOptimalHeight() {
            if (ddc.getWidth() < MIN_WIDTH) ddc.setWidth(getOptimalWidth());
            int dbHeight = Math.round(ddc.getWidth() * HEIGHT_MULTIPLIER);
            if (dbHeight < MIN_HEIGHT) dbHeight = MIN_HEIGHT;
            return dbHeight;
        }
        
        public int getOptimalWidth() {
            int ddWidth = dd.getWidth();
            int dbWidth = ddc.getWidth();
            return dbWidth >= ddWidth ? dbWidth : ddWidth >= MIN_WIDTH ? ddWidth : MIN_WIDTH;
        }

        public Object getValue() {
            return dateFormat.format(ddc.getSelectedDate());
        }

        public void setValue(Object value) {
            String s;
            if (value == null) {
                s = "";
            } else if (value instanceof String) {
                s = (String) value;
            } else {
                s = value.toString();
            }
            try {
                ddc.setSelectedDate(dateFormat.parse(s));
            } catch (ParseException e) {
                
            }
        }
        
    }

    public DropDownDateBox() {
        this(null);
    }
    
    /**
     * Creates a DropDownDateBox initialized a specific date
     * @param text The formatted date to initilize the DateBox to (MM/dd/yyyy)
     */
    public DropDownDateBox(String text) {
        super(new DefaultView(), new DateBox());
        ((DefaultView)getView()).init(this, this.getComponent());
        setEditMask("MM/dd/yyyy");
        if (text != null) {
            setText(text);
        } else {
            setText(getView().getValue().toString());
        }
    }
    
    /**
     * This method accepts an edit mask as a String and applies it to the
     * <code>TextField</code>. The edit mask also defines the Format that the
     * Date is displayed.
     */
    public void setEditMask(String editMask) {
        if (editMask.matches(".*?[9#AaXxhmp]+.*?")) throw new IllegalArgumentException("Only date mask characters are valid");
        ((DefaultView) getView()).dateFormat = new SimpleDateFormat(editMask);
        super.setEditMask(editMask);
    }

}
