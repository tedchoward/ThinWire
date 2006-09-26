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


import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import thinwire.render.Renderer;
import thinwire.ui.AlignX;
import thinwire.ui.DropDownGridBox;
import thinwire.ui.DropDownGridBox.DefaultView;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.ui.style.Border;
import thinwire.ui.style.Color;
import thinwire.ui.style.Style;
import thinwire.util.ArrayGrid;
import thinwire.util.Grid;

/**
 * A GridBox is a screen component that can display multi-column rows or data.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/GridBox-1.png"> <br>
 * 
 * <pre>
 * Dialog dlg = new Dialog(&quot;GridBox Test&quot;);
 * dlg.setBounds(25, 25, 600, 300);
 * 
 * final TextField tf = new TextField();
 * tf.setBounds(375, 25, 150, 20);
 * dlg.getChildren().add(tf);
 * 
 * GridBox gbx = new GridBox();
 * gbx.setBounds(25, 25, 300, 120);
 * gbx.setVisibleHeader(true);
 * gbx.setVisibleCheckBoxes(true);
 * gbx.setFullRowCheckBox(true);
 * 
 * GridBox.Column col1 = new GridBox.Column();
 * col1.setName(&quot;Name&quot;);
 * col1.setVisible(true);
 * GridBox.Column col2 = new GridBox.Column();
 * col2.setName(&quot;City&quot;);
 * col2.setVisible(true);
 * 
 * gbx.getColumns().add(col1);
 * gbx.getColumns().add(col2);
 * String[] names = { &quot;Smythe&quot;, &quot;Janes&quot;, &quot;Warren&quot;, &quot;Dempster&quot;, &quot;Hilcox&quot; };
 * String[] cities = { &quot;Tokyo&quot;, &quot;Hong Kong&quot;, &quot;Lethbridge&quot;, &quot;Moose Jaw&quot;, &quot;Red Deer&quot; };
 * 
 * for (int r = 0; r &lt; 5; r++) {
 *     GridBox.Row row = new GridBox.Row();
 *     row.add(names[r]);
 *     row.add(cities[r]);
 *     gbx.getRows().add(row);
 * }
 * gbx.addPropertyChangeListener(GridBox.Row.PROPERTY_ROW_SELECTED,
 *         new PropertyChangeListener() {
 *             public void propertyChange(PropertyChangeEvent evt) {
 *                 tf.setText((String) ((GridBox.Row) evt.getSource()).get(0));
 *             }
 *         });
 * 
 * dlg.getChildren().add(gbx);
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
 * <td>Space</td>
 * <td>Fires PropertyChangeEvent( propertyName = GridBox.Row.PROPERTY_CHECKED )</td>
 * <td>Only if isVisibleCheckBoxes() is true.</td>
 * </tr>
 * <tr>
 * <td>Enter</td>
 * <td>Fires Action( actionName = GridBox.ACTION_CLICK )</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>Arrow Up/Down</td>
 * <td>Fires PropertyChangeEvent( propertyName = GridBox.Row.PROPERTY_SELECTED )</td>
 * <td></td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author Joshua J. Gertzen
 */
public final class GridBox extends AbstractComponent implements Grid<GridBox.Row, GridBox.Column>, ActionEventComponent, ItemChangeEventComponent {
    private static Logger log = Logger.getLogger(GridBox.class.getName());
    public static final class CellPosition {
        private int rowIndex;
        private int columnIndex;
        
        CellPosition(int rowIndex, int columnIndex) {
            this.rowIndex = rowIndex;
            this.columnIndex = columnIndex;
        }
        
        public int getColumnIndex() {
            return columnIndex;
        }
        
        public int getRowIndex() {
            return rowIndex;
        }
    }
    	
    public static final class Row extends ArrayGrid.Row {
        public static final String PROPERTY_ROW_SELECTED = "rowSelected";
        public static final String PROPERTY_ROW_CHECKED = "rowChecked";
        public static final String PROPERTY_ROW_CHILD = "rowChild";    

        private GridBox child;
        private boolean checked;

        /**
         * Construct a Row.
         */
        public Row() {
            
        }
        
