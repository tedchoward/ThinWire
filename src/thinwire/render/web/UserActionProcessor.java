/*
 * Created on Jan 27, 2007
  */
package thinwire.render.web;

import java.io.IOException;
import java.io.Reader;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


class UserActionProcessor extends Thread {
    private static final char EVENT_WEB_COMPONENT = '0';
    private static final char EVENT_GET_EVENTS = '1';
    private static final char EVENT_SYNC_CALL = '2';
    private static final char EVENT_RUN_TIMER = '3';
    private static Logger log = Logger.getLogger(UserActionProcessor.class.getName()); 
    private String[] syncCallResponse = new String[1];
    private List<WebComponentEvent> queue = new LinkedList<WebComponentEvent>();
    private StringBuilder sbParseUserAction = new StringBuilder(1024);
    private char[] complexValueBuffer = new char[256];
    private boolean active;
    private int captureCount;
    private boolean threadCaptured;
    
    WebApplication app;
    
    UserActionProcessor(WebApplication app) {
        super("ThinWire AppThread-" + app.httpSession.getId());
        this.app = app;
    }

    public void run() {
        if (log.isLoggable(Level.FINE)) log.fine("entering thread#" + getId() + ":" + getName());
        active = true;
        
        try {
            while (true) {
                processEvent();
            }
        } catch (InterruptedException e) { /* purposefully do nothing */ }
        
        if (log.isLoggable(Level.FINE)) log.fine("exiting thread#" + getId() + ": " + getName());
    }
    
    void capture() {
        int currentCaptureCount = ++captureCount;
        if (log.isLoggable(Level.FINE)) log.fine("capture thread#" + getId() + ":" + getName() + " captureCount:" + captureCount);
        threadCaptured = true;

        while (threadCaptured) {
            try {
                processEvent();
                if (currentCaptureCount == captureCount) threadCaptured = true;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    void release() {
        threadCaptured = false;
        captureCount--;
        if (log.isLoggable(Level.FINE)) log.fine("release thread#" + getId() + ":" + getName() + " captureCount:" + captureCount);
    }
    
    boolean isActive() {
        synchronized (queue) {
            return active;
        }
    }
    
    String getSyncCallResponse() {
        synchronized (syncCallResponse) {
            String ret;
            
            if (syncCallResponse[0] == null) {
                try {
                    syncCallResponse.wait(120000);
                    ret = syncCallResponse[0];
                    if (ret == null) log.warning("sendClientEvent did not respond within 120 seconds, returning null");
                } catch (InterruptedException e) {
                    log.fine("thread interrupted while waiting for synchronized client response");
                    ret = null;
                }
            } else {
                ret = syncCallResponse[0];
            }
            
            syncCallResponse[0] = null;
            return ret;
        }
    }
    
    void queue(WebComponentEvent wce) {
        synchronized (queue) {
            if (log.isLoggable(Level.FINEST)) log.finest("queue user action event:" + wce);
            queue.add(wce);
            queue.notify();
        }
    }
    
    //This method is called by the servers request handler thread, not this thread.
    void queue(Reader r) throws IOException {
        synchronized (queue) {
            StringBuilder sb = sbParseUserAction;
            boolean notify = false;
            
            try {
                do {
                    char eventType = (char)r.read();
                    r.read(); //Remove ':'
                    
                    switch (eventType) {                
                        case EVENT_GET_EVENTS: break;

                        case EVENT_WEB_COMPONENT: {
                            readSimpleValue(sb, r);
                            Integer source = Integer.valueOf(sb.toString());
                            WebComponentListener wcl = app.getWebComponentListener(source);
                            
                            if (wcl != null) {
                                readSimpleValue(sb, r);
                                String name = sb.toString();
                                readComplexValue(sb, r);
                                String value = sb.toString();
                                WebComponentEvent ev = new WebComponentEvent(source, name, value);
                                if (log.isLoggable(Level.FINEST)) log.finest("queue user action event:" + ev);
                                queue.add(ev);
                                notify = true;
                            }
                            
                            break;                  
                        }
                        
                        case EVENT_RUN_TIMER: {
                            readSimpleValue(sb, r);
                            String timerId = sb.toString();
                            WebComponentEvent ev = ApplicationEventListener.newRunTimerEvent(timerId);
                            if (log.isLoggable(Level.FINEST)) log.finest("queue run timer event:" + ev);
                            queue.add(ev);
                            notify = true;
                            break;
                        }
                        
                        case EVENT_SYNC_CALL: {
                            readComplexValue(sb, r);
                            String value = sb.toString();
                            if (log.isLoggable(Level.FINEST)) log.finest("sync call response=" + value);
                            
                            synchronized (syncCallResponse) {
                                syncCallResponse[0] = value;
                                syncCallResponse.notify();
                            }
                            
                            break;
                        }
                    }            
                } while (r.read() == ':');
            } catch (SocketTimeoutException e) {
                log.log(Level.WARNING, "Invalid action event format received from client", e);
            } finally {
                sb.setLength(0);
                if (notify) queue.notify();
            }
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
    
    private void processEvent() throws InterruptedException {
        WebApplication wapp = (WebApplication)app;
        WebComponentEvent event;

        synchronized (queue) {
            if (queue.size() > 0) {
                event = queue.remove(0);
                if (log.isLoggable(Level.FINEST)) log.finest("process user action event:" + event);
                if (wapp.userActionListener != null) wapp.notifyUserActionReceived(event);

                if (wapp.playBackOn && !wapp.playBackEventReceived) {
                    wapp.playBackEventReceived = true;
                    wapp.playBackStart = new Date().getTime();
                }
            } else {
                active = false;
                queue.wait();
                active = true;
                event = null;
            }
        }

        if (event != null) {
            if (wapp.playBackOn) {
                wapp.flushClientEvents();
                if (ApplicationEventListener.SHUTDOWN.equals(event.getName())) wapp.setPlayBackOn(false);
            }
            
            try {
                WebComponentListener wcl = wapp.getWebComponentListener((Integer) event.getSource());
                if (wcl != null) wcl.componentChange(event);
            } catch (Exception e) {
                app.reportException(null, e);
            }
        }
    }
}