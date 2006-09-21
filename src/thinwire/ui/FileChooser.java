/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
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