        /**
         * Construct a Row that contains the values of the specified Collection.
         * @param c the new Row's values.
         */
        public Row(Collection<? extends Object> c) {
            this();
            addAll(c);
        }
        
        /**
         * Construct a Row that contains the values of the specified Array.
         * @param a the new Row's values.
         */
        public Row(Object... a) {
            this();
            
            for (Object o : a)
                add(o);
        }
        
        /**
         * Returns the checked state of the row.
         * @return the checked state of the row.
         * @throws IllegalStateException if the row has not been added to a GridBox. 
         */
        public boolean isChecked() {
            return checked;
        }
        
        /**
         * Sets the checked state of the row.
         * @param checked true to check the row, false to uncheck it.
         * @throws IllegalStateException if the row has not been added to a GridBox. 
         */
        public void setChecked(boolean checked) {
            GridBox gb = (GridBox)getParent();
            
            if (gb == null) {
                this.checked = checked;
            } else {                
                boolean oldChecked = this.checked;
        		if (oldChecked) gb.checkedRows.remove(this); 
                this.checked = checked;
                if (checked) gb.checkedRows.add(this);
        		
                if (gb.firePropertyChange(this, PROPERTY_ROW_CHECKED, oldChecked, checked)) {
                    //#IFDEF V1_1_COMPAT                    
                    if (gb.compatModeOn) gb.firePropertyChange(this, "checked", oldChecked, checked);
                    //#ENDIF
                    DropDownGridBox dd = getDropDown(gb);
                    if (dd != null) dd.setText(dd.getView().getValue().toString());
                }
            }
        }
                
        /**
         * Returns the selected state of the row.
         * @return the selected state of the row.
         */
        public boolean isSelected() {
            GridBox gb = (GridBox)getParent();
            boolean selected = gb == null ? false : gb.selectedRowIndex == this.getIndex();
            return selected;
        }
        
        /**
         * Sets the selected state of the row.
         * @param selected true to select the row, false to unselect it.
         * @throws IllegalStateException if the row has not been added to a GridBox. 
         */
        public void setSelected(boolean selected) {
            GridBox gb = (GridBox)getParent();
            if (gb == null) throw new IllegalStateException("the row must be added to a GridBox before it can be set to selected");
            int rowIndex = getIndex();
            boolean oldSelected = gb.selectedRowIndex == rowIndex;
    	    gb.selectedRowIndex = selected ? rowIndex : 0;
            
            //If the selected row has a child and this gridbox is part of a drop-down
            //then we want to take the text value from the drop-down and match it 
            //against the child gridbox
            GridBox child = getChild();
            
            if (child != null && child.getColumns().size() != 0) {
				DropDownGridBox dd = getDropDown(gb);
				if (dd != null) dd.getView().setValue(dd.getText());
            }
            
    		gb.firePropertyChange(this, PROPERTY_ROW_SELECTED, oldSelected, selected);
            //#IFDEF V1_1_COMPAT
            if (gb.compatModeOn) gb.firePropertyChange(this, "selected", oldSelected, selected);
            //#ENDIF
        }
        
        /**
         * Get this Row's child GridBox.
         * @return this Row's child GridBox.
         */
        public GridBox getChild() {
            return child;
        }
        
        /**
         * Set this Row's child GridBox.
         * @param child the child GridBox
         */
        public void setChild(GridBox child) {
            GridBox gb = (GridBox)getParent();
            if (gb != null && gb.isVisibleCheckBoxes()) throw new IllegalStateException("getParent().isVisibleCheckBoxes() == true");
            GridBox oldChild = this.child;
            this.child = child;
            if (child != null) child.setParent(this);            
    		
            if (gb != null) {
                if (oldChild != null) gb.rowsWithChildren.remove(this);
                
                if (child != null) {
                    gb.rowsWithChildren.add(this);
                    DropDown.copyDropDownStyle(gb, child, gb.getParent() instanceof DropDown);
                }
                
                gb.firePropertyChange(this, PROPERTY_ROW_CHILD, oldChild, child);                
            }
        }
    }
    
