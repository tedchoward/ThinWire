/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.render.web;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;

import thinwire.ui.AlignX;
import thinwire.ui.Application;
import thinwire.ui.DropDownGridBox;
import thinwire.ui.GridBox;
import thinwire.ui.Component;
import thinwire.ui.GridBox.CellPosition;
import thinwire.ui.GridBox.Column;
import thinwire.ui.GridBox.Row;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;

/**
 * @author Joshua J. Gertzen
 */
final class GridBoxRenderer extends ComponentRenderer implements ItemChangeListener {
    private static final String GRIDBOX_CLASS = "tw_GridBox";
    private static final String SET_VISIBLE_HEADER = "setVisibleHeader";
    private static final String SET_VISIBLE_CHECK_BOXES = "setVisibleCheckBoxes";
    private static final String SET_FULL_ROW_CHECK_BOX = "setFullRowCheckBox";
    private static final String SET_ROW_INDEX_SELECTED = "setRowIndexSelected";
    private static final String SET_ROW_INDEX_CHECK_STATE = "setRowIndexCheckState";
    private static final String SET_COLUMN_NAME = "setColumnName";
    private static final String SET_COLUMN_WIDTH = "setColumnWidth";
    private static final String SET_COLUMN_ALIGN_X = "setColumnAlignX";
    private static final String ADD_ROW = "addRow";
    private static final String REMOVE_ROW = "removeRow";
    private static final String SET_ROW = "setRow";
    private static final String SET_CELL = "setCell";
    private static final String ADD_COLUMN = "addColumn";
    private static final String SET_COLUMN = "setColumn";
    private static final String REMOVE_COLUMN = "removeColumn";
    private static final int MIN_SIZE = 25;
    private static final String VIEW_STATE_COLUMN_SORT = "columnSort";

    private Set<Integer> rowState = new HashSet<Integer>();
    private Set<Integer> columnState = new HashSet<Integer>();
    private GridBox gb;
    private Map<GridBox, GridBoxRenderer> childToRenderer;
    DropDownGridBox dd;
    
