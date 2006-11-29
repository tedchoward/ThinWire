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
import thinwire.ui.layout.TableLayout.Limit.Justify;

/**
 * @author Joshua J. Gertzen
 * @author Ted C. Howard
 */
public final class TableLayout extends AbstractLayout {
    private static final Logger log = Logger.getLogger(TableLayout.class.getName());
    
    public static class Limit {
        
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
        
        private int column;
        private int row;
        private int width;
        private int height;
        private String value;
        private Justify justification;
        
        public Limit() {
            this(0, 0, 1, 1, Justify.FULL);
        }
        
        public Limit(String range) {
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
        
        public Limit(int column, int row, int width, int height, Justify justification) {
            init(column, row, width, height, justification);
        }

        private void init(int column, int row, int width, int height, Justify justification) {
            rangeCheck("column", column);            
            rangeCheck("row", row);
            rangeCheck("width", width);
            rangeCheck("height", height);
            this.column = column;
            this.row = row;
            this.width = width;
            this.height = height;
            this.value = getClass().getName() + "(" + column + ", " + row + ", " + width + ", " + height + ")";
            this.justification = justification;
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
        
        public Justify getJustification() {
            return justification;
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
    private int margin;
    private int spacing;

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
        widths = sizes[0];
        heights = sizes[1];
        setSpacing(spacing);
        setMargin(margin);
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
        availableSize -= (sizes.length - 1) * spacing + margin * 2;
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
        int[] absoluteWidths = getAbsoluteSizes(container.getInnerWidth(), widths);
        int[] absoluteHeights = getAbsoluteSizes(container.getInnerHeight(), heights);

        for (Component c : (List<Component>) container.getChildren()) {
            Limit limit = (Limit)c.getLimit();
            if (limit == null) limit = DEFAULT_LIMIT;
            Justify just = limit.getJustification();
            int x = margin, y = margin, width = 0, height = 0;
            
            for (int i = 0, cnt = limit.width, column = limit.column; i < cnt; i++) {
                width += absoluteWidths[i + column] + spacing;
            }
            
            width -= spacing;
            
            if (just != Justify.FULL && c.getWidth() < width) width = c.getWidth();
            
            for (int i = 0, cnt = limit.height, row = limit.row; i < cnt; i++) {
                height += absoluteHeights[i + row] + spacing;
            }
            
            height -= spacing;
            
            if (just != Justify.FULL && c.getHeight() < height) height = c.getHeight();
            
            for (int i = 0, cnt = limit.column; i < cnt; i++) {
                x += absoluteWidths[i] + spacing;
            }
            
            if (width == c.getWidth()) {
                if (just.name().indexOf("RIGHT") > -1) {
                    x += absoluteWidths[limit.column] - width;
                } else if (just == Justify.CENTER || just == Justify.CENTER_BOTTOM || just == Justify.CENTER_FULL 
                    || just == Justify.CENTER_TOP) {
                    x += (absoluteWidths[limit.column] / 2) - (width / 2);
                }
            }
            
            for (int i = 0, cnt = limit.row; i < cnt; i++) {
                y += absoluteHeights[i] + spacing;
            }
            
            if (height == c.getHeight()) {
                if (just.name().indexOf("BOTTOM") > -1) {
                    y += absoluteHeights[limit.row] - height;
                } else if (just == Justify.CENTER || just == Justify.FULL_CENTER || just == Justify.LEFT_CENTER 
                    || just == Justify.RIGHT_CENTER) {
                    y += (absoluteHeights[limit.row] / 2) - (height / 2);
                }
            }
            
            if (width >= 0 && height >= 0) c.setBounds(x, y, width, height);
        }
    }
}
