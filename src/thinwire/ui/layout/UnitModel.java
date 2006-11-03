package thinwire.ui.layout;

import thinwire.ui.*;

public interface UnitModel {
    
    public int getX(Component c);
    public void setX(Component c, int x);
    
    public int getY(Component c);
    public void setY(Component c, int y);
    
    public int getWidth(Component c);
    public void setWidth(Component c, int width);
    
    public int getHeight(Component c);
    public void setHeight(Component c, int height);
    
    public void getBounds(Component c, int[] bounds);
    public void setBounds(Component c, int x, int y, int width, int height);
    
    public void getSize(Component c, int[] size);
    public void setSize(Component c, int width, int height);
    
    public void getPosition(Component c, int[] position);
    public void setPosition(Component c, int x, int y);
    
    public int getActualX(int modelX);
    public int getActualY(int modelY);
    public int getActualWidth(int modelWidth);
    public int getActualHeight(int modelHeight);
    
    public void init(Container container);
    public Container getContainer();
    
    

    //Called by renderer when:
    //1. Component x, y, width, height changes
    //2. Container width, height changes
    public void apply();
}
