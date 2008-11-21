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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joshua J. Gertzen
 */
public class EventProcessor extends Thread {
    private static final char EVENT_WEB_COMPONENT = '0';
    private static final char EVENT_GET_EVENTS = '1';
    private static final char EVENT_SYNC_CALL = '2';
    private static final char EVENT_RUN_TIMER = '3';
    private static final int TIMEOUT = 1000 * 60 * 5;//5 minutes
    private static final Level LEVEL = Level.FINER;
    private static final Logger log = Logger.getLogger(EventProcessor.class.getName());
    private static int nextId = 0;
    
    static class GracefulShutdown extends Error { }
    
    private EventProcessorPool pool;
    private List<WebComponentEvent> queue = new LinkedList<WebComponentEvent>();
    private StringBuilder sbParseUserAction = new StringBuilder(1024);
    private char[] complexValueBuffer = new char[256];
    private String syncCallResponse;
    private Writer response;
    private boolean waitToRespond;
    private int updateEventsSize;
    private boolean active;
    private int captureCount;
    private boolean threadCaptured;
    private long lastActivityTime;
    
    WebApplication app;
    
    EventProcessor(EventProcessorPool pool) {
        if (pool == null) throw new IllegalArgumentException("pool == null");
        setName("ThinWire-EventProcessorThread-" + (nextId++) + "-" + this.hashCode());
        this.pool = pool;
    }
    
    boolean isInUse() {
        return threadCaptured || active;
    }

    public void run() {
        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": entering thread");
        active = true;
        
