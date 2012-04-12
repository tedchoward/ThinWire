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

import java.util.*;
import java.util.logging.*;

/**
 * @author Joshua J. Gertzen
 */
class EventProcessorPool {
    private static final Logger log = Logger.getLogger(EventProcessorPool.class.getName());
    private static final Level LEVEL = Level.FINER;
    
    static final EventProcessorPool INSTANCE = new EventProcessorPool();

    private List<EventProcessor> pool = new LinkedList<EventProcessor>();
    private Map<WebApplication, EventProcessor> appToProcessor = new HashMap<WebApplication, EventProcessor>();
    
    private EventProcessorPool() { }
    
    EventProcessor getProcessor(WebApplication app) {
        synchronized (appToProcessor) {
            EventProcessor proc = appToProcessor.get(app);
            
            if (proc == null) {
                if (pool.size() > 0) {
                    proc = pool.remove(0);
                    if (log.isLoggable(LEVEL)) log.log(LEVEL, "Allocating " + proc.getName() + " from pool");
                } else {
                    proc = new EventProcessor(this);
                    proc.start();
                    if (log.isLoggable(LEVEL)) log.log(LEVEL, "New " + proc.getName() + " from pool");
                }
                
                proc.app = app;
                appToProcessor.put(app, proc);
            } else {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "Existing " + proc.getName() + " allocated to app");
            }
            
            return proc;
        }
    }
    
    void returnToPool(EventProcessor proc) {
        synchronized (appToProcessor) {
            if (proc.app == null) {
            	if (log.isLoggable(LEVEL)) log.log(LEVEL, "No application tied to EventProcessor " + proc.getName() + ", probably removed from pool in processUserActionEvents finally block");
            	return;
            }
            
            if (!proc.isInUse()) {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "Returning " + proc.getName() + " to pool");
                appToProcessor.remove(proc.app);
                proc.app = null;
                pool.add(proc);
            } else {
                if (log.isLoggable(LEVEL)) log.log(LEVEL, "Cannot return " + proc.getName() + " because thread is waiting in unreturnable call");
            }
        }
    }
    
    void removeFromPool(EventProcessor proc) {
        synchronized (appToProcessor) {
            if (log.isLoggable(LEVEL)) log.log(LEVEL, "Removing " + proc.getName() + " from pool - (proc.app == null) == " + (proc.app == null));            
            if (proc.app != null) appToProcessor.remove(proc.app);
            proc.app = null;
            pool.remove(proc);
        }
    }
}
