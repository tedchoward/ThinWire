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
//Manages the communication of both outbound and inbound events
var tw_EventManager = Class.extend({    
    _EVENT_WEB_COMPONENT: 0,
    _EVENT_GET_EVENTS: 1,
    _EVENT_SYNC_CALL: 2,
    _EVENT_RUN_TIMER: 3,
    _autoSyncResponse: true,
    _outboundEvents: null,
    _vsEventOrder: null,
    _activityInd: null,
    _activityIndTimer: 0,
    _timerId: 0,
    _comm: null,
    
    construct: function() {        
        this._sendEvents = this._sendEvents.bind(this);
        this._inboundEventListener = this._inboundEventListener.bind(this);
        this._hideActivityInd = this._hideActivityInd.bind(this);        
    },

    _setActivityIndVisible: function(state) {
		if (this._activityInd == null && window.tw_IMAGE_ACTIVITYINDICATOR != undefined) {
			this._activityInd = document.createElement("img");
			tw_Component.setCSSText("position:absolute;right:1px;top:1px;display:none;", this._activityInd);
	        document.body.appendChild(this._activityInd);                
            this._activityInd.src = tw_IMAGE_ACTIVITYINDICATOR;
		}
	
        if (this._activityInd != null) {
            if (state) {
                if (this._activityIndTimer != 0) {
                    clearTimeout(this._activityIndTimer);
                    this._activityIndTimer = 0;                    
                } else {
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
    
    _inboundEventListener: function(calls) {
        if (calls.length > 0) {
            try {
                eval("calls = " + calls);

	            for (var i = 0, cnt = calls.length; i < cnt; i++) {
	                var call = calls[i];
	
					try {
		                var obj = call.i == undefined ? (call.n == undefined ? window : call.n) : tw_Component.instances[call.i];                    

		                if (obj != null) {
		                    var ret = obj[call.m].apply(obj, call.a);            
		                    if (call.s && this._autoSyncResponse) this.sendSyncResponse(ret, true);
		                }
					} catch (e) {
						var eAry = ["ERROR INVOKING call ", i, " of ", cnt, ": ", call.m, "\n"];
						
						for (var item in e)
							eAry.push("error:" + item + "=" + (e[item].length > 100 ? e[item].substring(0, 100) : e[item]) + "\n");
					    
						for (var ai = 0; ai < call.a.length; ai++)
							eAry.push("arg" + ai + "=" + call.a[ai] + "\n");
							
						alert(eAry.join(""));
					}
	            }            
            } catch (e) {
				var eAry = ["SYNTAX ERROR WITH eval(calls):\n"];

				if (e instanceof String || typeof e == "string") {
					eAray.push("error:" + e);
				} else { 
				    for (var item in e)
						eAry.push("error:" + item + "=" + (e[item].length > 100 ? e[item].substring(0, 100) : e[item]) + "\n");
				}

				eAry.push("calls=" + (calls != null && calls.length > 250 ? calls.substring(0, 250) : calls))
				alert(eAry.join(""));
            } 
        }        
        
        this._setActivityIndVisible(false);
    },
        
    _sendEvents: function() {
        if (this._comm != null) {
            if (this._comm.isReady()) {
                this._setActivityIndVisible(true);
                var msg = this._outboundEvents.join(":");                
                this._outboundEvents = [];
                this._vsEventOrder = {};
                this._comm.send("POST", tw_APP_URL, msg);
                this._timerId = 0;
            } else {
                this._resetTimer(100);
            }
        }
    },
    
    _queueOutboundEvent: function(type, value) {
        this._outboundEvents.push(type);
        if (value == null) value = "";        
        this._outboundEvents.push(value);
    },

	_resetTimer: function(delay) {
        if (this._timerId != 0) clearTimeout(this._timerId);
        this._timerId = setTimeout(this._sendEvents, delay); //delay send until callstack completes execution                  
	},
	
	resetSendEventsTimer: function(delay) {
		if (this._timerId != 0) this._resetTimer(delay);
	},
    
    sendViewStateChanged: function(id, name, value) {
		this.queueViewStateChanged(id, name, value);
		this._resetTimer(0);
    },

    postViewStateChanged: this.sendViewStateChanged, //here in case there is an existing reference in 3rd party code
    
    queueViewStateChanged: function(id, name, value, key) {
        value = id + ":" + name + (value == null ? ":0:" : ":" + new String(value).length + ":" + value);
        if (key == null) key = id + ":" + name;
        var order = this._vsEventOrder[key];
                                        
        if (order == undefined) {            
            this._queueOutboundEvent(this._EVENT_WEB_COMPONENT, value);
            this._vsEventOrder[key] = this._outboundEvents.length - 1;            
        } else {
            this._outboundEvents[order] = value;
        }                            
    },
    
    removeQueuedViewStateChange: function(key) {
        var order = this._vsEventOrder[key];
        
        if (order != undefined) {
            delete this._vsEventOrder[key];
            this._outboundEvents[order - 1] = this._EVENT_GET_EVENTS;
            this._outboundEvents[order] = "";
        }
    },
        
    sendGetEvents: function() {
	    this._queueOutboundEvent(this._EVENT_GET_EVENTS, null);
		this._resetTimer(0);
	},    
	
    sendRunTimer: function(id) {
		this._queueOutboundEvent(this._EVENT_RUN_TIMER, id + ":");
		this._resetTimer(0);
	},
	    
	manualSyncResponse: function() { this._autoSyncResponse = false; },
    
    sendSyncResponse: function(value) {
        value = value == null ? "0:" : new String(value).length + ":" + value;
		this._queueOutboundEvent(this._EVENT_SYNC_CALL, value);
		this._resetTimer(0);
        this._autoSyncResponse = true;
    },
    
    start: function() {
        this._comm = new tw_HttpRequest(this._inboundEventListener);
        this._outboundEvents = [];
        this._inboundEvents = [];
        this._vsEventOrder = {};
        this.sendGetEvents();
    },
    
    stop: function() {
        if (this._timerId != 0) clearTimeout(this._timerId);
        if (this._activityIndTimer != 0) clearTimeout(this._activityIndTimer);
        this._activityIndTimer = this._timerId = 0;
        if (this._comm != null) this._comm.abort();
        if (this._activityInd != null) document.body.removeChild(this._activityInd);
        this._outboundEvents = this._inboundEvents = this._vsEventOrder = this._comm = this._activityInd = null;
    }         
});

