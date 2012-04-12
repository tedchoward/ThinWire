/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package thinwire.render.web;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;

import thinwire.ui.AlignTextComponent.AlignX;
import thinwire.ui.Application;
import thinwire.ui.GridBox;
import thinwire.ui.Component;
import thinwire.ui.DropDownGridBox.DefaultView;
import thinwire.ui.GridBox.Range;
import thinwire.ui.GridBox.Column;
import thinwire.ui.GridBox.Row;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.style.FX;

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
    private static final String SET_COLUMN_SORT_ORDER = "setColumnSortOrder";
    private static final String ADD_ROW = "addRow";
    private static final String REMOVE_ROW = "removeRow";
    private static final String SET_ROW = "setRow";
    private static final String CLEAR_ROWS = "clearRows";
    private static final String SET_CELL = "setCell";
    private static final String ADD_COLUMN = "addColumn";
    private static final String SET_COLUMN = "setColumn";
    private static final String REMOVE_COLUMN = "removeColumn";
    private static final String VIEW_STATE_COLUMN_SORT = "columnSort";
    private static final String SET_SORT_ALLOWED = "setSortAllowed";

    private Set<Integer> rowState = new HashSet<Integer>();
    private Set<Integer> columnState = new HashSet<Integer>();
    private GridBox gb;
    private Map<GridBox, GridBoxRenderer> childToRenderer;
    private int autoColumnWidth;
    
    @Override
    void init(String jsClass, WindowRenderer wr, Component comp, ComponentRenderer container) {
    	super.init(jsClass, wr, comp, container);
    	rowState.clear();
    	columnState.clear();
    }
    
    //TODO: Column indexes on the client side may differ if there is a hidden column between two visible columns
    void render(WindowRenderer wr, Component c, ComponentRenderer container) {
        render(wr, c, container, null);
    }
    
    private void render(WindowRenderer wr, Component c, ComponentRenderer container, Integer parentIndex) {
        init(GRIDBOX_CLASS, wr, c, container);
        gb = (GridBox)c;       

        this.wr = wr;
        gb.addItemChangeListener(this);
        
        if (container instanceof GridBoxRenderer) {
            //a gridbox for a dropdown does not support the focus, enabled, x or y properties
            setPropertyChangeIgnored(Component.PROPERTY_FOCUS, true);
            setPropertyChangeIgnored(Component.PROPERTY_ENABLED, true);
            setPropertyChangeIgnored(Component.PROPERTY_VISIBLE, true);
            setPropertyChangeIgnored(Component.PROPERTY_X, true);
            setPropertyChangeIgnored(Component.PROPERTY_Y, true);
            WebApplication app = (WebApplication) Application.current();
            DefaultView v = (DefaultView) app.setPackagePrivateMember("initDDGBView", null, gb);
            if (gb.getWidth() == 0) gb.setWidth(v.getOptimalWidth());
            if (gb.getHeight() == 0) gb.setHeight(v.getOptimalHeight());
        }
        
        calcAutoColumnWidth();
        StringBuilder colDefs = new StringBuilder();
        
        List<Column> columns = gb.getColumns();
        colDefs.append('[');
        
        for (int i = 0, cnt = columns.size(); i < cnt; i++) {
            Column col = (Column)columns.get(i);
            columnState.add(System.identityHashCode(col));        

            if (col.isVisible()) {
                colDefs.append("{v:");
                getValues(col, col.getDisplayFormat(), colDefs);
                colDefs.append(",n:");
                getValue(this, col.getHeader().getText(), null, colDefs);
                int width = col.getWidth();
                if (width == -1) width = autoColumnWidth;
                colDefs.append(",w:").append(width);
                colDefs.append(",a:\"").append(((AlignX)col.getAlignX()).name().toLowerCase()).append('"');
                colDefs.append(",s:").append(getSortOrderId(col.getSortOrder()));
                colDefs.append("},");
            }
        }

        int len = colDefs.length();
        
        if (len == 1)
            colDefs.append(']');
        else
            colDefs.setCharAt(len - 1, ']');

        addClientSideProperty(GridBox.Row.PROPERTY_ROW_CHECKED);
        addClientSideProperty(GridBox.Row.PROPERTY_ROW_SELECTED);
        addClientSideProperty(GridBox.Column.PROPERTY_COLUMN_WIDTH);
        addInitProperty(GridBox.PROPERTY_VISIBLE_HEADER, gb.isVisibleHeader());
        addInitProperty(GridBox.PROPERTY_VISIBLE_CHECK_BOXES, gb.isVisibleCheckBoxes());
        addInitProperty(GridBox.PROPERTY_FULL_ROW_CHECK_BOX, gb.isFullRowCheckBox());
        addInitProperty(GridBox.PROPERTY_SORT_ALLOWED, gb.isSortAllowed());
        addInitProperty("parentIndex", parentIndex);
        Row sr = gb.getSelectedRow();
        addInitProperty("selectedRow", sr == null || colDefs.length() == 2 ? -1 : sr.getIndex());
        if (gb.isVisibleCheckBoxes()) addInitProperty("checkedRows", getCheckedRowIndices());
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
    
    private void calcAutoColumnWidth() {
        int totalFixedWidth = 0;
        int countAutoSize = 0;
        
        for (GridBox.Column c : gb.getColumns()) {
            if (c.isVisible()) {
                int width = c.getWidth();
                
                if (width == -1) {
                    countAutoSize++;
                } else {
                    totalFixedWidth += width;
                }
            }
        }
        
        if (countAutoSize > 0) {
            //TODO: 21 is the scroll bar width, ideally this shouldn't be hard coded like this
            int childColumnWidth = gb.getRowsWithChildren().size() > 0 ? 12 : 0;
            autoColumnWidth = (int)Math.floor((gb.getWidth() - gb.getStyle().getBorder().getSize() - totalFixedWidth - 21 - childColumnWidth) / countAutoSize);
            if (autoColumnWidth < 0) autoColumnWidth = 0;
        } else {
            autoColumnWidth = 0;
        }
    }

    void destroy() {
        super.destroy();
        gb.removeItemChangeListener(this);
        gb = null;
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
                postClientEvent(SET_VISIBLE_CHECK_BOXES, newValue, newValue == Boolean.TRUE ? getCheckedRowIndices() : null);
            } else if (name.equals(GridBox.PROPERTY_FULL_ROW_CHECK_BOX)) {
                postClientEvent(SET_FULL_ROW_CHECK_BOX, newValue);
            } else if (name.equals(Component.PROPERTY_WIDTH)) {
                Integer width = (Integer)newValue;
                setPropertyWithEffect(Component.PROPERTY_WIDTH, width, pce.getOldValue(), SET_WIDTH, FX.PROPERTY_FX_SIZE_CHANGE);
                calcAutoColumnWidth();
                sendAutoColumnWidths(null);
            } else if (name.equals(Component.PROPERTY_HEIGHT)) {
                Integer height = (Integer)newValue;
                setPropertyWithEffect(Component.PROPERTY_HEIGHT, height, pce.getOldValue(), SET_HEIGHT, FX.PROPERTY_FX_SIZE_CHANGE);
            } else if (name.equals(GridBox.PROPERTY_SORT_ALLOWED)) {
            	postClientEvent(SET_SORT_ALLOWED, newValue);
            } else {
                super.propertyChange(pce);
            }
        } else if (source instanceof Row) {
            if (isPropertyChangeIgnored(name) || !rowState.contains(new Integer(System.identityHashCode(source)))) return;
            
            if (name.equals(GridBox.Row.PROPERTY_ROW_SELECTED)) {
                if (newValue == Boolean.TRUE) {
                    postClientEvent(SET_ROW_INDEX_SELECTED, ((Row)source).getIndex());
                }
            } else if (name.equals(GridBox.Row.PROPERTY_ROW_CHECKED)) {
                postClientEvent(SET_ROW_INDEX_CHECK_STATE, new Integer(((Row)source).getIndex()), newValue);
            } else if (name.equals(GridBox.Row.PROPERTY_ROW_CHILD)) {
                if (newValue != null) {
                    if (oldValue != null) ((GridBoxRenderer)childToRenderer.remove(oldValue)).destroy();
                    renderChild(new Integer(((Row)source).getIndex()), (GridBox)newValue);
                } else if (oldValue != null) {
                    GridBoxRenderer gbrChild = (GridBoxRenderer)childToRenderer.remove(oldValue);
                    gbrChild.postClientEvent(DESTROY);
                    gbrChild.destroy();
                }
            }
        } else if (source instanceof Column) {
            if (isPropertyChangeIgnored(name) || !columnState.contains(new Integer(System.identityHashCode(source)))) return;
            Column column = (Column)source;
            int index = column.getIndex();

            if (name.equals(GridBox.Column.PROPERTY_COLUMN_VISIBLE)) {
                if (((Boolean)newValue).booleanValue()) {
                    sendColumn(ADD_COLUMN, column);
                } else {
                    removeColumn(index);
                }
            } else if (column.isVisible()) {   
                if (name.equals(GridBox.Column.PROPERTY_COLUMN_NAME) && ((String)newValue).equals(column.getHeader().getText())) {
                    postClientEvent(SET_COLUMN_NAME, getVisibleIndex(index), getValue(this, (String)newValue, null, null));
                } else if (name.equals(GridBox.Header.PROPERTY_HEADER_TEXT)) {
                    postClientEvent(SET_COLUMN_NAME, getVisibleIndex(index), getValue(this, (String)newValue, null, null));                    
                } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_WIDTH)) {
                    if ((Integer) newValue == -1) {
                        calcAutoColumnWidth();
                        newValue = autoColumnWidth;
                    }
                    postClientEvent(SET_COLUMN_WIDTH, getVisibleIndex(index), newValue);
                } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_ALIGN_X)) {
                    postClientEvent(SET_COLUMN_ALIGN_X, getVisibleIndex(index), ((AlignX)newValue).name().toLowerCase());
                } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_SORT_ORDER)) {
                    postClientEvent(SET_COLUMN_SORT_ORDER, getVisibleIndex(index), getSortOrderId((Column.SortOrder)newValue));
                } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_DISPLAY_FORMAT)) {
                    sendColumn(SET_COLUMN, column);
                }
            }
        } else {
            super.propertyChange(pce);
        }
        
    }
    
    int getSortOrderId(Column.SortOrder sort) {
        int order = 0;
        if (sort == Column.SortOrder.ASC) order = 1;
        else if (sort == Column.SortOrder.DESC) order = 2;
        return order;
    }

    public void itemChange(ItemChangeEvent ice) {
        ItemChangeEvent.Type type = ice.getType();
        Range cp = (Range) ice.getPosition();
        int rowIndex = cp.getRowIndex();
        int columnIndex = cp.getColumnIndex();
        Object oldValue = ice.getOldValue();
        Object newValue = ice.getNewValue();

        if (rowIndex == -1) { // Column Change
            GridBox.Column nco = (GridBox.Column) newValue;
            GridBox.Column oco = (GridBox.Column) oldValue;

            if (type == ItemChangeEvent.Type.REMOVE) {
                if (((Column) oldValue).isVisible()) removeColumn(columnIndex);
                columnState.remove(new Integer(System.identityHashCode(oco)));
            } else if (type == ItemChangeEvent.Type.ADD) {
                columnState.add(new Integer(System.identityHashCode(nco)));
                if (nco.isVisible()) sendColumn(ADD_COLUMN, nco);
            } else {
                columnState.remove(new Integer(System.identityHashCode(oco)));
                columnState.add(new Integer(System.identityHashCode(nco)));
                if (nco.isVisible()) sendColumn(SET_COLUMN, nco);
            }
        } else if (columnIndex == -1) { // Row Change
            GridBox.Row nro = (GridBox.Row) newValue;
            GridBox.Row oro = (GridBox.Row) oldValue;

            if (type == ItemChangeEvent.Type.ADD) {
                rowState.add(new Integer(System.identityHashCode(nro)));
                
                if (nro.size() > 0) {
                    postClientEvent(ADD_ROW, rowIndex, getValues(nro, gb.getColumns(), null).toString(), nro.isChecked() ? 1 : 0, nro.isSelected() ? 1 : 0);
                    GridBox gbc = nro.getChild();
                    if (gbc != null) renderChild(rowIndex, gbc);
                }
            } else if (type == ItemChangeEvent.Type.REMOVE) {
                if (gb.getRows().size() == 0 && rowState.size() > 0) { //Clear was called
                    rowState.clear();
                    postClientEvent(CLEAR_ROWS);

                    if (childToRenderer != null && childToRenderer.size() > 0) {
                        GridBoxRenderer[] children = childToRenderer.values().toArray(new GridBoxRenderer[childToRenderer.size()]);
                        childToRenderer.clear();
                        
                        for (GridBoxRenderer gbr : children) {
                            gbr.destroy();
                        }
                    }
                } else if (rowState.size() > 0) { //Don't process residual clear() row removals. 
                    rowState.remove(new Integer(System.identityHashCode(oro)));                
                    postClientEvent(REMOVE_ROW, rowIndex);
                    GridBox gbc = oro.getChild();
                    if (gbc != null) ((GridBoxRenderer)childToRenderer.remove(gbc)).destroy();
                }
            } else {
                rowState.remove(new Integer(System.identityHashCode(oro)));
                GridBox gbc = oro.getChild();               
                if (gbc != null) ((GridBoxRenderer)childToRenderer.remove(gbc)).destroy();
                rowState.add(new Integer(System.identityHashCode(nro)));
                postClientEvent(SET_ROW, rowIndex, getValues(nro, gb.getColumns(), null).toString(), nro.isChecked() ? 1 : 0, nro.isSelected() ? 1 : 0);
                gbc = nro.getChild();
                if (gbc != null) renderChild(rowIndex, gbc);
            }
        } else { // Cell Change
        	GridBox.Column col = gb.getColumns().get(columnIndex);
        	
            if (col.isVisible()) {
                //XXX Wrapping the outbound value in a StringBuilder is a hack to get around a second layer of back-slash escaping.                
            	Object textValue = getValue(this, newValue, col.getDisplayFormat(), null);
                
            	if (textValue instanceof String) {
                	StringBuilder sb = new StringBuilder();
                	sb.append('"');
                	sb.append(textValue);
                	sb.append('"');
                	textValue = sb;
                }
            	
                postClientEvent(SET_CELL, getVisibleIndex(columnIndex), rowIndex, textValue);
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
        } else if (name.equals(VIEW_STATE_COLUMN_SORT)) {
            Column col = gb.getColumns().get(getRealIndex(Integer.parseInt(value)));
            col.setSortOrder(col.getSortOrder() == Column.SortOrder.ASC ? Column.SortOrder.DESC : Column.SortOrder.ASC); 
            if (gb.getSelectedRow() != null) postClientEvent(SET_ROW_INDEX_SELECTED, gb.getSelectedRow().getIndex(), Boolean.FALSE);
        } else if (name.equals(GridBox.Column.PROPERTY_COLUMN_WIDTH)) {
            setPropertyChangeIgnored(GridBox.Column.PROPERTY_COLUMN_WIDTH, true);
            String[] values = value.split(",");
            GridBox.Column col = gb.getColumns().get(getRealIndex(Integer.parseInt(values[0])));
            if (col.getWidth() != -1) col.setWidth(Integer.parseInt(values[1]));
            setPropertyChangeIgnored(GridBox.Column.PROPERTY_COLUMN_WIDTH, false);
        } else {
            super.componentChange(event);
        }
    }        

    private void renderChild(Integer rowIndex, GridBox gb) {
        if (childToRenderer == null) childToRenderer = new HashMap<GridBox, GridBoxRenderer>(5);
        GridBoxRenderer gbr = childToRenderer.get(gb);
        if (gbr == null) childToRenderer.put(gb, gbr = new GridBoxRenderer());
        gbr.render(wr, gb, this, rowIndex);
    }
    
    private void sendColumn(String method, Column c) {
        calcAutoColumnWidth();        
        int width = c.getWidth();
        if (width == -1) width = autoColumnWidth;
        
        postClientEvent(method, new Object[] {
                getVisibleIndex(c.getIndex()),
                getValues(c, c.getDisplayFormat(), null).toString(),
                getValue(this, c.getHeader().getText(), null, null), width,
                ((AlignX) c.getAlignX()).name().toLowerCase(),
                getSortOrderId(c.getSortOrder())});
        
        sendAutoColumnWidths(c);
    }

    private void sendAutoColumnWidths(Column exclude) {
        if (autoColumnWidth > 0) {
            List<GridBox.Column> lst = gb.getColumns();
            
            for (int i = 0, cnt = lst.size(); i < cnt; i++) {
                GridBox.Column c = lst.get(i);
    
                if (c != exclude && c.isVisible() && c.getWidth() == -1) {
                    postClientEvent(SET_COLUMN_WIDTH, getVisibleIndex(i), autoColumnWidth);
                }
            }
        }
    }

    private void removeColumn(Integer index) {
        postClientEvent(REMOVE_COLUMN, getVisibleIndex(index));
        calcAutoColumnWidth();
        sendAutoColumnWidths(null);
    }    
    
    private int getVisibleIndex(int index) {
        int visibleIndex = -1;
        List<GridBox.Column> cols = gb.getColumns();

        for (int i = 0; i <= index; i++) {
            if (i == index || cols.get(i).isVisible()) visibleIndex++;
        }
        
        return visibleIndex;
    }
    
    private int getRealIndex(int index) {
        int realIndex = index;
        List<GridBox.Column> cols = gb.getColumns();
        for (int i = 0; i < realIndex; i++) if (!cols.get(i).isVisible()) realIndex++;
        while(!cols.get(realIndex).isVisible()) realIndex++;
        return realIndex;
    }
        
    static Object getValue(ComponentRenderer cr, Object o, GridBox.Column.Format format, StringBuilder sb) {
        if (o == null) o = "";
        if (format != null) o = format.format(o);
        o = cr.parseRichText(o.toString());
      
        if (o instanceof String) {
        	o = getEscapedText((String)o, true);
        	if (sb != null) sb.append('"').append((String)o).append('"');
    		return o;
        } else {
        	if (sb != null) sb.append(o);
        	return o;
        }
    }

    //TODO: I made this not static, is that okay?
    private StringBuilder getValues(List<Object> l, Object formats, StringBuilder sb) {
        if (sb == null) sb = new StringBuilder();
        
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

                if (visible) {
                	getValue(this, l.get(i), format, sb);
                    sb.append(',');
                }
            }
        } else {
            Column.Format format = (Column.Format)formats;

            for (int i = 0, cnt = l.size(); i < cnt; i++) {
            	getValue(this, l.get(i), format, sb);
                sb.append(',');
            }
        }

        if (sb.charAt(sb.length() - 1) != '[')
            sb.setCharAt(sb.length() - 1, ']');
        else
            sb.append(']');

        return sb;
    }
    
    private String getCheckedRowIndices() {
        StringBuilder sb = new StringBuilder();
        sb.append(',');

        for (GridBox.Row r : gb.getCheckedRows()) {
            sb.append(r.getIndex()).append(',');                
        }
        
        return sb.toString();
    }
}
