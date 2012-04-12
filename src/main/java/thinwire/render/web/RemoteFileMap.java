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
package thinwire.render.web;

import java.io.*;
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

    private static final class FileData {
    	private int appRefCount;
    	private byte[] bytes;
    }
    
    private static final class RemoteFile {
        private String localName;
        private String remoteName;
    	private int refCount;
    	private FileData data;
    }

    private static final Map<String, FileData> FILE_DATA = new HashMap<String, FileData>(128);
    static final RemoteFileMap SHARED = new RemoteFileMap(null);
    
    private WebApplication app;
    private Map<String, RemoteFile> localToFile = new HashMap<String, RemoteFile>(32);
    private Map<String, RemoteFile> remoteToFile = new HashMap<String, RemoteFile>(32);
    
    RemoteFileMap(WebApplication app) {
    	this.app = app;
    }
    
    private String getUniqueRemoteName(String name) {
        if (remoteToFile.containsKey(name)) {                
            int lastIndex = name.lastIndexOf('.');
            String file, ext;
            
            if (lastIndex == -1) {
                file = name;
                ext = "";
            } else {
                file = name.substring(0, lastIndex);
                ext = name.substring(lastIndex);
            }
            
            StringBuilder sb = new StringBuilder();
            int i = 0;
            
            do {
                i++;
                sb.append(file).append('-').append(i).append(ext);
                name = sb.toString();
                sb.setLength(0);
            } while (remoteToFile.containsKey(name));
        }
        
        return name;
    }
    
    byte[] load(String remoteName) {
    	RemoteFile file = remoteToFile.get(remoteName);
    	
    	if (file == null) {
    		if (this == SHARED) {
    			throw new RuntimeException(new FileNotFoundException("The specified remote file '" + remoteName + "' does not have a mapping to a local file"));
    		} else {
    			return SHARED.load(remoteName);
    		}
    	}
    	
    	synchronized (file.data) {
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "Loaded file data for local file: localName='" + 
                    file.localName + "', remoteName='" + file.remoteName + "', refCount=" + 
                    file.refCount + ", data.length='" + file.data.bytes.length + "'");

    		return file.data.bytes;
    	}
    }
    
    String add(String localName) {
    	return add(localName, null);
    }
    
    String add(String localName, byte[] bytes) {
    	RemoteFile file = localToFile.get(localName);
    	boolean bytesNotUsed = bytes != null;
    	
    	if (file == null) {
    		file = new RemoteFile();
    		file.localName = localName;
            int lastIndex = localName.lastIndexOf(File.separatorChar);
            if (lastIndex == -1) lastIndex = localName.lastIndexOf(File.separatorChar == '/' ? '\\' : '/');  
            file.remoteName = getUniqueRemoteName(localName.substring(lastIndex + 1));
        	
        	synchronized (FILE_DATA) {
        		file.data = FILE_DATA.get(localName);
        		
        		if (file.data == null) {
        			FILE_DATA.put(localName, file.data = new FileData());

        			if (log.isLoggable(LEVEL)) log.log(LEVEL, "Added shared file for local file: localName='" + 
	                        file.localName + "', number of shared entries=" + FILE_DATA.size());
        		}
        	}

        	synchronized (file.data) {
        		if (file.data.bytes == null) {
        			if (bytesNotUsed) {
            			bytesNotUsed = false;
            			file.data.bytes = bytes;
        			} else {
	        			InputStream is = app == null ? WebApplication.getResourceAsStream(localName) : app.getContextResourceAsStream(localName);
	        	    	ByteArrayOutputStream os = new ByteArrayOutputStream();
	        	    	WebApplication.writeInputToStream(is, os);
	        	    	file.data.bytes = os.toByteArray();
        			}
        		}
        		
        		file.data.appRefCount++;
        	}
        	
            remoteToFile.put(file.remoteName, file);
            localToFile.put(localName, file);
    	}
    	
    	file.refCount++;

    	if (log.isLoggable(LEVEL)) log.log(LEVEL, "Added application file mapping for local file: localName='" + 
                localName + "', remoteName='" + file.remoteName + "', refCount=" + file.refCount + ", bytesNotUsed=" + bytesNotUsed);            
    	
    	return file.remoteName;
    }
    
    private void removeSharedFile(RemoteFile file) {
		synchronized (file.data) {
			if (file.data.bytes != null) {
				file.data.appRefCount--;
				
				if (file.data.appRefCount <= 0) {
					synchronized (FILE_DATA) {
						FILE_DATA.remove(file.localName);

						if (log.isLoggable(LEVEL)) log.log(LEVEL, "Removed shared file data for local file: localName='" + 
		                        file.localName + "', shared refCount=" + file.data.appRefCount + ", number of shared entries=" + FILE_DATA.size());
					}
					
	            	file.data.bytes = null;
				}
			}
		}
    }
    
    boolean contains(String localName) {
    	return localToFile.containsKey(localName);
    }
    
    void remove(String localName) {
    	RemoteFile file = localToFile.get(localName);

    	if (file != null) {
            file.refCount--;
            
            if (file.refCount <= 0) {
                localToFile.remove(file.localName);
                remoteToFile.remove(file.remoteName);
                removeSharedFile(file);
            }

            if (log.isLoggable(LEVEL)) log.log(LEVEL, "Removed application file mapping for local file: localName='" + 
                    file.localName + "', remoteName='" + file.remoteName + "', refCount=" + file.refCount);
        } else {
        	if (log.isLoggable(Level.WARNING)) log.log(Level.WARNING, "Attempt to remove reference for unmapped local file: '" + localName + "'");
        }
    }
    
    void destroy() {
    	for (RemoteFile file : remoteToFile.values()) {
    		removeSharedFile(file);
    	}
    	
    	remoteToFile.clear();
    	localToFile.clear();
    	remoteToFile = null;
    	localToFile = null;
    	app = null;
    }
}
