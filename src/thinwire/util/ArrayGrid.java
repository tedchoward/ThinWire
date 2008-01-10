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
package thinwire.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.AbstractList;

import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.util.Grid.Column;
import thinwire.util.Grid.Row;

/**
 * ArrayGrid is an implementation of the Grid interface which provides a disconnected dataset.<p>
 * The samples below populate an ArrayGrid with values from an
 * array and then print out the values in the ArrayGrid.<p>
 * <h3>Notes</h3>
 * <UL>
 * <LI>Don't add the same Column to an ArrayGrid twice.  The following code illustrates the error.
 * <pre>
 * Grid ag = new ArrayGrid();
 * Grid.Column col = new ArrayGrid.Column();
 * ag.getColumns().add(col);
 * ag.getColumns().add(col); //!!! Don't do this.
 * </pre>
 * <LI>Don't add the same Row to an ArrayGrid twice.  
 * <pre>
 *  Grid ag = new ArrayGrid();
 *  Grid.Row row = new ArrayGrid.Row();
 *  ag.getRows().add(row);
 *  ag.getRows().add(row); //!!! Don't do this.
 * </pre>
 * <LI>If you replace a Row in an ArrayGrid, be careful with the replaced Row.
 * If after replacing a Row, you retrieve an element from the replaced Row, the element
 * will still be converted according to the ArrayGrid's policy. 
 * <pre>
 *    Grid ag = new ArrayGrid();
 *    Grid.Row row1 = new ArrayGrid.Row();
 *    ag.getRows().add(row1);
 *    Grid.Row row2 = new ArrayGrid.Row();
 *    Grid.row row3 = ag.getRows().set(0, row2);  //Be careful with row3.
 *    Object rowVal = row3.get(0); //Be careful with rowVal.
 * <pre>
 * In the code above, row1 is replaced in ag with row2, and row3 is identical to
 * row1.  Despite its removal from ag, row1 (aka row3) still has ag as a parent.  If you retrieve
 * an element from row1 (aka row3), the value you retrieve may not be the exact Object placed in
 * row1.  The value you retrieve - e.g. rowVal in the code above - is a value converted from the
 * original via ag's policy.
 * 
 * </UL>
 * <b>Sample Code:</b><br>
 * <pre>
 *   int[][] values = new int[][]{ { 0, 1, 2, 3, 4 }, { 10, 11, 12, 13, 14 },
 *       { 20, 21, 22, 23, 24 }, { 30, 31, 32, 33, 34 }, { 40, 41, 42, 43, 44 } };
 *   
 *   ArrayGrid ag = new ArrayGrid();
 *   
 *   for (int i = 0; i < 5; i++) {
 *     ArrayGrid.Column col = new ArrayGrid.Column();
 *     ag.getColumns().add(col);
 *   }
 *   
 *   for (int i = 0; i < 5; i++) {
 *     ArrayGrid.Row row = new ArrayGrid.Row();
 *     ag.getRows().add(row);
 *   
 *     for (int j = 0; j < 5; j++) {
 *       row.set(j, new Integer(values[i][j]));
 *     }
 *   }
 *   
 *   System.out.println("\r\n");
 *   
 *   for (int i = 0; i < 5; i++) {
 *     ArrayGrid.Row row = (ArrayGrid.Row) ag.getRows().get(i);
 *     String line = "";
 *   
 *     for (int j = 0; j < 5; j++) {
 *       line += row.get(j) + " ";
 *     }
 *     System.out.println(line);
 *   }
 * </pre>
 * @author Joshua J. Gertzen
 */
public class ArrayGrid implements Grid {
    private boolean ensuringSymmetry;
    private List<Grid.Row> rows;
    private List<Grid.Column> columns;

    /**
     * Construct an ArrayGrid.
     */
    public ArrayGrid() {
        this(null);
    }

    /**
     * Constructs an ArrayGrid based on another grid.
     * @param g a grid that should be copied into this grid.
     */
    public ArrayGrid(Grid g) {
        rows = new RowList(this);
        columns = new ColumnList(this);
        if(g != null) columns.addAll(g.getColumns());
    }
    