    public static final class Column extends ArrayGrid.Column {
        public static final String PROPERTY_COLUMN_NAME = "columnName";
        public static final String PROPERTY_COLUMN_ALIGN_X = "columnAlignX";
        public static final String PROPERTY_COLUMN_WIDTH = "columnWidth";
        public static final String PROPERTY_COLUMN_VISIBLE = "columnVisible";
        public static final String PROPERTY_COLUMN_DISPLAY_NAME = "columnDisplayName";
        public static final String PROPERTY_COLUMN_DISPLAY_FORMAT = "columnDisplayFormat";
        public static final String PROPERTY_COLUMN_SORT_COMPARATOR = "columnSortComparator";
        public static final String PROPERTY_COLUMN_SORT_ORDER = "columnSortOrder";

        private static final Comparator<Object> DEFAULT_SORT = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
            }
        };        

        public static enum SortOrder {NONE, ASC, DESC};
        
        public interface Format {
            public Object format(Object value);
        }    
        
        //Visible used to default to false, which creates confusion when using this component.
        private boolean visible = true;
        private int width = -1;
        private AlignX alignX = AlignX.LEFT;
        private Format displayFormat = null;
        private Comparator sortComparator = DEFAULT_SORT;

        /**
         * Construct a Column.
         */
        public Column() {
            
            //#IFDEF V1_1_COMPAT
            if (isCompatModeOn()) visible = false;
            //#ENDIF
        }

        /**
         * Construct a Column, specifying a Collection whose values will serve as the Column's values.
         * @param c the new Column's values
         */
        public Column(Collection< ? extends Object> c) {
            this();
            addAll(c);
        }

        /**
         * Construct a Column that contains the values of the specified Array.
         * @param a the new Column's values
         */
        public Column(Object... a) {
            this();

            for (Object o : a)
                add(o);
        }

        /**
         * Gets whether this column is visible.
         * @return true if the column is visible.
         */
        public boolean isVisible() {
            return visible;
        }

        /**
         * Sets whether this column is visible.
         * @param visible (Default = false)
         */
        public void setVisible(boolean visible) {
            boolean oldVisible = this.visible;
            GridBox gb = (GridBox) getParent();
            this.visible = visible;
            if (gb != null) gb.firePropertyChange(this, PROPERTY_COLUMN_VISIBLE, oldVisible, visible);
        }

        /**
         * Sets the name of the column.
         */
        public void setName(String name) {
            String oldName = getName();
            name = name == null ? "" : name;
            GridBox gb = (GridBox) getParent();
            super.setName(name);
            if (gb != null) gb.firePropertyChange(this, PROPERTY_COLUMN_NAME, oldName, name);
        }

        /**
         * Sets the display name of the column.
         */
        public void setDisplayName(String displayName) {
            String oldDisplayName = getDisplayName();
            displayName = displayName == null ? "" : displayName;
            GridBox gb = (GridBox) getParent();
            super.setDisplayName(displayName);
            if (gb != null) gb.firePropertyChange(this, PROPERTY_COLUMN_DISPLAY_NAME, oldDisplayName, displayName);
        }

        public int getWidth() {
            return width;
        }

        /**
         * Sets the width of the column.
         * @param width the width of the column
         * @throws IllegalArgumentException if width < -1 or > 65534
         */
        public void setWidth(int width) {
            if (width < -1 || width >= 65535) throw new IllegalArgumentException("width < -1 || width >= 65535");
            int oldWidth = this.width;
            GridBox gb = (GridBox) getParent();
            this.width = width;
            if (gb != null) gb.firePropertyChange(this, PROPERTY_COLUMN_WIDTH, oldWidth, width);
        }

        public AlignX getAlignX() {
            return alignX;
        }

        /**
         * Sets the text justification (left, right, or center)
         * @param alignX (Default = AlignX.LEFT)
         */
        public void setAlignX(AlignX alignX) {
            if (alignX == null) alignX = AlignX.LEFT;
            AlignX oldAlignX = this.alignX;
            GridBox gb = (GridBox) getParent();
            this.alignX = alignX;
            if (gb != null) gb.firePropertyChange(this, PROPERTY_COLUMN_ALIGN_X, oldAlignX, alignX);
        }

        public Format getDisplayFormat() {
            return displayFormat;
        }

        /**
         * Set the display format for this Column.
         * A Column's Displ
         * @param displayFormat the display format to set for the column.
         */
        public void setDisplayFormat(Format displayFormat) {
            Format oldDisplayFormat = this.displayFormat;
            GridBox gb = (GridBox) getParent();
            this.displayFormat = displayFormat;
            if (gb != null) gb.firePropertyChange(this, PROPERTY_COLUMN_DISPLAY_FORMAT, oldDisplayFormat, displayFormat);
        }

        /**
         * Get this Column's Comparator.
         * @return this Column's Comparator
         */
        public Comparator getSortComparator() {
            return sortComparator;
        }

        /**
         * Set the Comparator for this Column.
         * @see java.util.Comparator
         * @param sortComparator this Column's Comparator.
         */
        public void setSortComparator(Comparator sortComparator) {
            if (sortComparator == null) sortComparator = DEFAULT_SORT;
            Comparator oldSortComparator = this.sortComparator;
            GridBox gb = (GridBox) getParent();
            this.sortComparator = sortComparator;            
            if (gb != null) gb.firePropertyChange(this, PROPERTY_COLUMN_SORT_COMPARATOR, oldSortComparator, sortComparator);                
            setSortOrder(null);
        }
        
        public SortOrder getSortOrder() {
            GridBox gb = (GridBox)getParent();
            return gb == null || gb.sortedColumn != this ? SortOrder.NONE : gb.sortedColumnOrder;
        }
        
        public void setSortOrder(SortOrder sortOrder) {
            GridBox gb = (GridBox)getParent();
            if (gb == null) throw new IllegalStateException("the column must be added to a GridBox before you can sort by it");            
            if (sortOrder == null) sortOrder = SortOrder.NONE;
            GridBox.Row selectedRow = gb.getSelectedRow();
            
            GridBox.Column oldSortedColumn = gb.sortedColumn;
            SortOrder oldSortedColumnOrder = gb.sortedColumnOrder;                      
            gb.sortedColumn = oldSortedColumn == this && sortOrder == SortOrder.NONE ? null : this;
            gb.sortedColumnOrder = sortOrder;
            gb.sort();
            
            if (oldSortedColumn != null && oldSortedColumn != this) gb.firePropertyChange(oldSortedColumn, PROPERTY_COLUMN_SORT_ORDER, oldSortedColumnOrder, SortOrder.NONE);
            gb.firePropertyChange(this, PROPERTY_COLUMN_SORT_ORDER, oldSortedColumn == this ? oldSortedColumnOrder : SortOrder.NONE, sortOrder);
            if (selectedRow != null) gb.selectedRowIndex = selectedRow.getIndex();
        }
    }
    
    private static DropDownGridBox getDropDown(GridBox gb) {
        DropDownGridBox dd = null;        
        Object o = gb.getParent();
        
        while (o != null) {
            if (o instanceof Container) {
                break;
            } else if (o instanceof GridBox.Row) {
                o = ((GridBox.Row)o).getParent();
            } else if (o instanceof DropDownGridBox) {
                dd = (DropDownGridBox)o;
                break;
            } else {
                o = ((Component)o).getParent();
            }
        }
        
        return dd;
    }    
    
    public static final String PROPERTY_VISIBLE_HEADER = "visibleHeader";
    public static final String PROPERTY_VISIBLE_CHECK_BOXES = "visibleCheckBoxes";
    public static final String PROPERTY_FULL_ROW_CHECK_BOX = "fullRowCheckBox";
    
    private boolean visibleHeader;
    private boolean visibleCheckBoxes;
    private boolean fullRowCheckBox;
    //#IFDEF V1_1_COMPAT
    private boolean compatModeOn;
    //#ENDIF
    private int selectedRowIndex = -1;
    private Column sortedColumn;
    private Column.SortOrder sortedColumnOrder = GridBox.Column.SortOrder.NONE; 
    
    private EventListenerImpl<ItemChangeListener> icei = new EventListenerImpl<ItemChangeListener>();
    private EventListenerImpl<ActionListener> aei = new EventListenerImpl<ActionListener>();
    private ArrayGrid<Row, Column> grid;
    private SortedSet<Row> checkedRows;
    private SortedSet<Row> roCheckedRows;
    private SortedSet<Row> rowsWithChildren;
    private SortedSet<Row> roRowsWithChildren;
            
	/**
	 * Constructs a new GridBox.
	 */
	public GridBox() {
		this.grid = new ArrayGrid<GridBox.Row, GridBox.Column>(this, GridBox.Row.class, GridBox.Column.class) {		    
		    protected void fireItemChange(Type type, int rowIndex, int columnIndex, Object oldValue, Object newValue) {
                if (rowIndex >= 0 && columnIndex == -1) {
                    List<GridBox.Row> rows = GridBox.this.getRows();
                    int size = rows.size();
                    
                    //If the selected row is removed or if the first row is added, then
                    //we need to guarantee the selected row is correct.
                    if (type == ItemChangeEvent.Type.REMOVE) {
                        GridBox.Row oldRow = (GridBox.Row)oldValue; 
                        if (oldRow.getChild() != null) GridBox.this.rowsWithChildren.remove(oldRow);
                        if (oldRow.isChecked()) GridBox.this.checkedRows.remove(oldRow);

                        if (rowIndex == GridBox.this.selectedRowIndex) {
                            if (rowIndex < size) {
                                rows.get(rowIndex).setSelected(true);
                            } else if (size > 0) {
                                rows.get(size - 1).setSelected(true);
                            } else {
                                GridBox.this.selectedRowIndex = -1;                              
                            }
                        } else if (rowIndex <= selectedRowIndex) {
                            if (GridBox.this.selectedRowIndex - 1 >= 0) GridBox.this.selectedRowIndex--;
                        }
                    } else if (type == ItemChangeEvent.Type.ADD) {
                        GridBox.Row newRow = (GridBox.Row)newValue;
                        if (newRow.getChild() != null) {
                            GridBox.this.rowsWithChildren.add(newRow);
                            DropDown.copyDropDownStyle(GridBox.this, newRow.getChild(), getParent() instanceof DropDown);
                        }
                        
                        if (newRow.isChecked()) GridBox.this.checkedRows.add(newRow);
                        
                        if (size == 1) {
                            if (GridBox.this.getColumns().size() > 0) {
                                newRow.setSelected(true);
                            } else {
                                //TODO: We should allow this state to occur
                                GridBox.this.selectedRowIndex = 0;
                            }
                        } else if (rowIndex <= GridBox.this.selectedRowIndex) {
                            if (GridBox.this.selectedRowIndex + 1 < size) GridBox.this.selectedRowIndex++;
                        }
                        
                        if (GridBox.this.sortedColumn != null) GridBox.this.sortedColumn.setSortOrder(GridBox.Column.SortOrder.NONE);
                    }
                }
                
                icei.fireItemChange(this, type, rowIndex, columnIndex, oldValue, newValue);
			}
		};
		
        Comparator<Row> indexOrder = new Comparator<Row>() {
            public int compare(Row r1, Row r2) {
                int index1 = r1.getIndex();
                int index2 = r2.getIndex();
                
                if (index1 < index2) {
                    return -1;
                } else if (index1 == index2){
                    return 0;
                } else {
                    return 1;
                }
            }            
        };
        
        addPropertyChangeListener(DropDown.STYLE_PROPERTIES, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                String propertyName = ev.getPropertyName();
                Object o = ev.getNewValue();

                for (GridBox.Row row : getRowsWithChildren()) {
                    Style s = row.getChild().getStyle();
                    DropDown.setStyleValue(s, propertyName, o);                    
                }
            }
        });
        
        checkedRows = new TreeSet<Row>(indexOrder);
        roCheckedRows = Collections.unmodifiableSortedSet(checkedRows);
        rowsWithChildren = new TreeSet<Row>(indexOrder);
        roRowsWithChildren = Collections.unmodifiableSortedSet(rowsWithChildren);        
        //#IFDEF V1_1_COMPAT
        compatModeOn = isCompatModeOn();
        //#ENDIF
	}
    
    void setRenderer(Renderer r) {
        super.setRenderer(r);
        aei.setRenderer(r);
    }
    
    private void sort() {
        if (sortedColumnOrder == GridBox.Column.SortOrder.NONE || sortedColumn == null) return;
        final int index = sortedColumn.getIndex();
        final Comparator<Object> sortComparator = sortedColumn.getSortComparator();
        
        if (sortedColumnOrder == GridBox.Column.SortOrder.DESC) {               
            Collections.sort(getRows(), new Comparator<Row>() {
                public int compare(Row o1, Row o2) {
                    return ~sortComparator.compare(o1.get(index), o2.get(index)) + 1;
                }
            });                                
        } else {                
            Collections.sort(getRows(), new Comparator<Row>() {
                public int compare(Row o1, Row o2) {
                    return sortComparator.compare(o1.get(index), o2.get(index));
                }
            });                                
        }
    }       
        
	public void addItemChangeListener(ItemChangeListener listener) {
        icei.addListener(listener);
	}
	
	public void removeItemChangeListener(ItemChangeListener listener) {
        icei.removeListener(listener);
	}	
    //#IFDEF V1_1_COMPAT
	
    /**
     * Adds a listener which will perform a certain action when a specific action occurs
     * (ex: click).
     * @param listener the listener to add
     * @deprecated for performance reasons, this form as been deprecated.  Use the named action form instead. 
     */
    public void addActionListener(ActionListener listener) {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use addActionListener(action, listener) instead.");        
        aei.addListener(ACTION_CLICK, listener);
    }
    //#ENDIF

    public void addActionListener(String action, ActionListener listener) {
        aei.addListener(action, listener);
    }
    
    public void addActionListener(String[] actions, ActionListener listener) {
        aei.addListener(actions, listener);
    }    
    
    public void removeActionListener(ActionListener listener) {
        aei.removeListener(listener);
    }
    
    /**
     * Programmatically fires an action which triggers the appropriate listener.
     * @param action the action to fire.  ACTION_CLICK is the only permissible value.
     * @param row the modified Row
     * @throws IllegalArgumentException if action is null or not equal to ACTION_CLICK.
     */
    public void fireAction(String action, Row row) {
        if (action == null || !action.equals(ACTION_CLICK)) throw new IllegalArgumentException("the specified action is not supported");        
        if (row == null) throw new IllegalArgumentException("row == null");
        GridBox gb = (GridBox)row.getParent();
        row.setSelected(true);

        if (row.getChild() == null) {
        	DropDownGridBox dd = getDropDown(gb);
        	if (dd != null) dd.setText(dd.getView().getValue().toString());
        }

        row.setSelected(true);        
        aei.fireAction(row, action);
    }
	
	public List<GridBox.Column> getColumns() {
		return grid.getColumns();
	}
	
	public List<GridBox.Row> getRows() {
		return grid.getRows();
	}
	
	/**
   * Returns a boolean indicating if this GridBox's column
   * headers are visible.
	 * @return true if this GridBox's column headers are
   *    visible.
	 */
	public boolean isVisibleHeader() {
	    return visibleHeader;
    }
	
	/**
	 * Sets whether the column headers are visible.
	 * @param visibleHeader Default: false
	 */
	public void setVisibleHeader(boolean visibleHeader) {
	    boolean oldVisibleHeader = this.visibleHeader;
		this.visibleHeader = visibleHeader; 
		firePropertyChange(this, PROPERTY_VISIBLE_HEADER, oldVisibleHeader, visibleHeader);
	}
	
	/**
   * Returns a boolean indicating whether this GridBox's
   * rows have visible CheckBoxes.
	 * @return true if there are visible CheckBoxes on the rows.
	 */
	public boolean isVisibleCheckBoxes() {
	    return visibleCheckBoxes;
    }
	
	/**
	 * Sets whether there are checkBoxes on each row.
	 * @param visibleCheckBoxes Default: false
	 */
	public void setVisibleCheckBoxes(boolean visibleCheckBoxes) {
        if (visibleCheckBoxes && !this.visibleCheckBoxes && rowsWithChildren.size() > 0) throw new IllegalStateException("getRowsWithChildren().size() > 0 [" + rowsWithChildren.size() + "]");       
	    boolean oldVisibleCheckBoxes = this.visibleCheckBoxes;                
		this.visibleCheckBoxes = visibleCheckBoxes;
		firePropertyChange(this, PROPERTY_VISIBLE_CHECK_BOXES, oldVisibleCheckBoxes, visibleCheckBoxes);
	}

	/**
   * Get a boolean indicating whether clicking anywhere on a row
   * in this GridBox will check the row's CheckBox.
	 * @return true if clicking anywhere on this GridBox's rows
   *    will check the row's CheckBox.
	 */
	public boolean isFullRowCheckBox() {
        return fullRowCheckBox;
	}

	/**
	 * When check boxes are turned on, if this flag is true, clicking anywhere on the row will
	 * 	check the box.
	 * @param fullRowCheckBox boolean which turns on CheckBox checking.
	 */
	public void setFullRowCheckBox(boolean fullRowCheckBox) {
	    boolean oldFullRowCheckBox = this.fullRowCheckBox;
	    this.fullRowCheckBox = fullRowCheckBox;
		firePropertyChange(this, PROPERTY_FULL_ROW_CHECK_BOX, oldFullRowCheckBox, fullRowCheckBox);
	}
	
	/**
	 * Returns the row that is currently selected.
	 * @return the selected Row.
	 */
	public Row getSelectedRow() {
		if (selectedRowIndex < 0) return null;
	    return selectedRowIndex < getRows().size() ? (Row)grid.getRows().get(selectedRowIndex) : null;
	}

	/**
	 * Returns any rows that are checked.
	 * @return an unmodifiableSortedSet of checked rows in index top-down order.
	 */
	public SortedSet<Row> getCheckedRows() {
	    return roCheckedRows;
    }
    
    /**
     * Returns any rows that have children.
     * @return an unmodifiableSortedSet of rows with children in index top-down order.
     */
    public SortedSet<Row> getRowsWithChildren() {
        return roRowsWithChildren;
    }
           
    @Override
    public boolean isFocusCapable() {
        Object parent = getParent();
        if (parent != null && !(parent instanceof Container)) return false;
        return super.isFocusCapable();
    }
    
    @Override
    public void setFocusCapable(boolean focusCapable) {
        Object parent = getParent();
        if (parent != null && !(parent instanceof Container)) throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_FOCUS_CAPABLE, false));
        super.setFocusCapable(focusCapable);
    }
    
    @Override
    public int getX() {
        Object parent = getParent();
        if (parent != null && !(parent instanceof Container)) throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_X, true));
        return super.getX();
    }
    
    @Override
    public void setX(int x) {
        Object parent = getParent();
        if (parent != null && !(parent instanceof Container)) throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_X, false));
        super.setX(x);
    }

    @Override
    public int getY() {
        Object parent = getParent();
        if (parent != null && !(parent instanceof Container)) throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_Y, true));
        return super.getY();
    }

    @Override
    public void setY(int y) {
        Object parent = getParent();
        if (parent != null && !(parent instanceof Container)) throw new UnsupportedOperationException(getStandardPropertyUnsupportedMsg(PROPERTY_Y, false));
        super.setY(y);
    }
    //#IFDEF V1_1_COMPAT
    
    DropDown.View<GridBox> view;     

    /**
     * @deprecated
     */
    public DropDown.View<GridBox> getView() {
        if (!isCompatModeOn()) throw new IllegalStateException("this method is deprecated as of v1.2 and cannot be called unless compat mode is on, use DropDown.getView() instead; GridBox's no longer hold a reference to a view.");

        if (getParent() instanceof DropDownGridBox) {
            view = ((DropDownGridBox)getParent()).getView();
        } else if (view == null) {
            DefaultView v = new DefaultView();
            v.init(getDropDown(this), this);
            view = v;
        }
        
        return view;
    }
    //#ENDIF
}

