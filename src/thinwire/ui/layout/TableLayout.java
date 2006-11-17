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

import java.util.List;
import java.util.logging.Logger;

import thinwire.ui.Component;

/**
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public final class TableLayout extends AbstractLayout {
    private static final Logger log = Logger.getLogger(TableLayout.class.getName());
    
    public static class Limit {
        private int column;
        private int row;
        private int width;
        private int height;
        private String value;
        
        public Limit() {
            this(0, 0, 1, 1);
        }
        
        public Limit(String range) {
            String[] values = ((String)range).split("\\s*,\\s*");
            init(values.length >= 1 ? Integer.parseInt(values[0]) : 0,
                 values.length >= 2 ? Integer.parseInt(values[1]) : 0,
                 values.length >= 3 ? Integer.parseInt(values[2]) : 1,
                 values.length >= 4 ? Integer.parseInt(values[3]) : 1);
        }
        
        public Limit(int column, int row, int width, int height) {
            init(column, row, width, height);
        }

        private void init(int column, int row, int width, int height) {
            rangeCheck("column", column);            
            rangeCheck("row", row);
            rangeCheck("width", width);
            rangeCheck("height", height);
            this.column = column;
            this.row = row;
            this.width = width;
            this.height = height;
            this.value = column + ", " + row + ", " + width + ", " + height;
        }
        
        private void rangeCheck(String name, int value) {
            if (value < 0 || value > Short.MAX_VALUE) throw new IllegalArgumentException(Limit.class.getName() + "." + name + " < 0 || " + Limit.class.getName() + "." + name + " > " + Short.MAX_VALUE);
        }
        
        public int getColumn() {
            return column;
        }
        
        public int getRow() {
            return row;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public boolean equals(Object o) {
            return o instanceof Limit && toString().equals(o.toString());
        }
        
        public int hashCode() {
            return toString().hashCode();
        }
        
        public String toString() {
            return value;
        }
    }
    
    private static final Limit DEFAULT_LIMIT = new Limit();
    
    private double[] widths;
    private double[] heights;
    protected int spacing;

    public TableLayout(double widths[], double[] heights) {
        this(widths, heights, 0);
    }

    public TableLayout(double widths[], double[] heights, int spacing) {
        super(Component.PROPERTY_LIMIT);
        if (widths == null || widths.length == 0) throw new IllegalArgumentException("widths == null || widths.length == 0");
        if (heights == null || heights.length == 0) throw new IllegalArgumentException("heights == null || heights.length == 0");
        if (spacing < 0 || spacing > Short.MAX_VALUE) throw new IllegalArgumentException("spacing < 0 || spacing > " + Short.MAX_VALUE);
        this.widths = widths;
        this.heights = heights;
        this.spacing = spacing;
        setAutoLayout(true);
    }
    
    protected Object getFormalLimit(Component comp) {
        Object oLimit = comp.getLimit();
        
        if (oLimit instanceof String) {
            return new Limit((String)oLimit);
        } else if (oLimit instanceof Limit) {
            return (Limit)oLimit;
        } else {
            return null;
        }
    }
    
    private int[] getAbsoluteSizes(int availableSize, double[] sizes) {
        int[] absoluteSizes = new int[sizes.length];
        availableSize -= (sizes.length - 1) * spacing;
        int fillCnt = 0;

        for (double f : sizes) { 
            if (f >= 1) availableSize -= f;
        }
        
        int pctSize = availableSize;

        for (int i = 0, cnt = sizes.length; i < cnt; i++) {
            double size = sizes[i];
            
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
            
            for (int i = 0, cnt = sizes.length; i < cnt; i++) {
                if (sizes[i] == 0) {
                    absoluteSizes[i] = fillSize;
                    lastIndex = i;
                }
            }
            
            if (lastIndex != -1) absoluteSizes[lastIndex] += availableSize % fillCnt;
        }
        
        return absoluteSizes;
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
        int[] absoluteWidths = getAbsoluteSizes(container.getInnerWidth(), widths);
        int[] absoluteHeights = getAbsoluteSizes(container.getInnerHeight(), heights);

        for (Component c : (List<Component>) container.getChildren()) {
            Limit limit = (Limit)c.getLimit();
            if (limit == null) limit = DEFAULT_LIMIT;
            int x = 0, y = 0, width = 0, height = 0;
            
            for (int i = 0, cnt = limit.getColumn(); i < cnt; i++) {
                x += absoluteWidths[i] + spacing;
            }
            
            for (int i = 0, cnt = limit.getRow(); i < cnt; i++) {
                y += absoluteHeights[i] + spacing;
            }
            
            for (int i = 0, cnt = limit.width, column = limit.column; i < cnt; i++) {
                width += absoluteWidths[i + column] + spacing;
            }
            
            width -= spacing;
            
            for (int i = 0, cnt = limit.height, row = limit.row; i < cnt; i++) {
                height += absoluteHeights[i + row] + spacing;
            }
            
            height -= spacing;
            if (width >= 0 && height >= 0) c.setBounds(x, y, width, height);
        }
    }
}