    /**
     * Construct an ArrayGrid, specifying an optional inner Grid,
     * a Row type, and a Column type.
     * 
     * @param grid the outer <code>Grid</code> implementation from which new rows/columns are created.
     */
    protected ArrayGrid(boolean subClass, Grid parent) {
        rows = new RowList(parent);
    	columns = new ColumnList(parent);
    }
    
    @SuppressWarnings("unchecked")
    public <C extends Grid.Column> List<C> getColumns() {
        return (List<C>)columns;
    }

    @SuppressWarnings("unchecked")
    public <R extends Grid.Row> List<R> getRows() {
        return (List<R>)rows;
    }
    
    @SuppressWarnings("unchecked")
    public Column newColumn() {
    	return new Column();
    }
    
    @SuppressWarnings("unchecked")
    public Row newRow() {
    	return new Row();
    }

    private void ensureSymmetry(Row r, int addAtIndex) {
        if (ensuringSymmetry) return;
        ensuringSymmetry = true;        
        
        if (r != null) {
        	//NOTE: Only one of the two following loops will actually execute, never both
        	//Grow number of columns to match row size
            while (columns.size() < r.size())
                columns.add(newColumn());

            //Grow row as necessary to match number of columns
	        while (r.size() < columns.size())
	            r.add(null);
        }
        
        for (Grid.Row row : rows) {            
            if (row.size() < columns.size()) {
                row.set(columns.size() - 1, null); //force the row to expand
                
                //Shift row values over
                for (int i = columns.size() - 1; addAtIndex < i; i--) row.set(i, row.get(i - 1));
                row.set(addAtIndex, null);
            }            
        }

        ensuringSymmetry = false;
    }
    
    protected void fireItemChange(Type type, int rowIndex, int columnIndex, Object oldValue, Object newValue) {
        
    }
    
    /**
     * Contains an ArrayList of objects, each object associated with a different column.
     * @see AbstractList
     * @see thinwire.util.Grid.Row
     */
    public static class Row extends AbstractList<Object> implements Grid.Row {
        private ArrayGrid grid;
        private Grid parent;       
        private List<Object> l;
        private int rowIndex;
        private Object userObject;
        
        /**
         * Construct a Row.
         */
        public Row() {
            l = new ArrayList<Object>(3);
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
                
        private void setParent(Grid parent, ArrayGrid grid) {
            this.parent = parent;
            this.grid = grid;
            if (parent == null) rowIndex = -1;
        }

        public Grid getParent() {
            return parent;
        }
        
        private int getColumnIndexByName(String columnName) {
            if (parent == null) throw new IllegalStateException("cannot access a row column by name before the row is added to a grid");            
            List<? extends Grid.Column> columns = parent.getColumns();
            
            for (int i = columns.size() - 1; i >= 0; i--) {
                if (columns.get(i).getName().equalsIgnoreCase(columnName)) {
                    return i;
                }
            }
            
            throw new IllegalArgumentException("there is no column with the name '" + columnName + "'");
        }
        
        private void setIndex(int index) {
            if (parent == null) throw new IllegalStateException("index cannot be set before the row is added to a grid");
            if (parent.getRows().get(index) != this) throw new IllegalArgumentException("this row is not at the specified index");
            this.rowIndex = index;
        }
        
        public int getIndex() {
            return rowIndex;
        }
        
        public Object getUserObject() {
        	return userObject;
        }

        public void setUserObject(Object value) {
        	this.userObject = value;
        }
        
        public Object get(String columnName) {
            return get(getColumnIndexByName(columnName));
        }
        
        public Object get(int index) {
            return l.get(index);
        }
        
        public Object set(int index, Object o) {            
            if (parent != null) {
                while (l.size() < parent.getColumns().size())
                    l.add(null);                
            }

            Object ret = l.set(index, o);            
            if (grid != null && !grid.ensuringSymmetry) grid.fireItemChange(Type.SET, rowIndex, index, ret, o);                    
            return ret;
        }
        
        public Object set(String columnName, Object o) {
            return set(getColumnIndexByName(columnName), o);
        }
        
        public void add(int index, Object o) {
            if (parent != null) throw new UnsupportedOperationException("you cannot add an item to a row, you must instead add a column and then set the cell's value");
            l.add(index, o);
            modCount++;
        }
        
        public Object remove(int index) {
            if (parent != null) throw new UnsupportedOperationException("you cannot remove an item from a row, you must instead remove a column or set this cell's value to a new value");
            Object o = l.remove(index);
            modCount++;
            return o;
        }

        public int size() {
            return l.size();
        }
    }

