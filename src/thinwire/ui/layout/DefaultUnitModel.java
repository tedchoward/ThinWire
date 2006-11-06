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
        if (position == null || position.length < 2 || position.length > 4) throw new IllegalArgumentException("position == null || position.length < 2 || position.length > 4");
        position[0] = c.getX();
        position[1] = c.getY();
    }

    public void getSize(Component c, int[] size) {
        if (size == null || size.length < 2 || size.length > 4) throw new IllegalArgumentException("size == null || size.length < 2 || size.length > 4");
        size[0] = c.getWidth();
        size[1] = c.getHeight();
    }

    public void setPosition(Component c, int x, int y) {
        c.setPosition(x, y);
    }

    public void setSize(Component c, int width, int height) {
        c.setSize(width, height);
    }

    public boolean isModelValid() {
        return true;
    }

}
