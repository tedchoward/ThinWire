/*
#IFNDEF ALT_LICENSE
                           ThinWire(R) RIA Ajax Framework
                 Copyright (C) 2003-2006 Custom Credit Systems

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
//Manages the communication of both outbound and inbound events
var tw_EventManager = Class.extend({    
    _EVENT_WEB_COMPONENT: 0,
    _EVENT_GET_EVENTS: 1,
    _EVENT_SYNC_CALL: 2,
    _EVENT_RUN_TIMER: 3,
    _s: true,
    _outboundEvents: null,
    _vsEventOrder: null,
    _activityInd: null,
    _activityIndSet: false,
    _activityIndTimer: 0,
    _timerId: 0,
    _comm: null,
    
    construct: function() {        
        this._sendEvents = this._sendEvents.bind(this);
        this._inboundEventListener = this._inboundEventListener.bind(this);
        this._hideActivityInd = this._hideActivityInd.bind(this);
        this._sr = this.sendSyncResponse; //Abbreviated version used by the server        
    },
    
    _resetTimer: function(time) {        
        clearTimeout(this._timerId);
        this._timerId = setTimeout(this._sendEvents, time);                    
    },
    
    _inboundEventListener: function(calls) {
        if (calls.length > 0) {
            try {
                var r, o; //NOTE: this needs to be here because it's used by eval(calls);
                eval(calls);
            } catch (e) {
                alert("SYNTAX ERROR WITH eval(calls): " + calls);
            }
        }        
        
        this._setActivityIndVisible(false);
    },

    _setActivityIndVisible: function(state) {
        if (this._activityInd != null) {
            if (!this._activityIndSet && window.tw_IMAGE_ACTIVITYINDICATOR != undefined) {
                this._activityInd.src = tw_IMAGE_ACTIVITYINDICATOR;
                this._activityIndSet = true;
            }
            
            if (state) {
                if (this._activityIndTimer != 0) {
                    clearTimeout(this._activityIndTimer);
                    this._activityIndTimer = 0;                    
                } else if (this._activityIndSet) {
                    this._activityInd.style.zIndex = ++tw_Component.zIndex;
                    this._activityInd.style.display = "block";
                }
            } else {
                if (this._activityIndTimer != 0) clearTimeout(this._activityIndTimer);
                this._activityIndTimer = setTimeout(this._hideActivityInd, 300);
            }
        }
    },
    
    _hideActivityInd: function() {
        this._activityIndTimer = 0;
        if (this._activityInd != null) this._activityInd.style.display = "none";        
    },
        
    _sendEvents: function() {
        if (this._comm.isReady()) {
            this._setActivityIndVisible(true);
            var msg = this._outboundEvents.join(":");                
            this._outboundEvents = [];
            this._vsEventOrder = {};
            this._comm.send("POST", tw_APP_URL, msg);           
        } else { //retrigger if comm is not ready yet 
            this._resetTimer(100);
        }
    },
        
    _sendOutboundEvent: function(type, value) {
        this._queueOutboundEvent(type, value);
        this._resetTimer(0);
    }, 
    
    _postOutboundEvent: function(type, value) {        
        this._queueOutboundEvent(type, value);
        this._resetTimer(100);
    },
    
    _queueOutboundEvent: function(type, value) {
        this._outboundEvents.push(type);
        if (value == null) value = "";
        this._outboundEvents.push(value);
    },
    
    sendViewStateChanged: function(id, name, value) {
        if (!(value instanceof String) && typeof(value) != "string") value = new String(value);
        value = id + ":" + name + (value == null ? ":0:" : ":" + value.length + ":" + value);
        this._sendOutboundEvent(this._EVENT_WEB_COMPONENT, value);
    },
        
    postViewStateChanged: function(id, name, value) {    
        if (!(value instanceof String) && typeof(value) != "string") value = new String(value);
        value = id + ":" + name + (value == null ? ":0:" : ":" + value.length + ":" + value);
        this._postOutboundEvent(this._EVENT_WEB_COMPONENT, value);
    },
    
    removeQueuedViewStateChange: function(key) {
        var order = this._vsEventOrder[key];
        
        if (order != undefined) {
            delete this._vsEventOrder[key];
            this._outboundEvents[order - 1] = this._EVENT_GET_EVENTS;
            this._outboundEvents[order] = "";
        }
    },
    
    queueViewStateChanged: function(id, name, value, key) {
        if (!(value instanceof String) && typeof(value) != "string") value = new String(value);
        value = id + ":" + name + (value == null ? ":0:" : ":" + value.length + ":" + value);
        if (key == null) key = id + ":" + name;
        var order = this._vsEventOrder[key];
                                        
        if (order == undefined) {            
            this._queueOutboundEvent(this._EVENT_WEB_COMPONENT, value);
            this._vsEventOrder[key] = this._outboundEvents.length - 1;            
        } else {
            this._outboundEvents[order] = value;
        }                            
    },
        
    sendGetEvents: function() { this._sendOutboundEvent(this._EVENT_GET_EVENTS, null); },    
    sendRunTimer: function(id) { this._sendOutboundEvent(this._EVENT_RUN_TIMER, id + ":"); },    
        
    manualSyncResponse: function() {
        this._s = false;
    },
    
    sendSyncResponse: function(value) {
        if (!(value instanceof String) && typeof(value) != "string") value = new String(value);
        value = value == null ? "0:" : value.length + ":" + value;
        this._sendOutboundEvent(this._EVENT_SYNC_CALL, value);
    },
    
    start: function() {
        this._activityInd = document.createElement("img");
        var s = this._activityInd.style;
        s.position = "absolute";
        s.right = "1px";
        s.top = "1px";
        s.display = "none";
        document.body.appendChild(this._activityInd);                
        
        this._comm = new tw_HttpRequest(this._inboundEventListener);
        this._outboundEvents = [];
        this._vsEventOrder = {};
        this.sendGetEvents();
    },
    
    stop: function() {
        if (this._timerId != 0) clearTimeout(this._timerId);
        if (this._activityIndTimer != 0) clearTimeout(this._activityIndTimer);
        this._activityIndTimer = this._timerId = 0;
        if (this._comm != null) this._comm.abort();
        document.body.removeChild(this._activityInd);
        this._outboundEvents = this._vsEventOrder = this._comm = this._activityInd = null;
    }         
});