        synchronized (queue) {
            try {
                while (true) {
                    processUserActionEvent();
                }
            } catch (GracefulShutdown e) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": exiting thread run method gracefully");
            } finally {
                //allow for graceful exit
                pool.removeFromPool(this);

                //Not entirely necessary, but it makes me feel better ;-)
                queue.clear();
                queue = null;
                sbParseUserAction = null;
                response = null;
                pool = null;
                syncCallResponse = null;
                complexValueBuffer = null;
            }
        }
        
        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": exiting thread");
    }
    
    void captureThread() {
        int currentCaptureCount = ++captureCount;
        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": capture count:" + captureCount);
        threadCaptured = true;

        //XXX Loops infinitely if entered while frame is not visible and a dialog is blocking.
        while (threadCaptured) {
            processUserActionEvent();
            if (currentCaptureCount == captureCount) threadCaptured = true;
        }
    }
    
    //Must only be called by the main run loop or the capture method!
    private void processUserActionEvent() {
        if (queue.size() > 0) {
            lastActivityTime = System.currentTimeMillis();

            WebComponentEvent event = queue.remove(0);
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": process user action event:" + event);
            
            try {
                WebComponentListener wcl = app.getWebComponentListener((Integer) event.getSource());
                if (wcl != null) wcl.componentChange(event);
            } catch (Exception e) {
                app.reportException(null, e);
            }
        } else if (app != null && app.timers != null && app.timers.size() > 0) {
        	//Extra checks required since thread may start before being tied to an app.
        	try {
        		if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": process timer task 1 of " + app.timers.size());
        		app.timers.remove(0).run();
        	} catch (Exception e) {
        		app.reportException(null, e);
        	}
        } else {
            active = false;
            waitToRespond = false;
            if( app.userActionListener != null) app.userActionListener.finishedProcessing();
            queue.notify();
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": Notified request handler thread so it returns if it is currently blocking");

            try {
                if (threadCaptured) {
                    if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": Waiting for this captured thread to receive new user action events");
                    //This wait has the potential to deadlock if the client fails to make a request back to the server.
                    //To prevent this, a session timeout should be set in web.xml
                    queue.wait();
                } else {
                    if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": Waiting " + TIMEOUT + " to be given new user action events, otherwise it will be shutdown");
                    queue.wait(TIMEOUT);
                    
                    long timePassed = System.currentTimeMillis() - lastActivityTime;
                    if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": time passed during wait=" + timePassed + ", timeout=" + TIMEOUT);
                    
                    //Bring the thread down if it's been idle for five minutes.
                    if (app == null && queue.size() == 0 && timePassed >= TIMEOUT) {
                        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": triggering thread graceful shutdown");
                        throw new GracefulShutdown();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if( app!=null&&app.userActionListener != null) app.userActionListener.startProcessing();
            active = true;
        }
    }
    
    void releaseThread() {
        threadCaptured = false;
        captureCount--;
        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": release count:" + captureCount);
    }
    
    //This method is called by the servers request handler thread, not this thread.
    void handleRequest(WebComponentEvent ev, Writer w) throws IOException {
        synchronized (queue) {
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": queue user action event:" + ev);
            //ev would be null if this is called from shutdown() in an attempt to continue a dialog flush()           
            if (ev != null) queue.add(ev);
            queue.notify();
            writeUpdateEvents(w);
        }
    }
    
    //This method is called by the servers request handler thread, not this thread.
    void handleRequest(Reader r, Writer w) throws IOException {
        synchronized (queue) {
            StringBuilder sb = sbParseUserAction;
            
            try {
                do {
                    char eventType = (char)r.read();
                    r.read(); //Remove ':'
                    
                    switch (eventType) {                
                        case EVENT_GET_EVENTS: break;

                        case EVENT_WEB_COMPONENT: {
                            readSimpleValue(sb, r);
                            Integer source = Integer.valueOf(sb.toString());
                            readSimpleValue(sb, r);
                            String name = sb.toString();
                            readComplexValue(sb, r);
                            String value = sb.toString();
                            WebComponentListener wcl = app.getWebComponentListener(source);
                            
                            if (wcl != null) {
                                WebComponentEvent ev = new WebComponentEvent(source, name, value);
                                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": queue user action event:" + ev);
                                queue.add(ev);
                            }
                            
                            break;                  
                        }
                        
                        case EVENT_RUN_TIMER: {
                            readSimpleValue(sb, r);
                            String timerId = sb.toString();
                            WebComponentEvent ev = ApplicationEventListener.newRunTimerEvent(timerId);
                            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": queue run timer event:" + ev);
                            queue.add(ev);
                            break;
                        }
                        
                        case EVENT_SYNC_CALL: {
                            readComplexValue(sb, r);
                            syncCallResponse = sb.toString();
                            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": sync call response:" + syncCallResponse);
                            break;
                        }
                    }            
                } while (r.read() == ':');
            } catch (SocketTimeoutException e) {
                log.log(Level.WARNING, "Invalid action event format received from client", e);
            } finally {
                sb.setLength(0);
            }
            
            queue.notify();
            writeUpdateEvents(w);
        }
    }
    
    //Must only be called by one of the handleRequest methods!
    private void writeUpdateEvents(Writer w) throws IOException {
        if (w == null) return;
        
        try {
            response = w;
            waitToRespond = true;
            updateEventsSize = 0;
            
            while (waitToRespond) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": waiting for events to be processed");
                queue.wait();
            }

            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": finishing up update events, active=" + active + ", updateEventsSize=" + updateEventsSize);
            
            if (active) {
            	if (app.state == WebApplication.State.SHUTDOWN) {
            		if (updateEventsSize == 0) w.write('[');
            		updateEventsSize += 1;
            	} else {
	            	w.write(updateEventsSize == 0 ? "[{m:\"" : ",{m:\"");
	            	w.write("sendGetEvents\",a:[],n:tw_em}");
	            	updateEventsSize += 36;
            	}
            }
        } catch (InterruptedException e) {
            //Only occurs if the request handler thread is interrupted, in which case we should
            //try and gracefully exit;
        } finally {
            if (updateEventsSize > 0) response.write(']');
            response = null;
            updateEventsSize = 0;
        }
    }
    
    private void readSimpleValue(StringBuilder sb, Reader r) throws IOException {
        sb.setLength(0);
        int ch;
        
        while ((ch = r.read()) != ':') {
            if (ch == -1) throw new IllegalStateException("premature end of post event encountered[" + sb.toString() + "]");
            sb.append((char)ch);
        }        
    }
    
    private void readComplexValue(StringBuilder sb, Reader r) throws IOException {
        readSimpleValue(sb, r);
        int length = Integer.parseInt(sb.toString());
        sb.setLength(0);        

        if (length > 0) {
            int size;
            char[] buff = complexValueBuffer;
            int buffLen = buff.length;
            
            do {
                size = length > buffLen ? buffLen : length;
                size = r.read(buff, 0, size);
                if (size == -1) throw new IllegalStateException("premature end of complex value on action event encountered[" + sb.toString() + "], length=" + length);
                length -= size;
                sb.append(buff, 0, size);
            } while (length > 0);
        }
    }
    
    String postUpdateEvent(boolean sync, Object objectId, String name, Object[] args) {
        try {
            int size = 0; 
            response.write(updateEventsSize == 0 ? "[{m:\"" : ",{m:\"");
            size += 5;
            response.write(name);
            response.write('\"');            
            size += name.length() + 1;
            
            if (objectId != null) {
                if (objectId instanceof Integer) {
                    response.write(",i:");
                    String value = objectId.toString();
                    response.write(value);
                    size += 3 + value.length();
                } else {
                    response.write(",n:");
                    String value = (String)objectId;
                    response.write(value);
                    size += 3 + value.length();
                }
            }

            if (args != null && args.length > 0) {
                response.write(",a:[");
                size += 4;

                for (int i = 0, cnt = args.length - 1; i < cnt; i++) {
                    String value = ComponentRenderer.stringValueOf(args[i]);
                    response.write(value);
                    response.write(',');
                    size += value.length() + 1;
                }

                String value = ComponentRenderer.stringValueOf(args[args.length - 1]);
                response.write(value);
                response.write(']');
                size += value.length() + 1;
            } else {
                response.write(",a:[]");
                size += 5;
            }

            if (sync) {
                response.write(",s:1}");
                size += 5;
            } else {
                response.write("}");
                size += 1;
            }
            
            updateEventsSize += size;
            
            if (waitToRespond && (sync || updateEventsSize >= 16384)) flush();
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sync ? syncCallResponse : null;
    }
    
    void flush() {
        waitToRespond = false;
        queue.notify();

        try {
            while (!waitToRespond) {
                //This wait has the potential to deadlock if the client fails to make a request back to the server.
                //To prevent this, a session timeout should be set in web.xml
                queue.wait();
            }
        } catch (InterruptedException e) {
            //Must throw an exception so the stack unrolls properly.
            throw new RuntimeException(e);
        }
    }
}