/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
package thinwire.ui.event;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.EventObject;

import thinwire.ui.Application;

/**
 * @author Joshua J. Gertzen
 */
public final class ExceptionEvent extends EventObject {
    private static final String DEFAULT_MESSAGE = "An exception has occurred in the application. " +
        "If you are unable to continue, please press F5 to restart the application.";
    
    private Throwable exception;
    private String defaultMessage = DEFAULT_MESSAGE;
    private boolean canceled;
    private boolean stopPropagation;
    private boolean suppressLogging;
    
    public ExceptionEvent(Object source, Throwable exception) {
        super(source == null ? (Application.current() == null ? Application.class : Application.current()) : source);
        this.exception = exception;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isStopPropagation() {
        return stopPropagation;
    }

    public boolean isSuppressLogging() {
        return suppressLogging;
    }        
    
    public void cancel() {
        cancel(false, false);
    }        

    public void cancel(boolean stopPropagation) {
        cancel(stopPropagation, false);
    }
    
    public void cancel(boolean stopPropagation, boolean suppressLogging) {
        this.canceled = true;            
        if (this.stopPropagation && !stopPropagation) throw new IllegalStateException("the propgation of this event has already been stopped");
        this.stopPropagation = stopPropagation;
        if (this.suppressLogging && !suppressLogging) throw new IllegalStateException("the logging of this event has already been suppressed");
        this.suppressLogging = suppressLogging;
    }        
            
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    public void setDefaultMessage(String defaultMessage) {
        if (defaultMessage == null || defaultMessage.length() == 0) defaultMessage = DEFAULT_MESSAGE;
        this.defaultMessage = defaultMessage;            
    }
            
    public Throwable getException() {
        return exception;
    }
    
    public String getStackTraceText() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        Throwable e = exception;
        
        do {
            e.printStackTrace(ps);                      
        } while ((e = e.getCause()) != null);
        
        return baos.toString();
    }
}