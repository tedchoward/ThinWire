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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import thinwire.render.RenderStateEvent;
import thinwire.render.RenderStateListener;
import thinwire.render.web.WebApplication;
import thinwire.ui.event.ActionEvent;
import thinwire.ui.event.ActionListener;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;
import thinwire.ui.style.Color;
import thinwire.ui.AlignTextComponent.AlignX;

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
        String name;
        String description;
        InputStream is;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
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
            saveToFile(Application.current().getRelativeFile(fileName));
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
    private static final String MAKE_FILE_CHOOSER_BUTTON = "tw_makeFileChooserBtn";
    private static final String FILE_CHOOSER_SUBMIT = "tw_FileChooser_submit";
    private static final int BROWSE_BUTTON_WIDTH = 80;
    private static final int BROWSE_BUTTON_HEIGHT = 20;
    private static final int TEXT_FIELD_BUTTON_GAP = 5;
    private static final int MAX_HEIGHT = 500;
    
    private WebApplication app;
    private TextField fileName;
    private Button browseButton;
    private int browseButtonId;
    
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
            }
        });
        kids.add(fileName);
        
        browseButton = new Button("Browse");
        browseButton.setSize(BROWSE_BUTTON_WIDTH, BROWSE_BUTTON_HEIGHT);
        kids.add(browseButton);
        
        addPropertyChangeListener(new String[] { PROPERTY_WIDTH, PROPERTY_HEIGHT }, sizeListener);
        setSize(300, 20);
        
        app.invokeAfterRendered(browseButton, new RenderStateListener() {
            public void renderStateChange(RenderStateEvent ev) {
                int fileNameId = app.getComponentId(fileName);
                browseButtonId = app.getComponentId(browseButton);
                app.clientSideFunctionCall(MAKE_FILE_CHOOSER_BUTTON, new Object[] { browseButtonId, fileNameId });
            }
        });
    }
    
    /**
     * Initiates the upload of the file from the user's machine to the server.
     * @return a <code>FileInfo</code> object for the file uploaded
     */
    public FileInfo getFileInfo() {
        if (fileName.getText().length() == 0) return null;
        app.clientSideFunctionCall(FILE_CHOOSER_SUBMIT, browseButtonId);
        FileInfo fi = ((Application) app).getFileInfo();
        return fi;
    }   
    
    @Override
    public List<Component> getChildren() {
        return Collections.unmodifiableList(super.getChildren());
    }
    
    /**
     * Displays a <code>Dialog</code> with a <code>FileChooser</code> along
     * with OK and Cancel <code>Buttons</code>.
     * 
     * @return the <code>FileInfo</code> for the file uploaded
     */
    public static FileInfo show() {
        return show(false);
    }
    
    /**
     * Displays a <code>Dialog</code> with a <code>FileChooser</code> and
     * optionally a description <code>TextField</code> along with OK and
     * Cancel <code>Buttons</code>.
     * 
     * @param showDescription
     *            displays a <code>TextField</code> for a description if true
     * @return the <code>FileInfo</code> for the file uploaded
     */
    public static FileInfo show(boolean showDescription) {
        List<FileInfo> l = show(showDescription, false);
        return l.size() > 0 ? l.get(0) : null;
    }
    
    /**
     * Displays a <code>Dialog</code> with a <code>FileChooser</code> and
     * optionally a description <code>TextField</code> along with OK and
     * Cancel <code>Buttons</code>. Optional <code>Buttons</code> are
     * availiable for adding and removing additional <code>FileChooser</code>
     * components.
     * 
     * @param showDescription
     *            displays a <code>TextField</code> for a description if true
     * @param multiFile
     *            displays Add and Remove <code>Buttons</code> for uploading multiple files
     * @return a <code>List</code> of <code>FileInfo</code> objects
     */
    public static List<FileInfo> show(final boolean showDescription, boolean multiFile) {
        final Frame f = Application.current().getFrame();
        final Dialog dlg = new Dialog(multiFile ? "Upload Multiple Files" : "Upload File");
        final List<Component> kids = dlg.getChildren();
        final List<Component[]> files = new ArrayList<Component[]>();
        final List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        
        int width = 600;
        int height = showDescription ? 98 : 121;
        
        dlg.setBounds((f.getWidth() - width) / 2, (f.getHeight() - height) / 2, width, height);
        files.add(addRow(dlg, 0, showDescription));
        
        final Button okBtn = new Button("OK");
        okBtn.setBounds(dlg.getInnerWidth() - 170, dlg.getInnerHeight() - 27, 80, 22);
        okBtn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                for (Component[] c : files) {
                    FileChooser fc = (FileChooser) c[0];
                    FileInfo fi = fc.getFileInfo();
                    if (c.length == 2) fi.setDescription(((TextField) c[1]).getText());
                    fileInfoList.add(fi);
                }
                dlg.setVisible(false);
            }
        });
        kids.add(okBtn);
        
        final Button cancelBtn = new Button("Cancel");
        cancelBtn.setBounds(dlg.getInnerWidth() - 85, dlg.getInnerHeight() - 27, 80, 22);
        cancelBtn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                dlg.setVisible(false);
            }
        });
        kids.add(cancelBtn);
        
        if (multiFile) {
            Button addBtn = new Button("Add");
            final Button removeBtn = new Button("Remove");
            
            addBtn.setBounds(5, dlg.getInnerHeight() - 27, 80, 22);
            addBtn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    dlg.setHeight(dlg.getHeight() + 50);
                    dlg.setY((f.getHeight() - dlg.getHeight()) / 2);
                    files.add(addRow(dlg, files.size(), showDescription));
                    if (files.size() > 1) removeBtn.setEnabled(true);
                }
            });
            kids.add(addBtn);
            
            removeBtn.setBounds(90, dlg.getInnerHeight() - 27, 80, 22);
            removeBtn.setEnabled(false);
            removeBtn.addActionListener(Button.ACTION_CLICK, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    Component[] lastFile = files.remove(files.size() - 1);
                    for (Component c : lastFile) {
                        kids.remove(c.getLabel());
                        kids.remove(c);
                    }
                    dlg.setHeight(dlg.getHeight() - 50);
                    dlg.setY((f.getHeight() - dlg.getHeight()) / 2);
                    if (files.size() <= 1) ((Button) ev.getSource()).setEnabled(false);
                }
            });
            kids.add(removeBtn);
        }
        
        dlg.addPropertyChangeListener(Dialog.PROPERTY_HEIGHT, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                int newY = ((Dialog) ev.getSource()).getInnerHeight() - 27;
                for (Component c : ((Dialog) ev.getSource()).getChildren()) {
                    if (c instanceof Button) c.setY(newY);
                }
            }
        });
        
        dlg.setVisible(true);
        return fileInfoList;
    }
    
    private static Component[] addRow(Dialog dlg, int rowCount, boolean showDescription) {
        List<Component> kids = dlg.getChildren();
        int y1 = showDescription ? 10 + (rowCount * 50) : 10 + (rowCount * 20);
        FileChooser fc = new FileChooser();
        fc.setBounds(85, y1, dlg.getInnerWidth() - 90, 20);
        kids.add(fc);
        Label fcl = new Label("File Name:");
        fcl.setBounds(10, y1, 70, 20);
        fcl.setAlignX(AlignX.RIGHT);
        fcl.setLabelFor(fc);
        kids.add(fcl);
        if (showDescription) {
            int y2 = y1 + 25;
            TextField description = new TextField();
            description.setBounds(85, y2, dlg.getInnerWidth() - 90, 20);
            kids.add(description);
            
            Label lblDescription = new Label("Description:");
            lblDescription.setBounds(10, y2, 70, 20);
            lblDescription.setAlignX(AlignX.RIGHT);
            lblDescription.setLabelFor(description);
            kids.add(lblDescription);
            return new Component[] { fc, description };
        } else {
            return new Component[] { fc };
        }
    }
}