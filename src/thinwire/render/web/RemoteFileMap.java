/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2007 Custom Credit Systems

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
package thinwire.render.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
        private byte[] data;
    }
    
    private Map<String, RemoteFileInfo> remoteToFileInfo;
    private Map<String, RemoteFileInfo> localToFileInfo;
    
    private RemoteFileMap(int initialMapSize) {
        remoteToFileInfo = new HashMap<String, RemoteFileInfo>(initialMapSize);
        localToFileInfo = new HashMap<String, RemoteFileInfo>(initialMapSize);
    }

    final String getLocalName(String remoteName) {
        RemoteFileInfo fileInfo = remoteToFileInfo.get(remoteName);
        return fileInfo == null ? null : fileInfo.localName;
    }
    
    final byte[] load(String remoteName) {
        RemoteFileInfo fileInfo = remoteToFileInfo.get(remoteName);
        
        if (fileInfo != null) {
            byte[] data;
            
            if (fileInfo.data == null) {
                data = WebApplication.getResourceBytes(fileInfo.localName);
            } else {
                data = fileInfo.data;
            }

            if (log.isLoggable(LEVEL)) log.log(LEVEL, "Loaded file data for local file: localName='" + 
                    fileInfo.localName + "', remoteName='" + fileInfo.remoteName + "', refCount='" + 
                    fileInfo.refCount + "', data.length='" + data.length + "'");
            
            return data;
        } else {        
            throw new RuntimeException(new FileNotFoundException("The specified remote file '" + remoteName + "' does not have a mapping to a local file"));
        }
    }
    
    String add(String localName) {
        return add(localName, null, null);
    }

    String add(String localName, String remoteName) {
        return add(localName, remoteName, null);
    }

    String add(String localName, String remoteName, byte[] data) {               
        synchronized (localToFileInfo) {
            RemoteFileInfo fileInfo = localToFileInfo.get(localName);
            
            if (fileInfo == null) {
                fileInfo = new RemoteFileInfo();
                fileInfo.localName = localName;
                fileInfo.refCount = 1;
                fileInfo.data = data;
                
                if (remoteName == null) {
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
                        
                        StringBuilder sb = new StringBuilder();
                        int i = 0;
                        
                        do {
                            i++;
                            sb.append(file).append('-').append(i).append(ext);
                            fileInfo.remoteName = sb.toString();
                            sb.setLength(0);
                        } while (remoteToFileInfo.containsKey(fileInfo.remoteName));
                    }
                } else {
                    fileInfo.remoteName = remoteName;
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
