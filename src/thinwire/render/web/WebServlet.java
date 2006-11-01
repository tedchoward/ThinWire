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

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import thinwire.ui.FileChooser;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * @author Joshua J. Gertzen
 */
public final class WebServlet extends HttpServlet {    
    private static final int EVENT_WEB_COMPONENT = 0;
    private static final int EVENT_GET_EVENTS = 1;
    private static final int EVENT_LOG_MESSAGE = 2;
    private static final int EVENT_SYNC_CALL = 3;
    private static final int EVENT_RUN_TIMER = 4;
    
    private static enum InitParam {
        MAIN_CLASS, EXTRA_ARGUMENTS, STYLE_SHEET;

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
        
	private static final Logger log = Logger.getLogger(WebServlet.class.getName());

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String method = request.getMethod();

        if (method.equals("GET")) {
            String resource = request.getParameter("_twr_");
            
            if (resource == null) {
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
    
    private void handleStart(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession httpSession = request.getSession();
        String id = httpSession.getId(); 
        WebApplication app = (WebApplication)httpSession.getAttribute("instance");
        if (log.isLoggable(Level.FINEST)) log.finest("start id=" + id + ",old instance is null=" + (app == null));
        
        //In the case of a refresh, there may be an old Application instance hanging
        //around.  Clean it up.
        if (app != null) {
            log.log(Level.FINER, "Initiating Application instance SHUTDOWN");
            app.queueWebComponentEvent(new WebComponentEvent(WebApplication.APPEVENT_ID, WebApplication.APPEVENT_SHUTDOWN, null));
        }

        response.setContentType("text/html");        
        response.getOutputStream().write(RemoteFileMap.INSTANCE.load(WebApplication.MAIN_PAGE));        

        List<String> args = new ArrayList<String>();
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
        
        app = new WebApplication(this, httpSession, getInitParameter(InitParam.MAIN_CLASS.mixedCaseName()), getInitParameter(InitParam.STYLE_SHEET.mixedCaseName()), args.toArray(new String[args.size()]));
        httpSession.setAttribute("instance", app);        
    }    
    
    private void handleResource(HttpServletRequest request, HttpServletResponse response, String resourceName) throws IOException, ServletException {        
        if (log.isLoggable(Level.FINEST)) log.finest("getting resource: " + resourceName);
        byte[] data = RemoteFileMap.INSTANCE.load(resourceName);
        
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
                response.getOutputStream().write(data);
            } catch (Exception e){
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }        
    }
    
    private void handlePostEvent(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession httpSession = request.getSession();        
        WebApplication app = (WebApplication)httpSession.getAttribute("instance");
        if (app == null) return;
        BufferedReader r = request.getReader();
        StringBuilder sb = new StringBuilder();
        
        do {
            readSimpleValue(sb, r);
            int eventType = Integer.parseInt(sb.toString());
            readComplexValue(sb, r);
            CharArrayReader car = new CharArrayReader(sb.toString().toCharArray());
            
            switch (eventType) {                
                case EVENT_WEB_COMPONENT: {
                    readSimpleValue(sb, car);
                    Integer source = Integer.valueOf(sb.toString());
                    WebComponentListener wcl = app.getWebComponentListener(source);
                    
                    if (wcl != null) {
                        readSimpleValue(sb, car);
                        String name = sb.toString();
                        readComplexValue(sb, car);
                        String value = sb.toString();                       
                        if (log.isLoggable(Level.FINEST)) log.finest("EVENT_WEB_COMPONENT:source=" + source + ",name=" + name + ",value=" + value);
                        WebComponentEvent wce = new WebComponentEvent(source, name, value);
                        app.queueWebComponentEvent(wce);
                    }
                    
                    break;                  
                }
                
                case EVENT_GET_EVENTS: break;
                
                case EVENT_LOG_MESSAGE: {
                    readSimpleValue(sb, car);
                    String levelName = sb.toString();
                    readComplexValue(sb, car);                    
                    String message = sb.toString();                    
                    app.queueWebComponentEvent(new WebComponentEvent(WebApplication.APPEVENT_ID, WebApplication.APPEVENT_LOG_MESSAGE,
                            new String[] {levelName, message}));
                    break;
                }
                
                case EVENT_SYNC_CALL: {
                    readComplexValue(sb, car);
                    String value = sb.toString();
                    app.notifySyncCallResponse(value);
                    if (log.isLoggable(Level.FINEST)) log.finest("EVENT_SYNC_CALL:response=" + value);
                    break;
                }
                
                case EVENT_RUN_TIMER: {
                    readSimpleValue(sb, car);
                    String timerId = sb.toString();
                    app.queueWebComponentEvent(new WebComponentEvent(WebApplication.APPEVENT_ID, WebApplication.APPEVENT_RUN_TIMER, timerId));
                    break;
                }
            }            
        } while (r.read() == ':');
        
        String events = app.getClientEvents();
        if (log.isLoggable(Level.FINEST)) log.finest("handleGetEvents:" + events);
        
        if (events != null) {
            response.setContentType("text/plain; charset=utf-8");
            response.setHeader("Cache-Control", "no-store");
            response.getWriter().print(events);
        }
    }
    
    private void readSimpleValue(StringBuilder sb, Reader r) throws IOException, ServletException {
        sb.setLength(0);
        int ch;
        
        while ((ch = r.read()) != ':') {
            if (ch == -1) throw new ServletException("premature end of post event encountered[" + sb.toString() + "]");
            sb.append((char)ch);
        }        
    }
    
    private void readComplexValue(StringBuilder sb, Reader r) throws IOException, ServletException {
        readSimpleValue(sb, r);
        int length = Integer.parseInt(sb.toString()) - 1;
        sb.setLength(0);        

        for (; length >= 0; length--)
            sb.append((char)r.read());
    }
    
    private void handleUserUpload(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession httpSession = request.getSession();
        WebApplication app = (WebApplication)httpSession.getAttribute("instance");        
        if (app != null) {
            try {
                DiskFileUpload upload = new DiskFileUpload();
                upload.setSizeThreshold(1000000);
                upload.setSizeMax(25000000);
                upload.setRepositoryPath("C:\\");
                List<FileItem> items = upload.parseRequest(request);
                if (items.size() > 0) {
                    FileChooser.FileInfo f = null;
                    synchronized(app.fileList) {
                        for (FileItem fi : items) {
                            if (!fi.isFormField() && fi.getSize() > 0) {
                                f = new FileChooser.FileInfo();
                                f.setName(fi.getName());
                                f.setInputStream(fi.getInputStream());
                                f.setDescription("");
                                app.fileList[0] = f;
                            }
                        }
                        app.fileList.notify();
                    }
                }
            } catch (FileUploadException e) {
                log.log(Level.SEVERE, null, e);
            }            
        }
    }
}
