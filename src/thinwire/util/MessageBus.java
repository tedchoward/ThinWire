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
public class MessageBus<T> extends EventBus<MessageBus.Listener, MessageBus.Message<T>, T> {
    public static interface Listener extends java.util.EventListener {
        public void messageReceived(Message ev);
    }
    
    public static class Message<T> extends java.util.EventObject {
        private Object data;
        private Object reply;
        private boolean replySent;
        
        public Message(T id) {
            this(id, null);
        }
        
        public Message(T id, Object data) {
        	super(id);
            this.data = data;
        }
        
        @SuppressWarnings("unchecked")
        public T getId() {
            return (T)getSource();
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
    
    private List<Message<T>> queue = new LinkedList<Message<T>>();
    
    public Object send(T id, Object data) {
    	return send(new Message<T>(id, data));
    }
    
    public Object send(Message<T> ev) {
        queue.add(ev);
        flush();
        return ev.reply;
    }

    public void post(T id, Object data) {
    	post(new Message<T>(id, data));
    }
    
    public void post(Message<T> ev) {
    	queue.add(ev);
    	if (queue.size() == 1) flush();
    }
    
    private void flush() {
    	while (queue.size() > 0) {
    		Message<T> ev = queue.remove(0);
    		if (ev == null) throw new IllegalArgumentException("event == null");
    		fireEvent(ev, ev.getId());
    	}
    }

    protected void runListener(Listener el, Message<T> eo) {
		el.messageReceived(eo);
	}

	protected T validate(T subType) {
		return subType;
	}
}