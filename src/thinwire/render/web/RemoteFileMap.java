/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.render.web;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joshua J. Gertzen
 */
final class RemoteFileMap {
    private static final Logger log = Logger.getLogger(RemoteFileMap.class.getName());
    private static final Level LEVEL = Level.FINER;
    static final RemoteFileMap INSTANCE = new RemoteFileMap(128); 
    
    private static final class RemoteFileInfo {
        private int refCount;
        private String remoteName;
        private String localName;
    }
    
    private Map<String, RemoteFileInfo> remoteToFileInfo;
    private Map<String, RemoteFileInfo> localToFileInfo;
    
    private RemoteFileMap(int initialMapSize) {
        remoteToFileInfo = new HashMap<String, RemoteFileInfo>(initialMapSize);
        localToFileInfo = new HashMap<String, RemoteFileInfo>(initialMapSize);
    }        

    final byte[] load(String remoteName) {
        RemoteFileInfo fileInfo = remoteToFileInfo.get(remoteName);
        
        if (fileInfo != null) {
            try {
                InputStream is;
                
                //"class:///thinwire.ui.layout.SplitLayout/resources/Image.png"                
                if (fileInfo.localName.startsWith("class:///")) {                    
                    int endIndex = fileInfo.localName.indexOf('/', 9);
                    String className = fileInfo.localName.substring(9, endIndex);
                    String resource = fileInfo.localName.substring(endIndex + 1);
                    Class clazz = Class.forName(className);
                    is = clazz.getResourceAsStream(resource);
                } else {                   
                    is = new BufferedInputStream(new FileInputStream(fileInfo.localName));
                }
                
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] bytes = new byte[128];
                int size;
                
                while ((size = is.read(bytes)) != -1)
                    os.write(bytes, 0, size);
                
                byte[] data = os.toByteArray();
                os.close();
                is.close();
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "Loaded file data for local file: localName='" + 
                        fileInfo.localName + "', remoteName='" + fileInfo.remoteName + "', refCount='" + 
                        fileInfo.refCount + "', data.length='" + data.length + "'");                        
                return data;
            } catch (Exception e) {
                if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
                throw (RuntimeException)e;
            }
        } else {        
            throw new RuntimeException(new FileNotFoundException("The specified remote file '" + remoteName + "' does not have a mapping to a local file"));
        }
    }
    
    String add(String localName) {               
        synchronized (localToFileInfo) {
            RemoteFileInfo fileInfo = localToFileInfo.get(localName);
            
            if (fileInfo == null) {
                fileInfo = new RemoteFileInfo();
                fileInfo.localName = localName;
                fileInfo.refCount = 1;
                
                int lastIndex = fileInfo.localName.lastIndexOf(File.separatorChar);
                if (lastIndex == -1) lastIndex = fileInfo.localName.lastIndexOf(File.separatorChar == '/' ? '\\' : '/');  
                fileInfo.remoteName = fileInfo.localName.substring(lastIndex + 1);
                
                if (remoteToFileInfo.containsKey(fileInfo.remoteName)) {                
                    lastIndex = fileInfo.remoteName.lastIndexOf('.');
                    String file, ext;
                    
                    if (lastIndex == -1) {
                        file = fileInfo.remoteName;
                        ext = "";
                    } else {
                        file = fileInfo.remoteName.substring(0, lastIndex);
                        ext = fileInfo.remoteName.substring(lastIndex);
                    }
                    
                    StringBuffer sb = new StringBuffer();
                    int i = 0;
                    
                    do {
                        i++;
                        sb.append(file).append('-').append(i).append(ext);
                        fileInfo.remoteName = sb.toString();
                        sb.setLength(0);
                    } while (remoteToFileInfo.containsKey(fileInfo.remoteName));
                }
                
                localToFileInfo.put(localName, fileInfo);
                remoteToFileInfo.put(fileInfo.remoteName, fileInfo);
            } else {
                fileInfo.refCount++;
            }
            
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "Added file mapping for local file: localName='" + 
                    localName + "', remoteName='" + fileInfo.remoteName + "', refCount='" + fileInfo.refCount + "'");            
            
            return fileInfo.remoteName;
        }
    }
    
    void remove(String localName) throws IOException {
        synchronized (localToFileInfo) {
            RemoteFileInfo fileInfo = localToFileInfo.get(localName);
            
            if (fileInfo != null) {
                fileInfo.refCount--;
                
                if (fileInfo.refCount <= 0) {
                    localToFileInfo.remove(localName);
                    remoteToFileInfo.remove(fileInfo.remoteName);
                }
            }
            
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "Removed file mapping for local file: localName='" + 
                    localName + "'" + (fileInfo == null ? "" : ", remoteName='" + fileInfo.remoteName + 
                    "', refCount='" + fileInfo.refCount + "'"));
        }
    }
}
