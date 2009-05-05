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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import thinwire.ui.FileChooser;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * @author Joshua J. Gertzen
 */
public class WebServlet extends HttpServlet {
	private static final Level LEVEL = Level.FINER;
    private static final Logger log = Logger.getLogger(WebServlet.class.getName());
    private File storageDirectory=null;
    private static enum InitParam {
        MAIN_CLASS, EXTRA_ARGUMENTS, STYLE_SHEET, RELOAD_ON_REFRESH, INITIAL_FRAME_TITLE;

        private String mixedCaseName;
        
        private InitParam() {
            StringBuilder sb = new StringBuilder();
            String[] parts = name().split("_");
            sb.append(parts[0].toLowerCase());
            
            for (int i = 1, cnt = parts.length; i < cnt; i++) {
                sb.append(parts[i].charAt(0)).append(parts[i].toLowerCase().substring(1));
            }
            
            mixedCaseName = sb.toString();            
        }
        
        public String mixedCaseName() {
            return mixedCaseName;
        }
                
        public static InitParam valueOfMixedCase(String mixedCaseName) {
            for (InitParam ip : values()) {
                if (ip.mixedCaseName().equals(mixedCaseName)) return ip;
            }

            return valueOf(mixedCaseName);
        }
    }
    
    private static class ApplicationHolder implements HttpSessionBindingListener, Serializable {
    	private static final long serialVersionUID = 2454889032868933806L;
        transient WebApplication app;
        
        public void valueBound(HttpSessionBindingEvent event) {
            
        }
        
