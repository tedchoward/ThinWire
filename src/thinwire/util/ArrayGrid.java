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
public class ArrayGrid<T> implements Grid<T> {
    private Class<? extends Row<T>> rowType;
    private Class<? extends Column<T>> columnType;
    private boolean ensuringSymmetry;
    private Grid<T> table;
    private List<Grid.Row<T>> rows;
    private List<Grid.Column<T>> columns;

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
    public ArrayGrid(Grid<T> g) {
        this(null, Row.class, Column.class);
        if(g != null) columns.addAll(g.getColumns());
    }
    
    /**
     * Construct an ArrayGrid, specifying an optional inner Grid,
     * a Row type, and a Column type.
     * 
     * @param grid the inner Grid to which this ArrayGrid provides access.  May be null.
     * @param rowType the class to which this ArrayGrid's Rows belong
     * @param columnType  the class to which this ArrayGrid's Columns belong
     * @throws ClassCastException if rowType does not extend Grid.Row or columnType does
     *   not extend Grid.Column.
     */
    @SuppressWarnings("unchecked")
    protected ArrayGrid(Grid<T> grid, Class<? extends Row> rowType, Class<? extends Column> columnType) {
        this.rowType = (Class<? extends Row<T>>)rowType;
        this.columnType = (Class<? extends Column<T>>)columnType;
        
        if (grid == null)
            this.table = this;
        else
            this.table = grid;
        
        rows = new RowList<T>(this);
        columns = new ColumnList<T>(this);
    }
    
    @SuppressWarnings("unchecked")
    public <C extends Grid.Column<T>> List<C> getColumns() {
        return (List<C>)columns;
    }

    @SuppressWarnings("unchecked")
    public <R extends Grid.Row<T>> List<R> getRows() {
        return (List<R>)rows;
    }
    
    private Column<T> newColumnInstance() {
        try {
            return columnType.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }        
    }
    
    private Row<T> newRowInstance() {
        try {
            return rowType.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }        
    }