    //TODO: Column indexes on the client side may differ if there is a hidden column between two visible columns
    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        render(wr, c, container, null);
    }
    
    private void render(WindowRenderer wr, Component c, ComponentRenderer container, Integer parentIndex) {
        init(GRIDBOX_CLASS, wr, c, container);
        gb = (GridBox)c;       

        this.wr = wr;
        gb.addItemChangeListener(this);

        StringBuffer checkedRows = new StringBuffer();
        
        synchronized (checkedRows) {
            getCheckedRowIndices(checkedRows);
        }
        
        StringBuffer colDefs = new StringBuffer();
        
        synchronized (colDefs) {            
            List<Column> columns = gb.getColumns();
            colDefs.append('[');

            for (int i = 0, cnt = columns.size(); i < cnt; i++) {
                Column col = (Column)columns.get(i);
                columnState.add(System.identityHashCode(col));        
    
                if (col.isVisible()) {
                    colDefs.append("{v:");
                    getValues(col, col.getDisplayFormat(), colDefs);
                    colDefs.append(",n:\"").append(getValue(col.getDisplayName(), null)).append("\"");
                    colDefs.append(",w:").append(col.getWidth());
                    colDefs.append(",a:\"").append(((AlignX)col.getAlignX()).name().toLowerCase()).append("\"");
                    colDefs.append("},");
                }
            }

            int len = colDefs.length();
            
            if (len == 1)
                colDefs.append(']');
            else
                colDefs.setCharAt(len - 1, ']');
        }        
        
        if (!(container instanceof ContainerRenderer)) {
            //a gridbox for a dropdown does not support the focus, enabled, x or y properties
            setPropertyChangeIgnored(Component.PROPERTY_FOCUS, true);
            setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);            
            setPropertyChangeIgnored(Component.PROPERTY_VISIBLE, true);            
            setPropertyChangeIgnored(Component.PROPERTY_X, true);            
            setPropertyChangeIgnored(Component.PROPERTY_Y, true);            

            if (container instanceof DropDownRenderer) {
                dd = (DropDownGridBox)container.comp;                
                if (gb.getWidth() == 0 || gb.getWidth() < dd.getWidth()) gb.setWidth(getCalcWidth());
                if (gb.getHeight() == 0 || gb.getHeight() < MIN_SIZE) gb.setHeight(getCalcHeight());                
            } else if (container instanceof GridBoxRenderer) {
                if (gb.getWidth() == 0) gb.setWidth(getCalcWidth());
                if (gb.getHeight() == 0) gb.setHeight(getCalcHeight());
            }
        } 

        addClientSideProperty(GridBox.Row.PROPERTY_ROW_CHECKED);
        addClientSideProperty(GridBox.Row.PROPERTY_ROW_SELECTED);
        addClientSideProperty(GridBox.Column.PROPERTY_COLUMN_WIDTH);
        addInitProperty(GridBox.PROPERTY_VISIBLE_HEADER, gb.isVisibleHeader());
        addInitProperty(GridBox.PROPERTY_VISIBLE_CHECK_BOXES, gb.isVisibleCheckBoxes());
        addInitProperty(GridBox.PROPERTY_FULL_ROW_CHECK_BOX, gb.isFullRowCheckBox());
        addInitProperty("parentIndex", parentIndex);
        Row sr = gb.getSelectedRow();
        addInitProperty("selectedRow", sr == null || colDefs.length() == 2 ? -1 : sr.getIndex());
        addInitProperty("checkedRows", checkedRows.toString());
        addInitProperty("columnData", colDefs);
        super.render(wr, c, container);
        
        List<Row> rows = gb.getRows();

        for (int i = 0, cnt = rows.size(); i < cnt; i++) {
            Row r = rows.get(i);
            rowState.add(new Integer(System.identityHashCode(r)));
            GridBox child = r.getChild();            
            if (child != null) renderChild(i, child);
        }
    }

    private int getCalcWidth() {
        int width = 0;
        List<GridBox.Column> cols = gb.getColumns(); 
        boolean[] visibleState = new boolean[cols.size()];
        
        for (int i = 0, size = cols.size(); i < size; i++) {
            visibleState[i] = cols.get(i).isVisible();
        }
                
        if (gb.isVisibleHeader()) {
            int cnt = 0;

            for (int i = 0, size = cols.size(); i < size; i++) {
                if (visibleState[i]) {
                    GridBox.Column col = cols.get(i);
                    String name = col.getDisplayName();
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

        for (Row r : gb.getRows()) {
            int cnt = 0;
            
            for (int i = 0, size = r.size(); i < size; i++) {
                if (visibleState[i]) {
                    Object cell = r.get(i);
                 
                    if (cell != null) {                    
                        String value = cell.toString();
                        int len = value.length();
                        String upperValue = value.toUpperCase();
                        if (value.equals(upperValue)) len = len / 7 + 1;
                        if (len < 4) len++;
                        cnt += len;
                    }
                }
            }
            
            if (cnt > width) width = cnt;
        }
        
        if (gb.isVisibleCheckBoxes()) width += 3;
        
        width *= 6.6; //TODO: Hardcoded character width.        
        
        if (gb.getParent() instanceof DropDownGridBox) {
            int ddWidth = ((DropDownGridBox)gb.getParent()).getWidth();
            if (ddWidth > width) width = ddWidth;
        }
        
        int maxWidth = Application.current().getFrame().getInnerWidth() / 2 - 10;
        if (width > maxWidth) width = maxWidth;
        if (width < MIN_SIZE) width = MIN_SIZE;
        return width;
    }
    
    private int getCalcHeight() {
        int height = gb.getRows().size();
        if (height < 3) height = 3;
        height *= 14; //TODO: Hardcoded row height        
        height += 10; //TODO: Hardcoded fudge factor for border        
        if (gb.isVisibleHeader()) height += 16; //TODO: Hardcoded column header size
        int maxHeight = Application.current().getFrame().getInnerHeight() / 2 - 20;
        if (gb.getParent() instanceof DropDownGridBox) maxHeight -= ((DropDownGridBox)gb.getParent()).getHeight();
        if (height > maxHeight) height = maxHeight;
        if (height < MIN_SIZE) height = MIN_SIZE; 
        return height;
    }

    void destroy() {
        super.destroy();
        gb.removeItemChangeListener(this);
        gb = null;
        dd = null;
        rowState.clear();
        columnState.clear();
        columnState = rowState = null;
        
        if (childToRenderer != null) {
            for (GridBoxRenderer gbr : childToRenderer.values()) {
                gbr.destroy();
            }
            
            childToRenderer.clear();
            childToRenderer = null;
        }
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        String name = pce.getPropertyName();
        Object source = pce.getSource();
        Object oldValue = pce.getOldValue();
        Object newValue = pce.getNewValue();

        if (source instanceof GridBox) {
            if (isPropertyChangeIgnored(name)) return;
            
            if (name.equals(GridBox.PROPERTY_VISIBLE_HEADER)) {
                postClientEvent(SET_VISIBLE_HEADER, newValue);
            } else if (name.equals(GridBox.PROPERTY_VISIBLE_CHECK_BOXES)) {
                postClientEvent(SET_VISIBLE_CHECK_BOXES, newValue, newValue == Boolean.TRUE ? getCheckedRowIndices(null) : null);
            } else if (name.equals(GridBox.PROPERTY_FULL_ROW_CHECK_BOX)) {
                postClientEvent(SET_FULL_ROW_CHECK_BOX, newValue);
            } else if (dd != null) {
                if (name.equals(Component.PROPERTY_WIDTH)) {
                    Integer width = (Integer)newValue;
                    width = width.intValue() < dd.getWidth() ? new Integer(dd.getWidth()) : width;
                    postClientEvent(SET_WIDTH, width);
                } else if (name.equals(Component.PROPERTY_HEIGHT)) {
                    Integer height = (Integer)newValue;
                    height = height.intValue() < MIN_SIZE ? new Integer(MIN_SIZE) : height;
                    postClientEvent(SET_HEIGHT, height);
                }
            } else {
                super.propertyChange(pce);
            }
        } else if (source instanceof Row) {
            if (isPropertyChangeIgnored(name) || !rowState.contains(new Integer(System.identityHashCode(source)))) return;
            
            if (name.equals(GridBox.Row.PROPERTY_ROW_SELECTED)) {
                postClientEvent(SET_ROW_INDEX_SELECTED, ((Row)source).getIndex(), Boolean.FALSE);
            } else if (name.equals(GridBox.Row.PROPERTY_ROW_CHECKED)) {
                postClientEvent(SET_ROW_INDEX_CHECK_STATE, new Integer(((Row)source).getIndex()), newValue);
            } else if (name.equals(GridBox.Row.PROPERTY_ROW_CHILD)) {
                if (newValue != null) {
                    if (oldValue != null) ((GridBoxRenderer)childToRenderer.remove(oldValue)).destroy();
                    renderChild(new Integer(((Row)source).getIndex()), (GridBox)newValue);
                } else if (oldValue != null) {
                    GridBoxRenderer gbrChild = (GridBoxRenderer)childToRenderer.remove(oldValue);
                    gbrChild.postClientEvent(DESTROY, null);
                    gbrChild.destroy();
                }
            }
        } else if (source instanceof Column) {
            if (isPropertyChangeIgnored(name) || !columnState.contains(new Integer(System.identityHashCode(source)))) return;
            Column column = (Column)source;
            Integer index = new Integer(column.getIndex());

            if (name.equals(GridBox.Column.PROPERTY_COLUMN_VISIBLE)) {
                if (((Boolean)newValue).booleanValue()) {
                    addColumn(column);
                } else {
                    removeColumn(index);
                }
            } else if (column.isVisible()) {   
                if (name.equals(GridBox.Column.PROPERTY_COLUMN_NAME) && ((String)newValue).equals(column.getDisplayName())) {
                    postClientEvent(SET_COLUMN_NAME, getVisibleIndex(index), newValue);
                } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_DISPLAY_NAME)) {
                    postClientEvent(SET_COLUMN_NAME, getVisibleIndex(index), newValue);                    
                } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_WIDTH)) {
                    postClientEvent(SET_COLUMN_WIDTH, getVisibleIndex(index), newValue);
                } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_ALIGN_X)) {
                    postClientEvent(SET_COLUMN_ALIGN_X, getVisibleIndex(index), ((AlignX)newValue).name().toLowerCase());
                } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_DISPLAY_FORMAT)) {
                    setColumn(column);
                }
            }
        } else {
            super.propertyChange(pce);
        }
        
    }

    public void itemChange(ItemChangeEvent ice) {
        ItemChangeEvent.Type type = ice.getType();
        CellPosition cp = (CellPosition) ice.getPosition();
        Integer rowIndex = new Integer(cp.getRowIndex());
        int columnIndex = cp.getColumnIndex();
        Object oldValue = ice.getOldValue();
        Object newValue = ice.getNewValue();

        if (rowIndex.intValue() == -1) { // Column Change
            GridBox.Column nco = (GridBox.Column) newValue;
            GridBox.Column oco = (GridBox.Column) oldValue;

            if (type == ItemChangeEvent.Type.REMOVE) {
                if (((Column) oldValue).isVisible()) removeColumn(new Integer(columnIndex));
                columnState.remove(new Integer(System.identityHashCode(oco)));
            } else if (type == ItemChangeEvent.Type.ADD) {
                columnState.add(new Integer(System.identityHashCode(nco)));
                if (nco.isVisible()) addColumn(nco);
            } else {
                columnState.remove(new Integer(System.identityHashCode(oco)));
                columnState.add(new Integer(System.identityHashCode(nco)));
                if (nco.isVisible()) setColumn(nco);
            }
        } else if (columnIndex == -1) { // Row Change
            GridBox.Row nro = (GridBox.Row) newValue;
            GridBox.Row oro = (GridBox.Row) oldValue;

            if (type == ItemChangeEvent.Type.ADD) {
                rowState.add(new Integer(System.identityHashCode(nro)));
                postClientEvent(ADD_ROW, rowIndex, getValues((List)newValue, gb.getColumns(), null).toString());
                GridBox gbc = nro.getChild();
                if (gbc != null) renderChild(rowIndex, gbc);
            } else if (type == ItemChangeEvent.Type.REMOVE) {
                rowState.remove(new Integer(System.identityHashCode(oro)));                
                postClientEvent(REMOVE_ROW, rowIndex);
                GridBox gbc = oro.getChild();
                if (gbc != null) ((GridBoxRenderer)childToRenderer.remove(gbc)).destroy();
            } else {
                rowState.remove(new Integer(System.identityHashCode(oro)));
                GridBox gbc = oro.getChild();               
                if (gbc != null) ((GridBoxRenderer)childToRenderer.remove(gbc)).destroy();
                rowState.add(new Integer(System.identityHashCode(nro)));
                postClientEvent(SET_ROW, rowIndex, getValues((List)newValue, gb.getColumns(), null).toString());
                gbc = nro.getChild();
                if (gbc != null) renderChild(rowIndex, gbc);
            }
        } else { // Cell Change
            if (gb.getColumns().get(columnIndex).isVisible()) {
                //Wrapping the outbound value in a StringBuffer is a hack to get around a second layer of back-slash escaping.
                postClientEvent(SET_CELL, getVisibleIndex(columnIndex), rowIndex,
                        new StringBuffer("\"" + getValue(newValue, gb.getColumns().get(columnIndex).getDisplayFormat())+ "\""));
            }
        }
    }    
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        String value = (String)event.getValue();

        if (name.equals(GridBox.Row.PROPERTY_ROW_SELECTED)) { 
            setPropertyChangeIgnored(name, true);
            ((Row)gb.getRows().get(Integer.parseInt(value))).setSelected(true);
            setPropertyChangeIgnored(name, false);
        } else if (name.equals(GridBox.Row.PROPERTY_ROW_CHECKED)) {
            setPropertyChangeIgnored(name, true);
            boolean state = value.charAt(0) == 't';
            int index = Integer.parseInt(value.substring(1));
            ((Row)gb.getRows().get(index)).setChecked(state);
            setPropertyChangeIgnored(name, false);            
        } else if(name.equals(GridBox.ACTION_CLICK)) {
            setPropertyChangeIgnored(GridBox.Row.PROPERTY_ROW_SELECTED, true);
            setPropertyChangeIgnored(GridBox.Row.PROPERTY_ROW_CHECKED, true);
            gb.fireAction(GridBox.ACTION_CLICK, (Row)gb.getRows().get(Integer.parseInt(value)));
            setPropertyChangeIgnored(GridBox.Row.PROPERTY_ROW_CHECKED, false);
            setPropertyChangeIgnored(GridBox.Row.PROPERTY_ROW_SELECTED, false);
        } else if (name.equals(VIEW_STATE_COLUMN_SORT)) {
            Column col = gb.getColumns().get(Integer.parseInt(value));
            col.setSortOrder(col.getSortOrder() == Column.SortOrder.ASC ? Column.SortOrder.DESC : Column.SortOrder.ASC); 
        } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_WIDTH)) {
            setPropertyChangeIgnored(GridBox.Column.PROPERTY_COLUMN_WIDTH, true);
            String[] values = value.split(",");
            gb.getColumns().get(Integer.parseInt(values[0])).setWidth(Integer.parseInt(values[1]));
            setPropertyChangeIgnored(GridBox.Column.PROPERTY_COLUMN_WIDTH, false);
        } else {
            super.componentChange(event);
        }
    }        

    private void renderChild(Integer rowIndex, GridBox gb) {
        if (childToRenderer == null) childToRenderer = new HashMap<GridBox, GridBoxRenderer>(5);
        GridBoxRenderer gbr = new GridBoxRenderer();
        childToRenderer.put(gb, gbr);
        gbr.render(wr, gb, this, rowIndex);
    }
    
    private void addColumn(Column c) {
        postClientEvent(ADD_COLUMN, new Object[] {
                new Integer(c.getIndex()),
                getValues(c, c.getDisplayFormat(), null).toString(),
                getValue(c.getDisplayName(), null), new Integer(c.getWidth()),
                ((AlignX) c.getAlignX()).name().toLowerCase() });
    }

    private void setColumn(Column c) {
        postClientEvent(SET_COLUMN, new Object[] {
                new Integer(c.getIndex()),
                getValues(c, c.getDisplayFormat(), null).toString(),
                getValue(c.getDisplayName(), null), new Integer(c.getWidth()),
                ((AlignX) c.getAlignX()).name().toLowerCase() });
    }

    private int getVisibleIndex(int index) {
        int visibleIndex = -1;
        List<GridBox.Column> cols = gb.getColumns();

        for (int i = 0; i <= index; i++) {
            if (cols.get(i).isVisible()) visibleIndex++;
        }
        
        return visibleIndex;
    }
    
    private void removeColumn(Integer index) {
        postClientEvent(REMOVE_COLUMN, index);
    }
    
    static String getValue(Object o, GridBox.Column.Format format) {
        if (o == null) return "";
        if (format != null) o = format.format(o);
        String s = o.toString(); 
        s = REGEX_DOUBLE_SLASH.matcher(s).replaceAll("\\\\\\\\");        
        s = REGEX_DOUBLE_QUOTE.matcher(s).replaceAll("\\\\\"");
        s = REGEX_CRLF.matcher(s).replaceAll(" ");                                    
        return s;
    }

    private static StringBuffer getValues(List<Object> l, Object formats, StringBuffer sb) {
        if (sb == null) sb = new StringBuffer();
        
        synchronized (sb) {             
            sb.append('[');
    
            if (formats instanceof List) {
                List<Column> formatList = (List)formats;
    
                for (int i = 0, cnt = l.size(); i < cnt; i++) {
                    Column.Format format;
                    boolean visible;
    
                    if (formatList.size() > 0) {
                        Column column = formatList.get(i);
                        format = column.getDisplayFormat();
                        column.getIndex();
                        visible = column.isVisible();
                    } else {
                        format = null;
                        visible = false;
                    }
    
                    if (visible) sb.append("\"").append(getValue(l.get(i), format)).append("\",");
                }
            } else {
                Column.Format format = (Column.Format)formats;
    
                for (int i = 0, cnt = l.size(); i < cnt; i++)
                    sb.append("\"").append(getValue(l.get(i), format)).append("\",");
            }
    
            if (sb.charAt(sb.length() - 1) != '[')
                sb.setCharAt(sb.length() - 1, ']');
            else
                sb.append(']');
        }

        return sb;
    }
    
    private StringBuffer getCheckedRowIndices(StringBuffer sb) {
        if (sb == null) sb = new StringBuffer();
        
        synchronized (sb) {
            sb.append(',');

            for (GridBox.Row r : gb.getCheckedRows()) {
                sb.append(r.getIndex()).append(',');                
            }
        }
        
        return sb;
    }
}
