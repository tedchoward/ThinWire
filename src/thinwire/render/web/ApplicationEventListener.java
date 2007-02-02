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
import thinwire.ui.Frame;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

class ApplicationEventListener implements WebComponentListener {
    private static final Logger log = Logger.getLogger(ApplicationEventListener.class.getName());
    private static final Level LEVEL = Level.FINER;
    
    static final Integer ID = new Integer(Integer.MAX_VALUE);
    private static final String SHUTDOWN_INSTANCE = "tw_shutdownInstance";
    private static final String INIT = "INIT";
    private static final String STARTUP = "STARTUP";
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
        StartupInfo info = new StartupInfo(mainClass, args);
        return new WebComponentEvent(ID, STARTUP, info);
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

        if (INIT.equals(name)) {
            app.sendStyleInitInfo();
            Frame f = app.getFrame();
            f.setVisible(true);
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": Testing synchornized client-side call by retrieving client time");
            String time = app.clientSideFunctionCallWaitForReturn("tw_getTime");
            if (log.isLoggable(LEVEL)) log.log(LEVEL, Thread.currentThread().getName() + ": Client time in milliseconds is " + time);

            //When the frame is set to non-visible, fire a shutdown event
            f.addPropertyChangeListener(Frame.PROPERTY_VISIBLE, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent pce) {
                    if (pce.getNewValue() == Boolean.FALSE) {
                        try {
                            app.shutdown(null);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        } else if (STARTUP.equals(name)) {
            ApplicationEventListener.StartupInfo info = (ApplicationEventListener.StartupInfo)event.getValue();

            try {
                Class clazz = Class.forName(info.mainClass);
                clazz.getMethod("main", new Class[] { String[].class }).invoke(clazz, new Object[] { info.args });
            } catch (Exception e) {
                if (!(e instanceof RuntimeException)) e = new RuntimeException(e);
                throw (RuntimeException)e;
            }                    
        } else if (SHUTDOWN.equals(name)) {
            if (app.getFrame().isVisible()) app.getFrame().setVisible(false);
        } else if (RUN_TIMER.equals(name)) {
            String timerId = (String)event.getValue();
            Timer timer = app.timerMap.get(timerId);
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