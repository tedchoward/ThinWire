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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import thinwire.render.web.WebApplication.Timer;
import thinwire.ui.Dialog;
import thinwire.ui.Frame;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

class ApplicationEventListener implements WebComponentListener {
    private static final Logger log = Logger.getLogger(ApplicationEventListener.class.getName());
    private static final Level LEVEL = Level.FINER;
    
    static final Integer ID = new Integer(Integer.MAX_VALUE);
    //private static final String SHUTDOWN_INSTANCE = "tw_shutdownInstance";
    private static final String INIT = "INIT";
    private static final String STARTUP = "STARTUP";
    private static final String REPAINT = "REPAINT";
    static final String SHUTDOWN = "SHUTDOWN";
    private static final String RUN_TIMER = "RUN_TIMER";
    
    private static final class StartupInfo {
        String mainClass;
        String[] args;
        
        StartupInfo(String mainClass, String[] args) {
            this.mainClass = mainClass;
            this.args = args;
        }
    }
    
    static final WebComponentEvent newRunTimerEvent(String timerId) {
        return new WebComponentEvent(ID, RUN_TIMER, timerId);
    }

    static final WebComponentEvent newInitEvent() {
        return new WebComponentEvent(ID, INIT, null);
    }
    
    static final WebComponentEvent newStartEvent(String mainClass, String[] args) {
    	if (mainClass == null || mainClass.length() == 0) throw new IllegalArgumentException("The init-param 'mainClass' is required and must point to your application's entry point");
        StartupInfo info = new StartupInfo(mainClass, args);
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
            f.setVisible(true);
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": testing synchornized client-side call by retrieving client time");
            String time = app.clientSideFunctionCallWaitForReturn("tw_getTime");
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": client time in milliseconds is " + time);

            //When the frame is set to non-visible, fire a shutdown event
            f.addPropertyChangeListener(Frame.PROPERTY_VISIBLE, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent pce) {
                    if (pce.getNewValue() == Boolean.FALSE) {
                        if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": frame visibility set to false, signaling shutdown");
                        app.signalShutdown();
                    }
                }
            });
        } else if (STARTUP.equals(name)) {
            ApplicationEventListener.StartupInfo info = (ApplicationEventListener.StartupInfo)event.getValue();

            try {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": calling entry point class=" + info.mainClass);
                Class clazz = Class.forName(info.mainClass);
                clazz.getMethod("main", new Class[] { String[].class }).invoke(clazz, new Object[] { info.args });
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
        	app.sendDefaultComponentStyles(fr);
            fr.render(fr, f, null);
            
            for (Dialog d : f.getDialogs()) {
            	WindowRenderer dr = app.getWindowRenderer(d);
            	dr.render(dr, d, fr);
            }
        } else if (SHUTDOWN.equals(name)) {
            Frame f = app.getFrame();
            
            if (f.isVisible()) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": shutdown event changing frame visibility to false");

                boolean captured = false;
                
                for (Dialog d : f.getDialogs()) {
                    if (d.isWaitForWindow()) captured = true;
                }
                
                //TODO Depends on this listener being the last listener executed once we toggle visibility of frame to false
                //This code attempts to force an exception that won't be caught so that the call stack unrolls properly in
                //cases where a waitForWindow dialog is in use.  It's important that this execute as the last listener so
                //that all other visibility listeners have a chance to execute properly.
                if (captured) {
                    f.addPropertyChangeListener(Frame.PROPERTY_VISIBLE, new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent pce) {
                            if (pce.getNewValue() == Boolean.FALSE) {
                                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": attempting to unroll stack naturally since their is a waitForWindow Dialog");
                                throw new EventProcessor.GracefulShutdown(); 
                            }
                        }
                    });
                }
                
                f.setVisible(false);
            } else {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": shutdown event doesn't need to do anything, frame already has visible set to false");
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