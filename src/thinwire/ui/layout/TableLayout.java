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
package thinwire.ui.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.util.ArrayGrid;
import thinwire.util.Grid;

/**
 * A layout that manages Components in a table structure that is backed by a Grid.
 * You can use this rapidly and easily layout user interfaces of almost any complexity.
 * Additionaly, since this layout is backed by a Grid, you can dynamically manipulate
 * the layout to accomplish sophisticated user interfaces.
 * <p>
 * The general concepts of this layout are adapted directly from the TableLayout
 * project at http://tablelayout.dev.java.net/.  While, the TableLayout project
 * at java.net is for the Java Swing UI framework, the basic idea behind the
 * layout is applicable to any user interface framework.  In general, you can
 * use a good portion of the documentation from that project to guide you in
 * using this layout.
 * </p>
 * <p>
 * There are two primary differences to keep in mind.  First,
 * ThinWire layout's use 'limits', whereas Swing layouts use 'constraints'.  The
 * concept is the same, but in ThinWire you set a limit by calling the "setLimit()"
 * method on a Component.  Second, since this layout is backed by a ThinWire
 * Grid, row/column additions and removals are done entirely different then
 * how the TableLayout project handles it.
 * </p>
 * <p>
 * With this layout, almost any Grid operation that the GridBox component supports
 * can be performed.  This gives you extensive flexibility in how forms are laid
 * out and manipulated.  Essentially, you can treat a TableLayout that's been
 * set on a Panel as an extremely flexible GridBox.  It takes a little more setup
 * then a GridBox because you have to create instances of the components you want
 * to display in each cell.  Additionally, the overall performance will be lower
 * if you have 100's of rows of information.  However, in exchange for those issues
 * you can use this layout to pull off complex data grids that approach the capability
 * of a modern spreadsheet.  
 * </p> 
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public final class TableLayout extends AbstractLayout implements Grid<TableLayout.Row, TableLayout.Column> {
    private static final Logger log = Logger.getLogger(TableLayout.class.getName());

    public enum Justify {
        CENTER,
        CENTER_BOTTOM,
        CENTER_FULL,
        CENTER_TOP,
        FULL,
        FULL_CENTER,
        LEFT_BOTTOM,
        LEFT_CENTER,
        LEFT_FULL,
        LEFT_TOP,
        RIGHT_BOTTOM,
        RIGHT_CENTER,
        RIGHT_FULL,
        RIGHT_TOP
    };
    
    public static final class Range {
        private int columnIndex;
        private int rowIndex;
        private TableLayout layout;
        private int columnSpan;
        private int rowSpan;
        private String stringValue;
        private Justify justify;
        
        public Range(TableLayout layout, String range) {
            if (layout == null) throw new IllegalArgumentException("layout == null");
            String[] values = ((String)range).split("\\s*,\\s*");
            Justify just = Justify.FULL;
            int width = 1;
            int height = 1;
            if (values.length >= 3) {
                if (values[2].equals("l")) {
                    values[2] = "left";
                } else if (values[2].equals("r")) {
                    values[2] = "right";
                } else if (values[2].equals("c")) {
                    values[2] = "center";
                } else if (values[2].equals("f")) {
                    values[2] = "full";
                } 
                
                if (values[3].equals("t")) {
                    values[3] = "top";
                } else if (values[3].equals("b")) {
                    values[3] = "bottom";
                } else if (values[3].equals("c")) {
                    values[3] = "center";
                } else if (values[3].equals("f")) {
                    values[3] = "full";
                }
                
                try {
                    String justStr = values[2].equals(values[3]) ? values[2] : values[2] + "_" + values[3];
                    just = Justify.valueOf(justStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    width = Integer.parseInt(values[2]);
                    height = Integer.parseInt(values[3]);
                }
                
            }
            
            init(layout, values.length >= 1 ? Integer.parseInt(values[0]) : 0,
                 values.length >= 2 ? Integer.parseInt(values[1]) : 0,
                 width, height, just);
        }

        public Range(TableLayout layout, int column, int row) {
            init(layout, column, row, 1, 1, Justify.FULL);
        }
        
        public Range(TableLayout layout, int column, int row, int columnSpan, int rowSpan) {
            init(layout, column, row, columnSpan, rowSpan, Justify.FULL);
        }
        
        public Range(TableLayout layout, int column, int row, int columnSpan, int rowSpan, Justify justify) {
            init(layout, column, row, columnSpan, rowSpan, justify);
        }

        private void init(TableLayout layout, int column, int row, int columnSpan, int rowSpan, Justify justification) {
            if (layout == null) throw new IllegalArgumentException("layout == null");
            rangeCheck("column", column);            
            rangeCheck("row", row);
            rangeCheck("columnSpan", columnSpan);
            rangeCheck("rowSpan", rowSpan);
            this.columnIndex = column;
            this.rowIndex = row;
            this.columnSpan = columnSpan;
            this.rowSpan = rowSpan;
            this.justify = justification;
        }
        
        private void rangeCheck(String name, int value) {
            if (value < 0 || value > Short.MAX_VALUE) throw new IllegalArgumentException(Range.class.getName() + "." + name + " < 0 || " + Range.class.getName() + "." + name + " > " + Short.MAX_VALUE);
        }
        
        public TableLayout getParent() {
            return layout;
        }
        
        public int getColumnIndex() {
            return columnIndex;
        }
        
        public int getRowIndex() {
            return rowIndex;
        }
        
        public Column getColumn() {
            if (layout == null || columnIndex < 0 || columnIndex >= layout.getColumns().size()) return null;
            return layout.getColumns().get(columnIndex);
        }

        public Row getRow() {
            if (layout == null || rowIndex < 0 || rowIndex >= layout.getRows().size()) return null;
            return layout.getRows().get(rowIndex);
        }

        public int getColumnSpan() {
            return columnSpan;
        }
        
        public int getRowSpan() {
            return rowSpan;
        }
        
        public Justify getJustify() {
            return justify;
        }

        public boolean equals(Object o) {
            return o instanceof Range && toString().equals(o.toString());
        }
        
        public int hashCode() {
            return toString().hashCode();
        }
        
        public String toString() {
            if (stringValue == null) stringValue = "TableLayout.Range{columnIndex:" + columnIndex + ",rowIndex:" + rowIndex + ",columnSpan:" + columnSpan + ",rowSpan:" + rowSpan + ",justify=" + justify + "}"; 
            return stringValue;
        }
    }
    
    public static final class Row extends ArrayGrid.Row {
        private double height;
        private boolean visible;

        public Row(double height) {
            super();
            this.height = height;
            this.visible = true;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
            TableLayout layout = (TableLayout) getParent();
            if (layout.isAutoApply()) layout.apply();
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            if (this.visible == visible) return;
            TableLayout layout = (TableLayout) getParent();
            if (this.visible) layout.visibleRows.remove(this); 
            this.visible = visible;
            if (this.visible) layout.visibleRows.add(this);

        	for (Object o : this) {
        		if (o instanceof Component) {
        			((Component) o).setVisible(this.visible);
        		}
        	}
        	
            if (layout.isAutoApply()) layout.apply();
        }
    }
    
    public static final class Column extends ArrayGrid.Column {
        private double width;
        private boolean visible;

        public Column(double width) {
            super();
            this.width = width;
            this.visible = true;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
            TableLayout layout = (TableLayout) getParent();
            if (layout.isAutoApply()) layout.apply();
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            if (this.visible == visible) return;
            TableLayout layout = (TableLayout) getParent();
            if (this.visible) layout.visibleColumns.remove(this); 
            this.visible = visible;
            if (this.visible) layout.visibleColumns.add(this);

        	for (Object o : this) {
        		if (o instanceof Component) {
        			((Component) o).setVisible(this.visible);
        		}
        	}

            if (layout.isAutoApply()) layout.apply();
        }
    }

    private int margin;
    private int spacing;
    private ArrayGrid<Row, Column> grid;
    private SortedSet<Row> visibleRows;
    private SortedSet<Row> roVisibleRows;
    private SortedSet<Column> visibleColumns;
    private SortedSet<Column> roVisibleColumns;
    private boolean ignoreSet;

    public TableLayout(double sizes[][]) {
        this(sizes, 0, 0);
    }

    public TableLayout(double sizes[][], int margin) {
        this(sizes, margin, 0);
    }
    
    public TableLayout(double sizes[][], int margin, int spacing) {
        this();
        if (sizes == null || sizes.length != 2) throw new IllegalArgumentException("sizes == null || sizes.length != 2");
        if (sizes[0] == null || sizes[0].length == 0) throw new IllegalArgumentException("sizes[0] == null || sizes[0].length == 0");
        if (sizes[1] == null || sizes[1].length == 0) throw new IllegalArgumentException("sizes[1] == null || sizes[1].length == 0");
        ignoreSet = true;
        List<Column> columns = getColumns();
        for (double w : sizes[0]) columns.add(new Column(w));
        
        List<Row> rows = getRows();
        for (double h : sizes[1]) rows.add(new Row(h));
        setSpacing(spacing);
        setMargin(margin);
        ignoreSet = false;
    }
    
    public TableLayout() {
        super(Component.PROPERTY_LIMIT);
        
        ignoreSet = true;
        this.grid = new ArrayGrid<TableLayout.Row, TableLayout.Column>(this, TableLayout.Row.class, TableLayout.Column.class) {
            @Override
            protected void fireItemChange(Type type, int rowIndex, int columnIndex, Object oldValue, Object newValue) {
                List<Component> kids = TableLayout.this.getContainer() == null ? null : TableLayout.this.getContainer().getChildren();
                
                if (rowIndex >= 0 && columnIndex == -1) {
                    TableLayout.this.setAutoApply(false);
                    
                    if (type == ItemChangeEvent.Type.ADD) {
                    	TableLayout.Row newRow = (TableLayout.Row) newValue;
                        if (newRow.isVisible()) TableLayout.this.visibleRows.add(newRow);
                        
                        if (!ignoreSet && kids != null) {
                            for (Component c : kids) {
                                Range l = (Range) c.getLimit();
                                
                                if (rowIndex <= l.getRowIndex()) {
                                    l.rowIndex++;
                                } else if (rowIndex < (l.getRowIndex() + l.getRowSpan())) {
                                    l.rowSpan++;
                                }
                            }
                            
                            for (int i = 0, cnt = newRow.size(); i < cnt; i++) {
                                Component c = (Component) newRow.get(i);
                                
                                if (c != null) {
                                    c.setLimit(new Range(TableLayout.this, i, rowIndex));
                                    kids.add(c);
                                }
                            }
                        }
                    } else if (type == ItemChangeEvent.Type.REMOVE) {
                        if (getRows().size() == 0) { //Clear was called
                            TableLayout.this.visibleRows.clear();
                            if (kids != null) kids.clear();
                        } else {
                            if (((TableLayout.Row)oldValue).isVisible()) TableLayout.this.visibleRows.remove(oldValue);
                            
                            for (Iterator<Component> i = kids.iterator(); i.hasNext();) {
                                Component c = i.next();
                                Range r = (Range) c.getLimit();
                                if (rowIndex == r.getRowIndex()) {
                                    i.remove();
                                } else if (rowIndex < r.getRowIndex()) {
                                    r.rowIndex--;
                                } else if (rowIndex < (r.getRowIndex() + r.getRowSpan())) {
                                    r.rowSpan--;
                                }
                            }
                        }
                    } else if (type == ItemChangeEvent.Type.SET) {
                        TableLayout.Row oldRow = (TableLayout.Row) oldValue;
                        if (oldRow.isVisible()) TableLayout.this.visibleRows.remove(oldRow);
                        
                        for(Iterator i = oldRow.iterator(); i.hasNext();) {
                            Component c = (Component) i.next();
                            kids.remove(c);
                        }
                        
                        //TODO: Bug, for some reason the new row has nothing in it.
                        //TableLayout.Row newRow = (TableLayout.Row) newValue;
                        TableLayout.Row newRow = TableLayout.this.getRows().get(rowIndex);
                        if (newRow.isVisible()) TableLayout.this.visibleRows.add(newRow);
                        
                        for (int i = 0, cnt = newRow.size(); i < cnt; i++) {
                            Component c = (Component) newRow.get(i);
                            c.setLimit(new Range(TableLayout.this, i, rowIndex));
                            kids.add(c);
                        }
                    }
                    
                    TableLayout.this.setAutoApply(true);
                } else if (rowIndex == -1 && columnIndex >= 0) {
                    TableLayout.this.setAutoApply(false);

                    if (type == ItemChangeEvent.Type.ADD) {
                    	TableLayout.Column newColumn = (TableLayout.Column) newValue;
                        if (newColumn.isVisible()) TableLayout.this.visibleColumns.add(newColumn);
                        
                        if (!ignoreSet && kids != null) {
                            for (Component c : kids) {
                                Range r = (Range) c.getLimit();
                                if (columnIndex <= r.getColumnIndex()) {
                                    r.columnIndex++;
                                } else if (columnIndex < (r.getColumnIndex() + r.getColumnSpan())) {
                                    r.columnSpan++;
                                }
                            }
                            
                            for (int i = 0, cnt = newColumn.size(); i < cnt; i++) {
                                Component c = (Component) newColumn.get(i);
                                if (c != null) {
                                    c.setLimit(new Range(TableLayout.this, columnIndex, i));
                                    kids.add(c);
                                }
                            }
                        }
                    } else if (type == ItemChangeEvent.Type.REMOVE) {
                        if (((TableLayout.Column)oldValue).isVisible()) TableLayout.this.visibleColumns.remove(oldValue);
                        
                        if (kids != null) {
                            for (Iterator<Component> i = kids.iterator(); i.hasNext();) {
                                Component c = i.next();
                                Range r = (Range) c.getLimit();
                                if (columnIndex == r.getColumnIndex()) {
                                    i.remove();
                                } else if (columnIndex < r.getColumnIndex()) {
                                    r.columnIndex--;
                                } else if (columnIndex < (r.getColumnIndex() + r.getColumnSpan())) {
                                    r.columnSpan--;
                                }
                            }
                        }
                    } else if (type == ItemChangeEvent.Type.SET) {
                        TableLayout.Column oldColumn = (TableLayout.Column) oldValue;
                        if (oldColumn.isVisible()) TableLayout.this.visibleColumns.remove(oldColumn);
                        
                        for(Iterator i = oldColumn.iterator(); i.hasNext();) {
                            Component c = (Component) i.next();
                            if (kids != null) kids.remove(c);
                        }
                        
                        TableLayout.Column newColumn = (TableLayout.Column)newValue;
                        if (newColumn.isVisible()) TableLayout.this.visibleColumns.add(newColumn);
                        
                        for (int i = 0, cnt = newColumn.size(); i < cnt; i++) {
                            Component c = (Component) newColumn.get(i);
                            c.setLimit(new Range(TableLayout.this, columnIndex, i));
                            if (kids != null) kids.add(c);
                        }
                    }
                    
                    TableLayout.this.setAutoApply(true);
                } else if (rowIndex != -1 && columnIndex != -1 && type == ItemChangeEvent.Type.SET) {
                    if (!ignoreSet && (oldValue == null || !oldValue.equals(newValue))) {
                        
                        if (oldValue != null) {
                            if (oldValue == newValue) return;
                            kids.remove(oldValue);
                        }
                        
                        Component newComp = (Component) newValue;
                        newComp.setLimit(new Range(TableLayout.this, columnIndex, rowIndex));
                        if (kids != null) kids.add(newComp);
                    }
                }
            }
        };
        
        
        
        Comparator<Row> rowIndexOrder = new Comparator<Row>() {
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

        Comparator<Column> columnIndexOrder = new Comparator<Column>() {
            public int compare(Column c1, Column c2) {
                int index1 = c1.getIndex();
                int index2 = c2.getIndex();
                
                if (index1 < index2) {
                    return -1;
                } else if (index1 == index2){
                    return 0;
                } else {
                    return 1;
                }
            }            
        };
        
        List<Row> rows = getRows();
        visibleRows = new TreeSet<Row>(rowIndexOrder);
        visibleRows.addAll(rows);
        roVisibleRows = Collections.unmodifiableSortedSet(visibleRows);

        List<Column> columns = getColumns();
        visibleColumns = new TreeSet<Column>(columnIndexOrder);
        visibleColumns.addAll(columns);
        roVisibleColumns = Collections.unmodifiableSortedSet(visibleColumns);
        
        
        setAutoApply(true);
        ignoreSet = false;
    }
    
    @Override
    protected void addComponent(Component comp) {
        List<Row> rows = getRows();
        Range l = (Range) comp.getLimit();
        int row = l.getRowIndex();
        int col = l.getColumnIndex();
        ignoreSet = true;
        for (int i = row, cnt = l.getRowSpan() + row; i < cnt; i++) {
            Row curRow = rows.get(i);
            for (int j = col, cnt2 = l.getColumnSpan() + col; j < cnt2; j++) {
                curRow.set(j, comp);
            }
        }
        ignoreSet = false;
    }

    @Override
    protected void removeComponent(Component comp) {
        List<Row> rows = getRows();
        Range r = (Range) comp.getLimit();
        int row = r.getRowIndex();
        int col = r.getColumnIndex();
        ignoreSet = true;
        for (int i = row, cnt = r.getRowSpan() + row; i < cnt; i++) {
            Row curRow = rows.get(i);
            for (int j = col, cnt2 = r.getColumnSpan() + col; j < cnt2; j++) {
                curRow.set(j, null);
            }
        }
        ignoreSet = false;
    }

    @Override
    protected Object getFormalLimit(Component comp) {
        Object oLimit = comp.getLimit();
        Range r;
        
        if (oLimit instanceof String) {
            r =  new Range(this, (String)oLimit);
        } else if (oLimit instanceof Range) {
            r = (Range)oLimit;
            if (r.layout != this) r = new Range(this, r.getColumnIndex(), r.getRowIndex(),
                    r.getColumnSpan(), r.getRowSpan(), r.getJustify());
        } else {
            r = new Range(this, 0, 0);
        }
        
        return r;
    }
    
    private int[] getAbsoluteSizes(int availableSize, SortedSet<? extends List> sizes) {
        int length = sizes.size();
        int[] absoluteSizes = new int[length];
        availableSize -= (length - 1) * spacing + margin * 2;
        int fillCnt = 0;

        for (List rc : sizes) {
            double f = rc instanceof Row ? ((Row) rc).getHeight() : ((Column) rc).getWidth();
            if (f >= 1) availableSize -= f;
        }
        
        int pctSize = availableSize;
        int i = 0;
        
        for (List o : sizes) {
            double size = o instanceof Row ? ((Row) o).getHeight() : ((Column) o).getWidth();
            
            if (size == 0) {
                fillCnt++;
            } else if (size < 1) {
                absoluteSizes[i] = (int)(pctSize * size);
                availableSize -= absoluteSizes[i];
            } else {
                absoluteSizes[i] = (int)size;
            }
            
            i++;
        }
        
        if (fillCnt > 0) {
            int fillSize = availableSize / fillCnt;
            int lastIndex = -1;
            i = 0;
            
            for (List o : sizes) {
                double size = o instanceof Row ? ((Row) o).getHeight() : ((Column) o).getWidth();

                if (size == 0) {
                    absoluteSizes[i] = fillSize;
                    lastIndex = i;
                }
                
                i++;
            }
            
            if (lastIndex != -1) absoluteSizes[lastIndex] += availableSize % fillCnt;
        }
        
        return absoluteSizes;
    }
    
    public int getMargin() {
        return margin;
    }
    
    public void setMargin(int margin) {
        if (margin < 0 || margin >= Short.MAX_VALUE) throw new IllegalArgumentException("margin < 0 || margin >= " + Short.MAX_VALUE);
        this.margin = margin;
        if (autoLayout) apply();
    }
    
    public int getSpacing() {
        return spacing;
    }
    
    public void setSpacing(int spacing) {
        if (spacing < 0 || spacing >= Short.MAX_VALUE) throw new IllegalArgumentException("spacing < 0 || spacing >= " + Short.MAX_VALUE);
        this.spacing = spacing;
        if (autoLayout) apply();
    }
    
    public void apply() {
        if (container == null) return;
        
        SortedSet<Row> visibleRows = getVisibleRows();
        SortedSet<Column> visibleColumns = getVisibleColumns();
        int[] absoluteWidths = getAbsoluteSizes(calculateInnerWidth(), visibleColumns);
        int[] absoluteHeights = getAbsoluteSizes(container.getInnerHeight(), visibleRows);
        
        Column[] columnArray = visibleColumns.toArray(new Column[0]);
        Map<Integer, List<Component>> rowComponents = new HashMap<Integer, List<Component>>();
        Map<Integer, List<Component>> colComponents = new HashMap<Integer, List<Component>>();
        
        int rowIndex = 0;
        for (Row r : visibleRows) {
        	for (int i = 0, cnt = columnArray.length; i < cnt; i++) {
        		Object o = r.get(columnArray[i].getIndex());
        		if (o instanceof Component) {
        			Component c = (Component) o;
        			Range limit = (Range) c.getLimit();
        			Justify just = limit.getJustify();
        			int x = margin, y = margin, width = 0, height = 0;
        			
        			if (rowComponents.get(rowIndex) == null) rowComponents.put(rowIndex, new ArrayList<Component>());
        			if (colComponents.get(i) == null) colComponents.put(i, new ArrayList<Component>());
        			
        			if (rowComponents.get(rowIndex).contains(c)) {
        				width = c.getWidth();
    					height = c.getHeight();
        				x = c.getX();
        				y = c.getY();
        				
        				if (!colComponents.get(i).contains(c)) {
	        				colComponents.get(i).add(c);
	        				width += spacing + absoluteWidths[i];
        				}
        			} else if (colComponents.get(i).contains(c)) {
        				width = c.getWidth();
        				height = c.getHeight();
        				x = c.getX();
        				y = c.getY();
        				
        				if (!rowComponents.get(rowIndex).contains(c)) {
        					rowComponents.get(rowIndex).add(c);
            				height += spacing + absoluteHeights[rowIndex];
        				}
        			} else {
        				rowComponents.get(rowIndex).add(c);
        				colComponents.get(i).add(c);
        				
        				for (int j = 0; j < i; j++) {
        	                x += absoluteWidths[j] + spacing;
        	            }
        				
        				for (int j = 0; j < rowIndex; j++) {
        	                y += absoluteHeights[j] + spacing;
        	            }
        				
        				width += absoluteWidths[i];
            			
            			if (just != Justify.FULL && c.getWidth() < width) width = c.getWidth();
            			
            			height += absoluteHeights[rowIndex];
            			
            			if (just != Justify.FULL && c.getHeight() < height) height = c.getHeight();
            			
            			if (width == c.getWidth()) {
                            if (just.name().indexOf("RIGHT") > -1) {
                                x += absoluteWidths[i] - width;
                            } else if (just == Justify.CENTER || just == Justify.CENTER_BOTTOM || just == Justify.CENTER_FULL 
                                || just == Justify.CENTER_TOP) {
                                x += (absoluteWidths[i] / 2) - (width / 2);
                            }
                        }
            			
            			if (height == c.getHeight()) {
                            if (just.name().indexOf("BOTTOM") > -1) {
                                y += absoluteHeights[rowIndex] - height;
                            } else if (just == Justify.CENTER || just == Justify.FULL_CENTER || just == Justify.LEFT_CENTER 
                                || just == Justify.RIGHT_CENTER) {
                                y += (absoluteHeights[rowIndex] / 2) - (height / 2);
                            }
                        }
        			}
        			
        			if (width >= 0 && height >= 0) c.setBounds(x, y, width, height);
        		}
        	}
        	
        	rowIndex++;
        }
    }

    public List<Column> getColumns() {
        return grid.getColumns();
    }

    public List<Row> getRows() {
        return grid.getRows();
    }
    
    public SortedSet<Column> getVisibleColumns() {
        return roVisibleColumns;
    }
    
    public SortedSet<Row> getVisibleRows() {
        return roVisibleRows;
    }
    
    public String toString() {
        return "TableLayout@" + System.identityHashCode(this) + "{columns.size=" + grid.getColumns().size() + ",rows.size=" + grid.getRows().size() + "}";
    }
    
    private int calculateInnerWidth() {
    	if (container.getScrollType() != Container.ScrollType.NONE) {
	    	int totalHeight = 0;
	    	SortedSet<Row> visibleRows = getVisibleRows();
	        for (Row r : visibleRows) {
	        	double height = r.getHeight();
	        	if (height >= 1) totalHeight += height;
	        }
	        totalHeight += spacing * visibleRows.size();
	        totalHeight += margin * 2;
	        int innerHeight = container.getInnerHeight();
	        int innerWidth = container.getInnerWidth();
	        if (innerHeight < totalHeight) innerWidth -= 20;
	        return innerWidth;
    	} else {
    		return container.getInnerWidth();
    	}
    }
}
