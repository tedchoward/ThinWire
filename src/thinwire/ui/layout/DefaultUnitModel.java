package thinwire.ui.layout;

import thinwire.ui.Component;
import thinwire.ui.Container;

public class DefaultUnitModel implements UnitModel {
    private Container container;

    public void apply() {
    }

    public void getBounds(Component c, int[] bounds) {
        if (bounds == null || bounds.length != 4) throw new IllegalArgumentException("bounds == null || bounds.length != 4");
        bounds[0] = c.getX();
        bounds[1] = c.getY();
        bounds[2] = c.getWidth();
        bounds[3] = c.getHeight();
    }

    public Container getContainer() {
        return container;
    }

    public void init(Container container) {
        if (this.container != null) throw new IllegalStateException("this.container != null");
        this.container = container;
    }

    public void setBounds(Component c, int x, int y, int width, int height) {
        c.setBounds(x, y, width, height);
    }

    public int getActualHeight(int modelHeight) {
        return modelHeight;
    }

    public int getActualWidth(int modelWidth) {
        return modelWidth;
    }

    public int getActualX(int modelX) {
        return modelX;
    }

    public int getActualY(int modelY) {
        return modelY;
    }

    public int getHeight(Component c) {
        return c.getHeight();
    }

    public int getWidth(Component c) {
        return c.getWidth();
    }

    public int getX(Component c) {
        return c.getX();
    }

    public int getY(Component c) {
        return c.getY();
    }

    public void setHeight(Component c, int height) {
        c.setHeight(height);
    }

    public void setWidth(Component c, int width) {
        c.setWidth(width);
    }

    public void setX(Component c, int x) {
        c.setX(x);
    }

    public void setY(Component c, int y) {
        c.setY(y);
    }

    public void getPosition(Component c, int[] position) {
        if (position == null || position.length >= 2) throw new IllegalArgumentException("position == null || position.length >= 2");
        position[0] = c.getX();
        position[1] = c.getY();
    }

    public void getSize(Component c, int[] size) {
        if (size == null || size.length >= 2) throw new IllegalArgumentException("size == null || size.length >= 2");
        size[0] = c.getWidth();
        size[1] = c.getHeight();
    }

    public void setPosition(Component c, int x, int y) {
        c.setPosition(x, y);
    }

    public void setSize(Component c, int width, int height) {
        c.setSize(width, height);
    }

}
