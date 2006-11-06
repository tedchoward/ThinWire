package thinwire.ui.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import thinwire.ui.Component;
import thinwire.ui.Container;
import thinwire.ui.Window;
import thinwire.ui.event.ItemChangeEvent;
import thinwire.ui.event.ItemChangeListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

public class TableUnitModel implements UnitModel {
    private class TableUnitModelConstraint {
        int x;
        int y;
        int width;
        int height;
    }
    
    private static final Logger log = Logger.getLogger(TableUnitModel.class.getName());
    
    private final Map<Component, TableUnitModelConstraint> componentMap = new HashMap<Component, TableUnitModelConstraint>();
    
    private Container container;
    private double[] widths;
    private double[] heights;
    private int columnSpace;
    private int rowSpace;
    private boolean modelValid;
    
    public void init(Container container) {
        if (this.container != null) throw new IllegalStateException("this.container != null");
        this.container = container;
        modelValid = false;
        if (this.container instanceof Window) {
            this.container.addPropertyChangeListener(new String[] {Window.PROPERTY_WIDTH, Window.PROPERTY_HEIGHT}, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    // Empty Listener makes sure size change properties are fired back up to the server
                    modelValid = false;
                }
            });
        }
        this.container.addItemChangeListener(new ItemChangeListener() {
            public void itemChange(ItemChangeEvent ev) {
                modelValid = false;
            }
        });
    }
    
    public void setWidths(double...widths) {
        this.widths = widths;
    }
    
    public void setHeights(double...heights) {
        this.heights = heights;
    }
    
    public void setColumnSpace(int columnSpace) {
        this.columnSpace = columnSpace;
    }
    
    public void setRowSpace(int rowSpace) {
        this.rowSpace = rowSpace;
    }
    
    private double[] getAbsoluteWidths() {
        double[] absoluteWidths = new double[widths.length];
        System.arraycopy(widths, 0, absoluteWidths, 0, widths.length);
        int availableWidth = container.getInnerWidth();
        availableWidth -= (widths.length - 1) * columnSpace;
        int fillCnt = 0;
        for (double f : widths) if (f >= 1) availableWidth -= f;
        int pctWidth = availableWidth;
        for (int i = 0, cnt = widths.length; i < cnt; i++) {
            if (widths[i] < 1 && widths[i] > 0) {
                absoluteWidths[i] = pctWidth * widths[i];
                availableWidth -= absoluteWidths[i];
            } else if (widths[i] == 0) {
                fillCnt++;
            }
        }
        if (fillCnt > 0) {
            int fillWidth = availableWidth / fillCnt;
            for (int i = 0, cnt = widths.length; i < cnt; i++) if (widths[i] == 0) absoluteWidths[i] = fillWidth;
        }
        return absoluteWidths;
    }
    
    private double[] getAbsoluteHeights() {
        double[] absoluteHeights = new double[heights.length];
        System.arraycopy(heights, 0, absoluteHeights, 0, heights.length);
        int availableHeight = container.getInnerHeight();
        availableHeight -= (heights.length - 1) * rowSpace;
        int fillCnt = 0;
        for (double f : heights) if (f >= 1) availableHeight -= f;
        int pctHeight = availableHeight;
        for (int i = 0, cnt = heights.length; i < cnt; i++) {
            if (heights[i] < 1 && heights[i] > 0) {
                absoluteHeights[i] = pctHeight * heights[i];
                availableHeight -= absoluteHeights[i];
            } else if (heights[i] == 0) {
                fillCnt++;
            }
        }
        if (fillCnt > 0) {
            int fillHeight = availableHeight / fillCnt;
            for (int i = 0, cnt = heights.length; i < cnt; i++) if (heights[i] == 0) absoluteHeights[i] = fillHeight;
        }
        return absoluteHeights;
    }

    public void apply() {
        if (modelValid) return;
        double[] absoluteWidths = getAbsoluteWidths();
        double[] absoluteHeights = getAbsoluteHeights();
        for (Component c : (List<Component>) container.getChildren()) {
            TableUnitModelConstraint tumc = componentMap.get(c);
            if (tumc == null) {
                tumc = new TableUnitModelConstraint();
                componentMap.put(c, tumc);
            }
            int actual = 0;
            for (int i = 0, cnt = c.getX(); i < cnt; i++) actual += absoluteWidths[i] + columnSpace;
            tumc.x = actual;
            actual = 0;
            for (int i = 0, cnt = c.getY(); i < cnt; i++) actual += absoluteHeights[i] + rowSpace;
            tumc.y = actual;
            actual = 0;
            for (int i = 0, cnt = c.getWidth(), x = c.getX(); i < cnt; i++) actual += absoluteWidths[i + x] + columnSpace;
            tumc.width = actual - columnSpace;
            actual = 0;
            for (int i = 0, cnt = c.getHeight(), y = c.getY(); i < cnt; i++) actual += absoluteHeights[i + y] + rowSpace;
            tumc.height = actual - rowSpace;
        }
        modelValid = true;
    }
    
    public boolean isModelValid() {
        return modelValid;
    }

    public int getActualHeight(int modelHeight) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getActualWidth(int modelWidth) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getActualX(int modelX) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getActualY(int modelY) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void getBounds(Component c, int[] bounds) {
        if (bounds == null || bounds.length != 4) throw new IllegalArgumentException("bounds == null || bounds.length != 4");
        TableUnitModelConstraint tumc = componentMap.get(c);
        bounds[0] = tumc.x;
        bounds[1] = tumc.y;
        bounds[2] = tumc.width;
        bounds[3] = tumc.height;
    }

    public Container getContainer() {
        return container;
    }

    public int getHeight(Component c) {
        return componentMap.get(c).height;
    }

    public void getPosition(Component c, int[] position) {
        if (position == null || position.length < 2 || position.length > 4) throw new IllegalArgumentException("position == null || position.length < 2 || position.length > 4");
        TableUnitModelConstraint tumc = componentMap.get(c);
        position[0] = tumc.x;
        position[1] = tumc.y;
    }

    public void getSize(Component c, int[] size) {
        if (size == null || size.length < 2 || size.length > 4) throw new IllegalArgumentException("size == null || size.length < 2 || size.length > 4");
        TableUnitModelConstraint tumc = componentMap.get(c);
        size[0] = tumc.width;
        size[1] = tumc.height;
    }

    public int getWidth(Component c) {
        return componentMap.get(c).width;
    }

    public int getX(Component c) {
        return componentMap.get(c).x;
    }

    public int getY(Component c) {
        return componentMap.get(c).y;
    }

    

    public void setBounds(Component c, int x, int y, int width, int height) {
        // TODO Auto-generated method stub

    }

    public void setHeight(Component c, int height) {
        // TODO Auto-generated method stub

    }

    public void setPosition(Component c, int x, int y) {
        // TODO Auto-generated method stub

    }

    public void setSize(Component c, int width, int height) {
        // TODO Auto-generated method stub

    }

    public void setWidth(Component c, int width) {
        // TODO Auto-generated method stub

    }

    public void setX(Component c, int x) {
        // TODO Auto-generated method stub

    }

    public void setY(Component c, int y) {
        // TODO Auto-generated method stub

    }

}