        public void valueUnbound(HttpSessionBindingEvent event) {
        	if (log.isLoggable(LEVEL)) log.log(LEVEL, "Unbinding application instance " + event.getSession().getId());            
            if (app != null) app.shutdown();
        }
    }
    public WebServlet() {
    	if (Thread.currentThread() instanceof EventProcessor) {
    		throw new IllegalStateException("This class '" + this.getClass().getName() + "' is managed by the servlet engine and cannot be constructed from an EventProcessor UI thread");
    	}
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String method = request.getMethod();

        if (method.equals("GET")) {
            String resource = request.getParameter("_twr_");
            
            if (resource == null) {
            	// check for path information that may mean we are asking for a different resource
            	String resourcePath=request.getPathInfo();
            	
            		handleStart(request, response);
            } else {
                handleResource(request, response, resource);
            }
        } else if (method.equals("POST")) {
            String action = request.getParameter("_twa_");
            
            if (action == null) {
                handlePostEvent(request, response);
            } else if (action.equals("upload")) {
                handleUserUpload(request, response);
            }
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }   
    
    private Set<String> getStartArguments(HttpServletRequest request) {
        Set<String> args = new TreeSet<String>();
        StringBuilder sb = new StringBuilder();        
        
        for (Map.Entry<String, String[]> e : ((Map<String, String[]>)request.getParameterMap()).entrySet()) {
            String key = e.getKey();
            String[] values = e.getValue();
                        
            if (values.length > 1) {
                for (int i = 0; i < values.length; i++) {
                    sb.append(key).append(i).append('=').append(values[i]);
                }
            } else {
                sb.append(key).append('=').append(values[0]);
            }
            
            args.add(sb.toString());
            sb.setLength(0);            
        }
        
        String extraArguments = getInitParameter(InitParam.EXTRA_ARGUMENTS.mixedCaseName());
        if (extraArguments == null) extraArguments = "";
        extraArguments = "," + extraArguments + ",";
        
        if (extraArguments.indexOf(",contextParam,") >= 0) {
            ServletContext sc = getServletContext();
            
            for (Enumeration<String> ipn = sc.getInitParameterNames(); ipn.hasMoreElements();) {
                String name = ipn.nextElement();                            
                sb.append("CONTEXT_PARAM_").append(name).append('=').append(sc.getInitParameter(name));                
                args.add(sb.toString());
                sb.setLength(0);
            }
        }
        
        if (extraArguments.indexOf(",initParam,") >= 0) {
            InitParam[] initParams = InitParam.values();
            
            ipn_loop: for (Enumeration<String> ipn = getInitParameterNames(); ipn.hasMoreElements();) {
                String name = ipn.nextElement();
                            
                for (InitParam ip : initParams) {
                    if (ip.mixedCaseName().equals(name))
                        continue ipn_loop;
                }
                
                sb.append("INIT_PARAM_").append(name).append('=').append(getInitParameter(name));                
                args.add(sb.toString());
                sb.setLength(0);
            }
        }
        
        if (extraArguments.indexOf(",header,") >= 0) {
            for (Enumeration<String> hn = request.getHeaderNames(); hn.hasMoreElements();) {
                String name = hn.nextElement();
                sb.append("HEADER_").append(name.toUpperCase()).append('=').append(request.getHeader(name));                
                args.add(sb.toString());
                sb.setLength(0);
            }            
        }
        
        if (extraArguments.indexOf(",clientInfo,") >= 0) {
            sb.append("CLIENT_INFO_USER").append('=').append(request.getRemoteUser());
            args.add(sb.toString());
            sb.setLength(0);
            sb.append("CLIENT_INFO_HOST").append('=').append(request.getRemoteHost());
            args.add(sb.toString());
            sb.setLength(0);
            sb.append("CLIENT_INFO_ADDRESS").append('=').append(request.getRemoteAddr());
            args.add(sb.toString());
            sb.setLength(0);
        }	
        
        return args;
    }
    
    private void handleStart(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession httpSession = request.getSession();
        String id = httpSession.getId(); 
        
        ApplicationHolder holder = (ApplicationHolder)httpSession.getAttribute("instance");
        response.setContentType("text/html");        
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("Expires", "-1");

        String initialFrameTitle = getInitParameter(InitParam.INITIAL_FRAME_TITLE.mixedCaseName());
        if (initialFrameTitle == null || initialFrameTitle.trim().length() == 0) initialFrameTitle = "Loading ThinWire Application...";
        response.getOutputStream().write(WebApplication.MAIN_PAGE.replaceAll("[$][{]initialFrameTitle[}]", initialFrameTitle).getBytes());
        
        String reload = getInitParameter(InitParam.RELOAD_ON_REFRESH.mixedCaseName());
        
        if (holder != null && holder.app != null && (reload == null || reload.toLowerCase().equals("false"))) {
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "repainting frame for application id=" + id);
            holder.app.repaint();
            return;
        } else {
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "starting new application with id=" + id);

            if (holder != null) {
	        	if (log.isLoggable(LEVEL)) log.log(LEVEL, "removing existing application instance with id=" + id);
	        	httpSession.removeAttribute("instance");
	        }
        }
        
        Set<String> args = getStartArguments(request);	        
        holder = new ApplicationHolder();
        String className = getInitParameter(InitParam.MAIN_CLASS.mixedCaseName());
        Class mainClass;
        
        if (className == null || className.trim().length() == 0) {
        	if (WebServlet.class != this.getClass()) {
        		mainClass = this.getClass();
        	} else {
            	throw new IllegalArgumentException("The init-param 'mainClass' is required or the servlet you specify must subclass WebServlet and contain a static 'main' method");
        	}
        } else {
        	try {
        		mainClass = Class.forName(className);
        	} catch (ClassNotFoundException e) {
        		throw new RuntimeException(e);
        	}
        }
        
