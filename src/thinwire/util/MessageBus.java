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
package thinwire.util;

import java.util.*;

/**
 * @author Joshua J. Gertzen
 */
public class MessageBus {
    static interface Listener {
        public void eventOccured(Event ev);
    }
    
    static class Event {
        private String id;
        private Object data;
        private Object reply;
        private boolean replySent;
        
        public Event(String id) {
            this(id, null);
        }
        
        public Event(String id, Object data) {
            if (id == null) throw new IllegalArgumentException("id == null");
            this.id = id;
            this.data = data;
        }
        
        public String getId() {
            return id;
        }
        
        public Object getData() {
            return data;
        }
                
        public void reply(Object reply) {
        	if (replySent) throw new IllegalStateException("reply already sent to this event: " + this.reply);
        	this.reply = reply;
        	replySent = true;
        }
    }    
    
    private Map<String, Set<Listener>> listeners = new HashMap<String, Set<Listener>>();
    private List<Event> queue = new LinkedList<Event>();
    
    public void addListener(String id, Listener listener) {
        if (id == null) throw new IllegalArgumentException("id == null");
        if (listener == null) throw new IllegalArgumentException("listener == null");
        Set<Listener> set = listeners.get(id);
        if (set == null) listeners.put(id, set = new HashSet<Listener>());
        set.add(listener);
    }
    
    public void removeListener(String id, Listener listener) {
        if (id == null) throw new IllegalArgumentException("id == null");
        if (listener == null) throw new IllegalArgumentException("listener == null");
        Set<Listener> set = listeners.get(id);
        if (set == null) return;
        set.remove(listener);
    }
    
    public Object send(String id, Object data) {
    	return send(new Event(id, data));
    }
    
    public Object send(Event ev) {
        queue.add(ev);
        flush();
        return ev.reply;
    }

    public void post(String id, Object data) {
    	post(new Event(id, data));
    }
    
    public void post(Event ev) {
    	queue.add(ev);
    	if (queue.size() == 1) flush();
    }
    
    private void flush() {
    	while (queue.size() > 0) {
    		Event ev = queue.remove(0);
    		if (ev == null) throw new IllegalArgumentException("event == null");
            Set<Listener> set = listeners.get(ev.getId());
            
            if (set != null) {
                for (Listener l : set.toArray(new Listener[set.size()])) {
                    l.eventOccured(ev);
                }
            }
    	}
    }
}
