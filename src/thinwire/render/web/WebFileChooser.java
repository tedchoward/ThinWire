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
package thinwire.render.web;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import thinwire.ui.MessageBox;
import thinwire.ui.Panel;
import thinwire.ui.Dialog;
import thinwire.ui.FileChooser;
import thinwire.ui.Frame;
import thinwire.ui.Component;
import thinwire.ui.ScrollType;

/**
 * @author Joshua J. Gertzen
 */
class WebFileChooser {
    private static final String PREPARE_FILE_CHOOSER = "tw_prepareFileChooser";
    
    private WebApplication app;
    private Dialog d;
    private Panel c;
    private Integer containerId;
    private List<FileChooser.FileInfo> files;
    
    WebFileChooser(WebApplication app) {
        this.app = app;
        files = new ArrayList<FileChooser.FileInfo>(5);
    }
    
    void show(boolean showDescription, boolean multiFile) {
        Frame f = app.getFrame();
        d = new Dialog(multiFile ? "Upload Multiple Files" : "Upload File");
        int width = 600;
        int height = 20;
        if (showDescription) height += 20 + 3;    
        
        if (multiFile) {
            height *= 5;
            width -= 20;
        }
        
        height += 78;
        
        d.setBounds((f.getWidth() - width) / 2, (f.getHeight() - height) / 2, width, height);
        
        List<Component> l = d.getChildren();
        c = new Panel();
        c.setScroll(ScrollType.AS_NEEDED);
        c.setBounds(0, 0, d.getWidth() - 6, d.getHeight());
        c.setScroll(ScrollType.AS_NEEDED);
        l.add(c);        
               
        d.setWaitForWindow(false);
        d.setVisible(true);
        WindowRenderer wr = app.getWindowRenderer(d);
        containerId = wr.getComponentId(c);
        app.clientSideFunctionCall(PREPARE_FILE_CHOOSER, containerId, showDescription, multiFile);
        d.setWaitForWindow(true);
    }
    
    void hide() {
        MessageBox.confirm("hide");
        d.setVisible(false);        
    }
    
    List<FileChooser.FileInfo> getFileInfoList() {
        return files;
    }
}
