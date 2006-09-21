/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
//Manages the communication of both outbound and inbound events
var tw_EventManager = Class.extend({    
    _EVENT_WEB_COMPONENT: 0,
    _EVENT_GET_EVENTS: 1,
    _EVENT_LOG_MESSAGE: 2, 
    _EVENT_SYNC_CALL: 3,
    _EVENT_RUN_TIMER: 4,
    _autoSyncResponse: true,
    _outboundEvents: null,
    _postOutboundEvents: false,
    _vsEventOrder: null,
    _inboundEvents: null,
    _activityInd: null,
    _activityIndTimer: 0,
    _timerId: 0,
    _comm: null,
    
    construct: function() {        
        this._eventLoop = this._eventLoop.bind(this);
        this._inboundEventListener = this._inboundEventListener.bind(this);
        this._hideActivityInd = this._hideActivityInd.bind(this);        
    },
    
    _resetTimer: function(time) {        
        clearTimeout(this._timerId);
        this._timerId = setTimeout(this._eventLoop, time);                    
    },
    
    _inboundEventListener: function(calls) {
        if (calls.length > 0) {
            try {
                eval("calls = " + calls);                
                this._inboundEvents = this._inboundEvents.concat(calls);                
                this._resetTimer(0);
            } catch (e) {
                alert("SYNTAX ERROR WITH eval(calls): " + calls);
            }
        }        
        
        this._setActivityIndVisible(false);
    },

    _setActivityIndVisible: function(state) {
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
        this._activityInd.style.display = "none";        
    },
        
    _eventLoop: function() {
        if (this._comm != null) {
            var timerTime = 100;
            
            if (this._postOutboundEvents && this._comm.isReady()) {
                this._setActivityIndVisible(true);
                this._postOutboundEvents = false;
                var msg = this._outboundEvents.join(":");                
                this._outboundEvents = [];
                this._vsEventOrder = {};
                msg = "data=" + encodeURIComponent(msg);
                this._inboundEventListener(this._comm.send("POST", tw_APP_URL, msg));
            }
            
            if (this._inboundEvents.length > 0) {
                var calls = this._inboundEvents;
                this._inboundEvents = [];
                
                for (var i = 0, cnt = calls.length; i < cnt; i++) {
                    var call = calls[i];            
                    var obj = call.i == undefined ? (call.n == undefined ? window : call.n) : tw_Component.instances[call.i];                    
                    
                    if (obj != null) {
                        if (call.s) this._autoSyncResponse = true; 
                        var ret = obj[call.m].apply(obj, call.a);            
                        
                        if (call.s && this._autoSyncResponse) {
                            this.sendSyncResponse(ret, true);
                            timerTime = 0;
                        }
                    }
                }            
            }
    
            this._resetTimer(timerTime);
        }        
    },
        
    _sendOutboundEvent: function(type, value) {
        this._queueOutboundEvent(type, value);
        this._postOutboundEvents = true;
        this._resetTimer(0);
    }, 
    
    _postOutboundEvent: function(type, value) {        
        this._queueOutboundEvent(type, value);
        this._postOutboundEvents = true;
    },
    
    _queueOutboundEvent: function(type, value) {
        this._outboundEvents.push(type);
        
        if (value == null) {
            this._outboundEvents.push(0);
            this._outboundEvents.push("");
        } else {
            this._outboundEvents.push(value.length);
            this._outboundEvents.push(value);
        }
    },
        
    manualSyncResponse: function() {
        this._autoSyncResponse = false;
    },
    
    sendViewStateChanged: function(id, name, value) {
        value = id + ":" + name + (value == null ? ":0:" : ":" + new String(value).length + ":" + value);
        this._sendOutboundEvent(this._EVENT_WEB_COMPONENT, value);
    },
        
    postViewStateChanged: function(id, name, value) {    
        value = id + ":" + name + (value == null ? ":0:" : ":" + new String(value).length + ":" + value);
        this._postOutboundEvent(this._EVENT_WEB_COMPONENT, value);
    },
    
    queueViewStateChanged: function(id, name, value) {
        value = id + ":" + name + (value == null ? ":0:" : ":" + new String(value).length + ":" + value);
        var key = id + ":" + name;
        var order = this._vsEventOrder[key];
                                        
        if (order == undefined) {            
            this._queueOutboundEvent(this._EVENT_WEB_COMPONENT, value);
            this._vsEventOrder[key] = this._outboundEvents.length - 2;            
        } else {
            this._outboundEvents[order] = value.length;
            this._outboundEvents[order + 1] = value;
        }                            
    },
        
    postLogMessage: function(levelName, msg) { this._postOutboundEvent(this._EVENT_LOG_MESSAGE, levelName + ":" + new String(msg).length + ":" + msg); },
    sendGetEvents: function() { this._sendOutboundEvent(this._EVENT_GET_EVENTS, ""); },    
    sendRunTimer: function(id) { this._sendOutboundEvent(this._EVENT_RUN_TIMER, id + ":"); },    
    
    sendSyncResponse: function(value, postOnly) {
        value = value == null ? "0:" : new String(value).length + ":" + value;
        
        if (postOnly) {
            this._postOutboundEvent(this._EVENT_SYNC_CALL, value);
        } else {
            this._sendOutboundEvent(this._EVENT_SYNC_CALL, value);
        }
    },
    
    start: function() {
        this._activityInd = document.createElement("img");
        this._activityInd.src = tw_EventManager.imageActivityInd;
        var s = this._activityInd.style;
        s.position = "absolute";
        s.right = "1px";
        s.top = "1px";
        s.display = "none";        
        document.body.appendChild(this._activityInd);                
        
        this._comm = new tw_HttpRequest(this._inboundEventListener);
        this._outboundEvents = [];
        this._inboundEvents = [];
        this._vsEventOrder = {};
        this._postOutboundEvents = false;
        this.sendGetEvents();
    },
    
    stop: function() {
        if (this._timerId != 0) clearTimeout(this._timerId);
        this._timerId = 0;
        if (this._comm != null) this._comm.abort();
        this._outboundEvents = null;
        this._inboundEvents = null;
        this._vsEventOrder = null;
        this._comm = null;
        document.body.removeChild(this._activityInd);
        this._activityInd = null;
    }         
});

tw_EventManager.imageActivityInd = "?_twr_=activityInd.gif";
