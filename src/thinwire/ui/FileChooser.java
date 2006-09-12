/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.ui;


import java.util.List;
import java.io.File;
import java.io.InputStream;

/**
 * A <code>FileChooser</code> is a Dialog that allows the user to upload a file.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/FileChooser-1.png"> <br>
 * 
 * <pre>
 * FileInfo f = FileChooser.show();
 * </pre>
 * 
 * </p>
 * <p>
 * <b>Keyboard Navigation:</b><br>
 * <table border="1">
 * <tr>
 * <td>KEY</td>
 * <td>RESPONSE</td>
 * <td>NOTE</td>
 * </tr>
 * </table>
 * </p>
 * @author Joshua J. Gertzen
 */
//TODO: The components rendered in the filechooser should be styled the same as the rest of the app.
public final class FileChooser {        
    public interface FileInfo {
        public String getName();
        public String getDescription();
        public InputStream getInputStream();
        public void saveToFile(String fileName);
        public void saveToFile(File file);
    }
    
    public static FileInfo show() {
        return show(false);
    }
    
    public static FileInfo show(boolean showDescription) {        
        List<FileInfo> l = show(showDescription, false);
        return l.size() > 0 ? l.get(0) : null;
    }
    
    public static List<FileInfo> show(boolean showDescription, boolean multiFile) {
        return Application.current().showFileChooser(showDescription, multiFile);
    }
}