    private void ensureSymmetry(Row<T> r, int addAtIndex) {
        if (ensuringSymmetry) return;
        ensuringSymmetry = true;        
        
        if (r != null) {
        	//NOTE: Only one of the two following loops will actually execute, never both
        	//Grow number of columns to match row size
            while (columns.size() < r.size())
                columns.add(newColumnInstance());

            //Grow row as necessary to match number of columns
	        while (r.size() < columns.size())
	            r.add(null);
        }
        
        for (Grid.Row<T> row : rows) {            
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
    public static class Row<T> extends AbstractList<T> implements Grid.Row<T> {
        private ArrayGrid<T> arrayGrid;
        private Grid<T> parent;       
        private List<T> l;
        private int rowIndex;
        private Object userObject;
        
        /**
         * Construct a Row.
         */
        public Row() {
            l = new ArrayList<T>(3);
        }
        
        /**
         * Construct a Row that contains the values of the specified Collection.
         * @param c the new Row's values.
         */
        public Row(Collection<? extends T> c) {
            this();
            addAll(c);
        }
        
        /**
         * Construct a Row that contains the values of the specified Array.
         * @param a the new Row's values.
         */
        public Row(T... a) {
            this();
            
            for (T o : a)
                add(o);
        }
                
        private void setParent(Grid<T> parent, ArrayGrid<T> arrayGrid) {
            this.parent = parent;
            this.arrayGrid = arrayGrid;
            if (parent == null) rowIndex = -1;
        }

        public Grid<T> getParent() {
            return parent;
        }
        
        private int getColumnIndexByName(String columnName) {
            if (parent == null) throw new IllegalStateException("cannot access a row column by name before the row is added to a grid");            
            List<? extends Grid.Column<T>> columns = parent.getColumns();
            
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
        
        public T get(String columnName) {
            return get(getColumnIndexByName(columnName));
        }
        
        public T get(int index) {
            return l.get(index);
        }
        
        public T set(int index, T o) {            
            if (parent != null) {
                while (l.size() < parent.getColumns().size())
                    l.add(null);                
            }

            T ret = l.set(index, o);            
            if (arrayGrid != null && !arrayGrid.ensuringSymmetry) arrayGrid.fireItemChange(Type.SET, rowIndex, index, ret, o);                    
            return ret;
        }
        
        public T set(String columnName, T o) {
            return set(getColumnIndexByName(columnName), o);
        }
        
        public void add(int index, T o) {
            if (parent != null) throw new UnsupportedOperationException("you cannot add an item to a row, you must instead add a column and then set the cell's value");
            l.add(index, o);
            modCount++;
        }
        
        public T remove(int index) {
            if (parent != null) throw new UnsupportedOperationException("you cannot remove an item from a row, you must instead remove a column or set this cell's value to a new value");
            T o = l.remove(index);
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
    public static class Column<T> extends AbstractList<T> implements Grid.Column<T> {
        private Grid<T> parent;
        private List<T> l;
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
        public Column(Collection<? extends T> c) {
            this();
            addAll(c);
        }
        
        /**
         * Construct a Column that contains the values of the specified Array.
         * @param a the new Column's values
         */
        public Column(T... a) {
            this();
            
            for (T o : a)
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
         * @param arrayGrid the ArrayGrid object
         */
        private void setParent(Grid<T> parent, ArrayGrid<T> arrayGrid) {            
            this.parent = parent;
            this.l = null;            
            if (parent == null) columnIndex = -1;            
            modCount++;
        }
        
        public Grid<T> getParent() {
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
        
        public T get(int index) {            
            if (parent == null) {
                if (index < 0 || index >= size() || l == null) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                return l.get(index);
            } else {
                if (index < 0 || index >= size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                return parent.getRows().get(index).get(columnIndex);
            }
        }
        
        public T set(int index, T o) {            
            if (parent == null) {
                if (index < 0 || index >= size() || l == null) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                return l.set(index, o);
            } else {
                if (index < 0 || index >= size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                Row<T> row = (Row<T>)parent.getRows().get(index);
                T ret = row.set(columnIndex, o);
                return ret;
            }
        }
                
        public void add(int index, T o) {
            if (parent != null) throw new UnsupportedOperationException("you cannot add an item to a column, you must instead add a row and then set the cell's value");            
            if (index < 0 || index > size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
            if (l == null) l = new ArrayList<T>(3);
            l.add(index, o);            
            modCount++;
        }

        public T remove(int index) {
            if (parent != null) throw new UnsupportedOperationException("you cannot remove an item from a column, you must instead remove a row or set this cell's value to a new value");            
            if (index < 0 || index > size() || l == null) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());            
            T o = l.remove(index);
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
        
    private static class RowList<T> extends AbstractList<Grid.Row<T>> {
    	private ArrayGrid<T> grid;
        private List<Row<T>> l;
        
        private RowList(ArrayGrid<T> grid) {
        	this.grid = grid;
            l = new ArrayList<Row<T>>();
        }
        
        private Row<T> prepareRow(List<T> o) {
            Row<T> r;
            
            if (o instanceof Row) {
                r = (Row<T>)o;
                Grid<T> t = r.getParent();
                
                if (t != null && t != grid.table) {
                    Row<T> nr = grid.newRowInstance();
                    nr.addAll(r);
                    r = nr;
                }
            } else {
                Row<T> nr = grid.newRowInstance();
                nr.addAll(o);
                r = nr;
            }
            
            return r;
        }
        
        public Grid.Row<T> get(int index) {
            return l.get(index);
        }

        /* 
         * Replace the Row at the specified position.<p>
         * Note:  The returned Row is not an ordinary List.  It
         * Any calls to get(int) on the Row will return an
         * Object formatted by the ArrayGrid's policy.  
         */
        public Grid.Row<T> set(int index, Grid.Row<T> o) {
            Row<T> ret = l.get(index);
            Row<T> r = prepareRow(o);
            grid.ensureSymmetry(r, r.size());            
            l.set(index, r);
            //If the parent is set to null during a call to Collections.sort,
            //problems arise.  djv 12/08/2004
            //ret.setParent(null, null);
            r.setParent(grid.table, grid);
            r.setIndex(index);
            grid.fireItemChange(Type.SET, index, -1, ret, r);            
            return ret;
        }
        
        public void add(int index, Grid.Row<T> o) {
            Row<T> r = prepareRow(o);
            grid.ensureSymmetry(r, r.size());
            l.add(index, r);
            
            for (int i = index + 1, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);
            
            r.setParent(grid.table, grid);
            r.setIndex(index);
            modCount++;
            grid.fireItemChange(Type.ADD, index, -1, null, r);
        }
        
        public Grid.Row<T> remove(int index) {
            Row<T> r = l.get(index);
            l.remove(index);
            r.setParent(null, null);
            
            for (int i = index, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);
            
            modCount++;
            grid.fireItemChange(Type.REMOVE, index, -1, r, null);            
            return r;
        }
        
        public void clear() {
            List<Row<T>> ol = l;
            l = new ArrayList<Row<T>>();
            modCount++;

            for (int i = 0, cnt = ol.size(); i < cnt; i++) {
                grid.fireItemChange(Type.REMOVE, i, -1, ol.get(i), null);            
            }
        }

        public int size() {
            return l.size();
        }
    }    

    private static class ColumnList<T> extends AbstractList<Grid.Column<T>> {
    	private ArrayGrid<T> grid;
        private List<Column<T>> l;
        private List<T> values;
        
        private ColumnList(ArrayGrid<T> grid) {
        	this.grid = grid;
            l = new ArrayList<Column<T>>();
        }
        
        private Column<T> prepareColumn(List<T> o) {
            Column<T> c;
            values = null;
            
            if (o instanceof Column) {
                c = (Column<T>)o;
                Grid<T> t = c.getParent();
                
                if (t != null && t != grid.table) {
                    values = c;
                    c = grid.newColumnInstance();
                    c.setName(((Column<T>)values).getName());
                } else
                    values = new ArrayList<T>(c);
            } else {
                values = o;
                c = grid.newColumnInstance();
            }
            
            return c;
        }
        
        private void loadValues(int index) {
            if (values != null) {
                for (int i = 0, cnt = values.size(); i < cnt; i++) {
                    if (i >= grid.rows.size()) grid.rows.add(grid.newRowInstance());
                    grid.rows.get(i).set(index, values.get(i));
                }
                
                values = null;
            }            
        }
        
        private void saveValues(Column<T> c, int index) {
            for (int i = 0, cnt = grid.rows.size(); i < cnt; i++) {
                c.add(grid.rows.get(i).get(index));
            }
        }
        
        public Grid.Column<T> get(int index) {
            return l.get(index);
        }

        public Grid.Column<T> set(int index, Grid.Column<T> o) {
            Column<T> ret = l.get(index);
            Column<T> c = prepareColumn(o);
            l.set(index, c);
            ret.setParent(null, null);
            saveValues(ret, index);
            c.setParent(grid.table, grid);
            c.setIndex(index);
            grid.ensureSymmetry(null, index);            
            loadValues(index);
            grid.fireItemChange(Type.SET, -1, index, ret, c);
            return ret;
        }
        
        public void add(int index, Grid.Column<T> o) {
            Column<T> c = prepareColumn(o);
            l.add(index, c);
            c.setParent(grid.table, grid);
            c.setIndex(index);
            grid.ensureSymmetry(null, index);

            for (int i = index + 1, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);

            loadValues(index);
            modCount++;
            grid.fireItemChange(Type.ADD, -1, index, null, c);
        }
        
        public Grid.Column<T> remove(int index) {
            Column<T> c = l.remove(index);
            c.setParent(null, null);            
            saveValues(c, index);
            
            for (int i = index, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);

            modCount++;
            grid.fireItemChange(Type.REMOVE, -1, index, c, null);            
            return c;
        }

        public int size() {
            return l.size();
        }
    }        
}
