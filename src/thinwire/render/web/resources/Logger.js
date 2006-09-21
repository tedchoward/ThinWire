/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
function tw_Logger() {
    //
    //Private Fields
    //      
	var _logLevel = 0;
    var _DOMObjectIntervalId = 0;
	 
    //
    //Private Methods
    //      
    function _parseLevel(name) {
		name = name.toUpperCase();
	    var level = 0;	    
		if (name == "SEVERE") level = 1;
	    else if (name == "WARNING") level = 2;
	    else if (name == "INFO") level = 3;
	    else if (name == "CONFIG") level = 4;
	    else if (name == "FINE") level = 5;
	    else if (name == "FINER") level = 6;
	    else if (name == "FINEST") level = 7;	
	    return level;
	}
        
    //
    //Public Methods
    //        
	this.setLogLevel = function(levelName) {
		_logLevel = _parseLevel(levelName);
        
        if (this.isLoggable("FINE") && tw_isIE) {
            _DOMObjectIntervalId = setInterval(function() {
                document.title = document.all.length + " DOM Objects - " + new Date();
            }, 1000);
        } else if (_DOMObjectIntervalId != 0) {
            clearInterval(_DOMObjectIntervalId);
        }
	}
    
	this.isLoggable = function(levelName) { 
	    var level = _parseLevel(levelName);
	    return level != 0 && level <= _logLevel;
	}    
	
    this.log = function(levelName, msg) {
	    var level = _parseLevel(levelName);
	    if (level == 0 || level > _logLevel) return;
        tw_em.postLogMessage(levelName, msg);
	}
	
	this.severe = function(msg) { this.log("severe", msg); }
	this.warning = function(msg) { this.log("warning", msg); }
	this.info = function(msg) { this.log("info", msg); }
	this.config = function(msg) { this.log("config", msg); }
	this.fine = function(msg) { this.log("fine", msg); }
	this.finer = function(msg) { this.log("finer", msg); }
	this.finest = function(msg) { this.log("finest", msg); }
}

var tw_log = new tw_Logger();
