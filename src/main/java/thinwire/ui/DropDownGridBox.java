/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

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
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.ui;

import java.util.List;

import thinwire.ui.GridBox.Row;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

/**
 * A DropDownGridBox wraps around a GridBox component to provide drop down
 * features.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/DropDownGridBox-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;DropDownGridBox Test&quot;);
 * dlg.setBounds(25, 25, 400, 200);
 * 
 * final TextField tf = new TextField();
 * tf.setBounds(275, 25, 100, 20);
 * dlg.getChildren().add(tf);
 * 
 * DropDownGridBox ddgb = new DropDownGridBox();
 * ddgb.setBounds(25, 25, 230, 20);
 * 
 * GridBox gb = ddgb.getComponent();
 * gb.setVisibleHeader(true);
 * gb.setHeight(120);
 * 
 * GridBox.Column col1 = new GridBox.Column();
 * col1.setName(&quot;Name&quot;);
 * GridBox.Column col2 = new GridBox.Column();
 * col2.setName(&quot;City&quot;);
 * GridBox.Column col3 = new GridBox.Column();
 * col3.setName(&quot;Country&quot;);
 * gb.getColumns().add(col1);
 * gb.getColumns().add(col2);
 * gb.getColumns().add(col3);
 * 
 * String[] names = { &quot;Smythe&quot;, &quot;Janes&quot;, &quot;Warren&quot;, &quot;Dempster&quot;, &quot;Hilcox&quot; };
 * String[] cities = { &quot;Tokyo&quot;, &quot;Hong Kong&quot;, &quot;Lethbridge&quot;, &quot;Juarez&quot;, &quot;Juneau&quot; };
 * String[] countries = { &quot;Japan&quot;, &quot;China&quot;, &quot;Canada&quot;, &quot;Mexico&quot;, &quot;USA&quot; };
 * 
 * for (int r = 0; r &lt; 5; r++) {
 *     GridBox.Row row = new GridBox.Row();
 *     row.add(names[r]);
 *     row.add(cities[r]);
 *     row.add(countries[r]);
 *     gb.getRows().add(row);
 * }
 * 
 * ((DropDownGridBox.DefaultView) ddgb.getView()).setColumnIndex(2);
 * 
 * ddgb.addPropertyChangeListener(DropDownGridBox.PROPERTY_TEXT,
 *         new PropertyChangeListener() {
 *             public void propertyChange(PropertyChangeEvent evt) {
 *                 tf.setText((String) evt.getNewValue());
 *             }
 *         });
 * 
 * dlg.getChildren().add(ddgb);
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
 * <td>Drops the Grid Box down.</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * <tr>
 * <td>Esc</td>
 * <td>Closes the Grid Box</td>
 * <td>Only if the component has focus.</td>
 * </tr>
 * </table> See GridBox for additional keyboard support.
 * </p>
 * 
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public class DropDownGridBox extends DropDown<GridBox> {
    public static class DefaultView extends DropDown.AbstractView<GridBox> {
        
        private static final int MIN_SIZE = 25;
        private static final int DEFAULT_MAX_WIDTH = 640 / 2 - 10;
        private static final int DEFAULT_MAX_HEIGHT = 480 / 2 - 20;
        
        private int columnIndex;
        private String delimiter;
        
        private PropertyChangeListener childChangePcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (ev.getOldValue() != null) {
                    GridBox gb = (GridBox) ev.getOldValue();
                    gb.removePropertyChangeListener(childChangePcl);
                    gb.removeActionListener(clickListener);
                    gb.removeItemChangeListener(itemChangeListener);
                }
                if (ev.getNewValue() != null) {
                    GridBox gb = (GridBox) ev.getNewValue();
                    gb.addPropertyChangeListener(GridBox.Row.PROPERTY_ROW_CHILD, childChangePcl);
                    gb.addActionListener(GridBox.ACTION_CLICK, clickListener);
                    iterateRows(gb.getRows());
                    gb.addItemChangeListener(itemChangeListener);
                }
            }
        };
        
        private PropertyChangeListener checkListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                dd.setText(getValue().toString());
            }
        };
        
        private ActionListener clickListener = new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (((GridBox.Range)ev.getSource()).getRow().getChild() == null) {
                    dd.setText(getValue().toString());
                }
            }
        };
        
        private ItemChangeListener itemChangeListener = new ItemChangeListener() {
			public void itemChange(ItemChangeEvent ev) {
				ItemChangeEvent.Type type = ev.getType();
				GridBox.Range range = (GridBox.Range) ev.getPosition();
				int columnIndex = range.getColumnIndex();
				int rowIndex = range.getRowIndex();
				
				if ((columnIndex == -1 && rowIndex >= 0) && (type == ItemChangeEvent.Type.ADD || type == ItemChangeEvent.Type.SET)) {
					GridBox.Row row = (GridBox.Row) ev.getNewValue();
					GridBox child = row.getChild();
					if (child != null) {
						child.addPropertyChangeListener(GridBox.Row.PROPERTY_ROW_CHILD, childChangePcl);
						child.addActionListener(GridBox.ACTION_CLICK, clickListener);
						iterateRows(child.getRows());
						child.addItemChangeListener(itemChangeListener);
					}
				}
			}
        };
        
        DefaultView() {
            setColumnIndex(0);
            setDelimiter(",");
        }
        
        @Override
        protected void init(DropDown<GridBox> ddgb, GridBox gb) {
            super.init(ddgb, gb);
            if (dd != null) {
                ddc.addPropertyChangeListener(GridBox.Row.PROPERTY_ROW_CHILD, childChangePcl);
                ddc.addActionListener(GridBox.ACTION_CLICK, clickListener);
                ddc.addPropertyChangeListener(GridBox.Row.PROPERTY_ROW_CHECKED, checkListener);
                ddc.addItemChangeListener(itemChangeListener);
                iterateRows(ddc.getRows());
            }
        }
        
        void iterateRows(List<GridBox.Row> rows) {
            for (GridBox.Row r : rows) {
                GridBox child = r.getChild();
                if (child != null) {
                    child.addPropertyChangeListener(GridBox.Row.PROPERTY_ROW_CHILD, childChangePcl);
                    child.addActionListener(GridBox.ACTION_CLICK, clickListener);
                    iterateRows(child.getRows());
                }
            }
        }
        
        public DropDownGridBox getDropDown() {
            return (DropDownGridBox) dd;
        }
        
        GridBox getGridBox() {
            GridBox gb = dd == null ? ddc : dd.getComponent();
            GridBox.Row row = gb.getSelectedRow();
            
            while (row != null && row.getChild() != null) {
                gb = row.getChild();
                row = gb.getSelectedRow();
            }
            
            return gb;
        }
                
        public Object getValue() {
            GridBox gb = getGridBox();
            if (gb == null) throw new IllegalStateException("gridBox == null");
            String delimiter;
            int columnIndex;
            //#IFDEF V1_1_COMPAT
            
            if (gb.view == null) {
            //#ENDIF
                delimiter = this.delimiter;
                columnIndex = this.columnIndex;
            //#IFDEF V1_1_COMPAT
            } else {
                DefaultView view = (DefaultView)gb.view;
                delimiter = view.delimiter;
                columnIndex = view.columnIndex;
            }
            //#ENDIF
            
            if (columnIndex >= gb.getColumns().size()) throw new IllegalStateException("columnIndex >= gridBox.getColumns().size()");
            String s;
            
            if (gb.isVisibleCheckBoxes()) {
                StringBuilder sb = new StringBuilder();
                
                for (GridBox.Row r : gb.getCheckedRows()) {
                    if (r.isChecked()) sb.append(r.get(columnIndex)).append(delimiter);
                }
                
                s = sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
            } else {
                s = gb.getSelectedRow().get(columnIndex).toString();
            }
            return s;
        }
        
        public void setValue(Object value) {
            GridBox gb = getGridBox();            
            if (gb == null) throw new IllegalStateException("gridBox == null");
            if (gb.getColumns().size() == 0) return;

            String delimiter;
            int columnIndex;
            //#IFDEF V1_1_COMPAT
            
            if (gb.view == null) {
            //#ENDIF
            delimiter = this.delimiter;
            columnIndex = this.columnIndex;
            //#IFDEF V1_1_COMPAT
            } else {
                DefaultView view = (DefaultView)gb.view;
                delimiter = view.delimiter;
                columnIndex = view.columnIndex;
            }          
            //#ENDIF
            
            if (columnIndex >= gb.getColumns().size()) throw new IllegalStateException("columnIndex >= gridBox.getColumns().size()");
            String s;
    
            if (value == null) {
                s = "";
            } else if (value instanceof String) {
                s = (String)value;
            } else {
                s = value.toString();
            }
            
            if (gb.isVisibleCheckBoxes()) {
                StringBuilder sb = new StringBuilder();
                sb.append(delimiter).append(s).append(delimiter);
                s = sb.toString();
                sb.setLength(0);
                
                for (GridBox.Row r : gb.getRows()) {
                    sb.append(delimiter).append(r.get(columnIndex)).append(delimiter);
                    r.setChecked(s.indexOf(sb.toString()) != -1);
                    sb.setLength(0);
                }
            } else {
                List<GridBox.Row> rows = gb.getRows();
                
                for (GridBox.Row r : rows) {
                    if (r.getChild() == null) {
                        value = r.get(columnIndex);
                        
                        if (value != null && value.toString().equals(s)) {
                            r.setSelected(true);
                            break;
                        }
                    }
                }
            }
        }
        
        public int getColumnIndex() {
            return columnIndex;
        }
        
        public void setColumnIndex(int columnIndex) {
            if (columnIndex < 0 || columnIndex > 128) throw new IllegalArgumentException("columnIndex < 0 || columnIndex > 128");
            this.columnIndex = columnIndex;
        }
        
        public String getDelimiter() {
            return delimiter;
        }
        
        public void setDelimiter(String delimiter) {
            if (delimiter == null || delimiter.length() == 0) throw new IllegalArgumentException("delimiter == null || delimiter.length() == 0");
            this.delimiter = delimiter;
        }

        //TODO: This is not correct when you have fixed-width columns (i.e. non-auto-size columns)
		public int getOptimalWidth() {
            int width = 0;
            List<GridBox.Column> cols = ddc.getColumns(); 
            boolean[] visibleState = new boolean[cols.size()];
            
            for (int i = 0, size = cols.size(); i < size; i++) {
                visibleState[i] = cols.get(i).isVisible();
            }
                    
            if (ddc.isVisibleHeader()) {
                int cnt = 0;

                for (int i = 0, size = cols.size(); i < size; i++) {
                    if (visibleState[i]) {
                        GridBox.Column col = cols.get(i);
                        String name = col.getHeader().getText();
                        if (name.length() == 0) name = col.getName();
                        int len = name.length();
                        String upperName = name.toUpperCase();
                        if (name.equals(upperName)) len = len / 7 + 1;
                        if (len < 4) len++;
                        cnt += len;
                    }
                }
                
                if (cnt > width) width = cnt;            
            }

            for (Row r : ddc.getRows()) {
                int cnt = 0;
                
                for (int i = 0, size = r.size(); i < size; i++) {
                    if (visibleState[i]) {
                        Object cell = r.get(i);
                     
                        if (cell != null) {                    
                            String value = cell.toString();
                            int len = value.length();
                            String upperValue = value.toUpperCase();
                            if (value.equals(upperValue)) len += len / 7 + 1;
                            if (len < 4) len++;
                            cnt += len;
                        }
                    }
                }
                
                if (cnt > width) width = cnt;
            }
            
            if (ddc.isVisibleCheckBoxes()) width += 3;
            
            width *= 6.6; //TODO: Hardcoded character width.        
            
            if (ddc.getParent() instanceof DropDownGridBox) {
                int ddWidth = ((DropDownGridBox)ddc.getParent()).getWidth();
                if (ddWidth > width) width = ddWidth;
            }
            
            Application app = Application.current();
            int maxWidth = app != null ? app.getFrame().getInnerWidth() / 2 - 10 : DEFAULT_MAX_WIDTH;
            if (width > maxWidth) width = maxWidth;
            if (width < MIN_SIZE) width = MIN_SIZE;
            return width;
		}

		public int getOptimalHeight() {
            int height = ddc.getRows().size();
            if (height < 3) height = 3;
            height *= 14; //TODO: Hardcoded row height        
            height += 10; //TODO: Hardcoded fudge factor for border        
            if (ddc.isVisibleHeader()) height += 16; //TODO: Hardcoded column header size
            
            Application app = Application.current();
            int maxHeight = app != null ? app.getFrame().getInnerHeight() / 2 - 20 : DEFAULT_MAX_HEIGHT;
            if (ddc.getParent() instanceof DropDownGridBox) maxHeight -= ((DropDownGridBox)ddc.getParent()).getHeight();
            if (height > maxHeight) height = maxHeight;
            if (height < MIN_SIZE) height = MIN_SIZE; 
            return height;
		}
    }
	
	/**
	 * Constructs a new DropDownGridBox with no text.
	 */
	public DropDownGridBox() {
	    this(null);
	}
	
	/**
	 * Constructs a new DropDownGridBox with the specified text.
	 */
	public DropDownGridBox(String text) {
        super(new DefaultView(), new GridBox());
        ((DefaultView)getView()).init(this, this.getComponent());
	    if (text != null) setText(text);        
	}	
}
