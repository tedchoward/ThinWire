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
package thinwire.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.AbstractList;

import thinwire.ui.event.ItemChangeEvent.Type;

/**
 * ArrayGrid is an implementation of the Grid interface which provides a disconnected dataset.<p>
 * The samples below populate an ArrayGrid with values from an
 * array and then prints out the values in the ArrayGrid.<p>
 * <h3>Notes</h3>
 * <UL>
 * <LI>Don't add the same Column to an ArrayGrid twice.  The following code illustrates the error.
 * <pre>
 * var ag = new ArrayGrid();
 * var col = new ArrayGrid.Column();
 * ag.getColumns().add(col);
 * ag.getColumns().add(col); //!!! Don't do this.
 * </pre>
 * <LI>Don't add the same Row to an ArrayGrid twice.  
 * <pre>
 *  var ag = new ArrayGrid();
 *  var row = new ArrayGrid.Row();
 *  ag.getRows().add(row);
 *  ag.getRows().add(row); //!!! Don't do this.
 * </pre>
 * <LI>If you replace a Row in an ArrayGrid, be careful with the replaced Row.
 * If after replacing a Row, you retrieve an element from the replaced Row, the element
 * will still be converted according to the ArrayGrid's policy. 
 * <pre>
 *    var ag = new ArrayGrid();
 *    var row1 = new ArrayGrid.Row();
 *    ag.getRows().add(row1);
 *    var row2 = new ArrayGrid.Row();
 *    var row3 = ag.getRows().set(0, row2);  //Be careful with row3.
 *    var rowVal = row3.get(0); //Be careful with rowVal.
 * <pre>
 * In the code above, row1 is replaced in ag with row2, and row3 is identical to
 * row1.  Despite its removal from ag, row1 (aka row3) still has ag as a parent.  If you retrieve
 * an element from row1 (aka row3), the value you retrieve may not be the exact Object placed in
 * row1.  The value you retrieve - e.g. rowVal in the code above - is a value converted from the
 * original via ag's policy.
 * 
 * </UL>
 * <b>Sample Java Code:</b><br>
 * <pre>
 *   int[][] values = { { 0, 1, 2, 3, 4 }, { 10, 11, 12, 13, 14 },
 *       { 20, 21, 22, 23, 24 }, { 30, 31, 32, 33, 34 }, { 40, 41, 42, 43, 44 } };
 *   
 *   ArrayGrid ag = new ArrayGrid();
 *   for (int i = 0; i < 5; i++) {
 *     ArrayGrid.Column col = new ArrayGrid.Column();
 *     ag.getColumns().add(col);
 *   }
 *   
 *   for (int i = 0; i < 5; i++) {
 *     ArrayGrid.Row row = new ArrayGrid.Row();
 *     ag.getRows().add(row);
 *     for (int j = 0; j < 5; j++) {
 *       row.set(j, new Integer(values[i][j]));
 *     }
 *   }
 *   
 *   System.out.println("\r\n");
 *   for (int i = 0; i < 5; i++) {
 *     ArrayGrid.Row row = (ArrayGrid.Row) ag.getRows().get(i);
 *     String line = "";
 *     for (int j = 0; j < 5; j++) {
 *       line += row.get(j) + " ";
 *     }
 *     System.out.println(line);
 *   }
 * </pre>
 * @author Joshua J. Gertzen
 */