    /**
     * Contains an ArrayList of objects, each object associated with a different row.
     * @see java.util.AbstractList
     * @see thinwire.util.Grid.Column
     */
    public static class Column extends AbstractList<Object> implements Grid.Column {
        private Grid parent;
        private List<Object> l;
        private int columnIndex;
        private String name = "";
        private Object userObject;
               
        /**
         * Construct a Column.
         */
        public Column() {
            columnIndex = -1;
        }
        
        /**
         * Construct a Column, specifying a Collection whose values will serve 
         *   as the Column's values.
         * @param c the new Column's values
         */
        public Column(Collection<? extends Object> c) {
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
        
        public String getName() {
            return name;            
        }
        
        public void setName(String name) {
            this.name = name == null ? "" : name;
        }
        
        /*
         * Sets the Grid to be associated with this column
         * @param parent The Grid interface
         */
        private void setParent(Grid parent) {            
            this.parent = parent;
            this.l = null;            
            if (parent == null) columnIndex = -1;            
            modCount++;
        }
        
        public Grid getParent() {
            return parent;
        }

        /*
         * Sets the index of the column
         * @param columnIndex The value to set the column index to
         */
        private void setIndex(int columnIndex) {
            if (parent == null) throw new IllegalArgumentException("index cannot be set before the table is set");
            if (parent.getColumns().get(columnIndex) != this) throw new IllegalArgumentException("this column is not at the specified index");
            this.columnIndex = columnIndex;
        }
        
        public int getIndex() {
            return columnIndex;
        }
        
        public Object getUserObject() {
        	return userObject;
        }

        public void setUserObject(Object value) {
        	this.userObject = value;
        }
        
        public Object get(int index) {            
            if (parent == null) {
                if (index < 0 || index >= size() || l == null) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                return l.get(index);
            } else {
                if (index < 0 || index >= size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                return parent.getRows().get(index).get(columnIndex);
            }
        }
        
        public Object set(int index, Object o) {            
            if (parent == null) {
                if (index < 0 || index >= size() || l == null) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                return l.set(index, o);
            } else {
                if (index < 0 || index >= size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                Grid.Row row = parent.getRows().get(index);
                Object ret = row.set(columnIndex, o);
                return ret;
            }
        }
                
        public void add(int index, Object o) {
            if (parent != null) throw new UnsupportedOperationException("you cannot add an item to a column, you must instead add a row and then set the cell's value");            
            if (index < 0 || index > size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
            if (l == null) l = new ArrayList<Object>(3);
            l.add(index, o);            
            modCount++;
        }

        public Object remove(int index) {
            if (parent != null) throw new UnsupportedOperationException("you cannot remove an item from a column, you must instead remove a row or set this cell's value to a new value");            
            if (index < 0 || index > size() || l == null) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());            
            Object o = l.remove(index);
            modCount++;
            return o;
        }

        public int size() {
            if (parent == null)
                return l == null ? 0 : l.size();
            else
                return parent.getRows().size();
        }
    }    
        
    private class RowList extends AbstractList<Grid.Row> {
    	private Grid parent;
        private List<Row> l;
        
        private RowList(Grid parent) {
        	this.parent = parent;
            l = new ArrayList<Row>();
        }
        
        private Row prepareRow(List<Object> o) {
        	Row r;
            
            if (o instanceof Row) {
                r = (Row)o;
                Grid t = r.getParent();
                
                if (t != null && t != parent) {
                	Row nr = parent.newRow();
                    nr.addAll(r);
                    r = nr;
                }
            } else {
            	Row nr = parent.newRow();
                nr.addAll(o);
                r = nr;
            }
            
            return r;
        }
        
        public Grid.Row get(int index) {
            return l.get(index);
        }

        /* 
         * Replace the Row at the specified position.<p>
         * Note:  The returned Row is not an ordinary List.  It
         * Any calls to get(int) on the Row will return an
         * Object formatted by the ArrayGrid's policy.  
         */
        public Grid.Row set(int index, Grid.Row o) {
        	Row ret = l.get(index);
        	Row r = prepareRow(o);
            ArrayGrid.this.ensureSymmetry(r, r.size());            
            l.set(index, r);
            //If the parent is set to null during a call to Collections.sort,
            //problems arise.  djv 12/08/2004
            //ret.setParent(null, null);
            r.setParent(parent, ArrayGrid.this);
            r.setIndex(index);
            ArrayGrid.this.fireItemChange(Type.SET, index, -1, ret, r);            
            return ret;
        }
        
        public void add(int index, Grid.Row o) {
        	Row r = prepareRow(o);
        	ArrayGrid.this.ensureSymmetry(r, r.size());
            l.add(index, r);
            
            for (int i = index + 1, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);
            
            r.setParent(parent, ArrayGrid.this);
            r.setIndex(index);
            modCount++;
            ArrayGrid.this.fireItemChange(Type.ADD, index, -1, null, r);
        }
        
        public Grid.Row remove(int index) {
        	Row r = l.get(index);
            l.remove(index);
            r.setParent(null, null);
            
            for (int i = index, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);
            
            modCount++;
            ArrayGrid.this.fireItemChange(Type.REMOVE, index, -1, r, null);            
            return r;
        }
        
        public void clear() {
            List<Row> ol = l;
            l = new ArrayList<Row>();
            modCount++;

            for (int i = 0, cnt = ol.size(); i < cnt; i++) {
            	ArrayGrid.this.fireItemChange(Type.REMOVE, i, -1, ol.get(i), null);            
            }
        }

        public int size() {
            return l.size();
        }
    }    

    private class ColumnList extends AbstractList<Grid.Column> {
    	private Grid parent;
        private List<Column> l;
        private List<Object> values;
        
        private ColumnList(Grid parent) {
        	this.parent = parent;
            l = new ArrayList<Column>();
        }
        
        private Column prepareColumn(List<Object> o) {
        	Column c;
            values = null;
            
            if (o instanceof Column) {
                c = (Column)o;
                Grid t = c.getParent();
                
                if (t != null && t != parent) {
                    values = c;
                    c = parent.newColumn();
                    c.setName(((Column)values).getName());
                } else
                    values = new ArrayList<Object>(c);
            } else {
                values = o;
                c = parent.newColumn();
            }
            
            return c;
        }
        
        private void loadValues(int index) {
            if (values != null) {
                for (int i = 0, cnt = values.size(); i < cnt; i++) {
                    if (i >= parent.getRows().size()) parent.getRows().add(parent.newRow());
                    parent.getRows().get(i).set(index, values.get(i));
                }
                
                values = null;
            }            
        }
        
        private void saveValues(Column c, int index) {
            for (int i = 0, cnt = parent.getRows().size(); i < cnt; i++) {
                c.add(parent.getRows().get(i).get(index));
            }
        }
        
        public Grid.Column get(int index) {
            return l.get(index);
        }

        public Grid.Column set(int index, Grid.Column o) {
        	Column ret = l.get(index);
        	Column c = prepareColumn(o);
            l.set(index, c);
            ret.setParent(null);
            saveValues(ret, index);
            c.setParent(parent);
            c.setIndex(index);
            ArrayGrid.this.ensureSymmetry(null, index);            
            loadValues(index);
            ArrayGrid.this.fireItemChange(Type.SET, -1, index, ret, c);
            return ret;
        }
        
        public void add(int index, Grid.Column o) {
        	Column c = prepareColumn(o);
            l.add(index, c);
            c.setParent(parent);
            c.setIndex(index);
            ArrayGrid.this.ensureSymmetry(null, index);

            for (int i = index + 1, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);

            loadValues(index);
            modCount++;
            ArrayGrid.this.fireItemChange(Type.ADD, -1, index, null, c);
        }
        
        public Grid.Column remove(int index) {
        	Column c = l.remove(index);
            c.setParent(null);            
            saveValues(c, index);
            
            for (int i = index, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);

            modCount++;
            ArrayGrid.this.fireItemChange(Type.REMOVE, -1, index, c, null);            
            return c;
        }

        public int size() {
            return l.size();
        }
    }        
}
