/*
 *                      ThinWire(TM) RIA Ajax Framework
 *              Copyright (C) 2003-2006 Custom Credit Systems
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Users wishing to use this library in proprietary products which are not 
 * themselves to be released under the GNU Public License should contact Custom
 * Credit Systems for a license to do so.
 * 
 *               Custom Credit Systems, Richardson, TX 75081, USA.
 *                          http://www.thinwire.com
 */
package thinwire.render.web;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import thinwire.ui.Application;
import thinwire.ui.Dialog;
import thinwire.ui.FileChooser;
import thinwire.ui.Component;
import thinwire.ui.Window;
import thinwire.ui.FileChooser.FileInfo;

/**
 * @author Joshua J. Gertzen
 */
public final class WebApplication extends Application {
    private static final String SHUTDOWN_INSTANCE = "tw_shutdownInstance";
    private static final String CLASS_NAME = WebApplication.class.getName();
    static final Logger log = Logger.getLogger(CLASS_NAME);
    private static final String PACKAGE_NAME = WebApplication.class.getPackage().getName();
    private static final Pattern REGEX_DOUBLE_SLASH = Pattern.compile("\\\\");
    private static final Pattern REGEX_DOUBLE_QUOTE = Pattern.compile("\"");
    private static final Pattern REGEX_CRLF = Pattern.compile("\\r?\\n");
    private static final String EOL = System.getProperty("line.separator");
    private static final long MINUTE = 60 * 1000;
    private static final long INSTANCE_TIMEOUT = 10 * MINUTE;
    private static final long INSTANCE_KEEP_ALIVE_CYCLE = INSTANCE_TIMEOUT - MINUTE;
    private static final long INSTANCE_MONITOR_THREAD_CYCLE = INSTANCE_TIMEOUT / 2;
    private static Thread instanceMonitorThread;
    private static final Runnable instanceMonitorRunnable = new Runnable() {
        public void run() {
            log.fine("Starting instance monitoring thread");
            Application[] apps = getApplications();

            do {
                try {
                    Thread.sleep(INSTANCE_MONITOR_THREAD_CYCLE);
                } catch (Exception e) {
                    throw new RuntimeException(e);                    
                }
                
                for (Application app : apps) {
                    WebApplication twapp = (WebApplication)app;
                    long currentTime = System.currentTimeMillis();
                    
                    //If more than two minutes have passed shutdown app
                    if (currentTime - twapp.lastClientRequestTime.longValue() > INSTANCE_TIMEOUT) {
                        log.fine("Shutting down application instance " + twapp.id + " due to inactivity");
                        twapp.queueWebComponentEvent(new WebComponentEvent(WebApplication.APPEVENT_ID, WebApplication.APPEVENT_SHUTDOWN, null));
                        
                        try {
                            //Attempt to join thread for 10 seconds, if this fails, forcefully kill the thread.
                            Thread appThread = twapp.getExecutionThread();
                            if (appThread != null) {                                
                                appThread.join(10000);
                                
                                if (appThread.isAlive()) {
                                    log.fine("Forcefully stopping application instance " + twapp.id + ", thread did not respond to join");
                                    appThread.stop();
                                    twapp.setExecutionThread(null);
                                }                                
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                
                apps = getApplications();
            } while (apps.length > 0);
            
            log.fine("Finishing instance monitoring thread");
        }
    };
    
    private final String id;

    private StringBuffer sbClientEvents;
    private List<WebComponentEvent> eventQueue;
    private Map<String, Timer> timerMap;
    private Map<String, Class<ComponentRenderer>> nameToRenderer;
    private Map<Window, WindowRenderer> windowToRenderer;
    private Map<Integer, WebComponentListener> webComponentListeners;
    private String[] syncCallResponse = new String[1];
    private WebFileChooser fileChooser;
    private boolean threadCaptured;
    private boolean threadWaiting;
    private boolean processClientEvents;
    private int nextCompId;
    private int captureCount;
    private Long lastClientRequestTime;
	
	//Stress Test Variables.
	private UserActionListener userActionListener;    
	private boolean playBackOn = false;
	private long playBackStart = -1;
	private long playBackDuration = -1;
	private long recordDuration = -1;
	private boolean playBackEventReceived = false;
	//end Stress Test.

    static public final Integer APPEVENT_ID = new Integer(Integer.MAX_VALUE);
    static public final String APPEVENT_SHUTDOWN = "SHUTDOWN";
    static final String APPEVENT_FILEUPLOAD_COMPLETE = "FILEUPLOAD_COMPLETE";
    static final String APPEVENT_LOG_MESSAGE = "LOG_MESSAGE";
    static final String APPEVENT_RUN_TIMER = "RUN_TIMER";

    private static class ClientSideScriptException extends RuntimeException {
        private ClientSideScriptException(String message) {
            super(message);
        }
    }
    
    private static class Timer {
        private Runnable task;
        private long timeout;
        private boolean repeat;
    }

    WebApplication(final WebServlet servlet, final HttpSession httpSession, final String mainClass, final String[] args) {
        nameToRenderer = new HashMap<String, Class<ComponentRenderer>>();
        windowToRenderer = new HashMap<Window, WindowRenderer>();
        eventQueue = new ArrayList<WebComponentEvent>();
        timerMap = new HashMap<String, Timer>();
        sbClientEvents = new StringBuffer(4096);
        sbClientEvents.append('[');
        webComponentListeners = new HashMap<Integer, WebComponentListener>();
        id = httpSession.getId();
        setBaseFolder(servlet.getServletContext().getRealPath(""));

        setWebComponentListener(APPEVENT_ID, new WebComponentListener() {
            public void componentChange(WebComponentEvent event) {
                String name = event.getName();

                if (APPEVENT_LOG_MESSAGE.equals(name)) {
                    String[] msg = (String[]) event.getValue();
                    Level level = Level.parse(msg[0].toUpperCase());

                    if (level == Level.SEVERE) {
                        reportException(this, new ClientSideScriptException(msg[1]));
                    } else if (log.isLoggable(level)) {
                        LogRecord lr = new LogRecord(level, msg[1]);
                        lr.setSourceClassName(CLASS_NAME);
                        lr.setSourceMethodName("[client-side]");
                        log.log(lr);
                    }
                } else if (APPEVENT_FILEUPLOAD_COMPLETE.equals(name)) {
                    fileChooser.hide();
                } else if (APPEVENT_SHUTDOWN.equals(name)) {
                    getFrame().setVisible(false);
                } else if (APPEVENT_RUN_TIMER.equals(name)) {
                    String timerId = (String)event.getValue();
                    Timer timer = timerMap.get(timerId);
                    if (timer != null) {
                        timer.task.run();
                        
                        if (timer.repeat) {
                            resetTimerTask(timer.task);                            
                        } else {
                            removeTimerTask(timer.task);
                        }
                    }
                }
            }
        });
        
        setExecutionThread(new Thread("ThinWire AppThread-" + id) {
            public void run() {
                try {
                    //set the frame to visible and then wait for the 
                    //frame size information and any other initial state
                    //events to return from the client.  The process those
                    //events and call the entry point for the application. 
                    synchronized (eventQueue) {
                        getFrame().setVisible(true);
                        
                        do {
                            eventQueue.wait();
                            WebComponentEvent event = eventQueue.remove(0);
                            
                            if (event != null) {
                                WebComponentListener wcl = getWebComponentListener((Integer) event.getSource());
                                if (wcl != null) wcl.componentChange(event);
                            }                            
                        } while (eventQueue.size() > 0);
                    }
                    
                    try {
                        Class clazz = Class.forName(mainClass);
                        clazz.getMethod("main", new Class[] { String[].class }).invoke(clazz, new Object[] { args });
                    } catch (Exception e) {
                        if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
                        throw (RuntimeException)e;
                    }                    
                    
                    //If the main method terminates but the frame is still visible, then wait for it to be closed.
                    while (getFrame().isVisible()) {
                        getFrame().setWaitForWindow(true);
                    }                                       
                                        
                    log.entering(WebApplication.class.getName(), "exit");
                    releaseThread(); // ?? The hideWindow does this, doesn't it?

                    // Call the client-side shutdown instance
                    callClientFunction(false, SHUTDOWN_INSTANCE,
                            new Object[] { "The application instance has shutdown. Press F5 to restart the"
                                    + " application or close the browser to end your session." });

                    if (userActionListener != null){
                    	userActionListener.stop();
                    }                    

                    // Set the execution thread to null,
                    // so that this application instance can get
                    // garbage collected.                    
                    setExecutionThread(null);
                    if (httpSession.getAttribute("instance") == WebApplication.this) httpSession.setAttribute("instance", null);                    
                    log.exiting(WebApplication.class.getName(), "exit");                    
                } catch (Exception e) {
                    reportException(null, e);
                }
            }
        });
        
        //Guarantee communication back and forth to the client every 60 seconds.  This causes an update
        //to the lastClientRequestTime and therefore prevents the app instance from being shutdown.
        //TODO: Make this feature and it's time configurable.
        this.addTimerTask(new Runnable() {
            public void run() {
                lastClientRequestTime = new Long(System.currentTimeMillis());
            }
        }, INSTANCE_KEEP_ALIVE_CYCLE, true);

        lastClientRequestTime = new Long(System.currentTimeMillis());
        
        synchronized (instanceMonitorRunnable) {
            if (instanceMonitorThread == null || !instanceMonitorThread.isAlive()) {
                instanceMonitorThread = new Thread(instanceMonitorRunnable, "ThinWire Instance Monitor Thread");
                instanceMonitorThread.setPriority(Thread.MIN_PRIORITY);
                instanceMonitorThread.start();
            }
        }

        getExecutionThread().start();
    }
    
    Integer getNextComponentId() {
        nextCompId = nextCompId == Integer.MAX_VALUE ? 1 : nextCompId + 1;
        return new Integer(nextCompId);
    }

    ComponentRenderer getRenderer(Component comp) {
        Class compClazz = comp.getClass();
        Class<ComponentRenderer> renderClazz = null;

        outer: while (compClazz != null) {
            String className = compClazz.getName();
            renderClazz = nameToRenderer.get(className);
            
            if (renderClazz == null) {
                List<Class> lst = new ArrayList<Class>();
                lst.add(compClazz);
                
                while (lst.size() > 0) {
                    compClazz = lst.remove(0);
                    className = compClazz.getName();
                    String qualClassName = PACKAGE_NAME + '.' + className.substring(className.lastIndexOf('.') + 1) + "Renderer";
                    
                    try {
                        renderClazz = (Class)Class.forName(qualClassName);
                        nameToRenderer.put(className, renderClazz);
                        break outer;
                    } catch (ClassNotFoundException e) {
                        //We'll continue trying until no classes in the hierarchy are left.
                    }
                    
                    Class sc = compClazz.getSuperclass();
                    if (Component.class.isAssignableFrom(sc)) lst.add(sc);
                    
                    for (Class i : compClazz.getInterfaces()) {
                        if (Component.class.isAssignableFrom(i)) lst.add(i);
                    }
                }                
            } else {
                break;
            }
        }
        
        if (renderClazz != null) {
            try {
                return (ComponentRenderer)renderClazz.newInstance();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("illegal access while trying to access " + renderClazz.getName(), e);
            } catch (InstantiationException e) {
                throw new RuntimeException(renderClazz.getName() + " could not be instantiated, using default renderer instead", e);
            }
        }
                
        throw new RuntimeException("Renderer for component class '" + comp.getClass() + "' not found");        
    }

    void notifySyncCallResponse(String response) {
        if (response == null) response = "";

        synchronized (syncCallResponse) {
            syncCallResponse[0] = response;
            syncCallResponse.notify();
        }
    }

    public String callClientFunction(boolean sync, String name, Object[] args) {
        return callClientFunction(sync, (Object)null, name, args);
    }

    public String callClientFunction(boolean sync, String objectName, String name, Object[] args) {
        return callClientFunction(sync, (Object)objectName, name, args);
    }

    public String callClientFunction(boolean sync, Integer objectId, String name, Object[] args) {
        return callClientFunction(sync, (Object)objectId, name, args);   
    }
    
    static void encodeObject(StringBuffer sb, Object o) {
        if (o == null) {
            sb.append("null");
        } else if (o instanceof Integer) {
            sb.append(String.valueOf(((Integer) o).intValue()));
        } else if (o instanceof Number) {
            sb.append(String.valueOf(((Number) o).doubleValue()));
        } else if (o instanceof Boolean) {
            sb.append(String.valueOf(((Boolean) o).booleanValue()));
        } else if (o instanceof StringBuffer) {
            sb.append(o.toString());
        } else {
            String s = o.toString();
            s = REGEX_DOUBLE_SLASH.matcher(s).replaceAll("\\\\\\\\");
            s = REGEX_DOUBLE_QUOTE.matcher(s).replaceAll("\\\\\"");
            s = REGEX_CRLF.matcher(s).replaceAll("\\\\r\\\\n");
            sb.append('\"').append(s).append('\"');
        }        
    }
    
    private String callClientFunction(boolean sync, Object objectId, String name, Object[] args) {
        StringBuffer sb = sbClientEvents;
        String ret = null;

        synchronized (sb) {
            processClientEvents = true;
            sb.append("{m:\"").append(name).append('\"');            
            
            if (objectId != null) {
                if (objectId instanceof Integer) {
                    sb.append(",i:").append(objectId);
                } else {
                    sb.append(",n:").append((String)objectId);
                }
            }

            if (args != null && args.length > 0) {
                sb.append(",a:[");

                for (int i = 0, cnt = args.length; i < cnt; i++) {
                    encodeObject(sb, args[i]);
                    sb.append(',');
                }

                sb.setCharAt(sb.length() - 1, ']');
            } else
                sb.append(",a:[]");

            if (sync) {
                sb.append(",s:1},");
                processClientEvents = true;
                sb.notify();
            } else {
                sb.append("},");

                if (sb.length() >= 1000) {
                    processClientEvents = true;
                    sb.notify();
                } else {
                    processClientEvents = false;
                }

                ret = null;
            }
        }

        if (sync) {
            synchronized (syncCallResponse) {
                if (syncCallResponse[0] == null) {
                    try {
                        long beforeWait = System.currentTimeMillis();
                        syncCallResponse.wait(120000);
                        
                        if (syncCallResponse[0] == null) {
                            long afterWait = System.currentTimeMillis();
                            StringBuffer sbscr = new StringBuffer();
                            sbscr.append("sendClientEvent did not respond within 120 seconds, methodName=");
                            sbscr.append(name);
                            sbscr.append(",timeBeforeWait=");
                            sbscr.append(beforeWait);
                            sbscr.append(",timeAfterWait=");
                            sbscr.append(afterWait);
                            sbscr.append(",argCount=");
                            sbscr.append(args.length);
                            sbscr.append(",argValues=");
                            
                            for (Object arg : args) {                                
                                sbscr.append('"').append(arg.toString()).append("\",");
                            }
                                                        
                            throw new IllegalStateException(sbscr.toString());
                        }
                    } catch (InterruptedException e) {
                        log.log(Level.SEVERE, null, e);
                    }
                }

                ret = syncCallResponse[0];
                syncCallResponse[0] = null;
            }
        }

        return ret;
    }
    
    public Integer getComponentId(Component comp) {
        Component w = comp;
        
        while (w != null && !(w instanceof Window)) {
            w  = (Component)w.getParent();
        }
        
        if (w != null) {
            WindowRenderer wr = windowToRenderer.get(w);
            return wr.getComponentId(comp);
        } else {
            return null;
        }
    }

    String getClientEvents() {
        String s = null;

        synchronized (sbClientEvents) {
            try {
                while (true) {
                    if (processClientEvents) {
                        synchronized (eventQueue) {
                            if (!threadWaiting) {
                                callClientFunction(false, "tw_em", "sendGetEvents", null);
                            }
                        }

                        int length = sbClientEvents.length();

                        if (length > 1) {
                            sbClientEvents.setCharAt(length - 1, ']');
                            s = sbClientEvents.substring(0, length);
                            sbClientEvents.setLength(0);
                            sbClientEvents.append('[');
                        }

                        processClientEvents = false;
                        break;
                    } else {
                        processClientEvents = true;
                        sbClientEvents.wait(100);
                    }
                }
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, null, e);
            }
        }

        return s;
    }

    void setWebComponentListener(Integer compId, WebComponentListener listener) {
        synchronized (webComponentListeners) {
            if (listener == null)
                webComponentListeners.remove(compId);
            else
                webComponentListeners.put(compId, listener);
        }
    }

    WebComponentListener getWebComponentListener(Integer compId) {
        synchronized (webComponentListeners) {
            return webComponentListeners.get(compId);
        }
    }

    public void queueWebComponentEvent(WebComponentEvent wce) {
        synchronized (eventQueue) {
            eventQueue.add(wce);
            eventQueue.notify();
        }
    }

    protected void captureThread() {
        int currentCaptureCount = ++captureCount;
        log.fine("increase captureCount:" + captureCount);
        threadCaptured = true;

        while (threadCaptured) {
            WebComponentEvent event;

            synchronized (eventQueue) {
                if (eventQueue.size() > 0) {
                    event = eventQueue.remove(0);
                    if (this.userActionListener != null){
                 	   this.notifyUserActionReceived(event);
                    }
                    if (this.playBackOn && !this.playBackEventReceived){
                    	this.playBackEventReceived = true;
                    	this.playBackStart = new Date().getTime();
                    }
                } else {
                    try {
                        threadWaiting = true;
                        eventQueue.wait();
                        threadWaiting = false;
                    } catch (InterruptedException e) {
                        log.log(Level.SEVERE, null, e);
                    }

                    event = null;
                }
            }

            if (event != null) {
                if (this.playBackOn){
                	this.flushClientEvents();
                	if (WebApplication.APPEVENT_SHUTDOWN.equals(event.getName())){
                		this.setPlayBackOn(false);
                	}
                }            	
                WebComponentListener wcl = getWebComponentListener((Integer) event.getSource());
                if (wcl != null) wcl.componentChange(event);
                if (currentCaptureCount == captureCount) threadCaptured = true;
            }
        }
    }

    protected void releaseThread() {
        threadCaptured = false;
        captureCount--;
        log.fine("decrease captureCount:" + captureCount);
    }

    protected void showWindow(Window w) {
        WindowRenderer wr = (WindowRenderer) windowToRenderer.get(w);
        if (wr != null) throw new IllegalStateException("A window cannot be set to visible while it is already visible");
        windowToRenderer.put(w, wr = (WindowRenderer) getRenderer(w));
        wr.ai = this;
        wr.render(wr, w, w instanceof Dialog ? windowToRenderer.get(getFrame()) : null);
        log.fine("Showing window with id:" + wr.id);
    }

    public void checkForUnwaitFrame() {
        if (getFrame().isWaitForWindow()) {
            boolean unwaitFrame = true;

            for (Window w : windowToRenderer.keySet()) {
                if (getFrame() != w && w.isWaitForWindow()) {
                    unwaitFrame = false;
                    break;
                }
            }

            if (unwaitFrame) getFrame().setWaitForWindow(false);
        }
    }

    protected void hideWindow(Window w) {
        WindowRenderer wr = (WindowRenderer) windowToRenderer.remove(w);
        if (wr == null) throw new IllegalStateException("Cannot close a window that has not been set to visible");
        log.fine("Closing window with id:" + wr.id);
        wr.destroy();
        checkForUnwaitFrame();
    }

    WindowRenderer getWindowRenderer(Window w) {
        return (WindowRenderer) windowToRenderer.get(w);
    }

    List<FileChooser.FileInfo> getFileInfoList() {
        return fileChooser.getFileInfoList();
    }

    protected List<FileInfo> showFileChooser(boolean showDescription, boolean multiFile) {
        fileChooser = new WebFileChooser(this);
        fileChooser.show(showDescription, multiFile);
        return fileChooser.getFileInfoList();
    }

    public void addTimerTask(Runnable task, long timeout) {
        addTimerTask(task, timeout, false);
    }

    public void addTimerTask(Runnable task, long timeout, boolean repeat) {
        String timerId = String.valueOf(System.identityHashCode(task));
        
        if (timerMap.containsKey(timerId)) {
            resetTimerTask(task);
        } else {
            Timer timer = new Timer();
            timer.task = task;
            timer.timeout = timeout;
            timer.repeat = repeat;
            timerMap.put(timerId, timer);
            callClientFunction(false, "tw_addTimerTask", new Object[] {timerId, timeout});
        }
    }

    public void resetTimerTask(Runnable task) {
        String timerId = String.valueOf(System.identityHashCode(task));
        Timer timer = timerMap.get(timerId);        
        if (timer != null) callClientFunction(false, "tw_addTimerTask", new Object[] {timerId, timer.timeout});
    }

    public void removeTimerTask(Runnable task) {
        String timerId = String.valueOf(System.identityHashCode(task));
        callClientFunction(false, "tw_removeTimerTask", new Object[] {timerId});
        timerMap.remove(timerId);
    }

    protected void setPackagePrivateMember(String memberName, Component comp, Object value) {
        super.setPackagePrivateMember(memberName, comp, value);
    }    

    public void setUserActionListener(UserActionListener listener) {
		this.userActionListener = listener;
	}

	private void notifyUserActionReceived(WebComponentEvent evt) {
		UserActionEvent uae = new UserActionEvent(evt);
		this.userActionListener.actionReceived(uae);
	}
    
    protected void finalize() {
        log.log(Level.FINER, "finalizing app " + this.id);
    }

	public void setPlayBackOn(boolean playBackOn){
		this.playBackOn = playBackOn;
		if (!this.playBackOn){
			this.endPlayBack();
		}
	}
	
	private void flushClientEvents() {
		synchronized (sbClientEvents) {
			sbClientEvents.setLength(0);
			sbClientEvents.append('[');
		}
	}
	
	private void endPlayBack(){
		log.entering("ThinWireApplication", "endPlayBack");
		this.playBackDuration = new Date().getTime() - this.playBackStart;
		StringBuffer sb = new StringBuffer(EOL + EOL);
		sb.append(Thread.currentThread().getName()
				+ " Playback Statistics" + EOL);
		sb.append("-----------------------------------------------------" + EOL);
		sb.append("Duration of recording session:  " + this.recordDuration + EOL);
		sb.append(" Duration of playback session:  " + this.playBackDuration + EOL);
		DecimalFormat df = new DecimalFormat();
		df.setMinimumFractionDigits(2);
		df.setMinimumIntegerDigits(1);
		double drecord = new Long(this.recordDuration).doubleValue();
		double dplay = new Long(this.playBackDuration).doubleValue();
		double pctChange = (((dplay/drecord) - 1) * 100);
		sb.append("                     % change:  " + df.format(pctChange)  + EOL + EOL);
		log.info(sb.toString());	
		log.exiting("ThinWireApplication", "endPlayBack");
	}

	public void setRecordDuration(long recordDuration) {
		this.recordDuration = recordDuration;
	}
	
}
