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
public interface Window extends Container<Component> {
    public static final String PROPERTY_MENU = "menu";
    public static final String PROPERTY_TITLE = "title";

    /**
     * Gets the title of the window.
     * @return the title of the window.
     */
    String getTitle();

    /**
     * Sets the title of the window.
     * @param title
     */
    void setTitle(String title);

    /**
     * Gets the main menubar for the window.
     * @return the main menubar for the window.
     */
    Menu getMenu();

    /**
	 * Sets the main menubar for the window. If null, there will be no menubar.
	 * @param menu
	 */
    void setMenu(Menu menu);
}