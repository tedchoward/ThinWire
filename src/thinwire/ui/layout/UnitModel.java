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
    public boolean apply();
}