        holder.app = new WebApplication(this.getServletContext().getRealPath(""), mainClass, getInitParameter(InitParam.STYLE_SHEET.mixedCaseName()), args.toArray(new String[args.size()]), initialFrameTitle);
        httpSession.setAttribute("instance", holder);
    }    
    
    private void handleResource(HttpServletRequest request, HttpServletResponse response, String resourceName) throws IOException, ServletException {        
        ApplicationHolder holder = (ApplicationHolder)request.getSession().getAttribute("instance");
 
        if (holder == null || holder.app == null) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no application instance exists from which to retreive resources");
        	return;
        }
        
        if (log.isLoggable(LEVEL)) log.log(LEVEL, "getting resource: " + resourceName);
        byte[] data = holder.app.remoteFileMap.load(resourceName);

        if (data == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            try {
                String mimeType;
                            
                //Necessary because the XMLHttpRequest object in certain browsers, such as Opera
                //do not properly handle text/javascript as the content type.  
                if (resourceName.endsWith(".js")) {
                    mimeType = "text/plain";
                } else {
                    mimeType = getServletContext().getMimeType(resourceName.toLowerCase());
                    if (mimeType != null && mimeType.startsWith("image/")) response.setHeader("Cache-Control", "max-age=43200");
                }
                
                response.setContentType(mimeType);

                if (data[0] == 31 && data[1] == -117) {
                	String encoding = request.getHeader("Accept-Encoding");
                	
                	if (encoding == null || encoding.toLowerCase().indexOf("gzip") == -1) {
                		if (log.isLoggable(Level.WARNING)) log.log(Level.WARNING, "User-Agent(" + request.getHeader("User-Agent") + ") does not accept gzip encoding, decompressing resource: " + resourceName);
                		WebApplication.writeInputToStream(new GZIPInputStream(new ByteArrayInputStream(data)), response.getOutputStream());
                	} else {
                		response.setHeader("Content-Encoding", "gzip");
                        response.getOutputStream().write(data);
                	}
                } else {
                    response.getOutputStream().write(data);
                }
            } catch (Exception e){
            	if (log.isLoggable(Level.WARNING)) log.log(Level.WARNING, "resource not found: " + resourceName, e);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }        
    }
    
    private void handlePostEvent(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession httpSession = request.getSession();        
        ApplicationHolder holder = (ApplicationHolder)httpSession.getAttribute("instance");
        response.setContentType("text/plain; charset=utf-8");
        response.setHeader("Cache-Control", "no-store");
        if (holder == null || holder.app == null) return;
        holder.app.processActionEvents(request.getReader(), response.getWriter());
        
        if (holder.app.state == WebApplication.State.TERMINATED) {
        	holder.app = null;
        	httpSession.invalidate();
        }
    }
    
    
    private File getStorageDirectory() throws IOException{
    	if(this.storageDirectory==null)
    	{
    		this.storageDirectory=File.createTempFile("upload", "tmp");
    		this.storageDirectory.mkdirs();
    		this.storageDirectory.deleteOnExit();
    	}
    	return this.storageDirectory;
    }
    
    
    private void handleUserUpload(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession httpSession = request.getSession();
        ApplicationHolder holder = (ApplicationHolder)httpSession.getAttribute("instance");
        
        if (holder.app != null) {
            try {
              DiskFileUpload upload = new DiskFileUpload();
            	 upload. setSizeThreshold(1000000);
                upload.setSizeMax(-1);
//                upload.setRepositoryPath("C:\\");
                upload.setRepositoryPath(getStorageDirectory().getAbsolutePath());
                List<FileItem> items = upload.parseRequest(request);

                if (items.size() > 0) {
                    FileChooser.FileInfo f = null;

                    synchronized(holder.app.fileList) {
                        for (FileItem fi : items) {
                            if (!fi.isFormField()) {
                                f = new FileChooser.FileInfo(fi.getName(), fi.getInputStream());
                                holder.app.fileList[0] = f;
                            }
                        }
                        
                        holder.app.fileList.notify();
                    }
                }
            } catch (FileUploadException e) {
                log.log(Level.SEVERE, null, e);
            }            
        }
        
        response.sendRedirect("?_twr_=FileUploadPage.html");
    }
}
