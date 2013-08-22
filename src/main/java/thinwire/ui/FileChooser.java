/*
ThinWire(R) Ajax RIA Framework
Copyright (C) 2003-2008 Custom Credit Systems

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package thinwire.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import thinwire.render.RenderStateEvent;
import thinwire.render.RenderStateListener;
import thinwire.render.web.WebApplication;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.style.Color;

/**
 * A <code>FileChooser</code> is a Component that enables a user to upload a
 * file. It consists of a disabled TextField and a Button, that when clicked,
 * causes the browser's file dialog to open. When the user chooses a file, the
 * path of the file is displayed in the TextField.
 * <p>
 * <b>Example:</b> <br>
 * <img src="doc-files/FileChooser-1.png"> <br>
 * 
 * <pre>
 * final Dialog dlg = new Dialog(&quot;FileChooser Test&quot;);
 * dlg.setBounds(10, 10, 320, 100);
 * final FileChooser fc = new FileChooser();
 * fc.setBounds(10, 10, 300, 20);
 * dlg.getChildren().add(fc);
 * 
 * Button b = new Button(&quot;OK&quot;);
 * b.setBounds(10, 40, 80, 25);
 * b.addActionListener(Button.ACTION_CLICK, new ActionListener() {
 *     public void actionPerformed(ActionEvent ev) {
 *         try {
 *             FileInfo fi = fc.getFileInfo();
 *             File f = File.createTempFile(&quot;tw_upload&quot;
 *                 + System.currentTimeMillis(), &quot;.txt&quot;);
 *             fi.saveToFile(f);
 *             Hyperlink h = new Hyperlink();
 *             h.setText("Click Here to See Your File");
 *             h.setLocation(f.getAbsolutePath());
 *             MessageBox mb = new MessageBox();
 *             mb.setTitle(&quot;File Chooser Test&quot;);
 *             mb.setComponent(h);
 *             mb.confirm();
 *             dlg.setVisible(false);
 *         } catch (Exception e) {
 *             throw new RuntimeException(e);
 *         }
 *     }
 * });
 * dlg.getChildren().add(b);
 * dlg.setVisible(true);
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
 * @author Ted C. Howard
 */
public class FileChooser extends Panel {
    
    /**
     * <code>FileInfo</code> is a class containing the name of the file
     * uploaded and its <code>InputStream</code>. It also contains
     * convenience methods for saving the file.
     * 
     * @author Joshua J. Gertzen and Ted C. Howard
     */
    public static class FileInfo {
    	String fullName;
        String name;
        String description;
        InputStream is;
        
        public FileInfo(String name, InputStream is) {
        	this.name = name;
        	this.is = is;
        	this.description = "";
        }
        
        public String getFullName() {
        	return fullName;
        }
        
        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
        
        /**
         * For convenience, an arbitrary description can be assigned to each
         * file uploaded.
         * 
         * @param description
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
        public InputStream getInputStream() {
            return is;
        }
        
        public void setInputStream(InputStream is) {
            this.is = is;
        }
        
        /**
         * Writes the uploaded file to a new <code>File</code> at the
         * specified location relative to the application base path.
         * 
         * @param fileName
         */
        public void saveToFile(String fileName) {
            saveToFile(Application.getRelativeFile(fileName));
        }
        
        /**
         * Writes the uploaded file to the specified <code>File</code>.
         * 
         * @param file
         */
        public void saveToFile(File file) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                byte[] block = new byte[256];
                int length;
                while ((length = is.read(block)) != -1) fos.write(block, 0, length);
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private static final Logger log = Logger.getLogger(FileChooser.class.getName());
    private static final int BROWSE_BUTTON_WIDTH = 80;
    private static final int BROWSE_BUTTON_HEIGHT = 20;
    private static final int TEXT_FIELD_BUTTON_GAP = 5;
    private static final int MAX_HEIGHT = 500;
    
    private WebApplication app;
    private TextField fileName;
    private Button browseButton;
    private FileInfo fileInfo;
    
    private PropertyChangeListener sizeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getPropertyName().equals(Component.PROPERTY_WIDTH)) {
                int newWidth = ((Container) ev.getSource()).getInnerWidth();
                int fileNameWidth = newWidth - (BROWSE_BUTTON_WIDTH + TEXT_FIELD_BUTTON_GAP);
                if (fileNameWidth < 0) fileNameWidth = 0;
                fileName.setWidth(fileNameWidth);
                browseButton.setX(fileNameWidth + TEXT_FIELD_BUTTON_GAP);
            } else if (ev.getPropertyName().equals(Component.PROPERTY_HEIGHT)) {
                int newHeight = ((Container) ev.getSource()).getInnerHeight();
                if (newHeight > MAX_HEIGHT) newHeight = MAX_HEIGHT;
                fileName.setHeight(newHeight);
                browseButton.setHeight(newHeight);
            }
        }
    };
    
    public FileChooser() {
        app = (WebApplication) Application.current();
        List<Component> kids = super.getChildren();
        getStyle().getBackground().setColor(Color.TRANSPARENT);
        
        fileName = new TextField();
        fileName.setPosition(0, 0);
        fileName.setHeight(BROWSE_BUTTON_HEIGHT);
        fileName.setEnabled(false);
        fileName.addPropertyChangeListener(TextField.PROPERTY_TEXT, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                // Empty listener makes sure text validation is accurate
        		fileInfo = null;
            }
        });
        kids.add(fileName);
        
        browseButton = new Button("Browse");
        browseButton.setSize(BROWSE_BUTTON_WIDTH, BROWSE_BUTTON_HEIGHT);
        kids.add(browseButton);
        
        addPropertyChangeListener(new String[] { PROPERTY_WIDTH, PROPERTY_HEIGHT }, sizeListener);
        setSize(300, 20);
        
        app.addRenderStateListener(browseButton, new RenderStateListener() {
            public void renderStateChange(RenderStateEvent ev) {
                app.clientSideMethodCall("tw_FileChooser", "newInstance", app.getComponentId(browseButton), app.getComponentId(fileName));
            }
        });
    }
    
    /**
     * Initiates the upload of the file from the user's machine to the server.
     * @return a <code>FileInfo</code> object for the file uploaded
     */
    public FileInfo getFileInfo() {
    	if (fileInfo == null) {
	    	String name = fileName.getText();
	        if (name.length() == 0) return null;
	        app.clientSideMethodCallWaitForReturn("tw_FileChooser", "submit", app.getComponentId(fileName));
	        fileInfo = ((Application) app).getFileInfo();
	        fileInfo.fullName = name;
    	}
    	
        return fileInfo;
    }   
    
    @Override
    public List<Component> getChildren() {
        return Collections.unmodifiableList(super.getChildren());
    }
}
