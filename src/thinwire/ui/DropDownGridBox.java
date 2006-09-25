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

import java.util.List;

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
 */
public final class DropDownGridBox extends DropDown<GridBox> {
    public static class DefaultView implements DropDown.View<GridBox> {
        private int columnIndex;
        private String delimiter;
        private DropDownGridBox ddgb;
        private GridBox gb;
        
        DefaultView() {
            setColumnIndex(0);
            setDelimiter(",");
        }
        
        void init(DropDownGridBox ddgb, GridBox gb) {
            this.ddgb = ddgb;
            this.gb = gb;
        }
        
        public DropDownGridBox getDropDown() {
            return ddgb;
        }
        
        GridBox getGridBox() {
            GridBox gb = ddgb == null ? this.gb : ddgb.getComponent();
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
                
                for (List l : gb.getRows()) {
                    GridBox.Row r = (GridBox.Row)l;
                    sb.append(delimiter).append(r.get(columnIndex)).append(delimiter);
                    r.setChecked(s.indexOf(sb.toString()) != -1);
                    sb.setLength(0);
                }
            } else {
                List<GridBox.Row> rows = gb.getRows();
                
                for (List l : rows) {
                    GridBox.Row r = (GridBox.Row)l;
                    
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
    //#IFDEF V1_1_COMPAT

    /**
	 * Returns the GridBox part of the drop down.
	 * @return a GridBox
     * @deprecated use getComponent instead
	 */
	public GridBox getGridBox() {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use getComponent() instead.");
	    return getComponent();
    }
	
    /**
     * Sets the gridBox for the dropdown to a differnt gridBox.
     * @param gridBox the new gridbox.
     * @deprecated use setComponent instead
     */
    public void setGridBox(GridBox gridBox) {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use setComponent() instead.");
        setComponent(gridBox);
        if (gridBox.view != null) setView(gridBox.view);
    }

    /**
     * Get the delimiter separating values in the text field in cases where multiple
     * values have been selected.
	 * @return the delimiter separating values in the text field 
     * @deprecated switch to getGridBox().getView().getDelimiter(), this will be removed in a future update.
	 */
	public String getTextDelimiter() {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use getView().getDelimiter() instead.");
	    return ((DefaultView)getView()).getDelimiter();
    }
	
	/**
	 * When checkboxes are turned on, if multiple values are selected, the delimiter is placed 
	 * 	between the selected values when they appear in the text field.
	 * @param textDelimiter the delimiter (Default: comma ',')
	 * @throws IllegalArgumentException if textDelimiter is null or empty
     * @deprecated switch to getGridBox().getView().setDelimiter(), this will be removed in a future update.
	 */
	public void setTextDelimiter(String textDelimiter) {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use getView().setDelimiter() instead.");
        ((DefaultView)getView()).setDelimiter(textDelimiter);        
	}
	
	/**
     * Get the number of the column whose value is displayed in the text field.
	 * @return the number of the displayed column
     * @deprecated switch to getGridBox().getView().getColumnIndex(), this will be removed in a future update.
	 */
	public int getTextColumnIndex() {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use getView().getColumnIndex() instead.");
	    return ((DefaultView)getView()).getColumnIndex();
	}

	/**
	 * Chooses which column's value will be displayed in the text field.
	 * @param textColumnIndex Default: 0 (first column)
     * @throws IllegalArgumentException if testColumnIndex > the number of
     *   columns in the GridBox.
     * @deprecated switch to getGridBox().getView().setColumnIndex(), this will be removed in a future update.
	 */
    public void setTextColumnIndex(int textColumnIndex) {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use getView().setColumnIndex() instead.");
        ((DefaultView)getView()).setColumnIndex(textColumnIndex);
	}
    //#ENDIF
}
