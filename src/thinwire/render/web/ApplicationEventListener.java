/*
 * Created on Jan 29, 2007
  */
package thinwire.render.web;

import java.util.logging.Logger;

import thinwire.render.web.WebApplication.Timer;
import thinwire.ui.Frame;
import thinwire.ui.event.PropertyChangeEvent;
import thinwire.ui.event.PropertyChangeListener;

class ApplicationEventListener implements WebComponentListener {
    private static final Logger log = Logger.getLogger(ApplicationEventListener.class.getName());
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

            //When the frame is set to non-visible, fire a shutdown event
            f.addPropertyChangeListener(Frame.PROPERTY_VISIBLE, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent pce) {
                    if (pce.getNewValue() == Boolean.FALSE) app.shutdown();
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
            if (app.getFrame().isVisible()) {
                app.getFrame().setVisible(false);
            } else {
                //app.eventProcessor.interrupt();

                // Call the client-side shutdown instance
                app.clientSideFunctionCall(SHUTDOWN_INSTANCE, 
                        "The application instance has shutdown. Press F5 to restart the application or close the browser to end your session.");

                if (app.userActionListener != null) app.userActionListener.stop();

                if (app.httpSession.getAttribute("instance") == app) app.httpSession.setAttribute("instance", null);                    
            }
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