public class ArrayGrid<R extends ArrayGrid.Row, C extends ArrayGrid.Column> implements Grid<R, C> {
    private Class<? extends Row> rowType;
    private Class<? extends Column> columnType;
    private List<R> rows;
    private List<C> columns;
    private boolean ensuringSymmetry;
    private Grid<R, C> table;

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
    public ArrayGrid(Grid<R, C> g) {
        this(null, Row.class, Column.class);   
        if(g != null)
            columns.addAll(g.getColumns());
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
    protected ArrayGrid(Grid<R, C> grid, Class<? extends Row> rowType, Class<? extends Column> columnType) {
        if (!(Row.class.isAssignableFrom(rowType) && Column.class.isAssignableFrom(columnType)))
            throw new ClassCastException("the rowType must be a subclass of Grid.Row and the columnType must be a subclass of Grid.Column");
        
        this.rowType = rowType;
        this.columnType = columnType;
        
        if (grid == null)
            this.table = this;
        else
            this.table = grid;
        
        rows = new RowList();
        columns = new ColumnList();
    }
    
	/**
	 * @see thinwire.util.Grid#getColumns
	 */
    public List<C> getColumns() {
        return columns;
    }

    /**
     * @see thinwire.util.Grid#getRows
     */
    public List<R> getRows() {
        return rows;
    }
    
    private C newColumnInstance() {
        try {
            return (C)columnType.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }        
    }
    
    private R newRowInstance() {
        try {
            return (R)rowType.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }        
    }

    private void ensureSymmetry(Row r, int addAtIndex) {
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
        private ArrayGrid<? extends Row, ? extends Column> arrayGrid;
        private Grid<? extends Grid.Row, ? extends Grid.Column> parent;       
        private List<Object> l;
        private int rowIndex;
        
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
                
        private void setParent(Grid<? extends Grid.Row, ? extends Grid.Column> parent, ArrayGrid<? extends Row, ? extends Column> arrayGrid) {
            this.parent = parent;
            this.arrayGrid = arrayGrid;
            if (parent == null) rowIndex = -1;
        }
        
        /**
         * Gets the grid that contains this row.
         * @see thinwire.util.Grid.Row#getParent
         */
        public Grid getParent() {
            return parent;
        }
        
        private int getColumnIndexByName(String columnName) {
            if (parent == null) throw new IllegalStateException("cannot access a row column by name before the row is added to a grid");            
            List<? extends Grid.Column> columns = parent.getColumns();
            
            for (int i = columns.size() - 1; i >= 0; i--) {
                if (((Column)columns.get(i)).getName().equalsIgnoreCase(columnName)) {
                    return i;
                }
            }
            
            throw new IllegalArgumentException("there is no column with the name '" + columnName + "'");
        }
        
        /*
         * Sets the index of the current row.
         * @param index The value to set the index to
         */
        private void setIndex(int index) {
            if (parent == null) throw new IllegalStateException("index cannot be set before the row is added to a grid");
            if (parent.getRows().get(index) != this) throw new IllegalArgumentException("this row is not at the specified index");
            this.rowIndex = index;
        }
        
        /**
         * Gets the index for this row.
         * @see thinwire.util.Grid.Row#getIndex()
         */
        public int getIndex() {
            return rowIndex;
        }
        
        /**
         * @see thinwire.util.Grid.Row#get(java.lang.String)
         */
        public Object get(String columnName) {
            return get(getColumnIndexByName(columnName));
        }
        
        /**
         * @see java.util.List#get(int)
         */
        public Object get(int index) {
            return l.get(index);
        }
        
        /**
         * @see java.util.List#set(int, java.lang.Object)
         */
        public Object set(int index, Object o) {            
            if (parent != null) {
                while (l.size() < parent.getColumns().size())
                    l.add(null);                
            }

            Object ret = l.set(index, o);            
            if (arrayGrid != null && !arrayGrid.ensuringSymmetry) arrayGrid.fireItemChange(Type.SET, rowIndex, index, ret, o);                    
            return ret;
        }
        
        public Object set(String columnName, Object o) {
            return set(getColumnIndexByName(columnName), o);
        }
        
        /**
         * @see java.util.List#add(int, java.lang.Object)
         */
        public void add(int index, Object o) {
            if (parent != null) throw new UnsupportedOperationException("you cannot add an item to a row, you must instead add a column and then set the cell's value");
            l.add(index, o);
            modCount++;
        }
        
        /**
         * @see java.util.List#remove(int)
         */
        public Object remove(int index) {
            if (parent != null) throw new UnsupportedOperationException("you cannot remove an item from a row, you must instead remove a column or set this cell's value to a new value");
            Object o = l.remove(index);
            modCount++;
            return o;
        }

        /**
         * @see java.util.Collection#size()
         */
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
        private Grid<? extends Grid.Row, ? extends Grid.Column> parent;
        private List<Object> l;
        private int columnIndex;
        private String name = "";
               
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
        
        /**
         * @see thinwire.util.Grid.Column#getName()
         */
        public String getName() {
            return name;            
        }
        
        /**
         * @see thinwire.util.Grid.Column#setName(java.lang.String)
         */
        public void setName(String name) {
            this.name = name == null ? "" : name;
        }
        
        /*
         * Sets the Grid to be associated with this column
         * @param parent The Grid interface
         * @param arrayGrid the ArrayGrid object
         */
        private void setParent(Grid<? extends Grid.Row, ? extends Grid.Column> parent, ArrayGrid<? extends Row, ? extends Column> arrayGrid) {            
            this.parent = parent;
            this.l = null;            
            if (parent == null) columnIndex = -1;            
            modCount++;
        }
        
        /**
         * Gets the grid that contains this column.
         * @see thinwire.util.Grid.Column#getParent()
         */
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
        
        /**
         * @see thinwire.util.Grid.Column#getIndex()
         */
        public int getIndex() {
            return columnIndex;
        }        
        
        /**
         * @see java.util.List#get(int)
         */
        public Object get(int index) {            
            if (parent == null) {
                if (index < 0 || index >= size() || l == null) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                return l.get(index);
            } else {
                if (index < 0 || index >= size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                return ((Row)parent.getRows().get(index)).get(columnIndex);
            }
        }
        
        /**
         * @see java.util.List#set(int, java.lang.Object)
         */
        public Object set(int index, Object o) {            
            if (parent == null) {
                if (index < 0 || index >= size() || l == null) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                return l.set(index, o);
            } else {
                if (index < 0 || index >= size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
                Row row = (Row)parent.getRows().get(index);
                Object ret = row.set(columnIndex, o);
                return ret;
            }
        }
                
        /**
         * @see java.util.List#add(int, java.lang.Object)
         */
        public void add(int index, Object o) {
            if (parent != null) throw new UnsupportedOperationException("you cannot add an item to a column, you must instead add a row and then set the cell's value");            
            if (index < 0 || index > size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
            if (l == null) l = new ArrayList<Object>(3);
            l.add(index, o);            
            modCount++;
        }
        
        /**
         * @see java.util.List#remove(int)
         */
        public Object remove(int index) {
            if (parent != null) throw new UnsupportedOperationException("you cannot remove an item from a column, you must instead remove a row or set this cell's value to a new value");            
            if (index < 0 || index > size() || l == null) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());            
            Object o = l.remove(index);
            modCount++;
            return o;
        }

        /**
         * @see java.util.Collection#size()
         */
        public int size() {
            if (parent == null)
                return l == null ? 0 : l.size();
            else
                return parent.getRows().size();
        }
    }    
        
    private class RowList extends AbstractList<R> {
        private List<R> l;
        
        private RowList() {
            l = new ArrayList<R>();
        }
        
        private R prepareRow(List<Object> o) {
            R r;
            
            if (o instanceof ArrayGrid.Row) {
                r = (R)o;
                Grid t = r.getParent();
                
                if (t != null && t != table) {
                    R nr = newRowInstance();
                    nr.addAll(r);
                    r = nr;
                }
            } else {
                R nr = newRowInstance();
                nr.addAll(o);
                r = nr;
            }
            
            return r;
        }
        
        /*
         * @see java.util.List#get(int)
         */
        public R get(int index) {
            return l.get(index);
        }

        /* 
         * Replace the Row at the specified position.<p>
         * Note:  The returned Row is not an ordinary List.  It
         * Any calls to get(int) on the Row will return an
         * Object formatted by the ArrayGrid's policy.  
         * 
         * @see java.util.List#set(int, java.lang.Object)
         */
        public R set(int index, R o) {
            if (!(o instanceof List)) throw new ClassCastException("unsupported type");
            R ret = l.get(index);
            R r = prepareRow(o);
            ensureSymmetry(r, r.size());            
            l.set(index, r);
            //If the parent is set to null during a call to Collections.sort,
            //problems arise.  djv 12/08/2004
            //ret.setParent(null, null);
            r.setParent(table, ArrayGrid.this);
            r.setIndex(index);
            fireItemChange(Type.SET, index, -1, ret, r);            
            return ret;
        }
        
        /*
         * @see java.util.List#add(int, java.lang.Object)
         */
        public void add(int index, R o) {
            if (!(o instanceof List)) throw new ClassCastException("unsupported type");
            R r = prepareRow(o);
            ensureSymmetry(r, r.size());
            l.add(index, r);
            
            for (int i = index + 1, cnt = l.size(); i < cnt; i++)
                ((Row)l.get(i)).setIndex(i);
            
            r.setParent(table, ArrayGrid.this);
            r.setIndex(index);
            modCount++;
            fireItemChange(Type.ADD, index, -1, null, r);
        }
        
        /*
         * @see java.util.List#remove(int)
         */
        public R remove(int index) {
            R r = l.get(index);
            l.remove(index);
            r.setParent(null, null);
            
            for (int i = index, cnt = l.size(); i < cnt; i++)
                ((Row)l.get(i)).setIndex(i);
            
            modCount++;
            fireItemChange(Type.REMOVE, index, -1, r, null);            
            return r;
        }
        
        public void clear() {
            List<R> ol = l;
            l = new ArrayList<R>();
            modCount++;

            for (int i = 0, cnt = ol.size(); i < cnt; i++) {
                fireItemChange(Type.REMOVE, i, -1, ol.get(i), null);            
            }
        }

        /*
         * @see java.util.Collection#size()
         */
        public int size() {
            return l.size();
        }
    }    

    private class ColumnList extends AbstractList<C> {
        private List<C> l;
        private List<Object> values;
        
        private ColumnList() {
            l = new ArrayList<C>();
        }
        
        private C prepareColumn(List<Object> o) {
            C c;
            values = null;
            
            if (o instanceof ArrayGrid.Column) {
                c = (C)o;
                Grid t = c.getParent();
                
                if (t != null && t != table) {
                    values = c;
                    c = newColumnInstance();
                    c.setName(((Column)values).getName());
                } else
                    values = new ArrayList<Object>(c);
            } else {
                values = o;
                c = (C)newColumnInstance();
            }
            
            return c;
        }
        
        private void loadValues(int index) {
            if (values != null) {
                for (int i = 0, cnt = values.size(); i < cnt; i++) {
                    if (i >= rows.size()) rows.add(newRowInstance());
                    ((Row)rows.get(i)).set(index, values.get(i));
                }
                
                values = null;
            }            
        }
        
        private void saveValues(C c, int index) {
            for (int i = 0, cnt = rows.size(); i < cnt; i++) {
                c.add(((Row)rows.get(i)).get(index));
            }
        }
        
        /*
         * @see java.util.List#get(int)
         */
        public C get(int index) {
            return l.get(index);
        }

        /* 
         * @see java.util.List#set(int, java.lang.Object)
         */
        public C set(int index, C o) {
            if (!(o instanceof List)) throw new ClassCastException("unsupported type");            
            C ret = l.get(index);
            C c = prepareColumn(o);
            l.set(index, c);
            ret.setParent(null, null);
            saveValues(ret, index);
            c.setParent(table, ArrayGrid.this);
            c.setIndex(index);
            ensureSymmetry(null, index);            
            loadValues(index);
            fireItemChange(Type.SET, -1, index, ret, c);
            return ret;
        }
        
        /*
         * @see java.util.List#add(int, java.lang.Object)
         */
        public void add(int index, C o) {
            if (!(o instanceof List)) throw new ClassCastException("unsupported type");
            C c = prepareColumn(o);
            l.add(index, c);
            c.setParent(table, ArrayGrid.this);
            c.setIndex(index);
            ensureSymmetry(null, index);

            for (int i = index + 1, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);

            loadValues(index);
            modCount++;
            fireItemChange(Type.ADD, -1, index, null, c);
        }
        
        /*
         * @see java.util.List#remove(int)
         */
        public C remove(int index) {
            C c = l.remove(index);
            c.setParent(null, null);            
            saveValues(c, index);
            
            for (int i = index, cnt = l.size(); i < cnt; i++)
                l.get(i).setIndex(i);

            modCount++;
            fireItemChange(Type.REMOVE, -1, index, c, null);            
            return c;
        }

        /*
         * @see java.util.Collection#size()
         */
        public int size() {
            return l.size();
        }
    }        
}