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
package thinwire.ui;

import java.util.List;

/**
 * @author Joshua J. Gertzen
 */
abstract class AbstractWindow extends AbstractContainer<Component> implements Window {
    static final int MENU_BAR_HEIGHT = 23;
    
    String title = "";
    Menu menu;
    boolean waitForWindow = true;

    AbstractWindow() {
        super(false);
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

    public void setVisible(boolean visible) {
        if (isVisible() != visible) {
            Application app = Application.current();
            Frame f = app.getFrame();
            if (this != f && !f.isVisible()) f.setVisible(true);
            if (this instanceof Dialog) f.dialogVisibilityChanged((Dialog)this, visible);            
            
            if (visible) {
                app.showWindow(this);
                super.setVisible(visible);
                if (waitForWindow) app.captureThread();
            } else {
                if (this == f) {
                    List<Dialog> dialogs = f.getDialogs();
                    
                    for (Dialog d : dialogs.toArray(new Dialog[dialogs.size()])) {
                        d.setVisible(false);
                    }
                }
                
                app.hideWindow(this);
                super.setVisible(visible);
                if (waitForWindow) app.releaseThread();
            }
        }
    }
        
    public boolean isWaitForWindow() {
        return this.waitForWindow;
    }   
    
    public void setWaitForWindow(boolean waitForWindow) {
        if (this.waitForWindow == waitForWindow) return;                
        boolean oldWaitForWindow = this.waitForWindow;
        this.waitForWindow = waitForWindow;
        firePropertyChange(this, PROPERTY_WAIT_FOR_WINDOW, oldWaitForWindow, waitForWindow);

        if (isVisible()) {
            if (oldWaitForWindow)
                Application.current().releaseThread();
            else
                Application.current().captureThread();
        }       
    }   
}