/*
                           ThinWire(R) Ajax RIA Framework
                        Copyright (C) 2003-2008 ThinWire LLC

  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option) any
  later version.

  This library is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License along
  with this library; if not, write to the Free Software Foundation, Inc., 59
  Temple Place, Suite 330, Boston, MA 02111-1307 USA

  Users who would rather have a commercial license, warranty or support should
  contact the following company who supports the technology:
  
            ThinWire LLC, 5919 Greenville #335, Dallas, TX 75206-1906
   	            email: info@thinwire.com    ph: +1 (214) 295-4859
 	                        http://www.thinwire.com

#VERSION_HEADER#
*/
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
@SuppressWarnings("unchecked")
abstract class AbstractWindow<C extends Window> extends AbstractContainer<C, Component> implements Window {
    static final int MENU_BAR_HEIGHT = 23;
    
    String title = "";
    Menu menu;

    AbstractWindow() {
        super.setVisible(false);
    }   

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        String oldTitle = this.title;
        title = title == null ? "" : title;
        this.title = title;
        firePropertyChange(this, PROPERTY_TITLE, oldTitle, title);
    }   
    
    public Menu getMenu() {
        return menu;
    }
    
    public void setMenu(Menu menu) {
        Menu oldMenu = this.menu;
        this.menu = menu;
        if (oldMenu != null) oldMenu.setParent(null);
        if (menu != null) menu.setParent(this);
        firePropertyChange(this, PROPERTY_MENU, oldMenu, menu);
    }
}