/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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
  contact the following company who invented, built and supports the technology:
  
                Custom Credit Systems, Richardson, TX 75081, USA.
   	            email: info@thinwire.com    ph: +1 (888) 644-6405
 	                        http://www.thinwire.com
#ENDIF
#IFDEF ALT_LICENSE
#LICENSE_HEADER#
#ENDIF
#VERSION_HEADER#
*/
package thinwire.ui;

/**
 * @author Joshua J. Gertzen
 */
public interface Window extends Container<Component> {
    public static final String PROPERTY_MENU = "menu";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_WAIT_FOR_WINDOW = "waitForWindow";

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

    Menu getMenu();

    /**
     * Sets the main menubar for the window.
     * @param menu
     */
    void setMenu(Menu menu);

    /**
     * Makes the window visible.
     * @param visible (Default = false)
     */
    void setVisible(boolean visible);

    boolean isWaitForWindow();

    /**
     * Sets whether the script execution pauses until the window is closed or not.
     * @param waitForWindow (Default = true)
     */
    void setWaitForWindow(boolean waitForWindow);

}