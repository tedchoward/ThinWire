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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import thinwire.render.web.WebApplication.Timer;
import thinwire.ui.Dialog;
import thinwire.ui.Frame;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

public class ApplicationEventListener implements WebComponentListener {
    private static final Logger log = Logger.getLogger(ApplicationEventListener.class.getName());
    private static final Level LEVEL = Level.FINER;
    
    static final Integer ID = new Integer(Integer.MAX_VALUE);
    private static final String INIT = "INIT";
    private static final String STARTUP = "STARTUP";
    private static final String REPAINT = "REPAINT";
    static final String SHUTDOWN = "SHUTDOWN";
    private static final String RUN_TIMER = "RUN_TIMER";
    
    private static final class StartupInfo {
        Class mainClass;
        String[] args;
        String initialFrameTitle;
        
        StartupInfo(Class mainClass, String[] args, String initialFrameTitle) {
            this.mainClass = mainClass;
            this.args = args;
            this.initialFrameTitle = initialFrameTitle;
        }
    }
    
    static final WebComponentEvent newRunTimerEvent(String timerId) {
        return new WebComponentEvent(ID, RUN_TIMER, timerId);
    }

    static final WebComponentEvent newInitEvent() {
        return new WebComponentEvent(ID, INIT, null);
    }
    
    static final WebComponentEvent newStartEvent(Class mainClass, String[] args, String initialFrameTitle) {
        StartupInfo info = new StartupInfo(mainClass, args, initialFrameTitle);
        return new WebComponentEvent(ID, STARTUP, info);
    }
    
    static final WebComponentEvent newRepaintEvent() {
    	return new WebComponentEvent(ID, REPAINT, null);
    }
    
    static final WebComponentEvent newShutdownEvent() {
        return new WebComponentEvent(ID, SHUTDOWN, null);
    }
    
    private WebApplication app;
    
    ApplicationEventListener(WebApplication app) {
        this.app = app;
    }
    
    public void componentChange(WebComponentEvent event) {
        String name = event.getName();
        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": handling application event name=" + name);

        if (INIT.equals(name)) {
            app.sendStyleInitInfo();
            Frame f = app.getFrame();
            f.setTitle(((StartupInfo)app.startupEvent.getValue()).initialFrameTitle);
            f.setVisible(true);
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": testing synchornized client-side call by retrieving client time");
            String time = app.clientSideFunctionCallWaitForReturn("tw_getTime");
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": client time in milliseconds is " + time);

            //When the frame is set to non-visible, fire a shutdown event
            //NOTE: The proper execution of this behavior depends on the this listener being run first
            f.addPropertyChangeListener(Frame.PROPERTY_VISIBLE, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent pce) {
                    if (pce.getNewValue() == Boolean.FALSE) {
                        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": frame visibility set to false, signaling shutdown");
                        app.state = WebApplication.State.SHUTDOWN;
                        //Triggers a return and initiates full shutdown, preventing anything else from being written out to the browser
                        app.flushEvents();
                    }
                }
            });
        } else if (STARTUP.equals(name)) {
            ApplicationEventListener.StartupInfo info = (ApplicationEventListener.StartupInfo)event.getValue();

            try {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": calling entry point class=" + info.mainClass);
                info.mainClass.getMethod("main", new Class[] { String[].class }).invoke(info.mainClass, new Object[] { info.args });
            } catch (Exception e) {
                Throwable th = e;          
                while (th.getCause() != null) th = th.getCause();                
                
                if (th instanceof EventProcessor.GracefulShutdown) {
                    if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": graceful shutdown from entry point main: " + info.mainClass);                    
                } else {
                	if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
                    throw (RuntimeException)e;
                }
            }
        } else if (REPAINT.equals(name)) {
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": repainting the application frame");
            app.sendStyleInitInfo();
            Frame f = app.getFrame();
            WindowRenderer fr = app.getWindowRenderer(f);
        	app.sendDefaultComponentStyles();
            fr.render(fr, f, null);
            
            for (Dialog d : f.getDialogs()) {
            	WindowRenderer dr = app.getWindowRenderer(d);
            	dr.render(dr, d, fr);
            }
            
            for (Map.Entry<String, Timer> entry : app.timerMap.entrySet()) {
            	app.clientSideFunctionCall("tw_addTimerTask", entry.getKey(), entry.getValue().timeout);
            }

            app.reloadFavicon();
        } else if (SHUTDOWN.equals(name)) {
            Frame f = app.getFrame();
            StringBuilder capturedTitles = null;
            
            Dialog[] diags = f.getDialogs().toArray(new Dialog[f.getDialogs().size()]);
            
            //Walk the dialogs from bottom up so the most recent dialog closes first.
            for (int i = diags.length; --i >= 0;) {
            	Dialog d = diags[i];
            	
                if (d.isWaitForWindow()) {
                	if (capturedTitles == null) {
                		capturedTitles = new StringBuilder();
                	} else {
                		capturedTitles.append(", ");
                	}
                	
                	capturedTitles.append('\'').append(d.getTitle()).append('\'');
                }

                if (d.isVisible()) {
            		try {
            			d.setVisible(false);
            		} catch (Exception e) {
                        if (log.isLoggable(Level.WARNING)) log.log(Level.WARNING, Thread.currentThread().getName() + ": exception setting dialog visible property to 'false' during shutdown", e);                    
            		}
            	}
            }

    		try {
    			f.setVisible(false);
    		} catch (Exception e) {
                if (log.isLoggable(Level.WARNING)) log.log(Level.WARNING, Thread.currentThread().getName() + ": exception setting frame visible property to 'false' during shutdown", e);                    
    		}

            if (capturedTitles != null) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": attempting to unroll stack naturally since their is a waitForWindow Dialog");
                //NOTE: If a developer ever uses a try/catch on Error, they will be able to intercept
                //the following graceful shutdown exception, in which case the behavior is undetermined.
                throw new EventProcessor.GracefulShutdown();
            }
        } else if (RUN_TIMER.equals(name)) {
            String timerId = (String)event.getValue();
            Timer timer = app.timerMap.get(timerId);
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": attempting to run timerId=" + timerId +
                    ", timer=" + timer + (timer == null ? "" : ",repeat=" + timer.repeat));

            if (timer != null) {
                timer.task.run();
                
                if (timer.repeat) {
                    app.resetTimerTask(timer.task);                            
                } else {
                    app.removeTimerTask(timer.task);
                }
            }
        }
    }
}