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
package thinwire.ui.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import thinwire.ui.Component;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeEvent.Type;
import thinwire.ui.layout.TableLayout.Range.Justify;
import thinwire.util.ArrayGrid;
import thinwire.util.Grid;

/**
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public final class TableLayout extends AbstractLayout implements Grid<TableLayout.Row, TableLayout.Column> {
    private static final Logger log = Logger.getLogger(TableLayout.class.getName());
    
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
            if (layout.isAutoLayout()) layout.apply();
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
            TableLayout layout = (TableLayout) getParent();
            List<Component> kids = layout.getContainer().getChildren();
            if (!visible) {
                for (Component c : kids) {
                    Range range = (Range) c.getLimit();
                    if (range.getRowIndex() == getIndex()) {
                        c.setVisible(false);
                    } else if (getIndex() < range.getRowIndex()) {
                        range.rowIndex--;
                    } else if (getIndex() < (range.getRowIndex() + range.getHeight())) {
                        range.height--;
                    }
                }
            } else {
                for (Component c : kids) {
                    Range range = (Range) c.getLimit();
                    if (getIndex() < range.getRowIndex() || (getIndex() == range.getRowIndex() && c.isVisible())){
                        range.rowIndex++;
                    } else if (range.getRowIndex() == getIndex() && !c.isVisible()) {
                        c.setVisible(true);
                    } else if (getIndex() < (range.getRowIndex() + range.getHeight())) {
                        range.height++;
                    }
                }
            }
            
            if (layout.isAutoLayout()) layout.apply();
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
            if (layout.isAutoLayout()) layout.apply();
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
            TableLayout layout = (TableLayout) getParent();
            List<Component> kids = layout.getContainer().getChildren();
            if (!visible) {
                for (Component c : kids) {
                    Range range = (Range) c.getLimit();
                    if (range.getColumnIndex() == getIndex()) {
                        c.setVisible(false);
                    } else if (getIndex() < range.getColumnIndex()) {
                        range.columnIndex--;
                    } else if (getIndex() < (range.getColumnIndex() + range.getWidth())) {
                        range.width--;
                    }
                }
            } else {
                for (Component c : kids) {
                    Range range = (Range) c.getLimit();
                    if (getIndex() < range.getColumnIndex() || (getIndex() == range.getColumnIndex() && c.isVisible())){
                        range.columnIndex++;
                    } else if (range.getColumnIndex() == getIndex() && !c.isVisible()) {
                        c.setVisible(true);
                    } else if (getIndex() < (range.getColumnIndex() + range.getWidth())) {
                        range.width++;
                    }
                }
            }
            
            if (layout.isAutoLayout()) layout.apply();
        }
    }
    
    public static class Range {
        
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
        
        private int columnIndex;
        private int rowIndex;
        private TableLayout layout = null;
        private int width;
        private int height;
        private String value;
        private Justify justification;
        
        public Range() {
            this(0, 0, 1, 1, Justify.FULL);
        }
        
        public Range(String range) {
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
            init(values.length >= 1 ? Integer.parseInt(values[0]) : 0,
                 values.length >= 2 ? Integer.parseInt(values[1]) : 0,
                 width, height, just);
        }
        
        public Range(int column, int row, int width, int height, Justify justification) {
            init(column, row, width, height, justification);
        }

        private void init(int column, int row, int width, int height, Justify justification) {
            rangeCheck("column", column);            
            rangeCheck("row", row);
            rangeCheck("width", width);
            rangeCheck("height", height);
            this.columnIndex = column;
            this.rowIndex = row;
            this.width = width;
            this.height = height;
            this.value = getClass().getName() + "(" + column + ", " + row + ", " + width + ", " + height + ")";
            this.justification = justification;
        }
        
        private void rangeCheck(String name, int value) {
            if (value < 0 || value > Short.MAX_VALUE) throw new IllegalArgumentException(Range.class.getName() + "." + name + " < 0 || " + Range.class.getName() + "." + name + " > " + Short.MAX_VALUE);
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

        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public Justify getJustification() {
            return justification;
        }


        public boolean equals(Object o) {
            return o instanceof Range && toString().equals(o.toString());
        }
        
        public int hashCode() {
            return toString().hashCode();
        }
        
        public String toString() {
            return value;
        }
    }
    
    private static final Range DEFAULT_LIMIT = new Range();
    
    private int margin;
    private int spacing;
    private ArrayGrid<Row, Column> grid;
    private boolean ignoreSet;

    public TableLayout(double sizes[][]) {
        this(sizes, 0, 0);
    }

    public TableLayout(double sizes[][], int margin) {
        this(sizes, margin, 0);
    }
    
    public TableLayout(double sizes[][], int margin, int spacing) {
        super(Component.PROPERTY_LIMIT);
        if (sizes == null || sizes.length != 2) throw new IllegalArgumentException("sizes == null || sizes.length != 2");
        if (sizes[0] == null || sizes[0].length == 0) throw new IllegalArgumentException("sizes[0] == null || sizes[0].length == 0");
        if (sizes[1] == null || sizes[1].length == 0) throw new IllegalArgumentException("sizes[1] == null || sizes[1].length == 0");
        ignoreSet = true;
        this.grid = new ArrayGrid<TableLayout.Row, TableLayout.Column>(this, TableLayout.Row.class, TableLayout.Column.class) {
            @Override
            protected void fireItemChange(Type type, int rowIndex, int columnIndex, Object oldValue, Object newValue) {
                if (rowIndex >= 0 && columnIndex == -1) {
                    
                    if (type == ItemChangeEvent.Type.ADD) {
                        if (!ignoreSet) {
                            for (Component c : TableLayout.this.getContainer().getChildren()) {
                                Range l = (Range) c.getLimit();
                                if (rowIndex <= l.getRowIndex()) {
                                    l.rowIndex++;
                                } else if (rowIndex < (l.getRowIndex() + l.getHeight())) {
                                    l.height++;
                                }
                            }
                            TableLayout.this.setAutoLayout(false);
                            TableLayout.Row newRow = (TableLayout.Row) newValue;
                            List<Component> kids = TableLayout.this.getContainer().getChildren();
                            for (int i = 0, cnt = newRow.size(); i < cnt; i++) {
                                Component c = (Component) newRow.get(i);
                                c.setLimit(i + ", " + rowIndex);
                                kids.add(c);
                            }
                            TableLayout.this.setAutoLayout(true);
                        }
                    } else if (type == ItemChangeEvent.Type.REMOVE) {
                        TableLayout.this.setAutoLayout(false);
                        for (Iterator<Component> i = TableLayout.this.getContainer().getChildren().iterator(); i.hasNext();) {
                            Component c = i.next();
                            Range r = (Range) c.getLimit();
                            if (rowIndex == r.getRowIndex()) {
                                i.remove();
                            } else if (rowIndex < r.getRowIndex()) {
                                r.rowIndex--;
                            } else if (rowIndex < (r.getRowIndex() + r.getHeight())) {
                                r.height--;
                            }
                        }
                        TableLayout.this.setAutoLayout(true);
                    } else if (type == ItemChangeEvent.Type.SET) {
                        TableLayout.this.setAutoLayout(false);
                        List<Component> kids = TableLayout.this.getContainer().getChildren();
                        TableLayout.Row oldRow = (TableLayout.Row) oldValue;
                        for(Iterator i = oldRow.iterator(); i.hasNext();) {
                            Component c = (Component) i.next();
                            kids.remove(c);
                        }
                        
                        //TableLayout.Row newRow = (TableLayout.Row) newValue;
                        TableLayout.Row newRow = TableLayout.this.getRows().get(rowIndex);
                        for (int i = 0, cnt = newRow.size(); i < cnt; i++) {
                            Component c = (Component) newRow.get(i);
                            c.setLimit(i + ", " + rowIndex);
                            kids.add(c);
                        }
                        TableLayout.this.setAutoLayout(true);
                    }
                    TableLayout.this.apply();
                } else if (rowIndex == -1 && columnIndex >= 0) {
                    if (type == ItemChangeEvent.Type.ADD) {
                        if (!ignoreSet) {
                            for (Component c : TableLayout.this.getContainer().getChildren()) {
                                Range r = (Range) c.getLimit();
                                if (columnIndex <= r.getColumnIndex()) {
                                    r.columnIndex++;
                                } else if (columnIndex < (r.getColumnIndex() + r.getWidth())) {
                                    r.width++;
                                }
                            }
                            TableLayout.this.setAutoLayout(false);
                            TableLayout.Column newColumn = (TableLayout.Column) newValue;
                            List<Component> kids = TableLayout.this.getContainer().getChildren();
                            for (int i = 0, cnt = newColumn.size(); i < cnt; i++) {
                                Component c = (Component) newColumn.get(i);
                                c.setLimit(columnIndex + ", " + i);
                                kids.add(c);
                            }
                            TableLayout.this.setAutoLayout(true);
                        }
                    } else if (type == ItemChangeEvent.Type.REMOVE) {
                        TableLayout.this.setAutoLayout(false);
                        for (Iterator<Component> i = TableLayout.this.getContainer().getChildren().iterator(); i.hasNext();) {
                            Component c = i.next();
                            Range r = (Range) c.getLimit();
                            if (columnIndex == r.getColumnIndex()) {
                                i.remove();
                            } else if (columnIndex < r.getColumnIndex()) {
                                r.columnIndex--;
                            } else if (columnIndex < (r.getColumnIndex() + r.getWidth())) {
                                r.width--;
                            }
                        }
                        TableLayout.this.setAutoLayout(true);
                    } else if (type == ItemChangeEvent.Type.SET) {
                        TableLayout.this.setAutoLayout(false);
                        List<Component> kids = TableLayout.this.getContainer().getChildren();
                        TableLayout.Column oldColumn = (TableLayout.Column) oldValue;
                        for(Iterator i = oldColumn.iterator(); i.hasNext();) {
                            Component c = (Component) i.next();
                            kids.remove(c);
                        }
                        
                        TableLayout.Column newColumn = (TableLayout.Column) newValue;
                        for (int i = 0, cnt = newColumn.size(); i < cnt; i++) {
                            Component c = (Component) newColumn.get(i);
                            c.setLimit(columnIndex + ", " + i);
                            kids.add(c);
                        }
                        TableLayout.this.setAutoLayout(true);
                    }
                    TableLayout.this.apply();
                } else if (rowIndex != -1 && columnIndex != -1 && type == ItemChangeEvent.Type.SET) {
                    if (!ignoreSet && (oldValue == null || !oldValue.equals(newValue))) {
                        if (oldValue != null) {
                            if (oldValue.equals(newValue)) return;
                            TableLayout.this.getContainer().getChildren().remove(oldValue);
                        }
                        Component newComp = (Component) newValue;
                        newComp.setLimit(columnIndex + ", " + rowIndex);
                        TableLayout.this.getContainer().getChildren().add(newComp);
                    }
                }
            }
        };
        
        List<Column> columns = getColumns();
        for (double w : sizes[0]) columns.add(new Column(w));
        
        List<Row> rows = getRows();
        for (double h : sizes[1]) rows.add(new Row(h));
        
        setSpacing(spacing);
        setMargin(margin);
        setAutoLayout(true);
        ignoreSet = false;
    }
    
    @Override
    protected void addComponent(Component comp) {
        List<Row> rows = getRows();
        Range l = (Range) comp.getLimit();
        if (l == null) l = DEFAULT_LIMIT;
        int row = l.getRowIndex();
        int col = l.getColumnIndex();
        ignoreSet = true;
        for (int i = row, cnt = l.getHeight() + row; i < cnt; i++) {
            Row curRow = rows.get(i);
            for (int j = col, cnt2 = l.getWidth() + col; j < cnt2; j++) {
                curRow.set(j, comp);
            }
        }
        ignoreSet = false;
    }

    @Override
    protected void removeComponent(Component comp) {
        List<Row> rows = getRows();
        Range r = (Range) comp.getLimit();
        if (r == null) r = DEFAULT_LIMIT;
        int row = r.getRowIndex();
        int col = r.getColumnIndex();
        ignoreSet = true;
        for (int i = row, cnt = r.getHeight() + row; i < cnt; i++) {
            Row curRow = rows.get(i);
            for (int j = col, cnt2 = r.getWidth() + col; j < cnt2; j++) {
                curRow.set(j, null);
            }
        }
        ignoreSet = false;
    }

    @Override
    protected Object getFormalLimit(Component comp) {
        Object oLimit = comp.getLimit();
        Range r = null;
        
        if (oLimit instanceof String) {
            r =  new Range((String)oLimit);
        } else if (oLimit instanceof Range) {
            r = (Range)oLimit;
        }
        
        if (r != null) r.layout = this;
        return r;
    }
    
    private int[] getAbsoluteSizes(int availableSize, List<? extends List> sizes) {
        int length = sizes.size();
        int[] absoluteSizes = new int[length];
        availableSize -= (length - 1) * spacing + margin * 2;
        int fillCnt = 0;

        for (List rc : sizes) {
            double f = rc instanceof Row ? ((Row) rc).getHeight() : ((Column) rc).getWidth();
            if (f >= 1) availableSize -= f;
        }
        
        int pctSize = availableSize;

        for (int i = 0, cnt = length; i < cnt; i++) {
            Object o = sizes.get(i);
            double size = o instanceof Row ? ((Row) o).getHeight() : ((Column) o).getWidth();
            
            if (size == 0) {
                fillCnt++;
            } else if (size < 1) {
                absoluteSizes[i] = (int)(pctSize * size);
                availableSize -= absoluteSizes[i];
            } else {
                absoluteSizes[i] = (int)size;
            }
        }
        
        if (fillCnt > 0) {
            int fillSize = availableSize / fillCnt;
            int lastIndex = -1;
            
            for (int i = 0, cnt = length; i < cnt; i++) {
                Object o = sizes.get(i);
                double size = o instanceof Row ? ((Row) o).getHeight() : ((Column) o).getWidth();
                if (size == 0) {
                    absoluteSizes[i] = fillSize;
                    lastIndex = i;
                }
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
        int[] absoluteWidths = getAbsoluteSizes(container.getInnerWidth(), getVisibleColumns());
        int[] absoluteHeights = getAbsoluteSizes(container.getInnerHeight(), getVisibleRows());

        for (Component c : (List<Component>) container.getChildren()) {
            Range limit = (Range)c.getLimit();
            if (limit == null) limit = DEFAULT_LIMIT;
            Justify just = limit.getJustification();
            int x = margin, y = margin, width = 0, height = 0;
            
            for (int i = 0, cnt = limit.width, column = limit.columnIndex; i < cnt; i++) {
                width += absoluteWidths[i + column] + spacing;
            }
            
            width -= spacing;
            
            if (just != Justify.FULL && c.getWidth() < width) width = c.getWidth();
            
            for (int i = 0, cnt = limit.height, row = limit.rowIndex; i < cnt; i++) {
                height += absoluteHeights[i + row] + spacing;
            }
            
            height -= spacing;
            
            if (just != Justify.FULL && c.getHeight() < height) height = c.getHeight();
            
            for (int i = 0, cnt = limit.columnIndex; i < cnt; i++) {
                x += absoluteWidths[i] + spacing;
            }
            
            if (width == c.getWidth()) {
                if (just.name().indexOf("RIGHT") > -1) {
                    x += absoluteWidths[limit.columnIndex] - width;
                } else if (just == Justify.CENTER || just == Justify.CENTER_BOTTOM || just == Justify.CENTER_FULL 
                    || just == Justify.CENTER_TOP) {
                    x += (absoluteWidths[limit.columnIndex] / 2) - (width / 2);
                }
            }
            
            for (int i = 0, cnt = limit.rowIndex; i < cnt; i++) {
                y += absoluteHeights[i] + spacing;
            }
            
            if (height == c.getHeight()) {
                if (just.name().indexOf("BOTTOM") > -1) {
                    y += absoluteHeights[limit.rowIndex] - height;
                } else if (just == Justify.CENTER || just == Justify.FULL_CENTER || just == Justify.LEFT_CENTER 
                    || just == Justify.RIGHT_CENTER) {
                    y += (absoluteHeights[limit.rowIndex] / 2) - (height / 2);
                }
            }
            
            if (width >= 0 && height >= 0) c.setBounds(x, y, width, height);
        }
    }

    public List<Column> getColumns() {
        return grid.getColumns();
    }

    public List<Row> getRows() {
        return grid.getRows();
    }
    
    public List<Column> getVisibleColumns() {
        List<Column> l = new ArrayList<Column>(getColumns());
        for (Iterator<Column> i = l.iterator(); i.hasNext();) {
            Column c = i.next();
            if (!c.isVisible()) i.remove();
        }
        return l;
    }
    
    public List<Row> getVisibleRows() {
        List<Row> l = new ArrayList<Row>(getRows());
        for (Iterator<Row> i = l.iterator(); i.hasNext();) {
            Row r = i.next();
            if (!r.isVisible()) i.remove();
        }
        return l;
    }
}
